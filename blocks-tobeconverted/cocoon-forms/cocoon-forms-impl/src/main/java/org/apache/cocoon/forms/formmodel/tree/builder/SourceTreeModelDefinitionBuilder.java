/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.forms.formmodel.tree.builder;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.formmodel.tree.SourceTreeModelDefinition;
import org.apache.cocoon.forms.formmodel.tree.TreeModelDefinition;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.matching.helpers.WildcardHelper;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.Element;

/**
 * Builds a {@link org.apache.cocoon.forms.formmodel.tree.SourceTreeModel}.
 * 
 * @version $Id$
 */
public class SourceTreeModelDefinitionBuilder extends AbstractLogEnabled
    implements Serviceable, TreeModelDefinitionBuilder {

    private ServiceManager manager;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public TreeModelDefinition build(Element modelElt) throws Exception {
        
        SourceTreeModelDefinition definition = new SourceTreeModelDefinition();
        
        definition.setURL(DomHelper.getAttribute(modelElt, "src"));
        
        Element fileSet = DomHelper.getChildElement(modelElt, FormsConstants.DEFINITION_NS, "fileset");
        if (fileSet != null) {
            definition.setFilePatterns(getPatterns(fileSet, "include"),
                    getPatterns(fileSet, "exclude"));
        }
        
        Element dirSet = DomHelper.getChildElement(modelElt, FormsConstants.DEFINITION_NS, "dirset");
        if (dirSet != null) {
            definition.setDirectoryPatterns(getPatterns(dirSet, "include"),
                    getPatterns(dirSet, "exclude"));
        }
        
        definition.setSourceResolver((SourceResolver)this.manager.lookup(SourceResolver.ROLE));
        
        return definition;
    }
    
    int[][] getPatterns(Element parent, String name) throws Exception {
        Element[] children = DomHelper.getChildElements(parent, FormsConstants.DEFINITION_NS, name);

        if (children.length == 0) {
            return null;
        }

        int[][] result = new int[children.length][];
        for (int i = 0; i < children.length; i++) {
            String pattern = DomHelper.getAttribute(children[i], "pattern");
            result[i] = WildcardHelper.compilePattern(pattern);
        }
        return result;
    }

}
