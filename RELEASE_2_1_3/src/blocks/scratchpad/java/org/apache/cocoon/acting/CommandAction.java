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
 * @author <a href="mailto:pklassen@s-und-n.de">Peter Klassen</a>
 * @version CVS $Id: CommandAction.java,v 1.1 2003/09/04 12:42:42 cziegeler Exp $
 */

public class CommandAction extends AbstractAction implements ThreadSafe {
	
	public final static String PARAM_COMMAND = "command";
	
	
    public Map act( Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters param )
	throws Exception {
		String command = param.getParameter(PARAM_COMMAND);
		Source src = null;
		
		if ((param != null) && (command != null)) {
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
		}
		else {
			return null;
		}
        return(EMPTY_MAP);
    }

}
