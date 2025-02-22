/*
 *  Copyright 2017 konsoletyper.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.java.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;

@RunWith(TeaVMTestRunner.class)
public class LongTest {

    @Test
    public void parsesLongInSubstring() {
        assertEquals(0, Long.parseLong("[0]", 1, 2, 10));
        assertEquals(473, Long.parseLong("[473]", 1, 4, 10));
        assertEquals(42, Long.parseLong("[+42]", 1, 4, 10));
        assertEquals(-255, Long.parseLong("[-FF]", 1, 4, 16));
    }

    @Test
    public void compares() {
        assertTrue(Long.compare(10, 5) > 0);
        assertTrue(Long.compare(5, 10) < 0);
        assertTrue(Long.compare(5, 5) == 0);
        assertTrue(Long.compare(Long.MAX_VALUE, Long.MIN_VALUE) > 0);
        assertTrue(Long.compare(Long.MIN_VALUE, Long.MAX_VALUE) < 0);
    }

    @Test
    @SkipJVM
    public void calculatesHashCode() {
        assertEquals(23 ^ 42, Long.hashCode((23L << 32) | 42));
    }

    @Test
    public void bitsReversed() {
        assertEquals(0, Long.reverse(0));
        assertEquals(0x8000000000000000L, Long.reverse(1));
        assertEquals(0x0020000000000000L, Long.reverse(0x400));
        assertEquals(0x0000000000000001L, Long.reverse(0x8000000000000000L));
        assertEquals(0x8888888888888888L, Long.reverse(0x1111111111111111L));
        assertEquals(0x0C0C0C0C0C0C0C0CL, Long.reverse(0x3030303030303030L));
        assertEquals(0x00000000000000FFL, Long.reverse(0xFF00000000000000L));
        assertEquals(0xFF00000000000000L, Long.reverse(0x00000000000000FFL));
        assertEquals(0xFFFFFFFFFFFFFFFFL, Long.reverse(0xFFFFFFFFFFFFFFFFL));
        assertEquals(0xF63BA00000000000L, Long.reverse(0x5DC6F));
    }

    @Test
    public void highestOneBit() {
        assertEquals(1L << 63, Long.highestOneBit(-1L));
        assertEquals(1L << 63, Long.highestOneBit(Long.MIN_VALUE));
        assertEquals(0, Long.highestOneBit(0L));
        assertEquals(16L, Long.highestOneBit(31L));
    }

    @Test
    public void lowestOneBit() {
        assertEquals(0L, Long.lowestOneBit(0L));
        assertEquals(2L, Long.lowestOneBit(50L));
        assertEquals(1L, Long.lowestOneBit(-1L));
    }

    @Test
    public void bitsCounted() {
        assertEquals(39, Long.bitCount(2587208649207147453L));
        assertEquals(0, Long.bitCount(0));
        assertEquals(64, Long.bitCount(-1));
        assertEquals(6, Long.bitCount(12345));
        assertEquals(59, Long.bitCount(-12345));

        for (int i = 0; i < 64; ++i) {
            assertEquals(1, Long.bitCount(1L << i));
        }
    }

    @Test
    public void toStringRadix16() {
        assertEquals("17", Long.toString(23, 16));
        assertEquals("1e240", Long.toString(123456, 16));
        assertEquals("-17", Long.toString(-23, 16));
        assertEquals("7fffffffffffffff", Long.toString(Long.MAX_VALUE, 16));
        assertEquals("-8000000000000000", Long.toString(Long.MIN_VALUE, 16));
    }

    @Test
    public void toStringRadix2() {
        assertEquals("10111", Long.toString(23, 2));
        assertEquals("11110001001000000", Long.toString(123456, 2));
        assertEquals("-10111", Long.toString(-23, 2));
        assertEquals("111111111111111111111111111111111111111111111111111111111111111",
                Long.toString(Long.MAX_VALUE, 2));
        assertEquals("-1000000000000000000000000000000000000000000000000000000000000000",
                Long.toString(Long.MIN_VALUE, 2));
    }
}
