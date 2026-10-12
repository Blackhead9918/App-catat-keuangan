# DESIGN_SPEC_REDESIGN.md

> **Status dokumen:** SPESIFIKASI. **Belum ada source code yang diubah.**
> Dokumen ini adalah lanjutan dari `DESIGN_CONTEXT.md` (analisis kondisi saat ini) dan berisi rancangan perubahan.
> Pratinjau visual ada di folder `design-preview/` — buka `design-preview/index.html`.

---

## 0. KEPUTUSAN YANG SUDAH DIKUNCI

| Topik | Keputusan | Konsekuensi |
|---|---|---|
| Arah desain | **Belum dipilih.** Dua kandidat: Arah A "Buku Kas" dan Arah C "Playful Ledger" | Tidak ada redesign yang dieksekusi sebelum pemilihan |
| Cakupan saat ini | **Belum eksekusi.** Tulis spesifikasi dulu | Dokumen ini + pratinjau saja |
| Prioritas jangka pendek | **Perbaikan paling mendesak (P0)** — lihat §10 | P0 boleh dieksekusi tanpa menunggu pilihan arah, karena P0 tidak mengandung keputusan estetika |
| Tema gelap | **Light-only** | `DarkBankingColorScheme` di `Theme.kt` tidak dipakai. Tidak ada toggle tema, tidak ada `isSystemInDarkTheme()`, tidak ada QA dark mode. `darkTheme` parameter dibiarkan ada demi kompatibilitas tapi hardcode `false` |
| Font | **Font sistem** (`FontFamily.Default`) | Tidak ada file di `res/font/`, tidak ada dependency font baru. Karakter visual dibentuk oleh ukuran, weight, dan spasi — bukan oleh jenis huruf |
| Bahasa | Indonesia | Semua copywriting baru dalam bahasa Indonesia |
| Platform | Android native, Jetpack Compose + Material 3 | Tidak ada perubahan teknologi |
| Backend | Room lokal, offline | Tidak ada perubahan `Entities` / `DAO` / `Database` |

**Yang secara eksplisit TIDAK termasuk dalam lingkup ini:** mode gelap, custom font, fitur AI, backend/sinkronisasi, multi-user, ekspor data, notifikasi terjadwal, lokalisasi (`stringResource`), dan perubahan pada navigasi bottom-bar.

---

## 1. MASALAH YANG HARUS DISELESAIKAN

Diambil dari `DESIGN_CONTEXT.md` §15. Diurutkan berdasarkan dampak nyata, bukan berdasarkan seberapa mudah diperbaiki.

| # | Masalah | Kategori | Tingkat |
|---|---|---|---|
| 1 | Hapus data langsung eksekusi tanpa konfirmasi & tanpa undo (9 tombol) | Bahaya data | **Kritis** |
| 2 | Saldo tampil `Rp 0` sebelum data Room siap | Informasi salah | **Kritis** |
| 3 | Bayar tagihan dari Kalender tanpa konfirmasi (Beranda/Ceklis pakai dialog) | Bahaya data | **Kritis** |
| 4 | Warna teks uang gagal kontras WCAG AA (hijau 2.6:1, merah 3.8:1) | Aksesibilitas | Tinggi |
| 5 | Merah dipakai untuk "pengeluaran" **dan** "error" **dan** "aksi destruktif" | Semantik warna | Tinggi |
| 6 | Tidak ada hierarki tipografi; hanya `bodyLarge` di-override, ~15 ukuran inline | Hierarki | Tinggi |
| 7 | Angka nominal tidak punya gaya sendiri (tidak tabular, tidak rata kolom) | Keterbacaan | Tinggi |
| 8 | 27 teks berukuran 10–11sp (di bawah lantai baca 12sp) | Aksesibilitas | Sedang |
| 9 | 6 nilai radius + 12 langkah spacing tanpa aturan | Konsistensi | Sedang |
| 10 | 5 header, 4 empty state, 8 `Surface(onClick)`, 2 kartu transaksi — semua duplikat | Pemeliharaan | Sedang |
| 11 | `Checkbox` tidak pernah tercentang (bukan kontrol, tapi tombol) | Kejujuran UI | Sedang |
| 12 | Beranda menumpuk 5 section dengan 3 jenis kartu berbeda | Hierarki | Sedang |

---

## 2. ARAH A — "BUKU KAS"

**Tesis:** aplikasi ini bukan rekening bank. Ini **buku kas + pengingat tagihan + catatan utang**. Bahasa visualnya harus datang dari situ: tenang, presisi, jujur. Presisi menggantikan dekorasi.

**Perubahan paling menentukan:** kartu gradient bergaya kartu ATM **dihapus**, diganti **Balance Sheet Block** — empat baris nilai yang memang isi sebenarnya dari kartu itu.

**Kesan yang dituju:** *"buku besar digital yang rapi."* Serius tanpa menjadi dingin. Semua keputusan visual mengarah ke pemindaian angka yang cepat dan akurat.

### 2.1 Warna

Sumber: satu file token. Tidak ada `Color.White` di Composable, tidak ada hex inline.

| Peran | Token | HEX | Pemakaian |
|---|---|---|---|
| Canvas | `canvas` | `#F6F7F9` | Latar aplikasi |
| Surface | `surface` | `#FFFFFF` | Kartu, sheet, dialog |
| Surface Sunken | `surfaceSunken` | `#F1F3F6` | Tile ikon, kolom input, chip tidak aktif |
| Garis (dekoratif) | `line` | `#E6E9EF` | Pemisah 1px. **Dekoratif** — bukan pembawa makna |
| Garis (fungsional) | `lineStrong` | `#D0D5DD` | Batas yang harus terlihat (outline input, pemisah kolom) |
| Teks utama | `ink` | `#101828` | Judul, semua angka utama |
| Teks sekunder | `ink2` | `#5B6472` | Keterangan, meta, label baris |
| Teks tersier | `ink3` | `#667085` | Timestamp, helper, label section |
| Teks nonaktif | `inkDisabled` | `#98A2B3` | Hanya non-teks (ikon mati) — **2.6:1, jangan untuk teks** |
| Brand | `brand` | `#0056D2` | CTA utama, tab aktif, link. **Dipakai hemat** |
| Brand lembut | `brandSoft` | `#EFF6FF` | Latar badge/ikon brand |
| Masuk | `inText` / `inBg` | `#047857` / `#D1FAE5` | Nominal pemasukan, status lunas |
| Keluar | `outText` / `outBg` | `#B42318` / `#FEE4E2` | Nominal pengeluaran, tagihan |
| Perlu aksi | `dueText` / `dueBg` | `#92400E` / `#FEF0C7` | Jatuh tempo dekat / terlambat |
| Piutang | `receivable` | `#0369A1` | Nominal & ikon piutang |
| Fokus | `focusRing` | `#0056D2` | Ring fokus keyboard/switch |

