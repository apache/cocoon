/*>$File$ -- $Id: JavaModule.java,v 1.2 1999-11-09 02:22:24 dirkx Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
package org.apache.cocoon.interpreter.java;

import java.util.*;
import org.w3c.dom.*;
import java.lang.reflect.*;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.interpreter.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.2 $ $Date: 1999-11-09 02:22:24 $
 */

public class JavaModule implements Module {
  private static Class[][] expectedTypes = {
    {},
    {Dictionary.class},
    {Dictionary.class, Node.class},
    {Node.class, Dictionary.class, org.openxml.x3p.ProcessContext.class},
    {Node.class},
  };

  private String className;
  private Class loadedClass;
  private Hashtable methods;

  public JavaModule(String theClassName)
    throws ClassNotFoundException,
           NoSuchMethodException
  {
    this.className = theClassName;
    this.methods = new Hashtable();

    this.loadedClass = Class.forName(this.className);

    Method[] methodList = this.loadedClass.getMethods();

    for (int i = 0; i < methodList.length; i++) {
      int modifiers = methodList[i].getModifiers();

      // Public local method
      if (methodList[i].getDeclaringClass() == this.loadedClass && Modifier.isPublic(modifiers)) {
        Class[] parameterTypes = methodList[i].getParameterTypes();


        for (int j = 0; j < expectedTypes.length; j++) {
          if (parameterTypes.length == expectedTypes[j].length) { // Same number of parameters
            int count;

            for (count = 0; count < parameterTypes.length; count++) {
              if (expectedTypes[j][count] != parameterTypes[count]) {
                break;
              }
            }
  
            if (count == expectedTypes[j].length) { // Same parameter types: bingo!
              this.methods.put(methodList[i].getName(), methodList[i]);
	          break;
            }
          }
        }
      }
    }

    if (this.methods.size() == 0) {
      throw new NoSuchMethodException("No suitable methods in class " + this.className);
    }
  }

  public Instance createInstance(Document document, Dictionary parameters) throws LanguageException {
    try {
      Instance instance = new JavaInstance(this.loadedClass.newInstance(), this.methods);
      Object object = instance.getInstance();

      if (object instanceof Configurable) {
        Configurations configurations = new Configurations();

        configurations.put("document", document);
        configurations.put("parameters", parameters);

        ((Configurable) object).init(configurations);
      }

      return instance;
    } catch (Exception e) {
      throw new LanguageException(e.getMessage());
    }
  }
}