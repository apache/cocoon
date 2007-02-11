package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class Flags {

	public static final class Flag {
		public final static Flag ANSWERED = null;
		public final static Flag DELETED = null;
		public final static Flag DRAFT = null;
		public final static Flag FLAGGED = null;
		public final static Flag RECENT = null;
		public final static Flag SEEN = null;
	}
	
	public Flag[] getSystemFlags() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public String[] getUserFlags() {
		throw new NoSuchMethodError("This is a mock object");
	}

}
