package org.apache.cocoon.formatter;

import org.apache.cocoon.framework.*;

/**
 * This class implements an abstract formatter based on XSLP publishing classes.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:19 $
 */

public abstract class XSLPFormatter implements Formatter, Configurable, Status {

    public static final String XMLDECL = "<?xml version=\"1.0\"?>";

    protected int spaces;
    
	public void init(Configurations conf) {
        this.spaces = Integer.parseInt((String) conf.get("indent_spaces", "1"));
	}
}