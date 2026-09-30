---
layout: default
lang: id
base: "/id"
key: "manual"
title: Panduan pengguna
class: doc
---
# Panduan pengguna

<p class="meta">Capital 2.2 · Android 8.0 dan yang lebih baru</p>

## Konsep dasar

Anda menyimpan uang di beberapa tempat: rekening tabungan, uang tunai, rekening efek, dompet kripto. Capital menyebut setiap tempat itu **kantong**. Anda ingin memakai uang itu untuk beberapa hal: dana darurat, liburan, laptop. Capital menyebut masing-masing hal itu **tujuan**. Anda menghubungkan kantong ke tujuan, dan aplikasi menghitung seberapa jauh setiap tujuan sudah terpenuhi dari apa yang Anda miliki hari ini. Tambahkan tabungan yang Anda **rencanakan**, dan aplikasi juga menunjukkan tanggal setiap tujuan terpenuhi.

Tidak ada apa pun di aplikasi ini yang memindahkan uang. Aplikasi ini hanya cermin dari apa yang Anda miliki dan kalkulator untuk apa yang bisa ditutupinya.

## Peluncuran pertama {#first-launch}

1. **Pilih folder.** Pilih folder khusus di perangkat, misalnya `Documents/Capital`. Di sinilah semua data ditulis. Folder yang sudah berisi data Capital langsung terbuka.
2. **Pengaturan → Mata uang default.** Total dan Ringkasan ditampilkan dalam mata uang ini.
3. Jika perlu, atur **kunci penyedia** di Pengaturan untuk operator yang memberikan batas lebih tinggi dengan kunci gratis (Alchemy, TronGrid, TON Center, CoinGecko). Setiap jaringan dan sumber harga punya pilihan default tanpa kunci.

## Kantong {#buckets}

Tab Kantong → **+**. Beri kantong nama dan mata uang. Buka kantong untuk menambahkan aset:

- **Aset manual**: nama, kode mata uang atau aset (EUR, USD, BTC, kode saham yang Anda nilai sendiri…) dan jumlah. Gunakan untuk saldo bank, uang tunai, dan apa pun yang tidak bisa dibaca aplikasi.
- **Aset dompet**: pilih jaringan (BTC, ETH, TON, TRX) dan tempel satu alamat publik. Saat diperbarui, aplikasi membaca saldo native dan, untuk ETH, TON, dan TRX, token fungible di alamat tersebut.

Jumlah dapat ditulis dengan titik atau koma desimal, tanpa pemisah ribuan. Setiap kantong menampilkan jumlah native dan nilainya dalam mata uang default Anda. Jika harga tidak tersedia, total ditandai belum lengkap; nilai tersimpan yang sudah usang tetap dapat dipakai dengan peringatan.

**Token.** Token dikenali dari alamat kontraknya, bukan dari namanya. Token hanya dihitung jika sumber harga pilihan Anda mencantumkan kontrak yang persis sama; token lainnya muncul sebagai *Token tidak dikenal · tidak dihitung* dan tidak masuk ke total. Buka editor aset dompet untuk memuat tokennya dan menonaktifkan token yang tidak Anda inginkan.

**Mode portofolio** (pengaturan kantong) memperlakukan kantong sebagai portofolio investasi: atur persentase target per aset, lihat porsi aktual dibandingkan target, dan gunakan **Seimbangkan ulang** untuk mendapatkan daftar apa yang perlu dibeli untuk jumlah tertentu. Penjualan hanya disarankan jika *Izinkan penjualan saat penyeimbangan ulang* aktif. Ini hanya kalkulator; tidak ada yang diubah.

## Tujuan {#goals}

Tab Tujuan → **+**. Tujuan memiliki nama, mata uang, jumlah target, dan tenggat. Buka tujuan lalu tekan **Hubungkan kantong** untuk menentukan kantong mana yang boleh mendanainya, dengan batas opsional: jumlah tetap, persentase dari kantong, atau persentase dari tujuan.

Cara uang dialokasikan:

- Tujuan dengan tenggat lebih awal didanai lebih dulu. Tujuan dengan tanggal yang sama didanai sesuai urutan yang ditampilkan; seret pegangan untuk mengubah urutannya.
- Kantong yang terhubung ke beberapa tujuan dibagi di antara tujuan-tujuan itu sesuai batasnya, dan tidak pernah dihitung dua kali.
- Hasilnya ditampilkan sebagai *terkumpul / target* dan *Masih dibutuhkan*. Di Ringkasan Anda melihat total, bagian yang dialokasikan ke tujuan, dan sisanya.

