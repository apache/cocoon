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
package org.apache.cocoon.bean;

import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.ProcessingException;

/**
 * A Target is a single page for generation. It encapsulates the URI 
 * arithmetic required to transform the URI of the page to be generated 
 * (the source URI) into the URI to which the resulting page should be 
 * written (the destination URI).
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: Target.java,v 1.1 2003/08/27 19:18:18 upayavira Exp $
 */
public class Target {
	// Defult type is append
	private static final String APPEND_TYPE = "append";
	private static final String REPLACE_TYPE = "replace";
	private static final String INSERT_TYPE = "insert";

	private final String type;
	private final String root;
	private final String sourceURI;
	private final String destURI;

	private transient int _hashCode;
	private transient String _toString;

	public Target(
		String type,
		String root,
		String sourceURI,
		String destURI)
		throws IllegalArgumentException {
		this.type = type;
		this.root = root;
		this.sourceURI = NetUtils.normalize(sourceURI);
		if (destURI == null || destURI.length() == 0) {
			throw new IllegalArgumentException("You must specify a destination directory when defining a target");
		}
		if (!destURI.endsWith("/")) {
			destURI += "/";
		}
		this.destURI = destURI;
	}

	public Target(String type, String sourceURI, String destURI)
		throws IllegalArgumentException {
		this(type, "", sourceURI, destURI);
	}

	public Target(String sourceURI, String destURI)
		throws IllegalArgumentException {
		this(APPEND_TYPE, "", sourceURI, destURI);
	}

	public Target getDerivedTarget(String newURI)
		throws IllegalArgumentException {
		if (!newURI.startsWith(root)) {
			return null;
		}
		newURI = newURI.substring(root.length());
		return new Target(this.type, this.root, newURI, this.destURI);
	}

	public String getFinalURI(String actualSourceURI)
		throws ProcessingException {
		if (!actualSourceURI.startsWith(root)) {
			throw new ProcessingException(
				"Derived target does not share same root: "
					+ actualSourceURI);
		}
		actualSourceURI = actualSourceURI.substring(root.length());

		if (APPEND_TYPE.equals(this.type)) {
			return destURI + actualSourceURI;
		} else if (REPLACE_TYPE.equals(this.type)) {
			return destURI;
		} else if (INSERT_TYPE.equals(this.type)) {
			int starPos = destURI.indexOf("*");
			if (starPos == -1) {
				throw new ProcessingException("Missing * in replace mapper uri");
			} else if (starPos == destURI.length() - 1) {
				return destURI.substring(0, starPos) + actualSourceURI;
			} else {
				return destURI.substring(0, starPos)
					+ actualSourceURI
					+ destURI.substring(starPos + 1);
			}
		} else {
			throw new ProcessingException(
				"Unknown mapper type: " + this.type);
		}
	}

	public String getSourceURI() {
		return root + sourceURI;
	}

	public boolean equals(Object o) {
		return (o instanceof Target) && o.toString().equals(toString());
	}

	public int hashCode() {
		if (_hashCode == 0) {
			return _hashCode = toString().hashCode();
		}
		return _hashCode;
	}

	public String toString() {
		if (_toString == null) {
			return _toString =
				"<"
					+ type
					+ "|"
					+ root
					+ "|"
					+ sourceURI
					+ "|"
					+ destURI
					+ ">";
		}
		return _toString;
	}
}