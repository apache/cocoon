package org.apache.cocoon.matcher;

import org.apache.cocoon.Request;

public interface Matcher {

	public boolean match(Request request);

}
