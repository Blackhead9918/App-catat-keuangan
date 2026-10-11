# Pencatat Keuangan - Rencana Implementasi

Aplikasi manajemen keuangan pribadi mandiri berbasis Android Jetpack Compose dengan kalender interaktif realita, pengingat gaji rutin 2 mingguan dengan konfirmasi cerdas, daftar checklist pengeluaran potong saldo otomatis, dan pencatatan aset piutang/hutang terpisah berbasis Room Database offline.

## User Review & Critical Decisions

> [!IMPORTANT]
> Berdasarkan jawaban klarifikasi yang telah dikonfirmasi:
> - **Penyimpanan Data**: Penyimpanan lokal di perangkat secara offline menggunakan **Room Database** (aman, cepat, dan privasi terjamin tanpa perlu login akun).
> - **Format Tampilan Saldo & Piutang**: Format saldo kas utama dengan rincian total piutang di sampingnya (misal: `Saldo: Rp 1.250.000 (Piutang: Rp 500.000)`).
> - **Gaya Visual & Tema**: **Modern Finansial Hijau Emerald** (Dominasi warna Dark Slate/Emerald Green khas finansial terpercaya, aksen Mint & Amber, tipografi tajam, dan kartu berlayer elegan).

---

## 1. Overview & Core Concept

- **Apa yang Dibangun**: Aplikasi Android pencatat keuangan komprehensif yang dirancang untuk memanajemen arus kas pribadi harian dan terjadwal secara praktis:
  1. **Sinkronisasi Kalender Real-Time**: Kalender terintegrasi yang menandai tanggal gajian, tenggat tagihan, dan jatuh tempo hutang/piutang sesuai penanggalan nyata.
  2. **Jadwal Gajian Rutin (Bi-weekly / Custom)**: Mendukung jadwal rutin (misal tiap 2 minggu sekali). Saat hari tiba atau aplikasi dibuka, muncul prompt tindakan: *"Sudah menerima gaji? [Ya] / [Tidak]"* dengan fleksibilitas edit nominal jika ada penyesuaian (lembur/potongan) disertai catatan penjelasan sebelum menambah saldo.
  3. **Checklist Pengeluaran Berencana**: Pengeluaran berkala seperti *"Bayar Wifi 150rb"* dalam bentuk checklist. Saat ditandai selesai (diceklis), saldo kas otomatis berkurang dan tercatat di riwayat.
  4. **Penyesuaian Saldo Manual dengan Alasan Wajib**: Saldo awal atau koreksi saldo sewaktu-waktu wajib menyertakan alasan/keterangan penyesuaian untuk transparansi audit keuangan pribadi.
  5. **Manajemen Aset Piutang & Hutang**: Uang yang dipinjam orang lain ("Si X berhutang 500rb") dicatat sebagai aset piutang (tidak langsung masuk saldo kas, melainkan tampil sebagai pendamping saldo kas). Ketika dibayar (lunas atau dicicil), saldo kas bertambah dan nilai piutang berkurang otomatis.
- **Target Pengguna**: Individu, pekerja dengan siklus gaji 2 mingguan/bulanan, dan pengelola keuangan mandiri yang membutuhkan kepastian pencatatan uang tunai dan piutang orang lain.
- **Nilai Utama**: Tidak ada transaksi siluman; setiap perubahan saldo memiliki alasan jelas, dan jadwal rutin tidak otomatis terpotong/bertambah tanpa konfirmasi sadar dari pengguna.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Alur Beranda & Ringkasan Saldo**:
   - Pengguna membuka aplikasi dan langsung melihat kartu saldo utama di bagian atas:
     - **Saldo Kas Aktif**: `Rp 1.250.000`
     - **Aset Piutang (Ada di Orang)**: `+Rp 500.000`
     - **Tanggungan Hutang**: `-Rp 150.000`
     - **Kekayaan Bersih**: `Rp 1.600.000`
   - Tombol cepat **"Sesuaikan Saldo"** membuka modal untuk mengatur saldo awal atau koreksi saldo, dengan validasi input wajib mengisi alasan perubahan.

2. **Alur Konfirmasi Gajian (Jadwal Rutin)**:
   - Jika ada jadwal gajian yang jatuh tempo hari ini atau terlewat:
     - Kartu banner prioritas muncul di bagian atas beranda: *"Waktunya Gajian! Jadwal: Gaji Pokok (Rp 3.500.000)"*.
     - Pengguna menekan tombol **[Sudah Terima]** atau **[Belum]**.
     - Jika menekan **[Sudah Terima]**, muncul dialog ringkas:
       - Opsi konfirmasi langsung dengan nominal yang direncanakan.
       - Opsi centang *"Nominal berbeda?"* untuk mengubah jumlah aktual (misal menjadi `Rp 3.750.000`) dan menuliskan alasan (misal: "Bonus lembur akhir bulan").
       - Saldo kas langsung bertambah, jadwal diperbarui ke siklus 2 minggu berikutnya, dan transaksi masuk ke riwayat.

