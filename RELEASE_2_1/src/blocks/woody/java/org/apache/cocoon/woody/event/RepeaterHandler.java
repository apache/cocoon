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
package org.apache.cocoon.woody.event;

import org.apache.cocoon.woody.FormHandler;
import org.apache.cocoon.woody.event.ActionEvent;
import org.apache.cocoon.woody.formmodel.Form;
import org.apache.cocoon.woody.formmodel.Repeater;

/**
 * This class provides a delegate woody FormHandler to be reused accross your
 * repeaters.
 * 
 * <p>Typical use is delegating it from your own repeater specific FormHandler
 * to provide classic add-row/remove-row features to the repeater on your form.
 * 
 * <p>In the model you would get:
 *
 * <pre>
 * &lt;wd:repeater id="myrows" &gt;
 *     &lt;wd:field id="name" required="true"&gt;
 *         &lt;wd:datatype base="string" /&gt;
 *         &lt;wd:label&gt;Name&lt;/wd:label&gt;
 *     &lt;/wd:field&gt;
 *     &lt;wd:booleanfield id="select"&gt;
 *         &lt;wd:label&gt;Select&lt;/wd:label&gt;
 *     &lt;/wd:booleanfield&gt;
 * &lt;/wd:repeater&gt;
 * 
 * &lt;wd:button id="removerows" action-command="remove-selected-rows"&gt;
 *     &lt;wd:label&gt;DELETE&lt;/wd:label&gt;
 * &lt;/wd:button&gt;
 * 
 * &lt;wd:button id="addrows" action-command="add-rows"&gt;
 *     &lt;wd:label&gt;ADD&lt;/wd:label&gt;
 * &lt;/wd:button&gt;
 * </pre>
 *
 * For which you can easily use this Handler by siimply passing
 * <code>FormHandler fh = new RepeaterHandler("myrows","add-rows",
 *                                   "remove-selected-rows","select")</code>
 * 
 * to the FormContext.
 * 
 */
public class RepeaterHandler implements FormHandler {

    private Form form;
    private final String repeaterName;
    private final String addCommand;
    private final String removeCommand;
    private final String selectId;

    /**
     * Constructs RepeaterHandler to deal with the add/remove actions of 
     * your form.
     * 
     * @param repeaterName  the id of your repeater as found in the 
     *                      wd:repeater/@id
     * @param addCommand    the action-command used to recognise the 'add' 
     *                      event as found in the wd:button/@action-command
     * @param removeCommand the action-command used to recognise the 'remove' 
     *                      event as found in the wd:button/@action-command
     * @param selectId      the booleanfield id in each row that marks the 
     *                      rows to be deleted as found in wd:booleanfield/@id 
     */
    public RepeaterHandler(
        String repeaterName,
        String addCommand,
        String removeCommand,
        String selectId) {
        this.repeaterName = repeaterName;
        this.addCommand = addCommand;
        this.removeCommand = removeCommand;
        this.selectId = selectId;
    }

    /**
     * @see org.apache.cocoon.woody.FormHandler#setup(org.apache.cocoon.woody.formmodel.Form)
     */
    public void setup(Form form) {
        this.form = form;
    }

    /**
     * @see org.apache.cocoon.woody.FormHandler#handleActionEvent(org.apache.cocoon.woody.event.ActionEvent)
     */
    public void handleActionEvent(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();

        if (command.equals(this.addCommand)) {
            Repeater repeater =
                (Repeater) form.getWidget(this.repeaterName);
            repeater.addRow();
        } else if (command.equals(this.removeCommand)) {
            removeSelectedRows();
        }
    }

    private void removeSelectedRows() {
        Repeater repeater = (Repeater) form.getWidget(this.repeaterName);
        for (int i = repeater.getSize() - 1; i >= 0; i--) {
            boolean selected =
                ((Boolean) repeater
                    .getRow(i)
                    .getWidget(this.selectId)
                    .getValue())
                    .booleanValue();
            if (selected)
                repeater.removeRow(i);
        }
    }
}
