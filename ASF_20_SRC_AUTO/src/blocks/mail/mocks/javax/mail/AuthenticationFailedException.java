package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 */
public class AuthenticationFailedException extends MessagingException {

    public AuthenticationFailedException() {
        throw new NoSuchMethodError("This is a mock object");
    }

    public AuthenticationFailedException(String s) {
        throw new NoSuchMethodError("This is a mock object");
    }

}
