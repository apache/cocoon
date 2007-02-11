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

import java.util.HashMap;
import java.util.Map;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.cocoon.mail.command.AbstractMailCommand;

/**
 * Build an AbstractMailCommand from MailContext.
 * <p>
 *   As a user requests a command, the command is mapped to an MailCommand instance.
 *   The registration of MailCommand, and the resolution of a command string to
 *   a command instance are the tasks of this class.
 * </p>
 *
 * @author Bernhard Huber
 * @since 28. Dezember 2002
 * @version CVS $Id: MailCommandBuilder.java,v 1.3 2003/09/24 21:22:33 cziegeler Exp $
 */
public class MailCommandBuilder extends AbstractLogEnabled {

    // global factory settings
    private Map cmdMap;

    /**
     *Constructor for the MailCommandBuilder object
     */
    public MailCommandBuilder() {
        configure();
    }


    /**
     *  Build a mail command.
     *
     *@param  mailContext  Description of the Parameter
     *@return              Description of the Return Value
     */
    public AbstractMailCommand buildAbstractMailCommand(MailContext mailContext) {
        AbstractMailCommand ama = null;

        try {
            // request parameter say "what"
            String cmd = mailContext.getParameter("cmd");
            if (cmd == null) {
                cmd = (String)mailContext.get( MailContext.MAIL_CURRENT_WORKING_COMMAND_ENTRY );
            }
            Class clazz = getClassForCommand(cmd);
            if (clazz != null) {
                ama = (AbstractMailCommand) clazz.newInstance();
                // enable logging of the mail command
                if (ama instanceof LogEnabled) {
                    ((LogEnabled) ama).enableLogging(getLogger());
                }
                // contextualize the mail command
                if (ama instanceof Contextualizable) {
                    ((Contextualizable) ama).contextualize(mailContext);
                }
                return ama;
            } else {
                getLogger().error("Cmd " + String.valueOf(cmd) + " is invalid");
            }
        } catch (Exception e) {
            String message = "Cannto build AbstractMailCommand";
            getLogger().error(message, e);
        }
        return ama;
    }


    /**
     * get Class for a command
     *
     * @param cmd the command
     * @return Class associated with cmd, or null iff cmd is not mapped to any class
     */
    protected Class getClassForCommand( String cmd ) {
        Class clazz = (Class)cmdMap.get( cmd );
        return clazz;
    }
    
    /**
     * test if command is mapped to a Command class
     *
     * @param cmd the command
     * @return true iff command is mapped to a Class, otherwise return false
     */
    public boolean isCommandMapped( String cmd ) {
        return cmdMap.containsKey( cmd );
    }
    
    /**
     *  configure the cmd to mail command class mapping.
     *  <p>
     *    New commands are registered here. A command name is associated with 
     *    each command class.
     *  </p>
     */
    public void configure() {
        cmdMap = new HashMap();
        cmdMap.put("cat-folder", MailCommandManager.MailFolderCatCommand.class);
        cmdMap.put("refresh-folder", MailCommandManager.MailRefreshFolderCommand.class);
        cmdMap.put("list-folder", MailCommandManager.MailListFolderCommand.class);
        cmdMap.put("list-folder-messages", MailCommandManager.MailListMessagesCommand.class);
        cmdMap.put("search-folder-messages", MailCommandManager.MailSearchMessagesCommand.class);
        cmdMap.put("cat-message-by-uid", MailCommandManager.MailCatMessageByUIDCommand.class);
        cmdMap.put("cat-message-by-id", MailCommandManager.MailCatMessageByIdCommand.class);
        cmdMap.put("cat-attachment-of-message-by-id", MailCommandManager.MailCatAttachmentMessageByIdCommand.class);
    }

}

