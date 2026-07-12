# PANDUAN EKSTRAKSI DRAF PRESENTASI UAS ANDROID (KOTLIN)

## 🎯 TUJUAN
Kamu adalah seorang Asisten AI Ahli Pemrograman Android Native. Tugasmu adalah menganalisis seluruh *source code* dalam proyek Android Studio ini, lalu membuat ringkasan informasi teknis yang siap digunakan untuk bahan presentasi UAS kelompok. 

Aplikasi ini bersifat native (Kotlin & XML Layout) dan menggunakan **SharedPreferences** sebagai media penyimpanan utama (Tanpa Database SQL/Room).

---

## 📋 INSTRUKSI ANALISIS & OUTPUT
Ekstrak informasi dari *source code* proyek ini dan susun hasilnya ke dalam 4 bagian utama berikut:

### BAGIAN 1: Konsep Aplikasi, MVP, & Tampilan Utama (XML Layout)
*   **Nama & Deskripsi Aplikasi:** Apa nama aplikasinya dan apa fungsi utamanya?
*   **Minimum Viable Product (MVP):** Sebutkan batasan fitur minimal yang membuat aplikasi ini sudah bisa berfungsi dan layak dinilai sebagai proyek UAS.
*   **Komponen UI (XML Layout):** Sebutkan file `.xml` utama yang digunakan. Identifikasi jenis layout (misal: `ConstraintLayout`, `LinearLayout`) dan komponen UI apa saja yang dipakai (sebutkan contohnya seperti `RecyclerView`, `EditText`, `Button`, `TextView`, dll).

### BAGIAN 2: Alur Logika Aplikasi & Fitur Khas Kotlin
*   **Activity/Fragment Lifecycle:** Jelaskan secara singkat di *activity* mana logika utama diinisialisasi (misal: di dalam fungsi `onCreate`).
*   **Navigasi & Oper Data (Intent):** Apakah ada perpindahan halaman menggunakan `Intent`? Jika ya, sebutkan file *activity* asal dan tujuannya. Apakah ada pengiriman data antar halaman memakai `.putExtra()`?
*   **Fitur Spesifik Kotlin:** Cari dan sebutkan penerapan fitur modern Kotlin di dalam kode, seperti penggunaan *View Binding* / *Data Binding*, fitur *Null Safety* (`?` atau `!!`), atau struktur kontrol khusus Kotlin lainnya yang membuat kode aman dari *crash*.

### BAGIAN 3: Manajemen Penyimpanan (SharedPreferences)
*   **Alasan Teknis:** Tuliskan justifikasi singkat mengapa aplikasi ini cukup menggunakan `SharedPreferences` dibanding database relasional.
*   **Proses Menulis Data (Save):** Tunjukkan potongan kode kodingan Kotlin (*code snippet*) bagian inisialisasi `SharedPreferences.Editor`, fungsi penulisan data (seperti `putString` / `putInt`), dan metode penguncian data (`apply()` atau `commit()`).
*   **Proses Membaca Data (Load/Fetch):** Tunjukkan potongan kode kodingan Kotlin saat memanggil data yang tersimpan menggunakan fungsi `getString` / `getInt`, lengkap dengan nilai *default*-nya jika data kosong.

### BAGIAN 4: Skenario Live Demo & Kesimpulan
*   **Alur Pengujian (Step-by-Step):** Tuliskan skenario ideal untuk mendemokan aplikasi ini di depan dosen (Mulai dari aplikasi dibuka kosong -> Input Data -> Simpan -> Aplikasi Dihapus dari Recent Apps -> Aplikasi Dibuka Kembali untuk membuktikan data di `SharedPreferences` tetap ada).
*   **Kesimpulan Teknis:** Tuliskan kesimpulan singkat mengenai efisiensi penggunaan Kotlin dan SharedPreferences pada skala proyek ini.

---

## 🚀 FORMAT OUTPUT YANG DIHARAPKAN
Keluarkan hasil analisismu dalam format Markdown yang rapi, padat, langsung pada intinya, tanpa basa-basi bertele-tele, agar mudah saya pelajari dan saya bawa kembali ke AI eksternal saya untuk disusun menjadi kerangka presentasi final.