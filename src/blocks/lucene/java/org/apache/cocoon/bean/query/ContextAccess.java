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

package org.apache.cocoon.bean.query;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;


/**
 * A component to help you get the Avalon Context.
 * Probably only a temporary solution to getting this from within a FlowScript.
 * cocoon.createObject (Packages.org.apache.cocoon.bean.query.ContextAccess);
 *
 * @version CVS $Id: ContextAccess.java,v 1.1 2004/06/21 10:00:20 jeremy Exp $
 */

public class ContextAccess implements Contextualizable {

    private Context avalonContext;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context avalonContext) {
        this.avalonContext = avalonContext;
    }

    /**
     * Return the Avalon Context
     * @return The context object
     */
    public Context getAvalonContext() {
        return this.avalonContext;
    }
}
