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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Provider;
import javax.mail.Store;
import javax.mail.URLName;

import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.mail.command.AbstractMailCommand;

/**
 * This action creates javamail objects, and puts XMLizable object wrappers
 * of these objects into the request attribute map.
 * <p>
 *  This action enables javamail access as action. It creates an http sesion,
 *  and puts the MailContext object into the session attributes.
 * </p>
 *
 * @see MailContext
 *
 * @author Bernhard Huber
 * @version CVS $Id: MailAction.java,v 1.6 2004/03/05 13:02:00 bdelacretaz Exp $
 * @since Cocoon 2.1, 16 December 2002
 */
public class MailAction extends ServiceableAction implements ThreadSafe {

    /**
     *  Request attribute name of a XMLizable folder
     */
    public final static String REQUEST_ATTRIBUTE_FOLDER = "folder";
    /**
     *  Request attribute name of a XMLizable folders object
     */
    public final static String REQUEST_ATTRIBUTE_FOLDERS = "folders";
    /**
     *  Request attribute name of a XMLizable message object
     */
    public final static String REQUEST_ATTRIBUTE_MESSAGE = "message";
    /**
     *  Request attribute name of a XMLizable messages object
     */
    public final static String REQUEST_ATTRIBUTE_MESSAGES = "messages";


