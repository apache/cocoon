/* ============================================================================ *
 *                   The Apache Software License, Version 1.1                   *
 * ============================================================================ *
 *                                                                              *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved. *
 *                                                                              *
 * Redistribution and use in source and binary forms, with or without modifica- *
 * tion, are permitted provided that the following conditions are met:          *
 *                                                                              *
 * 1. Redistributions of  source code must  retain the above copyright  notice, *
 *    this list of conditions and the following disclaimer.                     *
 *                                                                              *
 * 2. Redistributions in binary form must reproduce the above copyright notice, *
 *    this list of conditions and the following disclaimer in the documentation *
 *    and/or other materials provided with the distribution.                    *
 *                                                                              *
 * 3. The end-user documentation included with the redistribution, if any, must *
 *    include  the following  acknowledgment:  "This product includes  software *
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)." *
 *    Alternately, this  acknowledgment may  appear in the software itself,  if *
 *    and wherever such third-party acknowledgments normally appear.            *
 *                                                                              *
 * 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be *
 *    used to  endorse or promote  products derived from  this software without *
 *    prior written permission. For written permission, please contact          *
 *    apache@apache.org.                                                        *
 *                                                                              *
 * 5. Products  derived from this software may not  be called "Apache", nor may *
 *    "Apache" appear  in their name,  without prior written permission  of the *
 *    Apache Software Foundation.                                               *
 *                                                                              *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND *
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE *
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, *
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU- *
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS *
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON *
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT *
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF *
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
 *                                                                              *
 * This software  consists of voluntary contributions made  by many individuals *
 * on  behalf of the Apache Software  Foundation.  For more  information on the *
 * Apache Software Foundation, please see <http://www.apache.org/>.             *
 *                                                                              *
 * ============================================================================ */
package org.apache.garbage.tree;

import java.util.Iterator;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: TemplateFor.java,v 1.2 2003/06/24 16:59:19 cziegeler Exp $
 */
public class TemplateFor extends LocatedEvents implements Event {

    /** Our expression. */
    private CompiledExpression expression = null;

    /** The expression as a string. */
    private String data = null;

    /**
     * Create a new <code>TemplateFor</code> instance.
     *
     * @param variable The variable name to create/update..
     * @param data The data of this expression.
     */
    public TemplateFor(String data) {
        this(null, data);
    }

    /**
     * Create a new <code>TemplateFor</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data The data of this expression.
     */
    public TemplateFor(Locator locator, String data) {
        super(locator);
        if (data == null) {
            throw new TreeException(locator, "No expression data");
        }
        try {
            this.expression = JXPathContext.compile(data);
            this.data = data;
        } catch (Exception e) {
            throw new TreeException(locator, "Cannot create expression from \""
                                    + data + "\"", e);
        }
    }

    /**
     * Process this event in the context of the specified <code>Runtime</code>.
     *
     * @param runtime The <code>Runtime</code> receiving events notifications.
     * @throws SAXException In case of error processing this event.
     */
    public void process(Runtime runtime, JXPathContext context)
    throws SAXException {
        try {
            Iterator iterator = this.expression.iteratePointers(context);
            while (iterator.hasNext()) {
                Pointer ptr = (Pointer)iterator.next();
                JXPathContext ctx = context.getRelativeContext(ptr);
                VariableScope scp = new VariableScope(ctx);

                Iterator evt = this.iterator();
                while(evt.hasNext()) {
                    ((Event)evt.next()).process(runtime, ctx);
                }
            }
        } catch (SAXException e) {
            throw(e);
        } catch (Exception e) {
            throw new SAXException("Cannot evaluate expression {"
                                   + this.data + "}", e);
        }
    }

    /**
     * If possible, merge this <code>Event</code> to another.
     * <br />
     * By default all <code>AbstractEvent</code> instances will not merge with
     * each other. Solid implementations of this class will have to override
     * this method and (if extending the <code>LocatedEvent</code> abstract
     * class) call {@link LocatedEvent#mergeLocation(LocatedEvent)} to update
     * location information.
     *
     * @param event The <code>Event</code> to which this one should be merged.
     * @return Always <b>false</b>.
     * @see LocatedEvent#mergeLocation(LocatedEvent)
     */
    public boolean merge(Event event) {
        return(false);
    }
}
