# EON Browser TV v2

Android TV / Mi TV Stick WebView wrapper sa TV kursorom.

## Kontrole

- D-pad strelice: pomeranje kursora
- Držanje strelice: brže pomeranje
- OK / Enter: klik
- Back: prethodna web stranica / izlaz
- Play/Pause: prosleđuje se web stranici
- HTML5 fullscreen video: podržan kroz WebChromeClient

## Build

1. Raspakuj ZIP.
2. Otvori folder `EonBrowserTV_v2` u Android Studio.
3. Sačekaj Gradle Sync.
4. `Build > Build APK(s)`.
5. APK će tipično biti u:
   `app/build/outputs/apk/debug/app-debug.apk`
6. Instaliraj APK na Mi TV Stick.

## Početni URL

U `MainActivity.kt` promeni:

`private const val HOME_URL = "https://eon.tv"`

ako tvoj EON web servis koristi drugi URL.

## Važno

Aplikacija je standardni browser wrapper. Desktop User-Agent može pomoći
kod sajtova koji očekuju desktop browser, ali ne zaobilazi EON DRM,
autentikaciju, geo-ograničenja, device entitlement ili druga prava pristupa.
Android System WebView na samom uređaju mora podržavati tehnologiju koju
web player zahteva.
