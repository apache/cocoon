package org.apache.cocoon.processor.sql;

import org.w3c.dom.*;
import java.sql.*;
import java.text.*;
import java.util.*;

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
		System.err.println("TRYING TO ADD FORMAT FOR NAME: "+name+" TYPE: "+type);
		if (format == null || format.equals("")) {
			return;
		}
		if (name == null || name.equals("") && (type != null && !type.equals(""))) {
			System.err.println("ADDING TYPE: "+type);
			formats_by_type.put(type,format);
		} else {
			System.err.println("ADDING NAME: "+name);
			formats_by_name.put(name,format);
		}
	}

	protected String getFormat(Column column) {
		System.err.println("GETTING FORMAT FOR "+column.name+" "+column.type);
		String format = formats_by_name.getProperty(column.name);
		if (format == null) {
			format = formats_by_type.getProperty(column.type);
		}
		return format;
	}

	protected String formatColumn(Column column, ResultSet rs, int i) throws SQLException {
		String format = getFormat(column);
		String value;
		if (format != null) {
			if (column.type.equals("timestamp") ||
				column.type.equals("time") ||
				column.type.equals("date") ||
				column.type.equals("datetime")) {
					SimpleDateFormat date_format = new SimpleDateFormat(format);
					return date_format.format(rs.getDate(i));
			}
		}
		return rs.getString(i);
	}

}
