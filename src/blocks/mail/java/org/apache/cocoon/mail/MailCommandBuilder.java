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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
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
 * @version CVS $Id: MailCommandBuilder.java,v 1.5 2004/05/26 01:56:28 joerg Exp $
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
                ama.enableLogging(getLogger());
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

