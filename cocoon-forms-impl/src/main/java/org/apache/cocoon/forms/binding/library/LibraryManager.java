/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.binding.library;

/**
 * The work interface for the LibraryManager, the class that
 * manages all used form binding library definitions so they can be shared
 * between forms.
 *
 * @version $Id$
 */
public interface LibraryManager {

    String ROLE = LibraryManager.class.getName();

    /**
     * Create new instance of the {@link Library}.
     * @return new library instance
     */
    Library newLibrary();

    /**
     * Loads (and caches) a library from specified source URI.
     *
     * @param sourceURI URI of the library source.
     * @return Library loaded from the source URI.
     */
    Library load(String sourceURI) throws LibraryException;

    /**
     * Loads (and caches) a library from specified source URI, resolved relative
     * to the base URI.
     *
     * @param sourceURI Relative URI of the library source.
     * @param baseURI Base URI of the library source.
     * @return Library loaded from the source URI.
     */
    Library load(String sourceURI, String baseURI) throws LibraryException;

    /**
     * Get the cached instance of the library loaded from the specified source
     * URI.
     *
     * @param sourceURI URI of the library source.
     * @return Cached instance of the library, or null if it was not loaded.
     */
    Library get(String sourceURI) throws LibraryException;

    /**
     * Get the cached instance of the library loaded from the specified source
     * URI, resolved relative to the base URI.
     *
     * @param sourceURI Relative URI of the library source.
     * @param baseURI Base URI of the library source.
     * @return Cached instance of the library, or null if it was not loaded.
     */
    Library get(String sourceURI, String baseURI) throws LibraryException;
}
