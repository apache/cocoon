package org.apache.cocoon.matcher;

import org.apache.cocoon.Request;

public abstract class AuthenticationMatcher implements Matcher {

	protected String username_parameter = "username";
	protected String password_parameter = "password";

	public boolean match(Request request) {
		return match(request.getParameter(username_parameter),request.getParameter(password_parameter));
	}

	abstract public boolean match(String username, String password);

}
