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
package org.apache.cocoon.sitemap;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The SitemapOutputComponents are responsible for taking the results of a
 * pipeline to the end user.  Current examples are the Serializer and the
 * Reader.  The Sitemap will terminate the pipeline when it encounters the
 * first instance of a <code>SitemapOutputComponent</code>.  Just like the
 * <code>SitemapModelComponent</code>, all implementations of this contract
 * must be pooled for the same reasons.  The sitemap will query the output
 * component for the mime type and whether the sitemap should set the content
 * length in the response.  It will then provide the output component the
 * <code>java.io.OutputStream</code> so you can send the bytes directly to the
 * user.
 * <p>
 * It should be noted that there is no way to access any of the request,
 * response, or context objects within a component that just implements this
 * interface like the Serializer.  The idea is to keep things simple.  All your
 * response attributes should have been already set, and the only
 * responsibility at this point in time is to give the user what he wants--the
 * rendered object (page/image/etc.).
 * </p>
 *
 * @version $Id$
 */
public interface SitemapOutputComponent {

    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     *
     * @param out  The <code>OutputStream</code> target for the rendered results.
     *
     * @throws IOException if the stream can't be used.
     */
    void setOutputStream(OutputStream out) throws IOException;

    /**
     * Obtain the media type for the results being serialized. The
     * returned value is used to set <code>Content-Type</code> header
     * in the response, unless it was overwritten in the sitemap.
     * It helps responsible browsers to identify how to show the
     * information to the user.
     *
     * <p>
     * Example content type value: <code>text/html; charset=utf-8</code>.
     * </p>
     *
     * <p>
     * <strong>Warning:</strong>Microsoft Internet Explorer is a poor
     * netizen and does not always respect this information.  I am talking
     * about Microsoft's InternetExplorer.  It will first try to use the file
     * extension of the resource to determine the mime type, and then if that
     * fails it will fall back to respecting the mime type.  For that reason it
     * is essential that you also practice good netizen habits and make the
     * file extension and the mime type agree.  One example is the PDF
     * document.  In order for Microsoft to treat a result set as a PDF
     * document you must have the url end with ".pdf" as well as set the mime
     * type to "application/pdf".  Internet Explorer will fail if you try to
     * send the document "badhabit.xml?view=pdf" rendered as a PDF document.
     * It is because the file extension ".xml" will be remapped to "text/xml"
     * even if you set the mime type correctly.
     * </p>
     *
     * <p>
     * You may have some incorrectly configured servers that will work for one
     * browser and not the other because the mime-type and file extension do
     * not agree.  The world would be much simpler if all browsers blindly
     * accepted the mime type.  Just be aware of this issue when you are
     * creating your sitemap and serializing your results.
     *
     * @return the media type for the results.
     */
    String getMimeType();

    /**
     * Test if the component needs the content length set.
     * <p>
     * Most types of documents don't really care what the content length is,
     * so it is usually safe to leave the results of this method to false.  It
     * should be noted that the Adobe Acrobat Reader plugin for Microsoft
     * Internet Explorer has a bug that wasn't fixed until version 7.  The bug
     * prevents the PDF document from displaying correctly.  It will look like
     * an empty document or something similar.  So the general rule of thumb
     * for explicitly setting the content length is:
     * </p>
     * <ul>
     * <li>If it is a PDF document, always set content length (might require
     *     the document to be cached to get the number of bytes)</li>
     * <li>If you are writing a Reader and you have the content length, set
     *     it.</li>
     * <li>Otherwise it is safe to return false here.</li>
     * </ul>
     *
     * @return <code>true</code> if the content length needs to be set.
     */
    boolean shouldSetContentLength();
}
