package com.faldez.shachi.data.preference

enum class GridMode(val value: String) {
    Staggered("staggered"),
    Square("square")
}

enum class Filter(val value: String) {
    Disable("disable"),
    Hide("hide"),
    Mute("mute")
}

enum class Quality(val value: String) {
    Preview("preview"),
    Sample("sample"),
    Original("original")
}

fun String.toGridMode(): GridMode = when (this.lowercase()) {
    "staggered" -> GridMode.Staggered
    "square" -> GridMode.Square
    else -> throw IllegalAccessException()
}

fun String.toFilter(): Filter = when (this.lowercase()) {
    "disable" -> Filter.Disable
    "hide" -> Filter.Hide
    "mute" -> Filter.Mute
    else -> throw IllegalAccessException()
}

fun String.toQuality(): Quality = when (this.lowercase()) {
    "preview" -> Quality.Preview
    "sample" -> Quality.Sample
    "original" -> Quality.Original
    else -> throw IllegalAccessException()
}