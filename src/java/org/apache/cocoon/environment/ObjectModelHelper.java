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
package org.apache.cocoon.environment;

import java.util.Map;

/**
 * A set of constants and methods to access the content of the object model.
 * <p>
 * The object model is a <code>Map</code> used to pass information about the
 * calling environment to the sitemap and its components (matchers, actions,
 * transformers, etc).
 * <p>
 * This class provides accessors only for the objects in the object model that are
 * common to every environment and which can thus be used safely. Some environments
 * provide additional objects, but they are not described here and accessing them
 * should be done in due cause since this ties the application to that particular
 * environment.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ObjectModelHelper.java,v 1.4 2003/09/27 13:01:15 joerg Exp $
 */

public final class ObjectModelHelper {

    /** Key for the environment {@link Request} in the object model. */
    public final static String REQUEST_OBJECT  = "request";

    /** Key for the environment {@link Response} in the object model. */
    public final static String RESPONSE_OBJECT = "response";

    /** Key for the environment {@link Context} in the object model. */
    public final static String CONTEXT_OBJECT  = "context";

    /** Key for the expiration value (Long) in the object model. */
    public final static String EXPIRES_OBJECT  = "expires";
    
    /** Key for the throwable object, only available within a &lt;map:handle-errors>. */
    public final static String THROWABLE_OBJECT = "throwable";

    /**
     * Key for a {@link Map} containing information from
     * a parent request provided to a sub-request (internal processing)
     */
    public final static String PARENT_CONTEXT = "parent-context";


    private ObjectModelHelper() {
        // Forbid instantiation
    }

    public static final Request getRequest(Map objectModel) {
        return (Request)objectModel.get(REQUEST_OBJECT);
    }

    public static final Response getResponse(Map objectModel) {
        return (Response)objectModel.get(RESPONSE_OBJECT);
    }

    public static final Context getContext(Map objectModel) {
        return (Context)objectModel.get(CONTEXT_OBJECT);
    }

    public static final Long getExpires(Map objectModel) {
        return (Long)objectModel.get(EXPIRES_OBJECT);
    }
    
    public static final Throwable getThrowable(Map objectModel) {
        return (Throwable)objectModel.get(THROWABLE_OBJECT);
    }
}