3. **Alur Kalender Interaktif**:
   - Tab Kalender menampilkan grid bulan dengan tanggal hari ini ditandai secara visual.
   - Tanggal yang memiliki agenda/jadwal memiliki indikator dot berwarna (Hijau untuk Pemasukan/Gaji, Merah/Oranye untuk Tagihan Pengeluaran, Biru untuk Piutang).
   - Mengetuk tanggal akan menampilkan daftar agenda di tanggal tersebut beserta tombol tambah jadwal baru (Frekuensi: Sekali, Tiap 2 Minggu, Bulanan, Custom X Hari).

4. **Alur Checklist Pengeluaran**:
   - Tab/Bagian "Tagihan & Rencana Pengeluaran" menampilkan daftar item seperti `[ ] Bayar Wifi (Rp 150.000)` dan `[ ] Listrik PLN (Rp 200.000)`.
   - Mengetuk checkbox memunculkan konfirmasi cepat: *"Konfirmasi bayar Wifi Rp 150.000? Saldo akan berkurang otomatis."*
   - Setelah konfirmasi, item ditandai terbayar `[v]`, saldo kas terpotong, dan tercatat ke buku kas.

5. **Alur Piutang & Hutang (Pelunasan & Cicilan)**:
   - Menambahkan catatan piutang: Nama peminjam (misal "Budi"), nominal pinjaman (`Rp 500.000`), tanggal pinjam, dan catatan.
   - Pada kartu Budi, terdapat tombol **"Catat Pembayaran / Ceklis Pelunasan"**:
     - Pelunasan penuh: mencentang checkbox langsung melunasi `Rp 500.000`, menambah kas `Rp 500.000`, dan menghapus piutang.
     - Pembayaran sebagian (cicilan): pengguna memasukkan nominal yang dibayar (misal `Rp 200.000`). Kas bertambah `Rp 200.000`, sisa piutang Budi otomatis menjadi `Rp 300.000`.

### Visual Identity & Theme (Modern Finansial Hijau Emerald)

- **Aesthetic Direction**: Modern Finansial, bersih, terpercaya, dengan kontras tajam antara kartu gelap dan aksen hijau emerald bercahaya (*glowing mint accent*).
- **Color Palette**:
  - `Primary / Emerald`: `#00875A` / `#00A36C` (Kepercayaan finansial)
  - `Accent / Mint`: `#00E676` / `#69F0AE` (Penanda saldo positif & gajian)
  - `Expense / Coral Red`: `#FF5252` (Pengeluaran & hutang)
  - `Warning / Amber`: `#FFAB00` (Jatuh tempo & pengingat)
  - `Background / Surface`: `#0D1512` (Dark Slate bernuansa hijau hutan pekat) atau Light Emerald Cream `#F4F8F5`
  - `Card Surfaces`: `#16221D` dengan border halus `#273A32`
- **Typography & Layout**:
  - Display Font untuk angka nominal rupiah: Monospace/Semi-bold bersih dengan pemisah ribuan standar Indonesia (`Rp 1.250.000`).
  - Hierarki jelas: Saldo utama berukuran besar (32sp), sub-rincian (14sp), chip kategori rapi.

---

## 3. Key Product Decisions & Trade-Offs

- **Penyimpanan: Room Database (SQLite Lokal)**:
  - *Keputusan*: Menggunakan Room Database dengan 4 entitas terstruktur (`AccountBalance`, `FinancialSchedule`, `DebtReceivable`, `TransactionHistory`).
  - *Alasan*: Sepenuhnya offline, instan tanpa latensi jaringan, tidak memerlukan login pihak ketiga, dan data finansial tersimpan aman di perangkat lokal pengguna.
- **Siklus Jadwal Otomatis (Bi-weekly Calculator)**:
  - *Keputusan*: Jadwal rutin 2 mingguan dihitung berdasarkan tanggal awal (`startDateMillis + n * 14 hari`). Sistem memeriksa tanggal saat aplikasi dibuka dan menyajikan prompt konfirmasi.
  - *Alasan*: Pengguna tidak ingin saldo bertambah diam-diam jika kenyataannya gaji terlambat ditransfer. Konfirmasi manual menjamin saldo di aplikasi selalu akurat dengan rekening nyata.
- **Audit Log Penyesuaian Saldo**:
  - *Keputusan*: Setiap perubahan saldo manual wajib mengisi input alasan (`alasan penyesuaian`).
  - *Alasan*: Mencegah kebingungan di masa depan mengapa saldo bertambah atau berkurang tiba-tiba.

---

## 4. Technical Architecture & Data Strategy

