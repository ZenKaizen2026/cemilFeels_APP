# Product Requirement Document (PRD)
## Aplikasi Mobile: CEMILFEELS - Mood-Based Food Ordering App

* **Nama Proyek:** Cemilfeels Mobile App
* **Versi:** 1.1 (Pure Kotlin Native - Non-Database Final Project with Figma Style Guide Integration)
* **Platform:** Android (Primary - Kotlin / XML Layouts)
* **IDE:** Antigravity IDE / Android Studio

---

## 1. Ringkasan Eksekutif & Tujuan
Cemilfeels Mobile App adalah aplikasi inovatif non-database yang dirancang sebagai koping mekanisme untuk mengatasi stres akademik mahasiswa melalui kurasi camilan lokal. Aplikasi mendeteksi suasana hati pengguna (*mood*) secara instan melalui pilihan emotikon atau teks keluh kesah menggunakan logika pemrosesan lokal (*Kotlin Local Logic*), kemudian menyajikan menu camilan yang sesuai guna memulihkan suasana perasaan.

---

## 2. Alur Kerja Sederhana (Simple Workflow)
Sesuai rancangan alur antarmuka pengguna, berikut tahapan interaksi sederhana di dalam aplikasi:
1.  **Welcome / Splash Screen:** Menampilkan branding awal "CEMIL FEELS" dengan tombol "Start".
2.  **Authentication Screen:** Halaman Login dengan input Email & Password, atau opsi login pihak ketiga (Google).
3.  **Venting / Mood Selection Screen:** Ruang interaktif bagi pengguna untuk memilih emotikon (Bahagia, Sedih, Biasa aja, Cemas, Marah) atau mengetik keluh kesah secara bebas (0-1000 karakter).
4.  **Mood-Based Recommendation Screen:** Bottom sheet atau halaman katalog yang menyaring list camilan lokal berdasarkan analisis kata kunci suasana hati.
5.  **Cart & Checkout Screen:** Ringkasan pesanan, kustomisasi level kepedasan/catatan, perhitungan total harga + ongkir statis secara lokal.
6.  **Payment Processing:** Integrasi Implicit Intent untuk membuka aplikasi E-Wallet terpasang (misal ShopeePay) atau menampilkan fallback QRIS lokal jika aplikasi e-wallet tidak ditemukan.
7.  **Order Confirmation & Success Screen:** Halaman transisi konfirmasi pembayaran berhasil beserta data total tagihan dan waktu transaksi.
8.  **Live Order Status Tracker:** Simulasi pelacakan posisi/status makanan (*Menunggu -> Disiapkan -> Dikirim -> Tiba*) memanfaatkan hitung mundur Coroutines Delay secara lokal.

---

## 3. Panduan Gaya Desain & Spesifikasi Komponen (Style Guide & Layout Spec)
*Diperbarui berdasarkan dokumen Figma Team Library & Prototype Design terlampir:*

### A. Palet Warna & Tema Visual
Desain menerapkan pendekatan Material Design modern dengan nuansa warna bersahabat dan bersih:
* **Warna Utama (Primary Color):** Nuansa oranye pastel/salem yang hangat (membangkitkan nafsu makan dan kenyamanan emosional).
* **Warna Latar Belakang (Background Color):** Putih bersih (`#FFFFFF`) dengan aksen abu-abu terang (`#F5F5F5`) untuk memisahkan area card.
* **Warna Teks (Typography Color):** Abu-abu gelap / Hitam lembut untuk kontras optimal.

### B. Spesifikasi Halaman & Komponen (Figma Mapped)
1.  **Halaman Utama (Welcome & Login):**
    * Teks branding: **"MIL FEELS - Welcome! Discover your mood today."**
    * Tombol aksi utama berukuran besar dengan sudut membulat (*Rounded Button*).
2.  **Halaman Venting Space (Mood Picker):**
    * Elemen input teks *Card Box* transparan berbingkai tipis dengan indikator karakter `0/1000`.
    * Grid horizontal/vertical untuk 5 tombol emosi: **Bahagia, Sedih, Biasa aja, Cemas, Marah**.
    * Tombol konfirmasi: **"Siap, cari camilan!"**
3.  **Katalog Camilan (Recommendation Grid):**
    * Menggunakan komponen **ViewPager2** atau **RecyclerView** dalam bentuk grid kartu horizontal/vertikal.
    * Setiap kartu berisi: Foto produk (diambil dari folder `drawable`), Nama Camilan (Contoh: *Basreng*), Rating bintang (`4.5`), Deskripsi singkat (*"Pedasnya basreng nagih, instan usir bad mood..."*), Harga (`Rp. 16.000`), dan tombol tambah (`+`).
4.  **Halaman Transaksi (Checkout & Status):**
    * **Live Order Status Grid:** Menampilkan *Vertical Progress Line* (Garis status tahapan pengantaran) yang diperbarui otomatis oleh *timer loop*.
    * Informasi Detail: Nomor Order ID unik (Contoh: `#CML088228642625`), estimasi menit kedatangan, dan tombol "Salin".

---

## 4. Spesifikasi Teknis Pemrograman (Kotlin Local Logic)
* **Data & State Management:** Semua data camilan dikelola menggunakan model `data class` di dalam objek list Kotlin statis. State perubahan keranjang belanja dan status pelacakan diikat dengan `StateFlow` atau `LiveData` di dalam `MainActivity.kt`.
* **Aset Drawable:** Gambar/foto produk diletakkan di dalam folder `res/drawable` dengan format penamaan huruf kecil dan garis bawah (`snake_case`). Ikon UI menggunakan aset bawaan Android Studio (*Vector Assets*) sebagai placeholder sementara sebelum diganti secara modular.
* **Logika Pengkondisian:** Menggunakan fungsi pencocokan string (`.contains()`) untuk memetakan kata-kata keluh kesah pengguna ke kategori makanan tertentu (misal kata "stres/marah" memicu rekomendasi makanan pedas seperti Basreng).
