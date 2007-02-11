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
package org.apache.cocoon.transformation.constrained;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @version CVS $Id: XmlTreeConstraint.java,v 1.4 2004/03/05 10:07:26 bdelacretaz Exp $
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

