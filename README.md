# Pakistan Laws (Android)

Kotlin + Jetpack Compose + Material 3 app for studying Pakistani criminal and police laws (Punjab).

## Build the APK on GitHub (no computer needed)
1. Create a new GitHub repository and upload all files from this folder (keep the folder structure; the `.github` folder must be at the top level).
2. Open the **Actions** tab, choose **Build APK**, and tap **Run workflow** (it also runs on every push to `main`).
3. When the run finishes, open it and download the **PakistanLaws-debug-apk** artifact. Unzip it and install `app-debug.apk` (allow "install unknown apps").

## Status
- Part 1: project setup, theme (4 colour palettes, light/dark), bottom navigation skeleton, GitHub Actions build.
- Package name: `com.pakistanlaws.app`
- Min Android version: 8.0 (API 26)

## Notes
- `google-services.json` (Firebase) is added in Part 2 and is ignored by git.
- This app is for educational purposes only and is not legal advice.
