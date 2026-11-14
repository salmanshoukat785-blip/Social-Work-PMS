package com.socialwork.academy.data

import android.content.Context
import org.json.JSONObject

/**
 * One multiple-choice question.
 *
 * These are ADDITIONAL PRACTICE QUESTIONS made from the lists in the notes
 * (the Word file itself contains no MCQs). Every question has exactly one
 * correct option, checked by a script against the notes when the file was made.
 */
data class Mcq(
    val id: String,
    /** "en" or "ur": the language the question and options are written in. */
    val lang: String,
    val kind: String,
    val topicId: String,
    val paper: Int,
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String
) {
    val isUrdu: Boolean get() = lang == "ur"
}

data class McqBundle(
    val label: String,
    val questions: List<Mcq>
) {
    private val byId: Map<String, Mcq> = questions.associateBy { it.id }

    fun byId(id: String): Mcq? = byId[id]

    fun count(isUrdu: Boolean): Int = questions.count { it.isUrdu == isUrdu }
}

/** Loading state of the bundled questions. */
sealed interface McqState {
    data object Loading : McqState
    data class Ready(val bundle: McqBundle) : McqState
    data object Failed : McqState
}

object McqParser {

    fun parse(json: String): McqBundle {
        val root = JSONObject(json)
        val array = root.getJSONArray("questions")
        val list = ArrayList<Mcq>(array.length())
        for (i in 0 until array.length()) {
            val q = array.getJSONObject(i)
            val options = q.getJSONArray("options")
            list += Mcq(
                id = q.getString("id"),
                lang = q.getString("lang"),
                kind = q.optString("kind"),
                topicId = q.getString("topicId"),
                paper = q.getInt("paper"),
                question = q.getString("question"),
                options = (0 until options.length()).map { options.getString(it) },
                answerIndex = q.getInt("answer"),
                explanation = q.optString("explanation")
            )
        }
        return McqBundle(label = root.optString("label"), questions = list)
    }
}

object McqRepository {

    private const val ASSET_PATH = "content/mcqs.json"

    /** Reads the bundled questions (works offline). Returns null if they cannot be read. */
    fun load(context: Context): McqBundle? = try {
        val text = context.assets.open(ASSET_PATH).bufferedReader(Charsets.UTF_8).use { it.readText() }
        McqParser.parse(text)
    } catch (e: Exception) {
        null
    }
}