**Lencana.** *Terpenuhi* (hijau) jika tabungan hari ini sudah menutupi tujuan. *Akan terpenuhi tepat waktu* (hijau) jika rencana tabungan memenuhinya pada atau sebelum tenggat. *Belum terpenuhi* (kuning) jika tidak. Teks di bawah tujuan menunjukkan kapan tujuan terpenuhi atau berapa kekurangannya.

**Arsipkan** tujuan untuk menyimpannya tanpa ikut dihitung. Tujuan yang diarsipkan tercantum di bagian bawah.

## Rencana {#plans}

Tab Rencana → **+**. Rencana tabungan adalah jumlah yang ingin Anda tambahkan pada suatu tanggal, misalnya tabungan dari gaji di setiap akhir bulan. Rencana bukan bagian dari tabungan Anda; rencana hanya memperpanjang proyeksi: "Rencana tabungan memenuhi tujuan ini pada 30 Okt 2026 · tepat waktu".

Uang dari rencana diterapkan setelah kantong yang ada hari ini, ke tujuan sesuai urutan tenggat, sehingga hanya menambah bagian yang masih kurang. Setelah tanggal rencana terlewat, rencana pindah ke bagian **Diarsipkan** dan tidak lagi dihitung: entah Anda sudah memindahkan uangnya ke kantong dan aplikasi melihatnya di sana, atau rencana itu tidak terlaksana. Ubah tanggalnya ke masa depan untuk mengaktifkannya lagi; hapus jika sudah tidak relevan.

## Pembaruan

Ikon perbarui di bagian atas memuat ulang semua saldo dompet dan harga. Satu kantong juga dapat diperbarui sendiri. Aplikasi melakukan pembaruan sekali saat baru dibuka; kembali dari latar belakang hanya memuat ulang file lokal. Pembaruan memerlukan internet; tanpa internet, nilai sebelumnya tetap ada dan ditandai usang.

## Keamanan {#security}

Pengaturan → Keamanan.

- **Enkripsi** mengenkripsi semua file di folder, termasuk revisi lama, dengan kata sandi. Menonaktifkannya akan mendekripsi file-file tersebut. **Kata sandi tidak dapat dipulihkan**: jika kata sandi hilang, data tidak dapat dibuka. Cadangan tanpa enkripsi yang dibuat sebelum Anda mengaktifkan enkripsi tetap dapat dibaca; aplikasi memperingatkan tentang cadangan itu tetapi tidak dapat menghapusnya.
- **PIN** dan **biometrik** tersedia saat enkripsi aktif. *Gunakan kata sandi* selalu tersedia di layar PIN. Setelah 10 kali PIN salah, PIN dihapus dan hanya kata sandi yang berfungsi. Salah memasukkan PIN tidak pernah menghapus data.
- Selama enkripsi aktif, tangkapan layar dan pratinjau aplikasi terbaru diblokir.

## Sinkronisasi, cadangan, pemulihan {#sync-backup-recovery}

Capital menulis file snapshot dengan ID revisi dan checksum ke folder Anda dan tidak pernah menjalankan sinkronisasinya sendiri. Letakkan folder di bawah alat sinkronisasi apa pun yang sudah Anda pakai. Jika dua perangkat mengedit pada waktu yang sama, aplikasi menampilkan layar konflik dan membiarkan Anda memilih versi; kedua versi asli tetap ada di penyimpanan.

- **Ekspor cadangan** (Pengaturan) menulis satu file portabel. **Pulihkan** memvalidasi file itu sebelum ada yang diubah.
- Jika penyimpanan gagal, perubahan Anda tetap di memori dengan pilihan *Coba simpan lagi* dan *Simpan salinan ke folder*.
- Jika izin akses folder hilang, hubungkan kembali folder yang sama.
- File yang ditulis oleh versi aplikasi yang lebih baru ditolak oleh versi yang lebih lama; perbarui aplikasi.

## Bahasa {#language}

Aplikasi dimulai dalam bahasa perangkat jika bahasa itu termasuk 15 bahasa yang didukung, jika tidak dalam bahasa Inggris. Ubah di Pengaturan → Bahasa.

## Memasang di luar Google Play

Unduh APK dari [rilis terbaru]({{ site.repo }}/releases/latest) lalu buka; izinkan pemasangan dari sumber tersebut saat Android memintanya. Setiap rilis ditandatangani dengan kunci yang sama, sehingga versi baru terpasang di atas versi lama dan pengaturan Anda tetap ada. Folder berisi data Anda tidak pernah disentuh oleh pembaruan maupun pencopotan aplikasi.
