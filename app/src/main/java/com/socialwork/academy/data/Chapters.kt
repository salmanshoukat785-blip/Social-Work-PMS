package com.socialwork.academy.data

/**
 * A chapter of the PDF: chapter -> headings (topics) -> points.
 * [chapterTopicId] is the topic that is the chapter itself (opens when the
 * chapter bar is tapped); [childTopicIds] are the headings listed under it.
 */
data class Chapter(
    val paper: Int,
    val titleEn: String,
    val titleUr: String,
    val chapterTopicId: String?,
    val childTopicIds: List<String> = emptyList()
) {
    fun title(isUrdu: Boolean): String = if (isUrdu) titleUr else titleEn
    fun otherTitle(isUrdu: Boolean): String = if (isUrdu) titleEn else titleUr
    val key: String get() = "$paper:${chapterTopicId ?: childTopicIds.first()}"
    val allTopicIds: List<String> get() = listOfNotNull(chapterTopicId) + childTopicIds
}

object ChapterCatalog {

    private fun single(id: String): Chapter {
        val t = TopicCatalog.byId(id)!!
        return Chapter(t.paper, t.titleEn, t.titleUr, id)
    }

    val paper1: List<Chapter> = listOf(
        Chapter(1, "Society", "معاشرہ", "p1_society",
            listOf("p1_society_characteristics", "p1_europe_impact", "p1_culture_characteristics", "p1_rural_urban")),
        Chapter(1, "Social Institutions", "معاشرتی ادارے", "p1_social_institutions",
            listOf("p1_family", "p1_religious_institutions", "p1_educational_institutions", "p1_economic_institutions")),
        single("p1_norms_socialization"),
        single("p1_importance_institutions"),
        single("p1_social_problems"),
        Chapter(1, "Social Changes", "معاشرتی تبدیلیاں", null, listOf("p1_social_changes", "p1_barriers"))
    )

    val paper2: List<Chapter> = listOf(
        Chapter(2, "Philosophy of Social Work", "فلسفہِ سوشل ورک", null,
            listOf("p2_philosophy", "p2_objectives", "p2_principles", "p2_professional_voluntary")),
        Chapter(2, "Islam and Social Work", "اسلام اور سوشل ورک", null,
            listOf("p2_islam_social_work", "p2_worth_dignity", "p2_islamic_society_responsibility")),
        Chapter(2, "Methods of Social Work", "سوشل ورک کے طریقے", "p2_methods",
            listOf("p2_primary_methods", "p2_secondary_methods")),
        single("p2_social_relations"),
        single("p2_case_work"),
        single("p2_case_work_phases"),
        single("p2_social_action"),
        single("p2_group_work"),
        single("p2_community_development"),
        single("p2_research_steps"),
        single("p2_role_worker"),
        single("p2_data_tools"),
        single("p2_welfare_admin"),
        single("p2_ngo_role")
    )

    fun forPaper(paper: Int): List<Chapter> = if (paper == 2) paper2 else paper1

    val all: List<Chapter> = paper1 + paper2
}
