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
3. Jika perlu, atur **kunci penyedia** di Pengaturan untuk operator yang memberikan batas lebih tinggi dengan kunci gratis (Alchemy, TronGrid, TON Center, CoinGecko). Setiap jaringan dan sumber harga punya pilihan default tanpa kunci. Akun broker memerlukan **token akses** dari broker, dimasukkan di tab Broker; lihat [Akun broker dan forex]({{ page.base }}/accounts).

## Kantong {#buckets}

Tab Kantong → **+**. Beri kantong nama dan mata uang. Buka kantong untuk menambahkan aset:

- **Aset manual**: nama, kode mata uang atau aset (EUR, USD, BTC, kode saham yang Anda nilai sendiri…) dan jumlah. Gunakan untuk saldo bank, uang tunai, dan apa pun yang tidak bisa dibaca aplikasi.
- **Aset dompet**: pilih jaringan (BTC, ETH, TON, TRX) dan tempel satu alamat publik. Saat diperbarui, aplikasi membaca saldo native dan, untuk ETH, TON, dan TRX, token fungible di alamat tersebut.
- **Akun broker**: pilih salah satu akun yang ditambahkan di tab Broker. Saat diperbarui, aplikasi membaca total nilai akun dalam mata uang dasarnya. Satu akun dapat ditautkan ke satu kantong.

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

## FIRE {#fire}

