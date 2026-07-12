# Panduan Desain UI/UX (Design Guidelines) - Cemil_feels

Dokumen ini disusun berdasarkan analisis menyeluruh terhadap seluruh layout XML (`app/src/main/res/layout`) pada proyek **Cemil_feels**. Tujuan dokumen ini adalah menjadi **sumber kebenaran tunggal (Single Source of Truth)** bagi aspek desain UI/UX, sehingga penambahan fitur di masa mendatang tetap konsisten dengan identitas visual aplikasi.

---

## 1. Identitas Visual & Tema Dasar
Aplikasi **Cemil_feels** mengusung tema yang hangat, ramah, dan menenangkan (*warm, friendly, and mood-boosting*). Ini dicapai dengan penggunaan warna pastel hangat (Salem Orange), sudut-sudut elemen yang sangat membulat (*high rounded corners*), serta layout yang bersih dengan ruang bernapas (*breathable whitespace*).

---

## 2. Palet Warna (Color Palette)

### A. Warna Utama (Brand Colors)
Seluruh warna brand wajib didefinisikan di `app/src/main/res/values/colors.xml` dan **tidak boleh ditulis secara hardcoded (hex code)** di dalam layout XML.

| Nama Warna | Nilai Hex | Penggunaan Utama | Keterangan |
| :--- | :--- | :--- | :--- |
| `colorPrimary` | `#FF914D` | Tombol utama, aksen aktif, teks penting, chip terpilih | Salem/pastel orange hangat |
| `colorPrimaryDark`| `#E05A1F` | Header penting, harga snack, teks brand utama | Orange gelap yang kontras |
| `colorAccent` | `#FF7043` | Aksen sekunder, icon highlight | Orange kemerahan |
| `colorBackground` | `#FAF8F5` | Background layar utama | Warm off-white |
| `surfaceColor` | `#FFFFFF` | Background CardView, Dialog, BottomSheet | Putih bersih |

> [!WARNING]
> **Temuan Inkonsistensi Warna:**
> - Terdapat warna background hardcoded `#FFF9F5` (di `activity_cart`, `activity_checkout`, dan `activity_order_status`) dan `#FFF9F4` (di `activity_venting`). Ini harus disatukan menjadi `@color/colorBackground`.
> - Banyak tombol menggunakan background tint hardcoded `#FF7A1A` alih-alih `@color/colorPrimary`. Ini membuat warna orange tombol berbeda di beberapa halaman.

