---
layout: default
lang: id
base: "/id"
key: "privacy"
title: Kebijakan Privasi
class: doc
---
# Kebijakan Privasi

<p class="meta">Capital untuk Android (paket <code>dev.capital</code>) · Developer: {{ site.developer }} · Berlaku sejak 30 September 2026</p>

## Ringkasan

- Capital tidak memiliki akun pengguna, analitik, iklan, pelaporan error, maupun server yang dijalankan oleh developer. Developer tidak pernah menerima data Anda.
- Catatan keuangan Anda hanya disimpan di perangkat Anda, di folder yang Anda pilih. Anda dapat mengenkripsinya dengan kata sandi.
- Satu-satunya lalu lintas jaringan adalah permintaan yang dikirim aplikasi, atas perintah Anda, ke operator data harga dan blockchain yang Anda pilih di Pengaturan. Permintaan tersebut memuat alamat dompet publik, kontrak token, dan kode mata uang yang Anda pantau, serta kunci API yang Anda masukkan untuk operator tersebut.

## Apa yang disimpan aplikasi di perangkat Anda

**Di folder yang Anda pilih.** Kantong, aset, alamat dompet, tujuan, koneksi, rencana tabungan, harga tersimpan, dan pengaturan yang terkait dengan data tersebut. File berupa teks biasa kecuali Anda mengaktifkan enkripsi (Pengaturan → Keamanan). Dengan enkripsi aktif, setiap file dienkripsi dengan AES-256-GCM menggunakan kunci yang diturunkan dari kata sandi Anda dengan Argon2id. Kata sandi tidak dapat dipulihkan.

**Di penyimpanan privat aplikasi** (tidak dapat diakses aplikasi lain):

| Item | Tujuan |
|---|---|
| Izin akses ke folder yang dipilih | Membuka kembali folder saat aplikasi diluncurkan berikutnya |
| Kunci API penyedia yang Anda masukkan | Hanya dikirim ke operator yang menerbitkannya; dienkripsi dengan kunci yang disimpan di Android Keystore; tidak disertakan dalam snapshot, ekspor, maupun cadangan sistem operasi |
| Pengaturan kunci | Membuka folder terenkripsi tanpa kata sandi: salinan kunci data, dienkripsi dengan kunci yang diturunkan dari PIN Anda dan terikat ke Android Keystore. PIN itu sendiri tidak disimpan |
| Pilihan bahasa dan tema | Preferensi antarmuka |

Pencadangan Android dan transfer antarperangkat dinonaktifkan untuk aplikasi ini, sehingga sistem tidak menyalin data di atas ke Google atau ke perangkat lain.

## Apa yang keluar dari perangkat Anda

Capital hanya menghubungi operator yang Anda pilih di Pengaturan, hanya melalui HTTPS, dan hanya saat Anda memperbarui atau menguji sumber. Setiap permintaan dijawab lalu dibuang; aplikasi menyimpan saldo dan harga yang diterima di folder Anda, bukan permintaannya.

| Data yang dikirim | Kepada siapa | Alasan |
|---|---|---|
| Alamat dompet publik yang Anda tambahkan | Operator data blockchain yang dipilih untuk jaringan tersebut | Membaca saldo dan kepemilikan token di alamat itu |
| Alamat kontrak token dan ID aset | Operator harga kripto yang Anda pilih | Menentukan harga aset |
| Kode mata uang | Operator kurs fiat yang Anda pilih | Mengonversi antarmata uang |
| Kunci API yang Anda masukkan untuk suatu operator | Hanya operator tersebut | Mengautentikasi akun Anda sendiri di operator itu |

Setiap operator juga melihat alamat IP Anda, seperti pada permintaan internet mana pun. Operator tidak terkait dengan developer dan memproses permintaan berdasarkan ketentuan dan kebijakan privasi mereka sendiri, yang ditautkan dari Pengaturan → Sumber / atribusi di aplikasi:

