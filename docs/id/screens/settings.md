---
layout: screen
lang: id
base: "/id"
key: "screens/settings"
screen: settings
title: Pengaturan
---
# Pengaturan

**Apa ini.** Semua yang bukan data catatan: preferensi, sumber data, keamanan, dan folder. Dibuka dengan ikon roda gigi di bilah atas; di layar lebar, Pengaturan menjadi tab di panel navigasi samping.

**Preferensi.** Mata uang default dan tema (sistem, terang, gelap). Mata uang default dipakai untuk nilai di Ringkasan dan diusulkan untuk kantong dan tujuan baru; data yang sudah ada tetap memakai mata uangnya. **Bahasa** langsung mengganti bahasa antarmuka; *Default sistem* mengikuti perangkat.

**Penyedia data gratis.** Satu baris per jenis data, masing-masing dengan operator yang sedang dipakai: saldo BTC, ETH, TON, TRX; daftar token ETH, TON, TRX; harga kripto; kurs fiat. Ketuk baris untuk memilih operator lain, mengatur pencarian token ke **Nonaktif** untuk suatu jaringan, atau memasukkan kunci API opsional. Kunci disimpan terenkripsi di perangkat dan hanya dikirim ke operator yang menerbitkannya. **Uji sumber / perbarui portofolio** mengueri setiap operator dengan aset yang benar-benar Anda miliki dan melaporkan apa yang gagal. Tidak ada operator yang pernah diganti secara diam-diam.

**Harga dan waktu pembaruan.** Setiap harga tersimpan beserta waktu diamati dan diambilnya. Harga yang usang tetap dapat dipakai dan ditandai di Ringkasan.

**Keamanan.** **Enkripsi** mengenkripsi semua file di folder dengan kata sandi; menonaktifkannya akan mendekripsi file-file tersebut. Dengan enkripsi aktif, Anda dapat **Atur PIN**, mengaktifkan **Gunakan biometrik**, memilih **Kunci otomatis setelah di latar belakang**, dan **Ubah kata sandi**. Kata sandi tidak dapat dipulihkan. Sepuluh kali PIN salah akan menghapus PIN; kata sandi selalu berfungsi. Lihat [Keamanan]({{ page.base }}/manual#security).

**Penyimpanan.** Folder saat ini dan revisinya. **Hubungkan kembali / buka folder** menjalankan ulang pemilih folder; **Muat ulang file lokal** membaca ulang folder, misalnya setelah alat sinkronisasi Anda mengirimkan perubahan; **Ekspor cadangan** menulis satu file portabel (tanpa enkripsi jika enkripsi nonaktif, dan ditandai demikian); **Pulihkan cadangan** memvalidasi file sebelum mengganti data dan tetap menyimpan snapshot yang ada.

**Sumber / atribusi.** Tautan ke situs setiap operator.

**Legal.** Tautan ke [Kebijakan Privasi]({{ page.base }}/privacy), deklarasi [Keamanan data]({{ page.base }}/data-safety), dan [Fitur keuangan]({{ page.base }}/financial-features) di situs ini, dalam bahasa antarmuka. Versi aplikasi dan nomor build ada di bagian bawah.
