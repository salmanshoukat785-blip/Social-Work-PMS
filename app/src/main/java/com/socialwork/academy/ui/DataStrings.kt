package com.socialwork.academy.ui

/** Texts for the progress dashboard and the data part of Settings (Phase 9). */
data class DataStrings(
    // Progress dashboard
    val progressTitle: String,
    val progressDesc: String,
    val calcNote: String,
    val mcqsAttempted: String,
    val accuracyLabel: String,
    val bestMock: String,
    val bestQuiz: String,
    val notYet: String,
    val paperProgressFormat: String,
    val recentTitle: String,
    val noRecent: String,
    val weakTitle: String,
    val weakHelpFormat: String,
    val noWeak: String,
    val weekTitle: String,
    val weekHelp: String,
    val noWeekActivity: String,
    val weekQuizTimeFormat: String,
    val weekTopicsFormat: String,
    val questionsShortFormat: String,
    // Settings: data
    val dataTitle: String,
    val backupButton: String,
    val backupHelp: String,
    val restoreButton: String,
    val restoreHelp: String,
    val restoreConfirmTitle: String,
    val restoreConfirmBody: String,
    val resetButton: String,
    val resetHelp: String,
    val resetConfirmTitle: String,
    val resetConfirmBody: String,
    val confirm: String,
    val cancel: String,
    val statusBackupSaved: String,
    val statusBackupFailed: String,
    val statusRestoreDone: String,
    val statusRestoreInvalid: String,
    val statusRestoreFailed: String,
    val statusResetDone: String,
    val aboutTitle: String,
    val aboutBody: String,
    val versionFormat: String,
    val privacyTitle: String,
    val privacyBody: String
) {
    private fun fill(t: String, vararg pairs: Pair<String, Any>): String =
        pairs.fold(t) { acc, (k, v) -> acc.replace("{$k}", v.toString()) }

    fun paperProgress(paper: String, done: Int, total: Int) =
        fill(paperProgressFormat, "p" to paper, "done" to done, "total" to total)

    fun weakHelp(minAnswers: Int, belowPercent: Int) =
        fill(weakHelpFormat, "m" to minAnswers, "p" to belowPercent)

    fun weekQuizTime(minutes: Int) = fill(weekQuizTimeFormat, "m" to minutes)
    fun weekTopics(n: Int) = fill(weekTopicsFormat, "n" to n)
    fun questionsShort(n: Int) = fill(questionsShortFormat, "n" to n)
    fun version(v: String) = fill(versionFormat, "v" to v)

    fun all(): List<String> = listOf(
        progressTitle, progressDesc, calcNote, mcqsAttempted, accuracyLabel, bestMock, bestQuiz,
        notYet, paperProgressFormat, recentTitle, noRecent, weakTitle, weakHelpFormat, noWeak,
        weekTitle, weekHelp, noWeekActivity, weekQuizTimeFormat, weekTopicsFormat,
        questionsShortFormat, dataTitle, backupButton, backupHelp, restoreButton, restoreHelp,
        restoreConfirmTitle, restoreConfirmBody, resetButton, resetHelp, resetConfirmTitle,
        resetConfirmBody, confirm, cancel, statusBackupSaved, statusBackupFailed,
        statusRestoreDone, statusRestoreInvalid, statusRestoreFailed, statusResetDone,
        aboutTitle, aboutBody, versionFormat, privacyTitle, privacyBody
    )

    fun statusText(s: DataStatus): String = when (s) {
        DataStatus.BackupSaved -> statusBackupSaved
        DataStatus.BackupFailed -> statusBackupFailed
        DataStatus.RestoreDone -> statusRestoreDone
        DataStatus.RestoreInvalid -> statusRestoreInvalid
        DataStatus.RestoreFailed -> statusRestoreFailed
        DataStatus.ResetDone -> statusResetDone
    }

    companion object {
        const val BACKUP_FILE_NAME = "social-work-academy-backup.json"

        val English = DataStrings(
            progressTitle = "My progress",
            progressDesc = "Topics, quizzes and weekly activity",
            calcNote = "Every number here is worked out from what you did on this phone.",
            mcqsAttempted = "Questions answered",
            accuracyLabel = "Accuracy",
            bestMock = "Best mock test",
            bestQuiz = "Best practice quiz",
            notYet = "Not yet",
            paperProgressFormat = "{p}: {done} of {total} topics completed",
            recentTitle = "Recent quizzes",
            noRecent = "No quizzes finished yet.",
            weakTitle = "Topics to revise",
            weakHelpFormat = "Topics with at least {m} answers and accuracy below {p}%.",
            noWeak = "No weak topics yet. Answer more questions to see them here.",
            weekTitle = "Last 7 days",
            weekHelp = "Questions answered each day",
            noWeekActivity = "No activity in the last 7 days.",
            weekQuizTimeFormat = "Time in quizzes: {m} min",
            weekTopicsFormat = "Topics opened: {n}",
            questionsShortFormat = "{n} questions",
            dataTitle = "Your data",
            backupButton = "Save a backup",
            backupHelp = "Saves your progress, notes, bookmarks and quiz history to a file you choose.",
            restoreButton = "Restore from a backup",
            restoreHelp = "Loads a backup file made by this app.",
            restoreConfirmTitle = "Restore from a backup?",
            restoreConfirmBody = "The progress, notes, bookmarks and quiz history on this phone will be replaced by the ones in the file.",
            resetButton = "Reset progress",
            resetHelp = "Clears completed topics, quiz history, accuracy and flashcard progress. Notes and bookmarks are kept.",
            resetConfirmTitle = "Reset your progress?",
            resetConfirmBody = "This cannot be undone. Save a backup first if you may want it back.",
            confirm = "Yes",
            cancel = "Cancel",
            statusBackupSaved = "Backup saved.",
            statusBackupFailed = "The backup could not be saved.",
            statusRestoreDone = "Backup restored.",
            statusRestoreInvalid = "This file is not a backup made by this app.",
            statusRestoreFailed = "The backup could not be read.",
            statusResetDone = "Progress has been reset.",
            aboutTitle = "About this app",
            aboutBody = "Social Work Academy is an offline study app for the Social Work paper. " +
                "The notes come from your Word file. The MCQs, glossary and flashcards are practice aids made from those notes.",
            versionFormat = "Version {v}",
            privacyTitle = "Privacy",
            privacyBody = "Everything stays on this phone. The app has no internet permission, " +
                "no account, no ads and no tracking. A backup is only written to the place you choose."
        )

        val Urdu = DataStrings(
            progressTitle = "میری پیش رفت",
            progressDesc = "موضوعات، کوئز اور ہفتہ وار سرگرمی",
            calcNote = "یہاں ہر عدد اسی فون پر آپ کے کیے ہوئے کام سے نکالا گیا ہے۔",
            mcqsAttempted = "حل کیے گئے سوالات",
            accuracyLabel = "درستی",
            bestMock = "بہترین ماک ٹیسٹ",
            bestQuiz = "بہترین مشقی کوئز",
            notYet = "ابھی نہیں",
            paperProgressFormat = "{p}: {total} میں سے {done} موضوعات مکمل",
            recentTitle = "حالیہ کوئز",
            noRecent = "ابھی کوئی کوئز مکمل نہیں ہوا۔",
            weakTitle = "دہرانے کے موضوعات",
            weakHelpFormat = "وہ موضوعات جن میں کم از کم {m} جواب دیے گئے اور درستی {p}٪ سے کم ہے۔",
            noWeak = "ابھی کوئی کمزور موضوع نہیں۔ مزید سوالات حل کریں تاکہ یہاں نظر آئیں۔",
            weekTitle = "پچھلے 7 دن",
            weekHelp = "ہر دن حل کیے گئے سوالات",
            noWeekActivity = "پچھلے 7 دن میں کوئی سرگرمی نہیں۔",
            weekQuizTimeFormat = "کوئز میں لگا وقت: {m} منٹ",
            weekTopicsFormat = "کھولے گئے موضوعات: {n}",
            questionsShortFormat = "{n} سوالات",
            dataTitle = "آپ کا ڈیٹا",
            backupButton = "بیک اپ محفوظ کریں",
            backupHelp = "آپ کی پیش رفت، نوٹس، بک مارکس اور کوئز ہسٹری ایک فائل میں محفوظ ہوتی ہے جو آپ چنیں۔",
            restoreButton = "بیک اپ سے بحال کریں",
            restoreHelp = "اسی ایپ کی بنائی ہوئی بیک اپ فائل کھولتا ہے۔",
            restoreConfirmTitle = "بیک اپ سے بحال کریں؟",
            restoreConfirmBody = "اس فون کی پیش رفت، نوٹس، بک مارکس اور کوئز ہسٹری فائل والی چیزوں سے بدل جائیں گی۔",
            resetButton = "پیش رفت صاف کریں",
            resetHelp = "مکمل موضوعات، کوئز ہسٹری، درستی اور فلیش کارڈ کی پیش رفت صاف ہو جاتی ہے۔ نوٹس اور بک مارکس رہتے ہیں۔",
            resetConfirmTitle = "پیش رفت صاف کریں؟",
            resetConfirmBody = "یہ واپس نہیں ہو سکتا۔ واپس چاہیے ہو تو پہلے بیک اپ محفوظ کر لیں۔",
            confirm = "جی ہاں",
            cancel = "منسوخ",
            statusBackupSaved = "بیک اپ محفوظ ہو گیا۔",
            statusBackupFailed = "بیک اپ محفوظ نہیں ہو سکا۔",
            statusRestoreDone = "بیک اپ بحال ہو گیا۔",
            statusRestoreInvalid = "یہ فائل اس ایپ کا بیک اپ نہیں ہے۔",
            statusRestoreFailed = "بیک اپ پڑھا نہیں جا سکا۔",
            statusResetDone = "پیش رفت صاف ہو گئی۔",
            aboutTitle = "ایپ کے بارے میں",
            aboutBody = "سوشل ورک اکیڈمی سوشل ورک کے پرچے کی آف لائن تیاری کی ایپ ہے۔ " +
                "نوٹس آپ کی ورڈ فائل سے ہیں۔ ایم سی کیوز، اصطلاحات اور فلیش کارڈز انہی نوٹس سے بنائے گئے مشقی ذرائع ہیں۔",
            versionFormat = "ورژن {v}",
            privacyTitle = "رازداری",
            privacyBody = "سب کچھ اسی فون پر رہتا ہے۔ ایپ کو انٹرنیٹ کی اجازت نہیں، " +
                "کوئی اکاؤنٹ، اشتہار یا ٹریکنگ نہیں۔ بیک اپ صرف اسی جگہ لکھا جاتا ہے جو آپ چنیں۔"
        )

        fun forLanguage(isUrdu: Boolean) = if (isUrdu) Urdu else English
    }
}
