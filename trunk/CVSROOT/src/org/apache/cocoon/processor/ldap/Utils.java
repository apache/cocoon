/*
 * Copyright (c) 1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software and design ideas developed by the Java 
 *    Apache Project (http://java.apache.org/)."
 *
 * 4. The names "Cocoon", "Cocoon Servlet" and "Java Apache Project" must 
 *    not be used to endorse or promote products derived from this software 
 *    without prior written permission.
 *
 * 5. Products derived from this software may not be called "Cocoon"
 *    nor may "Cocoon" and "Java Apache Project" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software and design ideas developed by the Java 
 *    Apache Project (http://java.apache.org/)."
 *           
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *           
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Project. For more information
 * on the Java Apache Project please see <http://java.apache.org/>.
 */

package org.apache.cocoon.processor.ldap;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import org.w3c.dom.*;

/**
 * Utility methods for this processor.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.1 $ $Date: 1999-12-18 00:37:25 $
 */

public class Utils {

	public static final String ERROR_ELEMENT = "error-element";
	public static final String ERROR_MESSAGE_ATTRIBUTE = "error-message-attribute";
	public static final String ERROR_MESSAGE_ELEMENT = "error-message-element";
	public static final String ERROR_STACKTRACE_ATTRIBUTE = "error-stacktrace-attribute";
	public static final String ERROR_STACKTRACE_ELEMENT = "error-stacktrace-element";
	public static final String ERROR_MESSAGE_TEXT = "error-message-text";

	public static Element createErrorElement(Document document, Properties props, Exception e) {
		Element element = document.createElement(props.getProperty(ERROR_ELEMENT));
		String message_attribute = props.getProperty(ERROR_MESSAGE_ATTRIBUTE);
		String message_element = props.getProperty(ERROR_MESSAGE_ELEMENT);
		String message_text = props.getProperty(ERROR_MESSAGE_TEXT);
		if (message_text == null) message_text = e.getMessage();
		if (!message_attribute.equals(""))
			element.setAttribute(message_attribute,e.getMessage());
		if (!message_element.equals("")) {
			Element child = document.createElement(message_element);
			child.appendChild(document.createTextNode(e.getMessage()));
			element.appendChild(child);
		}
		String stacktrace_attribute = props.getProperty(ERROR_STACKTRACE_ATTRIBUTE);
		String stacktrace_element = props.getProperty(ERROR_STACKTRACE_ELEMENT);
		String stacktrace = null;
		if (!stacktrace_attribute.equals("") && !stacktrace_element.equals("")) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				stacktrace = sw.toString();
		}
		if (!stacktrace_attribute.equals(""))
			element.setAttribute(stacktrace_attribute,stacktrace);
		if (!stacktrace_element.equals("")) {
			Element child = document.createElement(stacktrace_element);
			child.appendChild(document.createTextNode(stacktrace));
			element.appendChild(child);
		}
		return element;
	}

}
