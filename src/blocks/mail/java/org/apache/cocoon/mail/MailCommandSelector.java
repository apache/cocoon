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
package org.apache.cocoon.mail;

import java.util.Map;
import org.apache.cocoon.selection.AbstractSwitchSelector;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;
import org.apache.avalon.framework.context.ContextException;
/*
usage:
  <map:select type="mail-selector">
    <!-- optional -->
    <map:parameter name="command" value="{request-attribute:cmd}"/>
    
    <!-- get value from request.getAttribute( "cmd" ) -->
    <map:parameter name="command" value="cmd"/>
    
    <map:when test="cat-folder">
    </map:when>
    
    <map:otherwise>
    </map:otherwise>

    <map:when test="command-defined">
    <map:when test="command-undefined">
    
    <map:when test="
*/

/**
 * @deprecated use RequestAttributeSelector, RequestParameterSelector, or ParameterSelector instead.
 * @version CVS $Id: MailCommandSelector.java,v 1.3 2004/03/05 13:02:00 bdelacretaz Exp $
 */
public class MailCommandSelector extends AbstractSwitchSelector {
  
  public Object getSelectorContext(Map objectModel, Parameters parameters) {
      Request request = ObjectModelHelper.getRequest(objectModel);
      // try to get the command from the request-attribute
      String cmdName = MailContext.MAIL_CURRENT_WORKING_COMMAND_ENTRY;
      String cmd = (String)request.getAttribute( cmdName );
      
      // try to get command from the request parameter
      if (cmd == null) {
          cmdName = "cmd";
          cmd = request.getParameter( cmdName );
      }
      
      // try to get command from the session attribute
      if (cmd == null) {
          Session session = request.getSession( false );
          if (session != null) {
              MailContext mailContext = (MailContext)session.getAttribute( MailContext.SESSION_MAIL_CONTEXT );
              if (mailContext != null) {
                  try {
                      cmd = (String)mailContext.get(MailContext.MAIL_CURRENT_WORKING_COMMAND_ENTRY);
                  } catch (ContextException ce) {
                      String message = "Cannot get command entry " + 
                        String.valueOf(MailContext.MAIL_CURRENT_WORKING_COMMAND_ENTRY) + " " + 
                        "from mailContext from session";
                      getLogger().warn( message, ce );
                  }
              }
          }
      }
      MailCommandBuilder mcb = new MailCommandBuilder();
      boolean isMapped = mcb.isCommandMapped( cmd );
      if (isMapped) {
          return cmd;
      } else {
          // uup the command is invalid, we will surly be not able to map it to a valid
          // AbstractMailAction
          return null;
      }
  }

  public boolean select(String expression, Object selectorContext) {
      if (selectorContext == null) {
          return false;
      } else {
          String cmd = (String)selectorContext;
          return cmd.equals( expression );
      }
  }

}

