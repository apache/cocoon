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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

// TODO: The exception messages should use I18n.
/**
 * This is the "{@link WidgetDefinition}" which is used to instantiate a
 * {@link ClassDefinition}. The resolve step replaces this definition with
 * the definitions contained in the referenced {@link ClassDefinition}.
 *
 * CVS $Id: NewDefinition.java,v 1.1 2003/12/29 06:14:49 tim Exp $
 * @author Timothy Larson
 */
public class NewDefinition extends AbstractWidgetDefinition {
    private boolean resolving;
    private ClassDefinition classDefinition;

    public NewDefinition() {
        super();
        resolving = false;
        classDefinition = null;
    }

    private ClassDefinition getClassDefinition() throws Exception {
        FormDefinition formDefinition = getFormDefinition();
        WidgetDefinition classDefinition = formDefinition.getWidgetDefinition(getId());
        if (classDefinition == null)
            throw new Exception("NewDefinition: Class with id \"" + getId() + "\" does not exist (" + getLocation() + ")");
        if (!(classDefinition instanceof ClassDefinition))
            throw new Exception("NewDefinition: Id \"" + getId() + "\" is not a class (" + getLocation() + ")");
        return (ClassDefinition)classDefinition;
    }

    // TODO: Should add checking for union defaults which would cause non-terminating recursion.
    public void resolve(List parents, WidgetDefinition parent) throws Exception {
        // Non-terminating recursion detection
        if (resolving) {
            // Search up parent list in hopes of finding a "Union" before finding previous "New" for this "Class".
            ListIterator parentsIt = parents.listIterator(parents.size());
            while(parentsIt.hasPrevious()) {
                WidgetDefinition definition = (WidgetDefinition)parentsIt.previous();
                if (definition instanceof UnionDefinition) break;
                if (definition == this)
                    throw new Exception("NewDefinition: Non-terminating recursion detected in widget definition : "
                        + parent.getId() + " (" + getLocation() + ")");
            }
        }
        // Resolution
        resolving = true;
        parents.add(this);
        classDefinition = getClassDefinition();
        Iterator definitionsIt = classDefinition.getWidgetDefinitions().iterator();
        parents.add(this);
        while (definitionsIt.hasNext()) {
            WidgetDefinition definition = (WidgetDefinition)definitionsIt.next();
            if (definition instanceof ContainerDefinition) {
                ((ContainerDefinition)definition).resolve(parents, parent);
            }
            if (!(definition instanceof NewDefinition)) {
                ((ContainerDefinition)parent).addWidgetDefinition(definition);
            }
        }
        parents.remove(parents.size()-1);
        resolving = false;
    }

    public Widget createInstance() {
        return null;
    }
}
