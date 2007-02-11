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
package org.apache.cocoon.components.validation;

/**
 * Encapsulates an error condition which was triggered
 * by a violation of the document validity during
 * validation
 *
 * @author  ivelin@apache.org
 * @version CVS $Id: Violation.java,v 1.2 2003/04/26 12:10:43 stephan Exp $
 */
public class Violation implements Comparable {

    /**
     * @return the XPath location of the Violation
     */
    public String getPath() {
        return xpath_;
    }

    /**
     * set the XPath location of the Violation
     *
     * @param xpath      
     */
    public void setPath(String xpath) {
        xpath_ = xpath;
    }

    /**
     * @return the error message
     */
    public String getMessage() {
        return message_;
    }

    /**
     * set the error message
     *
     * @param message    
     */
    public void setMessage(String message) {
        message_ = message;
    }

    public boolean equals(Object obj) {
        if (obj==null) {
            return false;
        }
        if (obj==this) {
            return true;
        }
        if ( !(obj instanceof Violation)) {
            throw new java.lang.IllegalArgumentException("Can only compare to a Violation object");
        }
        Violation v = (Violation) obj;

        if (getPath().equals(v.getPath()) &&
            getMessage().equals(v.getMessage())) {
            return true;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return (getPath().hashCode()^getMessage().hashCode());
    }

    public int compareTo(Object obj) {
        if (obj==null) {
            return 1;
        }
        if (obj==this) {
            return 0;
        }
        if ( !(obj instanceof Violation)) {
            throw new java.lang.IllegalArgumentException("Can only compare to a Violation object");
        }
        Violation v = (Violation) obj;
        int primaryResult = getPath().compareTo(v.getPath());

        if (primaryResult!=0) {
            return primaryResult;
        } else {
            if (getMessage()==null) {
                if (v.getMessage()==null) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                return (getMessage().compareTo(v.getMessage()));
            }
        }
    }

    private String xpath_;
    private String message_;

}
