/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package rmitest; 

// import the necessary classes from the java.io package 
import java.io.IOException;
import java.io.StringReader; 

// import the necessary classes from the java.rmi 
import java.rmi.Naming; 
import java.rmi.RemoteException; 
import java.rmi.NotBoundException; 

// import the necessary SAX classes 
import org.xml.sax.InputSource; 
import org.xml.sax.SAXException; 

// import of the classes used from Cocoon 2 
import org.apache.cocoon.ProcessingException; 
import org.apache.cocoon.generation.ComposerGenerator; 

// import of the classes from the 
// Avalon Framework 
import org.apache.avalon.framework.parameters.Parameters; 
import org.apache.avalon.framework.parameters.ParameterException; 
import org.apache.avalon.framework.component.ComponentException; 

// needed for obtaining parser in Cocoon 
import org.apache.excalibur.xml.sax.SAXParser;

/**
 * <p>
 * The <code>RMIGenerator</code> is a generator that reads a String via RMI
 * and generates SAX events. 
 * The RMIGenerator extends the <code>ComposerGenerator</code> class. This is
 * done so we can access the <code>ComponentManager</code> to obtain a
 * <code>SAXParser</code>.
 * </p>
 *
 * <p>
 * The methods invokes to obtain the String via RMI are defined in the 
 * <code>ServerFunctions</code> interface in resides in the same package
 * as this generator. A RMI server application that wants to be able to
 * &quot;feed&quot; XML data to this generator must implement the
 * <code>ServerFunctions</code> interface.
 * </p>
 *
 * <p>
 * <b>Usage:</b>
 * </p>
 *
 * <p>
 * Suppose you declare this generator in your sitemap and name it
 * <code>rmigenerator</code>. A typical example of use is the following:
 * </p>
 *
 * <p>
 * <pre>
 * <code>
 *   &lt;map:match pattern=&quot;rmi/**.xml&quot;&gt;
 *     &lt;map:generate type=&quot;rmigenerator&quot; src=&quot;{1}.xml&quot;&gt;
 *       &lt;!-- host parameter where RMI server is running, REQUIRED --&gt;
 *       &lt;map:parameter name=&quot;host&quot; value=&quot;myhost.com&quot;/&gt;
 *       &lt;!-- bindname parameter, name to which RMI server is bound in remote rmiregistry, REQUIRED --&gt;
 *       &lt;map:paramater name=&quot;bindname&quot; value=&quot;RMIServer&quot;/&gt;
 *       &lt;!-- port parameter, at which port the rmiregistry is running --&gt;
 *       &lt;!-- at the remote host, OPTIONAL --&gt;
 *       &lt;!-- 1099 is the default, this is in fact not really needed --&gt;
 *       &lt;map:parameter name=&quot;port&quot; value=&quot;1099&quot;/&gt;
 *     &lt;map:generate/&gt;
 *     &lt;map:transform src=&quot;somestylesheet.xsl&quot;/&gt;
 *     &lt;map:serialize/&gt;
 *   &lt;/map:match&gt;
 * </code>
 * </pre>
 * </p>
 *
 *
 * @author <a href="mailto:Erwin.Hermans@cs.kuleuven.ac.be">Erwin Hermans</a>
 *         (Student Computer Science Department KULeuven, 2001-2002)
 * @version CVS $Id: RMIGenerator.java,v 1.3 2004/03/06 02:26:14 antonio Exp $
 */
public class RMIGenerator extends ComposerGenerator { 

	/**
	 * Generate SAX events based on the parameters and the source specified
	 * in the sitemap. If the <code>src</code> attribute is specified, the
	 * <code>getResource(String)</code> method is invoked, otherwise the
	 * <code>sayHello()</code> is invoked on the remote object.
	 *
	 */
	public void generate () throws IOException, SAXException, ProcessingException { 
		String host; 
		
		// lookup parameter 'host' 
		try { 
			host = parameters.getParameter("host"); 
			// test if host is not the empty string 
			if (host == "") { 
				throw new ParameterException("The parameter 'host' may not be the empty string"); 
			} 
		} catch (ParameterException pe) { 
			// rethrow as a ProcessingException 
			throw new ProcessingException("Parameter 'host' not specified",pe); 
		}
		
		String bindname;
		
		// lookup parameter 'bindname' 
		try { 
			bindname = parameters.getParameter("bindname"); 
			// test if bindname is not the empty string 
			if (bindname == "") { 
				throw new ParameterException("The parameter 'bindname' may not be the empty string"); 
			}
		} catch (ParameterException pe) { 
			// rethrow as a ProcessingException 
			throw new ProcessingException("Parameter 'bindname' not specified",pe); 
		} 
		
		String port = ""; 
		// lookup parameter 'port' 
		try { 
			port = parameters.getParameter("port"); 
			port = ":" + port; 
		} catch (ParameterException pe) { 
			// reset port to the empty string 
			// port is not required 
			port = ""; 
		}
		
		try {
			ServerFunctions obj = (ServerFunctions)Naming.lookup("//" + host + port + "/" + bindname); 
			String message = "";

			// determine the method to invoke 
			// depending on value of source 
			if (this.source == null) { 
				message = obj.sayHello(); 
			} else { 
				message = obj.getResource(this.source);
			} 
			
			SAXParser parser = null; 
			parser = (SAXParser)this.manager.lookup(SAXParser.ROLE); 
			InputSource inputSource = new InputSource(new StringReader(message)); 
			parser.parse(inputSource,super.xmlConsumer); 
		} catch (NotBoundException nbe) { 
			throw new ProcessingException("Error looking up the RMI application server",nbe); 
		} catch (ComponentException ce) { 
			throw new ProcessingException("Error obtaining a SAXParser",ce); 
		} 
	} 
}
		
