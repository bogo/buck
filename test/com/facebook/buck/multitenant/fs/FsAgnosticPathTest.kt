/*
 * Copyright 2019-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.buck.multitenant.fs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class FsAgnosticPathTest {
    @get:Rule
    val thrown = ExpectedException.none()

    @Test
    fun emptyPathIsOk() {
        val path = FsAgnosticPath.of("")
        assertEquals("", path.toString())
    }

    @Test
    fun singleComponentIsOk() {
        val path = FsAgnosticPath.of("foo")
        assertEquals("foo", path.toString())
    }

    @Test
    fun multiComponentIsOk() {
        val path = FsAgnosticPath.of("foo/bar")
        assertEquals("foo/bar", path.toString())
    }

    @Test
    fun isEmpty() {
        val emptyPath = FsAgnosticPath.of("")
        assertTrue(emptyPath.isEmpty())

        val foo = FsAgnosticPath.of("foo")
        assertFalse(foo.isEmpty())

        val fooBar = FsAgnosticPath.of("foo/bar")
        assertFalse(fooBar.isEmpty())
    }

    @Test
    fun pathStartsWithItself() {
        val emptyPath = FsAgnosticPath.of("")
        assertTrue(emptyPath.startsWith(emptyPath))

        val foo = FsAgnosticPath.of("foo")
        assertTrue(foo.startsWith(foo))

        val fooBar = FsAgnosticPath.of("foo/bar")
        assertTrue(fooBar.startsWith(fooBar))
    }

    @Test
    fun emptyPathIsAUniversalPrefix() {
        val emptyPath = FsAgnosticPath.of("")
        assertTrue(FsAgnosticPath.of("foo").startsWith(emptyPath))
        assertTrue(FsAgnosticPath.of("foo/bar").startsWith(emptyPath))
    }

    @Test
    fun startsWith() {
        val foo = FsAgnosticPath.of("foo")
        val fooBar = FsAgnosticPath.of("foo/bar")
        val food = FsAgnosticPath.of("food")
        assertTrue(fooBar.startsWith(foo))
        assertFalse(foo.startsWith(fooBar))
        assertFalse(food.startsWith(foo))
    }

    @Test
    fun invalidSingleDotPath() {
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("'.' contained illegal path component: '.'")
        FsAgnosticPath.of(".")
    }

    @Test
    fun invalidDoubleDotPath() {
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("'..' contained illegal path component: '..'")
        FsAgnosticPath.of("..")
    }

    @Test
    fun invalidSlashOnlyPath() {
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("'/' must be relative but starts with '/'")
        FsAgnosticPath.of("/")
    }

    @Test
    fun invalidAbsolutePath() {
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("'/foo/bar' must be relative but starts with '/'")
        FsAgnosticPath.of("/foo/bar")
    }

    @Test
    fun invalidPathWithTrailingSlash() {
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("'foo/bar/' cannot have a trailing slash")
        FsAgnosticPath.of("foo/bar/")
    }

    @Test
    fun invalidPathWithDoubleSlash() {
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("'foo//bar' contained an empty path component")
        FsAgnosticPath.of("foo//bar")
    }

    @Test
    fun invalidPathWithDotComponent() {
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("'foo/./bar' contained illegal path component: '.'")
        FsAgnosticPath.of("foo/./bar")
    }

    @Test
    fun invalidPathWithDoubleDotComponent() {
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("'foo/../bar' contained illegal path component: '..'")
        FsAgnosticPath.of("foo/../bar")
    }
}