Tujuan berjenis **FIRE** menunjukkan kapan tabungan Anda dapat mulai membiayai hidup Anda dan berapa lama tabungan itu bertahan. Ini adalah hitungan atas angka Anda sendiri; tidak merekomendasikan apa pun. Layarnya dijelaskan di [Tujuan]({{ page.base }}/screens/goal#fire).

**Menyiapkan.**

1. Tab Tujuan → **+**, atur **Jenis tujuan** ke **FIRE**, lalu isi nama dan mata uang.
2. Masukkan yang Anda ketahui, dalam nilai uang hari ini: **Tanggal lahir**, **Tanggal FIRE**, **Saya bisa berinvestasi** (per minggu, 2 minggu, bulan, tahun, atau N hari), **Pengeluaran per bulan setelah FIRE**, **Dana yang dimiliki setelah FIRE**. Kosongkan yang ingin dihitung aplikasi.
3. Buka tujuan dan **Hubungkan kantong** untuk tabungan yang sudah Anda miliki, seperti pada tujuan lain.

**Cara membacanya.** Tabel memiliki tiga kolom, **Rata-rata**, **Optimis**, dan **Pesimis**, untuk tiga set suku pertumbuhan dan inflasi. **Uang bertahan** menyatakan hingga tanggal dan usia berapa uang menutupi pengeluaran; *melampaui harapan hidup* dan *uang tidak akan habis 🥳* adalah kasus yang baik; *kurang …* berarti tanggalnya tidak tercapai. Grafik menunjukkan uang dalam nilai uang hari ini, naik hingga tanggal FIRE yang bergaris putus-putus dan turun setelahnya. Anggap ketiga kurva sebagai rentang, bukan ramalan.

**Asumsi.** Kontribusi dan pengeluaran mempertahankan daya belinya (naik mengikuti inflasi). Pajak, pensiun, tunjangan, dan penghasilan setelah FIRE tidak dimodelkan. Bawaan: pertumbuhan 7 % (10 % optimis, 4 % pesimis), inflasi 3 % (2 %, 5 %), tingkat penarikan 4 %, harapan hidup 95. Ubah salah satunya di **Asumsi lanjutan**; nilai Anda selalu diutamakan.

**Sumber nilai bawaan.** UBS Global Investment Returns Yearbook 2026 (dunia, 1900–2024: saham 9,8 % per tahun nominal, obligasi 4,6 %, inflasi 2,9 %; 7 % kira-kira setara campuran 60/40); target inflasi 2 % bank sentral; Bengen (1994) dan studi Trinity (1998) untuk aturan penarikan 4 %.

**Suku langsung.** Pengaturan → Penyedia data gratis → **Inflasi** (**World Bank**, **ECB**, **OECD**, **Nonaktif**) dan **Pertumbuhan pasar** (**World Bank**, **Yahoo Finance · tidak resmi**, **Nonaktif**). **Perbarui** mengambilnya untuk mata uang setiap tujuan FIRE. Inflasi: harga konsumen World Bank (tahunan, per negara atau rata-rata dunia), HICP kawasan euro dari ECB (hanya EUR), OECD (negara anggota, bukan RUB). Pertumbuhan: perubahan indeks saham S&P Global dari World Bank per negara (EUR memakai indeks AS), atau penutupan bulanan 30 tahun dari endpoint tidak resmi Yahoo Finance (^GSPC, ^GDAXI, ^FTSE, ^N225, ^BSESN, ^BVSP, 000001.SS; mata uang lain memakai ^GSPC). Dari seri tersebut: inflasi rata-rata adalah rerata 20 tahun terakhir, optimis dan pesimis adalah rerata 5 tahun terendah dan tertinggi; pertumbuhan rata-rata adalah laju tahunan majemuk hingga 30 tahun, optimis dan pesimis adalah laju 10 tahun terbaik dan terburuk. Kedua sumber pertumbuhan adalah indeks harga tanpa dividen, sehingga menaksir terlalu rendah total imbal hasil. **Nonaktif** mempertahankan bawaan aplikasi. Pengambilan yang gagal mempertahankan seri sebelumnya dan menampilkan galatnya.

## Rencana {#plans}

Tab Rencana → **+**. Rencana tabungan adalah jumlah yang ingin Anda tambahkan pada suatu tanggal, misalnya tabungan dari gaji di setiap akhir bulan. Rencana bukan bagian dari tabungan Anda; rencana hanya memperpanjang proyeksi: "Rencana tabungan memenuhi tujuan ini pada 30 Okt 2026 · tepat waktu".

Uang dari rencana diterapkan setelah kantong yang ada hari ini, ke tujuan sesuai urutan tenggat, sehingga hanya menambah bagian yang masih kurang. Setelah tanggal rencana terlewat, rencana pindah ke bagian **Diarsipkan** dan tidak lagi dihitung: entah Anda sudah memindahkan uangnya ke kantong dan aplikasi melihatnya di sana, atau rencana itu tidak terlaksana. Ubah tanggalnya ke masa depan untuk mengaktifkannya lagi; hapus jika sudah tidak relevan.

## Broker

Tab Broker → **+**. Akun broker adalah koneksi hanya-baca: pilih broker (daftarnya ada di [Akun broker dan forex]({{ page.base }}/accounts)), masukkan ID akun atau ID query, lalu simpan; token atau kunci broker dimasukkan sekali di bagian **Kredensial** pada tab yang sama dan berlaku untuk semua akun broker tersebut. Tab ini mencantumkan setiap akun beserta nilai terakhirnya dan kantong tempat akun itu terhubung. **Abaikan saldo kurang dari** jumlah dalam mata uang default Anda (default 1) membuat saldo receh dihitung 0 di kantong. Hubungkan akun ke kantong lewat **Tambah aset → Akun broker**; hapus aset untuk memutus hubungan, hapus akun untuk menghapus koneksi. Langkah penyiapan, dengan tautan ke dokumentasi resmi para broker, ada di [Akun broker dan forex]({{ page.base }}/accounts).

## Pembaruan

Ikon perbarui di bagian atas memuat ulang semua saldo dompet, nilai akun broker, dan harga. Satu kantong juga dapat diperbarui sendiri. Aplikasi melakukan pembaruan sekali saat baru dibuka; kembali dari latar belakang hanya memuat ulang file lokal. Pembaruan memerlukan internet; tanpa internet, nilai sebelumnya tetap ada dan ditandai usang.

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
