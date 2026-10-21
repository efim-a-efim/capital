---
layout: screen
lang: id
base: "/id"
key: "screens/goal"
screen: goal
title: Tujuan
---
# Tujuan

**Apa ini.** Satu tujuan beserta pendanaannya. Dibuka dengan mengetuk kartu di tab [Tujuan]({{ page.base }}/screens/goals); **← Semua tujuan** untuk kembali.

**Header.** Nama dan lencana, terkumpul / target, tenggat, baris proyeksi, dan **Masih dibutuhkan**: target dikurangi yang sudah terkumpul hari ini.

**Tombol.** **Ubah tujuan** mengubah nama, mata uang, target, dan tenggat. **Arsipkan** menyimpan tujuan tanpa ikut dihitung; tujuan yang diarsipkan menampilkan **Aktifkan** sebagai gantinya. **Hapus** menghapus tujuan beserta koneksinya setelah konfirmasi.

## FIRE {#fire}

Tujuan berjenis **FIRE** (kemandirian finansial, pensiun dini) menjawab dua pertanyaan: kapan uang mulai dapat membiayai hidup Anda, dan berapa lama uang itu bertahan. Ini adalah proyeksi berdasarkan angka Anda sendiri; tidak merekomendasikan apa pun. Atur jenisnya lewat **Jenis tujuan** (**Tujuan tabungan** / **FIRE**) di editor.

**Kolom editor.** Semua kolom selain nama dan mata uang boleh kosong; aplikasi menghitung apa yang bisa dari sisanya.

- **Tanggal lahir · YYYY-MM-DD · opsional**: menampilkan usia dan menetapkan cakrawala sesuai harapan hidup.
- **Tanggal FIRE · YYYY-MM-DD · kosongkan untuk dihitung**: hari Anda berhenti menabung dan mulai membelanjakan.
- **Saya bisa berinvestasi · kosongkan untuk dihitung**: jumlah per **minggu**, **2 minggu**, **bulan**, **tahun**, atau **N hari** (lalu isi **Hari**).
- **Pengeluaran per bulan setelah FIRE · kosongkan untuk dihitung**.
- **Dana yang dimiliki setelah FIRE · kosongkan untuk dihitung**: dana FIRE, yaitu target tujuan.
- **Asumsi lanjutan**: **Inflasi % · rata-rata / optimis / pesimis**, **Pertumbuhan % · rata-rata / optimis / pesimis**, **Tingkat penarikan %** dan **Harapan hidup · tahun**. Setiap label diakhiri "· saat ini {nilai}", yaitu nilai yang berlaku bila kolom dikosongkan.

Semua jumlah dalam nilai uang hari ini dalam mata uang tujuan. Layar juga menampilkan nilai pada tanggal FIRE dalam uang masa depan. Uang yang dimiliki tujuan hari ini berasal dari kantong yang terhubung, seperti tujuan lain; [Rencana]({{ page.base }}/screens/plans) tidak ikut dalam perhitungan FIRE.

**Apa yang dihitung.**

- Tanggal FIRE: dari target dan kontribusi.
- Kontribusi: dari target dan tanggal FIRE.
- Target: dari pengeluaran (pengeluaran × 12 ÷ tingkat penarikan), atau dari tanggal FIRE dan kontribusi.
- Pengeluaran: dari target (target × tingkat penarikan ÷ 12).

Jika tanggal FIRE, kontribusi, dan target semuanya diisi, tanggal dipertahankan dan layar menampilkan kekurangan serta tanggal paling awal yang dapat dicapai. Target dan tanggal hasil hitungan ditulis ke target dan tenggat tujuan, sehingga urutan, alokasi, Ringkasan, dan Rencana bekerja seperti pada tujuan lain. Keduanya diperbarui pada setiap pengeditan dan setiap pembaruan.

**Bagian FIRE.** Tabel dengan satu kolom per skenario, **Rata-rata**, **Optimis**, dan **Pesimis**:

| Baris | Arti |
|---|---|
| **Tanggal FIRE** | Tanggalnya, dengan "usia N" bila tanggal lahir diketahui |
| **Kontribusi per bulan** | Jumlah yang harus diinvestasikan setiap bulan |
| **Dana FIRE · nilai uang hari ini** | Dana yang harus dimiliki, seperti yang dinyatakan di editor |
| **Dana FIRE · pada tanggal FIRE** | Dana yang sama setelah inflasi |
| **Pengeluaran per bulan · nilai uang hari ini** | Pengeluaran bulanan setelah FIRE |
| **Pengeluaran per bulan · pada tanggal FIRE** | Pengeluaran yang sama setelah inflasi |
| **Uang bertahan** | *hingga <tanggal> · usia N*, *N tahun · melampaui harapan hidup*, *uang tidak akan habis 🥳*, atau *kurang …* bila tanggal tidak tercapai |

Di bawah tabel terdapat suku tiap skenario ("Rata-rata: pertumbuhan 7% · inflasi 3% · penarikan 4%") dan baris "Suku: …" yang menyebut penyedia asalnya, atau *bawaan aplikasi*. Grafik di bawahnya adalah grafik penyusutan: nilai uang dalam uang hari ini dari sekarang hingga uang habis atau hingga akhir cakrawala, dengan tiga kurva berwarna, satu per skenario. Garis putus-putus menandai tanggal FIRE. Catatan di bawahnya berbunyi *Proyeksi berdasarkan angka Anda sendiri; tidak merekomendasikan apa pun.* Di tab [Tujuan]({{ page.base }}/screens/goals), tujuan tersebut menampilkan *FIRE pada <tanggal> · usia N*, atau *tidak tercapai*.

