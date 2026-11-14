package com.socialwork.academy.ui

/** Interface text for the "Additional Explanation" cards. */
data class DetailStrings(
    val title: String,
    val note: String,
    val shortLabel: String,
    val longLabel: String,
    val tapToOpen: String,
    val expandAll: String,
    val collapseAll: String
) {
    fun all(): List<String> = listOf(title, note, shortLabel, longLabel, tapToOpen, expandAll, collapseAll)

    companion object {
        val English = DetailStrings(
            title = "Additional Explanation",
            note = "Short and long explanation of each sub-heading. These were written to help your study. They are not from your Word file and are not teacher-reviewed, so check them with your teacher or books.",
            shortLabel = "Short",
            longLabel = "Long",
            tapToOpen = "Tap a sub-heading to read its explanation",
            expandAll = "Open all",
            collapseAll = "Close all"
        )
        val Urdu = DetailStrings(
            title = "اضافی وضاحت",
            note = "ہر ذیلی عنوان کی مختصر اور تفصیلی وضاحت۔ یہ مطالعے میں مدد کے لیے لکھی گئی ہے، آپ کی ورڈ فائل کا حصہ نہیں اور کسی استاد نے اس کی جانچ نہیں کی، اس لیے اپنے استاد یا کتابوں سے ملا لیں۔",
            shortLabel = "مختصر",
            longLabel = "تفصیلی",
            tapToOpen = "وضاحت پڑھنے کے لیے ذیلی عنوان پر ٹیپ کریں",
            expandAll = "سب کھولیں",
            collapseAll = "سب بند کریں"
        )
        fun of(isUrdu: Boolean) = if (isUrdu) Urdu else English
    }
}
