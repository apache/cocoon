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
package org.apache.cocoon.components.cprocessor.sitemap;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.SimpleParentProcessingNode;
import org.apache.cocoon.environment.Environment;

/**
 * Handles &lt;map:handle-errors&gt;
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: HandleErrorsNode.java,v 1.2 2004/01/05 08:17:30 cziegeler Exp $
 */
public final class HandleErrorsNode extends SimpleParentProcessingNode {

    private int statusCode;

    public HandleErrorsNode() {
    }

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        this.statusCode = config.getAttributeAsInteger("type", -1);
    }
    
    public int getStatusCode() {
        return this.statusCode;
    }

    public final boolean invoke(Environment env, InvokeContext context) throws Exception {

        if (getLogger().isInfoEnabled()) {
            getLogger().info("Processing handle-errors at " + getLocation());
        }

		if (statusCode == -1) {
            // No 'type' attribute : new Cocoon 2.1 behaviour, no implicit generator
            try {
                return invokeNodes(getChildNodes(), env, context);
                
            } catch(ProcessingException pe) {
                // Handle the various cases related to the transition from implicit generators in handle-errors to
                // explicit ones, in order to provide meaningful messages that will ease the migration
                if (statusCode == - 1 &&
                    pe.getMessage().indexOf("must set a generator first before you can use a transformer") != -1) {

                    throw new ProcessingException(
                        "Incomplete pipeline : 'handle-error' without a 'type' must include a generator, at " +
                        this.getLocation() + System.getProperty("line.separator") +
                        "Either add a generator (preferred) or a type='500' attribute (deprecated) on 'handle-errors'");
                        
                } else if (statusCode != -1 &&
                    pe.getMessage().indexOf("Generator already set") != -1){

                    throw new ProcessingException(
                        "Error : 'handle-error' with a 'type' attribute has an implicit generator, at " +
                        this.getLocation() + System.getProperty("line.separator") +
                        "Please remove the 'type' attribute on 'handle-error'");

                } else {
                    // Rethrow the exception
                    throw pe;
                }
            }
		} else {
		    // A 'type' attribute is present : add the implicit generator
            context.getProcessingPipeline().setGenerator("<notifier>", "", Parameters.EMPTY_PARAMETERS, Parameters.EMPTY_PARAMETERS);
            return invokeNodes(getChildNodes(), env, context);
		}
    }

}
