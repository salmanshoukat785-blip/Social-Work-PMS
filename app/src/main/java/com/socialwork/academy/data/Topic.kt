package com.socialwork.academy.data

/**
 * One study topic. Titles follow the supplied "Social Work Paper 1 & 2" file
 * and the topic list in the app requirements.
 * Phase 4 will load the notes for each topic from bundled content files.
 */
data class Topic(
    val id: String,
    val paper: Int,
    val titleEn: String,
    val titleUr: String
) {
    fun title(isUrdu: Boolean): String = if (isUrdu) titleUr else titleEn

    /** The title in the language that is NOT currently selected. */
    fun otherTitle(isUrdu: Boolean): String = if (isUrdu) titleEn else titleUr
}

object TopicCatalog {

    val paper1: List<Topic> = listOf(
        Topic("p1_society", 1, "Society", "معاشرہ"),
        Topic("p1_society_characteristics", 1, "Society Characteristics", "معاشرتی خصوصیات"),
        Topic("p1_europe_impact", 1, "Impact of Europe on Pakistani Culture", "یورپی اثرات اور پاکستانی ثقافت"),
        Topic("p1_culture_characteristics", 1, "Culture Characteristics", "ثقافتی خصوصیات"),
        Topic("p1_rural_urban", 1, "Rural & Urban Society", "دیہی اور شہری (دیہی و شہری زندگی کا موازنہ)"),
        Topic("p1_social_institutions", 1, "Social Institutions", "معاشرتی ادارے"),
        Topic("p1_family", 1, "Family", "خاندان"),
        Topic("p1_religious_institutions", 1, "Religious Institutions", "مذہبی ادارے"),
        Topic("p1_educational_institutions", 1, "Educational Institutions", "تعلیمی ادارے"),
        Topic("p1_economic_institutions", 1, "Economic Institutions", "معاشی ادارے"),
        Topic("p1_norms_socialization", 1, "Social Norms & Socialization", "معاشرتی اقدار اور سماجی کاری"),
        Topic("p1_importance_institutions", 1, "Importance of Social Institutions", "معاشرتی اداروں کی اہمیت"),
        Topic("p1_social_problems", 1, "Social Problems", "معاشرتی مسائل"),
        Topic("p1_social_changes", 1, "Social Changes", "معاشرتی تبدیلیاں"),
        Topic("p1_barriers", 1, "Hurdles / Barriers to Social Change", "رکاوٹیں / حائل عوامل")
    )

    val paper2: List<Topic> = listOf(
        Topic("p2_philosophy", 2, "Philosophy of Social Work", "فلسفہِ سوشل ورک"),
        Topic("p2_practice", 2, "Social Work Practice", "سوشل ورک پریکٹس"),
        Topic("p2_objectives", 2, "Objectives of Social Work", "مقاصد"),
        Topic("p2_principles", 2, "Principles of Social Work", "اصول"),
        Topic("p2_professional_voluntary", 2, "Professional & Voluntary Social Work", "پیشہ ورانہ اور رضاکارانہ"),
        Topic("p2_basic_philosophy", 2, "Basic Philosophy", "بنیادی فلسفہ"),
        Topic("p2_islam_social_work", 2, "Islam and Social Work", "اسلام اور سوشل ورک"),
        Topic("p2_worth_dignity", 2, "Worth and Dignity of Individual", "فرد کی قدر و منزلت"),
        Topic("p2_islamic_society_responsibility", 2, "Islamic Society and Individual Responsibility", "اسلامی معاشرہ اور انفرادی ذمہ داری"),
        Topic("p2_methods", 2, "Methods of Social Work", "سوشل ورک کے طریقے"),
        Topic("p2_primary_methods", 2, "Primary Methods", "بنیادی طریقے"),
        Topic("p2_secondary_methods", 2, "Secondary Methods", "ثانوی طریقے"),
        Topic("p2_social_relations", 2, "Social Relations", "سماجی تعلقات"),
        Topic("p2_case_work", 2, "Social Case Work", "سماجی انفرادی خدمت (سوشل کیس ورک)"),
        Topic("p2_social_action", 2, "Social Action: Importance", "فوائد اور سماجی عمل کی اہمیت"),
        Topic("p2_group_work", 2, "Social Group Work", "سماجی گروہی کام"),
        Topic("p2_community_development", 2, "Community Development", "کمیونٹی ڈویلپمنٹ"),
        Topic("p2_research_steps", 2, "Social Research Steps", "سماجی تحقیق کے مراحل"),
        Topic("p2_role_worker", 2, "Role of Professional Social Worker", "سماجی کارکن کا کردار"),
        Topic("p2_data_tools", 2, "Tools for Data Collection", "ڈیٹا جمع کرنے کے اوزار"),
        Topic("p2_welfare_admin", 2, "Social Welfare Administration: Importance", "سماجی بہبود کی انتظامیہ کی اہمیت"),
        Topic("p2_ngo_role", 2, "Role of NGOs", "غیر سرکاری تنظیموں (این جی اوز) کا کردار")
    )

    val all: List<Topic> = paper1 + paper2

    fun forPaper(paper: Int): List<Topic> = if (paper == 2) paper2 else paper1

    fun byId(id: String?): Topic? = if (id == null) null else all.firstOrNull { it.id == id }
}

/** Topic counts worked out from what the user has actually marked as completed. */
data class ProgressStats(val total: Int, val completed: Int) {
    val remaining: Int get() = total - completed
    val fraction: Float get() = if (total == 0) 0f else completed.toFloat() / total
}

fun progressFor(topics: List<Topic>, completedIds: Set<String>): ProgressStats =
    ProgressStats(total = topics.size, completed = topics.count { it.id in completedIds })
