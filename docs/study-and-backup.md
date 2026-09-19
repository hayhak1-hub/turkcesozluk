# Öğrenme ve yedekleme

- Quiz başlangıcındaki **Yanlışlardan quiz** seçeneği, geçerli sözlük modunda daha önce yanlış cevaplanan soruları açar. Doğru cevaplanan soru yanlışlar listesinden çıkarılır. Önceki sürümlerdeki yanlış cevaplar kaydedilmediği için geriye dönük eklenmez.
- Favorilerdeki **Bugünkü tekrarlar**, yeni ve tekrar tarihi gelmiş kartları açar. Kartı çevirdikten sonra verilen yanıt kaydedilir. Biliyorum aralıkları: 1, 3, 7, 14, 30, 60 gün. Tekrar göster: 10 dakika. Otomatik bildirim eklenmemiştir.
- Profil/istatistik ekranındaki yedekleme kartı JSON dosyasını dışa aktarır veya geri yükler. Favoriler, kullanıcı kelimeleri, arama geçmişi, etkinlik istatistikleri, yanlış sorular ve tekrar tarihleri dahildir. Dil/tema ayarları ve hazır sözlük içeriği dahil değildir.
- Geri yükleme mevcut kayıtları silmez; aynı kaydın daha yeni ilerlemesini korur. Dosya sınırı 10 MiB'dir. Yedek şifrelenmez; güvenli bir yerde saklanmalıdır. Otomatik bulut senkronizasyonu değildir.
- Room veritabanı 4'ten 5'e, mevcut tablolar silinmeden yeni `study_records` tablosu eklenerek geçirilir.

## Cihaz doğrulama listesi

1. Eski sürümün favori ve geçmiş kayıtları varken güncelleyin; kayıtların korunduğunu doğrulayın.
2. Quizde yanlış cevap verin; uygulamayı yeniden açıp yanlışlar quizinde soruyu bulun. Doğru cevaplayıp listeden çıktığını doğrulayın.
3. Favori kartı değerlendirin; uygulamayı yeniden açıp zamanı gelmeden bugünkü tekrarlarda görünmediğini doğrulayın.
4. Android dosya seçicisi ile yedek alın, geri yükleyin; aynı yedeği iki kere yüklemenin kopya kayıt üretmediğini kontrol edin.
5. Bozuk ve desteklenmeyen sürümlü yedeklerde mevcut kayıtların değişmediğini doğrulayın.
6. Büyük yazı boyutu, küçük ekran ve RTL dillerinde kartları, butonları ve kaydırmayı kontrol edin.

`BackupRepositoryTest` cihaz testleri yedek turunu, birleştirmeyi ve geçersiz dosyayı reddetmeyi kapsar. Cihaz/emülatörde ayrıca çalıştırılmalıdır.
