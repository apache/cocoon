package javax.activation;

import java.net.URL;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: URLDataSource.java,v 1.1 2003/08/08 11:35:03 cziegeler Exp $
 */

public class URLDataSource implements DataSource {

    public URLDataSource(URL url) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public String getName() {
        throw new NoSuchMethodError("This is a mock object");
    }
}