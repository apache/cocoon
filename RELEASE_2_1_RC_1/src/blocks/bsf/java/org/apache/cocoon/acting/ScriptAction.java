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

import com.ibm.bsf.BSFManager;
import com.ibm.bsf.util.IOUtils;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple action that executes any script that can be run by the BSF
 *
 * @author <a href="mailto:jafoster@uwaterloo.ca">Jason Foster</a>
 * @version CVS $Id: ScriptAction.java,v 1.1 2003/03/09 00:02:44 pier Exp $
 */

public class ScriptAction
extends ComposerAction
implements ThreadSafe {


    public Map act( Redirector redirector,
                    SourceResolver resolver,
                    Map objectModel,
                    String source,
                    Parameters par )
    throws Exception {
        Source src = null;
        try {
            // Figure out what script to open.  A missing script name is caught
            // by the resolver/SystemId grouping later on and causes an exception
            String scriptName = source;

            // Locate the appropriate file on the filesytem
            src = resolver.resolveURI(scriptName);
            String systemID = src.getURI();

            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("script source [" + scriptName + "]");
                getLogger().debug("script resolved to [" + systemID + "]");
            }

            Reader in = new InputStreamReader(src.getInputStream());

            // Set up the BSF manager and register relevant helper "beans"

            BSFManager mgr = new BSFManager();
            HashMap actionMap = new HashMap();

            // parameters to act(...)
            mgr.registerBean("resolver", resolver);
            mgr.registerBean("objectModel", objectModel);
            mgr.registerBean("parameters", par);

            // ScriptAction housekeeping
            mgr.registerBean("actionMap", actionMap);

            // helpers

            mgr.registerBean("logger", getLogger());
            mgr.registerBean("request", ( ObjectModelHelper.getRequest(objectModel) ) );
            mgr.registerBean("scriptaction", this );
            mgr.registerBean("manager", this.manager );

            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("BSFManager execution begining");
            }

            // Execute the script

            mgr.exec(BSFManager.getLangFromFilename(systemID), systemID, 0, 0,
                    IOUtils.getStringFromReader(in));

            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("BSFManager execution complete");
            }

            // Figure out what to return
            // TODO: decide on a more robust communication method

            if ( actionMap.containsKey( "scriptaction-continue" ) )
            {
                return ( Collections.unmodifiableMap(actionMap) );
            }
            else
            {
                return ( null );
            }
        } catch (Exception e) {
            throw new ProcessingException(
                    "Exception in ScriptAction.act()", e);
        } finally {
            resolver.release( src );
        } // try/catch
    } // public Map act(...)
} // public class ScriptAction
