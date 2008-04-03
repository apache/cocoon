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
package org.apache.cocoon.components.flow;

import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * Access to continuation data for monitoring applications
 */
public class WebContinuationDataBean {

    private static final String TYPE_JAVAFLOW = "javaflow";
    private static final String TYPE_FLOWSCRIPT = "flowscript";
    private static final String HAS_EXPIRED_NO = "no";
    private static final String HAS_EXPIRED_YES = "yes";

    private WebContinuation wc;
    private Format formatter = FastDateFormat.getInstance("HH:mm:ss");
    private List _children = new ArrayList();

    public WebContinuationDataBean(WebContinuation wc) {
        this.wc = wc;
        for (Iterator it = wc.getChildren().iterator(); it.hasNext();) {
            WebContinuationDataBean child = new WebContinuationDataBean(
                    (WebContinuation) it.next());
            this._children.add(child);
        }
    }

    public String getId() {
        return wc.getId();
    }

    public String getLastAccessTime() {
        return formatter.format(new Date(wc.getLastAccessTime()));
    }

    public String getInterpreterId() {
        return wc.getInterpreterId();
    }

    public String getTimeToLiveInMinutes() {
        return Long.toString(wc.getTimeToLive() / 1000 / 60);
    }

    public String getTimeToLive() {
        return Long.toString(wc.getTimeToLive());
    }

    public String getExpireTime() {
        return formatter.format(new Date(wc.getLastAccessTime() + wc.getTimeToLive()));
    }

    public String hasExpired() {
        if (wc.hasExpired()) {
            return HAS_EXPIRED_YES;
        }
        return HAS_EXPIRED_NO;
    }

    public String getType() {
        if (wc.getUserObject().getClass().getName().indexOf(
                "FOM_WebContinuation") > -1) {
            return TYPE_FLOWSCRIPT;
        }
        return TYPE_JAVAFLOW;
    }

    public List get_children() {
        return this._children;
    }

}
