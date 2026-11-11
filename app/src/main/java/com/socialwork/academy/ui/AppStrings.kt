package com.socialwork.academy.ui

/**
 * Interface text for the two supported languages.
 * Later phases can move longer content to the bundled JSON files.
 */
data class AppStrings(
    val appName: String,
    val tabHome: String,
    val tabPapers: String,
    val tabPractice: String,
    val tabSettings: String,
    val welcome: String,
    val welcomeNameFormat: String,
    val tagline: String,
    val paper1: String,
    val paper2: String,
    val paper1Sub: String,
    val paper2Sub: String,
    val papersTitle: String,
    val practiceTitle: String,
    val practiceBody: String,
    val settingsTitle: String,
    val languageLabel: String,
    val darkModeLabel: String,
    val offlineNote: String,
    val comingLater: String,
    val back: String,
    // Onboarding
    val ob1Title: String,
    val ob1Body: String,
    val ob2Title: String,
    val ob2Body: String,
    val ob3Title: String,
    val ob3Body: String,
    val next: String,
    val skip: String,
    val getStarted: String,
    // Language selection
    val chooseLanguage: String,
    val languageHint: String,
    val continueLabel: String,
    // Profile
    val profileTitle: String,
    val profileHint: String,
    val nameLabel: String,
    val institutionLabel: String,
    val skipForNow: String
) {
    fun welcomeName(name: String): String = welcomeNameFormat.replace("%s", name)

    /** Every text in this language; used by the unit test to catch blanks. */
    fun all(): List<String> = listOf(
        appName, tabHome, tabPapers, tabPractice, tabSettings, welcome, welcomeNameFormat,
        tagline, paper1, paper2, paper1Sub, paper2Sub, papersTitle, practiceTitle,
        practiceBody, settingsTitle, languageLabel, darkModeLabel, offlineNote,
        comingLater, back, ob1Title, ob1Body, ob2Title, ob2Body, ob3Title, ob3Body,
        next, skip, getStarted, chooseLanguage, languageHint, continueLabel,
        profileTitle, profileHint, nameLabel, institutionLabel, skipForNow
    )

    companion object {
        val English = AppStrings(
            appName = "Social Work Academy",
            tabHome = "Home",
            tabPapers = "Papers",
            tabPractice = "Practice",
            tabSettings = "Settings",
            welcome = "Welcome",
            welcomeNameFormat = "Welcome, %s",
            tagline = "Social Work exam preparation, fully offline",
            paper1 = "Paper 1",
            paper2 = "Paper 2",
            paper1Sub = "Society, culture and social institutions",
            paper2Sub = "Philosophy and methods of Social Work",
            papersTitle = "Papers",
            practiceTitle = "MCQs and Tests",
            practiceBody = "Quizzes and mock tests will be added in a later phase.",
            settingsTitle = "Settings",
            languageLabel = "Urdu (اردو)",
            darkModeLabel = "Dark mode",
            offlineNote = "This app works without internet.",
            comingLater = "Topics and notes will appear here in a later phase.",
            back = "Back",
            ob1Title = "Study Paper 1 and Paper 2",
            ob1Body = "Topic-wise notes in English and Urdu, arranged the way your course is.",
            ob2Title = "Practice and test yourself",
            ob2Body = "MCQs, quizzes and mock tests to prepare for your exam.",
            ob3Title = "Fully offline",
            ob3Body = "No internet and no account needed. Your progress stays on your phone.",
            next = "Next",
            skip = "Skip",
            getStarted = "Get Started",
            chooseLanguage = "Choose your language",
            languageHint = "You can change this later in Settings.",
            continueLabel = "Continue",
            profileTitle = "Your profile",
            profileHint = "Saved only on this phone. No account needed.",
            nameLabel = "Name",
            institutionLabel = "College / University (optional)",
            skipForNow = "Skip for now"
        )

        val Urdu = AppStrings(
            appName = "سوشل ورک اکیڈمی",
            tabHome = "ہوم",
            tabPapers = "پرچے",
            tabPractice = "مشق",
            tabSettings = "ترتیبات",
            welcome = "خوش آمدید",
            welcomeNameFormat = "خوش آمدید، %s",
            tagline = "سوشل ورک کے امتحان کی تیاری، مکمل آف لائن",
            paper1 = "پرچہ اول",
            paper2 = "پرچہ دوم",
            paper1Sub = "معاشرہ، ثقافت اور معاشرتی ادارے",
            paper2Sub = "فلسفۂ سوشل ورک اور طریقے",
            papersTitle = "پرچے",
            practiceTitle = "ایم سی کیوز اور ٹیسٹ",
            practiceBody = "کوئز اور ماک ٹیسٹ بعد کے مرحلے میں شامل کیے جائیں گے۔",
            settingsTitle = "ترتیبات",
            languageLabel = "اردو (Urdu)",
            darkModeLabel = "ڈارک موڈ",
            offlineNote = "یہ ایپ انٹرنیٹ کے بغیر چلتی ہے۔",
            comingLater = "موضوعات اور نوٹس بعد کے مرحلے میں یہاں نظر آئیں گے۔",
            back = "واپس",
            ob1Title = "پرچہ اول اور پرچہ دوم کی تیاری",
            ob1Body = "انگریزی اور اردو میں موضوع وار نوٹس، آپ کے کورس کی ترتیب کے مطابق۔",
            ob2Title = "مشق کریں اور خود کو جانچیں",
            ob2Body = "امتحان کی تیاری کے لیے ایم سی کیوز، کوئز اور ماک ٹیسٹ۔",
            ob3Title = "مکمل آف لائن",
            ob3Body = "انٹرنیٹ یا اکاؤنٹ کی ضرورت نہیں۔ آپ کی پیش رفت آپ کے فون میں ہی رہتی ہے۔",
            next = "آگے",
            skip = "چھوڑیں",
            getStarted = "شروع کریں",
            chooseLanguage = "اپنی زبان منتخب کریں",
            languageHint = "آپ اسے بعد میں ترتیبات میں بدل سکتے ہیں۔",
            continueLabel = "جاری رکھیں",
            profileTitle = "آپ کا پروفائل",
            profileHint = "صرف اسی فون میں محفوظ ہوتا ہے۔ اکاؤنٹ کی ضرورت نہیں۔",
            nameLabel = "نام",
            institutionLabel = "کالج / یونیورسٹی (اختیاری)",
            skipForNow = "ابھی چھوڑیں"
        )
    }
}