**Aturan pakai warna (mengikat):**

1. `inText` / `outText` / `dueText` **hanya** untuk teks. `inBg` / `outBg` / `dueBg` **hanya** untuk latar.
2. Merah (`outText`) = uang keluar. Untuk aksi destruktif, gunakan `outText` sebagai **isi** tombol dengan latar `outText` (teks putih) — bukan warna teks merah di latar putih. Dengan begitu "menghapus" dan "pengeluaran Rp 50.000" tidak lagi terlihat sama.
3. Brand biru **tidak** dipakai untuk header, tidak untuk semua ikon, tidak untuk chip aktif penuh. Header berwarna canvas.

**Rasio kontras terverifikasi (WCAG 2.1, dihitung dengan formula luminance relatif):**

| Pasangan | Rasio | Hasil |
|---|---|---|
| `ink` di atas `surface` | 17.75:1 | AA / AAA |
| `ink` di atas `canvas` | 16.56:1 | AA / AAA |
| `ink2` di atas `surface` | 5.98:1 | AA |
| `ink2` di atas `canvas` | 5.58:1 | AA |
| `ink3` di atas `surface` | 4.97:1 | AA |
| `ink3` di atas `canvas` | 4.64:1 | AA |
| `brand` di atas `surface` | 6.44:1 | AA |
| putih di atas `brand` | 6.44:1 | AA |
| `inText` di atas `surface` | 5.48:1 | AA |
| `inText` di atas `inBg` | 4.84:1 | AA |
| `outText` di atas `surface` | 6.57:1 | AA |
| `outText` di atas `outBg` | 5.45:1 | AA |
| putih di atas `outText` (tombol hapus) | 6.57:1 | AA |
| `dueText` di atas `dueBg` | 6.25:1 | AA |
| `receivable` di atas `surface` | 5.93:1 | AA |
| `line` di atas `surface` | 1.22:1 | Dekoratif — bukan pembawa makna, tidak tunduk pada 3:1 |

Catatan jujur: `lineStrong` (1.47:1) **juga** di bawah 3:1. Karena itu `lineStrong` hanya dipakai sebagai pemisah visual bersama isyarat lain (label, jarak). Untuk komponen yang batasnya wajib terlihat (outline `OutlinedTextField`, indikator fokus), gunakan `ink3` (4.97:1) atau `brand`.

### 2.2 Tipografi

Font: `FontFamily.Default`. Enam langkah, tidak ada lantai di bawah 12sp.

| Peran | Ukuran | Weight | Line height | Letter spacing | Pemakaian |
|---|---|---|---|---|---|
| `hero` | 32sp | Bold (700) | 38sp | −0.6sp | Angka kekayaan bersih |
| `amountL` | 24sp | SemiBold (600) | 30sp | −0.3sp | Total di kartu ringkasan |
| `amountM` | 16sp | SemiBold (600) | 22sp | 0 | Nominal pada baris & kartu |
| `title` | 17sp | SemiBold (600) | 24sp | −0.1sp | Judul kartu, judul dialog |
| `body` | 15sp | Regular (400) | 22sp | 0 | Teks isi, catatan |
| `label` | 13sp | Medium (500) | 18sp | 0.1sp | Label baris nilai, tombol |
| `caption` | 12sp | Medium (500) | 16sp | 0.2sp | Meta, timestamp, label section |

**Aturan angka (mengikat):**

- Semua nominal memakai `FontFeatureSetting("tnum")` — digit lebar tetap, sehingga angka bertumpuk rapi secara vertikal di kolom.
- Semua nominal rata **kanan** (`textAlign = TextAlign.End`) saat berada di kolom.
- Tanda +/− selalu eksplisit pada mutasi; tanpa tanda pada saldo.
- Format: `Rp 1.250.000` (Locale `id-ID`). Untuk ruang sempit boleh singkat: `Rp 1,25 jt` — **hanya** pada kartu kecil, tidak pernah pada saldo.
- Tidak ada nominal yang dirender dengan `labelSmall` atau di bawah 12sp.

### 2.3 Bentuk & Spacing

**Hanya tiga radius:**

| Radius | Token | Pemakaian |
|---|---|---|
| 20dp | `radiusSheet` | Bottom sheet, blok hero (Balance Sheet) |
| 14dp | `radiusCard` | Kartu, input, tombol utama |
| 10dp | `radiusChip` | Chip, badge, tombol kecil |

**Latar belakang bulat penuh** hanya untuk ikon-lingkaran 40dp dan FAB.

**Skala spacing (aturan 4dp):** `4 / 8 / 12 / 16 / 20 / 24 / 32`

- Padding horizontal layar: **20dp** (dipertahankan dari kondisi sekarang).
- Padding dalam kartu: **16dp seragam** — satu nilai untuk semua kartu.
- Jarak antar kartu dalam satu daftar: **8dp**.
- Jarak antar section: **24dp**.
- Padding atas judul section dari konten sebelumnya: **24dp**; jarak judul ke item pertama: **12dp**.

**Elevasi: nol.** Kartu didefinisikan oleh `surface` putih + garis `line` 1px. Hanya bottom sheet, FAB, dan snackbar yang boleh memiliki bayangan. Ini satu keputusan yang paling besar dampaknya terhadap kesan "bukan template".

### 2.4 Motion

Minimal dan fungsional. `MotionDurationShort = 150ms`, `MotionDurationMedium = 250ms`, easing standar Material 3.

| Kejadian | Gerakan |
|---|---|
| Pindah tab | Cross-fade konten 150ms, bottom nav tidak beranimasi |
| Buka bottom sheet | Slide up 250ms |
| Hapus item | Item memudar + mengecil 200ms, lalu Snackbar undo muncul |
| Kosong → berisi | Fade in 200ms |
| Skeleton → data | Cross-fade 200ms |
| Tidak ada | Animasi dekoratif, parallax, auto-play, confetti |

---

