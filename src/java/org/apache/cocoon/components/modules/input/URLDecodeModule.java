/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.modules.input;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.modules.input.AbstractInputModule;
import org.apache.cocoon.util.NetUtils;

/**
 * This module provides functionality for converting a String from the
 * application/x-www-form-urlencoded MIME format. It is useful for example for
 * calling remote services: <br/>
 * &lt;map:generate src="http://remote/page?param1={url-encode:{request-param:param1}}"/&gt;<br/>
 * Module configuration takes only one configuration parameter:
 * "encoding" which is a target string encoding. This is utf-8 by default.
 */
public final class URLDecodeModule extends AbstractInputModule
                                   implements ThreadSafe {

    public Object getAttribute(String name,
                               Configuration modeConf,
                               Map objectModel) throws ConfigurationException {
        if (name == null) {
            return null;
        }

        String encoding = (String) this.settings.get("encoding", "utf-8");
        try {
            return NetUtils.decode(name, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new ConfigurationException("URLDecodeModule, invalid encoding: " + encoding);
        }
    }
}
