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
 * This interface defines properties of JVM bytecode subroutines.
 * Note that it is 'abused' to maintain the top-level code in a
 * consistent fashion, too.
 * 
 * WARNING! These classes are a fork of the bcel verifier.
 *
 * @version $Id: Subroutine.java,v 1.1 2004/06/03 12:43:27 stephan Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public interface Subroutine{
	/**
	 * Returns all the JsrInstructions that have the
	 * first instruction of this subroutine as their target.
	 * <B>Must not be invoked on the 'top-level subroutine'.</B>
	 */
	public InstructionHandle[] getEnteringJsrInstructions();
	
	/**
	 * Returns the one and only RET that leaves the subroutine.
	 * Note that JustIce has a pretty rigid notion of a subroutine.
	 * <B>Must not be invoked on the 'top-level subroutine'.</B>
	 *
	 * @see org.apache.bcel.verifier.structurals.Subroutines
	 */
	public InstructionHandle getLeavingRET();

	/**
	 * Returns all instructions that together form this subroutine.
	 * Note that an instruction is part of exactly one subroutine
	 * (the top-level code is considered to be a special subroutine) -
	 * else it is not reachable at all (dead code).
	 */
	public InstructionHandle[] getInstructions();

	/**
	 * Returns if the given InstructionHandle refers to an instruction
	 * that is part of this subroutine. This is a convenience method
	 * that saves iteration over the InstructionHandle objects returned
	 * by getInstructions().
	 *
	 * @see #getInstructions()
	 */
	public boolean contains(InstructionHandle inst);

	/**
	 * Returns an int[] containing the indices of the local variable slots
	 * accessed by this Subroutine (read-accessed, write-accessed or both);
	 * local variables referenced by subroutines of this subroutine are
	 * not included.
	 *
	 * @see #getRecursivelyAccessedLocalsIndices()
	 */
	public int[] getAccessedLocalsIndices();

	/**
	 * Returns an int[] containing the indices of the local variable slots
	 * accessed by this Subroutine (read-accessed, write-accessed or both);
	 * local variables referenced by subroutines of this subroutine are
	 * included.
	 *
	 * @see #getAccessedLocalsIndices()
	 */
	public int[] getRecursivelyAccessedLocalsIndices();
		
	/**
	 * Returns the subroutines that are directly called from this subroutine.
	 */
	public Subroutine[] subSubs();
}
