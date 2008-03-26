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
package org.apache.cocoon.portal.services.impl;

import java.util.Iterator;

import org.apache.cocoon.configuration.PropertyHelper;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.om.SkinDescription;
import org.apache.cocoon.portal.services.VariableResolver;
import org.apache.cocoon.portal.util.AbstractBean;

/**
 * This is the default implementation of the {@link VariableResolver}.
 * It uses the settings object to replace variables.
 *
 * @version $Id$
 */
public class DefaultVariableResolver
    extends AbstractBean
    implements VariableResolver {

    /** TODO - we should provide a way to query the whole object model. */
    protected static final String SKINPATH = "{skinpath}";

    /** The settings object. */
    protected Settings settings;

    public void setSettings(final Settings s) {
        this.settings = s;
    }

    /**
     * @see org.apache.cocoon.portal.services.VariableResolver#compile(java.lang.String)
     */
    public CompiledExpression compile(String expression) {
        return new CompiledExpressionImpl(PropertyHelper.replace(expression, this.settings), this.settings, this.portalService);
    }

    /**
     * @see org.apache.cocoon.portal.services.VariableResolver#resolve(java.lang.String)
     */
    public String resolve(String expression) {
        return this.compile(expression).resolve();
    }

    protected static final class CompiledExpressionImpl implements CompiledExpression {

        final protected String value;

        final int insertPos;

        final PortalService portalService;

        final Settings settings;

        public CompiledExpressionImpl(final String v, final Settings s, final PortalService ps) {
            this.insertPos = v.indexOf(SKINPATH);
            if ( this.insertPos != -1 ) {
                if ( this.insertPos == 0 ) {
                    this.value = v.substring(SKINPATH.length());
                } else {
                    this.value = v.substring(0, insertPos) + v.substring(insertPos + SKINPATH.length());
                }
            } else {
                this.value = v;
            }
            this.portalService = ps;
            this.settings = s;
        }

        /**
         * @see org.apache.cocoon.portal.services.VariableResolver.CompiledExpression#resolve()
         */
        public String resolve() {
            if ( insertPos == -1 ) {
                return this.value;
            }
            // TODO - Skin detection should be moved to the portal and user service.
            final String skinName = this.settings.getProperty("skin");
            // find the correct skin
            SkinDescription desc = null;
            final Iterator i = this.portalService.getSkinDescriptions().iterator();
            while ( i.hasNext() && desc == null ) {
                final SkinDescription current = (SkinDescription)i.next();
                if ( current.getName().equals(skinName) ) {
                    desc = current;
                }
            }
            String skinPath = "";
            if ( desc != null ) {
                skinPath = desc.getBasePath().getAbsolutePath();
            }
            if ( insertPos == 0 ) {
                return skinPath + this.value;
            }
            return this.value.substring(0, this.insertPos) + skinPath + this.value.substring(this.insertPos);
        }
    }
}
