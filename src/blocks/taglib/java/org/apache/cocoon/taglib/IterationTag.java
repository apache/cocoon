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
package org.apache.cocoon.taglib;

import org.xml.sax.SAXException;

/**
 * The IterationTag interface extends Tag by defining one additional
 * method that controls the reevaluation of its body.
 *
 * <p> A tag handler that implements IterationTag is treated as one that
 * implements Tag regarding  the doStartTag() and doEndTag() methods.
 * IterationTag provides a new method: <code>doAfterBody()</code>.
 *
 * <p> The doAfterBody() method is invoked after every body evaluation
 * to control whether the body will be reevaluated or not.  If doAfterBody()
 * returns IterationTag.EVAL_BODY_AGAIN, then the body will be reevaluated.
 * If doAfterBody() returns Tag.SKIP_BODY, then the body will be skipped
 * and doEndTag() will be evaluated instead.
 *
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: IterationTag.java,v 1.2 2003/03/16 17:49:08 vgritsenko Exp $
 */
public interface IterationTag extends Tag {

    /**
     * Request the reevaluation of some body.
     * Returned from doAfterBody.
     */
    public final static int EVAL_BODY_AGAIN = 2;

    /**
     * Process body (re)evaluation.  This method is invoked by the
     * Taglib implementation object after every evaluation of
     * the body into the BodyEvaluation object. The method is
     * not invoked if there is no body evaluation.
     *
     * <p>
     * If doAfterBody returns EVAL_BODY_AGAIN, a new evaluation of the
     * body will happen (followed by another invocation of doAfterBody).
     * If doAfterBody returns SKIP_BODY no more body evaluations will
     * occur and then doEndTag will be invoked.
     *
     * <p>
     * The method re-invocations may be lead to different actions because
     * there might have been some changes to shared state, or because
     * of external computation.
     *
     * @return whether additional evaluations of the body are desired
     * @throws SAXException
     */
    int doAfterBody() throws SAXException;
}
