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
package org.apache.cocoon.woody.formmodel;


/**
 * The {@link WidgetDefinition} corresponding to a {@link Union} widget.
 *
 * CVS $Id: UnionDefinition.java,v 1.2 2003/12/29 17:52:12 stefano Exp $
 * @author Timothy Larson
 */
public class UnionDefinition extends AbstractContainerDefinition {
    private String caseWidgetId;

    /*
    public void setDatatype(Datatype datatype) {
        if (!String.class.isAssignableFrom(datatype.getTypeClass()))
            throw new RuntimeException("Only datatype string is allowed for this widget at " + getLocation() + ".");
        super.setDatatype(datatype);
    }

    public void setDefault(Object value) throws Exception {
        if (!(value == null || String.class.isAssignableFrom(value.getClass())))
            throw new Exception("UnionDefinition: Default case must be supplied as a string (" + getLocation() + ")");
        if (value == null || value.equals("")) {
            if (isRequired())
                throw new Exception("UnionWidget: Union is marked required, but no default case was supplied (" + getLocation() + ")");
            this.defaultValue = "";
        } else {
            if (!hasWidget((String)value))
                throw new Exception("UnionWidget: The default value \"" + value + "\" does not match a union case (" + getLocation() + ")");
            this.defaultValue = (String)value;
        }
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
    */

    public void setCaseWidgetId(String id) {
        caseWidgetId = id;
    }

    public String getCaseWidgetId() {
        return caseWidgetId;
    }

    public Widget createInstance() {
        Union unionWidget = new Union(this);
        return unionWidget;
    }
}
