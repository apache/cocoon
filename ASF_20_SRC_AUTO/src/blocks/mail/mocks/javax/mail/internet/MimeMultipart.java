package javax.mail.internet;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class MimeMultipart extends Multipart {

    public int getCount() {
        throw new NoSuchMethodError("This is a mock object");
    }

    public BodyPart getBodyPart(int index) throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }
}
