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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;
//import javax.mail.Session;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.cocoon.environment.Request;

/**
 * Encapsulation of context info of this webmail application
 *
 * @author Bernhard Huber
 * @since 29 December 2002
 * @version CVS $Id: MailContext.java,v 1.3 2004/03/05 13:02:00 bdelacretaz Exp $
 */
public class MailContext extends DefaultContext implements LogEnabled {
    /**
     * attribute name of MailContext object in an application session, eg http-session
     */
    public final static String SESSION_MAIL_CONTEXT = "mail-context";

    /**
     *  Description of the Field
     */
    public final static String MAIL_SESSION_ENTRY = "mail-session";
    /**
     *  Description of the Field
     */
    public final static String MAIL_STORE_ENTRY = "mail-store";

    /**
     *  Description of the Field
     */
    public final static String MAIL_CURRENT_WORKING_FOLDER_ENTRY = "mail-current-working-folder";
    /**
     *  Description of the Field
     */
    public final static String MAIL_CURRENT_WORKING_COMMAND_ENTRY = "mail-current-working-command";

    private Request request;
    private Logger logger;


    /**
     *Constructor for the MailContext object
     *
     *@param  parent  Description of the Parameter
     */
    MailContext(Context parent) {
        super(parent);
    }


    /**
     *  Sets the request attribute of the MailContext object
     *
     *@param  request  The new request value
     */
    public void setRequest(Request request) {
        this.request = request;
    }


    /**
     *  A specialization of the plain Context get method.
     *  <p>
     *    Implementing special key prefixes
     *  </p>
     *  <ul>
     *    <li>"param:key" get key from request parameters
     *    </li>
     *    <li>"param-integer:key" get key from request parameters, casting to Integer
     *    </li>
     *    <li>"param-folder:key" get key from request parameters, ie foldername, lookup
     *      foldername using key "folder:foldername"
     *    </li>
     *    <li>key get key via plain context get method
     *    </li>
     *  </ul>
     *
     *@param  key                   Description of the Parameter
     *@return                       Description of the Return Value
     *@exception  ContextException  Description of the Exception
     */
    public Object get(Object key) throws ContextException {
        String keyString = (String) key;

        final String PARAM_PREFIX_ENTRY = "param:";
        final String PARAM_INTEGER_PREFIX_ENTRY = "param-integer:";
        final String PARAM_FOLDER_PREFIX_ENTRY = "param-folder:";

        if (keyString.startsWith(PARAM_PREFIX_ENTRY)) {
            String paramName = keyString.substring(PARAM_PREFIX_ENTRY.length());
            String paramValue = getParameter(paramName);
            if (paramValue == null) {
                String message = "No parameter " + String.valueOf(keyString) + " available.";
                throw new ContextException(message);
            }
            return paramValue;
        } else if (keyString.startsWith(PARAM_INTEGER_PREFIX_ENTRY)) {
            String paramName = keyString.substring(PARAM_INTEGER_PREFIX_ENTRY.length());
            try {
                Integer paramValue = getParameterAsInteger(paramName);
                return paramValue;
            } catch (NumberFormatException nfe) {
                String message = "Cannot create Integer for parameter " + String.valueOf(keyString);
                throw new ContextException(message, nfe);
            }
        } else if (keyString.startsWith(PARAM_FOLDER_PREFIX_ENTRY)) {
            String paramName = keyString.substring(PARAM_FOLDER_PREFIX_ENTRY.length());
            String folderName = getParameter(paramName);
            if (folderName == null) {
                // no folderName is available in the parameters bag
                // try to get the current working folder
                try {
                    folderName = (String) super.get(MAIL_CURRENT_WORKING_FOLDER_ENTRY);
                } catch (ContextException ce) {
                    // no current working folder entry available
                    String message = "No " + MAIL_CURRENT_WORKING_FOLDER_ENTRY + " entry available ";
                    getLogger().error(message);
                    throw new ContextException(message, ce);
                }
            }

            // get folder object, folderName is okay
            Folder folder = null;
            try {
                folder = (Folder) getFolder(folderName);
            } catch (ContextException ce) {
                // folder is not stored yet

                Store store = (Store) get(MAIL_STORE_ENTRY);
                // get folder, eventually connect the store
                try {
                    if (!store.isConnected()) {
                        store.connect();
                    }
                    final String DEFAULT_FOLDER_NAME = "~";

                    // empty folder name is specified by empty string, or "~"
                    if (folderName.equals(DEFAULT_FOLDER_NAME) || folderName.length() == 0) {
                        folder = store.getDefaultFolder();
                    } else {
                        folder = store.getFolder(folderName);
                    }

                    // save the Folder, for later access
                    putFolder(folder);
                } catch (MessagingException me) {
                    String message = "Cannot get folder " + String.valueOf(folderName);
                    throw new ContextException(message, ce);
                }
            }
            return folder;
        } else {
            return super.get(key);
        }
    }


