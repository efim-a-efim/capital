package dev.capital.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dev.capital.domain.tr
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class Secrets(context: Context) {
    private val prefs=context.getSharedPreferences("provider-secrets",Context.MODE_PRIVATE)
    private val alias="capital-provider-keys"
    private fun key(): SecretKey {
        val store=KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return (store.getKey(alias,null) as? SecretKey) ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    fun get(provider: String): String {
        val saved=prefs.getString(provider,null) ?: return ""
        return runCatching {
            val bytes=Base64.decode(saved,Base64.NO_WRAP)
            val cipher=Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,bytes.copyOfRange(0,12)))
            cipher.doFinal(bytes.copyOfRange(12,bytes.size)).toString(Charsets.UTF_8)
        }.getOrElse { throw KeyState(tr("Cannot unlock {0} key. Re-enter it in Settings.",provider)) }
    }
    fun put(provider: String, value: String) {
        if(value.isBlank()) { prefs.edit().remove(provider).apply(); return }
        if(!(value.length <= 1024 && !value.contains('\n'))) throw KeyArgument(tr("Invalid API key"))
        val cipher=Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE,key())
        val bytes=cipher.iv+cipher.doFinal(value.trim().toByteArray())
        if(!prefs.edit().putString(provider,Base64.encodeToString(bytes,Base64.NO_WRAP)).commit()) throw KeyState(tr("Could not save API key"))
    }
}
