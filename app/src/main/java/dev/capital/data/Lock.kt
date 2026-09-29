package dev.capital.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** null = acceptable. Digits only, 4 to 12. */
fun validPin(pin: CharArray): String? = when {
    pin.any { it !in '0'..'9' } -> "PIN must contain digits only"
    pin.size < 4 || pin.size > 12 -> "PIN must be 4 to 12 digits"
    else -> null
}
/** Waiting time after the n-th wrong PIN. */
fun waitMillis(failures: Int): Long = when {
    failures < 5 -> 0L
    failures == 5 -> 30_000L
    failures == 6 -> 60_000L
    failures == 7 -> 300_000L
    failures == 8 -> 900_000L
    else -> 3_600_000L
}
fun formatWait(millis: Long): String { val s=(millis+999)/1000; return "%02d:%02d".format(s/60,s%60) }

/** Device-bound copy of the data key, guarded by a PIN and optionally biometry. Never touches data files. */
class Lock(context: Context) {
    private val prefs=context.getSharedPreferences("lock",Context.MODE_PRIVATE)
    private val rng=SecureRandom()
    private val store get() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    val hasPin: Boolean get() = prefs.contains("pin")
    val biometricEnabled: Boolean get() = prefs.contains("bio")
    val failures: Int get() = prefs.getInt("failures",0)
    fun blockedForMillis(now: Long = System.currentTimeMillis()): Long = (prefs.getLong("blockedUntil",0)-now).coerceAtLeast(0)
    var lockAfterSeconds: Int
        get() = prefs.getInt("lockAfter",60)
        set(value) { prefs.edit().putInt("lockAfter",value).apply() }

    private fun b64(bytes: ByteArray) = Base64.encodeToString(bytes,Base64.NO_WRAP)
    private fun unb64(text: String) = Base64.decode(text,Base64.NO_WRAP)
    private fun keystoreKey(alias: String,configure: KeyGenParameterSpec.Builder.()->Unit = {}): SecretKey {
        (store.getKey(alias,null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).apply(configure).build())
        }.generateKey()
    }
    private fun gcm(mode: Int,key: java.security.Key,nonce: ByteArray?,input: ByteArray): Pair<ByteArray,ByteArray> =
        Cipher.getInstance("AES/GCM/NoPadding").apply { if(nonce==null) init(mode,key) else init(mode,key,GCMParameterSpec(128,nonce)) }.let { it.iv to it.doFinal(input) }

    fun setPin(pin: CharArray,key: DataKey) {
        validPin(pin)?.let { throw IllegalArgumentException(it) }
        val kdf=Kdf(m=19456,t=2,p=1,salt=b64(ByteArray(16).also { rng.nextBytes(it) }))
        val pk=derive(pin,kdf); val data=key.bytes()
        try {
            val (n1,c1)=gcm(Cipher.ENCRYPT_MODE,SecretKeySpec(pk,"AES"),null,data)
            val (n2,c2)=gcm(Cipher.ENCRYPT_MODE,keystoreKey("capital-lock"),null,n1+c1)
            check(prefs.edit().putString("pin",b64(n2+c2)).putString("salt",kdf.salt).putInt("m",kdf.m).putInt("t",kdf.t).putInt("p",kdf.p).putInt("failures",0).putLong("blockedUntil",0).commit()) { "Could not save PIN" }
        } finally { pk.fill(0); data.fill(0) }
    }
    fun removePin() { disableBiometric(); prefs.edit().remove("pin").remove("salt").remove("m").remove("t").remove("p").remove("failures").remove("blockedUntil").commit() }
    /** null = wrong PIN. Throws with a readable message when blocked or no PIN. */
    fun unlockWithPin(pin: CharArray): DataKey? {
        val blob=prefs.getString("pin",null) ?: throw IllegalStateException("No PIN set. Use your password.")
        val wait=blockedForMillis()
        if(wait>0) throw IllegalStateException("Too many wrong entries. Try again in ${formatWait(wait)}")
        // ponytail: wall-clock waiting time; changing the device clock can shorten it. The Keystore binding and the 10-failure wipe remain.
        val n=failures+1
        check(prefs.edit().putInt("failures",n).putLong("blockedUntil",System.currentTimeMillis()+waitMillis(n)).commit()) { "Could not record the attempt" }
        val outer=try { val raw=unb64(blob); gcm(Cipher.DECRYPT_MODE,keystoreKey("capital-lock"),raw.copyOfRange(0,12),raw.copyOfRange(12,raw.size)).second }
            catch(_: GeneralSecurityException) { clear(); throw IllegalStateException("Device key unavailable. Use your password.") }
        val pk=derive(pin,Kdf(m=prefs.getInt("m",19456),t=prefs.getInt("t",2),p=prefs.getInt("p",1),salt=prefs.getString("salt",null).orEmpty()))
        try {
            val data=try { gcm(Cipher.DECRYPT_MODE,SecretKeySpec(pk,"AES"),outer.copyOfRange(0,12),outer.copyOfRange(12,outer.size)).second }
                catch(_: GeneralSecurityException) { if(n>=10) clear(); return null }
            try { resetFailures(); return DataKey(data) } finally { data.fill(0) }
        } finally { pk.fill(0); outer.fill(0) }
    }
    /** After a key change the old copies are useless: drop them and let the user set the PIN again. */
    fun rewrap(key: DataKey,pin: CharArray? = null) { if(pin==null) clear() else setPin(pin,key) }
    fun clear() {
        removePin()
        runCatching { store.deleteEntry("capital-lock") }
    }
    fun resetFailures() { prefs.edit().putInt("failures",0).putLong("blockedUntil",0).commit() }

    fun canUseBiometrics(context: Context): Boolean = BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)==BiometricManager.BIOMETRIC_SUCCESS
    fun biometricEncryptCipher(): Cipher {
        runCatching { store.deleteEntry("capital-bio") }
        val key=keystoreKey("capital-bio") { setUserAuthenticationRequired(true); setInvalidatedByBiometricEnrollment(true) }
        return Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE,key) }
    }
    fun storeBiometric(cipher: Cipher,key: DataKey) {
        val data=key.bytes()
        try {
            val blob=cipher.doFinal(data)
            check(prefs.edit().putString("bioIv",b64(cipher.iv)).putString("bio",b64(blob)).commit()) { "Could not save biometric unlock" }
        } finally { data.fill(0) }
    }
    fun biometricDecryptCipher(): Cipher? {
        val iv=prefs.getString("bioIv",null); if(iv==null || !prefs.contains("bio")) return null
        return try {
            val key=store.getKey("capital-bio",null) as? SecretKey ?: throw KeyPermanentlyInvalidatedException()
            Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE,key,GCMParameterSpec(128,unb64(iv))) }
        } catch(_: KeyPermanentlyInvalidatedException) { disableBiometric(); null }
        catch(_: GeneralSecurityException) { disableBiometric(); null }
    }
    fun openBiometric(cipher: Cipher): DataKey {
        val data=cipher.doFinal(unb64(prefs.getString("bio",null) ?: throw IllegalStateException("Biometric unlock is off")))
        try { return DataKey(data) } finally { data.fill(0) }
    }
    fun disableBiometric() {
        prefs.edit().remove("bio").remove("bioIv").commit()
        runCatching { store.deleteEntry("capital-bio") }
    }
}
