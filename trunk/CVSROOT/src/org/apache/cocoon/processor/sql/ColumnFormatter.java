/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 */

package org.apache.cocoon.processor.sql;

import org.w3c.dom.*;
import java.sql.*;
import java.text.*;
import java.util.*;

/**
 * This class allows certain columns to be formatted specially. If it gets
 * too complex, it should probably be broken apart into a manager and a
 * set of specific formatting classes (e.g. one for dates, one to transform
 * text into an HTML tree according to certain rules, etc.)
 *
 * @author <a href="balld@webslingerz.com">Donald A. Ball Jr.</a>
 * @version does this really change magically? let's see.
 */
class ColumnFormatter {

	protected Properties formats_by_name = new Properties();
	protected Properties formats_by_type = new Properties();

	protected ColumnFormatter(Element query_element) {
		NodeList children = query_element.getElementsByTagName("column");
		Element ary[] = new Element[children.getLength()];
		for (int i=0; i<ary.length; i++) {
			ary[i] = (Element)children.item(i);
		}
		for (int i=0; i<ary.length; i++) {
			Element element = ary[i];
			String name = element.getAttribute("name");
			String type = element.getAttribute("type");
			String format = element.getAttribute("format");
			putFormat(name,type,format);
		}
	}

	protected void putFormat(String name, String type, String format) {
		if (format == null || format.equals("")) {
			return;
		}
		if (name == null || name.equals("") && (type != null && !type.equals(""))) {
			formats_by_type.put(type,format);
		} else {
			formats_by_name.put(name,format);
		}
	}

	protected String getFormat(Column column) {
		String format = formats_by_name.getProperty(column.name);
		if (format == null) {
			format = formats_by_type.getProperty(column.type);
		}
		return format;
	}

	protected void addColumnNode(Document document, Element parent, Column column, ResultSet rs, int i, String value) throws SQLException {
		String format = getFormat(column);
		if (format != null) {
			if (column.type.equals("timestamp") || column.type.equals("time") || column.type.equals("date") || column.type.equals("datetime")) {
				SimpleDateFormat date_format = new SimpleDateFormat(format);
				parent.appendChild(document.createTextNode(date_format.format(rs.getDate(i))));
				return;
			} else if (column.type.equals("varchar") || column.type.equals("text")) {
				if (format.equals("br")) {
					StringBuffer sb = new StringBuffer();
					StringCharacterIterator iter = new StringCharacterIterator(rs.getString(i));
					for (char c = iter.first(); c != iter.DONE; c = iter.next()) {
						if (c == '\n') {
							if (sb.length() > 0) {
								parent.appendChild(document.createTextNode(sb.toString()));
								sb.setLength(0);
							}
							parent.appendChild(document.createElement("br"));
						} else {
							sb.append(c);
						}
					}
					if (sb.length() > 0) {
						parent.appendChild(document.createTextNode(sb.toString()));
					}
					return;
				}
			}
		}
		parent.appendChild(document.createTextNode(value));
	}

}
