#!/usr/bin/env python3
"""Apply the Firebase and user-initiated backup disclosure to every privacy locale."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
OUT = ROOT / "privacy_locales"
UPDATED = "2026-09-19"

# local storage/backup, Firebase, sharing, security
U = {
"tr": [
"Favoriler, kullanıcı kelimeleri, arama geçmişi, istatistikler, quiz yanlışları ve aralıklı tekrar ilerlemesi normalde yalnızca cihazınızda tutulur. Yalnızca siz ‘Yedeği kaydet’ seçeneğini kullandığınızda bu veriler seçtiğiniz dosya veya depolama sağlayıcısına aktarılır.",
"Reklam SDK’sı kullanmıyoruz. Kullanım ve kararlılığı anlamak için Google Firebase Analytics ve Firebase Crashlytics kullanıyoruz. Analytics; uygulama etkileşimleri, oturum bilgileri, uygulama örneği/cihaz tanımlayıcıları, cihaz ve uygulama bilgileri ile yaklaşık bölge gibi teknik verileri işleyebilir. Crashlytics; çökme ve ANR günlüklerini, yığın izlerini, uygulama/cihaz/işletim sistemi bilgilerini ve kurulum tanımlayıcılarını işleyebilir. Bu veriler Google tarafından Firebase koşulları ve gizlilik politikası uyarınca işlenir.",
"Kullanıcı verilerini satmayız. Veriler yalnızca burada açıklanan amaçlarla Google/Firebase’e, çevrimiçi sözlük sorgusu için TDK’ya ve yedekleme sırasında sizin seçtiğiniz depolama sağlayıcısına iletilebilir.",
"Çevrimiçi aktarımlar HTTPS kullanır. Dışa aktarılan JSON yedek dosyası uygulama tarafından şifrelenmez; dosyayı güvenli saklamak ve gerektiğinde silmek sizin sorumluluğunuzdadır. Seçtiğiniz depolama sağlayıcısının gizlilik koşulları geçerlidir."],
"en": [
"Favorites, user-added words, search history, statistics, quiz mistakes, and spaced-repetition progress normally remain only on your device. They are transferred to a file or storage provider chosen by you only when you use ‘Save backup’.",
"We do not use an advertising SDK. We use Google Firebase Analytics and Firebase Crashlytics to understand usage and stability. Analytics may process app interactions, session information, app-instance/device identifiers, device and app information, and approximate region. Crashlytics may process crash and ANR logs, stack traces, app/device/OS information, and installation identifiers. Google processes this data under the Firebase terms and Google privacy policy.",
"We do not sell user data. Data may be sent only for the purposes described here: to Google/Firebase, to TDK for online dictionary queries, and to the storage provider you choose for backup.",
"Online transfers use HTTPS. The exported JSON backup is not encrypted by the app; you are responsible for storing it securely and deleting it when no longer needed. The privacy terms of your chosen storage provider apply."],
"de": [
"Favoriten, eigene Wörter, Suchverlauf, Statistiken, Quizfehler und Lernfortschritt bleiben normalerweise nur auf deinem Gerät. Nur wenn du „Sicherung speichern“ wählst, werden diese Daten in eine Datei oder an den von dir gewählten Speicheranbieter übertragen.",
"Wir verwenden kein Werbe-SDK. Zur Analyse von Nutzung und Stabilität verwenden wir Google Firebase Analytics und Firebase Crashlytics. Analytics kann App-Interaktionen, Sitzungsdaten, App-Instanz-/Gerätekennungen, Geräte- und App-Informationen sowie die ungefähre Region verarbeiten. Crashlytics kann Absturz- und ANR-Protokolle, Stacktraces, App-/Geräte-/Betriebssystemdaten und Installationskennungen verarbeiten. Google verarbeitet diese Daten gemäß den Firebase-Bedingungen und der Google-Datenschutzerklärung.",
"Wir verkaufen keine Nutzerdaten. Daten können nur für die hier beschriebenen Zwecke an Google/Firebase, für Online-Wörterbuchabfragen an TDK und bei der Sicherung an den von dir gewählten Speicheranbieter übermittelt werden.",
"Online-Übertragungen erfolgen per HTTPS. Die exportierte JSON-Sicherungsdatei wird von der App nicht verschlüsselt; du bist für die sichere Aufbewahrung und Löschung verantwortlich. Es gelten die Datenschutzbedingungen des gewählten Speicheranbieters."],
"az": [
"Sevimlilər, əlavə etdiyiniz sözlər, axtarış tarixçəsi, statistika, quiz səhvləri və aralıqlı təkrar irəliləyişi adətən yalnız cihazınızda qalır. Bu məlumatlar yalnız “Yedəyi saxla” seçdikdə sizin seçdiyiniz fayla və ya yaddaş xidmətinə ötürülür.",
"Reklam SDK-sı istifadə etmirik. İstifadə və sabitliyi anlamaq üçün Google Firebase Analytics və Firebase Crashlytics istifadə edirik. Analytics tətbiq fəaliyyəti, sessiya məlumatı, tətbiq nümunəsi/cihaz identifikatorları, cihaz və tətbiq məlumatı və təxmini bölgəni emal edə bilər. Crashlytics çökmə və ANR qeydləri, yığın izləri, tətbiq/cihaz/ƏS məlumatı və quraşdırma identifikatorlarını emal edə bilər. Google bu məlumatları Firebase şərtləri və Google məxfilik siyasətinə əsasən emal edir.",
"İstifadəçi məlumatlarını satmırıq. Məlumat yalnız burada göstərilən məqsədlərlə Google/Firebase-ə, onlayn lüğət sorğuları üçün TDK-ya və yedək üçün seçdiyiniz yaddaş xidmətinə ötürülə bilər.",
"Onlayn ötürmələr HTTPS istifadə edir. İxrac edilən JSON yedəyi tətbiq tərəfindən şifrələnmir; onu təhlükəsiz saxlamaq və lazım olmadıqda silmək sizin məsuliyyətinizdir. Seçdiyiniz xidmətin məxfilik şərtləri tətbiq olunur."],
"kk": [
"Таңдаулылар, пайдаланушы қосқан сөздер, іздеу тарихы, статистика, тест қателері және аралық қайталау барысы әдетте тек құрылғыда қалады. Бұл деректер «Сақтық көшірмені сақтау» пәрменін таңдағанда ғана сіз таңдаған файлға немесе сақтау қызметіне беріледі.",
"Біз жарнама SDK-сын қолданбаймыз. Пайдалану мен тұрақтылықты түсіну үшін Google Firebase Analytics және Firebase Crashlytics қолданамыз. Analytics қолданба әрекеттерін, сеанс мәліметтерін, қолданба данасы/құрылғы идентификаторларын, құрылғы мен қолданба ақпаратын және шамамен аймақты өңдеуі мүмкін. Crashlytics бұзылу және ANR журналдарын, стек іздерін, қолданба/құрылғы/ОЖ ақпаратын және орнату идентификаторларын өңдеуі мүмкін. Google бұл деректерді Firebase шарттары мен Google құпиялық саясатына сай өңдейді.",
"Пайдаланушы деректерін сатпаймыз. Деректер тек осында сипатталған мақсаттармен Google/Firebase-ке, онлайн сөздік сұрауы үшін TDK-ға және сақтық көшірме үшін сіз таңдаған сақтау қызметіне жіберілуі мүмкін.",
"Онлайн тасымалдау HTTPS арқылы орындалады. Экспортталған JSON сақтық көшірмесін қолданба шифрламайды; оны қауіпсіз сақтау және қажет болмағанда жою сіздің жауапкершілігіңізде. Таңдалған сақтау қызметінің құпиялық шарттары қолданылады."],
"ky": [
"Тандалмалар, колдонуучу кошкон сөздөр, издөө таржымалы, статистика, тест каталары жана аралык кайталоо прогресси адатта түзмөктө гана калат. Бул маалымат «Камдык көчүрмөнү сактоо» тандалганда гана сиз тандаган файлга же сактагыч кызматына өткөрүлөт.",
"Жарнама SDK-сын колдонбойбуз. Колдонуу менен туруктуулукту түшүнүү үчүн Google Firebase Analytics жана Firebase Crashlytics колдонобуз. Analytics колдонмодогу аракеттерди, сессия маалыматтарын, колдонмо нускасы/түзмөк идентификаторлорун, түзмөк жана колдонмо маалыматын жана болжолдуу аймакты иштетиши мүмкүн. Crashlytics кыйроо жана ANR журналдарын, стек издерин, колдонмо/түзмөк/ОС маалыматын жана орнотуу идентификаторлорун иштетиши мүмкүн. Google бул маалыматты Firebase шарттарына жана Google купуялык саясатына ылайык иштетет.",
"Колдонуучу маалыматын сатпайбыз. Маалымат бул жерде айтылган максаттар үчүн гана Google/Firebase’ке, онлайн сөздүк суроосу үчүн TDK’га жана камдык көчүрмө үчүн сиз тандаган сактагыч кызматына жөнөтүлүшү мүмкүн.",
"Онлайн өткөрүүлөр HTTPS колдонот. Экспорттолгон JSON камдык көчүрмөсүн колдонмо шифрлебейт; аны коопсуз сактоо жана кереги жок болгондо өчүрүү сиздин жоопкерчилигиңиз. Тандалган сактагычтын купуялык шарттары колдонулат."],
"uz": [
"Sevimlilar, foydalanuvchi qo‘shgan so‘zlar, qidiruv tarixi, statistika, test xatolari va oraliqli takrorlash jarayoni odatda faqat qurilmangizda qoladi. Bu ma’lumotlar faqat “Zaxirani saqlash”ni tanlaganingizda siz tanlagan fayl yoki saqlash xizmatiga uzatiladi.",
"Biz reklama SDK-sidan foydalanmaymiz. Foydalanish va barqarorlikni tushunish uchun Google Firebase Analytics va Firebase Crashlytics’dan foydalanamiz. Analytics ilova harakatlari, seans ma’lumoti, ilova nusxasi/qurilma identifikatorlari, qurilma va ilova ma’lumoti hamda taxminiy hududni qayta ishlashi mumkin. Crashlytics nosozlik va ANR jurnallari, stek izlari, ilova/qurilma/OT ma’lumoti va o‘rnatish identifikatorlarini qayta ishlashi mumkin. Google bu ma’lumotlarni Firebase shartlari va Google maxfiylik siyosatiga muvofiq qayta ishlaydi.",
"Foydalanuvchi ma’lumotlarini sotmaymiz. Ma’lumot faqat shu yerda ko‘rsatilgan maqsadlarda Google/Firebase’ga, onlayn lug‘at so‘rovi uchun TDK’ga va zaxira uchun siz tanlagan saqlash xizmatiga yuborilishi mumkin.",
"Onlayn uzatish HTTPS orqali amalga oshiriladi. Eksport qilingan JSON zaxirasi ilova tomonidan shifrlanmaydi; uni xavfsiz saqlash va kerak bo‘lmaganda o‘chirish sizning mas’uliyatingiz. Tanlangan saqlash xizmatining maxfiylik shartlari amal qiladi."],
"tk": [
"Halanýanlar, ulanyjy goşan sözler, gözleg taryhy, statistika, test ýalňyşlary we aralyk gaýtalama ösüşi adatça diňe enjamyňyzda galýar. Bu maglumatlar diňe “Ätiýaçlygy sakla” saýlananda siziň saýlan faýlyňyza ýa-da saklaýyş hyzmatyňyza geçirilýär.",
"Mahabat SDK-syny ulanmaýarys. Ulanylyşy we durnuklylygy düşünmek üçin Google Firebase Analytics we Firebase Crashlytics ulanýarys. Analytics programma hereketlerini, sessiýa maglumatyny, programma nusgasy/enjam kesgitleýjilerini, enjam we programma maglumatyny hem-de takmynan sebiti işläp biler. Crashlytics çökme we ANR ýazgylaryny, stek yzlaryny, programma/enjam/OS maglumatyny we gurnama kesgitleýjilerini işläp biler. Google bu maglumatlary Firebase şertlerine we Google gizlinlik syýasatyna laýyklykda işleýär.",
"Ulanyjy maglumatlaryny satmaýarys. Maglumat diňe şu ýerde beýan edilen maksatlar üçin Google/Firebase-e, onlaýn sözlük soragy üçin TDK-a we ätiýaçlyk üçin saýlan saklaýyş hyzmatyňyza geçirilip bilner.",
"Onlaýn geçirimler HTTPS ulanýar. Eksport edilen JSON ätiýaçlygy programma tarapyndan şifrlenmeýär; ony howpsuz saklamak we gerek bolmadyk wagty öçürmek siziň jogapkärçiligiňizdir. Saýlan saklaýyş hyzmatyňyzyň gizlinlik şertleri ulanylýar."],
"zh-CN": [
"收藏词、用户添加的词语、搜索记录、统计数据、测验错题和间隔复习进度通常只保存在设备上。只有当您选择“保存备份”时，这些数据才会传输到您选择的文件或存储服务商。",
"我们不使用广告 SDK。我们使用 Google Firebase Analytics 和 Firebase Crashlytics 来了解使用情况和稳定性。Analytics 可能处理应用互动、会话信息、应用实例/设备标识符、设备与应用信息及大致区域；Crashlytics 可能处理崩溃和 ANR 日志、堆栈跟踪、应用/设备/操作系统信息及安装标识符。Google 会依据 Firebase 条款和 Google 隐私政策处理这些数据。",
"我们不出售用户数据。数据仅可能按本文所述目的发送给 Google/Firebase、用于在线词典查询的 TDK，以及您为备份选择的存储服务商。",
"在线传输使用 HTTPS。导出的 JSON 备份文件不会由应用加密；您有责任安全保管并在不再需要时删除。您选择的存储服务商的隐私条款同样适用。"],
"hi": [
"पसंदीदा शब्द, उपयोगकर्ता द्वारा जोड़े गए शब्द, खोज इतिहास, आँकड़े, क्विज़ की गलतियाँ और अंतराल पुनरावृत्ति की प्रगति सामान्यतः केवल आपके डिवाइस पर रहती है। ‘बैकअप सहेजें’ चुनने पर ही यह डेटा आपकी चुनी हुई फ़ाइल या स्टोरेज सेवा को भेजा जाता है।",
"हम विज्ञापन SDK का उपयोग नहीं करते। उपयोग और स्थिरता समझने के लिए Google Firebase Analytics और Firebase Crashlytics का उपयोग करते हैं। Analytics ऐप गतिविधि, सत्र जानकारी, ऐप-इंस्टेंस/डिवाइस पहचानकर्ता, डिवाइस व ऐप जानकारी और अनुमानित क्षेत्र संसाधित कर सकता है। Crashlytics क्रैश व ANR लॉग, स्टैक ट्रेस, ऐप/डिवाइस/OS जानकारी और इंस्टॉलेशन पहचानकर्ता संसाधित कर सकता है। Google इस डेटा को Firebase की शर्तों और Google गोपनीयता नीति के अनुसार संसाधित करता है।",
"हम उपयोगकर्ता डेटा नहीं बेचते। डेटा केवल यहाँ बताए उद्देश्यों के लिए Google/Firebase, ऑनलाइन शब्दकोश प्रश्न हेतु TDK और बैकअप के लिए आपकी चुनी स्टोरेज सेवा को भेजा जा सकता है।",
"ऑनलाइन स्थानांतरण HTTPS का उपयोग करते हैं। निर्यात की गई JSON बैकअप फ़ाइल ऐप द्वारा एन्क्रिप्ट नहीं होती; उसे सुरक्षित रखना और आवश्यकता न होने पर हटाना आपकी जिम्मेदारी है। चुनी हुई स्टोरेज सेवा की गोपनीयता शर्तें लागू होती हैं।"],
"es": [
"Los favoritos, las palabras añadidas, el historial de búsqueda, las estadísticas, los errores del cuestionario y el progreso de repetición espaciada normalmente permanecen solo en tu dispositivo. Solo se transfieren al archivo o proveedor de almacenamiento que elijas cuando usas «Guardar copia».",
"No usamos SDK publicitarios. Usamos Google Firebase Analytics y Firebase Crashlytics para comprender el uso y la estabilidad. Analytics puede tratar interacciones, datos de sesión, identificadores de instancia/dispositivo, información del dispositivo y la aplicación y región aproximada. Crashlytics puede tratar registros de fallos y ANR, trazas de pila, información de la aplicación/dispositivo/SO e identificadores de instalación. Google trata estos datos conforme a las condiciones de Firebase y su política de privacidad.",
"No vendemos datos de usuarios. Los datos solo pueden enviarse para los fines descritos: a Google/Firebase, a TDK para consultas del diccionario y al proveedor de almacenamiento que elijas para la copia de seguridad.",
"Las transferencias en línea usan HTTPS. La copia JSON exportada no está cifrada por la aplicación; eres responsable de guardarla de forma segura y eliminarla cuando ya no sea necesaria. Se aplican las condiciones del proveedor elegido."],
"fr": [
"Les favoris, mots ajoutés, recherches, statistiques, erreurs de quiz et progrès de répétition espacée restent normalement sur votre appareil. Ils ne sont transférés vers le fichier ou fournisseur de stockage choisi que lorsque vous utilisez « Enregistrer la sauvegarde ».",
"Nous n’utilisons aucun SDK publicitaire. Nous utilisons Google Firebase Analytics et Firebase Crashlytics pour comprendre l’usage et la stabilité. Analytics peut traiter les interactions, données de session, identifiants d’instance/appareil, informations sur l’appareil et l’application et région approximative. Crashlytics peut traiter les journaux de plantage et d’ANR, traces de pile, informations application/appareil/OS et identifiants d’installation. Google traite ces données selon les conditions Firebase et sa politique de confidentialité.",
"Nous ne vendons pas les données utilisateur. Elles peuvent uniquement être transmises aux fins décrites : à Google/Firebase, à TDK pour les recherches en ligne et au fournisseur de stockage choisi pour la sauvegarde.",
"Les transferts en ligne utilisent HTTPS. La sauvegarde JSON exportée n’est pas chiffrée par l’application ; vous devez la conserver en sécurité et la supprimer lorsqu’elle n’est plus utile. Les conditions du fournisseur choisi s’appliquent."],
"ar": [
"تبقى المفضلة والكلمات المضافة وسجل البحث والإحصاءات وأخطاء الاختبار وتقدم التكرار المتباعد عادةً على جهازك فقط. ولا تُنقل إلى الملف أو مزود التخزين الذي تختاره إلا عند استخدام «حفظ النسخة الاحتياطية».",
"لا نستخدم حزمة SDK للإعلانات. نستخدم Google Firebase Analytics وFirebase Crashlytics لفهم الاستخدام والاستقرار. قد تعالج Analytics تفاعلات التطبيق ومعلومات الجلسة ومعرفات نسخة التطبيق/الجهاز ومعلومات الجهاز والتطبيق والمنطقة التقريبية. وقد تعالج Crashlytics سجلات الأعطال وANR وتتبع المكدس ومعلومات التطبيق/الجهاز/نظام التشغيل ومعرفات التثبيت. تعالج Google هذه البيانات وفق شروط Firebase وسياسة خصوصية Google.",
"لا نبيع بيانات المستخدم. لا يجوز إرسال البيانات إلا للأغراض الموضحة هنا: إلى Google/Firebase، وإلى TDK لاستعلامات القاموس، وإلى مزود التخزين الذي تختاره للنسخ الاحتياطي.",
"تستخدم عمليات النقل عبر الإنترنت HTTPS. لا يشفّر التطبيق ملف النسخة الاحتياطية JSON المُصدّر؛ وأنت مسؤول عن حفظه بأمان وحذفه عند عدم الحاجة إليه. تسري شروط الخصوصية الخاصة بمزود التخزين المختار."],
"bn": [
"পছন্দের শব্দ, ব্যবহারকারীর যোগ করা শব্দ, অনুসন্ধানের ইতিহাস, পরিসংখ্যান, কুইজের ভুল এবং বিরতিযুক্ত পুনরাবৃত্তির অগ্রগতি সাধারণত শুধু আপনার ডিভাইসেই থাকে। ‘ব্যাকআপ সংরক্ষণ’ ব্যবহার করলেই এগুলো আপনার বেছে নেওয়া ফাইল বা স্টোরেজ সেবায় পাঠানো হয়।",
"আমরা বিজ্ঞাপন SDK ব্যবহার করি না। ব্যবহার ও স্থিতিশীলতা বুঝতে Google Firebase Analytics এবং Firebase Crashlytics ব্যবহার করি। Analytics অ্যাপের কার্যকলাপ, সেশন তথ্য, অ্যাপ-ইনস্ট্যান্স/ডিভাইস শনাক্তকারী, ডিভাইস ও অ্যাপের তথ্য এবং আনুমানিক অঞ্চল প্রক্রিয়া করতে পারে। Crashlytics ক্র্যাশ ও ANR লগ, স্ট্যাক ট্রেস, অ্যাপ/ডিভাইস/OS তথ্য এবং ইনস্টলেশন শনাক্তকারী প্রক্রিয়া করতে পারে। Google Firebase-এর শর্ত ও Google গোপনীয়তা নীতি অনুযায়ী এসব তথ্য প্রক্রিয়া করে।",
"আমরা ব্যবহারকারীর তথ্য বিক্রি করি না। তথ্য কেবল এখানে বর্ণিত উদ্দেশ্যে Google/Firebase, অনলাইন অভিধান অনুসন্ধানের জন্য TDK এবং ব্যাকআপের জন্য আপনার বেছে নেওয়া স্টোরেজ সেবায় পাঠানো হতে পারে।",
"অনলাইন স্থানান্তরে HTTPS ব্যবহৃত হয়। রপ্তানি করা JSON ব্যাকআপ অ্যাপ এনক্রিপ্ট করে না; নিরাপদে রাখা ও প্রয়োজন না হলে মুছে ফেলা আপনার দায়িত্ব। বেছে নেওয়া স্টোরেজ সেবার গোপনীয়তার শর্ত প্রযোজ্য।"],
"pt": [
"Favoritos, palavras adicionadas, histórico de pesquisa, estatísticas, erros do quiz e progresso de repetição espaçada normalmente permanecem apenas no dispositivo. Só são transferidos ao arquivo ou provedor de armazenamento escolhido quando você usa “Salvar backup”.",
"Não usamos SDK de publicidade. Usamos Google Firebase Analytics e Firebase Crashlytics para entender o uso e a estabilidade. O Analytics pode processar interações, dados de sessão, identificadores da instância/dispositivo, informações do dispositivo e app e região aproximada. O Crashlytics pode processar registros de falhas e ANR, rastreamentos de pilha, informações do app/dispositivo/SO e identificadores de instalação. O Google processa esses dados segundo os termos do Firebase e sua política de privacidade.",
"Não vendemos dados dos usuários. Os dados só podem ser enviados para os fins descritos: ao Google/Firebase, ao TDK para consultas do dicionário e ao provedor de armazenamento escolhido para o backup.",
"As transferências online usam HTTPS. O backup JSON exportado não é criptografado pelo app; você é responsável por guardá-lo com segurança e excluí-lo quando não precisar mais. Aplicam-se os termos do provedor escolhido."],
"ru": [
"Избранное, добавленные слова, история поиска, статистика, ошибки викторины и прогресс интервальных повторений обычно остаются только на устройстве. Они передаются в выбранный файл или хранилище только при использовании функции «Сохранить резервную копию».",
"Мы не используем рекламные SDK. Для анализа использования и стабильности применяются Google Firebase Analytics и Firebase Crashlytics. Analytics может обрабатывать действия в приложении, данные сеансов, идентификаторы экземпляра приложения/устройства, сведения об устройстве и приложении и примерный регион. Crashlytics может обрабатывать журналы сбоев и ANR, трассировки стека, сведения о приложении/устройстве/ОС и идентификаторы установки. Google обрабатывает эти данные по условиям Firebase и политике конфиденциальности Google.",
"Мы не продаём данные пользователей. Они могут передаваться только для описанных целей: Google/Firebase, TDK для онлайн-запросов и выбранному вами хранилищу для резервной копии.",
"Онлайн-передача выполняется по HTTPS. Экспортированный файл JSON не шифруется приложением; вы отвечаете за его безопасное хранение и удаление. Применяются условия конфиденциальности выбранного хранилища."],
"ur": [
"پسندیدہ الفاظ، صارف کے شامل کردہ الفاظ، تلاش کی سرگزشت، اعداد و شمار، کوئز کی غلطیاں اور وقفہ وار دہرائی کی پیش رفت عموماً صرف آپ کے آلے پر رہتی ہے۔ یہ معلومات صرف ’بیک اپ محفوظ کریں‘ استعمال کرنے پر آپ کی منتخب فائل یا اسٹوریج سروس کو منتقل ہوتی ہیں۔",
"ہم اشتہاری SDK استعمال نہیں کرتے۔ استعمال اور استحکام سمجھنے کے لیے Google Firebase Analytics اور Firebase Crashlytics استعمال کرتے ہیں۔ Analytics ایپ تعاملات، سیشن معلومات، ایپ انسٹینس/ڈیوائس شناخت کنندگان، ڈیوائس و ایپ معلومات اور تقریبی علاقہ پراسیس کر سکتا ہے۔ Crashlytics کریش و ANR لاگز، اسٹیک ٹریس، ایپ/ڈیوائس/OS معلومات اور انسٹالیشن شناخت کنندگان پراسیس کر سکتا ہے۔ Google یہ ڈیٹا Firebase شرائط اور Google رازداری پالیسی کے مطابق پراسیس کرتا ہے۔",
"ہم صارف کا ڈیٹا فروخت نہیں کرتے۔ ڈیٹا صرف یہاں بیان کردہ مقاصد کے لیے Google/Firebase، آن لائن لغت کے لیے TDK اور بیک اپ کے لیے آپ کی منتخب اسٹوریج سروس کو بھیجا جا سکتا ہے۔",
"آن لائن منتقلی HTTPS استعمال کرتی ہے۔ برآمد شدہ JSON بیک اپ کو ایپ خفیہ نہیں کرتی؛ اسے محفوظ رکھنا اور ضرورت نہ ہونے پر حذف کرنا آپ کی ذمہ داری ہے۔ منتخب اسٹوریج سروس کی رازداری شرائط لاگو ہوتی ہیں۔"],
"id": [
"Favorit, kata yang ditambahkan, riwayat pencarian, statistik, kesalahan kuis, dan kemajuan pengulangan berjeda biasanya hanya tersimpan di perangkat. Data baru dikirim ke berkas atau penyedia penyimpanan pilihan Anda saat Anda memakai “Simpan cadangan”.",
"Kami tidak menggunakan SDK iklan. Kami menggunakan Google Firebase Analytics dan Firebase Crashlytics untuk memahami penggunaan dan stabilitas. Analytics dapat memproses interaksi aplikasi, informasi sesi, pengenal instans aplikasi/perangkat, informasi perangkat dan aplikasi, serta perkiraan wilayah. Crashlytics dapat memproses log crash dan ANR, stack trace, informasi aplikasi/perangkat/OS, dan pengenal instalasi. Google memproses data ini berdasarkan persyaratan Firebase dan kebijakan privasi Google.",
"Kami tidak menjual data pengguna. Data hanya dapat dikirim untuk tujuan yang dijelaskan: ke Google/Firebase, ke TDK untuk kueri kamus daring, dan ke penyedia penyimpanan yang Anda pilih untuk cadangan.",
"Transfer daring menggunakan HTTPS. Cadangan JSON yang diekspor tidak dienkripsi oleh aplikasi; Anda bertanggung jawab menyimpan dan menghapusnya dengan aman. Persyaratan privasi penyedia pilihan Anda berlaku."],
"ja": [
"お気に入り、追加した単語、検索履歴、統計、クイズの間違い、間隔反復の進捗は通常、端末内だけに保存されます。「バックアップを保存」を使用した場合に限り、選択したファイルまたはストレージ事業者へ転送されます。",
"広告 SDK は使用していません。利用状況と安定性を把握するため、Google Firebase Analytics と Firebase Crashlytics を使用します。Analytics はアプリ操作、セッション情報、アプリインスタンス／端末識別子、端末・アプリ情報、概算地域を処理する場合があります。Crashlytics はクラッシュ・ANR ログ、スタックトレース、アプリ／端末／OS 情報、インストール識別子を処理する場合があります。Google は Firebase の規約とプライバシーポリシーに従って処理します。",
"ユーザーデータを販売しません。データは、ここに記載した目的に限り、Google/Firebase、オンライン辞書照会の TDK、バックアップ先として選択したストレージ事業者へ送信される場合があります。",
"オンライン転送には HTTPS を使用します。出力された JSON バックアップはアプリでは暗号化されません。安全な保管と不要時の削除は利用者の責任です。選択した事業者のプライバシー条件が適用されます。"],
"sw": [
"Vipendwa, maneno yaliyoongezwa, historia ya utafutaji, takwimu, makosa ya jaribio na maendeleo ya marudio kwa vipindi kwa kawaida hubaki kwenye kifaa chako. Hutumwa kwenye faili au huduma ya hifadhi uliyochagua tu unapotumia “Hifadhi nakala”.",
"Hatutumii SDK ya matangazo. Tunatumia Google Firebase Analytics na Firebase Crashlytics kuelewa matumizi na uthabiti. Analytics inaweza kuchakata mwingiliano wa programu, taarifa za kipindi, vitambulisho vya programu/kifaa, taarifa za kifaa na programu na eneo la kukadiria. Crashlytics inaweza kuchakata kumbukumbu za hitilafu na ANR, ufuatiliaji wa rundo, taarifa za programu/kifaa/OS na vitambulisho vya usakinishaji. Google huchakata data hii kwa masharti ya Firebase na sera ya faragha ya Google.",
"Hatuuzi data ya watumiaji. Data inaweza kutumwa tu kwa madhumuni yaliyoelezwa: Google/Firebase, TDK kwa hoja za kamusi mtandaoni na huduma ya hifadhi uliyochagua kwa nakala.",
"Uhamisho mtandaoni hutumia HTTPS. Nakala ya JSON inayohamishwa haisimbwi na programu; una wajibu wa kuihifadhi salama na kuifuta isipohitajika. Masharti ya faragha ya huduma uliyochagua yanatumika."],
"mr": [
"आवडते शब्द, वापरकर्त्याने जोडलेले शब्द, शोध इतिहास, आकडेवारी, प्रश्नमंजुषेतील चुका आणि अंतरित पुनरावृत्तीची प्रगती साधारणतः फक्त उपकरणावर राहते. ‘बॅकअप जतन करा’ वापरल्यावरच हा डेटा तुम्ही निवडलेल्या फाइल किंवा स्टोरेज सेवेकडे पाठवला जातो.",
"आम्ही जाहिरात SDK वापरत नाही. वापर आणि स्थिरता समजण्यासाठी Google Firebase Analytics व Firebase Crashlytics वापरतो. Analytics ॲप परस्परक्रिया, सत्र माहिती, ॲप-इन्स्टन्स/उपकरण ओळखकर्ता, उपकरण व ॲप माहिती आणि अंदाजे प्रदेश प्रक्रिया करू शकते. Crashlytics क्रॅश व ANR लॉग, स्टॅक ट्रेस, ॲप/उपकरण/OS माहिती आणि इंस्टॉलेशन ओळखकर्ता प्रक्रिया करू शकते. Google हा डेटा Firebase अटी आणि Google गोपनीयता धोरणानुसार प्रक्रिया करते.",
"आम्ही वापरकर्ता डेटा विकत नाही. डेटा फक्त येथे नमूद उद्देशांसाठी Google/Firebase, ऑनलाइन शब्दकोशासाठी TDK आणि बॅकअपसाठी तुम्ही निवडलेल्या स्टोरेज सेवेला पाठवला जाऊ शकतो.",
"ऑनलाइन हस्तांतरण HTTPS वापरते. निर्यात केलेला JSON बॅकअप ॲपकडून कूटबद्ध केला जात नाही; तो सुरक्षित ठेवणे व गरज नसताना हटवणे तुमची जबाबदारी आहे. निवडलेल्या सेवेच्या गोपनीयता अटी लागू होतात."],
"te": [
"ఇష్టమైన పదాలు, వినియోగదారు జోడించిన పదాలు, శోధన చరిత్ర, గణాంకాలు, క్విజ్ తప్పులు మరియు విరామ పునరావృత పురోగతి సాధారణంగా మీ పరికరంలోనే ఉంటాయి. ‘బ్యాకప్ సేవ్ చేయి’ ఉపయోగించినప్పుడు మాత్రమే ఇవి మీరు ఎంచుకున్న ఫైల్ లేదా స్టోరేజ్ సేవకు పంపబడతాయి.",
"మేము ప్రకటన SDKను ఉపయోగించము. వినియోగం మరియు స్థిరత్వాన్ని అర్థం చేసుకోవడానికి Google Firebase Analytics మరియు Firebase Crashlytics ఉపయోగిస్తాము. Analytics యాప్ పరస్పర చర్యలు, సెషన్ సమాచారం, యాప్-ఇన్‌స్టాన్స్/పరికర గుర్తింపులు, పరికరం మరియు యాప్ సమాచారం, సుమారు ప్రాంతాన్ని ప్రాసెస్ చేయవచ్చు. Crashlytics క్రాష్ మరియు ANR లాగ్‌లు, స్టాక్ ట్రేస్‌లు, యాప్/పరికరం/OS సమాచారం మరియు ఇన్‌స్టాలేషన్ గుర్తింపులను ప్రాసెస్ చేయవచ్చు. Google ఈ డేటాను Firebase నిబంధనలు మరియు Google గోప్యతా విధానం ప్రకారం ప్రాసెస్ చేస్తుంది.",
"మేము వినియోగదారు డేటాను విక్రయించము. ఇక్కడ వివరించిన ప్రయోజనాల కోసం మాత్రమే Google/Firebaseకు, ఆన్‌లైన్ నిఘంటువు కోసం TDKకు మరియు బ్యాకప్ కోసం మీరు ఎంచుకున్న స్టోరేజ్ సేవకు డేటా పంపబడవచ్చు.",
"ఆన్‌లైన్ బదిలీలు HTTPSను ఉపయోగిస్తాయి. ఎగుమతి చేసిన JSON బ్యాకప్‌ను యాప్ ఎన్‌క్రిప్ట్ చేయదు; దాన్ని సురక్షితంగా ఉంచడం మరియు అవసరం లేనప్పుడు తొలగించడం మీ బాధ్యత. ఎంచుకున్న సేవ గోప్యతా నిబంధనలు వర్తిస్తాయి."],
"ta": [
"பிடித்த சொற்கள், பயனர் சேர்த்த சொற்கள், தேடல் வரலாறு, புள்ளிவிவரங்கள், வினாடி வினா தவறுகள் மற்றும் இடைவெளி மீள்பார்வை முன்னேற்றம் பொதுவாக உங்கள் சாதனத்தில் மட்டுமே இருக்கும். ‘காப்புப்பிரதியைச் சேமி’ பயன்படுத்தும்போது மட்டுமே நீங்கள் தேர்ந்தெடுத்த கோப்பு அல்லது சேமிப்பகச் சேவைக்கு இவை அனுப்பப்படும்.",
"விளம்பர SDK-ஐ பயன்படுத்துவதில்லை. பயன்பாடு மற்றும் நிலைத்தன்மையைப் புரிந்துகொள்ள Google Firebase Analytics மற்றும் Firebase Crashlytics-ஐ பயன்படுத்துகிறோம். Analytics செயலி தொடர்புகள், அமர்வு தகவல், செயலி-நிகழ்வு/சாதன அடையாளங்கள், சாதனம் மற்றும் செயலி தகவல், தோராயமான பகுதியைச் செயலாக்கலாம். Crashlytics செயலிழப்பு மற்றும் ANR பதிவுகள், அடுக்கு தடங்கள், செயலி/சாதனம்/OS தகவல் மற்றும் நிறுவல் அடையாளங்களைச் செயலாக்கலாம். Google இந்தத் தரவை Firebase விதிமுறைகள் மற்றும் Google தனியுரிமைக் கொள்கைப்படி செயலாக்குகிறது.",
"பயனர் தரவை விற்கமாட்டோம். இங்கு விவரிக்கப்பட்ட நோக்கங்களுக்காக மட்டும் Google/Firebase, இணைய அகராதி வினவலுக்காக TDK மற்றும் காப்புப்பிரதிக்காக நீங்கள் தேர்ந்தெடுத்த சேமிப்பகச் சேவைக்கு தரவு அனுப்பப்படலாம்.",
"இணையப் பரிமாற்றங்கள் HTTPS-ஐ பயன்படுத்துகின்றன. ஏற்றுமதி செய்யப்பட்ட JSON காப்புப்பிரதி செயலியால் குறியாக்கப்படாது; அதை பாதுகாப்பாக வைத்திருப்பதும் தேவையில்லாதபோது நீக்குவதும் உங்கள் பொறுப்பு. தேர்ந்தெடுத்த சேவையின் தனியுரிமை விதிகள் பொருந்தும்."],
"vi": [
"Mục yêu thích, từ do người dùng thêm, lịch sử tìm kiếm, thống kê, câu trả lời sai và tiến độ ôn tập ngắt quãng thường chỉ nằm trên thiết bị. Dữ liệu chỉ được chuyển tới tệp hoặc nhà cung cấp lưu trữ bạn chọn khi dùng “Lưu bản sao lưu”.",
"Chúng tôi không dùng SDK quảng cáo. Chúng tôi dùng Google Firebase Analytics và Firebase Crashlytics để hiểu việc sử dụng và độ ổn định. Analytics có thể xử lý tương tác trong ứng dụng, thông tin phiên, mã nhận dạng phiên bản ứng dụng/thiết bị, thông tin thiết bị và ứng dụng và khu vực gần đúng. Crashlytics có thể xử lý nhật ký sự cố và ANR, dấu vết ngăn xếp, thông tin ứng dụng/thiết bị/HĐH và mã nhận dạng cài đặt. Google xử lý dữ liệu theo điều khoản Firebase và chính sách quyền riêng tư của Google.",
"Chúng tôi không bán dữ liệu người dùng. Dữ liệu chỉ có thể được gửi cho mục đích đã nêu: tới Google/Firebase, TDK để tra từ điển trực tuyến và nhà cung cấp lưu trữ bạn chọn để sao lưu.",
"Truyền dữ liệu trực tuyến dùng HTTPS. Bản sao lưu JSON xuất ra không được ứng dụng mã hóa; bạn chịu trách nhiệm lưu giữ an toàn và xóa khi không cần. Điều khoản của nhà cung cấp lưu trữ đã chọn được áp dụng."],
"ko": [
"즐겨찾기, 사용자가 추가한 단어, 검색 기록, 통계, 퀴즈 오답 및 간격 반복 진행 상황은 일반적으로 기기에만 저장됩니다. ‘백업 저장’을 사용할 때만 사용자가 선택한 파일 또는 저장소 제공업체로 전송됩니다.",
"광고 SDK는 사용하지 않습니다. 사용 현황과 안정성을 파악하기 위해 Google Firebase Analytics와 Firebase Crashlytics를 사용합니다. Analytics는 앱 상호작용, 세션 정보, 앱 인스턴스/기기 식별자, 기기 및 앱 정보와 대략적인 지역을 처리할 수 있습니다. Crashlytics는 비정상 종료 및 ANR 로그, 스택 추적, 앱/기기/OS 정보와 설치 식별자를 처리할 수 있습니다. Google은 Firebase 약관과 Google 개인정보처리방침에 따라 이를 처리합니다.",
"사용자 데이터를 판매하지 않습니다. 데이터는 여기에 설명한 목적으로만 Google/Firebase, 온라인 사전 조회를 위한 TDK 및 백업을 위해 사용자가 선택한 저장소 제공업체로 전송될 수 있습니다.",
"온라인 전송에는 HTTPS를 사용합니다. 내보낸 JSON 백업은 앱에서 암호화하지 않으므로 안전한 보관 및 불필요할 때 삭제할 책임은 사용자에게 있습니다. 선택한 저장소 제공업체의 개인정보 보호 약관이 적용됩니다."],
}


def main() -> None:
    files = {p.stem: p for p in OUT.glob("*.json")}
    if set(files) != set(U):
        raise SystemExit(f"Locale mismatch: files={sorted(files)} updates={sorted(U)}")
    for code, path in sorted(files.items()):
        data = json.loads(path.read_text(encoding="utf-8"))
        local, firebase, sharing, security = U[code]
        firebase += (' <a href="https://firebase.google.com/support/privacy" rel="noopener noreferrer">'
                     'Firebase Privacy and Security</a> · '
                     '<a href="https://policies.google.com/privacy" rel="noopener noreferrer">'
                     'Google Privacy Policy</a>')
        if len(data.get("sections", [])) != 12:
            raise SystemExit(f"{code}: expected 12 sections")
        data["updatedDate"] = UPDATED
        data["sections"][2]["paragraphs"][0] = local
        data["sections"][5]["paragraphs"][1] = firebase
        data["sections"][6]["paragraphs"][0] = sharing
        data["sections"][7]["paragraphs"][0] = security
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"Updated {code}")


if __name__ == "__main__":
    main()
