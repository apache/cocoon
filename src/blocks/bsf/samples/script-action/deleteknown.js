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

scriptaction = bsf.lookupBean( "scriptaction" )
manager      = bsf.lookupBean( "manager" )            
request      = bsf.lookupBean( "request" )
logger       = bsf.lookupBean( "logger" )
actionMap    = bsf.lookupBean( "actionMap" )

// Step 2 -- Perform the action

logger.debug( "START deleteknown.js" )

// Retrieve things from the session and request
// NOTE: they are all of type java.lang.String

session = request.getSession( false )
uwid 	= session.getAttribute( "uwid" )
id 	= request.getParameter( "id" )

logger.debug( "Raw" )
logger.debug( "  uwid          [" + uwid + "]" )
logger.debug( "  id            [" + id + "]" )

// Actually do the database work

// We have the choice of declaring things out here and making them explicitly
// null, or we have to use a different comparison in the "finally" block (defined?)

dbselector = null
datasource = null
conn = null
addStatement = null

try
{
    dbselector = manager.lookup( scriptaction.DB_CONNECTION )
    datasource = dbselector.select( "ceabplanner" )
    conn = datasource.getConnection()    
    deleteStatement = conn.prepareStatement(
        "DELETE FROM studentknowncourselist WHERE student = ( SELECT id FROM students WHERE uw_userid = ? ) AND known_course = ?"
    )

    deleteStatement.setString( 1, uwid ); 
    deleteStatement.setString( 2, id ); 

    result = deleteStatement.executeUpdate()
    logger.debug( "Result #1 [" + result + "]" )

    conn.commit()

    actionMap.put( "scriptaction-continue", "" )
    session.setAttribute( "results", "<SUCCESS>Course deleted at " + Date() + "</SUCCESS>" )
}
catch( ex )
{
    logger.debug( "Caught Exception" )
    logger.debug( "  " + ex )
}
finally
{
    if ( null != addStatement ) { addStatement.close() }
    if ( null != conn ) { conn.close() }
    if ( null != datasource ) { dbselector.release( datasource ) }
    if ( null != dbselector ) { manager.release( dbselector ) }
}

logger.debug( "END deleteknown.js" )
