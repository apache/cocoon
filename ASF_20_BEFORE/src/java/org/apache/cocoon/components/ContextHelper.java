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
package org.apache.cocoon.components;

import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;

/**
 * A set of constants and methods to access the content of the context
 * object. Some of the constants are defined in {@link org.apache.cocoon.Constants}.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ContextHelper.java,v 1.5 2004/02/24 09:54:56 cziegeler Exp $
 */

public final class ContextHelper {

    /** Application <code>Context</code> Key for the current object model */
    public static final String CONTEXT_OBJECT_MODEL = "object-model";

    /** Application <code>Context</code> Key for the current request object */
    public static final String CONTEXT_REQUEST_OBJECT = CONTEXT_OBJECT_MODEL + '.' + ObjectModelHelper.REQUEST_OBJECT;

    /** Application <code>Context</code> Key for the current response object */
    public static final String CONTEXT_RESPONSE_OBJECT = CONTEXT_OBJECT_MODEL + '.' + ObjectModelHelper.RESPONSE_OBJECT;

    /** Application <code>Context</code> Key for the current sitemap service manager */
    public static final String CONTEXT_SITEMAP_SERVICE_MANAGER = "sitemap-service-manager";
    
    private ContextHelper() {
        // Forbid instantiation
    }

    public static final Request getRequest(Context context) {
        // the request object is always present
        try {
            return (Request)context.get(CONTEXT_REQUEST_OBJECT);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the request object from the context.", ce);
        }
    }

    public static final Response getResponse(Context context) {
        // the response object is always present
        try {
            return (Response)context.get(CONTEXT_RESPONSE_OBJECT);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the response object from the context.", ce);
        }
    }

    public static final Map getObjectModel(Context context) {
        // the object model is always present
        try {
            return (Map)context.get(CONTEXT_OBJECT_MODEL);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the object model from the context.", ce);
        }
    }
}
