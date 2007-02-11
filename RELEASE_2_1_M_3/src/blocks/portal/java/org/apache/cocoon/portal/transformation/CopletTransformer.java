/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.transformation;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Includes coplet instance data by using JXPath expressions.
 * The transformer searches for tags &lt;coplet:coplet xmlns:coplet="http://apache.org/cocoon/portal/coplet/1.0"&gt;.
 * They must have an attribute "select" that contains a valid JXPath expression applying to the coplet instance data.<br><br>
 *
 * Example:<br><br>
 * 
 * <pre>&lt;maxpageable xmlns:coplet="http://apache.org/cocoon/portal/coplet/1.0"&gt;
 * 	&lt;coplet:coplet select="copletData.maxpageable"/&gt;
 * &lt;/maxpageable&gt;<br></pre>
 * 
 * The transformer will insert the boolean value specifying whether the coplet is 
 * maxpageable or not.<br> 
 * Please see also the documentation of superclass AbstractCopletTransformer for how
 * the coplet instance data are acquired.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Björn Lütkemeier</a>
 * @version CVS $Id: CopletTransformer.java,v 1.3 2003/05/27 11:54:17 cziegeler Exp $
 */
public class CopletTransformer 
extends AbstractCopletTransformer {

    /**
     * The namespace URI to listen for.
     */
    public static final String NAMESPACE_URI = "http://apache.org/cocoon/portal/coplet/1.0";
    
    /**
     * The XML element name to listen for.
     */
    public static final String COPLET_ELEM = "coplet";

    /**
     * The attribute containing the JXPath expression.
     */
    public static final String SELECT_ATTR = "select";

        
    /**
     * Creates new CopletTransformer.
     */
    public CopletTransformer() {
        this.defaultNamespaceURI = NAMESPACE_URI;
    }
    
    /**
     * Overridden from superclass.
     */
    public void startTransformingElement(String uri, String name, String raw, Attributes attr) 
    throws ProcessingException, IOException, SAXException {
        if (name.equals(COPLET_ELEM)) {
            String expression = attr.getValue(SELECT_ATTR);
            if (expression == null) {
                throw new ProcessingException("Attribute "+SELECT_ATTR+" must be spcified.");
            }
                
            CopletInstanceData cid = this.getCopletInstanceData();
            
            JXPathContext jxpathContext = JXPathContext.newContext( cid );
            Object object = jxpathContext.getValue(expression);
                
            if (object == null) {
                throw new ProcessingException("Could not find value for expression "+expression);
            }
                
        } else {
            super.startTransformingElement(uri, name, raw, attr);
        }
    }

    /**
     * Overridden from superclass.
     */
    public void endTransformingElement(String uri, String name, String raw) 
    throws ProcessingException, IOException, SAXException {
        if (!name.equals(COPLET_ELEM)) {
            super.endTransformingElement(uri, name, raw);
        }
    }

}
