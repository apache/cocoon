/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.generation;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Generates an XML listing of messages from an IMAP mail server.
 *
 * <p>You <b>must</b> configure this generator with "host", "user", and "pass" parameters
 * which specifies the mail server host, the user to login as, and the password to use,
 * respectively.  Beware that these passwords will be sent cleartext since the Generator
 * does not use an SSL-enabled IMAP connection.</p>
 *
 * <p>Also beware that storing sensitive data, (such as mail usernames and passwords) can
 * be very dangerous, so please be very careful in the method by which you send the user
 * and password parameters to the generator.</p>
 *
 * Instructions: get the JavaMail API jar from http://java.sun.com/products/javamail/, and
 * the JAF activation.jar from http://java.sun.com/beans/glasgow/jaf.html.  Put mail.jar
 * and activation.jar in xml-cocoon2/lib/local/, and recompile.  These jars could actually be
 * moved to lib/optional and added to jars.xml in the future.
 *
 * <br>TODO Refactor all of this to use the MailCommandManager, etc...
 *
 * @cocoon.sitemap.component.documentation
 * Generates an XML listing of messages from an IMAP mail server.
 *
 * @version $Id$
 */
public class IMAPGenerator extends AbstractGenerator {

    static final String URI = "http://apache.org/cocoon/imap/1.0/";
    static final String PREFIX = "imap";

    private String host;
    private String user;
    private String pass;

    private Properties props = new Properties();
    private Message message[];

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {

        // TODO: the default values should be something else...
        this.host = par.getParameter("host", "none");
        this.user = par.getParameter("user", "none");
        this.pass = par.getParameter("pass", "none");

        if (this.host.equals("none") ||
            this.user.equals("none") ||
            this.pass.equals("none")) {

            throw new ProcessingException("You must configure this generator with host, user, and pass parameters.");
        }
    }

    public void generate()
    throws SAXException, ProcessingException {

        try {
            Session sess = Session.getDefaultInstance(this.props, null);
            Store st = sess.getStore("imap");

            log("Connecting to IMAP server @ " + this.host);
            st.connect(this.host, this.user, this.pass);

            log("Attempting to open default folder");
            Folder f = st.getFolder("inbox");

            f.open(Folder.READ_WRITE);

            log("Downloading message list from folder");
            this.message = f.getMessages();

            int i = 0;

            log("Starting XML generation");
            this.contentHandler.startDocument();
            this.contentHandler.startPrefixMapping(PREFIX, URI);

            start("imap", XMLUtils.EMPTY_ATTRIBUTES);
            start("messages", XMLUtils.EMPTY_ATTRIBUTES);

            for (i = 0; i < this.message.length; i++) {
                // Loop through the messages and output XML.
                // TODO: actually use the attributes...

                start("msg", XMLUtils.EMPTY_ATTRIBUTES);

                start("subject", XMLUtils.EMPTY_ATTRIBUTES);
                data(this.message[i].getSubject());
                end("subject");

                start("from", XMLUtils.EMPTY_ATTRIBUTES);
                data(this.message[i].getFrom()[0].toString());
                end("from");

                start("sentDate", XMLUtils.EMPTY_ATTRIBUTES);
                data(this.message[i].getSentDate().toString());
                end("sentDate");

                start("num", XMLUtils.EMPTY_ATTRIBUTES);
                data(Integer.toString(this.message[i].getMessageNumber()));
                end("num");

                end("msg");
            }

            end("messages");
            end("imap");

            this.contentHandler.endPrefixMapping(PREFIX);
            this.contentHandler.endDocument();

            log("Finished generating XML");

        } catch (AuthenticationFailedException afe) {
            throw new ProcessingException("Failed to authenticate with the IMAP server.");
        } catch (Exception e) {
            // TODO: be more specific when catching this exception...
            throw new ProcessingException(e.toString());
        }
    }

    /**
     * Recycle the generator by removing references
     */
    public void recycle() {
        this.host = null;
        this.user = null;
        this.pass = null;

        this.props = null;
        this.message = null;

        super.recycle();
    }

    private void start(String name, Attributes attr)
    throws SAXException {
        super.contentHandler.startElement(URI, name, PREFIX + ":" + name, attr);
    }

    private void end(String name)
    throws SAXException {
        super.contentHandler.endElement(URI, name, PREFIX + ":" + name);
    }

    private void data(String data)
    throws SAXException {
        super.contentHandler.characters( data.toCharArray(), 0, data.length() );
    }

    private void log(String msg) {
        getLogger().debug(msg);
    }
}
