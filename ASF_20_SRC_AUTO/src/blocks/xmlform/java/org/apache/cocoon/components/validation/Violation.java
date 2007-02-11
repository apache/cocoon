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
package org.apache.cocoon.components.validation;

/**
 * Encapsulates an error condition which was triggered
 * by a violation of the document validity during
 * validation
 *
 * @author  ivelin@apache.org
 * @version CVS $Id: Violation.java,v 1.4 2004/03/05 13:02:37 bdelacretaz Exp $
 */
public class Violation implements Comparable {

    private String xpath_;
    private String message_;

    public Violation() {
    }

    public Violation(String xpath, String message) {
        setPath(xpath);
        setMessage(message);
    }

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
}
