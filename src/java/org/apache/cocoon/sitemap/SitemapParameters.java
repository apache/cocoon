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
package org.apache.cocoon.sitemap;

import org.apache.avalon.framework.parameters.Parameters;

/**
 * Extension to the Avalon Parameters
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SitemapParameters.java,v 1.1 2004/03/07 18:56:17 cziegeler Exp $
 */
public class SitemapParameters extends Parameters {
    
    protected String statementLocation;
    
    /**
    public String getParameterLocation(String name) {
        return null;   
    }
    */
    public String getStatementLocation(String name) {
        return this.statementLocation;   
    }
    
    public void setStatementLocation(String value) {
        this.statementLocation = value;   
    }
}
