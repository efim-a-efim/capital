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
- **Akun broker**: pilih broker (Interactive Brokers, OANDA, Trading 212, atau SnapTrade) dan masukkan ID akun atau ID query; untuk SnapTrade, **Muat akun** menampilkan daftar akun terhubung untuk dipilih. Saat diperbarui, aplikasi membaca total nilai akun dalam mata uang dasar akun; token akses dimasukkan di Pengaturan → Akun broker. **Panduan penyiapan akun broker** membuka [Akun broker dan forex]({{ page.base }}/accounts), yang berisi langkah untuk setiap broker.

**Token dan "tidak dihitung".** Token dikenali dari alamat kontraknya. Token hanya dihitung jika sumber harga Anda mencantumkan kontrak yang persis sama; jika tidak, token ditampilkan sebagai *Token tidak dikenal · tidak dihitung* dan tidak masuk ke total. Inilah yang mencegah "USDT" palsu hasil airdrop masuk ke tabungan Anda.

**Mode portofolio.** Jika aktif, layar menambahkan tabel berisi nilai, porsi aktual, target, dan selisih per aset, serta tombol **Seimbangkan ulang** yang meminta jumlah dan mencantumkan apa yang perlu dibeli. Penjualan hanya muncul jika *Izinkan penjualan saat penyeimbangan ulang* aktif. Tidak ada transaksi yang dilakukan.
