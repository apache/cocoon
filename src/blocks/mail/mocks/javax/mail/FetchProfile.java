package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class FetchProfile {

	public static class Item {
		public static final Item ENVELOPE = null;
		public static final Item FLAGS = null;
	}

	public void add(Item item) {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public void add(String item) {
		throw new NoSuchMethodError("This is a mock object");
	}
}
