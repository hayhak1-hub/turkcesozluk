# Türkçe Sözlük (esAnlamlı)

Çevrimdışı çalışan Türkçe eş anlamlı / sözlük uygulaması. Jetpack Compose ile yazılmış,
Google Play'de `com.hayhak.turkcesozluk` kimliğiyle yayınlanıyor.

## Özellikler

- **Beş sözlük kipi:** eş anlamlılar, fiiller, isimler (tanımlar), deyimler, sıfatlar — ve
  hepsini birleştiren `ALL` kipi. Kip `SettingsManager` üzerinden değişir, veri o anda
  yeniden yüklenir.
- **Çevrimdışı veri:** tüm sözlük `app/src/main/assets/*.csv` içinde gelir (~7,8 MB).
- **TDK yedeği:** yerelde bulunamayan kelimeler için `sozluk.gov.tr` sorgulanır.
- **Quiz, favoriler, öğrenme kartları, haftalık istatistik ve seri (streak) takibi.**
- **Oyun (Wordle tarzı):** ipucu olarak kelimenin anlamı verilir, kullanıcı kelimeyi
  6 denemede harf harf tahmin eder. Yeri kesinleşen harfler sonraki satıra taşınıp
  kilitlenir, işe yaramayacak tuşlar klavyede pasifleşir. Tur başına 2 "harf hediyesi"
  hakkı vardır. Hangi sözlükten oynanacağı oyun ekranındaki kip seçiciyle değiştirilir.
  Bilinemeyen kelime quizin "sadece yanlışlar" tekrar listesine yazılır.
- **Günün kelimesi** kartı ve günlük bildirim (WorkManager).
- **25 dilde arayüz**; tüm dil kaynakları çevrimdışı kullanım için uygulama paketine dahildir.

## Mimari

```
TurkceSozlukApp (Hilt)
  └── CoreRepository.initialize()      SettingsManager.modeState'i dinler
        └── CsvLoader                  assets/*.csv -> cacheDir'de ikili önbellek
              └── SynonymDataStore     ConcurrentHashMap + sıralı anahtar listeleri
                    ├── DictionaryRepository ─┐
                    └── QuizRepository       ─┼─> ViewModel'ler -> Compose ekranları
                        TdkRepository        ─┘
```

Notlar:

- CSV'yi elle ayrıştırmak büyük dosyalarda yavaş olduğundan sonuç `cacheDir` altında
  ikili biçimde önbelleğe alınır. Ayrıştırma biçimi değişirse `CsvLoader.CACHE_VERSION`
  artırılmalıdır; eski sürüm dosyaları otomatik silinir.
- `isimler.csv` (4,7 MB) yalnızca ilgili sekmeye ilk girişte yüklenir
  (`CoreRepository.ensureDefinitionsLoaded`).
- `SynonymDataStore.primaryKeys` yalnızca "ana" kelimeleri tutar; `cachedKeys` ters
  eşlemeleri (anlam cümlelerini) de içerir. Kullanıcıya kelime olarak gösterilecek her
  yerde `primaryKeys` kullanılmalıdır.

## Yerelleştirme

`app/src/main/res/values-*/strings.xml` dosyaları **elle düzenlenmez**; kaynak
`scripts/locales/*.json`'dır:

```bash
python scripts/generate_locales.py
```

Yeni bir anahtar eklerken: 25 JSON dosyasının hepsine değeri ekleyin, anahtarı
`scripts/generate_locales.py` içindeki `REQUIRED_KEYS` listesine yazın, betiği
çalıştırın ve varsayılan (Türkçe) `app/src/main/res/values/strings.xml` dosyasına
da elle ekleyin — bu dosya betik tarafından üretilmez. Eksik/fazla anahtar betikte
hata verir; CI üretilen dosyaların senkron olduğunu doğrular.

## Derleme

```bash
./gradlew :app:assembleEsanlamliDebug
```

Testler ve lint:

```bash
./gradlew :app:testEsanlamliDebugUnitTest :app:lintEsanlamliDebug
```

İmzalı sürüm için kök dizinde `keystore.properties` gerekir (`storeFile`,
`storePassword`, `esanlamliKeyAlias`, `esanlamliKeyPassword`). Dosya yoksa derleme
imzasız devam eder.

```bash
./gradlew :app:bundleEsanlamliRelease
```

## Test

Birim testler saf JVM üzerinde çalışır (`app/src/test`): CSV ayrıştırıcı, Türkçe
büyük/küçük harf kuralları, `SynonymDataStore`, sözlük/quiz repository'leri ve
oyunun tahmin değerlendirme motoru (`WordleEngine`).

Oyun mantığı bilerek Android'e bağımsız tutulmuştur: harf durumu hesabı
`game/WordleEngine.kt` içinde saf Kotlin'dir, aday kelime seçimi
`GameRepository`'dedir; ikisi de birim testle doğrulanır.

Room şemaları `app/schemas/` altına yazılır; bir sonraki şema sürümünün migration'ı
`MigrationTestHelper` ile enstrümantasyon testi olarak doğrulanabilir.

## Dizin düzeni

| Yol | İçerik |
| --- | --- |
| `app/src/main/java/.../data` | Room, repository'ler, CSV yükleyici, ayarlar |
| `app/src/main/java/.../ui` | Compose ekranları, bileşenler, tema |
| `app/src/main/java/.../viewmodel` | Ekran ViewModel'leri |
| `app/src/main/assets` | Sözlük CSV'leri ve gizlilik politikası |
| `scripts/` | Yerelleştirme ve Play Store listeleme üreticileri |
