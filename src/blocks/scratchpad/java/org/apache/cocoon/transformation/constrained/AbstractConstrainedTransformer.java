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
package org.apache.cocoon.transformation.constrained;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.AbstractTransformer;
import org.apache.cocoon.xml.XMLConsumer;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 *  This class is an abstract class from which you can extend your Transformer
 *  and write it in a way similar to AWT & Swing Event Handling. Part of this
 *  code is from the SQLTransformer of Donald Ball.
 *
 * @author     <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @author     <a href="mailto:tom.klaasen@pandora.be">Tom Klaasen</a>
 * @version CVS $Id: AbstractConstrainedTransformer.java,v 1.3 2003/11/15 04:21:29 joerg Exp $
 */
public abstract class AbstractConstrainedTransformer
		 extends AbstractTransformer {

	/**  The URI of the namespace this class processes */
	private String my_uri;
	/**
	 *  The 'name' of the namespace this class processes. FIXME: Seems to be the
	 *  last path element of the URI -- is that correct?
	 */
	private String my_name;
	/**  A List of open elements in the namespace of this class. */
	private List myUriOpenElements = new ArrayList();
	/**  A List of all open elements in the XML file that is processed. */
	private List globalOpenElements = new ArrayList();
	/**
	 *  A List of the attributes of the open elements in the XML file that is
	 *  processed.
	 */
	private List globalOpenElementsAttributes = new ArrayList();
	/**  The raw representation of the element that was last opened in the XML file. */
	private String lastOpenElementRaw = "";
	/**  The List with the listeners that are interested in the events of this class. */
	private List registeredListeners = new ArrayList();
	/**
	 *  The constraints of the registered listeners. FIXME: Find out how the
	 *  relation with the previous List is maintained.
	 */
	private List registeredListenerConstraints = new ArrayList();
	/**  FIXME: What's this? */
	private StringBuffer current_value = new StringBuffer();
	/**
	 *  The consumer of the SAX events generated by this
	 *  AbstractConstrainedTransformer.
	 */
	public XMLConsumer xml_consumer;
	/**
	 *  The lexical handler of the SAX events generated by this
	 *  AbstractConstrainedTransformer. FIXME: Isn't this always the same as the
	 *  xml_consumer??
	 */
	public LexicalHandler lexical_handler;


	/**
	 *  Initialize: set up the listeners etc.
	 *
	 *@param  parameters  The parameters that can be used by this method.
	 */
	public abstract void init(Parameters parameters);



	/**
	 *  Retrieve the URI of the namespace this AbstractConstrainedTransformer has
	 *  to process.
	 *
	 *@return    The URI of the namespace this AbstractConstrainedTransformer has
	 *      to process.
	 */
	public abstract String getUri();


	/**
	 *  Retrieve the 'name' of the namespace this class processes. FIXME: Seems to
	 *  be the last path element of the URI -- is that correct?
	 *
	 *@return    The 'name' of the namespace this class processes.
	 */
	public abstract String getName();


	public final void setup(
			SourceResolver resolver, Map objectModel, String source, Parameters parameters)
			 throws ProcessingException, SAXException, IOException {

		my_uri = getUri();
		getLogger().debug(my_uri);
		my_name = getName();
		getLogger().debug(my_name);
		init(parameters);

	}


	public void addEventListener(
			ElementEventListener l, XmlTreeConstraint constraint) {

		registeredListeners.add(l);
		registeredListenerConstraints.add(constraint);
	}


	public void setDocumentLocator(Locator locator) {
		if (super.contentHandler != null) {
			super.contentHandler.setDocumentLocator(locator);
		}
	}


	public void startElement(
			String uri, String name, String raw, Attributes attributes)
			 throws SAXException {
		System.out.println("Start Element " + uri + " " + name + " " + raw + " " + attributes);

		lastOpenElementRaw = raw;

		boolean isMyUri = false;

		if (uri.equals(my_uri)) {
			isMyUri = true;
			System.out.println(raw + " isMyUri!!!");
			if (getLogger().isDebugEnabled()) {
				getLogger().debug(raw + " isMyUri!!!");
			}
			myUriOpenElements.add(name);

			current_value.delete(0, current_value.length());
			globalOpenElements.add(name);
			globalOpenElementsAttributes.add(attributes);

			ListIterator constraintsIter =
					this.registeredListenerConstraints.listIterator();

			while (constraintsIter.hasNext()) {
				if (((XmlTreeConstraint) constraintsIter.next())
						.isAllowed(new Boolean(isMyUri), this.myUriOpenElements,
						this.globalOpenElements)) {
					return;
				}
			}
		} else {
			System.out.println(raw + " is NOT MyUri!!!");
			if (getLogger().isDebugEnabled()) {
				getLogger().debug(raw + " is NOT MyUri!!!");
			}

			super.startElement(uri, name, raw, attributes);
		}
	}


	public void endElement(String uri, String name, String raw)
			 throws SAXException {

		try {

			boolean isMyUri = false;

			if (uri.equals(my_uri)) {
				isMyUri = true;

				ListIterator constraintsIter =
						this.registeredListenerConstraints.listIterator();
				XmlTreeConstraint currentConstraint;
				boolean isAllowed;
				boolean isElementTag = lastOpenElementRaw.equals(raw);

				while (constraintsIter.hasNext()) {
					currentConstraint =
							(XmlTreeConstraint) constraintsIter.next();
					isAllowed =
							currentConstraint.isAllowed(new Boolean(isMyUri),
							this.myUriOpenElements,
							this.globalOpenElements);

					if (isAllowed) {

						EventListener el =
								(EventListener) (registeredListeners.get(constraintsIter.previousIndex()));

						if (isElementTag) {
							//it's an element

							((ElementEventListener) el).elementValueRecieved(
									new ElementValueEvent(
									this, name, current_value.toString(),
									(Attributes) this.globalOpenElementsAttributes
									.get(globalOpenElementsAttributes.size()
									 - 1)));
						} else {
							//it's a container
							((ElementEventListener) el).containerElementEnded(
									new ContainerElementEndEvent(
									this, name));
						}

						if (uri.equals(my_uri)) {
							myUriOpenElements.remove(myUriOpenElements.size()
									 - 1);
						}

						globalOpenElements.remove(globalOpenElements.size() - 1);
						globalOpenElementsAttributes.remove(globalOpenElementsAttributes.size() - 1);
						current_value.delete(0, current_value.length());

						return;
					}
				}

				if (globalOpenElements.size() > 0) {
					globalOpenElements.remove(globalOpenElements.size() - 1);
					globalOpenElementsAttributes.remove(globalOpenElementsAttributes.size() - 1);

				}

				if (uri.equals(my_uri) && (myUriOpenElements.size() > 0)) {
					myUriOpenElements.remove(myUriOpenElements.size() - 1);

				}
			} else {
				super.endElement(uri, name, raw);
			}

			current_value.delete(0, current_value.length());

		} catch (Exception t) {
			throw new SAXException("Exception in transformer "
					 + this.getName() + " with uri "
					 + this.getUri()
					 + " while ending element.", t);
		}
	}


	/**
	 *  Utility methods also for subclasses
	 *
	 *@author     Nicola Ken Barozzi
	 *@since      14 May 2002
	 */
	public class RipperListener extends ElementEventAdapter {

		public void elementValueRecieved(ElementValueEvent e) {
			//do nothing so that element doesn't pass through
		}
	}


	public void characters(char ary[], int start, int length)
			 throws SAXException {

		if (this.globalOpenElements.isEmpty()) {
			super.characters(ary, start, length);
		} else {
			current_value.append(ary, start, length);
		}
	}


	public void start(String name, AttributesImpl attr)
			 throws SAXException {
		super.contentHandler.startElement(getUri(), name, name, attr);
		attr.clear();
	}


	public void start(String name) throws SAXException {
		super.contentHandler.startElement(getUri(), name, name,
				new AttributesImpl());
	}


	public void end(String name) throws SAXException {
		super.contentHandler.endElement(getUri(), name, name);
	}


	public void data(String data) throws SAXException {

		if (data != null) {
			super.contentHandler.characters(data.toCharArray(), 0,
					data.length());
		}
	}


	public static String getStringValue(Object object) {

		if (object instanceof byte[]) {
			return new String((byte[]) object);
		} else if (object instanceof char[]) {
			return new String((char[]) object);
		} else if (object != null) {
			return object.toString();
		} else {
			return "";
		}
	}
}

