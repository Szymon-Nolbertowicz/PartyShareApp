package com.example.partyshare

import android.util.Log
import android.util.Patterns
import org.junit.Test
import com.google.common.truth.Truth.assertThat

import org.junit.Assert.*
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    val EMAIL_ADDRESS_PATTERN = Pattern.compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
    "\\@" +
    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
    "(" +
    "\\." +
    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
    ")+"
    );

    @Test
    fun `email_isCorrect`() {
        val testString = "test123@gmail.com"
        var result = false
        if (EMAIL_ADDRESS_PATTERN.matcher(testString).matches())
            result = true
        assertEquals(result, true)
    }

    @Test
    fun `email_isNotCorrect`() {
        val testString = "test123gmail.com"
        var result = false
        if (EMAIL_ADDRESS_PATTERN.matcher(testString).matches())
            result = true
        assertEquals(result, true)
    }
}