| Data | Operator |
|---|---|
| Bitcoin | [Blockstream](https://blockstream.info), [mempool.space](https://mempool.space) |
| Ethereum dan token ERC-20 | [PublicNode](https://publicnode.com), [Alchemy](https://www.alchemy.com), [Blockscout](https://www.blockscout.com), [Ethplorer](https://ethplorer.io) |
| TON dan jetton | [TON Center](https://toncenter.com), [TonAPI](https://tonapi.io) |
| TRON dan token TRC-20 | [TronGrid](https://www.trongrid.io), [PublicNode](https://publicnode.com) |
| Harga kripto | [DefiLlama](https://defillama.com), [CoinGecko](https://www.coingecko.com), [CoinPaprika](https://coinpaprika.com) |
| Kurs fiat | [Frankfurter](https://frankfurter.dev), [Bank Sentral Eropa](https://www.ecb.europa.eu) |

Tidak ada data yang dikirim ke tempat lain. Tidak ada data yang dijual, dibagikan untuk iklan, atau dipakai untuk membuat profil. Kueri ke blockchain publik mengungkapkan bahwa alamat yang Anda pantau menarik bagi seseorang di alamat IP Anda; gunakan VPN jika hal itu penting bagi Anda.

## Apa yang tidak pernah dilakukan aplikasi

- Aplikasi tidak pernah meminta, menyimpan, atau mengirim kunci privat maupun seed phrase. Aplikasi tidak dapat menandatangani atau mengirim transaksi.
- Aplikasi tidak pernah mentransfer uang. Alokasi tujuan hanyalah perhitungan yang ditampilkan kepada Anda, tidak lebih.
- Aplikasi tidak pernah menghubungi developer. Tidak ada telemetri, tidak ada pemeriksaan pembaruan di dalam aplikasi, tidak ada notifikasi push.

## Izin

| Izin | Kegunaan |
|---|---|
| Internet | Permintaan ke operator yang tercantum di atas |
| Akses folder | Diberikan oleh Anda melalui pemilih folder Android untuk folder yang Anda pilih; aplikasi tidak dapat membaca folder lain |
| Biometrik | Membuka kunci dengan sidik jari atau wajah melalui dialog bawaan Android; aplikasi hanya menerima hasil berhasil atau gagal, tidak pernah data biometrik |

## Sinkronisasi dan cadangan

Capital tidak menyinkronkan apa pun sendiri. Jika Anda meletakkan folder di bawah alat sinkronisasi (Syncthing, Nextcloud, Google Drive, …), ketentuan privasi alat tersebut berlaku untuk salinan yang dibuatnya. File berupa teks biasa kecuali enkripsi aktif; salinan tanpa enkripsi yang dibuat sebelum Anda mengaktifkan enkripsi tetap dapat dibaca oleh siapa pun yang memegangnya.

**Ekspor cadangan** di Pengaturan menulis satu file ke lokasi yang Anda pilih. File itu berisi data yang sama dan hanya seaman lokasi penyimpanannya.

## Menghapus data Anda

Hapus folder yang Anda pilih (beserta salinan yang dibuat alat sinkronisasi Anda) lalu copot aplikasi. Mencopot aplikasi menghapus penyimpanan privat aplikasi, termasuk kunci penyedia dan pengaturan kunci. Developer tidak menyimpan apa pun yang perlu dihapus dan tidak dapat menghapus apa pun atas nama Anda. Operator yang Anda kueri mungkin menyimpan log permintaan sesuai aturan retensi mereka sendiri.

## Anak-anak

Capital adalah alat keuangan pribadi untuk orang dewasa. Aplikasi ini tidak ditujukan untuk anak-anak di bawah 13 tahun dan tidak dengan sengaja mengumpulkan data dari mereka.

## Perubahan kebijakan ini

Versi terbaru selalu tersedia di [{{ site.url }}{{ page.base }}/privacy]({{ page.base }}/privacy). Perubahan penting dicantumkan dalam catatan rilis versi yang memperkenalkannya.

## Kontak

{{ site.developer }} · [{{ site.contact }}](mailto:{{ site.contact }}) · [Pelacak masalah]({{ site.repo }}/issues)
