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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;


/**
 * A simple test CronJob which also calls a pipeline internally.
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @author <a href="http://apache.org/~reinhard">Reinhard Poetz</a> 
 * @version CVS $Id: TestCronJob.java,v 1.4 2003/12/22 13:25:12 joerg Exp $
 *
 * @since 2.1.1
 */
public class TestCronJob extends AbstractPipelineCallingCronJob
    implements CronJob, Configurable, ConfigurableCronJob {
    
    /** Parameter key for the message */
    public static final String PARAMETER_MESSAGE = "TestCronJob.Parameter.Message";

    /** Parameter key for the sleep value */
    public static final String PARAMETER_SLEEP = "TestCronJob.Parameter.Sleep";

	/** Parameter key for the pipeline to be called */
	public static final String PARAMETER_PIPELINE = "TestCronJob.Parameter.Pipeline";    

    /** The configured message */
    private String m_msg;

    /** The configured sleep time */
    private int m_sleep;
    
    /** The pipeline to be called */
    private String pipeline = null;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(final Configuration config)
    	throws ConfigurationException {
        m_msg = config.getChild("msg").getValue("I was not configured");
        m_sleep = config.getChild("sleep").getValueAsInteger(11000);
        pipeline = config.getChild("pipeline").getValue("samples/hello-world/hello.xhtml");
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.CronJob#execute(java.lang.String)
     */
    public void execute(String name) {
        getLogger().info("CronJob " + name + " launched at " + new Date() + " with message '" + m_msg +
                         "' and sleep timeout of " + m_sleep + "ms");
        
        InputStream is = null;
        try {
            is = process(pipeline);
        } catch (Exception e) {
            getLogger().error("error in execution of TestCronJob", e);
        } 
        StringBuffer sb = new StringBuffer();
        try {        
            InputStreamReader reader = new InputStreamReader(is);
            sb = new StringBuffer();
            char[] b = new char[8192];
            int n;

            while((n = reader.read(b)) > 0) {
                sb.append(b, 0, n); 
            } 
        } catch( IOException ioe ) {
            getLogger().error("error trying to read the InputStream returned by the Pipeline processor");
        }
        getLogger().info("Cronjob " + name + " called pipeline " + pipeline + 
            " and received following content:\n" + sb.toString() );
       
        try {
            Thread.sleep(m_sleep);
        } catch (final InterruptedException ie) {
            //getLogger().error("CronJob " + name + " interrupted", ie);
        }

        getLogger().info("CronJob " + name + " finished at " + new Date() + " with message '" + m_msg +
                         "' and sleep timeout of " + m_sleep + "ms");
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.cron.ConfigurableCronJob#setup(org.apache.avalon.framework.parameters.Parameters, java.util.Map)
     */
    public void setup(Parameters params, Map objects) {
        if (null != params) {
            m_msg = params.getParameter(PARAMETER_MESSAGE, m_msg);
            m_sleep = params.getParameterAsInteger(PARAMETER_SLEEP, m_sleep);
            pipeline = params.getParameter(PARAMETER_PIPELINE, pipeline );
            
        }
    }


    
}
