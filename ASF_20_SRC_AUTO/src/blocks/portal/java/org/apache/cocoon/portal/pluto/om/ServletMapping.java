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
package org.apache.cocoon.portal.pluto.om;


/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: ServletMapping.java,v 1.2 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class ServletMapping implements java.io.Serializable {

    private String id = null;
    private String servletName = null;
    private String urlPattern = null;

    public ServletMapping()
    {
    }

    // additional methods.

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getServletName()
    {
        return servletName;
    }

    public void setServletName(String servletName)
    {
        this.servletName = servletName;
    }

    public String getUrlPattern()
    {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern)
    {
        this.urlPattern = urlPattern;
    }

}
