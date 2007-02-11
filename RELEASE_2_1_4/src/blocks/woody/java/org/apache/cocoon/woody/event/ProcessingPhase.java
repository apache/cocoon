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
package org.apache.cocoon.woody.event;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enum.ValuedEnum;

/**
 * Type-safe enumeration of the various form processing phases.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: ProcessingPhase.java,v 1.2 2004/02/11 09:27:55 antonio Exp $
 */
public class ProcessingPhase extends ValuedEnum {

    protected ProcessingPhase(String name, int value) {
        super(name, value);
    }
    
    public static final int LOAD_MODEL_VALUE = 0;
    public static final ProcessingPhase LOAD_MODEL = new ProcessingPhase("Load model", LOAD_MODEL_VALUE);
    
    public static final int READ_FROM_REQUEST_VALUE = 1;
    public static final ProcessingPhase READ_FROM_REQUEST = new ProcessingPhase("Read from request", READ_FROM_REQUEST_VALUE);
    
    public static final int VALIDATE_VALUE = 2;
    public static final ProcessingPhase VALIDATE = new ProcessingPhase("Validate", VALIDATE_VALUE);
    
    public static final int SAVE_MODEL_VALUE = 3;
    public static final ProcessingPhase SAVE_MODEL = new ProcessingPhase("Save model", SAVE_MODEL_VALUE);
     
    public static ProcessingPhase getEnum(String name) {
      return (ProcessingPhase) getEnum(ProcessingPhase.class, name);
    }
    
    public static ProcessingPhase getEnum(int value) {
      return (ProcessingPhase) getEnum(ProcessingPhase.class, value);
    }

    public static Map getEnumMap() {
      return getEnumMap(ProcessingPhase.class);
    }
 
    public static List getEnumList() {
      return getEnumList(ProcessingPhase.class);
    }
 
    public static Iterator iterator() {
      return iterator(ProcessingPhase.class);
    }
}
