/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto.om.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.pluto.om.portlet.ContentType;
import org.apache.pluto.util.StringUtils;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: ContentTypeImpl.java,v 1.2 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class ContentTypeImpl
implements ContentType, java.io.Serializable, Support {

    private String contentType = null;
    private Collection portletModes = new ArrayList();

    private Collection castorPortletModes = new ArrayList();

    public ContentTypeImpl()
    {
    }

    // ContentType implementation.

    public String getContentType()
    {
        return contentType;
    }

    public Iterator getPortletModes()
    {
        return portletModes.iterator();
    }
    
    // Support implementation.
    public void postLoad(Object parameter) throws Exception
    {
        portletModes.clear();
        Iterator iterator = castorPortletModes.iterator();
        while (iterator.hasNext()) {
            String name = (String)iterator.next();
            portletModes.add(new javax.portlet.PortletMode(name));
        }
        if (!portletModes.contains(javax.portlet.PortletMode.VIEW)) {
            portletModes.add(javax.portlet.PortletMode.VIEW);
        }
    }

    public void preBuild(Object parameter) throws Exception
    {
    }

    public void postBuild(Object parameter) throws Exception
    {
    }

    public void preStore(Object parameter) throws Exception
    {
        castorPortletModes.clear();
        Iterator iterator = portletModes.iterator();
        while (iterator.hasNext()) {
            javax.portlet.PortletMode mode = (javax.portlet.PortletMode)iterator.next();
            castorPortletModes.add(mode.toString());
        }
    }

    public void postStore(Object parameter) throws Exception
    {
    }

    // additional methods.

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public void setPortletModes(Collection portletModes)
    {
        this.portletModes = portletModes;
    }

    public boolean supportsPortletMode(javax.portlet.PortletMode portletMode)
    {
    	return portletModes.contains(portletMode);
    }	
    
    public String toString()
    {
        return toString(0);
    }

    public String toString(int indent)
    {
        StringBuffer buffer = new StringBuffer(50);
        StringUtils.newLine(buffer,indent);
        buffer.append(getClass().toString()); buffer.append(":");
        StringUtils.newLine(buffer,indent);
        buffer.append("{");
        StringUtils.newLine(buffer,indent);
        buffer.append("contentType='"); buffer.append(contentType); buffer.append("'");
        int i = 0;
        Iterator iterator = portletModes.iterator();
        while (iterator.hasNext()) {
            StringUtils.newLine(buffer,indent);
            buffer.append("portletMode["); 
            buffer.append(i++); 
            buffer.append("]='");
            buffer.append(iterator.next());
            buffer.append("'");
        }
        StringUtils.newLine(buffer,indent);
        buffer.append("}");
        return buffer.toString();
    }

    public Collection getCastorPortletModes()
    {
        return castorPortletModes;
    }

}