## 3. ARAH C — "PLAYFUL LEDGER"

**Tesis:** aplikasi keuangan pribadi tidak harus terlihat seperti milik bank. Karakter kuat membuat user ingin membukanya. Bahasa visual: neo-brutalis ringan — **border hitam 2px, hard offset shadow, warna blok solid, tipografi tebal tegas**.

**Kesan yang dituju:** *"buku catatan fisik dengan stiker."* Hangat, ekspresif, tidak menggurui.

### 3.1 Warna

| Peran | Token | HEX | Pemakaian |
|---|---|---|---|
| Canvas | `canvas` | `#FDF9F0` | Latar kertas hangat |
| Surface | `surface` | `#FFFFFF` | Kartu |
| Border | `border` | `#111827` | **Border 2dp di semua kartu, chip, tombol** |
| Bayangan keras | `shadow` | `#111827` | `4px 4px 0` offset, tanpa blur |
| Teks utama | `ink` | `#111827` | |
| Teks sekunder | `ink2` | `#4B5563` | 7.56:1 |
| Teks sekunder di canvas | `ink2Canvas` | `#6B7280` | 4.60:1 |
| Brand | `brand` | `#2563EB` | Tombol utama, tab aktif |
| Aksen sorot | `highlight` | `#FDE047` | Sorotan kuning: badge, tab aktif, penanda |
| Masuk | `inText` / `inBg` | `#15803D` / `#DCFCE7` | 5.02:1 |
| Keluar | `outText` / `outBg` | `#B42318` / `#FEE2E2` | 5.38:1 |
| Perlu aksi | `dueText` / `dueBg` | `#92400E` / `#FEF08A` | 6.09:1 |

**Rasio kontras terverifikasi:**

| Pasangan | Rasio | Hasil |
|---|---|---|
| `ink` di atas `canvas` | 16.88:1 | AA / AAA |
| `ink` di atas `surface` | 17.74:1 | AA / AAA |
| `ink2` di atas `surface` | 7.56:1 | AA |
| `ink2Canvas` di atas `canvas` | 4.60:1 | AA |
| `brand` di atas `surface` | 5.17:1 | AA |
| putih di atas `brand` | 5.17:1 | AA |
| `inText` di atas `surface` | 5.02:1 | AA |
| `inText` di atas `inBg` | 4.57:1 | AA |
| `outText` di atas `surface` | 6.57:1 | AA |
| `outText` di atas `outBg` | 5.38:1 | AA |
| putih di atas `outText` | 6.57:1 | AA |
| `dueText` di atas `dueBg` | 6.09:1 | AA |
| `ink` di atas `highlight` | 13.46:1 | AA / AAA |

Catatan jujur: `#B45309` di atas `#FDE047` hanya 3.81:1 — **gagal AA untuk teks normal**. Karena itu pasangan itu hanya dipakai untuk **ikon/penanda**, dan bila perlu teks di atas `highlight`, gunakan `ink`.

Keduanya tetap memakai aturan teks yang sama dengan §10 P0-4 dan P0-5 (kontras & semantik merah), hanya nilai tokennya berbeda.

### 3.2 Tipografi

Font tetap sistem, tetapi bebannya berbeda: **weight lebih tebal, ukuran satu langkah lebih besar, tracking lebih rapat**.

| Peran | Ukuran | Weight | Line height | Letter spacing |
|---|---|---|---|---|
| `hero` | 34sp | ExtraBold (800) | 40sp | −0.8sp |
| `amountL` | 26sp | Bold (700) | 32sp | −0.4sp |
| `amountM` | 18sp | Bold (700) | 24sp | −0.2sp |
| `title` | 17sp | Bold (700) | 23sp | 0 |
| `body` | 15sp | Regular (400) | 22sp | 0 |
| `label` | 13sp | SemiBold (600) | 18sp | 0.1sp |
| `caption` | 12sp | SemiBold (600) | 16sp | 0.3sp |

Aturan angka identik dengan Arah A (tabular, rata kanan, tanda eksplisit).

### 3.3 Bentuk, Bayangan, Spacing

| Radius | Token | Pemakaian |
|---|---|---|
| 18dp | `radiusCard` | Kartu, bottom sheet |
| 12dp | `radiusChip` | Chip, badge, input |
| 999dp | `radiusPill` | Tombol utama, FAB |

- **Border:** 2dp `#111827`, selalu. Tidak ada kartu tanpa border.
- **Hard shadow:** `offset (4, 4)`, blur 0, warna `#111827`. Padding kanan-bawah kartu harus diberi ruang 4dp ekstra agar bayangan tidak terpotong tetangga.
- Skala spacing sama: `4 / 8 / 12 / 16 / 20 / 24 / 32`. Padding kartu 16dp. Padding horizontal layar 20dp.
- **Aturan penting:** bayangan keras hanya untuk elemen yang **bisa ditekan** (kartu actionable, tombol, chip). Elemen statis (label section, pemisah) tidak memakai border dan bayangan. Ini yang membedakan neo-brutalis yang terbaca dari yang berisik.

### 3.4 Motion

Lebih ekspresif tetapi tetap pendek: 120–250ms, easing `overshoot` ringan (spring `medium bouncy`) hanya untuk: tombol ditekan (mengecil ke 96%), item baru masuk, bottom sheet. Semua animasi lain standar Material 3. **Tidak** ada animasi looping atau dekoratif.

---

## 4. KOMPONEN BERSAMA (WAJIB — BERLAKU UNTUK KEDUA ARAH)

Tujuan bagian ini: **mengurangi 5 header jadi 1, 4 empty state jadi 1, 2 kartu transaksi jadi 1, 2 dialog tagihan jadi 1.** Semua komponen di bawah dibuat sekali di `ui/components/` (atau `ui/design/`).