**Cara menghitung.** Dalam langkah bulanan. Hingga tanggal FIRE, kontribusi mempertahankan daya belinya sehingga naik mengikuti inflasi, dan uang tumbuh sesuai suku skenario. Setelah tanggal FIRE, pengeluaran naik mengikuti inflasi dan sisanya terus tumbuh sesuai suku skenario. Cakrawalanya adalah tanggal lahir ditambah harapan hidup (bawaan 95), atau 50 tahun setelah tanggal FIRE bila tanggal lahir tidak diisi. *Uang tidak akan habis* berarti uang tidak terpakai habis dalam 200 tahun dan nilainya di akhir, dalam uang hari ini, setidaknya sama dengan pada tanggal FIRE.

**Bawaan dan sumber.** Tanpa nilai yang Anda isi atau suku yang diambil, aplikasi memakai pertumbuhan 7 % (optimis 10 %, pesimis 4 %), inflasi 3 % (2 %, 5 %), dan tingkat penarikan 4 %. Angka ini berasal dari UBS Global Investment Returns Yearbook 2026 (dunia, 1900–2024: saham 9,8 % per tahun nominal, obligasi 4,6 %, inflasi 2,9 %; 7 % kira-kira setara campuran 60/40), target inflasi 2 % bank sentral, serta Bengen (1994) dan studi Trinity (1998) untuk aturan 4 %. Lihat juga [FIRE]({{ page.base }}/manual#fire) di manual.

**Suku langsung.** **Perbarui** mengambil inflasi dan pertumbuhan pasar untuk mata uang setiap tujuan FIRE, dari penyedia yang dipilih di [Pengaturan]({{ page.base }}/screens/settings). Urutannya: nilai **Asumsi lanjutan** Anda, lalu nilai yang diambil, lalu bawaan. Pengambilan yang gagal mempertahankan seri sebelumnya dan menampilkan galatnya. Kedua sumber pertumbuhan adalah indeks harga tanpa dividen, sehingga menaksir terlalu rendah total imbal hasil.

**Pesan.**

| Pesan | Yang perlu dilakukan |
|---|---|
| *Masukkan target, pengeluaran bulanan, atau tanggal FIRE beserta kontribusi* | Isi salah satu dari ketiganya; tanpa itu tidak ada yang dapat dihitung. |
| *Masukkan tanggal FIRE atau kontribusi* | Target sudah diketahui tetapi tanggal maupun kontribusi belum; isi salah satunya. |
| *Tanggal FIRE sudah lewat* | Pilih tanggal di masa depan. |
| *Tanggal lahir berada di masa depan* | Perbaiki tanggal lahir. |
| *Tingkat penarikan harus di atas nol* | Masukkan tingkat penarikan di atas 0 di **Asumsi lanjutan**, atau kosongkan untuk memakai bawaan. |
| *Tidak tercapai sebelum <tanggal>* | Dengan kontribusi dan suku ini, target tidak tercapai sebelum tanggal itu (akhir cakrawala); naikkan kontribusi, turunkan target atau pengeluaran, atau pilih skenario lain. |
| *Tidak ada data suku untuk <mata uang>* | Penyedia inflasi atau pertumbuhan tidak memiliki seri untuk mata uang ini; pilih penyedia lain atau isi suku di **Asumsi lanjutan**. |
| *ECB tidak mencakup <mata uang>* | ECB hanya menyediakan inflasi kawasan euro; pilih penyedia **Inflasi** lain di Pengaturan. |
| *OECD tidak mencakup <mata uang>* | OECD tidak memiliki seri untuk mata uang ini (misalnya RUB); pilih penyedia **Inflasi** lain di Pengaturan. |

**Sumber dana.** Kantong yang boleh mendanai tujuan ini. **Hubungkan kantong** menambahkan satu kantong dengan batas kontribusi:

- **Otomatis — hingga sisa kebutuhan**: kantong memberikan berapa pun yang masih dibutuhkan tujuan, setelah tujuan sebelumnya mengambil bagiannya.
- **Jumlah tetap dalam mata uang tujuan**.
- **% dari kantong**: paling banyak porsi itu dari nilai kantong.
- **% dari tujuan**: paling banyak porsi itu dari target.

Pratinjau di editor menunjukkan kontribusi koneksi tersebut hari ini. Batas adalah nilai maksimum: urutan tujuan, tabungan yang tersedia, dan koneksi lain dapat mengurangi kontribusi. Setiap sumber yang tercantum menunjukkan berapa yang diberikannya sekarang dan mengapa tidak lebih; **Ubah koneksi** mengubah batasnya, **Putuskan koneksi** menghapusnya.

**Rencana tabungan.** Rencana yang sampai ke tujuan ini, masing-masing dengan jumlah yang ditetapkan proyeksi untuknya. Rencana yang sudah habis dipakai tujuan sebelumnya tidak muncul di sini.

**Membaca proyeksi.** "Rencana tabungan memenuhi tujuan ini pada 20 Des 2026 · tepat waktu" berarti akumulasi rencana hingga tanggal itu menutupi sisa kebutuhan sebelum tenggat. "Memenuhi hingga … · kurang …" berarti rencana tidak cukup; tambahkan rencana, geser tanggalnya, atau turunkan target.