```
┌──────────────────────────────────────────────────────────────┐
│                      Jetpack Compose UI                      │
├───────────────┬────────────────┬──────────────┬──────────────┤
│  HomeScreen   │  CalendarTab   │ ChecklistTab │ DebtAssetTab │
│(Saldo & Prompt│(Sync Real-Date │ (Tagihan &   │ (Piutang &   │
│  Gaji Rutin)  │ Jadwal Kalender│ Ceklis Bayar)│ Hutang Orang)│
└───────▲───────┴────────▲───────┴──────▲───────┴──────▲───────┘
        │                │              │              │
┌───────┴────────────────┴──────────────┴──────────────┴───────┐
│              FinancialViewModel (StateFlow & MVI)             │
│   - currentBalanceState, pendingSchedules, dueSalaryEvents   │
│   - debtsAndReceivablesState, calendarMonthState             │
└──────────────────────────────▲───────────────────────────────┘
                               │
┌──────────────────────────────┴───────────────────────────────┐
│                    FinancialRepository                       │
├──────────────────────────────────────────────────────────────┤
│                     Room AppDatabase                         │
│  ┌─────────────────────────┐     ┌────────────────────────┐  │
│  │   AccountBalanceEntity   │     │ FinancialScheduleEntity│  │
│  ├─────────────────────────┤     ├────────────────────────┤  │
│  │  DebtReceivableEntity   │     │ TransactionItemEntity  │  │
│  └─────────────────────────┘     └────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### Data Model Entities

1. **`AccountBalance`**:
   - `id`: Int (PrimaryKey, single row)
   - `cashBalance`: Long (dalam Rupiah)
   - `lastUpdated`: Long (Timestamp)
2. **`FinancialSchedule`**:
   - `id`: Long (PrimaryKey auto-generate)
   - `title`: String ("Gaji Pokok", "Bayar Wifi", dll.)
   - `nominal`: Long
   - `type`: ScheduleType (`INCOME`, `EXPENSE`)
   - `recurrence`: RecurrenceType (`ONCE`, `BI_WEEKLY`, `MONTHLY`, `CUSTOM_DAYS`)
   - `intervalDays`: Int (default 14 untuk bi-weekly)
   - `startDate`: Long (Epoch millis)
   - `nextDueDate`: Long (Epoch millis)
   - `notificationNote`: String ("Waktunya gajian!", dll.)
   - `isCompleted`: Boolean
3. **`DebtReceivable`**:
   - `id`: Long (PrimaryKey)
   - `personName`: String ("Budi", "Toko Sejahtera")
   - `type`: DebtType (`PIUTANG` = aset ada di orang lain, `HUTANG` = kewajiban kita)
   - `originalAmount`: Long
   - `remainingAmount`: Long
   - `dueDate`: Long?
   - `notes`: String
   - `isFullySettled`: Boolean
4. **`TransactionHistory`**:
   - `id`: Long (PrimaryKey)
   - `title`: String
   - `amount`: Long
   - `type`: TransactionType (`INCOME`, `EXPENSE`, `MANUAL_ADJUSTMENT`, `DEBT_SETTLEMENT`)
   - `reasonOrNote`: String (Alasan penyesuaian saldo, koreksi gaji, dsb.)
   - `timestamp`: Long

---

## 5. Rencana Eksekusi Bertahap (Setelah Persetujuan)

1. **Sinkronisasi Metadata & Resource**:
   - Perbarui nama aplikasi di `metadata.json` dan `res/values/strings.xml` menjadi `"Pencatat Keuangan"`.
2. **Data Layer (Room Database)**:
   - Buat Entitas, DAO (`FinancialDao`), dan Room Database (`AppDatabase`).
   - Buat `FinancialRepository` dengan operasi reaktif (`Flow`).
3. **Domain & ViewModel**:
   - Implementasikan `FinancialViewModel` dengan logika:
     - Evaluasi jadwal jatuh tempo saat app dibuka (prompt gajian).
     - Eksekusi konfirmasi penerimaan gaji (dengan toleransi penyesuaian nominal + catatan).
     - Eksekusi checklist pengeluaran (otomatis potong saldo kas).
     - Penyesuaian saldo manual dengan catatan wajib.
     - Pembayaran cicilan/lunas piutang & hutang.
4. **UI Components (Jetpack Compose & M3)**:
   - Skema warna Emerald & Dark Slate di `Theme.kt`.
   - **Komponen Kalender Kustom**: Tampilan kalender bulanan interaktif dengan penanda tanggal dan indikator jadwal.
   - **Dialog Prompt Gajian**: Banner dan dialog "Sudah menerima gaji? Ya/Tidak" dengan opsi ubah nominal dan alasan.
   - **Daftar Checklist Tagihan**: Checkbox interaktif dengan animasi pemotongan saldo.
   - **Layar Piutang & Hutang**: Kartu aset piutang dengan pelacak sisa pembayaran dan konfirmasi pelunasan.
   - **Dialog Penyesuaian Saldo**: Validasi form penyesuaian saldo kas.
5. **Verifikasi & Build**:
   - Jalankan `compile_applet` untuk memastikan kompilasi sukses 100% tanpa error.
