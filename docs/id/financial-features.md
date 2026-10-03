---
layout: default
lang: id
base: "/id"
key: "financial-features"
title: Deklarasi fitur keuangan
class: doc
---
# Deklarasi fitur keuangan

<p class="meta">Jawaban untuk formulir Google Play Console (Kebijakan dan program → Konten aplikasi → Fitur keuangan), beserta alasannya. Diperiksa terhadap versi 2.2.1 pada 30 September 2026.</p>

## Jawaban formulir

**Select all of the financial features the app provides** (Pilih semua fitur keuangan yang disediakan aplikasi)**:** **The app does not provide any financial features** (Aplikasi tidak menyediakan fitur keuangan apa pun)**.**

## Alasan

Capital adalah pencatat tabungan pribadi. Aplikasi ini mencatat apa yang sudah dimiliki pengguna dan menunjukkan bagaimana tabungan tersebut dipetakan ke tujuan pengguna sendiri. Dibandingkan dengan setiap fitur di formulir:

| Fitur di formulir | Capital |
|---|---|
| Personal loan direct lender, loan facilitator, payday loans, line of credit, earned wage advances, microfinance, buy now pay later (pemberi pinjaman pribadi langsung, fasilitator pinjaman, pinjaman gajian, jalur kredit, uang muka gaji, keuangan mikro, beli sekarang bayar nanti) | Tidak ada pinjaman dalam bentuk apa pun |
| Banking (Perbankan) | Tidak ada rekening, simpanan, atau akses rekening. Saldo bank diketik sendiri oleh pengguna. Nilai akun efek dan forex dibaca melalui antarmuka pelaporan milik broker dengan token yang dibuat pengguna; aplikasi tidak dapat membuat order, mentransfer, atau menarik dana |
| Mobile payments and digital wallets, money transfer and wire services (Pembayaran seluler dan dompet digital, layanan transfer uang dan pengiriman uang) | Tidak dapat mengirim, menerima, atau menyimpan uang. Alokasi tujuan adalah perhitungan yang ditampilkan di layar; tidak ada yang dipindahkan |
| Cryptocurrency wallet (Dompet mata uang kripto) | Membaca saldo alamat publik yang ditempel pengguna. Aplikasi tidak pernah menyimpan kunci privat atau seed phrase dan tidak dapat menandatangani atau menyiarkan transaksi, jadi aplikasi ini bukan dompet |
| Cryptocurrency exchange (Bursa mata uang kripto) | Tidak ada perdagangan, perutean order, atau on-ramp fiat |
| Rewards and incentives, crowdfunding and chit funds, prediction markets (Hadiah dan insentif, urun dana dan arisan, pasar prediksi) | Tidak ada |
| Credit monitoring and reporting (Pemantauan dan pelaporan kredit) | Tidak ada |
| Financial advice (Saran keuangan) | Tidak ada. Proyeksi hanya menampilkan hasil hitungan atas angka milik pengguna sendiri ("Rencana tabungan memenuhi tujuan ini pada …"); proyeksi tidak merekomendasikan produk, aset, atau tindakan apa pun. Kalkulator penyeimbangan ulang mencantumkan pembelian yang diperlukan untuk mencapai persentase yang ditetapkan pengguna sendiri |
| Insurance (Asuransi) | Tidak ada |
| In-app purchases, donations (Pembelian dalam aplikasi, donasi) | Tidak ada yang diproses oleh aplikasi. Layar tip menampilkan alamat dompet publik developer (sama dengan yang ada di situs ini); transfer dilakukan di aplikasi dompet milik pengguna sendiri, tidak membuka fitur apa pun, dan tidak terlihat oleh aplikasi |

Aplikasi juga tidak menyediakan pembelian dalam aplikasi maupun fitur berbayar.

## Jika peninjau tidak setuju

Jika peninjau Play tetap mengklasifikasikan aplikasi sebagai penyedia fitur keuangan, opsi terdekat adalah **Other** (Lainnya) dengan deskripsi ini:

> Read-only personal savings tracker. Users type in their balances, paste public blockchain addresses or connect a brokerage account with a reporting token; the app fetches balances, account values and market prices from third-party data sources and shows how the savings cover the user's own goals. No custody, no keys, no transactions, no lending, no trading, no advice.

(Terjemahan: Pencatat tabungan pribadi yang hanya membaca data. Pengguna mengetik saldo, menempel alamat blockchain publik, atau menghubungkan akun efek dengan token pelaporan; aplikasi mengambil saldo, nilai akun, dan harga pasar dari sumber data pihak ketiga dan menunjukkan bagaimana tabungan menutupi tujuan pengguna sendiri. Tanpa kustodi, tanpa kunci, tanpa transaksi, tanpa pinjaman, tanpa perdagangan, tanpa saran.)

Persyaratan khusus negara untuk aplikasi pinjaman pribadi, serta pertanyaan tentang mata uang kripto untuk Amerika Serikat, tidak berlaku karena tidak ada satu pun fitur tersebut yang dipilih.

## Fakta terkait yang mungkin ditanyakan peninjau

- Data pasar berasal dari operator pihak ketiga yang dipilih pengguna (lihat [Kebijakan Privasi]({{ page.base }}/privacy)). Aplikasi menampilkan nama dan situs operator di Pengaturan.
- Kueri dompet menggunakan API blockchain publik yang hanya membaca data.
- Akun broker (Interactive Brokers, OANDA, Trading 212, SnapTrade, Alpaca, Tradier, tastytrade, Public.com, eToro, Indexa Capital, T-Invest, ALOR, Capital.com, Akahu) dibaca dengan token atau kunci yang dibuat pengguna di portal milik broker; aplikasi hanya memanggil endpoint pelaporan dan tidak dapat membuat order atau memindahkan uang. Penyiapannya didokumentasikan di [Akun broker dan forex]({{ page.base }}/accounts).
- Aplikasi berjalan sepenuhnya di perangkat dan tidak memiliki server yang dijalankan developer.
