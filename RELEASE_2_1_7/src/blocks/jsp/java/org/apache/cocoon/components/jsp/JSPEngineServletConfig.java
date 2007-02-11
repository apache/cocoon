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
package org.apache.cocoon.components.jsp;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * Stub implementation of ServletConfig.
 */
public final class JSPEngineServletConfig implements ServletConfig {

    private final ServletContext context;
    private final String name;
    
    public JSPEngineServletConfig(ServletContext context, String name) {
        this.context = context;
        this.name = name;
    }
    public String getServletName() { return name; }
    public Enumeration getInitParameterNames() { return context.getInitParameterNames(); }
    public ServletContext getServletContext() { return context; }
    public String getInitParameter(String name) { return context.getInitParameter(name); }

}
