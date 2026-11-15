package com.socialwork.academy.ui

/** Interface text for the "Additional Explanation" cards. */
data class DetailStrings(
    val title: String,
    val note: String,
    val shortLabel: String,
    val longLabel: String,
    val tapToOpen: String,
    val expandAll: String,
    val collapseAll: String,
    val readMore: String,
    val readLess: String
) {
    fun all(): List<String> = listOf(title, note, shortLabel, longLabel, tapToOpen, expandAll, collapseAll, readMore, readLess)

    companion object {
        val English = DetailStrings(
            title = "Additional Explanation",
            note = "A short and a long explanation for each point. These were written to help your study. They are not from your Word file and are not teacher-reviewed, so check them with your teacher or books.",
            shortLabel = "Short",
            longLabel = "Long",
            tapToOpen = "Tap a point to read its explanation",
            expandAll = "Open all",
            collapseAll = "Close all",
            readMore = "Read more",
            readLess = "Show less"
        )
        val Urdu = DetailStrings(
            title = "اضافی وضاحت",
            note = "ہر نکتے کی مختصر اور تفصیلی وضاحت۔ یہ مطالعے میں مدد کے لیے لکھی گئی ہے، آپ کی ورڈ فائل کا حصہ نہیں اور کسی استاد نے اس کی جانچ نہیں کی، اس لیے اپنے استاد یا کتابوں سے ملا لیں۔",
            shortLabel = "مختصر",
            longLabel = "تفصیلی",
            tapToOpen = "وضاحت پڑھنے کے لیے کسی نکتے پر ٹیپ کریں",
            expandAll = "سب کھولیں",
            collapseAll = "سب بند کریں",
            readMore = "مزید پڑھیں",
            readLess = "کم دکھائیں"
        )
        fun of(isUrdu: Boolean) = if (isUrdu) Urdu else English
    }
}
