# Esensi Pemrograman Berorientasi Objek (OOP) Kotlin untuk Android

Dokumentasi ini merangkum ringkasan inti dari 4 pilar dasar Object-Oriented Programming (OOP) menggunakan Kotlin yang menjadi fondasi utama dalam pengembangan aplikasi di Android Studio.

---

## 1. Fondasi Utama: Class & Object
* [cite_start]**Class**: *Blueprint* atau cetakan dasar yang mendefinisikan struktur (Property) dan perilaku (Method)[cite: 452].
* [cite_start]**Object**: Hasil nyata atau *instance* konkret yang dibuat langsung dari Class[cite: 457].
* [cite_start]**Property & Method**: Property adalah variabel penyimpan data objek [cite: 485][cite_start], sedangkan Method adalah aksi/fungsi yang bisa dilakukan oleh objek tersebut[cite: 494, 496].

## 2. Pilar I: Inheritance (Pewarisan)
[cite_start]Mekanisme saat sebuah *child class* mewarisi sifat dan perilaku dari *parent class* untuk menghindari duplikasi kode (*Don't Repeat Yourself / DRY*)[cite: 220, 228, 231].
* **Keyword `open`**: Di Kotlin, secara default class bersifat *close*. [cite_start]Properti atau method harus ditandai `open` agar bisa diturunkan atau di-*override*[cite: 265, 268].
* [cite_start]**Keyword `override`**: Digunakan di *child class* untuk menulis ulang atau memodifikasi implementasi method dari *parent class*[cite: 271].
* [cite_start]**Penerapan di Android**: Terlihat jelas saat membuat `MainActivity` yang merupakan turunan dari `AppCompatActivity` [cite: 60, 63][cite_start], di mana kita melakukan `override fun onCreate()` untuk menginisialisasi UI[cite: 65, 66].

## 3. Pilar II: Polymorphism (Banyak Bentuk)
[cite_start]Kemampuan suatu objek untuk memiliki banyak bentuk atau perilaku yang berbeda, meskipun dipanggil melalui satu perintah atau method yang sama[cite: 20, 21, 22].
* [cite_start]**Runtime Polymorphism**: Kotlin menentukan method mana yang dipanggil saat program berjalan berdasarkan *instance* objeknya, bukan saat kompilasi[cite: 54].
* [cite_start]**Penerapan di Android**: Sangat berguna untuk membuat sistem yang fleksibel, contohnya membuat method `hitungGaji()` yang otomatis berperilaku berbeda tergantung apakah objeknya Staf Tetap atau Kontrak[cite: 181].

## 4. Pilar III: Encapsulation (Penyembunyian Data)
[cite_start]Prinsip melindungi integritas data internal dengan menyembunyikannya dari akses luar secara sembarangan, dan hanya mengizinkan akses lewat method yang terkontrol[cite: 76, 78, 79, 118].
* **Access Modifiers**:
    * [cite_start]`private`: Hanya bisa diakses di dalam class/file yang sama (Sangat penting untuk enkapsulasi)[cite: 90, 285, 286].
    * [cite_start]`protected`: Bisa diakses di class yang sama dan semua *subclass*-nya[cite: 96, 284].
    * [cite_start]`internal`: Hanya bisa diakses di dalam satu module yang sama[cite: 94, 288].
    * [cite_start]`public`: Bisa diakses dari mana saja (Modifier default di Kotlin)[cite: 92, 281].
* [cite_start]**Custom Getters & Setters**: Digunakan sebagai pengawal data untuk memvalidasi input sebelum disimpan ke properti (misal: memastikan input umur tidak negatif atau format email valid)[cite: 97, 98, 107, 164].

## 5. Pilar IV: Abstraction (Abstraksi)
[cite_start]Menyembunyikan detail implementasi yang rumit dan hanya menampilkan fungsi penting/esensinya saja kepada pengguna[cite: 564].
* [cite_start]**Abstract Class**: Kerangka dasar (*blueprint*) yang tidak bisa diinstansiasi langsung dan memaksa *child class* untuk mengimplementasikan *abstract method*-nya[cite: 307, 308, 310].
* [cite_start]**Interface**: Sebuah kontrak perilaku tanpa menyimpan *state* objek, memungkinkan satu class memiliki banyak kemampuan (*multiple interface*)[cite: 334, 338, 344].