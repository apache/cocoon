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
package org.apache.cocoon.bean;

import org.apache.excalibur.event.command.CommandFailureHandler;
import org.apache.excalibur.event.command.Command;

/**
 * CocoonCommandFailureHandler does XYZ
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $ Revision: 1.1 $
 */
public class CocoonCommandFailureHandler implements CommandFailureHandler
{
    public boolean handleCommandFailure( Command command, Throwable throwable )
    {
        // TODO: provide solution for what to do.  Returning "true" shuts down the command manager.
        // TODO: at least we want to provide for logging.
        return false;
    }
}
