/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cocoon.reading;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import javax.xml.soap.SOAPException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
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

import org.w3c.dom.Element;
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
 * @version CVS $Id: AxisRPCReader.java,v 1.6 2004/03/05 13:01:42 bdelacretaz Exp $
 */
public class AxisRPCReader extends ServiceableReader
    implements Configurable, Disposable
{

    // soap server reference
    private SoapServer m_server;
    
    /** Are we in development stage ? */
    private boolean m_isDevelompent = false;

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
	 */
	public void configure(Configuration config) throws ConfigurationException
	{
        m_isDevelompent = config.getChild("development-stage").getValueAsBoolean(m_isDevelompent );
	}

    public void service(final ServiceManager manager) throws ServiceException {
        super.service(manager);
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
            String url = HttpUtils.getRequestURL(req).toString();
            msgContext.setProperty(MessageContext.TRANS_URL, url);
            
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
                if (responseMsg == null) {
                    //tell everyone that something is wrong
                    throw new Exception("no response message");
                }
            } 
            catch (AxisFault fault)
            {
                if (getLogger().isErrorEnabled())
                {
                    getLogger().error("Axis Fault", fault);
                }

                // log and sanitize
                processAxisFault(fault);
                configureResponseFromAxisFault(res, fault);
                responseMsg = msgContext.getResponseMessage();
                if (responseMsg == null) {
                    responseMsg = new Message(fault);
                }
            }
            catch (Exception e)
            {
                //other exceptions are internal trouble
                responseMsg = msgContext.getResponseMessage();
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                if (getLogger().isErrorEnabled())
                {
                    getLogger().error("Error during SOAP call", e);
                }
                if (responseMsg == null) {
                    AxisFault fault = AxisFault.makeFault(e);
                    processAxisFault(fault);
                    responseMsg = new Message(fault);
                }
            }
        }
        catch (AxisFault fault)
        {
            if (getLogger().isErrorEnabled())
            {
                getLogger().error("Axis fault occured while perforing request", fault);
            }
            processAxisFault(fault);
            configureResponseFromAxisFault(res, fault);
            responseMsg = msgContext.getResponseMessage();
            if( responseMsg == null) {
                responseMsg = new Message(fault);
            }
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
     * routine called whenever an axis fault is caught; where they
     * are logged and any other business. The method may modify the fault
     * in the process
     * @param fault what went wrong.
     */
    protected void processAxisFault(AxisFault fault) {
        //log the fault
        Element runtimeException = fault.lookupFaultDetail(
                Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
        if (runtimeException != null) {
            getLogger().info("AxisFault:", fault);
            //strip runtime details
            fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
        } else if (getLogger().isDebugEnabled()) {
            getLogger().debug("AxisFault:", fault);
        }
        //dev systems only give fault dumps
        if (m_isDevelompent) {
            //strip out the stack trace
            fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_STACKTRACE);
        }
    }

    /**
     * Configure the servlet response status code and maybe other headers
     * from the fault info.
     * @param response response to configure
     * @param fault what went wrong
     */
    private void configureResponseFromAxisFault(HttpServletResponse response,
                                                AxisFault fault) {
        // then get the status code
        // It's been suggested that a lack of SOAPAction
        // should produce some other error code (in the 400s)...
        int status = getHttpServletResponseStatus(fault);
        if (status == HttpServletResponse.SC_UNAUTHORIZED) {
            // unauth access results in authentication request
            // TODO: less generic realm choice?
          response.setHeader("WWW-Authenticate","Basic realm=\"AXIS\"");
        }
        response.setStatus(status);
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
        String soapAction = req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);

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
