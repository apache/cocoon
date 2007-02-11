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
package org.apache.cocoon.woody.binding;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.woody.datatype.convertor.Convertor;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

/**
 * Simple binding for multi fields: on save, first deletes the target data
 * before recreating it from scratch.
 *
 * @version CVS $Id: MultiValueJXPathBinding.java,v 1.2 2004/02/17 13:54:28 joerg Exp $
 */
public class MultiValueJXPathBinding extends JXPathBindingBase {

    private final String multiValueId;
    private final String multiValuePath;
    private final String rowPath;
    private final JXPathBindingBase updateBinding;
    private final Convertor convertor;
    private final Locale convertorLocale;

    public MultiValueJXPathBinding(
        JXPathBindingBuilderBase.CommonAttributes commonAtts, String multiValueId,
        String multiValuePath, String rowPath,
        JXPathBindingBase[] updateBindings, Convertor convertor, Locale convertorLocale) {
        super(commonAtts);
        this.multiValueId = multiValueId;
        this.multiValuePath = multiValuePath;
        this.rowPath = rowPath;
        this.updateBinding = new ComposedJXPathBindingBase(JXPathBindingBuilderBase.CommonAttributes.DEFAULT, updateBindings);
        this.convertor = convertor;
        this.convertorLocale = convertorLocale;
    }

    public void doLoad(Widget frmModel, JXPathContext jctx) throws BindingException {
        Widget widget = frmModel.getWidget(this.multiValueId);
        if (widget == null) {
            throw new BindingException("The widget with the ID [" + this.multiValueId
                    + "] referenced in the binding does not exist in the form definition.");
        }

        // Move to multi value context
        Pointer ptr = jctx.getPointer(this.multiValuePath);
        if (ptr.getNode() != null) {
            // There are some nodes to load from

            JXPathContext multiValueContext = jctx.getRelativeContext(ptr);
            // build a jxpath iterator for pointers
            Iterator rowPointers = multiValueContext.iterate(this.rowPath);

            LinkedList list = new LinkedList();

            while (rowPointers.hasNext()) {
                Object value = rowPointers.next();

                if (value != null && convertor != null) {
                    if (value instanceof String) {
                        value = convertor.convertFromString((String)value, convertorLocale, null);
                    } else {
                        getLogger().warn("Convertor ignored on backend-value which isn't of type String.");
                    }
                }

                list.add(value);
            }

            widget.setValue(list.toArray());
        }

        if (getLogger().isDebugEnabled())
            getLogger().debug("done loading values " + toString());
    }

    public void doSave(Widget frmModel, JXPathContext jctx) throws BindingException {
        Widget widget = frmModel.getWidget(this.multiValueId);
        Object[] values = (Object[])widget.getValue();

        JXPathContext multiValueContext = jctx.getRelativeContext(jctx.createPath(this.multiValuePath));
        // Delete all that is already present
        multiValueContext.removeAll(this.rowPath);

        boolean update = false;

        if (values != null) {
            // first update the values
            for (int i = 0; i < values.length; i++) {
                String path = this.rowPath + '[' + (i+1) + ']';
                Pointer rowPtr = multiValueContext.createPath(path);

                Object value = values[i];
                if (value != null && convertor != null) {
                    value = convertor.convertToString(value, convertorLocale, null);
                }

                rowPtr.setValue(value);
            }

            // now perform any other bindings that need to be performed when the value is updated
            this.updateBinding.saveFormToModel(frmModel, multiValueContext);

            update = true;
        }


        if (getLogger().isDebugEnabled()) {
            getLogger().debug("done saving " + toString() + " -- on-update == " + update);
        }



    }

    public String toString() {
        return "MultiValueJXPathBinding [widget=" + this.multiValueId + ", xpath=" + this.multiValuePath + "]";
    }

    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        this.updateBinding.enableLogging(logger);
    }
}
