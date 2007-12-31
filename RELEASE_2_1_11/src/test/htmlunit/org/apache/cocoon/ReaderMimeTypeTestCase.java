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
package org.apache.cocoon;

/**
 * HtmlUnit TestCase for Content type headers returned by Cocoon readers.
 *
 * Each httpRequest is repeated twice to check caching pipeline.
 * Check for text/xml instead of text/html in order not to be fooled
 * by 404 or 500 error pages.
 *
 * @version $Id: $
 */
public class ReaderMimeTypeTestCase
    extends HtmlUnitTestCase
{
    static final String pageurl = "/samples/test/reader-mime-type/";

    /**
     * Check that content-type is <I>not</I> derived from URL extension.
     */
    public void test10()
        throws Exception
    {
        call10();
        call10();
    }

    private void call10()
        throws Exception
    {
        loadResponse(pageurl+"test10.xml");
        assertEquals("Content type should be undefined", "", response.getContentType());
    }

    /**
     * Check content-type derived from map:read/@src.
     */
    public void test20()
        throws Exception
    {
        call20();
        call20();
    }

    private void call20()
        throws Exception
    {
        loadResponse(pageurl+"test20.x20");
        assertEquals("Content type", "text/xml", response.getContentType());
    }

    /**
     * Check content-type derived from map:read/@mime-type.
     */
    public void test30()
        throws Exception
    {
        call30();
        call30();
    }

    private void call30()
        throws Exception
    {
        loadResponse(pageurl+"test30.x30");
        assertEquals("Content type", "text/xml", response.getContentType());
    }

    /**
     * Check content-type derived from map:reader/@mime-type.
     */
    public void test40()
        throws Exception
    {
        call40();
        call40();
    }

    private void call40()
        throws Exception
    {
        loadResponse(pageurl+"test40.x40");
        assertEquals("Content type", "text/xml", response.getContentType());
    }

    /**
     * Check that content-type is <I>not</I> inherited across cocoon:/
     * indirections
     */
    public void test50()
        throws Exception
    {
        call50();
        call50();
    }

    private void call50()
        throws Exception
    {
        loadResponse(pageurl+"test50.xml");
        assertEquals("Content type should be undefined", "", response.getContentType());
    }

    /**
     * Check content-type derived from map:read/@src.
     */
    public void test60()
        throws Exception
    {
        call60();
        call60();
    }

    private void call60()
        throws Exception
    {
        loadResponse(pageurl+"test60.x60");
        assertEquals("Content type", "text/xml", response.getContentType());
    }

    /**
     * Check content-type derived from map:read/@mime-type.
     */
    public void test70()
        throws Exception
    {
        call70();
        call70();
    }

    private void call70()
        throws Exception
    {
        loadResponse(pageurl+"test70.x70");
        assertEquals("Content type", "text/xml", response.getContentType());
    }
}
