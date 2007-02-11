package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public abstract class BodyPart implements Part {

    public void setContent(String a, String b) {
        throw new NoSuchMethodError("This is a mock object");
    }

}
