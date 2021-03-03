package com.begateway.mobilepayments.models.network.request

import com.google.gson.annotations.SerializedName

enum class Language {
    @SerializedName("en") ENGLISH,
    @SerializedName("es") SPANISH,
    @SerializedName("tr") TURKISH,
    @SerializedName("de") GERMAN,
    @SerializedName("it") ITALIAN,
    @SerializedName("ru") RUSSIAN,
    @SerializedName("zh") CHINESE,
    @SerializedName("fr") FRENCH,
    @SerializedName("da") DANISH,
    @SerializedName("sv") SWEDISH,
    @SerializedName("no") NORWEGIAN,
    @SerializedName("fi") FINNISH,
    @SerializedName("pl") POLISH,
    @SerializedName("ja") JAPANESE,
    @SerializedName("ua") UKRAINIAN,
    @SerializedName("be") BELARUSSIAN,
    @SerializedName("ka") GEORGIAN,
}