---
layout: screen
lang: id
base: "/id"
key: "screens/bucket"
screen: bucket
title: Kantong
---
# Kantong

**Apa ini.** Satu kantong beserta asetnya. Dibuka dengan mengetuk kartu di tab [Kantong]({{ page.base }}/screens/buckets); **← Semua kantong** untuk kembali.

**Header.** Nilai kantong, lalu dua angka yang hanya bermakna jika dilihat bersama: **Dialokasikan**, bagian yang diklaim oleh tujuan yang terhubung, dan **Tersedia**, sisanya. **Ubah kantong** membuka nama, mata uang, dan sakelar portofolio. **Hapus kantong** menghapus kantong beserta asetnya setelah konfirmasi.

**Aset.** Setiap aset menampilkan nama, nilainya dalam mata uang kantong, cara pelacakannya (*Manual*, *Dompet*, atau *Akun broker*), jumlah native, kapan nilainya diamati, dan kapan terakhir diambil. **Ubah / pindahkan** mengubah aset atau memindahkannya ke kantong lain; **Hapus** menghapusnya.

**Tambah aset** membuka editor aset:

- **Manual**: nama, kode mata uang atau aset, dan jumlah. Gunakan untuk apa pun yang tidak bisa dibaca aplikasi.
- **Dompet**: pilih jaringan (BTC, ETH, TON, TRX) dan tempel satu alamat publik. Aplikasi membaca saldo native saat pembaruan dan, di ETH, TON, dan TRX, token fungible di alamat itu. Buka editor lagi dan tekan **Muat token** untuk melihatnya dan menonaktifkan token yang tidak ingin Anda hitung.
- **Akun broker**: pilih salah satu akun yang ditambahkan di layar [Broker]({{ page.base }}/screens/brokers); kosongkan nama untuk memakai nama akun. Saat diperbarui, aplikasi membaca total nilai akun dalam mata uang dasar akun. Satu akun dapat ditautkan ke satu kantong; menghapus aset akan memutus tautan tanpa menghapus akun.

**Token dan "tidak dihitung".** Token dikenali dari alamat kontraknya. Token hanya dihitung jika sumber harga Anda mencantumkan kontrak yang persis sama; jika tidak, token ditampilkan sebagai *Token tidak dikenal · tidak dihitung* dan tidak masuk ke total. Inilah yang mencegah "USDT" palsu hasil airdrop masuk ke tabungan Anda.

**PnL investasi.** Aktifkan di **Ubah kantong** (kotak centang **PnL investasi**). Aplikasi lalu mencatat, pada setiap pembaruan, jumlah setiap aset serta setoran dan penarikan akun broker-nya, dan layar mendapat bagian **Investasi**:

- Lima baris, **Sepanjang waktu**, **Setahun terakhir**, **Sebulan terakhir**, **Seminggu terakhir** dan **Sehari terakhir**, masing-masing dengan laba atau rugi dalam mata uang kantong dan imbal hasil tertimbang waktu dalam persen, hijau jika positif dan merah jika negatif. Sebagai gantinya, baris menampilkan *sejak <tanggal>* jika riwayat dimulai setelah periode itu berawal.
- Grafik **Nilai** untuk nilai kantong dan grafik **Laba rugi** untuk hasil kumulatif, keduanya dalam mata uang kantong. Chip **Semua**, **1Y**, **1M**, **1W** dan **1D** memfokuskan kedua grafik pada rentang waktu itu; kurva PnL mulai lagi dari nol di awal rentang.
- *Sejak <tanggal> · dalam <mata uang>* menunjukkan kapan riwayat dimulai. *Beberapa hari tidak memiliki kurs; nilainya memakai kurs terakhir yang diketahui* muncul jika riwayat kurs memiliki celah.

**Cara penghitungan.** Laba atau rugi = perubahan nilai dikurangi setoran ditambah penarikan. Persentasenya tertimbang waktu: imbal hasil setiap hari dirangkai, sehingga waktu setoran Anda tidak memengaruhinya. Setoran dan penarikan untuk akun yang tertaut berasal dari broker ([broker mana saja]({{ page.base }}/accounts#pnl)); untuk aset manual dan dompet, perubahan jumlah dihitung sebagai setoran atau penarikan pada kurs hari itu, bukan sebagai laba. Nilai masa lalu memakai riwayat kurs harian (**Pengaturan → Riwayat kurs**). Mengaktifkan sakelar ini membangun riwayat dari snapshot yang sudah ada di folder Anda dan dari broker (*Membangun riwayat PnL…*, lalu *Riwayat PnL siap: N hari*); sebelum pembaruan pertama, bagian ini menampilkan *Belum ada riwayat*.

**Mode portofolio.** Jika aktif, layar menambahkan tabel berisi nilai, porsi aktual, target, dan selisih per aset, serta tombol **Seimbangkan ulang** yang meminta jumlah dan mencantumkan apa yang perlu dibeli. Penjualan hanya muncul jika *Izinkan penjualan saat penyeimbangan ulang* aktif. Tidak ada transaksi yang dilakukan.
