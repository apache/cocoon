package org.apache.cocoon.generation;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

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
 * TODO Refactor all of this to use the MailCommandManager, etc...
 *
 * @author <a href="mailto:tony@apache.org">Tony Collen</a>
 * @version CVS $Id: IMAPGenerator.java,v 1.5 2003/12/22 13:38:22 joerg Exp $
 */
public class IMAPGenerator extends AbstractGenerator {
    
    static final String URI = "http://apache.org/cocoon/imap/1.0/";
    static final String PREFIX = "imap";

    private String host;
    private String user;
    private String pass;
    
    private Properties props = new Properties();
    private Message message[] = null;
    
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

            AttributesImpl attr = new AttributesImpl();

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

            start("imap", attr);

            start("messages", attr);

            for (i = 0; i < this.message.length; i++) {

            // loop through the messages and output XML.
            // TODO: actually use the attributes...

            start("msg", attr);

            start("subject", attr);
            data( this.message[i].getSubject() );
            end("subject");

            start("from", attr);
            data( this.message[i].getFrom()[0].toString() );
            end("from");

            start("sentDate", attr);
            data( this.message[i].getSentDate().toString() );
            end("sentDate");

            start("num", attr);
            data( Integer.toString( this.message[i].getMessageNumber() ) );
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
            throw new ProcessingException( e.toString() );
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

    private void start(String name, AttributesImpl attr)
    throws SAXException 
    {
        super.contentHandler.startElement(URI, name, PREFIX + ":" + name, attr);
        attr.clear();
    }

    private void end(String name) 
    throws SAXException
    {
        super.contentHandler.endElement(URI, name, PREFIX + ":" + name);
    }
    
    private void data(String data) 
    throws SAXException
    {
        super.contentHandler.characters( data.toCharArray(), 0, data.length() );
    }
    
    private void log(String msg)
    {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug(msg);
        }
    }
    
}
