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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.impl.PartSource;
import org.apache.cocoon.environment.Redirector;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * The CopySourceAction copies the content of it's "src" attribute to its "dest" parameter.
 * The destination must of course resolve to a <code>WriteableSource</code>
 * <p>
 * Example :
 * <pre>
 *   &lt;map:act type="copy-source" src="cocoon://pipeline.xml"&gt;
 *     &lt;map:parameter name="dest" value="context://WEB-INF/data/file.xml"/&gt;
 *     .../...
 *   &lt;/map:act&gt;
 *</pre>
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: CopySourceAction.java,v 1.2 2003/12/20 14:29:19 sylvain Exp $
 */
public class CopySourceAction extends ServiceableAction implements ThreadSafe
{
    
    private SourceResolver resolver;

    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
    }
    
    public Map act(Redirector redirector, org.apache.cocoon.environment.SourceResolver oldResolver, Map objectModel, String source, Parameters par)
        throws Exception {
        
        // Get source and destination Sources
        Source src = resolver.resolveURI(source);
        Source dest = resolver.resolveURI(par.getParameter("dest"));
        
        // Check that dest is writeable
        if (! (dest instanceof ModifiableSource)) {
            throw new IllegalArgumentException("Non-writeable URI : " + dest.getURI());
        }
        
        if (dest instanceof TraversableSource) {
            TraversableSource trDest = (TraversableSource) dest;
            if (trDest.isCollection()) {
                if (src instanceof TraversableSource) {
                    dest = trDest.getChild(((TraversableSource)src).getName());
                } else if (src instanceof PartSource){
                    // FIXME : quick hack to store "upload://xxx" sources into directories
                    // it would be better for the PartSource to be Traversable, or to
                    // create a new "NamedSource" interface
                    dest = trDest.getChild(((PartSource)src).getPart().getFileName());
                }
            }
        }
        
        ModifiableSource wdest = (ModifiableSource)dest;
        
        // Get streams
        InputStream is = src.getInputStream();
        OutputStream os = wdest.getOutputStream();
        
        // And transfer all content.
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer, 0, buffer.length)) > 0) {
                os.write(buffer, 0, len);
            }
            os.close();
        } catch(Exception e) {
            if (wdest.canCancel(os)) {
                wdest.cancel(os);
            }
        } finally {
            is.close();
        }
        // Success !
        return EMPTY_MAP;
    }
}
