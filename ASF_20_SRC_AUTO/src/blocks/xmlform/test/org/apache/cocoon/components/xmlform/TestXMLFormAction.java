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

package org.apache.cocoon.components.xmlform;

import org.apache.cocoon.acting.AbstractXMLFormAction;
import org.apache.cocoon.components.xmlform.Form;
import org.apache.cocoon.components.xmlform.FormListener;

import java.util.Map;

/**
 *
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: TestXMLFormAction.java,v 1.3 2004/03/05 13:02:38 bdelacretaz Exp $
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

