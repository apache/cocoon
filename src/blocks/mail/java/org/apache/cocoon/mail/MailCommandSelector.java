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
 * @version CVS $Id: MailCommandSelector.java,v 1.2 2003/03/11 19:04:58 vgritsenko Exp $
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

