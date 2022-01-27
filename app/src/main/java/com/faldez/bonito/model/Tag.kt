package com.faldez.bonito.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tag(
    val id: Int,
    val name: String,
    val count: Int,
    val type: Int,
    val ambiguous: Boolean,
) : Parcelable {
    override fun toString(): String {
        return name
    }
}