/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout.impl;

import org.apache.cocoon.portal.layout.AbstractLayout;
import org.apache.cocoon.portal.layout.Layout;

/**
 * A link layout references another layout to be used instead. The reference
 * can be changed using events.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * 
 * @version CVS $Id: LinkLayout.java,v 1.4 2004/04/25 20:09:34 haul Exp $
 */
public class LinkLayout extends AbstractLayout implements Layout {

    protected String linkedLayoutKey;
    protected String linkedLayoutId;

    public void setLayoutId(String layoutId) {
        this.linkedLayoutId = layoutId;
    }

    public String getLayoutId() {
        return this.linkedLayoutId;
    }

    public String getLayoutKey() {
        return linkedLayoutKey;
    }

    public void setLayoutKey(String key) {
        linkedLayoutKey = key;
    }

}
