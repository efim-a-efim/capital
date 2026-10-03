---
layout: default
lang: id
base: "/id"
key: "accounts"
title: Akun broker dan forex
class: doc
---
# Akun broker dan forex

Capital dapat membaca total nilai akun efek atau forex dengan cara yang sama seperti membaca dompet kripto. Anda menambahkan akun ke sebuah kantong sebagai aset bertipe **Akun broker**, dan setiap pembaruan mengambil net asset value (NAV) akun dalam mata uang dasar akun. Aplikasi hanya membaca: aplikasi memakai antarmuka pelaporan broker dengan token yang Anda buat sendiri, tidak pernah membuat, mengubah, atau membatalkan order, dan tidak pernah memindahkan uang.

Capital hanya terhubung ke antarmuka yang kredensialnya berumur panjang: token atau kunci yang Anda buat sekali dan tetap berlaku sampai Anda mencabutnya (atau, untuk Interactive Brokers, sampai kedaluwarsa yang Anda pilih, hingga satu tahun). Yang didukung saat ini:

| Broker | Antarmuka yang dipakai | Yang dibaca |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (hanya pengambilan laporan) | Net asset value pada hari bursa terakhir, dalam mata uang dasar akun |
| [OANDA](#oanda) | v20 REST API, akun live fxTrade | Net asset value pada saat pembaruan, dalam mata uang akun |
| [Trading 212](#trading-212) | Public API, akun Invest dan Stocks ISA | Total nilai akun pada saat pembaruan, dalam mata uang utama akun |
| [SnapTrade](#snaptrade) | SnapTrade Personal, agregator yang mencakup banyak broker | Total nilai akun sebagaimana dilaporkan broker ke SnapTrade, dalam mata uang akun |

## Sebelum mulai {#before-you-start}

- **Apa yang keluar dari perangkat.** Pada setiap pembaruan, aplikasi mengirim token akses dan ID akun atau ID query ke broker tersebut melalui HTTPS. Broker melihat alamat IP Anda, seperti pada permintaan internet mana pun.
- **Tempat kredensial disimpan.** Pengaturan → Akun broker. Kredensial dienkripsi dengan kunci yang disimpan di Android Keystore, tidak pernah ditulis ke folder data Anda, dan tidak disertakan dalam ekspor maupun cadangan sistem. Satu set kredensial per broker mencakup semua akun yang Anda tambahkan untuk broker itu.
- **Yang disimpan di folder Anda.** ID akun, nilai terakhir yang dibaca, dan waktu pembacaannya. Tidak ada yang lain dari broker.
- **Tampilan situs broker dapat berubah.** Langkah di bawah ini sesuai dengan situs web broker per Oktober 2026. Broker sesekali mengganti nama menu dan memindahkan pengaturan, jadi sebuah langkah mungkin tampak sedikit berbeda saat Anda mengikutinya. Dokumentasi resmi broker, yang ditautkan di setiap bagian, adalah sumber yang berwenang: jika sebuah langkah di sini tidak lagi sesuai, cari istilah yang sama di halaman broker.

## Interactive Brokers {#interactive-brokers}

Capital memakai **Flex Web Service**, antarmuka Interactive Brokers untuk mengambil laporan yang sudah dikonfigurasi. Token yang dipakainya hanya dapat membuat dan mengunduh laporan; token itu tidak dapat dipakai untuk login, trading, atau penarikan dana. Capital meminta **Net Asset Value (NAV) Summary in Base** dari sebuah Activity Flex Query dan mengambil total pada tanggal laporan terbaru, sehingga nilainya adalah penutupan hari bursa terakhir.

### 1. Buat Flex Query

1. Login ke [Client Portal](https://www.interactivebrokers.com/portal) dan buka **Performance & Reports → Flex Queries** (Kinerja & Laporan; pada sebagian akun menunya bernama *Reporting*).
2. Di bawah **Activity Flex Query** tekan **+** (Create, buat). Beri nama query, misalnya `Capital`.
3. Di daftar **Sections** aktifkan tepat dua bagian dan kolom berikut (memilih semua kolom suatu bagian juga bisa):
   - **Account Information**: *Account ID*, *Currency*.
   - **Net Asset Value (NAV) Summary in Base**: *Report Date*, *Total*.
4. Di **Delivery Configuration** atur **Format** ke `XML` dan **Period** ke `Last Business Day`. Opsi lainnya boleh memakai nilai default.
5. Simpan query, lalu tekan ikon **i** (informasi) di sebelahnya dan catat **Query ID**, sebuah angka.

Query harus mencakup satu akun. Jika Anda memiliki akun tertaut atau struktur advisor, buat satu query per akun dan pilih hanya akun itu saat membuatnya.

### 2. Aktifkan Flex Web Service dan buat token

1. Di halaman **Flex Queries** yang sama, buka **Flex Web Service Configuration**.
2. Aktifkan **Flex Web Service Status** dan simpan. Sebuah token dibuat.
3. Untuk menentukan berapa lama token berlaku, tekan **Generate New Token** (buat token baru): dari 6 jam hingga 1 tahun. Biarkan **Valid for IP address** kosong untuk ponsel, yang alamat IP-nya berganti. Membuat token baru membatalkan token sebelumnya.
4. Salin token.

### 3. Hubungkan di Capital

1. **Pengaturan → Akun broker → Token akses: Interactive Brokers**, tempel token dan simpan.
2. Buka kantong, **Tambah aset**, atur **Pelacakan** ke **Akun broker**, **Broker** ke Interactive Brokers, masukkan **Flex Query id** dan simpan.
3. Tekan **Perbarui**. Proses pertama memakan waktu hingga setengah menit karena laporan dibuat berdasarkan permintaan.

Jika token kedaluwarsa, pembaruan melaporkan *Token sudah kedaluwarsa; buat token baru di Client Portal*: buat token baru dan tempel di Pengaturan. Interactive Brokers mengizinkan satu permintaan laporan per detik dan sepuluh per menit dari satu token, batas yang tidak pernah dilampaui oleh satu pembaruan.

Dokumentasi Interactive Brokers: [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [Enable and create the access token](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Create a Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query reference](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capital memanggil **account summary** dari OANDA v20 REST API dan menyimpan NAV akun (saldo ditambah untung atau rugi yang belum direalisasi) dalam mata uang akun. Hanya akun live **fxTrade** yang didukung; akun latihan bukan tabungan.

**Personal access token OANDA tidak bersifat hanya-baca.** Token itu memberi akses API penuh ke setiap sub-akun login Anda, termasuk trading. Capital hanya memanggil account summary, tetapi siapa pun yang memperoleh token itu dapat melakukan trading dengannya. Perlakukan seperti kata sandi: tempel hanya di Capital, dan cabut di portal OANDA jika ponsel Anda hilang.

### 1. Buat token

1. Login ke portal manajemen akun fxTrade OANDA Anda.
2. Buka **My Services → Manage API Access** (Layanan Saya → Kelola Akses API; di portal lama: *My Account → My Services → Manage API Access*).
3. Setujui lisensi API dan tekan **Generate**. Salin token; OANDA tidak menampilkannya lagi. Jika hilang, cabut di sana dan buat yang baru.

### 2. Temukan ID akun

ID akun v20 berbentuk `001-001-1234567-001`, dengan tanda hubung. ID itu tercantum di portal yang sama di samping setiap sub-akun, dan di platform fxTrade pada rincian akun.

### 3. Hubungkan di Capital

1. **Pengaturan → Akun broker → Token akses: OANDA**, tempel token dan simpan.
2. Buka kantong, **Tambah aset**, atur **Pelacakan** ke **Akun broker**, **Broker** ke OANDA, masukkan **ID akun OANDA** dan simpan.
3. Tekan **Perbarui**.

Akun margin yang NAV-nya negatif dilaporkan sebagai kesalahan, bukan dihitung sebagai tabungan.

Dokumentasi OANDA: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital memanggil **account summary** dari Trading 212 Public API dan menyimpan total nilai akun dalam mata uang utama akun. API ini mencakup akun **Invest** dan **Stocks ISA**; satu pasangan kunci milik satu akun, dan Capital menyimpan satu pasangan kunci, jadi aplikasi membaca satu akun Trading 212.

### 1. Buat kunci API

1. Di aplikasi atau situs web Trading 212, buka menu (**☰**) → **Settings** → **API (Beta)** dan setujui peringatan risiko.
2. Tekan **Generate API key**. Beri nama, sisakan hanya izin **Account data** (baca), dan pilih akses IP *Unrestricted* (alamat IP ponsel berganti).
3. Kirim. Salin kedua nilai: **API Key** dan **API Secret Key**. Secret hanya ditampilkan sekali; jika hilang, hapus kuncinya dan buat pasangan baru.

### 2. Hubungkan di Capital

1. **Pengaturan → Akun broker → Kunci API: Trading 212** dan **Rahasia API: Trading 212**, tempel masing-masing nilai.
2. Buka kantong, **Tambah aset**, atur **Pelacakan** ke **Akun broker**, **Broker** ke Trading 212, masukkan **Nomor akun Trading 212** (ID akun yang ditampilkan di aplikasi, hanya angka) dan simpan.
3. Tekan **Perbarui**. Trading 212 mengizinkan satu permintaan summary setiap 5 detik.

Dokumentasi Trading 212: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) adalah agregator: Anda menghubungkan akun broker ke SnapTrade sekali, lalu SnapTrade membacanya untuk Anda. Layanan ini mencakup banyak broker yang tidak memiliki API publik sendiri. Capital memakai **SnapTrade Personal**, paket gratis untuk akun Anda sendiri, dengan ID klien dan kunci konsumen milik Anda sendiri. Data pada paket ini diperbarui oleh SnapTrade sekitar sekali sehari.

Yang dikirim: ID klien Anda dan, sebagai tanda tangan, tidak ada bagian dari kunci konsumen itu sendiri (permintaan ditandatangani dengannya). SnapTrade, bukan Capital, yang memegang koneksi ke broker Anda; ketentuan dan kebijakan privasinya berlaku untuk koneksi tersebut.

### 1. Buat kunci API

1. Daftar di [dasbor SnapTrade](https://dashboard.snaptrade.com/signup) dan pilih paket **Personal**.
2. Di dasbor, buat kunci API. Salin **ID klien** dan **kunci konsumen**; kunci konsumen hanya ditampilkan sekali.

### 2. Hubungkan di Capital

1. **Pengaturan → Akun broker → ID klien: SnapTrade** dan **Kunci konsumen: SnapTrade**, tempel masing-masing nilai.
2. Buka kantong, **Tambah aset**, atur **Pelacakan** ke **Akun broker** dan **Broker** ke SnapTrade.
3. Tekan **Hubungkan broker melalui SnapTrade**. SnapTrade Connection Portal terbuka di browser; login ke broker Anda di sana (tautan berlaku 5 menit). Kembali ke Capital.
4. Tekan **Muat akun** dan pilih akun; ID-nya mengisi kolom **ID akun SnapTrade**. Simpan, lalu **Perbarui**.

Akun yang belum selesai disinkronkan oleh SnapTrade melaporkan *SnapTrade belum memiliki total nilai untuk akun ini*; perbarui lagi nanti.

Dokumentasi SnapTrade: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Broker lain {#other-brokers}

Capital hanya terhubung ke antarmuka yang berfungsi dari ponsel melalui HTTPS dengan token yang dapat Anda buat sendiri dan yang hanya membaca tanpa dapat melakukan trading. Untuk saat ini, itu mengecualikan:

- Akun **MetaTrader 4 dan 5**. Kata sandi investor memberi akses hanya-baca, tetapi hanya di dalam terminal MetaTrader; broker tidak menyediakan antarmuka HTTPS untuknya.
- Broker yang API-nya memerlukan program yang berjalan di komputer (misalnya gateway Client Portal Web API milik Interactive Brokers; Capital memakai Flex Web Service sebagai gantinya) atau pendaftaran aplikasi OAuth.
- Broker yang API-nya hanya menerbitkan token berumur pendek melalui OAuth, misalnya Saxo Bank (token akses berlaku 20 menit; token 24 jam dari portal pengembang hanya untuk lingkungan simulasi).
- Bank dan broker tanpa API publik.

Banyak dari broker ini dicakup oleh [SnapTrade](#snaptrade). Jika tidak, masukkan saldo tersebut sebagai aset **Manual** dan perbarui angkanya saat Anda memeriksa laporan rekening. Jika broker Anda menyediakan endpoint HTTPS berbasis token yang sederhana untuk membaca nilai akun, [buka issue]({{ site.repo }}/issues) beserta tautan ke dokumentasinya.

## Pesan dan tindakan yang perlu dilakukan {#messages}

| Pesan | Yang perlu dilakukan |
|---|---|
| *Interactive Brokers memerlukan token akses Anda di Pengaturan → Akun broker* / *OANDA memerlukan token akses Anda …* | Tempel token, kunci, atau ID klien di Pengaturan → Akun broker. |
| *Token sudah kedaluwarsa; buat token baru di Client Portal* | Buat token Flex Web Service baru dan tempel. |
| *Token tidak valid* | Salin token sekali lagi; token baru menggantikan yang lama. |
| *Token dibatasi untuk alamat IP lain* | Buat token tanpa pembatasan IP. |
| *Flex Query id tidak valid* | Periksa angkanya; query harus berupa Activity Flex Query dari login ini. |
| *Tambahkan bagian Net Asset Value (NAV) Summary in Base dengan Report Date dan Total ke Flex Query* | Ubah query dan tambahkan bagian serta kolomnya. |
| *Tambahkan kolom Currency dari Account Information ke Flex Query* | Ubah query dan tambahkan kolomnya. |
| *Query mengembalikan N akun; buat satu Flex Query untuk setiap akun* | Buat query yang mencakup satu akun. |
| *Laporan belum siap; perbarui lagi satu menit lagi* | Interactive Brokers masih membuat laporan; perbarui lagi. |
| *Akses ditolak; periksa kunci atau kuota penyedia* | Token OANDA salah atau sudah dicabut, atau pasangan kunci Trading 212 salah atau tidak memiliki izin Account data. |
| *SnapTrade belum memiliki total nilai untuk akun ini; sinkronkan koneksi lalu coba lagi* | SnapTrade belum menyinkronkan broker; perbarui lagi nanti. |
| *Belum ada akun yang terhubung. Hubungkan broker melalui SnapTrade terlebih dahulu.* | Buka Connection Portal dari editor dan hubungkan broker. |
| *Nilai akun negatif … tidak didukung* | Akun berada dalam posisi debit; tidak menambah tabungan Anda. |

Nilai sebelumnya tetap terlihat setelah salah satu pesan ini, dan ditandai usang.
