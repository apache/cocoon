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

import java.io.IOException;
import java.util.List;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.mail.command.AbstractMailCommand;
import org.apache.cocoon.mail.command.MailCommands;

/**
 * Manage invocation of mail commands.
 *
 * @author Bernhard Huber
 * @since 23 October 2002
 * @version CVS $Id: MailCommandManager.java,v 1.2 2003/03/11 19:04:58 vgritsenko Exp $
 */
public class MailCommandManager extends AbstractLogEnabled {

    /**
     *  Description of the Field
     */
    public final static String DEFAULT_FOLDER_NAME = "INBOX";

    /**
     *  Description of the Field
     */
    public final static String DEFAULT_FOLDER_PATTERN = "%";

    /**
     *  Context key specifying the foldername.
     */
    public final static String CONTEXT_FOLDER_ENTRY = "folder";
    /**
     *  Description of the Field
     */
    public final static String CONTEXT_UID_ENTRY = "uid";
    /**
     *  Description of the Field
     */
    public final static String CONTEXT_ID_ENTRY = "id";
    /**
     *  Description of the Field
     */
    public final static String CONTEXT_PARTID_ENTRY = "part-id";
    /**
     *  Description of the Field
     */
    public final static String CONTEXT_FOLDER_PATTERN_ENTRY = "folder-pattern";
    /**
     *  Description of the Field
     */
    public final static String CONTEXT_MAX_FOLDER_LEVEL_ENTRY = "max-folder-level";


    /**
     *  Creates a new instance of MailHeaderList
     */
    public MailCommandManager() { }


    /**
     *  Open a javamail folder
     *
     *@param  f                       Description of the Parameter
     *@param  mode                    folder opening mode, use Folder.READ_WRITE, or Folder.READ_ONLY
     *@exception  MessagingException  Description of the Exception
     */
    public static void openFolder(Folder f, int mode) throws MessagingException {
        if (!f.isOpen()) {
            f.open(mode);
        }
    }


    /**
     *  Close a javamail folder
     *
     *@param  f                       Description of the Parameter
     *@exception  MessagingException  Description of the Exception
     */
    public static void closeFolder(Folder f) throws MessagingException {
        if (f != null && f.isOpen()) {
            // fix me : do we need expungeOnExit = true?
            f.close(false);
        }
    }


    /**
     *  Open a javamail store
     *
     *@param  s                       Description of the Parameter
     *@exception  MessagingException  Description of the Exception
     */
    public static void openStore(Store s) throws MessagingException {
        if (!s.isConnected()) {
            s.connect();
        }
    }


    /**
     *  Close a javamail store
     *
     *@param  s                       Description of the Parameter
     *@exception  MessagingException  Description of the Exception
     */
    public static void closeStore(Store s) throws MessagingException {
        if (s != null && s.isConnected()) {
            s.close();
        }
    }


    /**
     *  Description of the Method
     *
     *@param  aList  Description of the Parameter
     *@return        Description of the Return Value
     */
    public List execute(List aList) {
        MailCommands folderCommands = new MailCommands(aList);
        try {
            folderCommands.execute();
        } catch (MessagingException me) {
            // log exception
            getLogger().error("Cannot execute", me);
        }
        return folderCommands.getResults();
    }


    /**
     *  Description of the Method
     *
     *@param  amfa  Description of the Parameter
     *@return       Description of the Return Value
     */
    public List execute(AbstractMailCommand amfa) {
        try {
            amfa.execute();
        } catch (MessagingException me) {
            // log exception
            getLogger().error("Cannot execute", me);
        }
        return amfa.getResults();
    }


    /**
     *  Retrieve folder, and put it as command result.
     *
     *@author     Bernhard Huber
     *@created    23. Oktober 2002
     *@version    CVS Version: $Id: MailCommandManager.java,v 1.2 2003/03/11 19:04:58 vgritsenko Exp $
     */
    public static class MailFolderCatCommand extends AbstractMailCommand implements Contextualizable {

        private Folder aFolder;


        /**
         *Constructor for the MailFolderCommand object
         */
        public MailFolderCatCommand() { }


        /**
         *  Description of the Method
         *
         *@param  ctx                   Description of the Parameter
         *@exception  ContextException  Description of the Exception
         */
        public void contextualize(Context ctx) throws ContextException {
            MailContext mctx = (MailContext) ctx;
            this.aFolder = mctx.getTheFolder(CONTEXT_FOLDER_ENTRY);
        }


