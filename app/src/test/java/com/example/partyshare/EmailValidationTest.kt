package com.example.partyshare

import android.util.Patterns
import org.junit.Assert.*
import org.junit.Test
import java.util.regex.Pattern

class EmailValidationTest {
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
    fun `correct email returns true`() {
        var msg = false
        val result = "something@gmail.com"
        if(result.isNotEmpty() && EMAIL_ADDRESS_PATTERN.matcher(result).matches()) {
            msg = true
        }
        assertTrue("", msg)
    }

    @Test
    fun `e-mail with space returns false`() {
        var msg = false
        val result = "some thing@gmail.com"
        if(result.isNotEmpty() && EMAIL_ADDRESS_PATTERN.matcher(result).matches()) {
            msg = true
        }
        assertFalse("", msg)
    }

    @Test
    fun `e-mail with double @ returns false`() {
        var msg = false
        val result = "something@@gmail.com"
        if(result.isNotEmpty() && EMAIL_ADDRESS_PATTERN.matcher(result).matches()) {
            msg = true
        }
        assertFalse("", msg)
    }

    @Test
    fun `empty e-mail returns false`() {
        var msg = false
        val result = "something@@gmail.com"
        if(result.isNotEmpty() && EMAIL_ADDRESS_PATTERN.matcher(result).matches()) {
            msg = true
        }
        assertFalse("", msg)
    }

    @Test
    fun `e-mail without domain returns false`() {
        var msg = false
        val result = "something@gmail"
        if(result.isNotEmpty() && EMAIL_ADDRESS_PATTERN.matcher(result).matches()) {
            msg = true
        }
        assertFalse("", msg)
    }

    @Test
    fun `e-mail with double dot returns false`() {
        var msg = false
        val result = "something@gmail..com"
        if(result.isNotEmpty() && EMAIL_ADDRESS_PATTERN.matcher(result).matches()) {
            msg = true
        }
        assertFalse("", msg)
    }

    @Test
    fun `e-mail with @ after domain returns false`() {
        var msg = false
        val result = "something@gmail.com@"
        if(result.isNotEmpty() && EMAIL_ADDRESS_PATTERN.matcher(result).matches()) {
            msg = true
        }
        assertFalse("", msg)
    }

}