### B. Warna Teks (Text Colors)
| Nama Warna | Nilai Hex | Penggunaan Utama | Keterangan |
| :--- | :--- | :--- | :--- |
| `@color/colorTextDark` | `#2C2C2C` | Judul, teks utama, isi konten | Abu-abu sangat gelap (bukan hitam pekat #000) |
| `@color/textSecondary` | `#757575` | Subtitle, label sekunder, deskripsi panjang | Abu-abu sedang |
| `@android:color/darker_gray` | `#9E9E9E` | Teks non-aktif, placeholder hint, divider | Abu-abu muda |

> [!WARNING]
> **Temuan Inkonsistensi Teks:**
> Teks gelap menggunakan variasi hardcoded: `#212121` (Cart/Checkout), `#333333` (Venting), `#222222` (Dialog Detail). Semuanya harus disatukan menggunakan `@color/colorTextDark` (`#2C2C2C`) untuk konsistensi kontras.

### C. Warna Fungsional & Status
| Status | Warna Hex | Penggunaan Utama | Keterangan |
| :--- | :--- | :--- | :--- |
| **Success** | `#2E7D32` (Teks)<br>`#E8F5E9` (Background Panel) | Status Selesai, Pembayaran Berhasil | Hijau Daun (Soft Green) |
| **Warning/Star**| `#F9A825` atau `#F39C12` | Bintang rating snack, level pedas | Kuning/Gold hangat |
| **Dividers** | `#EEEEEE` atau `#EAEAEA` | Garis pemisah antar section | Abu-abu ultra-light |

---

## 3. Tipografi (Typography)
Ukuran huruf harus mengikuti skala proporsional untuk menjaga hierarki informasi. Gunakan atribut `android:textSize` dan `android:textStyle` secara konsisten:

1. **Header Utama / Judul Halaman:**
   - Ukuran: `20sp` s.d. `24sp`
   - Style: `bold`
   - Contoh: Judul Keranjang (`22sp`), Live Order Status (`22sp`), Judul Checkout (`24sp`).
2. **Sub-header / Judul Section:**
   - Ukuran: `16sp` s.d. `18sp`
   - Style: `bold`
   - Contoh: Teks "Pilih Mood Kamu" (`18sp`), Nama Snack di Detail (`16sp`).
3. **Teks Utama / Nama Item / Konten:**
   - Ukuran: `14sp` s.d. `15sp`
   - Style: `bold` atau `regular` (tergantung penekanan)
   - Contoh: Nama snack di list (`15sp`), Level Kepedasan (`14sp`).
4. **Teks Sekunder / Deskripsi:**
   - Ukuran: `12sp` s.d. `13sp`
   - Style: `regular` (tanpa bold)
   - Contoh: Subtitle halaman, deskripsi produk, estimasi tiba.
5. **Teks Mikro / Keterangan Tambahan:**
   - Ukuran: `11sp` s.d. `9sp`
   - Style: `regular`
   - Contoh: Versi aplikasi (`12sp`), Info jatuh tempo (`11sp`), Label level pedas minimum (`9sp`).

---

## 4. Komponen Tombol (Buttons)

Semua tombol utama wajib menggunakan komponen Material Components (`com.google.android.material.button.MaterialButton`).

### A. Panduan Dimensi Tombol
Untuk menghindari tombol yang terlihat terlalu kaku atau berubah-ubah ukurannya di setiap halaman, gunakan panduan berikut:

| Tipe Tombol | Tinggi (`layout_height`) | Sudut (`cornerRadius`) | Ukuran Teks (`textSize`) | Keterangan |
| :--- | :--- | :--- | :--- | :--- |
| **Tombol Utama (Action)** | `56dp` atau `58dp` | `16dp` s.d. `18dp` | `16sp` s.d. `17sp` (Bold) | Contoh: *Get Started*, *Sign In*, *Checkout*, *Bayar* |
| **Tombol Sekunder (Outline)**| `54dp` | `12dp` s.d. `16dp` | `15sp` (Bold/Medium) | Contoh: *Kembali ke Home*, *Order Again* |
| **Tombol Kecil (Item)** | `36dp` atau `32dp` | `8dp` atau `12dp` | `12sp` s.d. `13sp` (Bold) | Contoh: Tombol tambah snack (`item_snack`), tombol `+`/`-` (`item_cart`) |

> [!WARNING]
> **Temuan Inkonsistensi Tombol:**
> Saat ini, sudut tombol (`cornerRadius`) sangat acak di berbagai layout: `28dp` (Venting), `26dp` (Order Status), `25dp` (Cart), `18dp` (Login/Welcome), `16dp` (Payment/Rec), `12dp` (Success/QRIS), dan default (Main).
> **Rekomendasi:** Gunakan **`16dp`** sebagai standar universal untuk seluruh Tombol Utama/Sekunder di halaman transaksi/konten untuk menyelaraskan tampilan visual.

---

## 5. Komponen Kartu (CardViews)

Aplikasi Cemil_feels menggunakan `com.google.android.material.card.MaterialCardView` untuk mengelompokkan informasi.

### A. Ketentuan Corner Radius & Elevasi Kartu
Untuk menciptakan hierarki visual yang konsisten, gunakan skala corner radius berikut:

*   **Small (`8dp` s.d. `12dp`):**
    *   Penggunaan: Kartu item kecil seperti foto produk, card item keranjang (`item_cart`), card item snack (`item_snack`).
    *   Elevasi: `2dp` s.d. `4dp`.
*   **Medium (`16dp` s.d. `20dp`):**
    *   Penggunaan: Kartu grup info sekunder (Info Pembayaran, Detail Alamat, Detail Qris, Bubble chat/Venting input).
    *   Elevasi: `2dp` s.d. `6dp`.
*   **Large (`24dp` s.d. `32dp`):**
    *   Penggunaan: Kartu utama layar (*Hero Banner* Home, Panel Login Utama, Panel Detail Transaksi Dialog, Logo Card di Welcome).
    *   Elevasi: `8dp` s.d. `10dp`.

---

## 6. Kolom Input (Text Inputs)

Semua input teks wajib menggunakan Material TextInputLayout OutlinedBox.

### A. Struktur Standar XML TextInputLayout:
```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Label Input"
    app:boxCornerRadiusTopStart="18dp"
    app:boxCornerRadiusTopEnd="18dp"
    app:boxCornerRadiusBottomStart="18dp"
    app:boxCornerRadiusBottomEnd="18dp"
    app:boxStrokeColor="@color/colorPrimary"
    app:hintTextColor="@color/colorPrimary">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/et_input_id"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textColor="@color/colorTextDark"
        android:inputType="text" />
</com.google.android.material.textfield.TextInputLayout>
```

> [!WARNING]
> **Temuan Inkonsistensi Input:**
> - `activity_main` menggunakan `TextInputLayout` tanpa `boxCornerRadius` kustom (sudutnya tajam bawaan material).
> - `activity_venting` menggunakan custom `MaterialCardView` yang membungkus raw `EditText` transparan untuk area cerita.
> - **Rekomendasi:** Untuk input cerita/venting panjang, gunakan `TextInputLayout` dengan `boxCornerRadius="18dp"` dan set `TextInputEditText` dengan tinggi kustom dan `gravity="top|start"`. Ini menjaga konsistensi visual field input di semua layar.

---

## 7. Layout Spacing & Grid (Paddings & Margins)

Gunakan kelipatan **8dp** sebagai grid spacing standar untuk margin dan padding guna menjaga keseimbangan layout:

*   **Screen Padding:** `24dp` untuk halaman pembuka (Welcome, Login, Success), dan `20dp` untuk halaman konten padat (Home, Checkout, Cart).
*   **Vertical Spacing (Antar Elemen Besar):** `24dp` atau `28dp` (misal: jarak dari Greeting ke Search Bar).
*   **Element Group Spacing (Antar Elemen Sedang):** `16dp` (misal: jarak antar form input, jarak antar item kartu).
*   **Sub-element Spacing (Antar Elemen Kecil/Teks):** `8dp` atau `12dp` (misal: jarak dari Judul Snack ke Rating Bintang, jarak dari label input ke TextInputLayout).

---

## 8. Konsistensi Bahasa UI (Linguistic Consistency)
Saat ini ada percampuran bahasa antara bahasa Inggris dan bahasa Indonesia di UI.
*   *Bahasa Inggris:* "Good Afternoon", "See All", "Sign In", "Sign Up", "Forgot Password?", "Live Order Status".
*   *Bahasa Indonesia:* "Keranjang Belanja", "Rincian Transaksi", "Pilih Mood Kamu", "Atau mau cerita?".

**Rekomendasi:**
Disarankan untuk melakukan lokalisasi penuh ke **Bahasa Indonesia** karena target pengguna aplikasi ini bersifat lokal.
*   *Sign In* -> Masuk
*   *Sign Up* -> Daftar
*   *Forgot Password?* -> Lupa Kata Sandi?
*   *See All* -> Lihat Semua
*   *Live Order Status* -> Status Pesanan Langsung

---

## 9. Checklist Konsistensi Sebelum Merge / Rilis Fitur Baru
Gunakan checklist ini saat membuat layout XML baru:
- [ ] Apakah warna background menggunakan `@color/colorBackground`?
- [ ] Apakah teks menggunakan `@color/colorTextDark` untuk teks utama dan `@color/textSecondary` untuk teks sekunder?
- [ ] Apakah tidak ada kode warna hex hardcoded (seperti `#FFF9F5` atau `#FF7A1A`) di layout XML?
- [ ] Apakah tinggi tombol utama diatur ke `56dp`/`58dp` dengan `cornerRadius="16dp"`?
- [ ] Apakah text input menggunakan `TextInputLayout.OutlinedBox` dengan `boxCornerRadius="18dp"`?
- [ ] Apakah semua CardView menggunakan radius terstandar (`8dp` / `16dp` / `24dp`)?
- [ ] Apakah spacing/margin mengikuti kelipatan 8dp (`8dp`, `16dp`, `24dp`, `32dp`)?
