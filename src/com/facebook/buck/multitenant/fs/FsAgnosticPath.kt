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

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder

private val PATH_CACHE: Cache<String, FsAgnosticPath> = CacheBuilder.newBuilder().softValues().build()

/**
 * Prefer this to [java.nio.file.Path] in the multitenant packages. Whereas a [java.nio.file.Path]
 * is associated with a [java.nio.file.FileSystem], [FsAgnosticPath] is basically just a glorified
 * wrapper around a [String] for type safety with `Path`-like methods.
 *
 * This path will always serialize itself using '/' as the path separator, even on Windows.
 */
data class FsAgnosticPath private constructor(private val path: String) {
    companion object {
        /**
         * @param path must be a normalized, relative path.
         */
        fun of(path: String): FsAgnosticPath {
            val cachedPath = PATH_CACHE.getIfPresent(path)
            if (cachedPath != null) {
                return cachedPath
            }

            verifyPath(path)
            val newPath = FsAgnosticPath(path.intern())
            PATH_CACHE.put(path, newPath)
            return newPath
        }
    }

    fun isEmpty(): Boolean {
        return path.isEmpty()
    }

    fun startsWith(prefixPath: FsAgnosticPath): Boolean {
        return if (path.startsWith(prefixPath.path)) {
            if (prefixPath.path.isEmpty() || prefixPath.path.length == path.length) {
                true
            } else {
                path.get(prefixPath.path.length) == '/'
            }
        } else {
            false
        }
    }

    override fun toString(): String {
        return path
    }
}

private fun verifyPath(path: String) {
    if (path == "") {
        return
    }

    if (path.startsWith('/')) {
        throw IllegalArgumentException("'${path}' must be relative but starts with '/'")
    }
    if (path.endsWith('/')) {
        throw IllegalArgumentException("'${path}' cannot have a trailing slash")
    }

    for (component in path.split("/")) {
        if (component == "") {
            throw IllegalArgumentException("'${path}' contained an empty path component")
        }
        if (component == ".") {
            throw IllegalArgumentException("'${path}' contained illegal path component: '.'")
        }
        if (component == "..") {
            throw IllegalArgumentException("'${path}' contained illegal path component: '..'")
        }
    }
}