| Komponen | Menggantikan | Perilaku |
|---|---|---|
| `MoneyText` | semua penulisan nominal inline | Parameter: `amount`, `signed: Boolean`, `size: MoneySize`, `tone: MoneyTone`. Mengurus format, tanda, warna peran, tabular, rata kanan |
| `BalanceSheetBlock` | `BankingBalanceCard` | Blok hero: label "Kekayaan Bersih", angka `hero`, 3 baris nilai (Kas / Piutang / Hutang). Aksi "Atur Saldo" sebagai tombol teks di kanan label |
| `ObligationCard` | `HomeExpenseChecklistItem`, `ScheduleCardItem`, `RecurringOverviewCard`, `HomePiutangItem`, item hutang | **Satu kartu untuk semua kewajiban bertanggal.** Punya `state`: `Overdue` / `DueToday` / `DueSoon` / `Scheduled` / `Done`. Aksi utama di kanan, aksi sekunder di menu |
| `LedgerRow` | `BankingTransactionRowItem`, `HistoryItemCard` | Baris mutasi: judul, catatan 1 baris (`maxLines = 1`, ellipsis), timestamp, nominal bertanda. Tanpa kartu di dalam daftar — pemisah 1px |
| `SectionHeader` | 5 penulisan judul section | Judul `caption` (uppercase) + hitungan opsional + aksi teks kanan |
| `EmptyState` | 4 penulisan empty state | Ikon 40dp + judul + 1 baris penjelasan + aksi opsional. **Selalu ada**, tidak pernah menyembunyikan section |
| `AppHeader` | `BankingHomeHeader` + 4 header lain | Judul + subjudul opsional + slot aksi kanan (ikon 40dp, target 48dp) |
| `AmountField` | `OutlinedTextField` nominal | Format ribuan saat mengetik, suffix "Rp" sebagai prefix, keyboard numerik |
| `ConfirmSheet` | AlertDialog konfirmasi | Bottom sheet: judul, ringkasan dampak (nominal + saldo sesudah), tombol batal/utama |
| `UndoSnackbar` | — (baru) | Setelah hapus: "Dihapus · URUNGKAN" 5 detik |
| `SkeletonBlock` | — (baru) | Placeholder loading: blok `surfaceSunken` radius sesuai |
| `InsetButton` / `PrimaryButton` | 8 penulisan `Surface(onClick)` | Tinggi minimum 44dp; tombol kecil dalam kartu tetap 40dp tappable area 48dp |

### 4.1 `ObligationCard` — spesifikasi detail

Ini komponen paling berulang di aplikasi. Ia menyerap empat bentuk kartu berbeda yang sekarang ada.

**Struktur (kiri ke kanan):**
`[state bar 4dp] [tile ikon 40dp] [judul + meta] [spacer] [nominal + aksi]`

| State | State bar | Tile ikon | Label status | Aksi utama |
|---|---|---|---|---|
| `Overdue` | `outText` | `outBg` + ikon seru | "Terlambat 3 hari" (`outText`) | Tombol isi `outText`, teks putih |
| `DueToday` | `dueText` | `dueBg` + ikon jam | "Jatuh tempo hari ini" (`dueText`) | Tombol isi `dueText`, teks putih |
| `DueSoon` | `dueText` | `dueBg` + ikon kalender | "Jatuh tempo 3 hari lagi" | Tombol outline |
| `Scheduled` | `line` | `surfaceSunken` + ikon kalender | "12 Nov 2026" (`ink3`) | Tombol outline |
| `Done` | `inText` | `inBg` + ikon centang | "Lunas 11 Okt" (`inText`) | Tidak ada; hanya menu |

**Meta baris** (maksimum 1 baris, ellipsis): `frekuensi • catatan pengingat`. Tidak ada catatan pengingat → hanya frekuensi. Di Beranda, catatan pengingat dipindah ke baris kedua di bawah judul (maks 1 baris); di daftar panjang (Ceklis) meta dipendekkan.

**Aturan anti-padat:** satu kartu maksimal **satu** nominal dan **satu** label status. Hitungan periode pembayaran (1/2/3/6) pindah ke `ConfirmSheet`, bukan di kartu.

**Target ketuk:** seluruh kartu = 1 aksi utama (membayar/konfirmasi). Menu `⋮` terpisah 48dp untuk Edit/Hapus. Saat ini kartu dengan checkbox kecil di dalamnya adalah target ketuk yang ambigu — ini yang diperbaiki.

---

## 5. RANCANGAN PER SCREEN

### 5.1 Beranda — **diramping drastis**

Menjawab satu pertanyaan: *"Apa yang perlu saya lakukan hari ini, dan bagaimana posisi saya?"*

```
AppHeader ("Buku Kas" / "Oktober 2026 · 4 tagihan aktif")   [ikon kalender]
BalanceSheetBlock  — Kekayaan Bersih + 3 baris nilai         [Atur Saldo]
──── (24dp)
SectionHeader "PERLU TINDAKAN" (2)
  ObligationCard [Overdue]
  ObligationCard [DueSoon]
SectionHeader "AKAN DATANG"
  ObligationCard [Scheduled]
  ObligationCard [Scheduled]
────
PrimaryButton "Catat tagihan baru"  (±48dp)
Bottom nav
```

**Yang dipindahkan keluar dari Beranda:**

| Sekarang | Pindah ke |
|---|---|
| Quick Actions (3 tombol besar) | Dihapus. "Jadwal Gaji" & "Catat Piutang" masuk menu `+`; "Ceklis Tagihan" tidak perlu karena sudah ada tab |
| Section "Aset Piutang Aktif" (maks 2) | Tab Aset/Hutang. Di Beranda cukup angka piutang di Balance Sheet |
| Section "Mutasi Rekening & Kas Terkini" (maks 5) | Tab Riwayat. Di Beranda cukup 3 baris `LedgerRow` tanpa judul section, atau dihapus total |
| Banner Gajian (kartu penuh) | Menjadi `ObligationCard [DueToday]` di "Perlu Tindakan" — gajian adalah jadwal, sama seperti tagihan |

**Prinsip:** Beranda hanya menampilkan hal yang **butuh keputusan**. Sisanya adalah referensi, dan referensi punya tabnya sendiri.

### 5.2 Kalender

Struktur sekarang sudah benar dan dipertahankan. Yang diperbaiki:

