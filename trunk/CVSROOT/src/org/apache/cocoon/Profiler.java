/*-- $Id: Profiler.java,v 1.3 2001-02-20 19:52:16 greenrd Exp $ --

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

package org.apache.cocoon;

import org.apache.cocoon.framework.Actor;
import org.apache.cocoon.framework.Director;
import org.apache.cocoon.framework.Status;
import org.apache.cocoon.logger.Logger;
import org.apache.tools.DOMWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;

/**
 * Records how long certain processes takes for each request.
 *
 * startEvent () and finishEvent () should be called at the start and
 * end of each process. Use getProfileTable to get the results as a DOM.
 *
 * @author <a href="mailto:greenrd@hotmail.com">Robin Green</a>
 * @version $Revision: 1.3 $ $Date: 2001-02-20 19:52:16 $
 */
public class Profiler implements Actor, Status {

  public static final DateFormat DATE_FORMAT =
    DateFormat.getDateTimeInstance ();

  protected Logger logger;

  /** This should be a ListMap. */
  protected Hashtable requests = new Hashtable ();
  protected Vector requestOrder = new Vector ();

  /** This should be a ListSet. */
  protected Vector eventNames = new Vector ();

  public void init (Director director) {
    logger = (Logger) director.getActor ("logger");
  }

  public void startEvent (RequestMarker request, Class event) {
    startEvent (request, mangleName (event.getName ()));
  }

  public void startEvent (RequestMarker request, String event) {
    if (!eventNames.contains (event)) eventNames.addElement (event);
    Hashtable rm = getRequestMap (request);
    rm.put (event, new ProfProcess (System.currentTimeMillis (), event));
  }

  public void finishEvent (RequestMarker request, Class event) {
    finishEvent (request, mangleName (event.getName ()));
  }

  public void finishEvent (RequestMarker request, String event) {
    long finTime = System.currentTimeMillis ();
    Hashtable rm = getRequestMap (request);
    ProfProcess pp = (ProfProcess) rm.get (event);
    if (pp == null) {
      logger.log (this, "Event '" + event + "' not started yet!", Logger.WARNING);
    }
    else {
      pp.finishedAt (finTime);
    }
  }

  public String mangleName (String name) {
    return name.replace ('.', '-');
  }

  public void clear () {
    requests.clear ();
    requestOrder.removeAllElements ();
    eventNames.removeAllElements ();
  }

  public Element getProfileTable (Document dest) {
    DOMWriter domWriter = new DOMWriter (dest, "profile-table");
    domWriter.push ("headings");
    domWriter.addQuick ("heading", "URI");
    domWriter.addQuick ("heading", "Date and Time");
    for (Enumeration enum = eventNames.elements (); enum.hasMoreElements (); ) {
      domWriter.addQuick ("heading", (String) enum.nextElement ());
    }
    domWriter.pop ("headings");
    for (Enumeration rq = requestOrder.elements (); rq.hasMoreElements (); ) {
      domWriter.push ("row");
      RequestMarker request = (RequestMarker) rq.nextElement ();
      Hashtable map = (Hashtable) requests.get (request);
      domWriter.addQuick ("uri", request.uri);
      domWriter.addQuick ("date-time", DATE_FORMAT.format (request.startTime));
      for (Enumeration proc = eventNames.elements (); proc.hasMoreElements (); ) {
        String event = (String) proc.nextElement ();
        ProfProcess pp = (ProfProcess) map.get (event);
        if (pp != null && pp.finished) {
          domWriter.addQuick (event, pp.getDuration () + "ms");
        }
        else {
          domWriter.addQuick (event, "-");
        }
      }
      domWriter.pop ("row");
    }
    return (Element) domWriter.getCurrent ();
  }

  protected Hashtable getRequestMap (RequestMarker request) {
    Hashtable map = (Hashtable) requests.get (request);
    if (map == null) {
      requests.put (request, map = new Hashtable ());
      requestOrder.addElement (request);
    }
    return map;
  }

  /**
   * We cannot guarantee that the request object will not be recycled by the
   * servlet runner, so use RequestMarker instead.
   */
  public static class RequestMarker {

    protected String uri;
    protected Date startTime;

    public RequestMarker (HttpServletRequest req) {
      // Not safe to store the req object as it might be recycled
      uri = req.getRequestURI ();
      startTime = new Date (System.currentTimeMillis ());
    }
  }

  protected class ProfProcess {
    protected final String event;
    protected boolean finished = false;
    protected long time;

    protected ProfProcess (long startTime, String event) {
      this.event = event;
      time = startTime;
    }

    protected void finishedAt (long finishTime) {
      time = finishTime - time;
      finished = true;
    }

    protected long getDuration () {
      if (!finished) {
        logger.log (Profiler.this, "Event '" + event + "' not finished yet!",
                    Logger.WARNING);
        return -1;
      }
      return time;
    }

  }

  public String getStatus () {
    return "Cocoon Performance Profiler";
  }
}
