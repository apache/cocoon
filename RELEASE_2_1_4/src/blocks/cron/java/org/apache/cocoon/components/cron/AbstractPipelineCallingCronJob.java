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
package org.apache.cocoon.components.cron;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.Processor;
import org.apache.cocoon.caching.CachingOutputStream;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.background.BackgroundEnvironment;
import org.apache.cocoon.util.NullOutputStream;

/**
 * An abstract CronJob implementation that provides a <code>process()</code>
 * method which calls a Cocoon pipeline internally. It uses the 
 * <code>org.apache.cocoon.environment.background.BackgroundEnvironment</code>
 * to avoid an external call.
 *
 * @author <a href="http://apache.org/~reinhard">Reinhard Poetz</a> 
 * @version CVS $Id: AbstractPipelineCallingCronJob.java,v 1.1 2003/12/19 09:01:43 reinhard Exp $
 *
 * @since 2.1.4
 */
public abstract class AbstractPipelineCallingCronJob
    extends ServiceableCronJob
    implements CronJob, Contextualizable {

    protected org.apache.cocoon.environment.Context context = null;

    /**
     * Call an available pipeline and return
     * 
     * @param uri of the pipeline
     * @return The return of the pipeline call as <code>InputStream</code>
     * @throws Exception - if pipeline is not available or returned OutputStream couldn't be closed
     *        
     */
    protected InputStream process(String uri) throws Exception {
        // use the CachingOutputStream because it buffers all bytes written
        CachingOutputStream os =
            new CachingOutputStream(new NullOutputStream());
        File c = new File(this.context.getRealPath("/"));
        BackgroundEnvironment env =
            new BackgroundEnvironment(uri, "", c, os, this.getLogger());
        process(uri, env);
        os.close();
        return new ByteArrayInputStream(os.getContent());
    }

    /**
     * Call the Cocoon processor to execute a pipeline. 
     * 
     * TODO (RP): Is this correct or too simplified? (I guess it is to simple for 
     *            pipelines with components using the ComponentManager ...)
     */
    private boolean process(String uri, Environment env) throws Exception {   
        if (uri.length() > 0 && uri.charAt(0) == '/') {
            uri = uri.substring(1);
        } else {
            uri = env.getURIPrefix() + uri;
        }

        Processor processor = null;
        boolean result = false;

        try {
            processor = (Processor) this.manager.lookup(Processor.ROLE);
            result = processor.process(env);
            env.commitResponse();
            return (result);
        } catch (Exception any) {
            throw (any);
        } finally {
            this.manager.release(processor);
        }
    }

    public void contextualize(Context context) throws ContextException {
        this.context =
            (org.apache.cocoon.environment.Context) context.get(
                Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

}
