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
package org.apache.cocoon.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

/**
 * This action can be used to set information in either the object model,
 * the request or the session.
 * 
 * <p>All parameters set for this action are set in the according location
 * whereas the parameter name is the key and the value of the parameter
 * will be set as a string value for this key.
 *
 * @cocoon.sitemap.component.documentation
 * This action can be used to set information in either the object model,
 * the request or the session.
 *
 * @version $Id$
 */
public class SetterAction
    extends AbstractAction
    implements Parameterizable, ThreadSafe {

    public static final int MODE_OBJECT_MODEL = 1;
    public static final int MODE_REQUEST_ATTR = 2;
    public static final int MODE_SESSION_ATTR = 3;
    
    public static final String MODEDEF_OBJECT_MODEL = "object-model";
    public static final String MODEDEF_REQUEST_ATTR = "request-attribute";
    public static final String MODEDEF_SESSION_ATTR = "session-attribute";

    protected int mode = MODE_OBJECT_MODEL;

    /**
     * @see Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     * @throws ParameterException
     */
    public void parameterize(Parameters params) 
    throws ParameterException {
        String modeDef = params.getParameter("mode", null);
        if ( modeDef != null ) {
            if ( MODEDEF_OBJECT_MODEL.equals(modeDef) ) {
                this.mode = MODE_OBJECT_MODEL;
            } else if ( MODEDEF_REQUEST_ATTR.equals(modeDef) ) {
                this.mode = MODE_REQUEST_ATTR;
            } else if ( MODEDEF_SESSION_ATTR.equals(modeDef) ) {
                this.mode = MODE_SESSION_ATTR;
            } else {
                throw new ParameterException("Unknown mode: " + this.mode);
            }
        }
    }

    /**
     * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters parameters)
    throws Exception {
        final String[] names = parameters.getNames();
        for(int i = 0; i < names.length; i++) {
            final String name = names[i];
            if ( this.mode == MODE_OBJECT_MODEL ) {
                objectModel.put(name, parameters.getParameter(name));                
            } else if ( this.mode == MODE_REQUEST_ATTR ) {
                ObjectModelHelper.getRequest(objectModel).setAttribute(name, parameters.getParameter(name));                
            } else if ( this.mode == MODE_SESSION_ATTR ) {
                ObjectModelHelper.getRequest(objectModel).getSession().setAttribute(name, parameters.getParameter(name));                
            }
        }
        return EMPTY_MAP;
    }
}
