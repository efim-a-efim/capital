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

Capital hanya terhubung ke antarmuka yang kredensialnya berumur panjang: token atau kunci yang Anda buat sekali dan tetap berlaku sampai Anda mencabutnya, sampai kedaluwarsa yang Anda pilih, atau selama minimal beberapa bulan (token T-Invest berakhir setelah tiga bulan tanpa dipakai, token ALOR setelah satu tahun). Setiap broker di bawah ini tersedia apa pun bahasa yang dipakai aplikasi. Yang didukung saat ini:

| Broker | Antarmuka yang dipakai | Yang dibaca |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (hanya pengambilan laporan) | Net asset value pada hari bursa terakhir, dalam mata uang dasar akun |
| [OANDA](#oanda) | v20 REST API, akun live fxTrade | Net asset value pada saat pembaruan, dalam mata uang akun |
| [Trading 212](#trading-212) | Public API, akun Invest dan Stocks ISA | Total nilai akun pada saat pembaruan, dalam mata uang utama akun |
| [SnapTrade](#snaptrade) | SnapTrade Personal, agregator yang mencakup banyak broker | Total nilai akun sebagaimana dilaporkan broker ke SnapTrade, dalam mata uang akun |
| [Alpaca](#alpaca) | Trading API, akun live | Ekuitas (kas ditambah posisi), dalam dolar AS |
| [Tradier](#tradier) | Brokerage API | Total ekuitas, dalam dolar AS |
| [tastytrade](#tastytrade) | Open API dengan OAuth grant pribadi | Net liquidating value, dalam dolar AS |
| [Public.com](#public) | Individual API | Total nilai akun, dalam dolar AS |
| [eToro](#etoro) | Public API | Saldo akun yang dipilih (untuk akun trading: kas ditambah posisi yang diinvestasikan), dalam mata uangnya |
| [Indexa Capital](#indexa-capital) | REST API, token hanya-baca | Total portofolio pada tanggal valuasi terakhir, dalam mata uang akun |
| [T-Invest](#t-invest) | T-Invest API (T-Bank) | Total nilai portofolio, dalam rubel |
| [ALOR](#alor) | ALOR OpenAPI | Valuasi portofolio di Moscow Exchange, dalam rubel |
| [Capital.com](#capital-com) | Public API, akun live | Saldo termasuk untung dan rugi posisi terbuka, dalam mata uang akun |
| [Akahu](#akahu) | Aplikasi pribadi Akahu, agregator Selandia Baru | Saldo akun yang terhubung (Sharesies, Hatch, Kernel, KiwiSaver, dan lainnya), dalam mata uangnya |

## Sebelum mulai {#before-you-start}

- **Apa yang keluar dari perangkat.** Pada setiap pembaruan, aplikasi mengirim token akses dan ID akun atau ID query ke broker tersebut melalui HTTPS. Broker melihat alamat IP Anda, seperti pada permintaan internet mana pun.
- **Tempat kredensial disimpan.** Tab Broker → Kredensial. Kredensial dienkripsi dengan kunci yang disimpan di Android Keystore, tidak pernah ditulis ke folder data Anda, dan tidak disertakan dalam ekspor maupun cadangan sistem. Satu set kredensial per broker mencakup semua akun yang Anda tambahkan untuk broker itu.
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

1. **Broker → Kredensial → Token akses: Interactive Brokers**, tempel token dan simpan.
2. **Broker → +**: masukkan nama, atur **Broker** ke Interactive Brokers, masukkan **Flex Query id** dan simpan.
3. Buka kantong, **Tambah aset**, atur **Pelacakan** ke **Akun broker**, pilih akun, lalu simpan.
4. Tekan **Perbarui**. Proses pertama memakan waktu hingga setengah menit karena laporan dibuat berdasarkan permintaan.

Jika token kedaluwarsa, pembaruan melaporkan *Token sudah kedaluwarsa; buat token baru di Client Portal*: buat token baru dan tempel di bagian Kredensial. Interactive Brokers mengizinkan satu permintaan laporan per detik dan sepuluh per menit dari satu token, batas yang tidak pernah dilampaui oleh satu pembaruan.

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

1. **Broker → Kredensial → Token akses: OANDA**, tempel token dan simpan.
2. **Broker → +**: masukkan nama, atur **Broker** ke OANDA, masukkan **ID akun OANDA** dan simpan.
3. Buka kantong, **Tambah aset**, atur **Pelacakan** ke **Akun broker**, pilih akun, lalu simpan.
4. Tekan **Perbarui**.

Akun margin yang NAV-nya negatif dilaporkan sebagai kesalahan, bukan dihitung sebagai tabungan.

Dokumentasi OANDA: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital memanggil **account summary** dari Trading 212 Public API dan menyimpan total nilai akun dalam mata uang utama akun. API ini mencakup akun **Invest** dan **Stocks ISA**; satu pasangan kunci milik satu akun, dan Capital menyimpan satu pasangan kunci, jadi aplikasi membaca satu akun Trading 212.

### 1. Buat kunci API

1. Di aplikasi atau situs web Trading 212, buka menu (**☰**) → **Settings** → **API (Beta)** dan setujui peringatan risiko.
2. Tekan **Generate API key**. Beri nama, sisakan hanya izin **Account data** (baca), dan pilih akses IP *Unrestricted* (alamat IP ponsel berganti).
3. Kirim. Salin kedua nilai: **API Key** dan **API Secret Key**. Secret hanya ditampilkan sekali; jika hilang, hapus kuncinya dan buat pasangan baru.

### 2. Hubungkan di Capital

1. **Broker → Kredensial → Kunci API: Trading 212** dan **Rahasia API: Trading 212**, tempel masing-masing nilai.
2. **Broker → +**: masukkan nama, atur **Broker** ke Trading 212, masukkan **Nomor akun Trading 212** (ID akun yang ditampilkan di aplikasi, hanya angka) dan simpan.
3. Buka kantong, **Tambah aset**, atur **Pelacakan** ke **Akun broker**, pilih akun, lalu simpan.
4. Tekan **Perbarui**. Trading 212 mengizinkan satu permintaan summary setiap 5 detik.

Dokumentasi Trading 212: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) adalah agregator: Anda menghubungkan akun broker ke SnapTrade sekali, lalu SnapTrade membacanya untuk Anda. Layanan ini mencakup banyak broker yang tidak memiliki API publik sendiri. Capital memakai **SnapTrade Personal**, paket gratis untuk akun Anda sendiri, dengan ID klien dan kunci konsumen milik Anda sendiri. Data pada paket ini diperbarui oleh SnapTrade sekitar sekali sehari.

Yang dikirim: ID klien Anda dan, sebagai tanda tangan, tidak ada bagian dari kunci konsumen itu sendiri (permintaan ditandatangani dengannya). SnapTrade, bukan Capital, yang memegang koneksi ke broker Anda; ketentuan dan kebijakan privasinya berlaku untuk koneksi tersebut.

### 1. Buat kunci API

1. Daftar di [dasbor SnapTrade](https://dashboard.snaptrade.com/signup) dan pilih paket **Personal**.
2. Di dasbor, buat kunci API. Salin **ID klien** dan **kunci konsumen**; kunci konsumen hanya ditampilkan sekali.

### 2. Hubungkan di Capital

1. **Broker → Kredensial → ID klien: SnapTrade** dan **Kunci konsumen: SnapTrade**, tempel masing-masing nilai.
2. **Broker → +**: masukkan nama dan atur **Broker** ke SnapTrade.
3. Tekan **Hubungkan broker melalui SnapTrade**. SnapTrade Connection Portal terbuka di browser; login ke broker Anda di sana (tautan berlaku 5 menit). Kembali ke Capital.
4. Tekan **Muat akun** dan pilih akun; ID-nya mengisi kolom **ID akun SnapTrade**. Simpan.
5. Buka kantong, **Tambah aset**, atur **Pelacakan** ke **Akun broker**, pilih akun, lalu simpan, kemudan **Perbarui**.

Akun yang belum selesai disinkronkan oleh SnapTrade melaporkan *SnapTrade belum memiliki total nilai untuk akun ini*; perbarui lagi nanti.

Dokumentasi SnapTrade: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Menghubungkan akun di Capital {#connect}

Bagian di bawah ini menjelaskan cara membuat kredensial di tiap broker. Di Capital langkahnya sama untuk semuanya:

1. **Broker → Kredensial**: tekan tombol kredensial broker dan tempel setiap nilai.
2. **Broker → +**: masukkan nama, pilih **Broker**, lalu tekan **Muat akun** dan pilih akunnya (atau ketik ID-nya) dan simpan.
3. Buka kantong, **Tambah aset**, atur **Pelacakan** ke **Akun broker**, pilih akun, lalu simpan. Tekan **Perbarui**.

## Alpaca {#alpaca}

Alpaca menerbitkan ID kunci dan rahasia untuk setiap akun; keduanya tetap berlaku sampai Anda membuatnya ulang. Hanya akun live yang dibaca: kunci akun paper tidak berfungsi pada API live.

1. Login ke [dasbor Alpaca](https://app.alpaca.markets), beralih ke akun live Anda, lalu di beranda, di bawah **API Keys**, tekan **Generate New Keys**.
2. Salin **API Key ID** dan **Secret Key**; rahasia hanya ditampilkan sekali.
3. Di Capital tempel keduanya sebagai **Kunci API: Alpaca** dan **Rahasia API: Alpaca**, lalu ikuti [Menghubungkan akun](#connect). **Muat akun** menampilkan nomor akun milik kunci tersebut.

Dokumentasi Alpaca: [Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

Token API dari pengaturan Tradier Anda tidak pernah kedaluwarsa.

1. Login ke Tradier dan buka [Settings → API Access](https://web.tradier.com/user/api). Salin **API Access Token** akun brokerage Anda (bukan token sandbox).
2. Di Capital tempel sebagai **Token akses: Tradier**, lalu ikuti [Menghubungkan akun](#connect).

Dokumentasi Tradier: [Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytrade memakai OAuth grant pribadi: Anda membuat aplikasi untuk diri sendiri dan sebuah grant yang token penyegarnya tidak pernah kedaluwarsa. Pada setiap pembaruan, Capital menukarnya dengan token akses berumur 15 menit.

1. Di [my.tastytrade.com](https://my.tastytrade.com) buka **Manage → My Profile → API → OAuth Applications** dan tekan **+ New OAuth client**. Beri nama, URI pengalihan HTTPS apa pun (misalnya `https://capital.fimych.dev`) dan hanya cakupan **read**. Simpan dan salin **Client Secret**; hanya ditampilkan sekali.
2. Tekan **Manage** di sebelah aplikasi, lalu **Create Grant**, dan salin **refresh token**.
3. Di Capital tempel keduanya sebagai **Token penyegar: tastytrade** dan **Rahasia klien: tastytrade**, lalu ikuti [Menghubungkan akun](#connect).

Dokumentasi tastytrade: [OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

Individual API milik Public ditujukan untuk akun Anda sendiri. Kunci rahasianya berumur panjang dan dapat dicabut; pada setiap pembaruan, Capital menukarnya dengan token akses berumur lima menit.

1. Di aplikasi web Public buka halaman **API** pada pengaturan Anda dan buat **secret key**.
2. Di Capital tempel sebagai **Kunci rahasia: Public.com**, lalu ikuti [Menghubungkan akun](#connect).

Dokumentasi Public: [Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

Kunci eToro berumur panjang; Anda dapat memberinya tanggal kedaluwarsa dan daftar IP, dan Anda dapat membuatnya hanya-baca. Akun eToro Anda harus sudah terverifikasi.

1. Di eToro buka **Settings → Trading → API Key Management** dan tekan **Create New Key**. Pilih lingkungan **Real**, izin **Read**, tanpa daftar IP, dan, jika mau, tanggal kedaluwarsa. Konfirmasi dengan kode SMS.
2. Salin **Public API Key** dan **User Key**; kunci pengguna hanya ditampilkan sekali.
3. Di Capital tempel keduanya sebagai **Kunci API publik: eToro** dan **Kunci pengguna: eToro**, lalu ikuti [Menghubungkan akun](#connect). **Muat akun** mencantumkan akun trading, kas, dan akun eToro Anda lainnya.

Dokumentasi eToro: [Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

Token dari area pribadi Indexa bersifat hanya-baca. Token itu terikat pada e-mail, kata sandi, dan perangkat Anda: setelah mengganti kata sandi, buat ulang.

1. Di area pribadi Indexa buka **Pengaturan pengguna → Aplikasi** (*User settings → Applications*) dan salin tokennya.
2. Di Capital tempel sebagai **Token akses: Indexa Capital**, lalu ikuti [Menghubungkan akun](#connect). Akun pensiun dan akun investasi sama-sama dicantumkan.

Indexa menghitung nilai dana sekali setiap hari kerja; tanggal observasi adalah tanggal valuasi tersebut.

Dokumentasi Indexa Capital: [REST API](https://indexacapital.com/en/api-rest-v1) · [Connecting with the API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

T-Invest API milik T-Bank menerima token yang Anda terbitkan di pengaturan investasi. Token berakhir tiga bulan setelah pemakaian terakhir dan harus dipakai dalam tujuh hari sejak diterbitkan; pembaruan mingguan menjaganya tetap aktif. Pilih token **hanya-baca**.

1. Buka [pengaturan T-Invest](https://www.tbank.ru/invest/settings/) dan terbitkan **token API T-Invest** untuk bursa dengan akses **hanya-baca** (semua akun atau satu akun). Konfirmasi transaksi dengan kode harus dimatikan agar token dapat diterbitkan. Salin token; hanya ditampilkan sekali.
2. Di Capital tempel sebagai **Token akses: T-Invest**, lalu ikuti [Menghubungkan akun](#connect).

T-Bank melayani API ini dengan Russian Trusted Root CA, yang tidak disertakan Android. Capital mempercayai sertifikat itu hanya untuk alamat T-Invest API (`invest-public-api.tbank.ru`), dan tidak untuk koneksi lain.

Dokumentasi T-Invest: [Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALOR menerbitkan token penyegar yang berlaku satu tahun; pada setiap pembaruan, Capital menukarnya dengan token akses berumur 30 menit. ALOR tidak menyediakan token hanya-baca: token itu dapat dipakai untuk trading, sedangkan Capital hanya membaca.

1. Login ke [portal developer ALOR](https://alor.dev), tautkan akun trading Anda, buka **API Access Tokens** dan tekan **Create Token**. Salin token penyegar.
2. Di Capital tempel sebagai **Token penyegar: ALOR**, lalu ikuti [Menghubungkan akun](#connect). **Muat akun** mencantumkan portofolio milik akun tersebut (pasar saham D…, pasar valuta G…, derivatif 7500…); tambahkan satu untuk setiap portofolio.

Dokumentasi ALOR: [Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Kunci Capital.com berlaku satu tahun secara default, atau sampai tanggal yang Anda pilih. Kunci itu membawa hak trading (Capital.com tidak memiliki kunci hanya-baca); Capital hanya membaca. Setiap kunci memiliki kata sandinya sendiri, yang bukan kata sandi akun Anda.

1. Aktifkan autentikasi dua faktor, lalu buka **Settings → API integrations** dan tekan **Generate API key**. Beri label dan **custom password**, biarkan atau atur kedaluwarsanya, dan konfirmasi dengan kode 2FA. Salin kunci; hanya ditampilkan sekali.
2. Di Capital tempel **Kunci API: Capital.com**, e-mail login Anda sebagai **E-mail login: Capital.com** dan custom password sebagai **Kata sandi kunci API: Capital.com**, lalu ikuti [Menghubungkan akun](#connect). Hanya akun live yang dibaca.

Dokumentasi Capital.com: [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) menghubungkan bank, platform investasi, dan skema KiwiSaver di Selandia Baru; aplikasi pribadi gratis membaca akun Anda sendiri. Akahu memperbarui datanya sekitar sekali sehari.

1. Daftar di [my.akahu.nz](https://my.akahu.nz) dan hubungkan penyedia Anda (misalnya Sharesies, Hatch, Kernel, Simplicity, Milford, atau skema KiwiSaver Anda).
2. Buka halaman **Developers**, setujui ketentuan developer, dan salin **App ID Token** serta **User Access Token**.
3. Di Capital tempel keduanya sebagai **Token ID aplikasi: Akahu** dan **Token akses pengguna: Akahu**, lalu ikuti [Menghubungkan akun](#connect).

Dokumentasi Akahu: [Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## Broker populer menurut pasar {#by-market}

Cara menghubungkan broker yang paling banyak dipakai di pasar bahasa-bahasa Capital, per Oktober 2026. *Langsung* berarti ada bagian di atas; *SnapTrade* berarti melalui [SnapTrade](#snaptrade); selain itu, alasan mengapa broker tidak dapat dibaca, dan saldonya dapat dicatat sebagai aset **Manual**.

| Pasar | Broker | Cara |
|---|---|---|
| Amerika Serikat | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | Langsung |
| Amerika Serikat | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| Amerika Serikat | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | Tanpa API publik |
| Kanada | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| Kanada | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | Tanpa API publik |
| Inggris dan Irlandia | Trading 212, eToro, Interactive Brokers | Langsung |
| Inggris dan Irlandia | AJ Bell | SnapTrade |
| Inggris dan Irlandia | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | Tanpa API publik |
| Inggris dan Irlandia | IG | Tidak mungkin: setiap sesi memerlukan kata sandi akun |
| Eropa | Indexa Capital (Spanyol), eToro, Trading 212, Interactive Brokers | Langsung |
| Eropa | DEGIRO, BUX | SnapTrade |
| Eropa | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | Tanpa API publik untuk investasi |
| Eropa | XTB | Tidak mungkin: API ditutup pada Maret 2025 |
| Eropa | Saxo, comdirect | Tidak mungkin: hanya token berumur pendek atau sesi TAN |
| Eropa | Bitpanda, Freedom24 | Tidak mungkin: API tidak mengembalikan total nilai akun |
| Rusia dan Kazakhstan | T-Invest, ALOR | Langsung |
| Rusia dan Kazakhstan | BCS | Tidak mungkin: tidak ada total nilai, dan tokennya kedaluwarsa setelah 90 hari |
| Rusia dan Kazakhstan | Finam | Belum: mata uang nilai akun tidak terdokumentasi |
| Rusia dan Kazakhstan | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | Tanpa API publik, atau tanpa total nilai di dalamnya |
| India | Zerodha, Upstox | SnapTrade (aturan SEBI mengakhiri sesi API setiap hari, sehingga koneksi perlu sering diperbarui) |
| India | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | Tidak mungkin: aturan SEBI mengakhiri setiap sesi API setiap hari |
| Pakistan dan Bangladesh | Semua broker bursa | Tanpa API publik |
| Tiongkok, Hong Kong, dan Taiwan | moomoo | SnapTrade |
| Tiongkok, Hong Kong, dan Taiwan | Futu, Tiger Brokers, Longbridge | Belum: masa berlaku kunci atau format respons belum terdokumentasi lengkap, atau kunci tidak dapat dibatasi hanya untuk membaca |
| Tiongkok, Hong Kong, dan Taiwan | East Money, Huatai, CITIC, Yuanta, Fubon | Tanpa API web publik (hanya terminal desktop atau SDK sertifikat) |
| Jepang | OANDA Japan (akun yang memenuhi syarat akses API) | Langsung, sebagai OANDA |
| Jepang | SBI Securities, Rakuten Securities, Monex, Matsui | Tanpa API publik |
| Australia dan Selandia Baru | CommSec, Stake | SnapTrade |
| Australia dan Selandia Baru | Sharesies, Hatch, Kernel, Simplicity, skema KiwiSaver | Akahu (akun Selandia Baru) |
| Timur Tengah dan Afrika | eToro | Langsung |
| Timur Tengah dan Afrika | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | Tanpa API publik untuk individu |
| Asia Tenggara | Stockbit, Ajaib, Bibit, IPOT, VPS | Tanpa API publik |
| Asia Tenggara | SSI, TCBS, DNSE | Tidak mungkin: token 8 jam dengan kode sekali pakai, atau hanya saldo kas |
| Amerika Latin | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | Tanpa API publik untuk individu, atau login hanya dengan kata sandi |
| Forex dan CFD | OANDA, Capital.com | Langsung |
| Forex dan CFD | Broker MetaTrader (XM, Exness, Pepperstone, IC Markets, Admirals) | Tidak mungkin: tidak ada akses baca HTTPS |
| Forex dan CFD | Broker cTrader, FXCM, Forex.com | Tidak mungkin: pendaftaran aplikasi, API usang, atau login dengan kata sandi |

## Broker lain {#other-brokers}

Capital hanya terhubung ke antarmuka yang berfungsi dari ponsel melalui HTTPS dengan token yang dapat Anda buat sendiri dan yang hanya membaca tanpa dapat melakukan trading. Untuk saat ini, itu mengecualikan:

- Akun **MetaTrader 4 dan 5**. Kata sandi investor memberi akses hanya-baca, tetapi hanya di dalam terminal MetaTrader; broker tidak menyediakan antarmuka HTTPS untuknya.
- Broker yang API-nya memerlukan program yang berjalan di komputer (misalnya gateway Client Portal Web API milik Interactive Brokers; Capital memakai Flex Web Service sebagai gantinya) atau pendaftaran aplikasi OAuth.
- Broker yang API-nya hanya menerbitkan token berumur pendek melalui OAuth, misalnya Saxo Bank (token akses berlaku 20 menit; token 24 jam dari portal pengembang hanya untuk lingkungan simulasi).
- Bank dan broker tanpa API publik.

Banyak dari broker ini dicakup oleh [SnapTrade](#snaptrade). Jika tidak, masukkan saldo tersebut sebagai aset **Manual** dan perbarui angkanya saat Anda memeriksa laporan rekening. Jika broker Anda menyediakan endpoint HTTPS berbasis token yang sederhana untuk membaca nilai akun, [buka issue]({{ site.repo }}/issues) beserta tautan ke dokumentasinya. Setiap broker di Capital adalah plugin kecil; developer dapat menambahkannya dengan mengikuti [panduan plugin]({{ site.repo }}/blob/main/BROKER-PLUGINS.md).

## Pesan dan tindakan yang perlu dilakukan {#messages}

| Pesan | Yang perlu dilakukan |
|---|---|
| *Interactive Brokers memerlukan kredensialnya di layar Broker* / *OANDA memerlukan kredensialnya …* | Tempel token, kunci, atau ID klien di bagian Kredensial pada tab Broker. |
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
| *tastytrade menolak token penyegar atau rahasia klien; buat grant baru* | Buat grant baru untuk aplikasi dan tempel token penyegarnya; periksa rahasia klien. |
| *Capital.com tidak membuka sesi; periksa kunci API, login, dan kata sandi kunci* | Kunci, e-mail, atau kata sandi kustom kunci salah, atau kunci sudah kedaluwarsa. |
| *Akun tidak ditemukan; pilih lagi* | Broker tidak lagi mencantumkan akun ini; ubah akun dan pilih dari **Muat akun**. |

Nilai sebelumnya tetap terlihat setelah salah satu pesan ini, dan ditandai usang.
