package com.socialwork.academy.ui

/** Interface text for the study tools (Phase 8): notes, glossary, flashcards, PDF reader. */
data class StudyStrings(
    val toolsTitle: String,
    val glossaryTitle: String,
    val glossaryDesc: String,
    val flashcardsTitle: String,
    val flashcardsDesc: String,
    val myNotesTitle: String,
    val myNotesDesc: String,
    val pdfTitle: String,
    val pdfDesc: String,
    val glossaryNote: String,
    val searchTerms: String,
    val termsCountFormat: String,
    val noTerms: String,
    val allPapers: String,
    val loading: String,
    val loadError: String,
    // Notes
    val noteTitle: String,
    val noteHint: String,
    val noteSaved: String,
    val noteDelete: String,
    val noNotes: String,
    val notesCountFormat: String,
    // Flashcards
    val deckTitle: String,
    val onlyNew: String,
    val frontLanguage: String,
    val english: String,
    val urdu: String,
    val startCards: String,
    val knownCountFormat: String,
    val resetKnown: String,
    val noCards: String,
    val cardOfFormat: String,
    val tapToFlip: String,
    val iKnow: String,
    val showAgain: String,
    val roundDone: String,
    val roundSummaryFormat: String,
    val newRound: String,
    val exit: String,
    // PDF
    val pdfNote: String,
    val pdfChoose: String,
    val pdfChooseAnother: String,
    val pdfError: String,
    val pageOfFormat: String,
    val pdfPageDescription: String
) {
    fun termsCount(n: Int) = termsCountFormat.replace("{n}", n.toString())
    fun notesCount(n: Int) = notesCountFormat.replace("{n}", n.toString())
    fun knownCount(n: Int, total: Int) =
        knownCountFormat.replace("{n}", n.toString()).replace("{t}", total.toString())
    fun cardOf(i: Int, n: Int) = cardOfFormat.replace("{i}", i.toString()).replace("{n}", n.toString())
    fun roundSummary(known: Int, again: Int) =
        roundSummaryFormat.replace("{k}", known.toString()).replace("{a}", again.toString())
    fun pageOf(i: Int, n: Int) = pageOfFormat.replace("{i}", i.toString()).replace("{n}", n.toString())

    fun all(): List<String> = listOf(
        toolsTitle, glossaryTitle, glossaryDesc, flashcardsTitle, flashcardsDesc, myNotesTitle,
        myNotesDesc, pdfTitle, pdfDesc, glossaryNote, searchTerms, termsCountFormat, noTerms,
        allPapers, loading, loadError, noteTitle, noteHint, noteSaved, noteDelete, noNotes,
        notesCountFormat, deckTitle, onlyNew, frontLanguage, english, urdu, startCards,
        knownCountFormat, resetKnown, noCards, cardOfFormat, tapToFlip, iKnow, showAgain,
        roundDone, roundSummaryFormat, newRound, exit, pdfNote, pdfChoose, pdfChooseAnother,
        pdfError, pageOfFormat, pdfPageDescription
    )

    companion object {
        val English = StudyStrings(
            toolsTitle = "Study tools",
            glossaryTitle = "Glossary",
            glossaryDesc = "English and Urdu terms from the notes",
            flashcardsTitle = "Flashcards",
            flashcardsDesc = "Revise the terms card by card",
            myNotesTitle = "My notes",
            myNotesDesc = "Notes you wrote on topics",
            pdfTitle = "PDF reader",
            pdfDesc = "Open a PDF from your phone",
            glossaryNote = "Terms taken from the notes. English and Urdu are matched by their " +
                "position in the lists of the Word file, so this is a study aid, not a dictionary.",
            searchTerms = "Search terms",
            termsCountFormat = "{n} terms",
            noTerms = "No terms found.",
            allPapers = "Both papers",
            loading = "Loading…",
            loadError = "The terms could not be loaded.",
            noteTitle = "My note",
            noteHint = "Write your own note for this topic",
            noteSaved = "Saved on this phone",
            noteDelete = "Delete note",
            noNotes = "You have not written any notes yet. Open a topic and write one at the bottom.",
            notesCountFormat = "{n} notes",
            deckTitle = "Choose cards",
            onlyNew = "Only cards I do not know yet",
            frontLanguage = "Show first",
            english = "English",
            urdu = "Urdu",
            startCards = "Start",
            knownCountFormat = "You know {n} of {t} cards",
            resetKnown = "Forget my progress",
            noCards = "There are no cards for this choice.",
            cardOfFormat = "Card {i} of {n}",
            tapToFlip = "Tap the card to turn it over",
            iKnow = "I know it",
            showAgain = "Show again",
            roundDone = "Round finished",
            roundSummaryFormat = "Known: {k}. To revise: {a}.",
            newRound = "New round",
            exit = "Exit",
            pdfNote = "Choose a PDF stored on your phone. It is read on the phone and is not uploaded anywhere.",
            pdfChoose = "Choose a PDF",
            pdfChooseAnother = "Choose another PDF",
            pdfError = "This PDF could not be opened.",
            pageOfFormat = "Page {i} of {n}",
            pdfPageDescription = "PDF page"
        )

        val Urdu = StudyStrings(
            toolsTitle = "مطالعے کے اوزار",
            glossaryTitle = "اصطلاحات",
            glossaryDesc = "نوٹس سے انگریزی اور اردو الفاظ",
            flashcardsTitle = "فلیش کارڈز",
            flashcardsDesc = "الفاظ کارڈ بہ کارڈ دہرائیں",
            myNotesTitle = "میرے نوٹس",
            myNotesDesc = "موضوعات پر آپ کے لکھے نوٹس",
            pdfTitle = "پی ڈی ایف ریڈر",
            pdfDesc = "فون سے پی ڈی ایف کھولیں",
            glossaryNote = "نوٹس سے لیے گئے الفاظ۔ انگریزی اور اردو کو ورڈ فائل کی فہرستوں میں ان کی " +
                "ترتیب کے مطابق ملایا گیا ہے، اس لیے یہ مطالعے میں مدد ہے، لغت نہیں۔",
            searchTerms = "الفاظ تلاش کریں",
            termsCountFormat = "{n} الفاظ",
            noTerms = "کوئی لفظ نہیں ملا۔",
            allPapers = "دونوں پرچے",
            loading = "کھل رہا ہے…",
            loadError = "الفاظ نہیں کھل سکے۔",
            noteTitle = "میرا نوٹ",
            noteHint = "اس موضوع پر اپنا نوٹ لکھیں",
            noteSaved = "اس فون پر محفوظ",
            noteDelete = "نوٹ مٹائیں",
            noNotes = "آپ نے ابھی کوئی نوٹ نہیں لکھا۔ کوئی موضوع کھولیں اور نیچے نوٹ لکھیں۔",
            notesCountFormat = "{n} نوٹس",
            deckTitle = "کارڈ چنیں",
            onlyNew = "صرف وہ کارڈ جو ابھی یاد نہیں",
            frontLanguage = "پہلے دکھائیں",
            english = "انگریزی",
            urdu = "اردو",
            startCards = "شروع کریں",
            knownCountFormat = "{t} میں سے {n} کارڈ آپ کو یاد ہیں",
            resetKnown = "میری پیش رفت مٹائیں",
            noCards = "اس انتخاب کے لیے کوئی کارڈ نہیں۔",
            cardOfFormat = "کارڈ {i} از {n}",
            tapToFlip = "کارڈ الٹنے کے لیے اس پر ٹیپ کریں",
            iKnow = "مجھے یاد ہے",
            showAgain = "دوبارہ دکھائیں",
            roundDone = "دور مکمل",
            roundSummaryFormat = "یاد: {k}۔ دہرانے کے لیے: {a}۔",
            newRound = "نیا دور",
            exit = "باہر",
            pdfNote = "اپنے فون میں محفوظ کوئی پی ڈی ایف چنیں۔ یہ فون پر ہی پڑھی جاتی ہے، کہیں اپ لوڈ نہیں ہوتی۔",
            pdfChoose = "پی ڈی ایف چنیں",
            pdfChooseAnother = "دوسری پی ڈی ایف چنیں",
            pdfError = "یہ پی ڈی ایف نہیں کھل سکی۔",
            pageOfFormat = "صفحہ {i} از {n}",
            pdfPageDescription = "پی ڈی ایف کا صفحہ"
        )

        fun forLanguage(isUrdu: Boolean) = if (isUrdu) Urdu else English
    }
}
