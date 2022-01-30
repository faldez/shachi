package com.faldez.sachi.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tag(
    val id: Int,
    val name: String,
    val count: Int,
    val type: Int,
    val ambiguous: Boolean,
    val excluded: Boolean = false,
) : Parcelable {
    override fun toString(): String {
        return if (excluded) {
            "-$name"
        } else {
            name
        }
    }

    companion object {
        fun fromName(name: String): Tag {
            return Tag(id = 0, name = name, count = 0, type = 0, ambiguous = false)
        }
    }
}