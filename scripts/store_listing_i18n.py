# -*- coding: utf-8 -*-
"""Google Play Store listing copy — keyed by app locale (matches AppLocale.kt)."""

# All 25 in-app UI languages (same order as AppLocale.kt, excluding SYSTEM).
APP_LOCALES = [
    "tr",
    "az",
    "kk",
    "ky",
    "uz",
    "tk",
    "en",
    "zh-CN",
    "hi",
    "es",
    "fr",
    "ar",
    "bn",
    "pt",
    "ru",
    "ur",
    "id",
    "de",
    "ja",
    "sw",
    "mr",
    "te",
    "ta",
    "vi",
    "ko",
]

# Play Console store-listing locale per app tag; None = app-only (no Play listing).
APP_TO_PLAY: dict[str, str | None] = {
    "tr": "tr-TR",
    "az": "az-AZ",
    "kk": "kk",
    "ky": "ky-KG",
    "uz": None,
    "tk": None,
    "en": "en-US",
    "zh-CN": "zh-CN",
    "hi": "hi-IN",
    "es": "es-ES",
    "fr": "fr-FR",
    "ar": "ar",
    "bn": "bn-BD",
    "pt": "pt-BR",
    "ru": "ru-RU",
    "ur": "ur",
    "id": "id",
    "de": "de-DE",
    "ja": "ja-JP",
    "sw": "sw",
    "mr": "mr-IN",
    "te": "te-IN",
    "ta": "ta-IN",
    "vi": "vi",
    "ko": "ko-KR",
}

PLAY_LOCALES = [code for code in (APP_TO_PLAY[t] for t in APP_LOCALES) if code]

