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
package org.apache.cocoon.acting;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;

import java.util.Map;
import java.util.HashMap;

/**
 * This action simply checks to see if a given resource exists. 
 * It checks whether the specified in the src attribute source is resolvable, or not.
 * The action returns a <code>Map</code> if it exists, setting the parameter
 * <code>resource-exists</code> to <code>true</code>, otherwise the parameter
 * <code>resource-exists</code> is set to <code>false</code>.
 *
 * <p>Instead of src attribute, source can be specified using
 * parameter named 'url' (this is old syntax).
 *
 * You might want to test the parameter <code>resouce-exists</code> in a
 * following <code>&lt;map:select type="parameter"&gt;</code> in the sitemap.
 *
 * @author <a href="mailto:balld@apache.org">Donald Ball</a>
 * @author <a href="mailto:huber@apache.org">Bernhard Huber</a>
 *
 * @version CVS $Id: ExtendedResourceExistsAction.java,v 1.1 2003/09/04 12:42:42 cziegeler Exp $
 * @since 2.1
 */
public class ExtendedResourceExistsAction extends ComposerAction implements ThreadSafe {

    /**
     * This parameter name is put into the objectModel map, setting it to <code>true</code>,
     * or <code>false</code>
     */
    public final static String RESOURCE_EXISTS_PARAM_NAME = "resource-exists";
    
    /**
     * Execute the ExtendedResourceExistsAction.
     *
     * @param redirector Cocoon's redirector
     * @param resolver Cocoon's source resolver, used for testing if a source is resolvable
     * @param source the source, e.g.: index.html
     * @param parameters of this action
     * @return Map having an entry named as defined in <code>RESOURCE_EXISTS_PARAM_NAME</code> having
     *   value <code>"true"</code>, iff source is resolvable, else having value <code>"false"</code>.
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception {
        String urlstring = parameters.getParameter("url", source);
        Source src = null;
        
        Map result = new HashMap();
        
        try {
            // try to resolve the source
            src = resolver.resolveURI(urlstring);
            src.getInputStream();
            
            // as no exception has been thrown assume that
            // the source exists, and is accessible
            result.put( RESOURCE_EXISTS_PARAM_NAME, "true" );

        } catch (Exception e) {
            
            // as an exception is thrown assume that the source
            // can not be resolved, and does not exists
            getLogger().warn( "Resource " + String.valueOf(urlstring) + " does not exist, " +
              "set parameter " + RESOURCE_EXISTS_PARAM_NAME + " to false" );

            result.put( RESOURCE_EXISTS_PARAM_NAME, "false" );
        } finally {
            // do houskeeping release the resolved src
            resolver.release(src);
        }
        return result;
    }
}

