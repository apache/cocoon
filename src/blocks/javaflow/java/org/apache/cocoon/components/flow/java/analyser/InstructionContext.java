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

import java.util.ArrayList;

import org.apache.bcel.generic.InstructionHandle;

/**
 * An InstructionContext offers convenient access
 * to information like control flow successors and
 * such.
 *
 * @version $Id: InstructionContext.java,v 1.2 2004/06/29 15:07:14 joerg Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public interface InstructionContext{

	/**
	 * The getTag and setTag methods may be used for
	 * temporary flagging, such as graph colouring.
	 * Nothing in the InstructionContext object depends
	 * on the value of the tag. JustIce does not use it.
	 * 
	 * @see #setTag(int tag)
	 */
	public int getTag();

	/**
	 * The getTag and setTag methods may be used for
	 * temporary flagging, such as graph colouring.
	 * Nothing in the InstructionContext object depends
	 * on the value of the tag. JustIce does not use it.
	 * 
	 * @see #getTag()
	 */
	public void setTag(int tag);

	/**
	 * This method symbolically executes the Instruction
	 * held in the InstructionContext.
	 * It "merges in" the incoming execution frame situation
	 * (see The Java Virtual Machine Specification, 2nd
	 * edition, page 146).
	 * By so doing, the outgoing execution frame situation
	 * is calculated.
	 *
	 * This method is JustIce-specific and is usually of
	 * no sense for users of the ControlFlowGraph class.
	 * They should use getInstruction().accept(Visitor),
	 * possibly in conjunction with the ExecutionVisitor.
	 * 
	 * WARNING! These classes are a fork of the bcel verifier.
	 *
	 * @see ControlFlowGraph
	 * @see ExecutionVisitor
	 * @see #getOutFrame(ArrayList)
	 * @return true -  if and only if the "outgoing" frame situation
	 * changed from the one before execute()ing.
	 */
	boolean execute(Frame inFrame, ArrayList executionPredecessors, ExecutionVisitor ev);

	Frame getInFrame();

	/**
	 * This method returns the outgoing execution frame situation;
	 * therefore <B>it has to be calculated by execute(Frame, ArrayList)
	 * first.</B>
	 *
	 * @see #execute(Frame, ArrayList, ExecutionVisitor)
	 */
	Frame getOutFrame(ArrayList executionPredecessors);
	
	/**
	 * Returns the InstructionHandle this InstructionContext is wrapped around.
	 *
	 * @return The InstructionHandle this InstructionContext is wrapped around.
	 */
	InstructionHandle getInstruction();

	/**
	 * Returns the usual control flow successors.
	 * @see #getExceptionHandlers()
	 */
	InstructionContext[] getSuccessors();

	/**
	 * Returns the exception handlers that protect this instruction.
	 * They are special control flow successors.
	 */
	ExceptionHandler[] getExceptionHandlers();
}
