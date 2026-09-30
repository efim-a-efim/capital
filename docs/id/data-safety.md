---
layout: default
lang: id
base: "/id"
key: "data-safety"
title: Deklarasi keamanan data
class: doc
---
# Deklarasi keamanan data

<p class="meta">Jawaban untuk formulir Google Play Console (Kebijakan dan program → Konten aplikasi → Keamanan data), beserta alasan di balik setiap jawaban. Diperiksa terhadap versi 2.2.1 pada 30 September 2026. <a href="{{ page.base }}/privacy">Kebijakan Privasi</a> adalah pernyataan untuk pengguna mengenai fakta yang sama.</p>

## Cara aplikasi menangani data

Capital tidak memiliki backend. Semua yang dimasukkan pengguna tetap berada di folder pada perangkat. Satu-satunya data yang pernah keluar dari perangkat adalah data yang dikirim aplikasi, atas perintah pengguna, ke operator data pihak ketiga yang dipilih pengguna di Pengaturan: alamat dompet publik, ID kontrak token, kode mata uang, dan kunci API yang dimasukkan pengguna untuk operator tersebut. Operator menjawab permintaan; aplikasi menyimpan saldo dan harga yang diterima secara lokal dan tidak menyimpan salinan permintaannya. Tidak ada SDK di aplikasi yang mengirim data ke pihak mana pun: dependensinya hanya AndroidX, Kotlin, OkHttp, Bouncy Castle, dan ZXing (render QR, offline). Layar tip menampilkan alamat statis yang tertanam di aplikasi dan tidak mengirim apa pun.

Google Play menganggap data *dikumpulkan* ketika data itu dikirim keluar dari perangkat, meskipun tidak melibatkan server developer dan pemrosesannya bersifat sementara, sehingga deklarasinya bukan "tidak mengumpulkan apa pun". Yang dideklarasikan adalah satu jenis data yang bersifat sementara dan opsional.

## Jawaban formulir

### Ringkasan

| Pertanyaan | Jawaban |
|---|---|
| Does your app collect or share any of the required user data types? (Apakah aplikasi Anda mengumpulkan atau membagikan salah satu jenis data pengguna yang diwajibkan?) | **Yes** (Ya) |
| Is all of the user data collected by your app encrypted in transit? (Apakah semua data pengguna yang dikumpulkan aplikasi Anda dienkripsi saat dalam pengiriman?) | **Yes** (Ya) — hanya HTTPS; lalu lintas cleartext dinonaktifkan di manifest |
| Do you provide a way for users to request that their data is deleted? (Apakah Anda menyediakan cara bagi pengguna untuk meminta agar data mereka dihapus?) | **Yes** (Ya) — tidak ada data yang disimpan setelah permintaan selesai, sehingga memenuhi aturan "dihapus dalam 90 hari setelah dikumpulkan" untuk lencana tersebut. Pengguna menghapus data di perangkat dengan menghapus folder dan mencopot aplikasi; lihat Kebijakan Privasi. |

### Jenis data

Pilih tepat satu jenis.

| Kategori | Jenis data | Collected (Dikumpulkan) | Shared (Dibagikan) | Ephemeral (Sementara) | Required or optional (Wajib atau opsional) | Purposes (Tujuan) |
|---|---|---|---|---|---|---|
| Info keuangan (Financial info) | Info keuangan lainnya (Other financial info) | Yes (Ya) | No (Tidak) | **Yes** (Ya) | **Optional** (Opsional) | Fungsi aplikasi (App functionality) |

Cakupan jenis ini: alamat blockchain publik yang dipantau pengguna, kontrak token yang ditemukan di alamat tersebut, dan kode mata uang dari aset pengguna. Data ini dikirim ke operator data yang dipilih pengguna agar saldo dan harga dapat diambil, disimpan di memori selama permintaan berlangsung, lalu dibuang.

Mengapa **tidak dibagikan**: transfer berlangsung langsung dari perangkat ke operator yang dipilih pengguna, pada pembaruan yang dimulai oleh pengguna, setelah aplikasi memberi tahu pengguna di Pengaturan operator mana yang akan dikueri dan bahwa permintaan tersebut mengungkapkan alamat dan IP kepada operator itu. Ini adalah pengecualian "tindakan yang dimulai pengguna ketika pengguna secara wajar mengharapkan data dibagikan". Developer tidak menerima apa pun dan tidak memiliki penyedia layanan.

Mengapa **opsional**: aplikasi dapat digunakan sepenuhnya hanya dengan aset manual. Alamat dan kunci API dimasukkan atas pilihan pengguna.

Kunci API yang dimasukkan pengguna hanya dikirim ke operator yang menerbitkannya. Kunci itu adalah kredensial pengguna untuk layanan milik operator tersebut dan tidak dideklarasikan sebagai jenis data pengguna tersendiri; jika peninjau bertanya, jelaskan seperti di atas.

### Jenis yang **tidak** dikumpulkan

Semua kategori lain dijawab "No" (Tidak): tidak ada lokasi, info pribadi, kontak, pesan, foto atau media, file dan dokumen, aktivitas aplikasi, penjelajahan web, info dan performa aplikasi (tidak ada log error, tidak ada diagnostik), maupun ID perangkat atau ID lainnya. Alamat IP sampai ke operator sebagai bagian dari setiap permintaan HTTPS dan tidak digunakan oleh aplikasi untuk tujuan apa pun.

Catatan keuangan pengguna (aset, tujuan, rencana) hanya diproses di perangkat dan berada di luar cakupan formulir.

### Praktik keamanan

| Item | Jawaban |
|---|---|
| Independent security review (MASA) (Tinjauan keamanan independen) | No (Tidak) |
| Committed to follow the Families policy (Berkomitmen mematuhi kebijakan Keluarga) | No (Tidak) (bukan aplikasi untuk anak-anak) |

## Deklarasi terkait di halaman Konten aplikasi

| Deklarasi | Jawaban |
|---|---|
| URL kebijakan privasi | `{{ site.url }}/privacy` |
| Iklan | Tidak, aplikasi tidak berisi iklan |
| Akses aplikasi | Semua fungsi tersedia tanpa akses khusus. Tanpa login. Kunci API penyedia bersifat opsional; setiap penyedia punya pilihan default tanpa kunci. |
| Rating konten (IARC) | Kuesioner utilitas / produktivitas; tanpa kekerasan, konten seksual, perjudian, zat terlarang, interaksi pengguna, atau berbagi lokasi. Hasil yang diharapkan: Semua Umur (Everyone) / PEGI 3. |
| Target audiens dan konten | 18 tahun ke atas (alat keuangan pribadi; tidak dirancang untuk anak-anak) |
| Aplikasi berita | Tidak |
| Pelacakan kontak dan status COVID-19 | Tidak |
| Keamanan data | Seperti di atas |
| Aplikasi pemerintah | Tidak |
| Fitur keuangan | Lihat [Deklarasi fitur keuangan]({{ page.base }}/financial-features) |
| Aplikasi kesehatan | Tidak ada fitur kesehatan |

## Apa yang perlu diperbarui saat aplikasi berubah

Periksa ulang halaman ini ketika rilis menambahkan analitik, pelaporan error, akun, server yang dijalankan developer, SDK baru dengan akses jaringan, atau berbagi data di perangkat dengan aplikasi lain. Setiap perubahan tersebut mengubah jawaban formulir.
