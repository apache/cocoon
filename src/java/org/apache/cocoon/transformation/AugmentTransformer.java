package org.apache.cocoon.transformation;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.Map;

/**
 * <p>Augments all <code>href</code> attributes with the full path to
 * the request.</p>
 *
 * <p>Usage in sitemap:</p>
 *
 * <pre>
 *    &lt;map:transform type="augment"&gt
 *      &lt;map:parameter name="mount" value="directory/to/be/appended"/&gt;
 *    &lt;/map:transform&gt;
 * </pre>
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @since October 10, 2001
 * @version CVS $Id: AugmentTransformer.java,v 1.2 2003/03/16 17:49:15 vgritsenko Exp $
 */
public class AugmentTransformer
  extends AbstractTransformer
  implements Poolable
{
  Map objectModel;
  Request request;
  String baseURI;
  
  public void setup(SourceResolver resolver,
                    Map objectModel,
                    String source,
                    Parameters parameters)
    throws ProcessingException, SAXException, IOException
  {
    this.objectModel = objectModel;
    request = (Request)objectModel.get("request");

    String mountPoint = (String)parameters.getParameter("mount", null);
    
    StringBuffer uribuf = new StringBuffer();
    boolean isSecure = request.isSecure();
    int port = request.getServerPort();

    if (isSecure)
      uribuf.append("https://");
    else
      uribuf.append("http://");

    uribuf.append(request.getServerName());

    if (isSecure) {
      if (port != 443) {
        uribuf.append(":").append(port);
      }
    }
    else {
      if (port != 80) {
        uribuf.append(":").append(port);
      }
    }
    if (mountPoint == null) {
      String requestedURI = request.getRequestURI();
      requestedURI = requestedURI.substring(0, requestedURI.lastIndexOf("/"));
      uribuf.append(requestedURI);
      uribuf.append("/");
    }
    else {
      uribuf.append(request.getContextPath());
      uribuf.append("/");
      uribuf.append(mountPoint);
    }
    baseURI = uribuf.toString();
  }

  public void startElement(String uri,
                           String name,
                           String qname,
                           Attributes attrs)
    throws SAXException
  {
    AttributesImpl newAttrs = null;
    
    for (int i = 0, size = attrs.getLength(); i < size; i++) {
      String attrName = attrs.getLocalName(i);
      if (attrName.equals("href")) {
        String value = attrs.getValue(i);

        // Don't touch the attribute if it's an absolute URL
        if (value.startsWith("http:") || value.startsWith("https:"))
          continue;

        if (newAttrs == null)
          newAttrs = new AttributesImpl(attrs);

        String newValue = baseURI + value;
        newAttrs.setValue(i, newValue);
      }
    }

    if (newAttrs == null)
      super.startElement(uri, name, qname, attrs);
    else
      super.startElement(uri, name, qname, newAttrs);
  }

  public void recycle()
  {
    this.objectModel = null;
  }
}
