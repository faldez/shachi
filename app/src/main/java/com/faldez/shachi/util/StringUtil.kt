package com.faldez.shachi.util

class StringUtil {
    companion object {
        fun findTokenStart(text: String, cursor: Int): Int {
            val start = if (cursor <= 0) {
                0
            } else {
                var i = if (cursor >= text.length) {
                    text.length - 1
                } else {
                    cursor
                }
                while (i > 0 && text[i - 1] != ' ') {
                    i -= 1
                }
                i
            }
            return if (text[start] == '{' || text[start] == '-' || text[start] == '~') start + 1
            else start
        }

        fun findTokenEnd(text: String, cursor: Int): Int {
            val end = if (cursor == text.length - 1) {
                text.length - 1
            } else {
                var i = if (cursor == text.length) {
                    cursor - 1
                } else {
                    cursor
                }
                while (i < text.length - 1 && text[i + 1] != ' ') {
                    i += 1
                }
                if (i < text.length) {
                    i
                } else {
                    text.length - 1
                }
            }
            return if (text[end] == '}') end - 1
            else end
        }

        fun getCurrentToken(text: String, cursor: Int): String {
            val start = findTokenStart(text, cursor)
            val end = findTokenEnd(text, cursor)
            val text = text.substring(start, end + 1)
            return text
        }
    }
}