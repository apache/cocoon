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

package org.apache.cocoon.components.xmlform;

import org.apache.cocoon.acting.AbstractXMLFormAction;
import org.apache.cocoon.components.xmlform.Form;
import org.apache.cocoon.components.xmlform.FormListener;

import java.util.Map;

/**
 *
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: TestXMLFormAction.java,v 1.2 2003/04/26 12:09:44 stephan Exp $
 */
public class TestXMLFormAction extends AbstractXMLFormAction
  implements FormListener {

    // different form views
    // participating in the wizard

    final String VIEW_START = "start";

    final String VIEW_FIRST = "view1";

    final String VIEW_SECOND = "view2";

    // action commands used in the wizard

    final String CMD_START = "start";

    final String CMD_NEXT = "next";

    final String CMD_PREV = "prev";

    public Map prepare() {
        if (getCommand()==null) {
            return page(VIEW_START);
        } else if (getCommand().equals(CMD_START)) {
            return page(VIEW_FIRST);
        } else if (Form.lookup(getObjectModel(), getFormId())==null) {
            return page(VIEW_START);
        }

        return super.PREPARE_RESULT_CONTINUE;
    }

    public Map perform() {
        TestBean model = (TestBean) getForm().getModel();

        model.incrementCount();

        if ((getCommand().equals(CMD_NEXT)) &&
            (getForm().getViolations()!=null)) {
            return page(getFormView());
        } else {
            getForm().clearViolations();

            // get the user submitted command (through a submit button)
            String command = getCommand();
            // get the form view which was submitted
            String formView = getFormView();

            // apply state machine (flow control) rules
            if (formView.equals(VIEW_FIRST)) {
                if (command.equals(CMD_NEXT)) {
                    return page(VIEW_SECOND);
                } else if (command.equals(CMD_PREV)) {
                    return page(VIEW_START);
                }
            } else if (formView.equals(VIEW_SECOND)) {
                if (command.equals(CMD_NEXT)) {
                    return page(VIEW_START);
                } else if (command.equals(CMD_PREV)) {
                    return page(VIEW_FIRST);
                }
            }
        }

        return page(VIEW_START);
    }

    public void reset(Form form) {
        return;
    }

    public boolean filterRequestParameter(Form form, String parameterName) {
        return false;
    }
}

