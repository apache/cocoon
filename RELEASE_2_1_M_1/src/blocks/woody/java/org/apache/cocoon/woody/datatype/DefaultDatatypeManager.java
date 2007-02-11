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

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.cocoon.woody.datatype.typeimpl.StringTypeBuilder;
import org.apache.cocoon.woody.datatype.typeimpl.LongTypeBuilder;
import org.apache.cocoon.woody.datatype.validationruleimpl.*;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.components.LifecycleHelper;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.HashMap;

/**
 * Implementation of the {@link DatatypeManager} component.
 *
 * <p>It supports an extensible number of datatype and validation rule implementations
 * by delegating the creation of them to {@link DatatypeBuilder}s and {@link ValidationRuleBuilder}s.
 * Currently the list of datatype and validationrule builders is hardcoded, but this will
 * become externally configurable in the future.
 *
 */
public class DefaultDatatypeManager implements DatatypeManager, Initializable, ThreadSafe, Composable {
    private Map typeBuilderMap = new HashMap();
    private Map validationRuleBuilderMap = new HashMap();
    private ComponentManager componentManager;

    public void initialize() throws Exception {
        LifecycleHelper lifecycleHelper = new LifecycleHelper(null, null, componentManager, null, null, null);

        // TODO all the stuff below should come from a configuration file, so that this is extensible

        // Setup the type builders
        Object typeBuilder;

        typeBuilder = new StringTypeBuilder();
        lifecycleHelper.setupComponent(typeBuilder);
        typeBuilderMap.put("string", typeBuilder);

        typeBuilder = new LongTypeBuilder();
        lifecycleHelper.setupComponent(typeBuilder);
        typeBuilderMap.put("long", typeBuilder);


        // Setup the validation rule builders
        Object validationRuleBuilder;

        validationRuleBuilder = new LengthValidationRuleBuilder();
        lifecycleHelper.setupComponent(validationRuleBuilder);
        validationRuleBuilderMap.put("length", validationRuleBuilder);

        validationRuleBuilder = new EmailValidationRuleBuilder();
        lifecycleHelper.setupComponent(validationRuleBuilder);
        validationRuleBuilderMap.put("email", validationRuleBuilder);

        validationRuleBuilder = new ValueCountValidationRuleBuilder();
        lifecycleHelper.setupComponent(validationRuleBuilder);
        validationRuleBuilderMap.put("value-count", validationRuleBuilder);

        validationRuleBuilder = new RangeValidationRuleBuilder();
        lifecycleHelper.setupComponent(validationRuleBuilder);
        validationRuleBuilderMap.put("range", validationRuleBuilder);

        validationRuleBuilder = new AssertValidationRuleBuilder();
        lifecycleHelper.setupComponent(validationRuleBuilder);
        validationRuleBuilderMap.put("assert", validationRuleBuilder);
    }

    public void compose(ComponentManager componentManager) throws ComponentException {
        this.componentManager = componentManager;
    }

    public Datatype createDatatype(Element datatypeElement, boolean arrayType) throws Exception {
        String typeName = DomHelper.getAttribute(datatypeElement, "base");
        DatatypeBuilder builder = (DatatypeBuilder)typeBuilderMap.get(typeName);
        if (builder == null)
            throw new Exception("Unknown datatype '" + typeName + "' specified at " + DomHelper.getLocation(datatypeElement));
        else
            return builder.build(datatypeElement, arrayType, this);
    }

    public ValidationRule createValidationRule(Element validationRuleElement) throws Exception {
        String name  = validationRuleElement.getLocalName();
        ValidationRuleBuilder builder = (ValidationRuleBuilder)validationRuleBuilderMap.get(name);
        if (builder == null)
            throw new Exception("Unknown validation rule + \"" + name + "\" specified at " + DomHelper.getLocation(validationRuleElement));
        else
            return builder.build(validationRuleElement);
    }
}
