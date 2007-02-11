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
 * @version CVS $Id: AbstractPipelineCallingCronJob.java,v 1.2 2004/03/05 13:01:49 bdelacretaz Exp $
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
