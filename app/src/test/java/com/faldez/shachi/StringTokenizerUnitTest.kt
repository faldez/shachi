package com.faldez.shachi

import com.faldez.shachi.util.StringUtil
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class StringTokenizerUnitTest {
    @Test
    fun tokenizerIsCorrect() {
        val cursor = 12
        val text = "arknights sma long_hair"
        val token = StringUtil.getCurrentToken(text, cursor)
        assertEquals("sma", token)
    }
}