---
layout: screen
lang: id
base: "/id"
key: "screens/brokers"
screen: brokers
title: Broker
---
# Broker

**Apa ini.** Koneksi hanya-baca Anda ke akun efek dan forex. Akun broker terdiri dari broker, ID akun atau ID query, dan kredensial broker; saat diperbarui, aplikasi membaca total nilai akun dalam mata uang dasarnya. Akun berada di layar ini, bukan di dalam kantong: kantong hanya menautkan ke akun, dan kantong tetap menjadi tempat tabungan Anda dihitung.

**Isi setiap baris.** Nama akun, broker dan ID-nya, nilai terakhir yang dibaca dalam mata uang akun dan mata uang default Anda, kapan nilainya diamati dan kapan diambil, serta kantong tempat akun terhubung: **Terhubung ke …** membuka kantong itu, *Belum terhubung ke kantong* berarti belum ada yang menghitungnya. **Ubah** mengganti nama, broker, atau ID; **Hapus** menghapus akun dan, bila akun tertaut, juga aset yang menautkannya.

**Menambah akun.** Tekan **+**, masukkan nama, pilih broker, lalu masukkan ID yang dipakai broker itu: Flex Query id untuk Interactive Brokers, ID akun untuk OANDA, nomor akun untuk Trading 212. Untuk SnapTrade tekan **Hubungkan broker melalui SnapTrade**, kembali ke aplikasi, tekan **Muat akun**, lalu pilih satu akun. Simpan. Mata uang dan nilainya muncul setelah pembaruan berikutnya.

**Abaikan saldo kurang dari.** Centang dan masukkan jumlah dalam mata uang default Anda (default 1) agar sisa receh tidak masuk tabungan: bila nilai akun, dikonversi dengan kurs tersimpan, di bawah jumlah itu, aset yang tertaut dihitung 0 dan barisnya menampilkan *Dihitung 0: di bawah …*. Nilai sebenarnya tetap terlihat di layar ini. Tanpa kurs untuk mata uang akun, tidak ada yang diabaikan.

**Menautkan ke kantong.** Buka kantong, tekan **Tambah aset**, atur **Pelacakan** ke **Akun broker**, lalu pilih akunnya; kosongkan nama untuk memakai nama akun. Satu akun hanya bisa berada di satu kantong pada satu waktu. **Ubah / pindahkan** pada aset memindahkannya ke kantong lain; menghapus aset memutus tautan akun tanpa menghapusnya.

**Kredensial.** Token atau kunci setiap broker yang didukung; satu set per broker berlaku untuk semua akun broker itu. Kredensial dienkripsi dengan kunci yang disimpan di Android Keystore, tidak pernah ditulis ke folder data, tidak disertakan dalam ekspor maupun cadangan sistem, dan hanya dikirim ke broker yang menerbitkannya. **Panduan penyiapan akun broker** membuka [Akun broker dan forex]({{ page.base }}/accounts), yang berisi langkah untuk setiap broker.

**Perbarui.** Ikon perbarui di layar ini membaca semua akun; pembaruan pada kantong hanya membaca akun yang tertaut ke kantong itu. Akun yang tidak dapat dibaca mempertahankan nilai terakhirnya dan menampilkan pesan dari broker di bawah barisnya. Aplikasi hanya membaca: tidak pernah membuat order atau memindahkan uang.
