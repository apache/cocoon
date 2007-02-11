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

package org.apache.cocoon.jxpath;

import java.util.Enumeration;

import org.apache.cocoon.environment.Context;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.servlet.Util;

/**
 * Implementation of the DynamicPropertyHandler interface that provides
 * access to attributes of a Cocoon Context.
 *
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: CocoonContextHandler.java,v 1.3 2004/03/05 13:02:24 bdelacretaz Exp $
 */
public class CocoonContextHandler implements DynamicPropertyHandler {

    public String[] getPropertyNames(Object context){
        Enumeration e = ((Context)context).getAttributeNames();
        return Util.toStrings(e);
    }

    public Object getProperty(Object context, String property){
        return ((Context)context).getAttribute(property);
    }

    public void setProperty(Object context, String property, Object value){
        ((Context)context).setAttribute(property, value);
    }
}
