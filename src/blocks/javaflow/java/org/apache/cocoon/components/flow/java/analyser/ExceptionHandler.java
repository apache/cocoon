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

/**
 * This class represents an exception handler; that is, an ObjectType
 * representing a subclass of java.lang.Throwable and the instruction
 * the handler starts off (represented by an InstructionContext).
 * 
 * WARNING! These classes are a fork of the bcel verifier. 
 * 
 * @version $Id: ExceptionHandler.java,v 1.1 2004/06/03 12:43:27 stephan Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public class ExceptionHandler{
	/** The type of the exception to catch. NULL means ANY. */
	private ObjectType catchtype;
	
	/** The InstructionHandle where the handling begins. */
	private InstructionHandle handlerpc;

	/** Leave instance creation to JustIce. */
	ExceptionHandler(ObjectType catch_type, InstructionHandle handler_pc){
		catchtype = catch_type;
		handlerpc = handler_pc;
	}

	/**
	 * Returns the type of the exception that's handled. <B>'null' means 'ANY'.</B>
	 */
	public ObjectType getExceptionType(){
		return catchtype;
	}

	/**
	 * Returns the InstructionHandle where the handler starts off.
	 */
	public InstructionHandle getHandlerStart(){
		return handlerpc;
	}
}
