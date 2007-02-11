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

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @version CVS $Id: XmlTreeConstraint.java,v 1.3 2003/12/12 05:39:38 antonio Exp $
 */
public class XmlTreeConstraint {

	private boolean debug = true;

	private Boolean isMyUriRequired;
	private List allowedMyUriStackEnd = null;
	private List allowedGlobalStackEnd = null;


	public XmlTreeConstraint(List allowedMyUriStackEnd,
			List allowedGlobalStackEnd,
			Boolean isMyUriRequired) {

		this.allowedMyUriStackEnd = allowedMyUriStackEnd;
		this.allowedGlobalStackEnd = allowedGlobalStackEnd;
		this.isMyUriRequired = isMyUriRequired;
	}


	public XmlTreeConstraint(List allowedMyUriStackEnd,
			Boolean isMyUriRequired) {
		this.allowedMyUriStackEnd = allowedMyUriStackEnd;
		this.isMyUriRequired = isMyUriRequired;
	}


	public XmlTreeConstraint(List allowedMyUriStackEnd) {
		this.allowedMyUriStackEnd = allowedMyUriStackEnd;
	}


	public XmlTreeConstraint(String[] allowedMyUriStackEnd,
			String[] allowedGlobalStackEnd,
			boolean isMyUriRequired) {

		this.allowedMyUriStackEnd = Arrays.asList(allowedMyUriStackEnd);
		this.allowedGlobalStackEnd = Arrays.asList(allowedGlobalStackEnd);
		this.isMyUriRequired = new Boolean(isMyUriRequired);
	}


	public XmlTreeConstraint(String[] allowedMyUriStackEnd,
			boolean isMyUriRequired) {
		this.allowedMyUriStackEnd = Arrays.asList(allowedMyUriStackEnd);
		this.isMyUriRequired = new Boolean(isMyUriRequired);
	}


	public XmlTreeConstraint(String[] allowedMyUriStackEnd) {
		this.allowedMyUriStackEnd = Arrays.asList(allowedMyUriStackEnd);
	}


	/**
	 *  Check whether the current element is allowed on this position in the XML
	 *  tree / SAX stream.
	 *
	 *@param  isMyUri      Is the URI of the element the same as the URI of the
	 *      calling object? <code>true</code> if it is, else <code>false</code>
	 *@param  myUriStack   The List with elements in the namespace of the calling
	 *      object.
	 *@param  globalStack  The List with all elements (thus far) of the XML file
	 *      being processed.
	 *@return              <code>true</code> if the element is allowed on this
	 *      position, <code>false</code> otherwise.
	 */
	public boolean isAllowed(Boolean isMyUri, List myUriStack,
			List globalStack) {
		if (debug) {
			System.out.println("isMyUri :" + isMyUri + "\nrequired uri " + isMyUriRequired);
			System.out.println("myUristack: \n" + myUriStack);
			System.out.println("allowedMyUriStackEnd: \n" + allowedMyUriStackEnd);
			System.out.println("globalstack: \n" + globalStack);
			System.out.println("allowedGlobalStackEnd: \n" + allowedGlobalStackEnd);
		}
		if (isMyUriRequired != null) {
			if (isMyUri == null) {
				return false;
			}

			/*
			 *  new code
			 */
			if (isMyUriRequired.booleanValue()) {
				if (!isMyUri.booleanValue()) {
					return false;
				}
			}
			/*
			 *  old code -- seems wrong to me (tomK)
			 *  if (!isMyUriRequired.equals(isMyUri)) {
			 *  return false;
			 *  }
			 */
		}

		if (allowedMyUriStackEnd != null) {
			if (myUriStack == null) {
				return false;
			}

			if (!areEndsEqual(allowedMyUriStackEnd, myUriStack)) {
				return false;
			}
		}

		if (allowedGlobalStackEnd != null) {
			if (globalStack == null) {
				return false;
			}

			if (!areEndsEqual(allowedGlobalStackEnd, globalStack)) {
				return false;
			}
		}

		return true;
	}


	private boolean areEndsEqual(List l1, List l2) {

		if (l1.size() > l2.size()) {
			if ((!(l1.size() == l2.size()))
					 && ((l1.size() < 1) || (l2.size() < 1))) {
				return false;
			}

			//make l1 the smaller one
			List l3 = l1;

			l1 = l2;
			l2 = l3;
		}

		ListIterator iter1 = l1.listIterator(l1.size());
		ListIterator iter2 = l2.listIterator(l2.size());

		while (iter1.hasPrevious()) {
			if (!((iter1.previous().toString())
					.equals(iter2.previous().toString()))) {

				//System.out.println("ends are NOT equal.");
				return false;
			}
		}

		//System.out.println("ends are equal.");
		return true;
	}
}

