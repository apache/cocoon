/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.core.logging;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.core.Settings;

/**
 * This is an extension of the default context implementation.
 * It first looks into the settings object and only if the key
 * is not found there, it delegates to the parent.
 * 
 * @version $Id$
 * @since 2.2
 */
public class SettingsContext extends DefaultContext {

    private final Settings settings;

    public SettingsContext(Context parentContext, Settings s) {
        super(parentContext);
        this.settings = s;
    }

    public SettingsContext(Settings s) {
        super();
        this.settings = s;
    }

    /**
     * @see org.apache.avalon.framework.context.Context#get(java.lang.Object)
     */
    public Object get(Object name) throws ContextException {
        if ( this.settings.getProperty(name.toString()) != null ) {
            return this.settings.getProperty(name.toString());
        }
        return super.get(name);
    }

}
