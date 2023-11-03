package com.sendbird.uikit.internal.model

import com.sendbird.uikit.consts.StringSet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.regex.PatternSyntaxException

@Serializable
internal data class Form(
    @SerialName(StringSet.key)
    val formKey: String,
    @SerialName(StringSet.fields)
    val formFields: List<FormField>,
    @SerialName(StringSet.data)
    val data: Map<String, String>? = null
) {
    val answeredList: List<Answer>?
        get() {
            return data?.map { Answer(it.key, it.value) }
        }
    val isAnswered: Boolean
        get() = data != null
}

@Serializable
internal data class FormField(
    @SerialName(StringSet.key)
    val formFieldKey: String,
    @SerialName(StringSet.title)
    val title: String,
    @SerialName(StringSet.input_type)
    val inputType: String? = null,
    @SerialName(StringSet.regex)
    val regex: String? = null,
    @SerialName(StringSet.placeholder)
    val placeholder: String? = null,
    @SerialName(StringSet.required)
    val required: Boolean,
    @Transient
    var messageId: Long = 0L,
    @Transient
    var answer: Answer? = null,
    @Transient
    var temporaryAnswer: Answer? = null,
    @Transient
    var lastValidation: Boolean? = null
) {
    val formFileInputType: FormFieldInputType
        get() = FormFieldInputType.from(inputType)

    fun isValid(s: String): Boolean {
        val regex = this.regex?.toRegex() ?: return true
        return try {
            s.matches(regex)
        } catch (_: PatternSyntaxException) {
            true // if the regex is invalid pattern, it assumes the given string is valid.
        }
    }

    fun isReadyToSubmit(): Boolean {
        val answer = temporaryAnswer
        if (answer != null && !isValid(answer.answer)) return false
        return !required || answer != null
    }
}

internal data class Answer(
    val formFieldKey: String,
    var answer: String
)

internal enum class FormFieldInputType(val value: String) {
    TEXT(StringSet.text),
    PHONE(StringSet.phone),
    EMAIL(StringSet.email),
    PASSWORD(StringSet.password);

    companion object {
        fun from(inputType: String?): FormFieldInputType {
            return values().find { it.value.equals(inputType, true) } ?: TEXT
        }
    }
}
