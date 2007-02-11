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
package org.apache.cocoon.woody.samples;

import org.apache.cocoon.woody.acting.AbstractWoodyAction;
import org.apache.cocoon.woody.formmodel.Form;
import org.apache.cocoon.woody.formmodel.Repeater;
import org.apache.cocoon.woody.formmodel.Field;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.avalon.framework.parameters.Parameters;

import java.util.Map;
import java.util.Date;

/**
 * An action that creates an instance of a specific example form included with Woody,
 * and adds some rows to its repeater widget. This example is meant to illustrate
 * how you can prepopulate a Form instance before its initial display.
 * 
 * @version $Id: InitForm1Action.java,v 1.5 2004/02/11 10:43:32 antonio Exp $
 */
public class InitForm1Action extends AbstractWoodyAction {
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters)
            throws Exception {
        String formSource = parameters.getParameter("form-definition");
        String formAttribute = parameters.getParameter("attribute-name");

        Form form = formManager.createForm(resolver.resolveURI(formSource));

        Field birthDate = (Field)form.getWidget("birthdate");
        birthDate.setValue(new Date());

        Repeater repeater = (Repeater)form.getWidget("contacts");
        repeater.addRow();
        Field field = (Field)repeater.getWidget(0, "firstname");
        field.setValue("Jules");

        repeater.addRow();
        field = (Field)repeater.getWidget(1, "firstname");
        field.setValue("Lucien");

        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(formAttribute, form);

        return null;
    }
}
