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
package org.apache.cocoon.components.treeprocessor.variables;

import java.io.Reader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.template.expression.AbstractStringTemplateParser;
import org.apache.commons.io.IOUtils;

public class LegacySitemapStringTemplateParser extends AbstractStringTemplateParser {
    
    private ServiceManager serviceManager;
    
    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public List parseSubstitutions(Reader in) throws Exception {
        LinkedList substitutions = new LinkedList();
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer);
        String expression = writer.toString();
        substitutions.add(new SitemapExpressionSubstitution(expression, serviceManager));
        return substitutions;
    }
    
public final class SitemapExpressionSubstitution implements Subst {
        
        private PreparedVariableResolver resolver;
        
        public SitemapExpressionSubstitution(String expression, ServiceManager serviceManager) throws PatternException {
            try {
                this.resolver = (PreparedVariableResolver)serviceManager.lookup(PreparedVariableResolver.ROLE);
                this.resolver.setExpression(expression);
            } catch (ServiceException e) {
                throw new PatternException("Could not obtain PreparedVariableResolver", e);
            }
        }

        public Boolean getBooleanValue(ObjectModel objectModel) throws Exception {
            throw new UnsupportedOperationException();
        }

        public Object getCompiledExpression() {
            throw new UnsupportedOperationException();
        }

        public int getIntValue(ObjectModel objectModel) throws Exception {
            throw new UnsupportedOperationException();
        }

        public Iterator getIterator(ObjectModel objectModel) throws Exception {
            throw new UnsupportedOperationException();
        }

        public Object getNode(ObjectModel objectModel) throws Exception {
            throw new UnsupportedOperationException();
        }

        public Number getNumberValue(ObjectModel objectModel) throws Exception {
            throw new UnsupportedOperationException();
        }

        public String getRaw() {
            throw new UnsupportedOperationException();
        }

        public String getStringValue(ObjectModel objectModel) throws Exception {
            throw new UnsupportedOperationException();
        }
        
        public String getStringValue(InvokeContext context, Map oldObjectModel) throws PatternException {
            return resolver.resolve(context, oldObjectModel);
        }

        public Object getValue(ObjectModel objectModel) throws Exception {
            throw new UnsupportedOperationException();
        }

        public void setLenient(Boolean lenient) {
            //ignore
        }   
    }

}