        /**
         *  Description of the Method
         *
         *@exception  MessagingException  Description of the Exception
         */
        public void execute() throws MessagingException {
            MailCommandManager.openFolder(aFolder, Folder.READ_ONLY);
            addResult(aFolder);
        }
    }


////
    /**
     *  Retrieve folder, and put it as command result.
     *
     *@author     Bernhard Huber
     *@created    23. Oktober 2002
     *@version    CVS Version: $Id: MailCommandManager.java,v 1.2 2003/03/11 19:04:58 vgritsenko Exp $
     */
    public static class MailRefreshFolderCommand extends AbstractMailCommand implements Contextualizable {

        private Folder aFolder;


        /**
         *Constructor for the MailFolderCommand object
         */
        public MailRefreshFolderCommand() { }


        /**
         *  Description of the Method
         *
         *@param  ctx                   Description of the Parameter
         *@exception  ContextException  Description of the Exception
         */
        public void contextualize(Context ctx) throws ContextException {
            MailContext mctx = (MailContext) ctx;
            this.aFolder = mctx.getTheFolder(CONTEXT_FOLDER_ENTRY);
        }


        /**
         *  Description of the Method
         *
         *@exception  MessagingException  Description of the Exception
         */
        public void execute() throws MessagingException {
            MailCommandManager.closeFolder(aFolder);
            MailCommandManager.openFolder(aFolder, Folder.READ_ONLY);
            addResult(aFolder);
        }
    }


    /**
     *  Retrieved headers of all messages of a folder, put
     *   retrieved messages as command result.
     *
     *@author     Bernhard Huber
     *@created    23. Oktober 2002
     *@version    CVS Version: $Id: MailCommandManager.java,v 1.2 2003/03/11 19:04:58 vgritsenko Exp $
     */
    public static class MailListMessagesCommand extends AbstractMailCommand implements Contextualizable {

        private Folder aFolder;


        /**
         *Constructor for the MailAllHeadersCommand object
         */
        public MailListMessagesCommand() { }


        /**
         *  Description of the Method
         *
         *@param  ctx                   Description of the Parameter
         *@exception  ContextException  Description of the Exception
         */
        public void contextualize(Context ctx) throws ContextException {
            // try to get the folder object
            MailContext mctx = (MailContext) ctx;
            this.aFolder = mctx.getTheFolder(CONTEXT_FOLDER_ENTRY);
        }


        /**
         *  Description of the Method
         *
         *@exception  MessagingException  Description of the Exception
         */
        public void execute() throws MessagingException {
            MailCommandManager.openFolder(aFolder, Folder.READ_ONLY);

            // add folder, too
            addResult(aFolder);

            Message[] messages = aFolder.getMessages();

            // Use a suitable FetchProfile
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.FLAGS);
            fp.add("X-Mailer");
            aFolder.fetch(messages, fp);

