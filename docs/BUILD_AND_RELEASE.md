# Building and releasing Social Work Academy

## 1. Everyday use: upload a zip, get an APK

1. Upload `SocialWorkAcademy_PhaseN.zip` to the repository (root folder or `upload/`), then commit.
2. Open the **Actions** tab. The run "Android Build" starts by itself.
3. When it finishes, open the run and scroll to **Artifacts** at the bottom.
   Download **SocialWorkAcademy-debug-apk**, unzip it and install the `.apk` on your phone.
4. The run page also shows a small table (the summary):
   - **Debug APK**: did the build work?
   - **No network permission**: checks that the app asks for no internet access.
   - **Unit tests** and **Lint**: quality checks. If a test fails, the APK is still built and
     kept, but the run turns red at the end. Download **unit-test-report** to see which test failed.
5. After a successful build the workflow unpacks the zip into the repository and deletes the zip.
   Old files from earlier phases are removed first (`app/src`), so nothing stale is left behind.

You can also start a build by hand: Actions -> Android Build -> Run workflow.

If the run is red: open the failed step, and send a screenshot of the red lines.
"Do not claim the build works until the run is green" is the rule, so please check the run itself.

## 2. The debug APK and the release APK

- The **debug APK** is for testing on your own phone. GitHub signs every debug build with a new temporary key,
  so a new debug APK usually **cannot be installed over the previous one** ("App not installed").
  Uninstall the old version first. Uninstalling deletes your notes and progress, so save a backup in
  Settings before you uninstall, and restore it afterwards.
- A **release APK** is signed with **your own key**. Because the key never changes, each new version installs
  over the old one and keeps your data. The same key must be used for every future version.

## 3. Making a signed release APK

You need a key file (a "keystore"). Make it **once** and keep it safe.
If you lose it, you cannot publish updates that install over the old version.

### a) Create the keystore
You need a computer with Java, or the terminal of a GitHub Codespace (it works in a mobile browser too).

    keytool -genkeypair -v -keystore release.jks -alias socialwork -keyalg RSA -keysize 2048 -validity 10000

Choose strong passwords and write down: the keystore password, the alias (`socialwork` above) and the key password.

### b) Turn the file into text
    base64 -w0 release.jks > release.jks.base64.txt

### c) Add four secrets
Repository -> Settings -> Secrets and variables -> Actions -> New repository secret:

| Name | Value |
|---|---|
| KEYSTORE_BASE64 | the whole text inside `release.jks.base64.txt` |
| KEYSTORE_PASSWORD | the keystore password |
| KEY_ALIAS | the alias, for example `socialwork` |
| KEY_PASSWORD | the key password |

Never put the keystore file or the passwords in the repository. (`*.jks` and `*.keystore` are already ignored.)

### d) Run the release build
Actions -> **Release APK (signed)** -> Run workflow. When it is green, download
**SocialWorkAcademy-release-apk** from Artifacts. The workflow also checks the signature.

## 4. Before every new version
Raise `versionCode` by 1 in `app/build.gradle.kts` (and change `versionName`). Android only installs
an update when `versionCode` is higher.
