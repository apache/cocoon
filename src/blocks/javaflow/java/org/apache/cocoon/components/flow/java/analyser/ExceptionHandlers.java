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
package org.apache.cocoon.components.flow.java.analyser;

import org.apache.bcel.generic.*;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * This class allows easy access to ExceptionHandler objects.
 * 
 * WARNING! These classes are a fork of the bcel verifier.
 *
 * @version $Id: ExceptionHandlers.java,v 1.1 2004/06/03 12:43:27 stephan Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public class ExceptionHandlers{
	/**
	 * The ExceptionHandler instances.
	 * Key: InstructionHandle objects, Values: HashSet<ExceptionHandler> instances.
	 */
	private Hashtable exceptionhandlers;
	 
	/**
	 * Constructor. Creates a new ExceptionHandlers instance.
	 */
	public ExceptionHandlers(MethodGen mg){
		exceptionhandlers = new Hashtable();
		CodeExceptionGen[] cegs = mg.getExceptionHandlers();
		for (int i=0; i<cegs.length; i++){
			ExceptionHandler eh = new ExceptionHandler(cegs[i].getCatchType(), cegs[i].getHandlerPC());
			for (InstructionHandle ih=cegs[i].getStartPC(); ih != cegs[i].getEndPC().getNext(); ih=ih.getNext()){
				HashSet hs;
				hs = (HashSet) exceptionhandlers.get(ih);
				if (hs == null){
					hs = new HashSet();
					exceptionhandlers.put(ih, hs);
				}
				hs.add(eh);
			}
		}
	}
	
	/**
	 * Returns all the ExceptionHandler instances representing exception
	 * handlers that protect the instruction ih.
	 */
	public ExceptionHandler[] getExceptionHandlers(InstructionHandle ih){
		HashSet hs = (HashSet) exceptionhandlers.get(ih);
		if (hs == null) return new ExceptionHandler[0];
		else{
			ExceptionHandler[] ret = new ExceptionHandler[hs.size()];
			return (ExceptionHandler[]) (hs.toArray(ret));
		}
	}

}
