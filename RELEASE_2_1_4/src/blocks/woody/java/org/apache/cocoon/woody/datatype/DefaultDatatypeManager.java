/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.woody.datatype;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.CascadingException;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.util.SimpleServiceSelector;
import org.apache.cocoon.woody.datatype.convertor.Convertor;
import org.w3c.dom.Element;

/**
 * Implementation of the {@link DatatypeManager} component.
 *
 * <p>It supports an extensible number of datatype and validation rule implementations
 * by delegating the creation of them to {@link DatatypeBuilder}s and {@link ValidationRuleBuilder}s.
 * Currently the list of datatype and validationrule builders is hardcoded, but this will
 * become externally configurable in the future.
 * 
 * @version $Id: DefaultDatatypeManager.java,v 1.7 2004/02/11 09:53:43 antonio Exp $
 *
 */
public class DefaultDatatypeManager extends AbstractLogEnabled implements DatatypeManager, ThreadSafe, Serviceable,
        Configurable, Initializable, Disposable {
    private SimpleServiceSelector typeBuilderSelector;
    private SimpleServiceSelector validationRuleBuilderSelector;
    private ServiceManager serviceManager;
    private Configuration configuration;

    public void configure(Configuration configuration) throws ConfigurationException {
        this.configuration = configuration;
    }

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
    }

    public void initialize() throws Exception {
        typeBuilderSelector = new SimpleServiceSelector("datatype", DatatypeBuilder.class);
        typeBuilderSelector.service(serviceManager);
        typeBuilderSelector.configure(configuration.getChild("datatypes"));

        validationRuleBuilderSelector = new SimpleServiceSelector("validation-rule", ValidationRuleBuilder.class);
        validationRuleBuilderSelector.service(serviceManager);
        validationRuleBuilderSelector.configure(configuration.getChild("validation-rules"));

        configuration = null;
    }

    public Datatype createDatatype(Element datatypeElement, boolean arrayType) throws Exception {
        String typeName = DomHelper.getAttribute(datatypeElement, "base");
        DatatypeBuilder builder = null;
        try {
            builder = (DatatypeBuilder)typeBuilderSelector.select(typeName);
        } catch (ServiceException e) {
            throw new CascadingException("Unknown datatype '" + typeName + "' specified at " + DomHelper.getLocation(datatypeElement), e);
        }
        return builder.build(datatypeElement, arrayType, this);
    }

    public ValidationRule createValidationRule(Element validationRuleElement) throws Exception {
        String name  = validationRuleElement.getLocalName();
        ValidationRuleBuilder builder = null;
        try {
            builder = (ValidationRuleBuilder)validationRuleBuilderSelector.select(name);
        } catch (ServiceException e) {
            throw new CascadingException("Unknown validation rule \"" + name + "\" specified at " + DomHelper.getLocation(validationRuleElement), e);
        }
        return builder.build(validationRuleElement);
    }

    public Convertor createConvertor(String dataTypeName, Element convertorElement) throws Exception {
        DatatypeBuilder datatypeBulder = (DatatypeBuilder)typeBuilderSelector.select(dataTypeName);
        return datatypeBulder.buildConvertor(convertorElement);
    }

    public void dispose() {
        typeBuilderSelector.dispose();
        validationRuleBuilderSelector.dispose();
    }
}
