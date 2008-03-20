/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.tools.it;

import com.gargoylesoftware.htmlunit.SubmitMethod;
import com.gargoylesoftware.htmlunit.WebResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;


class HttpClientResponse implements WebResponse {
    private URL url;

    private HttpMethodBase method;

    private int statusCode;

    private long loadTime;

    HttpClientResponse(URL url, HttpMethodBase method) throws IOException {
        long t0 = System.currentTimeMillis();
        this.url = url;
        this.method = method;
        HttpClient client = new HttpClient();
        statusCode = client.executeMethod(method);
        long t1 = System.currentTimeMillis();
        this.loadTime = t1 - t0;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return method.getStatusText();
    }

    public String getContentType() {
        return method.getResponseHeader("Content-type").getValue();
    }

    public String getContentAsString() {
        try {
            return method.getResponseBodyAsString();
        } catch (IOException ex) {
            return null;
        }
    }

    public InputStream getContentAsStream() throws IOException {
        return method.getResponseBodyAsStream();
    }

    public URL getUrl() {
        return url;
    }

    public String getResponseHeaderValue(String headerName) {
        return method.getResponseHeader(headerName).getValue();
    }

    public long getLoadTimeInMilliSeconds() {
        return loadTime;
    }

    public String getContentCharSet() {
        return method.getResponseCharSet();
    }

    public byte[] getResponseBody() {
        try {
            return method.getResponseBody();
        } catch (IOException ex) {
            return null;
        }
    }

    public SubmitMethod getRequestMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    public List getResponseHeaders() {
        // TODO Auto-generated method stub
        return null;
    }
}
