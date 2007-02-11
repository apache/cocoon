/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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

package org.apache.cocoon.reading;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.transport.http.AxisHttpSession;
import org.apache.axis.transport.http.HTTPConstants;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.axis.SoapServer;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.http.HttpEnvironment;

import org.xml.sax.SAXException;

/**
 * SOAP Reader
 *
 * <p>
 *  This reader accepts a SOAP Request, and generates the resultant
 *  response as output. Essentially, this reader allows you to serve SOAP
 *  requests from your Cocoon application.
 * </p>
 *
 * <p>
 *  Code originates from the Apache
 *  <a href="http://xml.apache.org/axis">AXIS</a> project,
 *  <code>org.apache.axis.http.transport.AxisServlet</code>.
 * </p>
 *
 * Ported to Cocoon by:
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 *
 * Original <code>AxisServlet</code> authors:
 *
 * @author <a href="mailto:">Steve Loughran</a>
 * @author <a href="mailto:dug@us.ibm.com">Doug Davis</a>
 *
 * @version CVS $Id: AxisRPCReader.java,v 1.2 2003/03/11 17:44:17 vgritsenko Exp $
 */
public class AxisRPCReader extends ComposerReader
    implements Disposable
{
    // soap server reference
    private SoapServer m_server;

    /**
     * Compose this reader
     *
     * @param manager a <code>ComponentManager</code> instance
     * @exception ComponentException if an error occurs
     */
    public void compose(final ComponentManager manager)
        throws ComponentException
    {
        super.compose(manager);

        // set soap server reference
        m_server = (SoapServer) manager.lookup(SoapServer.ROLE);
    }

    /**
     * Axis RPC Router <code>setup</code> method.
     *
     * <p>
     *  This method sets the reader up for use. Essentially it checks that
     *  its been invoked in a HTTP-POST environment, reads some optional
     *  configuration variables, and obtains several component references to
     *  be used later.
     * </p>
     *
     * @param resolver <code>SourceResolver</code> instance
     * @param objectModel request/response/context data
     * @param src source <code>String</code> instance
     * @param parameters sitemap invocation time customization parameters
     * @exception ProcessingException if an error occurs
     * @exception IOException if an error occurs
     * @exception SAXException if an error occurs
     */
    public void setup(
        final SourceResolver resolver, 
        final Map objectModel,
        final String src,
        final Parameters parameters
    )
        throws ProcessingException, IOException, SAXException
    {
        super.setup(resolver, objectModel, src, parameters); 

        checkHTTPPost(objectModel);

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("AxisRPCReader.setup() complete");
        }
    }

    /**
     * Helper method to ensure that given a HTTP-POST.
     *
     * @param objectModel Request/Response/Context map.
     * @exception ProcessingException if a non HTTP-POST request has been made.
     */
    private void checkHTTPPost(final Map objectModel)
        throws ProcessingException
    {
        String method = ObjectModelHelper.getRequest(objectModel).getMethod();

        if (!"POST".equalsIgnoreCase(method))
            throw new ProcessingException(
                "Reader only supports HTTP-POST (supplied was " + method + ")"
            );
    }

    /**
     * Axis RPC Router <code>generate</code> method.
     *
     * <p>
     *  This method reads the SOAP request in from the input stream, invokes
     *  the requested method and sends the result back to the requestor
     * </p>
     *
     * @exception IOException if an IO error occurs
     * @exception SAXException if a SAX error occurs
     * @exception ProcessingException if a processing error occurs
     */
    public void generate()
        throws IOException, SAXException, ProcessingException
    {
        HttpServletRequest req =
            (HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
        HttpServletResponse res =
            (HttpServletResponse) objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
        ServletContext con =
            (ServletContext) objectModel.get(HttpEnvironment.HTTP_SERVLET_CONTEXT);

        String soapAction = null;
        MessageContext msgContext = null;
        Message responseMsg = null;

        try
        {
            res.setBufferSize(1024 * 8); // provide performance boost.

            // Get message context w/ various properties set
            msgContext = m_server.createMessageContext(req, res, con);

            // Get request message
            Message requestMsg =
                new Message(
                    req.getInputStream(), false,
                    req.getHeader(HTTPConstants.HEADER_CONTENT_TYPE),
                    req.getHeader(HTTPConstants.HEADER_CONTENT_LOCATION)
                );

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Request message:\n" + messageToString(requestMsg));
            }

            // Set the request(incoming) message field in the context
            msgContext.setRequestMessage(requestMsg);

            try
            {
                //
                // Save the SOAPAction header in the MessageContext bag.
                // This will be used to tell the Axis Engine which service
                // is being invoked.  This will save us the trouble of
                // having to parse the Request message - although we will
                // need to double-check later on that the SOAPAction header
                // does in fact match the URI in the body.
                // (is this last stmt true??? (I don't think so - Glen))
                //
                soapAction = getSoapAction(req);

                if (soapAction != null)
                {
                    msgContext.setUseSOAPAction(true);
                    msgContext.setSOAPActionURI(soapAction);
                }

                // Create a Session wrapper for the HTTP session.
                msgContext.setSession(new AxisHttpSession(req));

                // Invoke the Axis engine...
                if(getLogger().isDebugEnabled())
                {
                    getLogger().debug("Invoking Axis Engine");
                }

                m_server.invoke(msgContext);

                if(getLogger().isDebugEnabled())
                {
                    getLogger().debug("Return from Axis Engine");
                }

                responseMsg = msgContext.getResponseMessage();
            } 
            catch (AxisFault e)
            {
                if (getLogger().isErrorEnabled())
                {
                    getLogger().error("Axis Fault", e);
                }

                // It's been suggested that a lack of SOAPAction
                // should produce some other error code (in the 400s)...
                int status = getHttpServletResponseStatus(e);
                if (status == HttpServletResponse.SC_UNAUTHORIZED)
                {
                    res.setHeader("WWW-Authenticate","Basic realm=\"AXIS\"");
                }

                res.setStatus(status);
                responseMsg = new Message(e);
            }
            catch (Exception e)
            {
                if (getLogger().isErrorEnabled())
                {
                    getLogger().error("Error during SOAP call", e);
                }

                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseMsg = new Message(AxisFault.makeFault(e));
            }
        }
        catch (AxisFault fault)
        {
            if (getLogger().isErrorEnabled())
            {
                getLogger().error("Axis fault occured while perforing request", fault);
            }

            responseMsg = new Message(fault);
        }
        catch (Exception e)
        {
            throw new ProcessingException("Exception thrown while performing request", e);
        }

        // Send response back
        if (responseMsg != null)
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Sending response:\n" + messageToString(responseMsg));
            }

            sendResponse(getProtocolVersion(req), msgContext.getSOAPConstants(), res, responseMsg);
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("AxisRPCReader.generate() complete");
        }
    }

    /**
     * Extract information from AxisFault and map it to a HTTP Status code.
     *
     * @param af Axis Fault
     * @return HTTP Status code.
     */
    protected int getHttpServletResponseStatus(AxisFault af)
    {
        // This will raise a 401 for both "Unauthenticated" & "Unauthorized"...
        return af.getFaultCode().getLocalPart().startsWith("Server.Unauth")
                   ? HttpServletResponse.SC_UNAUTHORIZED
                   : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    /**
     * write a message to the response, set appropriate headers for content
     * type..etc.
     * @param clientVersion client protocol, one of the HTTPConstants strings
     * @param res   response
     * @param responseMsg message to write
     * @throws AxisFault
     * @throws IOException if the response stream can not be written to
     */
    private void sendResponse(
        final String clientVersion,
        final SOAPConstants constants,
        final HttpServletResponse res,
        final Message responseMsg
    )
        throws AxisFault, IOException
    {
        if (responseMsg == null)
        {
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("No axis response, not sending one");
            }
        }
        else
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Returned Content-Type:" + responseMsg.getContentType(constants));
                getLogger().debug("Returned Content-Length:" + responseMsg.getContentLength());
            }

            try
            {
                res.setContentType(responseMsg.getContentType(constants));
                responseMsg.writeTo(res.getOutputStream());
            }
            catch (SOAPException e)
            {
                getLogger().error("Exception sending response", e);
            }
        }

        if (!res.isCommitted())
        {
            res.flushBuffer(); // Force it right now.
        }
    }

    /**
     * Extract the SOAPAction header.
     * if SOAPAction is null then we'll we be forced to scan the body for it.
     * if SOAPAction is "" then use the URL
     * @param req incoming request
     * @return the action
     * @throws AxisFault
     */
    private String getSoapAction(HttpServletRequest req)
        throws AxisFault
    {
        String soapAction = (String)req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("HEADER_SOAP_ACTION:" + soapAction);
        }

        //
        // Technically, if we don't find this header, we should probably fault.
        // It's required in the SOAP HTTP binding.
        //
        if (soapAction == null)
        {
            throw new AxisFault(
                      "Client.NoSOAPAction",
                      "No SOAPAction header",
                      null, null
                  );
        }

        if (soapAction.length() == 0)
            soapAction = req.getContextPath(); // Is this right?

        return soapAction;
    }

    /**
     * Return the HTTP protocol level 1.1 or 1.0
     * by derived class.
     */
    private String getProtocolVersion(HttpServletRequest req){
        String ret = HTTPConstants.HEADER_PROTOCOL_V10;
        String prot = req.getProtocol();
        if (prot!= null) {
            int sindex= prot.indexOf('/');
            if (-1 != sindex) {
                String ver= prot.substring(sindex+1);
                if (HTTPConstants.HEADER_PROTOCOL_V11.equals(ver.trim())) {
                    ret = HTTPConstants.HEADER_PROTOCOL_V11;
                }
            }
        }
        return ret;
    }

    /**
     * Helper method to convert a <code>Message</code> structure
     * into a <code>String</code>.
     *
     * @param msg a <code>Message</code> value
     * @return a <code>String</code> value
     */
    private String messageToString(final Message msg)
    {
        try
        {
            OutputStream os = new ByteArrayOutputStream();
            msg.writeTo(os);
            return os.toString();
        }
        catch (Exception e)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn(
                    "Warning, could not convert message (" + msg + ") into string", e
                );
            }

            return null;
        }
    }

    /**
     * Dispose this reader. Release all held resources.
     */
    public void dispose()
    {
        manager.release(m_server);
    }
}
