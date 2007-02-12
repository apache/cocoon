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
package org.apache.cocoon.portal.acting;

import java.util.Collections;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.NetUtils;

/**
 * This action builds correct urls. It uses the src parameter as the base url
 * and adds all sitemap parameters as request parameters.
 *
 * @version $Id$
 */
public class URLAction
    extends ServiceableAction
    implements ThreadSafe {

    /**
     * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters par)
    throws Exception {
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Portal URL Action called with resolver="+resolver+
                                   ", objectModel="+objectModel+
                                   ", source="+source+
                                   ", par="+par);
        }

        StringBuffer buffer = new StringBuffer(source);
        boolean hasParams = (source.indexOf('?') != -1);
        final String[] names = par.getNames();
        for( int i=0; i<names.length; i++ ) {
            final String key = names[i];
            final String value = par.getParameter(key);
            if ( hasParams ) {
                buffer.append('&');
            } else {
                buffer.append('?');
                hasParams = true;
            }
            buffer.append(key);
            buffer.append('=');
            buffer.append(NetUtils.encode(value, "utf-8"));
        }
        final Map result = Collections.singletonMap("url", buffer.toString());
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Portal URL Action provides url=" + buffer.toString());
        }

        return result;
    }
}
