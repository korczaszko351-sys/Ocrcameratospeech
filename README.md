# OCR do mowy (Android / Kotlin)

Aplikacja Android, która używa tylnej kamery, rozpoznaje widoczny tekst lokalnie przez Google ML Kit i odczytuje go systemowym TTS po polsku.

## Budowanie tylko z telefonu

1. Utwórz repozytorium GitHub i dodaj do niego wszystkie pliki z tego projektu.
2. Wejdź w **Actions** → **Build Android APK** → **Run workflow**.
3. Po zielonym statusie otwórz ukończone wykonanie.
4. W sekcji **Artifacts** pobierz `OCR-do-mowy-debug-APK`.
5. Wypakuj ZIP i zainstaluj `app-debug.apk` na telefonie.

Workflow celowo pobiera Gradle w chmurze. Nie wymaga pliku `gradlew` ani Android Studio.

## Użycie

- Zezwól aplikacji na dostęp do kamery.
- Skieruj tylną kamerę na czytelny tekst.
- Włączone `Auto` odczyta nowy, stabilny tekst.
- Przycisk `Odczytaj` wypowiada bieżący wynik ręcznie.

## Prywatność

OCR korzysta z modelu dołączonego do aplikacji. Obraz z kamery nie jest wysyłany do własnego serwera.
