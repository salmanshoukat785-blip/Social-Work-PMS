# Test checklist: offline, Urdu/English layout, accessibility

The automatic tests check the project files (no internet permission, no network libraries, texts complete
in both languages). A phone test is still needed for what only a person can judge. Tick each line.

## A. Fully offline
1. Install the APK. Switch on **Airplane mode** and turn Wi-Fi and mobile data off.
2. Open the app. Go through the first-run screens. It must start without any message about internet.
3. Papers -> open a topic in Paper 1 and Paper 2: the English and Urdu notes show.
4. Search a word in English and in Urdu: results appear.
5. Practice -> take a 20-question quiz with the timer on. Finish it. Open the review and "Retry incorrect".
6. Practice -> Mock test -> start one, mark a question, open the question list, submit.
7. Tools -> Glossary, Flashcards, My notes (write a note in a topic, close the app fully, open it again: the note is still there).
8. Tools -> PDF reader -> choose a PDF stored on the phone.
9. Tools -> My progress shows numbers only after you did something.
10. Settings -> save a backup to a file, then restore it.
11. Android Settings -> Apps -> Social Work Academy -> Permissions: the app must not ask for any internet or network permission.

## B. Urdu (right-to-left) and English (left-to-right)
1. Settings -> switch the language to Urdu. Every screen: text starts at the right, back arrows point right,
   bottom tabs run right-to-left, nothing is cut off.
2. Quiz questions written in Urdu are right-to-left; English ones are left-to-right even in an Urdu interface.
3. Switch back to English and repeat a quick look.
4. Rotate the phone on the quiz screen, the notes and the settings: nothing is lost or cut.

## C. Text size and display
1. Android Settings -> Display -> Font size: set it to the largest. Open the quiz, a topic, Settings and
   My progress: all text must stay readable and buttons must not hide their text.
2. Try the dark theme in Settings: text is readable everywhere, the correct answer in a quiz is green with the word "Correct".

## D. TalkBack (screen reader)
1. Switch TalkBack on. On each main screen the title is read as a heading.
2. In a quiz each answer is read as a choice, and after answering it says "Correct" or "Wrong".
3. The language, theme and other switches in Settings are read with their name and on/off state.
4. The weekly chart in My progress reads like "Mon, 5 questions" for each day.
5. All icon buttons (back, bookmark, copy, share, delete) have a spoken name.

Write down anything that fails and send it with a screenshot: which screen, which language, which font size.
