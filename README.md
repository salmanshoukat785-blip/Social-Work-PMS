# Islamic Studies Academy (Android)

Kotlin + Jetpack Compose, 100% offline, Urdu + English.

## Part A5 (this version)
- Progress screen (overall, per chapter, study streak, quiz stats)
- Settings (profile, languages, level, text size, theme, PIN, reminder, backup/restore, reset)
- Daily study reminder (local notification), backup/restore to a JSON file of your choice

## Part A4
- Search across topic titles and lesson text
- Quiz: Practice (instant feedback), Timed (30 s per question), Mock test (all chapters, one timer), Review mistakes
- Results with full review of every question, attempt history
- Questions are imported from `app/src/main/assets/questions/*.json` (12 SAMPLE draft questions included)

## Part A3
- Lesson reader: Basic / Intermediate / Advanced, Urdu / English / both, text size, light/dark/system theme
- Bookmarks, personal notes, mark as completed, previous/next lesson, continue where you left off
- Lesson content is imported from `app/src/main/assets/content/*.json` (one SAMPLE draft lesson included: chapter 1 > اسلام معنی و مفہوم)

## Part A2
- Splash screen, onboarding (language, 3 intro pages, name + level, optional 4-digit PIN)
- PIN lock screen (salted + hashed, stored only on the phone)
- Home dashboard (greeting, progress, tiles) and Urdu/English UI switch (Urdu = right-to-left layout)
- Settings stored with DataStore

## Part A1
- Project setup (Gradle, Compose, Material 3, Navigation, Room)
- Theme (navy / emerald / gold / warm white, light + dark)
- Room database (chapters, headings, lesson sections, questions, quiz attempts, progress, bookmarks, notes)
- Import of `app/src/main/assets/master_outline.json` (chapter > sub-heading > topic) into the database
- Temporary screens: chapter list and chapter detail (to check the outline on a real phone)

## Build
- GitHub: upload `app.zip` to the repo root (see `build-apk.yml`); the APK appears under Actions > Artifacts.
- Android Studio: open this folder, let it sync, run `app`.