    /**
     * Execute mail commands.
     *
     *@param  redirector     Cocoon's redirector
     *@param  resolver       Cocoon's source resolver, used for testing if a source is resolvable
     *@param  source         the source, e.g.: index.html
     *@param  objectModel    Description of the Parameter
     *@param  par            Description of the Parameter
     *@exception  Exception  Description of the Exception
     */
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters par) throws Exception {
        Map actionMap = new HashMap();

        Request request = ObjectModelHelper.getRequest(objectModel);

        String command = request.getParameter("cmd");
        String folderName = request.getParameter("folder");
        String userid = request.getParameter("mail-userid");
        String password = request.getParameter("mail-password");

        // assert mailContext is available
        Session session = request.getSession(true);
        MailContext mailContext = (MailContext) session.getAttribute(MailContext.SESSION_MAIL_CONTEXT);
        if (mailContext == null) {
            // no mailContext is yet available
            // create it and put it into http-session
            mailContext = new MailContextHttpSession(null);
            mailContext.enableLogging(getLogger());
            session.setAttribute(MailContext.SESSION_MAIL_CONTEXT, mailContext);
        }

        // assert mailSession is available
        javax.mail.Session mailSession = null;
        Store mailStore = null;
        try {
            try {
                mailSession = (javax.mail.Session) mailContext.get(MailContext.MAIL_SESSION_ENTRY);
            } catch (ContextException ce) {
                // build session properties
                Properties sessionProperties = new Properties();
                String[] allParameterNames = par.getNames();
                for (int i = 0; i < allParameterNames.length; i++) {
                    String parameterName = allParameterNames[i];
                    final String PARAMETER_NAME_PREFIX = "javax.mail.Session.props:";
                    if (parameterName.startsWith(PARAMETER_NAME_PREFIX)) {
                        String sessionPropName = parameterName.substring(PARAMETER_NAME_PREFIX.length());
                        String sessionPropValue = par.getParameter(parameterName, null);
                        if (sessionPropValue != null) {
                            getLogger().debug("Add session property " +
                                    String.valueOf(sessionPropName) + ": " +
                                    String.valueOf(sessionPropValue));
                            sessionProperties.put(sessionPropName, sessionPropValue);
                        }
                    }
                }
                mailSession = javax.mail.Session.getDefaultInstance(sessionProperties, null);
                checkProviders(mailSession);
                mailContext.put(MailContext.MAIL_SESSION_ENTRY, mailSession);
            }
        } catch (Exception e) {
            String message = "Cannot create mail session";
            getLogger().error(message, e);
            throw new ProcessingException(message, e);
        }

        // assert mailStore is available
        String storeURLNameExpanded = null;
        String storeURLNameTemplate = par.getParameter("store-urlname", null);
        try {
            try {
                mailStore = (Store) mailContext.get(MailContext.MAIL_STORE_ENTRY);
            } catch (ContextException ce) {

                // imap://{userid}:{password}@host:port/
                storeURLNameExpanded = getURLNameExpanded(storeURLNameTemplate, userid, password);

                URLName urlNameExpanded = new URLName(storeURLNameExpanded);
                getLogger().info("get store using URLName " + String.valueOf(urlNameExpanded));
                mailStore = mailSession.getStore(urlNameExpanded);
                mailStore.connect();
                mailContext.put(MailContext.MAIL_STORE_ENTRY, mailStore);
            }
        } catch (Exception e) {
            String message = "Cannot get store, and connect " + String.valueOf(storeURLNameExpanded);
            getLogger().error(message, e);
            throw new ProcessingException(message, e);
        }

        if (folderName != null) {
            // make folderName the current working folder (a la cwd)
            // check foldername a bit
            mailContext.put(MailContext.MAIL_CURRENT_WORKING_FOLDER_ENTRY, folderName);
        } else {
            // no folderName in request parameter, retrieve current working folder
            folderName = (String) mailContext.get(MailContext.MAIL_CURRENT_WORKING_FOLDER_ENTRY);
        }
        actionMap.put(MailContext.MAIL_CURRENT_WORKING_FOLDER_ENTRY, folderName);

        if (command != null) {
            mailContext.put(MailContext.MAIL_CURRENT_WORKING_COMMAND_ENTRY, command);
        } else {
            command = (String) mailContext.get(MailContext.MAIL_CURRENT_WORKING_COMMAND_ENTRY);
        }
        actionMap.put(MailContext.MAIL_CURRENT_WORKING_COMMAND_ENTRY, command);

        // mailSession and mailStore are available

        // excecute mail command, and populate request attribute
        mailContext.setRequest(request);
        populateRequestAttribute(request, mailContext);
        // it's better to release ref to request, as it is not needed, and helps
        // recycling of the request
        mailContext.setRequest(null);

        return actionMap;
    }


    /**
     *  Gets the uRLNameExpanded attribute of the MailGenerator object
     *
     *@param  storeURLNameTemplate  Description of the Parameter
     *@param  userid                Description of the Parameter
     *@param  password              Description of the Parameter
     *@return                       The uRLNameExpanded value
     */
    protected String getURLNameExpanded(String storeURLNameTemplate, String userid, String password) {
        String tokenStart = "''";
        String tokenEnd = "''";
        Properties filters = new Properties();
        filters.put("mail-userid", userid);
        filters.put("mail-passwd", password);

        String filteredURLName = filter(tokenStart, tokenEnd, storeURLNameTemplate, filters);
        return filteredURLName;
    }


    /**
     * replace occurences of <code>TOKEN_STARTxxxTOKEN_END</code> by value of entry xxx in tokens table.
     *
     *@param  tokenStart  token start marker
     *@param  tokenEnd    token end marker
     *@param  s           the string examined
     *@param  tokens      Description of the Parameter
     *@return             String replaced all tokenized entries of original String s.
     */
    protected String filter(final String tokenStart, final String tokenEnd, String s, Properties tokens) {
        int index = s.indexOf(tokenStart);

        if (index > -1) {
            try {
                StringBuffer b = new StringBuffer();
                int i = 0;
                String token = null;
                String value = null;

                do {
                    int endIndex = s.indexOf(tokenEnd, index + tokenStart.length() + 1);
                    if (endIndex == -1) {
                        break;
                    }
                    token = s.substring(index + tokenStart.length(), endIndex);
                    b.append(s.substring(i, index));
                    if (tokens.containsKey(token)) {
                        value = (String) tokens.get(token);
                        b.append(value);
                        i = index + tokenStart.length() + token.length() + tokenEnd.length();
                    } else {
                        // just append TOKEN_START and search further
                        b.append(tokenStart);
                        i = index + tokenStart.length();
                    }
                } while ((index = s.indexOf(tokenStart, i)) > -1);

                b.append(s.substring(i));
                return b.toString();
            } catch (StringIndexOutOfBoundsException e) {
                return s;
            }
        } else {
            return s;
        }
    }


    /**
     *  Check that the provider need is available
     *
     *@param  session  The javamail Session used for checking its providers.
     */
    protected void checkProviders(javax.mail.Session session) {
        Provider[] providers = session.getProviders();
        // just log the available providers
        for (int i = 0; i < providers.length; i++) {
            getLogger().info("mail provider " + providers[i]);
        }
    }


    /**
     *  Populate request attribute map.
     *  <p>
     *    Execute mail command, and populate request attribute map with
     *    XMLizable javamail objects, created by the mail command
     *  </p>
     *
     *@param  request        triggering the creation of javamail objects
     *@param  mailContext    javamail context, store, session, folders
     *@exception  Exception  Description of the Exception
     */
    protected void populateRequestAttribute(Request request, MailContext mailContext) throws Exception {
        String folderName = (String) mailContext.get(MailContext.MAIL_CURRENT_WORKING_FOLDER_ENTRY);
        request.setAttribute(MailContext.MAIL_CURRENT_WORKING_FOLDER_ENTRY, folderName);
        String command = (String) mailContext.get(MailContext.MAIL_CURRENT_WORKING_COMMAND_ENTRY);
        request.setAttribute(MailContext.MAIL_CURRENT_WORKING_COMMAND_ENTRY, command);

        // build javamail objects
        List javaMailResult = retrieveJavaMailObjects(mailContext);
        Iterator javaMailResultIterator;

        // put javamail objects into request attribute map
        javaMailResultIterator = javaMailResult.iterator();
        //Request request = ObjectModelHelper.getRequest(objectModel);
        putXMLizerToRequestAttribute(request, javaMailResultIterator);
    }


    /**
     *  Put XMLizable javamail objects into request attribute map
     *
     *@param  request         holding the destination attribute map
     *@param  resultIterator  Iterator of
     */
    protected void putXMLizerToRequestAttribute(Request request, Iterator resultIterator) {
        if (resultIterator != null) {
            // marshal java mail objects
            Logger logger = getLogger();

            // make it an optional parameter?
            String datePattern = "dd.MM.yyyy HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(datePattern);

            while (resultIterator.hasNext()) {
                Object objRef = resultIterator.next();

                getLogger().debug("Creating XMLizer for " + String.valueOf(objRef));

                if (objRef instanceof Folder) {
                    MailContentHandlerDelegate.FolderXMLizer fx = new MailContentHandlerDelegate.FolderXMLizer((Folder) objRef);
                    fx.enableLogging(logger);
                    request.setAttribute(REQUEST_ATTRIBUTE_FOLDER, fx);
                } else if (objRef instanceof Folder[]) {
                    Folder[] folders = (Folder[]) objRef;
                    MailContentHandlerDelegate.FolderXMLizer[] fxs = new MailContentHandlerDelegate.FolderXMLizer[folders.length];
                    for (int i = 0; i < folders.length; i++) {
                        fxs[i] = new MailContentHandlerDelegate.FolderXMLizer(folders[i]);
                        fxs[i].enableLogging(logger);
                    }
                    // trust that array of XMLizable is handled
                    request.setAttribute(REQUEST_ATTRIBUTE_FOLDERS, fxs);
                } else if (objRef instanceof Message) {
                    MailContentHandlerDelegate.MessageXMLizer mx = new MailContentHandlerDelegate.MessageXMLizer((Message) objRef);
                    mx.enableLogging(logger);
                    mx.setSimpleDateFormat(sdf);
                    request.setAttribute(REQUEST_ATTRIBUTE_MESSAGE, mx);
                } else if (objRef instanceof Message[]) {
                    MailContentHandlerDelegate.MessageEnvelopeXMLizer mex = new MailContentHandlerDelegate.MessageEnvelopeXMLizer((Message[]) objRef);
                    mex.enableLogging(logger);
                    mex.setSimpleDateFormat(sdf);
                    request.setAttribute(REQUEST_ATTRIBUTE_MESSAGES, mex);
                }
            }
        }
    }


    /**
     *   Retrieve javamail objects
     *
     *@param  mailContext              Description of the Parameter
     *@return                          List of retrieved javamail objects
     *@exception  ProcessingException  thrown iff retrieval fails
     */
    protected List retrieveJavaMailObjects(MailContext mailContext) throws ProcessingException {

        List result = null;
        try {
            // do we have a MailCommandManager ?
            MailCommandManager mam = new MailCommandManager();
            mam.enableLogging(getLogger());

            // build the MailCommand(s)
            MailCommandBuilder mab = new MailCommandBuilder();
            mab.enableLogging(getLogger());
            AbstractMailCommand ama = mab.buildAbstractMailCommand(mailContext);

            getLogger().debug("Executing " + String.valueOf(ama));

            // execute the command(s)
            result = mam.execute(ama);

            // return the javamail objects
            return result;
        } catch (Exception e) {
            String message = "Cannot retrieve javamail objects";
            getLogger().error(message, e);
            throw new ProcessingException(message, e);
        }
    }
}

