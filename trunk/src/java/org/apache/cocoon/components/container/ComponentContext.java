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
package org.apache.cocoon.components.container;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;

import java.util.Map;

/**
 * This is the {@link Context} implementation for Cocoon components.
 * It extends the {@link DefaultContext} by a special handling for
 * getting objects from the object model.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ComponentContext.java,v 1.4 2004/02/20 20:34:37 cziegeler Exp $
 */

public class ComponentContext
    extends DefaultContext {

    protected static final String OBJECT_MODEL_KEY_PREFIX = ContextHelper.CONTEXT_OBJECT_MODEL + '.';

    /**
     * Create a Context with specified data and parent.
     *
     * @param contextData the context data
     * @param parent the parent Context (may be null)
     */
    public ComponentContext(final Map contextData, final Context parent) {
        super( contextData, parent );
    }

    /**
     * Create a Context with specified data.
     *
     * @param contextData the context data
     */
    public ComponentContext(final Map contextData) {
        super( contextData );
    }

    /**
     * Create a Context with specified parent.
     *
     * @param parent the parent Context (may be null)
     */
    public ComponentContext(final Context parent) {
        super( parent );
    }

    /**
     * Create a Context with no parent.
     *
     */
    public ComponentContext() {
        super();
    }

    /**
     * Retrieve an item from the Context.
     *
     * @param key the key of item
     * @return the item stored in context
     * @throws ContextException if item not present
     */
    public Object get( final Object key )
    throws ContextException {
        if ( key.equals(ContextHelper.CONTEXT_OBJECT_MODEL)) {
            final Environment env = EnvironmentHelper.getCurrentEnvironment();
            if ( env == null ) {
                throw new ContextException("Unable to locate " + key + " (No environment available)");
            }
            return env.getObjectModel();
        }
        if ( key instanceof String ) {
            String stringKey = (String)key;
            if ( stringKey.startsWith(OBJECT_MODEL_KEY_PREFIX) ) {
                final Environment env = EnvironmentHelper.getCurrentEnvironment();
                if ( env == null ) {
                    throw new ContextException("Unable to locate " + key + " (No environment available)");
                }
                final Map objectModel = env.getObjectModel();
                String objectKey = stringKey.substring(OBJECT_MODEL_KEY_PREFIX.length());

                Object o = objectModel.get( objectKey );
                if ( o == null ) {
                    final String message = "Unable to locate " + key;
                    throw new ContextException( message );
                }
                return o;
            }
        }
        return super.get( key );
    }

}
