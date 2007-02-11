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

package org.apache.cocoon.taglib.jxpath.core;

import org.apache.cocoon.taglib.IterationTag;
import org.apache.cocoon.taglib.core.ForEachSupport;
import org.apache.cocoon.taglib.core.LoopTag;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <p>A handler for &lt;forEach&gt; that accepts attributes as Strings
 * and evaluates them as expressions at runtime.</p>
 *
 * Migration from JSTL1.0
 * @see org.apache.taglibs.standard.tag.el.core.ForEachTag
 *
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: ForEachTag.java,v 1.2 2003/03/16 17:49:09 vgritsenko Exp $
 */
public class ForEachTag extends ForEachSupport implements LoopTag, IterationTag {

    //*********************************************************************
    // 'Private' state (implementation details)

    private String begin_; // stores EL-based property
    private String end_; // stores EL-based property
    private String step_; // stores EL-based property
    private String items_; // stores EL-based property

    //*********************************************************************
    // Constructor

    public ForEachTag() {
        super();
        init();
    }

    //*********************************************************************
    // Tag logic

    /* Begins iterating by processing the first item. */
    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

        // evaluate any expressions we were passed, once per invocation
        evaluateExpressions();

        // chain to the parent implementation
        return super.doStartTag(namespaceURI, localName, qName, atts);
    }

    // Releases any resources we may have (or inherit)
    public void recycle() {
        init();
        super.recycle();
   
    }

    //*********************************************************************
    // Accessor methods

    // for EL-based attribute
    public void setBegin(String begin_) {
        this.begin_ = begin_;
        this.beginSpecified = true;
    }

    // for EL-based attribute
    public void setEnd(String end_) {
        this.end_ = end_;
        this.endSpecified = true;
    }

    // for EL-based attribute
    public void setStep(String step_) {
        this.step_ = step_;
        this.stepSpecified = true;
    }

    public void setItems(String items_) {
        this.items_ = items_;
    }

    //*********************************************************************
    // Private (utility) methods

    // (re)initializes state (during release() or construction)
    private void init() {
        // defaults for interface with page author
        begin_ = null; // (no expression)
        end_ = null; // (no expression)
        step_ = null; // (no expression)
        items_ = null; // (no expression)
    }

    /* Evaluates expressions as necessary */
    private void evaluateExpressions() throws SAXException {

        if (begin_ != null) {
            begin = Integer.parseInt(begin_);
        }
        if (end_ != null) {
            end = Integer.parseInt(end_);
        }
        if (step_ != null) {
            step = Integer.parseInt(step_);
        }
        if (items_ != null) {
            rawItems = getVariable(items_);
        }

        /* 
         * Note: we don't check for type mismatches here; we assume
         * the expression evaluator will return the expected type
         * (by virtue of knowledge we give it about what that type is).
         * A ClassCastException here is truly unexpected, so we let it
         * propagate up.
         */
        /*
        if (begin_ != null) {
           Object r = ExpressionEvaluatorManager.evaluate("begin", begin_, Integer.class, this, pageContext);
           if (r == null)
               throw new NullAttributeException("forEach", "begin");
           begin = ((Integer) r).intValue();
           validateBegin();
        }
        
        if (end_ != null) {
           Object r = ExpressionEvaluatorManager.evaluate("end", end_, Integer.class, this, pageContext);
           if (r == null)
               throw new NullAttributeException("forEach", "end");
           end = ((Integer) r).intValue();
           validateEnd();
        }
        
        if (step_ != null) {
           Object r = ExpressionEvaluatorManager.evaluate("step", step_, Integer.class, this, pageContext);
           if (r == null)
               throw new NullAttributeException("forEach", "step");
           step = ((Integer) r).intValue();
           validateStep();
        }
        
        if (items_ != null) {
           rawItems = ExpressionEvaluatorManager.evaluate("items", items_, Object.class, this, pageContext);
           // use an empty list to indicate "no iteration", if relevant
           if (rawItems == null)
               rawItems = new ArrayList();
        }
        */
    }
}
