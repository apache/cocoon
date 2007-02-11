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
package org.apache.cocoon.portlet;

import org.apache.log.format.Formatter;
import org.apache.log.output.AbstractOutputTarget;

import javax.portlet.PortletContext;

/**
 * Log output target for JSR-168 Portlet context
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PortletOutputLogTarget.java,v 1.2 2004/03/05 13:02:17 bdelacretaz Exp $
 */
public class PortletOutputLogTarget extends AbstractOutputTarget {

    private PortletContext context;

    public PortletOutputLogTarget(PortletContext context) {
        this.context = context;
        open();
    }

    public PortletOutputLogTarget(PortletContext context, Formatter formatter) {
        super(formatter);
        this.context = context;
        open();
    }

    protected void write(String message) {
        PortletContext context = this.context;
        if (context != null) {
            synchronized (context) {
                context.log(message);
            }
        }
    }

    public synchronized void close() {
        super.close();
        this.context = null;
    }
}