            // add all messages to the result
            addResult(messages);

        }
    }


    /**
     *  List all subfolders of a folder, put
     *  all retrieved folders as command result.
     *
     *@author     Bernhard Huber
     *@created    23. Oktober 2002
     *@version    CVS Version: $Id: MailCommandManager.java,v 1.2 2003/03/11 19:04:58 vgritsenko Exp $
     */
    public static class MailListFolderCommand extends AbstractMailCommand implements Contextualizable {

        private Folder aFolder;
        private String folderPattern = MailCommandManager.DEFAULT_FOLDER_PATTERN;


        /**
         *Constructor for the MailFoldersCommand object
         */
        public MailListFolderCommand() { }


        /**
         *   Gets the folderPattern attribute of the ListFolderCommand object
         *
         *@return    The folderPattern value
         */
        public String getFolderPattern() {
            return this.folderPattern;
        }


        /**
         *  Description of the Method
         *
         *@param  ctx                   Description of the Parameter
         *@exception  ContextException  Description of the Exception
         */
        public void contextualize(Context ctx) throws ContextException {
            MailContext mctx = (MailContext) ctx;
            this.aFolder = mctx.getTheFolder(CONTEXT_FOLDER_ENTRY);

            try {
                this.folderPattern = (String) ctx.get("param:" + CONTEXT_FOLDER_PATTERN_ENTRY);
            } catch (ContextException ce) {
                // use default folder pattern
                this.folderPattern = MailCommandManager.DEFAULT_FOLDER_PATTERN;
            }
        }


        /**
         *  Description of the Method
         *
         *@exception  MessagingException  Description of the Exception
         */
        public void execute() throws MessagingException {
            // spec say: folder list can be invoked on closed folder MailCommandManager.openFolder(aFolder,Folder.READ_ONLY);
            //addResult(aFolder);
            Folder[] subFolders = aFolder.list(this.folderPattern);
            getLogger().debug("Adding " + String.valueOf(subFolders.length) + " subFolders ");
            for (int i = 0; i < subFolders.length; i++) {
                getLogger().debug("subFolder " + String.valueOf(i) + " name " + subFolders[i].getFullName());
            }
            addResult(subFolders);
        }
    }


    /**
     *  Retrieved a message (envelope plus content) of a folder by its uid, put
     *   retrieved message as command result.
     *
     *@author     Bernhard Huber
     *@created    23. Oktober 2002
     *@version    CVS Version: $Id: MailCommandManager.java,v 1.2 2003/03/11 19:04:58 vgritsenko Exp $
     */
    public static class MailCatMessageByUIDCommand extends AbstractMailCommand implements Contextualizable {

        private int msgUID = 1;
        private Folder aFolder;


        /**
         *Constructor for the MailMessageByUIDCommand object
         */
        public MailCatMessageByUIDCommand() { }


        /**
         *  Description of the Method
         *
         *@param  ctx                   Description of the Parameter
         *@exception  ContextException  Description of the Exception
         */
        public void contextualize(Context ctx) throws ContextException {
            MailContext mctx = (MailContext) ctx;
            this.aFolder = mctx.getTheFolder(CONTEXT_FOLDER_ENTRY);

            Integer i = (Integer) ctx.get("param-integer:" + CONTEXT_UID_ENTRY);
            if (i == null) {
                String message = "Missing mandatory context entry " + String.valueOf(CONTEXT_UID_ENTRY);
                throw new ContextException(message);
            }
            this.msgUID = i.intValue();
        }


        /**
         *  Description of the Method
         *
         *@exception  MessagingException  Description of the Exception
         */
        public void execute() throws MessagingException {
            UIDFolder uidFolder = (UIDFolder) aFolder;
            MailCommandManager.openFolder(aFolder, Folder.READ_ONLY);

            // add folder, too
            addResult(aFolder);

            Message msg = uidFolder.getMessageByUID(msgUID);
            addResult(msg);
        }
    }


    /**
     *  Retrieved a message (envelope plus content) of a folder by its id, put
     *   retrieved message as command result.
     *
     *@author     Bernhard Huber
     *@created    23. Oktober 2002
     *@version    CVS Version: $Id: MailCommandManager.java,v 1.2 2003/03/11 19:04:58 vgritsenko Exp $
     */
    public static class MailCatMessageByIdCommand extends AbstractMailCommand implements Contextualizable {

        private int msgId = 1;
        private Folder aFolder;


        /**
         *Constructor for the MailMessageByIdCommand object
         */
        public MailCatMessageByIdCommand() { }


        /**
         *  Description of the Method
         *
         *@param  ctx                   Description of the Parameter
         *@exception  ContextException  Description of the Exception
         */
        public void contextualize(Context ctx) throws ContextException {
            MailContext mctx = (MailContext) ctx;
            this.aFolder = mctx.getTheFolder(CONTEXT_FOLDER_ENTRY);

            try {
                Integer i = (Integer) ctx.get("param-integer:" + CONTEXT_ID_ENTRY);
                this.msgId = i.intValue();
            } catch (ContextException ce) {
                String message = "Missing mandatory context entry " + String.valueOf(CONTEXT_ID_ENTRY);
                throw new ContextException(message);
            }
        }


        /**
         *  Description of the Method
         *
         *@exception  MessagingException  Description of the Exception
         */
        public void execute() throws MessagingException {
            MailCommandManager.openFolder(aFolder, Folder.READ_ONLY);

            // add folder, too
            addResult(aFolder);

            Message msg = aFolder.getMessage(msgId);
            addResult(msg);
        }
    }


    /**
     *  Retrieved a message part by its part id, specifying the message by id, put
     *   retrieved part as command result.
     *
     *@author     Bernhard Huber
     *@created    23. Oktober 2002
     *@version    CVS Version: $Id: MailCommandManager.java,v 1.2 2003/03/11 19:04:58 vgritsenko Exp $
     */
    public static class MailCatAttachmentMessageByIdCommand extends AbstractMailCommand implements Contextualizable {

        private int msgId = -1;
        private int partId = -1;
        private Folder aFolder;


        /**
         *Constructor for the MailCatAttachmentMessageByIdCommand object
         */
        public MailCatAttachmentMessageByIdCommand() { }


        /**
         *  Description of the Method
         *
         *@param  ctx                   Description of the Parameter
         *@exception  ContextException  Description of the Exception
         */
        public void contextualize(Context ctx) throws ContextException {
            MailContext mctx = (MailContext) ctx;
            this.aFolder = mctx.getTheFolder(CONTEXT_FOLDER_ENTRY);

            Integer i = (Integer) ctx.get("param-integer:" + CONTEXT_ID_ENTRY);
            if (i == null) {
                String message = "Missing mandatory context entry " + String.valueOf(CONTEXT_ID_ENTRY);
                throw new ContextException(message);
            }
            this.msgId = i.intValue();

            i = (Integer) ctx.get("param-integer:" + CONTEXT_PARTID_ENTRY);
            if (i == null) {
                String message = "Missing mandatory context entry " + String.valueOf(CONTEXT_PARTID_ENTRY);
                throw new ContextException(message);
            }
            this.partId = i.intValue();
        }


        /**
         *  Description of the Method
         *
         *@exception  MessagingException  Description of the Exception
         */
        public void execute() throws MessagingException {
            MailCommandManager.openFolder(aFolder, Folder.READ_ONLY);

            // add folder, too
            addResult(aFolder);

            // get the message
            Message msg = aFolder.getMessage(msgId);

            if (msg == null) {
                String message = "Cannot get message for id " + String.valueOf(msgId);
                getLogger().warn(message);
                return;
            }
            try {
                Part part = null;
                Object objRef = msg.getContent();
                if (!(objRef instanceof Multipart)) {
                    String message = "Message of id " + String.valueOf(msgId) + " is not a multipart message!";
                    getLogger().warn(message);
                    return;
                }
                Multipart multipart = (Multipart) objRef;
                int numParts = multipart.getCount();

                if (partId < numParts) {
                    part = multipart.getBodyPart(partId);
                } else {
                    String message = "Invalid part id " + String.valueOf(this.partId) + " of message id " + String.valueOf(this.msgId);
                    getLogger().warn(message);
                }
                addResult(part);
            } catch (IOException ioe) {
                String message = "Cannot get content of " +
                        "message for id " + String.valueOf(msgId);
                throw new MessagingException(message, ioe);
            }
        }
    }


    /**
     *  Description of the Class
     *
     *@author     Administrator
     *@created    02. J�nner 2003
     *@version    CVS Version: $Id: MailCommandManager.java,v 1.2 2003/03/11 19:04:58 vgritsenko Exp $
     */
    public static class MailSearchMessagesCommand extends AbstractMailCommand implements Contextualizable {
        private Folder aFolder;
        private SearchTerm searchTerm;


        /**
         *  Description of the Method
         *
         *@param  ctx                   Description of the Parameter
         *@exception  ContextException  Description of the Exception
         */
        public void contextualize(Context ctx) throws ContextException {
            MailContext mctx = (MailContext) ctx;
            this.aFolder = mctx.getTheFolder(CONTEXT_FOLDER_ENTRY);

            String searchString = (String) ctx.get("param:" + "search");
            if (searchString == null) {
                searchString = "";
            }
            searchTerm = new OrTerm(
                    new SubjectTerm(searchString),
                    new FromStringTerm(searchString));

            // build searchTerm from searchTermString

            /* proposed searchTermString syntax
              {header}:comp-op:{value} & {header}:comp-op:{value} | .....
              eg. subject:eq:cocoon & date::MM/DD/2002
              header:com-op:
                subject:[cont|ncont]:
                sender:[cont|ncont]:
                body:[cont|ncont]:
                date:[is|nis|before|after]
                status:[is|nis]:[Read|New|Replied|Forwarded]
                to:[cont|ncont]:
                cc:
                age-in-days:[is|nis|gt|lt]:
                reply-to:[cont|ncont]:
            */
        }


        /**
         *  Description of the Method
         *
         *@exception  MessagingException  Description of the Exception
         */
        public void execute() throws MessagingException {
            MailCommandManager.openFolder(aFolder, Folder.READ_ONLY);

            // add folder, too
            addResult(aFolder);

            Message[] msgs = aFolder.search(searchTerm);
            addResult(msgs);
        }
    }
}

