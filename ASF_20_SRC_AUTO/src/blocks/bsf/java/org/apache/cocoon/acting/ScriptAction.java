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
 * @version CVS $Id: ScriptAction.java,v 1.3 2004/03/05 13:01:47 bdelacretaz Exp $
 */

public class ScriptAction
extends ServiceableAction
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