- Header jadi `AppHeader` (bukan bar biru) + tombol "Hari Ini" sebagai aksi header.
- Kartu navigasi bulan digabung ke dalam area kalender (satu kartu `surface` radius 20 berisi: nama bulan, prev/next, grid, legend).
- **Sel kalender:** target ketuk penuh (bukan 12dp radius kecil di dalam sel yang lebih besar), radius 10dp, hari ini ditandai **outline**, hari terpilih ditandai **isi `brand`**. Indicator dot maksimum 2, ukuran 5dp; lebih dari 2 jadwal → tampilkan dot `ink3` + angka kecil.
- Daftar agenda memakai `ObligationCard`, sehingga kartu tagihan di Kalender identik dengan di Ceklis dan Beranda.
- **Bayar tagihan dari Kalender wajib memakai `ConfirmSheet` yang sama** (masalah #3).

### 5.3 Ceklis Tagihan

- Tiga tab (Perlu Bayar / Bulan Depan / Selesai) **dipertahankan** — datanya memang tiga kategori.
- Kartu ringkasan "Tagihan Perlu Dibayar Sekarang" → `BalanceSheetBlock` versi ringkas: total + jumlah item + jatuh tempo terdekat.
- Tab "Selesai" memakai `ObligationCard [Done]`. **Aksi hapus di tab ini harus dibedakan** dari aksi hapus di tab aktif: di Selesai, aksi utamanya "Kembalikan" (belum benar-benar selesai), dan hapus permanen masuk menu `⋮` dengan `ConfirmSheet` merah.
- `Checkbox` **dihapus**. Kartu itu aksi, bukan form.
- **Empty state wajib tampil** di ketiga tab, termasuk "Perlu Bayar" saat kosong (sekarang section hanya disembunyikan).

### 5.4 Aset Piutang & Hutang

- Kartu ringkasan 4 angka → `BalanceSheetBlock` dengan 3 baris (Piutang / Hutang / Kas) + 1 angka hero (Kekayaan Bersih). Urutan: **hero di atas** agar konsisten dengan Beranda.
- Tiga tab (Piutang / Hutang / Lunas) dipertahankan.
- Setiap item memakai `ObligationCard` dengan nominal sisa yang dominan; **progress bar cicilan** (dari `originalAmount` vs `remainingAmount`) ditambahkan sebagai elemen visual 4dp di bawah judul — data sudah ada, tidak butuh perubahan schema.
- `dueDate` yang saat ini selalu `null`: **(a)** sembunyikan seluruh jejaknya sampai ada input, atau **(b)** tampilkan field tanggal opsional di `AddDebtDialog`. **Keputusan produk diperlukan** — lihat §16.
- Aksi hapus → menu `⋮` + `ConfirmSheet` merah.

### 5.5 Riwayat — **dinaikkan kelasnya**

Ini tab yang paling banyak untung dari redesign, dan paling mudah: datanya sudah lengkap.

```
AppHeader ("Riwayat" / "Oktober 2026")                     [Atur Saldo]
Ringkasan: Saldo saat ini Rp 3.250.000
FilterChip: Semua | Pemasukan | Pengeluaran | Penyesuaian
────
"SENIN, 11 OKT"                         Masuk +Rp 850.000
  LedgerRow  Gaji Diterima: Gaji Bulanan   +Rp 8.500.000
  LedgerRow  Bayar Tagihan: Internet        −Rp 350.000
  ─────────────────────────────────────────────────────
  Saldo akhir hari                          Rp 3.250.000
```

**Yang ditambahkan:**

1. **Grup per tanggal** dengan judul tanggal dan total masuk/keluar hari itu (data sudah ada; pengelompokan di UI).
2. **Saldo berjalan** per transaksi. Dihitung di sisi UI dengan menjalankan mundur dari `cashBalance` saat ini. **Tidak ada perubahan schema atau DAO.**
3. **Pemisah baris**, bukan kartu per transaksi — daftar jadi lebih padat dan lebih mudah dipindai.
4. Jumlah hasil yang difilter ditampilkan di `SectionHeader` ("47 transaksi").
5. `EmptyState` berbeda untuk "belum ada transaksi" vs "tidak ada hasil untuk filter ini".

**Catatan implementasi:** saldo berjalan mengasumsikan 15 transaksi terbaru dari DAO saat ini (`getRecentTransactionsFlow(15)`) tidak cukup. Untuk Riwayat dipakai `allTransactionsFlow` yang sudah tersedia di `FinancialRepository` — jadi tidak ada perubahan repository pun.

---

## 6. STATE

Ini bagian yang paling banyak hilang di implementasi sekarang. Empat state wajib ada di setiap permukaan yang menampilkan data.

| State | Tampilan | Aturan |
|---|---|---|
| **Loading** | `SkeletonBlock` berbentuk sama dengan konten akhir | Saldo **tidak pernah** dirender sebagai `Rp 0` saat `isInitialized == false`; render `—` di `hero` + skeleton untuk baris |
| **Empty** | `EmptyState` | Selalu tampil, section tidak pernah disembunyikan. Teks menjelaskan aksi berikutnya |
| **Error** | Tidak ada jalur error di aplikasi ini (Room lokal, tanpa network) | Cukup jaga: operasi tulis yang gagal tidak boleh meninggalkan UI dalam keadaan seolah berhasil. Karena `ViewModel` memakai `viewModelScope.launch` tanpa penanganan, minimal bungkus dengan `try/catch` dan tampilkan `Snackbar` |
| **Sukses** | `Snackbar` non-blocking | Satu pola untuk semua: `Snackbar` biasa untuk konfirmasi ("Tagihan ditandai lunas"), `UndoSnackbar` untuk penghapusan |

**Penghapusan:** `AlertDialog` konfirmasi **atau** hapus langsung + `UndoSnackbar`. Yang **tidak boleh** lagi: hapus langsung tanpa keduanya. Rekomendasi: undo untuk item yang bisa dibuat ulang dengan cepat (tagihan terjadwal), konfirmasi untuk yang kehilangan riwayat (hutang/piutang dengan cicilan).

---

## 7. AKSESIBILITAS

| Aspek | Aturan |
|---|---|
| Kontras | Semua pasangan teks ≥ 4.5:1 (tabel di §2.1 / §3.1). Tidak ada teks di bawah 4.5:1 |
| Ukuran teks | Lantai 12sp. 27 kemunculan `fontSize = 10.sp`/`11.sp` dinaikkan |
| Target ketuk | Minimum 48×48dp. Tombol kecil dalam kartu boleh 40dp visual, area ketuk 48dp. Chip "Reset" di header Beranda dipindah ke menu |
| Pembesaran font | Layout harus tetap utuh pada `fontScale = 1.3`. Larangan: `maxLines = 1` pada teks yang bisa panjang (judul tagihan, nama orang), `fixed height` pada kartu berisi teks |
| Label | Setiap ikon yang bukan dekorasi punya `contentDescription`. Setiap ikon di bottom nav punya label teks 12sp (sudah ada) |
| Semantik | Kartu yang bisa ditekan memakai `Modifier.clickable(onClickLabel = "Bayar tagihan Internet")`, bukan `Surface(onClick)` tanpa label |
| Status | Status tidak boleh hanya disampaikan lewat warna. `Overdue` harus punya **teks** "Terlambat 3 hari", bukan hanya state bar merah |
| Reduced motion | Animasi dekoratif tidak ada, jadi tidak ada yang perlu dimatikan. Transisi tetap 150–250ms |
| Screen reader | Urutan baca mengikuti urutan visual. `MoneyText` mengucapkan "delapan juta lima ratus ribu rupiah", bukan "Rp 8.500.000" |
| `testTag` | Semua `testTag` yang ada dipertahankan |

---

## 8. ATURAN KONTEN

| Elemen | Aturan |
|---|---|
| Judul tab | Satu kata, tanpa huruf kapital semua: "Beranda", "Kalender", "Ceklis", "Aset", "Riwayat" |
| Label section | Huruf kapital semua, `caption`, tracking 0.2sp: "PERLU TINDAKAN", "AKAN DATANG" |
| Subjudul header | Maksimum 1 baris, informasi faktual saja. Pola yang disarankan: `<periode> · <jumlah>`. Contoh: "Oktober 2026 · 4 tagihan aktif" |
| Tombol | Kata kerja + objek bila ruang cukup: "Bayar tagihan", "Terima gaji", "Catat hutang". Bukan "OK", "Simpan", "Submit" |
| Nominal | Selalu `Rp` + spasi + angka. Tanpa desimal |
| Tanggal | `<d MMM yyyy>` pada header/agenda; `<d MMM>` pada kartu; jam `<HH:mm>` pada mutasi |
| Nama orang | Selalu ditampilkan penuh; tidak boleh terpotong di tengah kata pada `fontScale` normal |
| Empty state | Tidak menyalahkan user. Contoh: "Belum ada tagihan. Tambahkan tagihan rutin supaya tidak terlewat." |

Teks tetap berupa string literal di dalam Composable (keputusan: lokalisasi di luar lingkup), tetapi **dikumpulkan dalam satu `object Strings`** di `ui/` agar tidak tersebar dan agar penggantian ke `stringResource` nanti menjadi pekerjaan mekanis.

---

## 9. BATASAN IMPLEMENTASI (WAJIB DIPATUHI)

Diambil dari `DESIGN_CONTEXT.md` §16 dan diverifikasi ulang terhadap kode.

**Tidak boleh diubah:**

- **Signature callback** di semua Composable: `onConfirmSalary`, `onPayExpense`, `onAddSchedule`, `onAdjustBalance`, `onDeleteSchedule`, `onDeleteExpense`, `onDeleteDebt`, `onSettleDebt`, `onAddDebt`, `onDismissSalary`. Tipe & urutan parameter tetap.
- **Nilai chip periode pembayaran** (1/2/3 di Beranda, 1/2/3/6 di Ceklis) — nilainya langsung dikalikan ke potongan saldo di `FinancialRepository.markExpenseAsPaid`. Boleh dipindahkan ke `ConfirmSheet`, **tidak boleh diubah nilainya**.
- **`Switch` "Nominal berbeda"** dan input alasan di `SalaryPromptDialog` — keduanya mengikat validasi tombol konfirmasi.
- **Gate waktu `isDueExact`** pada tombol "Terima" gaji. Tombol sengaja tidak aktif sebelum tanggal jatuh tempo — ini aturan produk, bukan cacat UI.
- **`FilterChip` tipe & frekuensi** di `AddScheduleDialog` (`REC_ONCE` / `REC_BI_WEEKLY` / `REC_MONTHLY` / `REC_CUSTOM`) — nilainya masuk database dan menentukan perhitungan tanggal berikutnya.
- **Constanta `Constants`** dan seluruh nilai `type` pada entity — menentukan warna, ikon, filter, dan arah mutasi saldo.
- **`netWorth = cash + piutang − hutang`** — bila salah satu komponen disembunyikan, angka kekayaan bersih tidak bisa diverifikasi user.
- **Tanda `amount`** pada `TransactionItemEntity` — warna & ikon bergantung sepenuhnya pada ini.
- **Padding FAB `bottom = 90.dp`** dan **`contentPadding(bottom = 100.dp)`** pada `LazyColumn` — keduanya terikat tinggi bottom nav. Jika tinggi nav berubah, kedua nilai harus dihitung ulang bersamaan.
- **`BackHandler`** — back mengembalikan ke Beranda. Boleh dipertahankan apa adanya.

**Perlu kehati-hatian khusus:**

- `AppHeader` yang statis (fixed, tidak ikut scroll) di Beranda. Mengubahnya menjadi ikut scroll mengubah `Column`/`LazyColumn` terluar, yang berarti mengubah seluruh file.
- `combine()` di `FinancialViewModel` — jumlah flow yang digabung menentukan berapa kali UI recompose. Menambah state UI baru (mis. state filter di Riwayat) sebaiknya **lokal di Composable**, bukan flow baru.
- `fallbackToDestructiveMigration()` di `AppDatabase` — migrasi schema akan menghapus data user. Karena itu **tidak ada perubahan field entity dalam lingkup ini**.
- `Modifier.testTag` yang ada.

---

## 10. PERBAIKAN MENDESAK (P0) — BOLEH DIEKSEKUSI TANPA MENUNGGU PILIHAN ARAH

Delapan perbaikan ini **tidak mengandung keputusan estetika**. Semuanya memperbaiki kesalahan atau bahaya, dan hasilnya tetap valid di Arah A maupun Arah C. Inilah yang disarankan dikerjakan lebih dulu.

### P0-1 — Jangan pernah tampilkan `Rp 0` saat data belum siap

**Bukti:** `FinancialViewModel.kt:37` → `isInitialized: Boolean = false`; `:140` → `isInitialized = true`. Tidak ada satu pun pemakaian di `ui/screens/` (hasil pencarian kosong).

**Akar masalah:** `FinancialUiState()` default berisi `cashBalance = 0L`, sehingga UI merender angka valid yang **salah** selama beberapa frame pertama.

**Perbaikan:** di `BankingBalanceCard` (dan kartu ringkasan `ChecklistScreen` + `DebtAssetScreen`), bila `!uiState.isInitialized` render `—` alih-alih hasil `formatRupiah(0)`. Tidak perlu mengubah `ViewModel` — field sudah ada.

### P0-2 — Konfirmasi sebelum menghapus

**Bukti:** `MainActivity.kt:199` (`onDeleteSchedule`), `:213` (`onDeleteExpense`), `:227` (`onDeleteDebt`) memanggil `viewModel.deleteSchedule(schedule)` / `viewModel.deleteDebt(debt)` **langsung**.

**Pemicu dari UI (9 titik):** `CalendarScreen.kt:340`, `:370`; `ChecklistScreen.kt:283`, `:312`, `:332`; `DebtAssetScreen.kt:274`, `:293`, `:312`.

**Perbaikan:** satu `AlertDialog` konfirmasi reusable dirender di `MainActivity`, menampung `pendingDelete` di state, dieksekusi hanya setelah dikonfirmasi. Menampilkan **nama item** dan dampaknya (untuk hutang/piutang: sisa nominal yang akan hilang dari catatan).

### P0-3 — Bayar tagihan dari Kalender harus sekonsisten Beranda/Ceklis

**Bukti:** `MainActivity.kt:197-198` → `onPayExpense = { scheduleId -> viewModel.payExpenseChecklist(scheduleId, 1) }`. Beranda & Ceklis membuka dialog konfirmasi dengan pilihan periode; Kalender langsung memotong saldo satu periode tanpa konfirmasi.

**Perbaikan:** arahkan aksi dari Kalender ke dialog konfirmasi yang sama. Ini juga menghapus salah satu dari dua implementasi dialog tagihan yang duplikat.

### P0-4 — Perbaiki kontras warna nominal

**Bukti (hitungan):** `IncomeGreen #10B981` = **2.6:1** di atas putih; `ExpenseRed #EF4444` = **3.8:1**. Keduanya di bawah 4.5:1 yang disyaratkan WCAG AA untuk teks normal — dan nominal justru teks paling penting di aplikasi ini.

**Perbaikan:** tambah token `inText #047857` (5.48:1) dan `outText #B42318` (6.57:1) untuk **teks**; simpan `#10B981` / `#EF4444` untuk **fill/badge/ikon**. Perubahan nilai, bukan struktur.

### P0-5 — Pisahkan merah "pengeluaran" dari merah "bahaya"

**Bukti:** `Theme.kt` → `error = ExpenseRed`. Jadi "pengeluaran Rp 50.000" dan "hapus permanen" berwarna identik.

**Perbaikan:** token terpisah — `outText` untuk nilai uang keluar, `danger` untuk aksi destruktif (nilai boleh sama, **maknanya dipisah** agar bisa berbeda nanti). Tombol hapus memakai latar merah + teks putih, bukan teks merah di latar putih.

### P0-6 — `Checkbox` yang tidak pernah tercentang

**Bukti:** `HomeScreen.kt:1043` dan `ChecklistScreen.kt:503` → `checked = false` hardcode. (Bandingkan `SalaryPromptDialog.kt:131` yang memang toggle sungguhan.)

Kontrol ini berbohong: bentuknya checkbox, perilakunya tombol. **Perbaikan:** ganti menjadi tombol aksi eksplisit ("Bayar") atau ikon aksi, tanpa `Checkbox`.

### P0-7 — Naikkan lantai ukuran teks ke 12sp

**Bukti:** 27 kemunculan `fontSize = 10.sp` atau `11.sp` — `HomeScreen.kt` (13), `CalendarScreen.kt` (5), `DebtAssetScreen.kt` (4), `HistoryScreen.kt` (3), `ChecklistScreen.kt` (2), `MainActivity.kt` (1).

**Perbaikan:** ganti ke 12sp. Pada label section, kompensasi dengan `letterSpacing` agar tetap terasa kecil tanpa menjadi tak terbaca.

### P0-8 — Chip "Reset" di header Beranda

**Bukti:** `BankingHomeHeader` menempatkan chip tanggal dan chip `Reset` berdampingan, dengan area ketuk kecil. Tombol ini membuka alur **penghapusan seluruh data**.

**Perbaikan:** pindahkan "Kelola & Bersihkan Data" ke menu `⋮` di header, dan pertahankan dialog konfirmasi yang sudah ada. Ini memperbaiki masalah target ketuk sekaligus risiko salah tekan.

**Tidak ada di P0:** warna, radius, tipografi, tata letak, animasi, ikon. Semua itu menunggu pilihan arah.

---

## 11. PEKERJAAN KONSISTENSI (P1) — SETELAH PILIHAN ARAH

Dikerjakan bersama redesign karena menyentuh file yang sama.

1. `ui/theme/Color.kt` + `Type.kt` + `Theme.kt` → token arah terpilih, `darkTheme` hardcode `false` dipertahankan.
2. `Color.White` (55 kemunculan) → `MaterialTheme.colorScheme.surface`.
3. 5 header → 1 `AppHeader`.
4. `BankingTransactionRowItem` + `HistoryItemCard` → 1 `LedgerRow`.
5. 2 dialog "Konfirmasi Pembayaran Tagihan" → 1 `ConfirmSheet`.
6. 4 empty state → 1 `EmptyState`.
7. 8 `Surface(onClick)` → `InsetButton` / `PrimaryButton`.
8. 4 bentuk kartu kewajiban → 1 `ObligationCard`.
9. 6 radius → 3; padding kartu → 16dp seragam.
10. Hapus 7 warna template yang tidak terpakai di `res/values/colors.xml`.
11. Kumpulkan string literal ke satu `object Strings`.

---

## 12. RENCANA EKSEKUSI & KRITERIA SELESAI

Empat tahap. Setiap tahap harus dapat direview, dibangun, dan diuji sendiri.

### Tahap 0 — P0 (mandiri, tanpa menunggu arah)

**Selesai bila:**

- [ ] Saat aplikasi pertama dibuka, kartu saldo menampilkan `—` dan tidak pernah `Rp 0` sebelum data Room termuat.
- [ ] Menghapus tagihan / hutang / piutang dari Beranda, Kalender, Ceklis, dan Aset selalu meminta konfirmasi.
- [ ] Membayar tagihan dari Kalender memunculkan dialog konfirmasi yang sama seperti dari Beranda/Ceklis.
- [ ] Tidak ada nominal hijau tumpang tindih dengan latar putih pada kontras < 4.5:1.
- [ ] Tidak ada `Checkbox` dengan `checked = false` hardcode yang tersisa.
- [ ] Tidak ada `fontSize` di bawah 12sp.
- [ ] "Kelola & Bersihkan Data" tidak lagi berada di header Beranda.
- [ ] Aplikasi membangun dan berjalan tanpa regresi fungsional.

### Tahap 1 — Fondasi arah terpilih

Token + komponen bersama dibuat, **struktur screen belum diubah**. Hasilnya: seluruh aplikasi langsung terlihat konsisten meski tata letaknya masih lama.

**Selesai bila:**
- [ ] Tidak ada hex warna di luar `ui/theme/`.
- [ ] Tidak ada `Color.White` di Composable.
- [ ] Semua nominal memakai `MoneyText` dengan tabular figures.
- [ ] Hanya tiga nilai radius yang muncul di kode UI.
- [ ] Padding kartu 16dp di semua tempat.

### Tahap 2 — Beranda + Riwayat

Dua screen dengan dampak terbesar.

**Selesai bila:**
- [ ] Beranda hanya memuat: header, Balance Sheet, "Perlu Tindakan", "Akan Datang", satu CTA.
- [ ] Tidak ada Quick Actions dan tidak ada section mutasi di Beranda.
- [ ] Riwayat dikelompokkan per tanggal dengan saldo berjalan per transaksi.
- [ ] Keempat state (loading / empty / error / sukses) tampil benar dan dapat dipicu secara manual untuk diuji.

### Tahap 3 — Kalender, Ceklis, Aset + penutup

**Selesai bila:**
- [ ] Kelima screen memakai `AppHeader` dan `ObligationCard` yang sama.
- [ ] Semua dialog konfirmasi destruktif memakai `ConfirmSheet`.
- [ ] Layout tidak rusak pada `fontScale` 1.0, 1.3, dan 2.0.
- [ ] Semua target ketuk ≥ 48dp, terverifikasi di layar 320dp lebar.
- [ ] Tidak ada nama item yang terpotong di tengah kata pada `fontScale` normal.

---

## 13. BERKAS PRATINJAU

Semua SVG bisa dibuka langsung di browser (klik dua kali) atau lewat VS Code preview. Tidak perlu build, server, atau dependency.

| Berkas | Isi |
|---|---|
| `design-preview/index.html` | **Mulai dari sini.** Galeri perbandingan dua arah, berdampingan |
| `design-preview/buku-kas-beranda.svg` | Arah A — Beranda |
| `design-preview/buku-kas-riwayat.svg` | Arah A — Riwayat dengan saldo berjalan |
| `design-preview/buku-kas-komponen.svg` | Arah A — token, tipografi, `ObligationCard` semua state, tombol, empty state |
| `design-preview/playful-beranda.svg` | Arah C — Beranda |
| `design-preview/playful-riwayat.svg` | Arah C — Riwayat |
| `design-preview/playful-komponen.svg` | Arah C — token, tipografi, komponen, state |

Mockup digambar pada lebar **360dp** (layar Android terkecil yang umum). Angka, teks, dan warna di dalamnya **persis** nilai yang tertulis di §2 dan §3 — mockup ini adalah spesifikasi, bukan ilustrasi.

---

## 14. CARA MEMILIH

| Kalau kamu... | Pilih |
|---|---|
| Mengutamakan keterbacaan cepat, kepercayaan, dan aplikasi yang terasa "dewasa" | **Arah A — Buku Kas** |
| Ingin dominan dibanding aplikasi keuangan lain, ingin terasa hangat dan berkarakter | **Arah C — Playful Ledger** |
| Belum yakin | Lihat `design-preview/index.html`, lalu bandingkan Beranda keduanya berdampingan |

Yang **tidak** berbeda antar arah (jadi bukan bahan pertimbangan): struktur informasi, alur, komponen bersama, penanganan state, aksesibilitas, dan seluruh pekerjaan P0. Yang berbeda hanya lapisan visual — warna, tipografi, bentuk, dan gerak. Artinya memilih arah **tidak mengubah arsitektur aplikasi**, hanya tampilannya.

---

## 15. RISIKO

| Risiko | Mitigasi |
|---|---|
| Redesign menyentuh ±3.400 baris Compose di 13 file | Dikerjakan bertahap 0→3; setiap tahap dibangun dan dijalankan sebelum lanjut |
| `LazyColumn` + header statis di Beranda saling terkait | Tahap 2 mengerjakan Beranda sebagai satu kesatuan, bukan potongan |
| `fontScale` besar merusak tata letak yang memakai `maxLines = 1` | Diuji pada 1.3 dan 2.0 sebagai kriteria selesai Tahap 3 |
| Padding FAB/nav terikat tinggi bottom nav | Kedua nilai dihitung ulang bersamaan bila tinggi nav disentuh |
| Mockup SVG tidak identik dengan hasil Compose (line height, padding font) | Mockup mengikat **token**, tidak mengikat rendering; selisih beberapa dp dapat diterima |
| Menambah state filter di Riwayat → recompose berlebih | State filter tetap lokal di Composable, tidak masuk `combine()` di `ViewModel` |

---

## 16. PERTANYAAN YANG HARUS DIJAWAB SEBELUM EKSEKUSI

1. **Arah mana** yang dipakai — A (Buku Kas) atau C (Playful Ledger)?
2. **Bahasa visual kartu ATM** benar-benar dihapus (Arah A), atau tetap ada sebagai elemen brand (Arah B)?
3. **Nama produk**: header Beranda sekarang "PENCATAT KEUANGAN". Apakah tetap, atau diganti nama yang lebih pendek seperti "Buku Kas"?
4. **Hapus data**: undang-undang mana yang dipakai — konfirmasi dialog, atau langsung hapus + `Undo`? (Rekomendasi: `Undo` untuk jadwal/tagihan, konfirmasi untuk hutang/piutang.)
5. **`dueDate` pada hutang/piutang**: diaktifkan dengan input tanggal di dialog, atau dihapus dari lingkup UI selamanya?
6. **Tab "Selesai" & "Lunas"**: apakah item di sana otomatis dibersihkan setelah N bulan, atau disimpan permanen?
7. **Apakah Beranda boleh benar-benar kosong** ketika tidak ada tagihan? (Rekomendasi: ya, dengan `EmptyState` yang mengajak menambah tagihan.)
8. **Apakah lima tab bottom navigation tetap**, atau digabung menjadi empat (mis. Kalender + Ceklis menjadi "Jadwal")?