STORE_LISTINGS = {
    "tr": {
        # title: 13 chars | short: 73 chars
        "title": "Türkçe Sözlük",
        "short_description": (
            "25 dilde! Eş anlamlı, fiil, deyim ve quizlerle kelime dağarcığını geliştir!"
        ),
        "full_description": (
            "📚 Türkçe Sözlük — kapsamlı Türkçe kelime dağarcığı uygulaması! "
            "Öğrenciler, yazarlar ve Türkçe öğrenenler için. 25 dilde arayüz.\n\n"
            "✨ ÖZELLİKLER\n\n"
            "🔍 Çoklu sözlük modları: eş anlamlılar, fiiller, isimler, deyimler, "
            "sıfatlar ve karma mod\n\n"
            "🔎 Akıllı arama, öneriler, sesli arama ve TDK Güncel Türkçe Sözlük "
            "online sorgulama\n\n"
            "⭐ Favoriler ve öğrenme kartları\n\n"
            "➕ Kendi kelimelerinizi ekleyin\n\n"
            "🎯 Quiz modu ve istatistikler\n\n"
            "📅 Günün kelimesi — isteğe bağlı bildirim\n\n"
            "🌍 25 dilde arayüz; Türk dilleri dahil\n\n"
            "🎨 Modern arayüz: yan menü, sağ üstte açık/koyu tema\n\n"
            "📊 Profilde istatistikler ve seri takibi\n\n"
            "🔒 Gizlilik: kişisel veri yok, uygulama içi gizlilik politikası "
            "25 dilde, yalnızca yerel depolama\n\n"
            "💯 Tamamen ücretsiz, reklamsız\n\n"
            "Hemen indirin ve kelime haznenizi geliştirin! 🚀"
        ),
    },
    "en": {
        # title: 18 chars | short: 74 chars
        "title": "Turkish Dictionary",
        "short_description": (
            "In 25 languages! Build vocabulary with synonyms, verbs, idioms & quizzes!"
        ),
        "full_description": (
            "📚 Turkish Dictionary — a comprehensive Turkish vocabulary app for "
            "students, writers and Turkish learners. Interface available in 25 languages.\n\n"
            "✨ FEATURES\n\n"
            "🔍 Multiple dictionary modes: synonyms, verbs, nouns, idioms, "
            "adjectives and mixed mode\n\n"
            "🔎 Smart search with suggestions, voice search and TDK Güncel Türkçe "
            "Sözlük online lookup\n\n"
            "⭐ Favorites and learning flashcards\n\n"
            "➕ Add your own words\n\n"
            "🎯 Quiz mode with statistics\n\n"
            "📅 Word of the day with optional notification\n\n"
            "🌍 UI in 25 languages, including Turkic languages\n\n"
            "🎨 Modern UI: side menu navigation, light/dark theme toggle top-right\n\n"
            "📊 Stats and streak tracking on your profile\n\n"
            "🔒 Privacy: no personal data collected, in-app privacy policy in "
            "25 languages, local storage only\n\n"
            "💯 Completely free, no ads\n\n"
            "Download now and expand your vocabulary! 🚀"
        ),
    },
    "de": {
        # title: 21 chars | short: 79 chars
        "title": "Türkisches Wörterbuch",
        "short_description": (
            "In 25 Sprachen! Wortschatz mit Synonymen, Verben, Redewendungen & Quiz!"
        ),
        "full_description": (
            "📚 Türkisches Wörterbuch — umfassende Türkisch-Vokabel-App für "
            "Schüler, Autoren und Türkischlernende. Oberfläche in 25 Sprachen.\n\n"
            "✨ FUNKTIONEN\n\n"
            "🔍 Mehrere Wörterbuchmodi: Synonyme, Verben, Substantive, Redewendungen, "
            "Adjektive und Mischmodus\n\n"
            "🔎 Intelligente Suche mit Vorschlägen, Sprachsuche und TDK Güncel "
            "Türkçe Sözlük Online-Abfrage\n\n"
            "⭐ Favoriten und Lernkarten\n\n"
            "➕ Eigene Wörter hinzufügen\n\n"
            "🎯 Quiz-Modus mit Statistiken\n\n"
            "📅 Wort des Tages mit optionaler Benachrichtigung\n\n"
            "🌍 Oberfläche in 25 Sprachen, einschließlich turkischer Sprachen\n\n"
            "🎨 Moderne Oberfläche: Seitenmenü, Hell/Dunkel-Umschalter oben rechts\n\n"
            "📊 Statistiken und Serien im Profil\n\n"
            "🔒 Datenschutz: keine personenbezogenen Daten, In-App-Datenschutzrichtlinie "
            "in 25 Sprachen, nur lokale Speicherung\n\n"
            "💯 Völlig kostenlos, ohne Werbung\n\n"
            "Jetzt herunterladen und Wortschatz erweitern! 🚀"
        ),
    },
    "az": {
        # title: 13 chars | short: 78 chars
        "title": "Türkcə Lüğət",
        "short_description": (
            "25 dildə! Sinonim, feil, deyim və viktorinalarla lüğətinizi genişləndirin!"
        ),
        "full_description": (
            "📚 Türkcə Lüğət — tələbələr, yazarlar və Türk dili öyrənənlər üçün "
            "hərtərəfli lüğət tətbiqi. 25 dildə interfeys.\n\n"
            "✨ XÜSUSİYYƏTLƏR\n\n"
            "🔍 Çoxlu lüğət rejimləri: sinonimlər, feillər, isimlər, deyimlər, "
            "sifətlər və qarışıq rejim\n\n"
            "🔎 Ağıllı axtarış, təkliflər, səsli axtarış və TDK Güncel Türkçe "
            "Sözlük onlayn sorğusu\n\n"
            "⭐ Seçilmişlər və öyrənmə kartları\n\n"
            "➕ Öz sözlərinizi əlavə edin\n\n"
            "🎯 Viktorina rejimi və statistika\n\n"
            "📅 Günün sözü — ixtiyari bildiriş\n\n"
            "🌍 25 dildə interfeys; türk dilləri daxil\n\n"
            "🎨 Müasir interfeys: yan menyu, sağ yuxarıda açıq/tünd tema\n\n"
            "📊 Profildə statistika və seriya izləmə\n\n"
            "🔒 Məxfilik: şəxsi məlumat yoxdur, tətbiqdaxili məxfilik siyasəti "
            "25 dildə, yalnız yerli saxlama\n\n"
            "💯 Tamamilə pulsuz, reklamsız\n\n"
            "İndi endirin və lüğətinizi genişləndirin! 🚀"
        ),
    },
    "kk": {
        # title: 20 chars | short: 78 chars
        "title": "Түрік тілінің сөздігі",
        "short_description": (
            "25 тілде! Синонимдер, етістіктер, мақал-мәтелдер мен викторина арқылы!"
        ),
        "full_description": (
            "📚 Түрік тілінің сөздігі — студенттер, жазушылар және түрік тілін "
            "үйренушілерге арналған кешенді сөздік қосымшасы. 25 тілде интерфейс.\n\n"
            "✨ МҮМКІНДІКТЕР\n\n"
            "🔍 Бірнеше сөздік режимі: синонимдер, етістіктер, зат есімдер, "
            "мақал-мәтелдер, сын есімдер және аралас режим\n\n"
            "🔎 Ақылды іздеу, ұсыныстар, дауыс арқылы іздеу және TDK Güncel "
            "Türkçe Sözlük онлайн сұрау\n\n"
            "⭐ Таңдаулылар және оқу карточкалары\n\n"
            "➕ Өз сөздеріңізді қосыңыз\n\n"
            "🎯 Викторина режимі және статистика\n\n"
            "📅 Күннің сөзі — міндетті емес хабарландыру\n\n"
            "🌍 25 тілде интерфейс; тürk тілдері қоса\n\n"
            "🎨 Заманауи интерфейс: бүйір меню, оң жоғарыда ашық/қараңғы тема\n\n"
            "📊 Профильде статистика және серия бақылау\n\n"
            "🔒 Құпиялылық: жеке деректер жиналмайды, қосымшадағы саясат "
            "25 тілде, тек жергілікті сақтау\n\n"
            "💯 Толығымен тегін, жарнамасыз\n\n"
            "Қазір жүктеп, сөз қорыңызды кеңейтіңіз! 🚀"
        ),
    },
    "ky": {
        # title: 13 chars | short: 67 chars
        "title": "Түркчө Сөздүк",
        "short_description": (
            "25 тилде! Синонимдер, этиштер, макал-лакаптар жана викторина менен!"
        ),
        "full_description": (
            "📚 Түркчө Сөздүк — студенттер, жазуучулар жана түрк тилин "
            "үйрөнүүчүлөр үчүн кеңири сөздүк тиркемеси. 25 тилде интерфейс.\n\n"
            "✨ МҮМКҮНЧҮЛҮКТӨР\n\n"
            "🔍 Бир нече сөздүк режими: синонимдер, этиштер, зат атоочтор, "
            "макал-лакаптар, сын ат атоочтор жана аралаш режим\n\n"
            "🔎 Акылдуу издөө, сунуштар, үн менен издөө жана TDK Güncel "
            "Türkçe Sözlük онлайн суроо\n\n"
            "⭐ Тандалгандар жана окуу карталары\n\n"
            "➕ Өз сөздөрүңүздү кошуңуз\n\n"
            "🎯 Викторина режими жана статистика\n\n"
            "📅 Күнүн сөзү — милдеттүү эмес эскертме\n\n"
            "🌍 25 тилде интерфейс; тürk тилдери кошумча\n\n"
            "🎨 Заманбап интерфейс: бок меню, оң жогоруда ачык/караңгы тема\n\n"
            "📊 Профилде статистика жана серия көзөмөл\n\n"
            "🔒 Купуялык: жеке маалымат жок, тиркемедеги саясат "
            "25 тилде, жергиликтүү сактоо гана\n\n"
            "💯 Акысыз, жарнамасыз\n\n"
            "Азыр жүктөп, сөз байлыгыңызды кеңейттиңиз! 🚀"
        ),
    },
    "uz": {
        # title: 13 chars | short: 77 chars | app-only (no Play store listing)
        "title": "Turkcha lug'at",
        "short_description": (
            "25 til! Sinonimlar, fe'llar, maqollar va viktorina bilan lug'atingizni!"
        ),
        "full_description": (
            "📚 Turkcha lug'at — talabalar, yozuvchilar va turk tilini "
            "o'rganuvchilar uchun keng qamrovli lug'at ilovasi. 25 til interfeysi.\n\n"
            "✨ IMKONIYATLAR\n\n"
            "🔍 Ko'p rejimli lug'at: sinonimlar, fe'llar, otlar, maqollar, "
            "sifatlar va aralash rejim\n\n"
            "🔎 Aqlli qidiruv, takliflar, ovozli qidiruv va TDK Güncel "
            "Türkçe Sözlük onlayn so'rov\n\n"
            "⭐ Tanlanganlar va o'rganish kartochkalari\n\n"
            "➕ O'z so'zlaringizni qo'shing\n\n"
            "🎯 Viktorina rejimi va statistika\n\n"
            "📅 Kun so'zi — ixtiyoriy bildirishnoma\n\n"
            "🌍 25 til interfeysi; turk tillari ham bor\n\n"
            "🎨 Zamonaviy interfeys: yon menyu, o'ng yuqorida yorug'/qorong'u mavzu\n\n"
            "📊 Profilda statistika va seriya kuzatuvi\n\n"
            "🔒 Maxfiylik: shaxsiy ma'lumot yo'q, ilova ichidagi siyosat "
            "25 til, faqat mahalliy saqlash\n\n"
            "💯 To'liq bepul, reklamasiz\n\n"
            "Hozir yuklab oling va lug'atingizni kengaytiring! 🚀"
        ),
    },
    "tk": {
        # title: 13 chars | short: 76 chars | app-only (no Play store listing)
        "title": "Türkçe Sözlük",
        "short_description": (
            "25 dilde! Sinonim, feil, deyim we wiktorina bilen sözlügiňizi giňeldiň!"
        ),
        "full_description": (
            "📚 Türkçe Sözlük — okuwçylar, ýazyjy we türk dilini öwrenýänler "
            "üçin giň sözlük programmasy. 25 dilde interfeýs.\n\n"
            "✨ AÝRATYNLYKLAR\n\n"
            "🔍 Köp rejimli sözlük: sinonimler, feiller, atlar, deyimler, "
            "sypatlar we garyşyk rejim\n\n"
            "🔎 Akylly gözleg, teklipler, sesli gözleg we TDK Güncel "
            "Türkçe Sözlük onlaýn sorag\n\n"
            "⭐ Saýlananlar we öwreniş kartlary\n\n"
            "➕ Öz sözleriňizi goşuň\n\n"
            "🎯 Wiktorina we statistika\n\n"
            "📅 Günüň sözi — islege bagly habarnama\n\n"
            "🌍 25 dilde interfeýs; türk dilleri hem bar\n\n"
            "🎨 Täze interfeýs: gapdal menýu, sag ýokarda açyk/garaňky tema\n\n"
            "📊 Profilde statistika we yzygiderlilik\n\n"
            "🔒 Gizlinlik: şahsy maglumat ýok, programma içindäki syýasat "
            "25 dilde, diňe ýerli saklama\n\n"
            "💯 Doly mugt, reklamsyz\n\n"
            "Häzir göçürip alyň we sözlügiňizi giňeldiň! 🚀"
        ),
    },
    "ru": {
        # title: 17 chars | short: 78 chars
        "title": "Турецкий словарь",
        "short_description": (
            "На 25 языках! Синонимы, глаголы, идиомы и викторины для словарного запаса!"
        ),
        "full_description": (
            "📚 Турецкий словарь — полноценное приложение для турецкого словарного "
            "запаса для учеников, писателей и изучающих турецкий. Интерфейс на 25 языках.\n\n"
            "✨ ВОЗМОЖНОСТИ\n\n"
            "🔍 Несколько режимов словаря: синонимы, глаголы, существительные, "
            "идиомы, прилагательные и смешанный режим\n\n"
            "🔎 Умный поиск с подсказками, голосовой поиск и онлайн-запрос "
            "TDK Güncel Türkçe Sözlük\n\n"
            "⭐ Избранное и обучающие карточки\n\n"
            "➕ Добавляйте свои слова\n\n"
            "🎯 Режим викторины со статистикой\n\n"
            "📅 Слово дня с необязательным уведомлением\n\n"
            "🌍 Интерфейс на 25 языках, включая тюркские\n\n"
            "🎨 Современный интерфейс: боковое меню, переключатель темы справа сверху\n\n"
            "📊 Статистика и серия в профиле\n\n"
            "🔒 Конфиденциальность: без личных данных, политика в приложении "
            "на 25 языках, только локальное хранение\n\n"
            "💯 Полностью бесплатно, без рекламы\n\n"
            "Скачайте сейчас и расширяйте словарный запас! 🚀"
        ),
    },
    "ar": {
        # title: 14 chars | short: 68 chars
        "title": "قاموس التركية",
        "short_description": (
            "بـ25 لغة! عزّز مفرداتك بالمرادفات والأفعال والاصطلاحات والاختبارات!"
        ),
        "full_description": (
            "📚 قاموس التركية — تطبيق شامل لمفردات اللغة التركية للطلاب "
            "والكتاب ومتعلمي التركية. واجهة بـ25 لغة.\n\n"
            "✨ الميزات\n\n"
            "🔍 أوضاع قاموس متعددة: مرادفات، أفعال، أسماء، اصطلاحات، "
            "صفات ووضع مختلط\n\n"
            "🔎 بحث ذكي مع اقتراحات، بحث صوتي واستعلام عبر TDK Güncel "
            "Türkçe Sözlük على الإنترنت\n\n"
            "⭐ المفضلة وبطاقات التعلم\n\n"
            "➕ أضف كلماتك الخاصة\n\n"
            "🎯 وضع الاختبار مع إحصائيات\n\n"
            "📅 كلمة اليوم مع إشعار اختياري\n\n"
            "🌍 واجهة بـ25 لغة، بما فيها اللغات التركية\n\n"
            "🎨 واجهة عصرية: قائمة جانبية، تبديل الوضع الفاتح/الداكن أعلى اليمين\n\n"
            "📊 إحصائيات وسلسلة في الملف الشخصي\n\n"
            "🔒 الخصوصية: لا بيانات شخصية، سياسة خصوصية داخل التطبيق "
            "بـ25 لغة، تخزين محلي فقط\n\n"
            "💯 مجاني بالكامل، بدون إعلانات\n\n"
            "حمّل الآن ووسّع مفرداتك! 🚀"
        ),
    },
    "fr": {
        # title: 18 chars | short: 79 chars
        "title": "Dictionnaire turc",
        "short_description": (
            "En 25 langues ! Enrichissez votre vocabulaire : synonymes, verbes, quiz !"
        ),
        "full_description": (
            "📚 Dictionnaire turc — application complète de vocabulaire turc pour "
            "étudiants, écrivains et apprenants. Interface en 25 langues.\n\n"
            "✨ FONCTIONNALITÉS\n\n"
            "🔍 Modes dictionnaire multiples : synonymes, verbes, noms, expressions, "
            "adjectifs et mode mixte\n\n"
            "🔎 Recherche intelligente avec suggestions, recherche vocale et "
            "consultation en ligne TDK Güncel Türkçe Sözlük\n\n"
            "⭐ Favoris et cartes d'apprentissage\n\n"
            "➕ Ajoutez vos propres mots\n\n"
            "🎯 Mode quiz avec statistiques\n\n"
            "📅 Mot du jour avec notification optionnelle\n\n"
            "🌍 Interface en 25 langues, y compris les langues turques\n\n"
            "🎨 Interface moderne : menu latéral, thème clair/sombre en haut à droite\n\n"
            "📊 Statistiques et série dans le profil\n\n"
            "🔒 Confidentialité : aucune donnée personnelle, politique intégrée "
            "en 25 langues, stockage local uniquement\n\n"
            "💯 Entièrement gratuit, sans publicité\n\n"
            "Téléchargez maintenant et enrichissez votre vocabulaire ! 🚀"
        ),
    },
    "es": {
        # title: 17 chars | short: 78 chars
        "title": "Diccionario turco",
        "short_description": (
            "¡En 25 idiomas! Mejora vocabulario con sinónimos, verbos, modismos y quiz!"
        ),
        "full_description": (
            "📚 Diccionario turco — aplicación integral de vocabulario turco para "
            "estudiantes, escritores y aprendices. Interfaz en 25 idiomas.\n\n"
            "✨ CARACTERÍSTICAS\n\n"
            "🔍 Varios modos de diccionario: sinónimos, verbos, sustantivos, "
            "modismos, adjetivos y modo mixto\n\n"
            "🔎 Búsqueda inteligente con sugerencias, búsqueda por voz y "
            "consulta en línea TDK Güncel Türkçe Sözlük\n\n"
            "⭐ Favoritos y tarjetas de aprendizaje\n\n"
            "➕ Añade tus propias palabras\n\n"
            "🎯 Modo quiz con estadísticas\n\n"
            "📅 Palabra del día con notificación opcional\n\n"
            "🌍 Interfaz en 25 idiomas, incluidas lenguas túrquicas\n\n"
            "🎨 Interfaz moderna: menú lateral, tema claro/oscuro arriba a la derecha\n\n"
            "📊 Estadísticas y racha en el perfil\n\n"
            "🔒 Privacidad: sin datos personales, política en la app "
            "en 25 idiomas, solo almacenamiento local\n\n"
            "💯 Totalmente gratis, sin anuncios\n\n"
            "¡Descarga ahora y amplía tu vocabulario! 🚀"
        ),
    },
    "pt": {
        # title: 17 chars | short: 77 chars
        "title": "Dicionário turco",
        "short_description": (
            "Vocabulário turco em 25 idiomas: sinônimos, verbos, expressões e quiz"
        ),
        "full_description": (
            "📚 Dicionário turco — app completo de vocabulário turco para "
            "estudantes, escritores e aprendizes. Interface em 25 idiomas.\n\n"
            "✨ RECURSOS\n\n"
            "🔍 Vários modos de dicionário: sinônimos, verbos, substantivos, "
            "expressões, adjetivos e modo misto\n\n"
            "🔎 Busca inteligente com sugestões, busca por voz e consulta "
            "online TDK Güncel Türkçe Sözlük\n\n"
            "⭐ Favoritos e flashcards de aprendizado\n\n"
            "➕ Adicione suas próprias palavras\n\n"
            "🎯 Modo quiz com estatísticas\n\n"
            "📅 Palavra do dia com notificação opcional\n\n"
            "🌍 Interface em 25 idiomas, incluindo línguas turcas\n\n"
            "🎨 UI moderna: menu lateral, tema claro/escuro no canto superior direito\n\n"
            "📊 Estatísticas e sequência no perfil\n\n"
            "🔒 Privacidade: sem dados pessoais, política no app "
            "em 25 idiomas, apenas armazenamento local\n\n"
            "💯 Totalmente grátis, sem anúncios\n\n"
            "Baixe agora e amplie seu vocabulário! 🚀"
        ),
    },
    "hi": {
        # title: 14 chars | short: 72 chars
        "title": "तुर्की शब्दकोश",
        "short_description": (
            "25 भाषाओं में! पर्याय, क्रिया, मुहावरे और क्विज़ से शब्दावली बढ़ाएँ!"
        ),
        "full_description": (
            "📚 तुर्की शब्दकोश — छात्रों, लेखकों और तुर्की सीखने वालों के लिए "
            "व्यापक शब्दावली ऐप। 25 भाषाओं में इंटरफ़ेस।\n\n"
            "✨ विशेषताएँ\n\n"
            "🔍 कई शब्दकोश मोड: पर्याय, क्रियाएँ, संज्ञाएँ, मुहावरे, "
            "विशेषण और मिश्रित मोड\n\n"
            "🔎 स्मार्ट खोज, सुझाव, वॉइस खोज और TDK Güncel Türkçe "
            "Sözlük ऑनलाइन लुकअप\n\n"
            "⭐ पसंदीदा और सीखने के फ़्लैशकार्ड\n\n"
            "➕ अपने शब्द जोड़ें\n\n"
            "🎯 क्विज़ मोड और आँकड़े\n\n"
            "📅 दिन का शब्द — वैकल्पिक सूचना\n\n"
            "🌍 25 भाषाओं में UI; तुर्क भाषाएँ सहित\n\n"
            "🎨 आधुनिक UI: साइड मेनू, ऊपर दाएँ लाइट/डार्क थीम\n\n"
            "📊 प्रोफ़ाइल में आँकड़े और स्ट्रीक\n\n"
            "🔒 गोपनीयता: कोई व्यक्तिगत डेटा नहीं, ऐप में नीति "
            "25 भाषाओं में, केवल स्थानीय संग्रहण\n\n"
            "💯 पूरी तरह मुफ़्त, बिना विज्ञापन\n\n"
            "अभी डाउनलोड करें और शब्दावली बढ़ाएँ! 🚀"
        ),
    },
    "zh-CN": {
        # title: 7 chars | short: 35 chars
        "title": "土耳其语词典",
        "short_description": (
            "25种语言！同义词、动词、习语和测验，全面提升土耳其语词汇量！"
        ),
        "full_description": (
            "📚 土耳其语词典 — 面向学生、写作者和土耳其语学习者的全面词汇应用。"
            "界面支持25种语言。\n\n"
            "✨ 功能\n\n"
            "🔍 多种词典模式：同义词、动词、名词、习语、形容词和混合模式\n\n"
            "🔎 智能搜索、建议、语音搜索及 TDK Güncel Türkçe Sözlük 在线查询\n\n"
            "⭐ 收藏与学习闪卡\n\n"
            "➕ 添加自定义词汇\n\n"
            "🎯 测验模式与统计数据\n\n"
            "📅 每日一词，可选通知\n\n"
            "🌍 25种语言界面，含突厥语族语言\n\n"
            "🎨 现代界面：侧边菜单，右上角浅色/深色主题切换\n\n"
            "📊 个人资料中的统计与连续记录\n\n"
            "🔒 隐私：不收集个人数据，应用内隐私政策25种语言，仅本地存储\n\n"
            "💯 完全免费，无广告\n\n"
            "立即下载，扩展您的词汇量！🚀"
        ),
    },
    "ja": {
        # title: 8 chars | short: 42 chars
        "title": "トルコ語辞典",
        "short_description": (
            "25言語対応！類語・動詞・慣用句・クイズでトルコ語語彙を強化！"
        ),
        "full_description": (
            "📚 トルコ語辞典 — 学生、作家、トルコ語学習者向けの包括的語彙アプリ。"
            "25言語のUI。\n\n"
            "✨ 機能\n\n"
            "🔍 複数の辞書モード：類語、動詞、名詞、慣用句、形容詞、混合モード\n\n"
            "🔎 スマート検索、候補、音声検索、TDK Güncel Türkçe Sözlük オンライン検索\n\n"
            "⭐ お気に入りと学習フラッシュカード\n\n"
            "➕ 独自の単語を追加\n\n"
            "🎯 クイズモードと統計\n\n"
            "📅 今日の単語（通知は任意）\n\n"
            "🌍 25言語UI、テュルク語族を含む\n\n"
            "🎨 モダンUI：サイドメニュー、右上でライト/ダークテーマ切替\n\n"
            "📊 プロフィールの統計と連続記録\n\n"
            "🔒 プライバシー：個人データなし、アプリ内プライバシーポリシー25言語、ローカルのみ\n\n"
            "💯 完全無料、広告なし\n\n"
            "今すぐダウンロードして語彙を広げましょう！🚀"
        ),
    },
    "ko": {
        # title: 7 chars | short: 40 chars
        "title": "터키어 사전",
        "short_description": (
            "25개 언어! 동의어, 동사, 관용구, 퀴즈로 터키어 어휘력을 키우세요!"
        ),
        "full_description": (
            "📚 터키어 사전 — 학생, 작가, 터키어 학습자를 위한 종합 어휘 앱. "
            "25개 언어 UI.\n\n"
            "✨ 기능\n\n"
            "🔍 다중 사전 모드: 동의어, 동사, 명사, 관용구, 형용사, 혼합 모드\n\n"
            "🔎 스마트 검색, 제안, 음성 검색, TDK Güncel Türkçe Sözlük 온라인 조회\n\n"
            "⭐ 즐겨찾기 및 학습 플래시카드\n\n"
            "➕ 나만의 단어 추가\n\n"
            "🎯 퀴즈 모드 및 통계\n\n"
            "📅 오늘의 단어 — 선택적 알림\n\n"
            "🌍 25개 언어 UI, 투르크어족 포함\n\n"
            "🎨 현대적 UI: 사이드 메뉴, 우측 상단 라이트/다크 테마\n\n"
            "📊 프로필 통계 및 연속 기록\n\n"
            "🔒 개인정보: 개인 데이터 없음, 앱 내 정책 25개 언어, 로컬 저장만\n\n"
            "💯 완전 무료, 광고 없음\n\n"
            "지금 다운로드하고 어휘를 넓히세요! 🚀"
        ),
    },
    "bn": {
        # title: 14 chars | short: 72 chars
        "title": "তুর্কি অভিধান",
        "short_description": (
            "২৫টি ভাষায়! প্রতিশব্দ, ক্রিয়া, প্রবাদ ও কুইজে শব্দভান্ডার বাড়ান!"
        ),
        "full_description": (
            "📚 তুর্কি অভিধান — শিক্ষার্থী, লেখক ও তুর্কি ভাষা শিখছেন তাদের "
            "জন্য সম্পূর্ণ শব্দভান্ডার অ্যাপ। ২৫টি ভাষায় ইন্টারফেস।\n\n"
            "✨ বৈশিষ্ট্য\n\n"
            "🔍 একাধিক অভিধান মোড: প্রতিশব্দ, ক্রিয়া, বিশেষ্য, প্রবাদ, "
            "বিশেষণ ও মিশ্র মোড\n\n"
            "🔎 স্মার্ট অনুসন্ধান, পরামর্শ, ভয়েস সার্চ ও TDK Güncel "
            "Türkçe Sözlük অনলাইন খোঁজ\n\n"
            "⭐ পছন্দের তালিকা ও শেখার ফ্ল্যাশকার্ড\n\n"
            "➕ নিজের শব্দ যোগ করুন\n\n"
            "🎯 কুইজ মোড ও পরিসংখ্যান\n\n"
            "📅 দিনের শব্দ — ঐচ্ছিক বিজ্ঞপ্তি\n\n"
            "🌍 ২৫টি ভাষায় UI; তুর্কি ভাষাসহ\n\n"
            "🎨 আধুনিক UI: সাইড মেনু, উপরে ডানে লাইট/ডার্ক থিম\n\n"
            "📊 প্রোফাইলে পরিসংখ্যান ও ধারাবাহিকতা\n\n"
            "🔒 গোপনীয়তা: কোনো ব্যক্তিগত তথ্য নেই, অ্যাপে নীতি "
            "২৫টি ভাষায়, শুধু স্থানীয় সংরক্ষণ\n\n"
            "💯 সম্পূর্ণ বিনামূল্যে, বিজ্ঞাপন নেই\n\n"
            "এখনই ডাউনলোড করুন ও শব্দভান্ডার বাড়ান! 🚀"
        ),
    },
    "id": {
        # title: 11 chars | short: 76 chars
        "title": "Kamus Turki",
        "short_description": (
            "25 bahasa! Perkaya kosakata dengan sinonim, kata kerja, idiom & kuis!"
        ),
        "full_description": (
            "📚 Kamus Turki — aplikasi kosakata Turki lengkap untuk "
            "pelajar, penulis dan pembelajar bahasa Turki. Antarmuka 25 bahasa.\n\n"
            "✨ FITUR\n\n"
            "🔍 Beberapa mode kamus: sinonim, kata kerja, kata benda, idiom, "
            "kata sifat dan mode campuran\n\n"
            "🔎 Pencarian cerdas dengan saran, pencarian suara dan "
            "lookup online TDK Güncel Türkçe Sözlük\n\n"
            "⭐ Favorit dan kartu flash belajar\n\n"
            "➕ Tambahkan kata Anda sendiri\n\n"
            "🎯 Mode kuis dengan statistik\n\n"
            "📅 Kata hari ini dengan notifikasi opsional\n\n"
            "🌍 UI 25 bahasa, termasuk bahasa Turkik\n\n"
            "🎨 UI modern: menu samping, tema terang/gelap kanan atas\n\n"
            "📊 Statistik dan streak di profil\n\n"
            "🔒 Privasi: tanpa data pribadi, kebijakan dalam app "
            "25 bahasa, penyimpanan lokal saja\n\n"
            "💯 Gratis sepenuhnya, tanpa iklan\n\n"
            "Unduh sekarang dan perluas kosakata Anda! 🚀"
        ),
    },
    "ur": {
        # title: 9 chars | short: 68 chars
        "title": "ترکی لغت",
        "short_description": (
            "25 زبانوں میں! مترادفات، افعال، محاورے اور کوئز سے الفاظ بڑھائیں!"
        ),
        "full_description": (
            "📚 ترکی لغت — طلبہ، مصنفین اور ترکی سیکھنے والوں کے لیے "
            "جامع الفاظ کی ایپ۔ 25 زبانوں میں انٹرفیس۔\n\n"
            "✨ خصوصیات\n\n"
            "🔍 متعدد لغت موڈ: مترادفات، افعال، اسم، محاورے، "
            "صفتیں اور مخلوط موڈ\n\n"
            "🔎 ذكي تلاش، تجاویز، آواز سے تلاش اور TDK Güncel "
            "Türkçe Sözlük آن لائن تلاش\n\n"
            "⭐ پسندیدہ اور سیکھنے کے فلیش کارڈ\n\n"
            "➕ اپنے الفاظ شامل کریں\n\n"
            "🎯 کوئز موڈ اور اعداد و شمار\n\n"
            "📅 دن کا لفظ — اختیاری اطلاع\n\n"
            "🌍 25 زبانوں میں UI؛ ترکی زبانیں شامل\n\n"
            "🎨 جدید UI: سائیڈ مینو، اوپر دائیں لائٹ/ڈارک تھیم\n\n"
            "📊 پروفائل میں اعداد و شمار اور سلسلہ\n\n"
            "🔒 رازداری: کوئی ذاتی ڈیٹا نہیں، ایپ میں پالیسی "
            "25 زبانوں میں، صرف مقامی ذخیرہ\n\n"
            "💯 مکمل مفت، بغیر اشتہار\n\n"
            "ابھی ڈاؤن لوڈ کریں اور الفاظ بڑھائیں! 🚀"
        ),
    },
    "vi": {
        # title: 24 chars | short: 76 chars
        "title": "Từ điển tiếng Thổ Nhĩ Kỳ",
        "short_description": (
            "25 ngôn ngữ! Mở rộng từ vựng với từ đồng nghĩa, động từ, thành ngữ & quiz!"
        ),
        "full_description": (
            "📚 Từ điển tiếng Thổ Nhĩ Kỳ — ứng dụng từ vựng tiếng Thổ toàn diện "
            "cho học sinh, nhà văn và người học. Giao diện 25 ngôn ngữ.\n\n"
            "✨ TÍNH NĂNG\n\n"
            "🔍 Nhiều chế độ từ điển: từ đồng nghĩa, động từ, danh từ, thành ngữ, "
            "tính từ và chế độ hỗn hợp\n\n"
            "🔎 Tìm kiếm thông minh, gợi ý, tìm bằng giọng nói và tra cứu "
            "TDK Güncel Türkçe Sözlük trực tuyến\n\n"
            "⭐ Yêu thích và thẻ học flashcard\n\n"
            "➕ Thêm từ của riêng bạn\n\n"
            "🎯 Chế độ quiz với thống kê\n\n"
            "📅 Từ trong ngày — thông báo tùy chọn\n\n"
            "🌍 Giao diện 25 ngôn ngữ, gồm các ngôn ngữ Turk\n\n"
            "🎨 Giao diện hiện đại: menu bên, chuyển sáng/tối góc trên phải\n\n"
            "📊 Thống kê và chuỗi ngày trên hồ sơ\n\n"
            "🔒 Quyền riêng tư: không thu thập dữ liệu cá nhân, chính sách trong app "
            "25 ngôn ngữ, chỉ lưu trữ cục bộ\n\n"
            "💯 Hoàn toàn miễn phí, không quảng cáo\n\n"
            "Tải ngay và mở rộng vốn từ! 🚀"
        ),
    },
    "ta": {
        # title: 16 chars | short: 68 chars
        "title": "துருக்கிய அகராதி",
        "short_description": (
            "25 மொழிகளில்! ஒத்த சொற்கள், வினை, மரபுத்தொடர்கள், வினாடி வினாவுடன்!"
        ),
        "full_description": (
            "📚 துருக்கிய அகராதி — மாணவர்கள், எழுத்தாளர்கள் மற்றும் துருக்கியம் "
            "கற்பவர்களுக்கான விரிவான சொல்லகராதி செயலி. 25 மொழி UI.\n\n"
            "✨ அம்சங்கள்\n\n"
            "🔍 பல அகராதி முறைகள்: ஒத்த சொற்கள், வினை, பெயர், மரபுத்தொடர்கள், "
            "பெயரடை மற்றும் கலப்பு முறை\n\n"
            "🔎 ஸ்மார்ட் தேடல், பரிந்துரைகள், குரல் தேடல் மற்றும் TDK Güncel "
            "Türkçe Sözlük ஆன்லைன் தேடல்\n\n"
            "⭐ பிடித்தவை மற்றும் கற்றல் ஃபிளாஷ் கார்டுகள்\n\n"
            "➕ உங்கள் சொற்களைச் சேர்க்கவும்\n\n"
            "🎯 வினாடி வினா முறை மற்றும் புள்ளிவிவரங்கள்\n\n"
            "📅 இன்றைய சொல் — விருப்ப அறிவிப்பு\n\n"
            "🌍 25 மொழி UI; துருக்கிய மொழிகள் உட்பட\n\n"
            "🎨 நவீன UI: பக்க மெனு, மேல் வலது லைட்/டார்க் தீம்\n\n"
            "📊 சுயவிவரத்தில் புள்ளிவிவரம் மற்றும் தொடர்\n\n"
            "🔒 தனியுரிமை: தனிப்பட்ட தரவு இல்லை, செயலியில் கொள்கை "
            "25 மொழிகளில், உள்ளூர் சேமிப்பு மட்டும்\n\n"
            "💯 முற்றிலும் இலவசம், விளம்பரம் இல்லை\n\n"
            "இப்போது பதிவிறக்கம் செய்து சொல்லகராதியை விரிவுபடுத்துங்கள்! 🚀"
        ),
    },
    "te": {
        # title: 16 chars | short: 68 chars
        "title": "టర్కిష్ నిఘంటువు",
        "short_description": (
            "25 భాషల్లో! పర్యాయపదాలు, క్రియలు, సామెతలు, క్విజ్‌తో పదజాలం పెంచండి!"
        ),
        "full_description": (
            "📚 టర్కిష్ నిఘంటువు — విద్యార్థులు, రచయితలు మరియు టర్కిష్ "
            "నేర్చుకునేవారికి సమగ్ర పదజాలం యాప్. 25 భాషల UI.\n\n"
            "✨ లక్షణాలు\n\n"
            "🔍 అనేక నిఘంటువు మోడ్‌లు: పర్యాయపదాలు, క్రియలు, నామవాచకాలు, "
            "సామెతలు, విశేషణాలు మరియు మిశ్రమ మోడ్\n\n"
            "🔎 స్మార్ట్ శోధన, సూచనలు, వాయిస్ శోధన మరియు TDK Güncel "
            "Türkçe Sözlük ఆన్‌లైన్ శోధన\n\n"
            "⭐ ఇష్టమైనవి మరియు నేర్చుకోవడానికి ఫ్లాష్ కార్డులు\n\n"
            "➕ మీ పదాలను జోడించండి\n\n"
            "🎯 క్విజ్ మోడ్ మరియు గణాంకాలు\n\n"
            "📅 రోజు పదం — ఐచ్ఛిక నోటిఫికేషన్\n\n"
            "🌍 25 భాషల UI; టర్కిక్ భాషలు సహా\n\n"
            "🎨 ఆధునిక UI: సైడ్ మెనూ, పై కుడి లైట్/డార్క్ థీమ్\n\n"
            "📊 ప్రొఫైల్‌లో గణాంకాలు మరియు స్ట్రీక్\n\n"
            "🔒 గోప్యత: వ్యక్తిగత డేటా లేదు, యాప్‌లో విధానం "
            "25 భాషల్లో, స్థానిక నిల్వ మాత్రమే\n\n"
            "💯 పూర్తిగా ఉచితం, ప్రకటనలు లేవు\n\n"
            "ఇప్పుడే డౌన్‌లోడ్ చేసి పదజాలం పెంచుకోండి! 🚀"
        ),
    },
    "mr": {
        # title: 14 chars | short: 72 chars
        "title": "तुर्की शब्दकोश",
        "short_description": (
            "25 भाषांमध्ये! समानार्थी, क्रियापद, वाक्प्रचार आणि क्विझने शब्दसंग्रह!"
        ),
        "full_description": (
            "📚 तुर्की शब्दकोश — विद्यार्थी, लेखक आणि तुर्की शिकणाऱ्यांसाठी "
            "सर्वसमावेशक शब्दसंग्रह अ‍ॅप. 25 भाषांमध्ये UI.\n\n"
            "✨ वैशिष्ट्ये\n\n"
            "🔍 अनेक शब्दकोश मोड: समानार्थी, क्रियापद, नाम, वाक्प्रचार, "
            "विशेषण आणि मिश्र मोड\n\n"
            "🔎 स्मार्ट शोध, सूचना, व्हॉइस शोध आणि TDK Güncel "
            "Türkçe Sözlük ऑनलाइन शोध\n\n"
            "⭐ आवडते आणि शिकण्याची फ्लॅशकार्ड\n\n"
            "➕ स्वतःची शब्दे जोडा\n\n"
            "🎯 क्विझ मोड आणि आकडेवारी\n\n"
            "📅 दिवसाचे शब्द — पर्यायी सूचना\n\n"
            "🌍 25 भाषांमध्ये UI; तुर्की भाषा समावेश\n\n"
            "🎨 आधुनिक UI: साइड मेनू, वर उजवीकडे लाइट/डार्क थीम\n\n"
            "📊 प्रोफाइलमध्ये आकडेवारी आणि साखळी\n\n"
            "🔒 गोपनीयता: वैयक्तिक डेटा नाही, अ‍ॅपमध्ये धोरण "
            "25 भाषांमध्ये, फक्त स्थानिक साठवण\n\n"
            "💯 पूर्णपणे मोफत, जाहिराती नाहीत\n\n"
            "आत्ताच डाउनलोड करा आणि शब्दसंग्रह वाढवा! 🚀"
        ),
    },
    "sw": {
        # title: 16 chars | short: 72 chars
        "title": "Kamusi ya Kituruki",
        "short_description": (
            "Lugha 25! Ongeza msamiati kwa visawe, vitenzi, methali na maswali!"
        ),
        "full_description": (
            "📚 Kamusi ya Kituruki — programu kamili ya msamiati wa Kituruki "
            "kwa wanafunzi, waandishi na wajifunzaji. Kiolesura cha lugha 25.\n\n"
            "✨ VIPENGELE\n\n"
            "🔍 Hali nyingi za kamusi: visawe, vitenzi, nomino, methali, "
            "vivumishi na hali mchanganyiko\n\n"
            "🔎 Utafutaji mahiri, mapendekezo, utafutaji wa sauti na "
            "TDK Güncel Türkçe Sözlük mtandaoni\n\n"
            "⭐ Vipendwa na kadi za kujifunza\n\n"
            "➕ Ongeza maneno yako mwenyewe\n\n"
            "🎯 Hali ya maswali na takwimu\n\n"
            "📅 Neno la siku — arifa hiari\n\n"
            "🌍 UI ya lugha 25, ikiwa ni lugha za Kituruki\n\n"
            "🎨 UI ya kisasa: menyu ya upande, mandhari angavu/giza juu kulia\n\n"
            "📊 Takwimu na mfululizo kwenye wasifu\n\n"
            "🔒 Faragha: hakuna data ya kibinafsi, sera ndani ya programu "
            "lugha 25, hifadhi ya ndani tu\n\n"
            "💯 Bure kabisa, bila matangazo\n\n"
            "Pakua sasa na panua msamiati wako! 🚀"
        ),
    },
}


def _validate_store_listings() -> None:
    """Raise ValueError if any listing exceeds Play Store limits."""
    assert set(APP_LOCALES) == set(STORE_LISTINGS), (
        f"APP_LOCALES and STORE_LISTINGS keys must match: "
        f"missing={set(APP_LOCALES) - set(STORE_LISTINGS)}, "
        f"extra={set(STORE_LISTINGS) - set(APP_LOCALES)}"
    )
    for locale in APP_LOCALES:
        entry = STORE_LISTINGS[locale]
        for key in ("title", "short_description", "full_description"):
            if key not in entry:
                raise ValueError(f"{locale}: missing {key}")
        title_len = len(entry["title"])
        short_len = len(entry["short_description"])
        full_len = len(entry["full_description"])
        if title_len > 30:
            raise ValueError(
                f"{locale}: title is {title_len} chars (max 30): {entry['title']!r}"
            )
        if short_len > 80:
            raise ValueError(
                f"{locale}: short_description is {short_len} chars (max 80): "
                f"{entry['short_description']!r}"
            )
        if full_len > 4000:
            raise ValueError(
                f"{locale}: full_description is {full_len} chars (max 4000)"
            )


_validate_store_listings()
