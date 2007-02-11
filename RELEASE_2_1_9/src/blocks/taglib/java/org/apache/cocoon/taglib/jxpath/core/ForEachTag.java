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
 * @version CVS $Id: ForEachTag.java,v 1.3 2004/03/05 13:02:25 bdelacretaz Exp $
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