    /**
     *  Gets the theFolder attribute of the MailContext object
     *
     *@param  entry                 Description of the Parameter
     *@return                       The theFolder value
     *@exception  ContextException  Description of the Exception
     */
    public Folder getTheFolder(String entry) throws ContextException {
        Folder f;
        try {
            f = (Folder) get("param-folder:" + entry);
        } catch (Exception e) {
            String message = "Cannot get Folder object for " + String.valueOf(entry);
            throw new ContextException(message, e);
        }
        return f;
    }


    /**
     *  Gets the folder attribute of the MailContext object
     *
     *@param  folderName            Description of the Parameter
     *@return                       The folder value
     *@exception  ContextException  Description of the Exception
     */
    public Object getFolder(String folderName) throws ContextException {
        // canonicalize folder name
        folderName = canoncializeFoldername(folderName);

        final String key = "folder:" + folderName;
        getLogger().debug("Getting folder " + String.valueOf(key));

        Object o = super.get(key);
        getLogger().debug("Successfully getting folder " + String.valueOf(key) + ": " + String.valueOf(o));
        return o;
    }


    /**
     *  Remove and close Store of this MailContext, implicitly remove all folders, too.
     */
    public void removeStore() {
        try {
            getLogger().info("Remove store " + String.valueOf(this));
            removeAllFolders();

            Map map = getContextData();
            Store store = (Store) map.remove(MAIL_STORE_ENTRY);
            if (store != null) {
                MailCommandManager.closeStore(store);
            }
        } catch (Exception e) {
            String message = "Cannot remove store";
            getLogger().error(message, e);
        }
    }


    /**
     * remove all folders in this MailContext object
     */
    public void removeAllFolders() {
        try {
            getLogger().info("Remove folders " + String.valueOf(this));

            Map map = getContextData();
            Set entrySet = map.entrySet();
            Iterator i = entrySet.iterator();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                String key = (String) me.getKey();
                if (key.startsWith("folder:")) {
                    Folder f = (Folder) me.getValue();
                    MailCommandManager.closeFolder(f);
                    i.remove();
                }
            }
        } catch (Exception e) {
            String message = "Cannot remove all folders";
            getLogger().error(message, e);
        }
    }


    /**
     *  put a folder in this MailContext object map
     *
     *@param  folder                Description of the Parameter
     *@exception  ContextException  Description of the Exception
     */
    public void putFolder(Folder folder) throws ContextException {
        String folderName = folder.getFullName();
        // canonicalize folder name
        folderName = canoncializeFoldername(folderName);

        final String key = "folder:" + folderName;

        getLogger().debug("Putting folder key: " + String.valueOf(key) +
                " folder " + String.valueOf(folder));

        // close folder if folder is overwritten
        try {
            Object objRef = super.get(key);
            if (objRef != null) {
                // close this folder as it is goint to get overwritten
                try {
                    Folder f = (Folder) objRef;
                    MailCommandManager.closeFolder(f);
                } catch (MessagingException me) {
                    String message = "Cannot close folder";
                    getLogger().warn(message, me);
                }
            }
        } catch (ContextException e) {
            // ignore as we set it
        }

        // Shall we garbage collect folders?

        super.put(key, folder);
    }


    /**
     *  Description of the Method
     *
     *@param  folders               Description of the Parameter
     *@exception  ContextException  Description of the Exception
     */
    public void putFolder(Folder[] folders) throws ContextException {
        for (int i = 0; i < folders.length; i++) {
            putFolder(folders[i]);
        }
    }


    /**
     *  Description of the Method
     *
     *@param  logger  Description of the Parameter
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }



    /**
     *  Gets the parameter attribute of the MailContext object
     *
     *@param  key  Description of the Parameter
     *@return      The parameter value
     */
    protected String getParameter(String key) {
        String value = request.getParameter(key);
        return value;
    }


    /**
     *  Gets the parameterAsInteger attribute of the MailContext object
     *
     *@param  key  Description of the Parameter
     *@return      The parameterAsInteger value
     */
    protected Integer getParameterAsInteger(String key) {
        String value = request.getParameter(key);
        Integer i = new Integer(value);
        return i;
    }


    /**
     *  Gets the logger attribute of the MailContext object
     *
     *@return    The logger value
     */
    protected Logger getLogger() {
        return this.logger;
    }


    /**
     *  Description of the Method
     *
     *@param  fn  Description of the Parameter
     *@return     Description of the Return Value
     */
    protected String canoncializeFoldername(String fn) {
        //
        return fn;
    }

}

