package com.socialwork.academy.ui

/** Interface text for the MCQ / quiz screens (Phase 6), in both languages. */
data class QuizStrings(
    val note: String,
    val loading: String,
    val loadError: String,
    val setupTitle: String,
    val scopeTitle: String,
    val scopeAll: String,
    val scopePaper1: String,
    val scopePaper2: String,
    val scopeTopic: String,
    val pickTopic: String,
    val countTitle: String,
    val timerTitle: String,
    val noTimer: String,
    val minutesFormat: String,
    val feedbackLabel: String,
    val feedbackHint: String,
    val availableFormat: String,
    val fewerNote: String,
    val noQuestions: String,
    val startQuiz: String,
    val questionOfFormat: String,
    val previous: String,
    val next: String,
    val finish: String,
    val finishTitle: String,
    val unansweredFormat: String,
    val allAnswered: String,
    val finishNow: String,
    val keepGoing: String,
    val correct: String,
    val wrong: String,
    val yourAnswer: String,
    val rightAnswer: String,
    val explanation: String,
    val notAnswered: String,
    val timeUp: String,
    val resultTitle: String,
    val scoreFormat: String,
    val correctLabel: String,
    val wrongLabel: String,
    val unansweredLabel: String,
    val timeTaken: String,
    val topicAccuracyTitle: String,
    val noAccuracy: String,
    val accuracyFormat: String,
    val reviewAnswers: String,
    val retryIncorrect: String,
    val perfectScore: String,
    val backToPractice: String,
    val historyTitle: String,
    val noHistory: String,
    val reviewTitle: String
) {
    fun minutes(n: Int): String = minutesFormat.replace("{n}", n.toString())

    fun available(n: Int): String = availableFormat.replace("{n}", n.toString())

    fun questionOf(i: Int, n: Int): String =
        questionOfFormat.replace("{i}", i.toString()).replace("{n}", n.toString())

    fun unanswered(n: Int): String = unansweredFormat.replace("{n}", n.toString())

    fun score(correct: Int, total: Int): String =
        scoreFormat.replace("{c}", correct.toString()).replace("{t}", total.toString())

    fun accuracy(correct: Int, attempted: Int, percent: Int): String =
        accuracyFormat
            .replace("{c}", correct.toString())
            .replace("{a}", attempted.toString())
            .replace("{p}", percent.toString())

    /** Every text; used by the unit test to catch blanks. */
    fun all(): List<String> = listOf(
        note, loading, loadError, setupTitle, scopeTitle, scopeAll, scopePaper1, scopePaper2,
        scopeTopic, pickTopic, countTitle, timerTitle, noTimer, minutesFormat, feedbackLabel,
        feedbackHint, availableFormat, fewerNote, noQuestions, startQuiz, questionOfFormat,
        previous, next, finish, finishTitle, unansweredFormat, allAnswered, finishNow, keepGoing,
        correct, wrong, yourAnswer, rightAnswer, explanation, notAnswered, timeUp, resultTitle,
        scoreFormat, correctLabel, wrongLabel, unansweredLabel, timeTaken, topicAccuracyTitle,
        noAccuracy, accuracyFormat, reviewAnswers, retryIncorrect, perfectScore, backToPractice,
        historyTitle, noHistory, reviewTitle
    )

    companion object {
        val English = QuizStrings(
            note = "Additional practice questions, made from the lists in the notes. " +
                "The Word file itself has no MCQs, and these have not been checked by a teacher.",
            loading = "Loading questions…",
            loadError = "The questions could not be loaded.",
            setupTitle = "Start a quiz",
            scopeTitle = "Questions from",
            scopeAll = "Both papers (mixed)",
            scopePaper1 = "Paper 1",
            scopePaper2 = "Paper 2",
            scopeTopic = "One topic",
            pickTopic = "Choose a topic",
            countTitle = "Number of questions",
            timerTitle = "Timer",
            noTimer = "No timer",
            minutesFormat = "{n} min",
            feedbackLabel = "Show answer after each question",
            feedbackHint = "Off: answers are shown at the end, and you can change them before finishing.",
            availableFormat = "{n} questions available",
            fewerNote = "Fewer questions are available than chosen, so all of them will be used.",
            noQuestions = "There are no questions for this choice yet.",
            startQuiz = "Start quiz",
            questionOfFormat = "Question {i} of {n}",
            previous = "Previous",
            next = "Next",
            finish = "Finish",
            finishTitle = "Finish the quiz?",
            unansweredFormat = "You have {n} unanswered questions.",
            allAnswered = "You have answered every question.",
            finishNow = "Finish now",
            keepGoing = "Keep going",
            correct = "Correct",
            wrong = "Wrong",
            yourAnswer = "Your answer",
            rightAnswer = "Correct answer",
            explanation = "Explanation",
            notAnswered = "Not answered",
            timeUp = "Time is up",
            resultTitle = "Result",
            scoreFormat = "{c} of {t} correct",
            correctLabel = "Correct",
            wrongLabel = "Wrong",
            unansweredLabel = "Unanswered",
            timeTaken = "Time taken",
            topicAccuracyTitle = "Accuracy by topic",
            noAccuracy = "Finish a quiz to see your accuracy for each topic.",
            accuracyFormat = "{c} of {a} correct - {p}%",
            reviewAnswers = "Review answers",
            retryIncorrect = "Retry incorrect questions",
            perfectScore = "No wrong answers. Well done!",
            backToPractice = "Back to practice",
            historyTitle = "Recent quizzes",
            noHistory = "Your finished quizzes will appear here.",
            reviewTitle = "Review"
        )

        val Urdu = QuizStrings(
            note = "اضافی مشقی سوالات، جو نوٹس کی فہرستوں سے بنائے گئے ہیں۔ " +
                "ورڈ فائل میں خود ایم سی کیوز نہیں ہیں، اور انہیں کسی استاد نے نہیں جانچا۔",
            loading = "سوالات کھل رہے ہیں…",
            loadError = "سوالات نہیں کھل سکے۔",
            setupTitle = "کوئز شروع کریں",
            scopeTitle = "سوالات کہاں سے",
            scopeAll = "دونوں پرچے (ملے جلے)",
            scopePaper1 = "پرچہ اول",
            scopePaper2 = "پرچہ دوم",
            scopeTopic = "ایک موضوع",
            pickTopic = "موضوع چنیں",
            countTitle = "سوالات کی تعداد",
            timerTitle = "ٹائمر",
            noTimer = "ٹائمر نہیں",
            minutesFormat = "{n} منٹ",
            feedbackLabel = "ہر سوال کے بعد جواب دکھائیں",
            feedbackHint = "بند ہو تو جوابات آخر میں دکھائے جائیں گے، اور ختم کرنے سے پہلے جواب بدلا جا سکتا ہے۔",
            availableFormat = "{n} سوالات دستیاب ہیں",
            fewerNote = "چنی ہوئی تعداد سے کم سوالات دستیاب ہیں، اس لیے سب استعمال ہوں گے۔",
            noQuestions = "اس انتخاب کے لیے ابھی کوئی سوال نہیں ہے۔",
            startQuiz = "کوئز شروع کریں",
            questionOfFormat = "سوال {i} از {n}",
            previous = "پچھلا",
            next = "اگلا",
            finish = "ختم کریں",
            finishTitle = "کوئز ختم کریں؟",
            unansweredFormat = "{n} سوالات کا جواب نہیں دیا گیا۔",
            allAnswered = "آپ نے ہر سوال کا جواب دیا ہے۔",
            finishNow = "ابھی ختم کریں",
            keepGoing = "جاری رکھیں",
            correct = "درست",
            wrong = "غلط",
            yourAnswer = "آپ کا جواب",
            rightAnswer = "درست جواب",
            explanation = "وضاحت",
            notAnswered = "جواب نہیں دیا",
            timeUp = "وقت ختم ہو گیا",
            resultTitle = "نتیجہ",
            scoreFormat = "{t} میں سے {c} درست",
            correctLabel = "درست",
            wrongLabel = "غلط",
            unansweredLabel = "بغیر جواب",
            timeTaken = "لگا ہوا وقت",
            topicAccuracyTitle = "موضوع کے لحاظ سے درستی",
            noAccuracy = "ہر موضوع کی درستی دیکھنے کے لیے ایک کوئز مکمل کریں۔",
            accuracyFormat = "{a} میں سے {c} درست - {p}٪",
            reviewAnswers = "جوابات کا جائزہ",
            retryIncorrect = "غلط سوالات دوبارہ حل کریں",
            perfectScore = "کوئی جواب غلط نہیں۔ شاباش!",
            backToPractice = "مشق پر واپس",
            historyTitle = "حالیہ کوئز",
            noHistory = "مکمل کیے ہوئے کوئز یہاں نظر آئیں گے۔",
            reviewTitle = "جائزہ"
        )

        fun forLanguage(isUrdu: Boolean): QuizStrings = if (isUrdu) Urdu else English
    }
}
