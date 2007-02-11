/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
// Step 1 -- Retrieve helper "beans" from the BSF framework

request      = bsf.lookupBean( "request" )
logger       = bsf.lookupBean( "logger" )
actionMap    = bsf.lookupBean( "actionMap" )

// Step 2 -- Perform the action

logger.debug( "START login.js" )

// Retrieve things from the request
// NOTE: they are all of type java.lang.String

uwid     = request.getParameter( "uwid" )
password = request.getParameter( "password" )

logger.debug( "Raw" )
logger.debug( "  uwid     [" + uwid + "]" )
//logger.debug( "  password [" + password + "]" )

// Hack since either the guy we stole the code from or
// Bruce has made an implementation error

password = password + "\0\0\0\0\0\0\0\0\0"

//logger.debug( "Cooked" )
//logger.debug( "  password [" + password + "]" )

try
{

    session = request.getSession(true)

    radius = new Packages.RadiusClient( "engine.uwaterloo.ca","1812","7sjU2xbHa31s2" )
    result = radius.Authenticate( uwid, password )

    if ( radius.ACCESS_ACCEPT == result )
    {
        logger.debug( "Authentication succeeded" )
        
        // Get (and optionally create) and populate the session
        
        session.setAttribute( "uwid", uwid )
        
        actionMap.put( "scriptaction-continue", "" )    
    }
    else
    {
        logger.debug( "Authentication failed" )
        session.setAttribute( "results", "<FAILURE>Incorrect User ID or Password</FAILURE>" )
    }

}
catch( ex )
{
    logger.debug( "Caught Exception" )
    logger.debug( "  " + ex )
}

logger.debug( "END login.js" )
