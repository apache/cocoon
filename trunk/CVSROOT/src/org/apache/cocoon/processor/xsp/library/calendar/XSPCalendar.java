/**
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.processor.xsp.library.calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/*
 * a class to generate xml calendar views
 *
 * @author Donald A. Ball Jr.
 * @version 1.0
 */
public class XSPCalendar {

	public static SimpleDateFormat month_number_format = new SimpleDateFormat("M");
	public static SimpleDateFormat month_name_format = new SimpleDateFormat("MMMM");
	public static SimpleDateFormat month_format = new SimpleDateFormat("yyyy-MM");

	/** Generates a calendar of the form:
	    <pre>
		 <month number="3" name="March">
		  <week number="1">
		   <day/>
		   <day/>
		   <day/>
		   <day/>
		   <day/>
		   <day number="1" string="01 March 2000"/>
		   <day number="2" string="02 March 2000"/>
		  </week>
		  <week number="2">
		   <day number="3"/>
		   <day number="4"/>
		   <day number="5"/>
		   <day number="6"/>
		   <day number="7"/>
		   <day number="8"/>
		   <day number="9"/>
		  </week>
		  ...
		 </month>
		</pre>
	**/
	public static Element generateMonth(Document document, String date_format, Date date) {
		SimpleDateFormat format = null;
		if (date_format != null) {
			format = new SimpleDateFormat(date_format);
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		Element month_element = document.createElement("month");
		month_element.setAttribute("number",month_number_format.format(date));
		month_element.setAttribute("name",month_name_format.format(date));
		month_element.setAttribute("year",""+calendar.get(Calendar.YEAR));
		Element week_element = document.createElement("week");
		int week_number = 1;
		week_element.setAttribute("number","1");
		month_element.appendChild(week_element);
		calendar.set(Calendar.DAY_OF_MONTH,1);
		Calendar temp = (Calendar)calendar.clone();
		while (temp.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			temp.add(Calendar.DATE,-1);
			week_element.appendChild(document.createElement("day"));
		}
		temp.setTime(calendar.getTime());
		while (temp.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
			if (temp.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				week_number++;
				week_element = document.createElement("week");
				week_element.setAttribute("number",""+week_number);
				month_element.appendChild(week_element);
			}
			Element day_element = document.createElement("day");
			day_element.setAttribute("number",""+temp.get(Calendar.DAY_OF_MONTH));
			day_element.setAttribute("string",""+format.format(temp.getTime()));
			week_element.appendChild(day_element);
			temp.add(Calendar.DATE,1);
		}
		while (temp.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			week_element.appendChild(document.createElement("day"));
			temp.add(Calendar.DATE,1);
		}
		return month_element;
	}

	public static Element generateMonth(Document document, String date_format, String date) throws ParseException {
		return generateMonth(document,date_format,month_format.parse(date));
	}

	public static Element generateMonth(Document document, String date_format) {
		return generateMonth(document,date_format,new Date());
	}

}
