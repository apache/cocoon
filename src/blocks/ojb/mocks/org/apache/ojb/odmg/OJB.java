
/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: OJB.java,v 1.1 2004/01/27 06:15:14 giacomo Exp $
 */
package org.apache.ojb.odmg;

import org.odmg.Implementation;

public class OJB {

    public static Implementation getInstance() {
        return null;
    }
}
