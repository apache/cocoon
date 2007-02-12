/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cocoon.acting;

import java.io.File;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceUtil;

/**
 * Action class, which simply calls a process on an operating system.
 * Therefore, a parameter named 'command' has to be configured in the sitemap 
 * to hold the filename as value.
 * 
 * @version $Id$
 */
public class CommandAction extends AbstractAction implements ThreadSafe {
	
	public final static String PARAM_COMMAND = "command";

    /**
     * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map act( Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters param )
	throws Exception {
		String command = param.getParameter(PARAM_COMMAND);
		Source src = null;
		
		if ( command != null ) {
			try {
				src = resolver.resolveURI(command);
                File file = SourceUtil.getFile(src);
                if ( file != null ) {
                    Runtime.getRuntime().exec(file.getAbsolutePath());
                } else {
                    this.getLogger().error("Command does not point to a file " + command);
                }
			} catch(Exception e) {
				this.getLogger().error("Error while execute an OS-Process",e);
				return null;
			} finally {
				resolver.release(src);
			}
		} else {
			return null;
		}
        return EMPTY_MAP;
    }
}
