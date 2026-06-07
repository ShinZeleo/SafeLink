# 🛡️ SafeLink - Ultimate Link Safety Scanner

[![Android](https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-orange?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![Security](https://img.shields.io/badge/Security-Encrypted-blue?style=for-the-badge&logo=shieldui)](https://sqlcipher.net/)
[![API](https://img.shields.io/badge/API-VirusTotal_v3-red?style=for-the-badge)](https://developers.virustotal.com/reference/overview)

**SafeLink** adalah solusi keamanan digital modern yang dirancang untuk melindungi pengguna dari ancaman phishing, malware, dan situs web berbahaya secara real-time. Dibangun dengan fokus pada privasi dan kemudahan penggunaan, SafeLink memastikan setiap link yang Anda klik adalah aman melalui analisis berlapis.

---

## ✨ Fitur Utama

### 🔍 Real-time Security Scan

Integrasi penuh dengan **VirusTotal API v3**. Setiap URL yang dimasukkan akan dikonversi menjadi ID Base64 (tanpa padding) dan diperiksa melalui puluhan engine antivirus global secara simultan.

### 📸 QR Code Intelligent Scanner

Menggunakan engine **ZXing (Zebra Crossing)** untuk ekstraksi URL langsung dari kode QR. Memungkinkan pemindaian cepat tanpa perlu input manual yang rawan kesalahan ketik.

### 🔐 Multi-Layer Persistence (SQLCipher)

Data riwayat dan bookmark tidak hanya disimpan secara lokal, tetapi juga dienkripsi menggunakan **AES-256** melalui **SQLCipher**. Ini memastikan data browsing Anda tidak dapat dibaca oleh aplikasi pihak ketiga atau akses file sistem yang tidak sah.

### 🌓 Premium UI & Glassmorphism

Antarmuka pengguna yang modern dengan dukungan penuh **Dark & Light Mode**. Menggunakan prinsip desain Glassmorphism dengan transparansi dan blur yang dioptimalkan untuk performa Android.

### 📊 Algoritma Security Score

Kalkulasi skor keamanan (0-100) berbasis algoritma internal yang menggabungkan:

- Hasil analisis VirusTotal.
- Validasi protokol HTTPS.
- Deteksi pola _Typo-squatting_ (domain yang mirip dengan situs populer).
- Deteksi URL Shortener.

---

## 🛠️ Arsitektur & Teknologi

SafeLink diimplementasikan menggunakan standar industri pengembangan aplikasi Android:

### Core Frameworks

- **Retrofit 2 & OkHttp**: Menangani request networking API secara asynchronous dengan interceptor kustom untuk autentikasi API Key.
- **Jetpack Navigation**: Manajemen alur antar fragment yang seamless menggunakan single-activity architecture.
- **Lottie Framework**: Mengintegrasikan animasi vektor berbasis JSON untuk memberikan feedback visual yang engaging saat proses pemindaian.
- **Glide**: Library optimasi pemuatan gambar dan favicon secara cepat untuk identifikasi visual website.

### Keamanan & Data

- **SQLCipher**: Implementasi database SQLite tingkat lanjut dengan enkripsi tingkat militer.
- **SharedPreferences**: Digunakan untuk menyimpan preferensi user (tema, settings) secara efisien.
- **ExecutorService**: Manajemen background thread yang efisien untuk memastikan UI tetap responsif (mencegah Application Not Responding - ANR).

---

## 📐 Detail Teknis Implementasi

| Komponen         | Deskripsi Implementasi                                                                                                       |
| :--------------- | :--------------------------------------------------------------------------------------------------------------------------- |
| **Activity**     | `SplashActivity` (Launcher), `MainActivity` (Nav Host), `ResultActivity` (Detail), `SafeBrowserActivity` (Internal WebView). |
| **Navigation**   | Implementasi `nav_graph.xml` dengan animasi transisi _slide-in_ dan _fade-on_ kustom antar fragment.                         |
| **Networking**   | Implementasi `ApiClient` singleton dengan konversi Base64 kustom untuk kompatibilitas endpoint VirusTotal v3.                |
| **Animation**    | Penggunaan `LottieAnimationView` untuk loading state dan `ObjectAnimator` untuk entrance UI elements.                        |
| **Safe Browser** | WebView internal cerdas yang memblokir konten berbahaya otomatis sebelum halaman sepenuhnya dimuat.                          |
| **Unit Testing** | Implementasi `JUnit` untuk memvalidasi logika deteksi keamanan heuristik secara otomatis. |

---

## 🚀 Instalasi & Persiapan

### Prasyarat

- Android Studio Ladybug (2024.2.1) atau versi lebih baru.
- Java Development Kit (JDK) 21.

### Konfigurasi API Key

Untuk alasan keamanan, API Key dikelola melalui `BuildConfig`. Silakan tambahkan kunci Anda sendiri untuk menjalankan aplikasi:

1. Dapatkan API Key di [VirusTotal Community](https://www.virustotal.com/).
2. Buka file `local.properties` di root folder project.
3. Tambahkan baris berikut:
   ```properties
   VIRUSTOTAL_API_KEY=masukkan_api_key_anda_disini
   ```

---

<p align="center">
  Made with ❤️ by <b>ShinZeleo</b>
</p>
