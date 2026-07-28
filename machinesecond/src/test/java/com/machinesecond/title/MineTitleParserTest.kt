package com.machinesecond.title

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MineTitleParserTest {

    @Test
    fun testTable_20_12() {
        val title = "20-12"
        val sep = "-"
        val hint = "20"
        assertEquals(2, MineTitleParser.mineCount(title, sep, hint))
        assertEquals(setOf(1, 2), MineTitleParser.mineDigits(title, sep, hint))
        assertTrue(MineTitleParser.isCombinedGreeting(title, sep, hint))
    }

    @Test
    fun testTable_1() {
        val title = "1"
        val sep = ""
        val hint = ""
        assertEquals(1, MineTitleParser.mineCount(title, sep, hint))
        assertEquals(setOf(1), MineTitleParser.mineDigits(title, sep, hint))
        assertTrue(MineTitleParser.isCombinedGreeting(title, sep, hint))
    }

    @Test
    fun testTable_100123() {
        val title = "100123"
        val sep = ""
        val hint = "100"
        assertEquals(3, MineTitleParser.mineCount(title, sep, hint))
        assertEquals(setOf(1, 2, 3), MineTitleParser.mineDigits(title, sep, hint))
        assertTrue(MineTitleParser.isCombinedGreeting(title, sep, hint))
    }

    @Test
    fun testTable_gongxi() {
        val title = "恭喜发财"
        val sep = ""
        val hint = ""
        assertEquals(0, MineTitleParser.mineCount(title, sep, hint))
        assertEquals(emptySet<Int>(), MineTitleParser.mineDigits(title, sep, hint))
        assertFalse(MineTitleParser.isCombinedGreeting(title, sep, hint))
    }

    @Test
    fun testTable_peifu() {
        val title = "赔付张三"
        val sep = ""
        val hint = ""
        assertEquals(0, MineTitleParser.mineCount(title, sep, hint))
        assertEquals(emptySet<Int>(), MineTitleParser.mineDigits(title, sep, hint))
        assertFalse(MineTitleParser.isCombinedGreeting(title, sep, hint))
    }

    @Test
    fun testTable_2_1() {
        val title = "2-1"
        val sep = "-"
        val hint = "2"
        assertEquals(1, MineTitleParser.mineCount(title, sep, hint))
        assertEquals(setOf(1), MineTitleParser.mineDigits(title, sep, hint))
        assertTrue(MineTitleParser.isCombinedGreeting(title, sep, hint))
    }

    @Test
    fun testLastDigitOfAmountYuan() {
        assertEquals(1, MineTitleParser.lastDigitOfAmountYuan(0.01))
        assertEquals(2, MineTitleParser.lastDigitOfAmountYuan(19.02))
    }

    @Test
    fun testMatrixKey() {
        val packetCount = 9
        val mineCount = 2
        val key = "${packetCount - 4}-$mineCount"
        assertEquals("5-2", key)
    }

    @Test
    fun testYuanFromServer() {
        assertEquals(0.03, MineTitleParser.yuanFromServer(3.0), 0.0001)
        assertEquals(19.02, MineTitleParser.yuanFromServer(1902.0), 0.0001)
        assertEquals(12.12, MineTitleParser.yuanFromServer(12.12), 0.0001)
        assertEquals(0.0, MineTitleParser.yuanFromServer(0.0), 0.0001)
    }
}
