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
import org.apache.excalibur.source.SourceNotFoundException;

import java.util.Map;

/**
 * This action simply checks to see if a given resource exists. It checks
 * whether the specified in the src attribute source exists or not.
 * The action returns empty <code>Map</code> if it exists, null otherwise.
 * 
 * <p>
 * Instead of src attribute, source can be specified using
 * parameter named 'url' (this is old syntax).
 * 
 * <p>
 * <b>Note:</b> {@link org.apache.cocoon.selection.ResourceExistsSelector}
 * should be preferred to this component, as the semantics of a Selector better
 * matches the supplied functionality.
 *
 * @author <a href="mailto:balld@apache.org">Donald Ball</a>
 * @version CVS $Id: ResourceExistsAction.java,v 1.4 2003/12/12 14:33:09 vgritsenko Exp $
 */
public class ResourceExistsAction extends ServiceableAction implements ThreadSafe {

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws Exception {
        String resourceURI = parameters.getParameter("url", src);
        Source source = null;
        try {
            source = resolver.resolveURI(resourceURI);
            if (source.exists()) {
                return EMPTY_MAP;
            }
        } catch (SourceNotFoundException e) {
            // Do not log
        } catch (Exception e) {
            getLogger().warn("Exception resolving resource " + resourceURI, e);
        } finally {
            if (source != null) {
                resolver.release(source);
            }
        }
        return null;
    }
}
