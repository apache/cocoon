package javax.mail.internet;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class ContentType {

	public ContentType (String type) throws ParseException {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public String getPrimaryType() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public String getSubType() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public String getBaseType() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public ParameterList getParameterList() {
		throw new NoSuchMethodError("This is a mock object");
	}
}
