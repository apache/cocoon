/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.cocoon.components.flow.java;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.ConstantValue;
import org.apache.bcel.classfile.Deprecated;
import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Synthetic;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.TABLESWITCH;

public final class DecompilingVisitor extends org.apache.bcel.classfile.EmptyVisitor {
	private JavaClass clazz;
	private PrintWriter out;
	private String clazzname;
	private ConstantPoolGen cp;

	public DecompilingVisitor(JavaClass clazz, OutputStream out) {
		this.clazz = clazz;
		this.out = new PrintWriter(out);
		clazzname = clazz.getClassName();
		cp = new ConstantPoolGen(clazz.getConstantPool());
	}

	public void start() {
		new org.apache.bcel.classfile.DescendingVisitor(clazz, this).visit();
		out.close();
	}

	public void visitJavaClass(JavaClass clazz) {

		out.println("// source " + clazz.getSourceFileName());
		out.println(Utility.accessToString(clazz.getAccessFlags(), true) + " "
				+ Utility.classOrInterface(clazz.getAccessFlags()) + " "
				+ clazz.getClassName().replace('.', '/'));
		out.println("  extends " + clazz.getSuperclassName().replace('.', '/'));

		String[] interfaces = clazz.getInterfaceNames();

		if (interfaces.length > 0) {
			out.print("  implements");
			for (int i = 0; i < interfaces.length; i++)
				out.print(" " + interfaces[i].replace('.', '/'));
			out.println();
		}
		out.println();
	}

	public void visitField(Field field) {
		out.print("  " + Utility.accessToString(field.getAccessFlags()) + " "
				+ field.getName() + " " + field.getSignature());
		if (field.getAttributes().length == 0)
			out.print("\n");
	}

	public void visitConstantValue(ConstantValue cv) {
		out.println(" = " + cv);
	}

	private Method _method;

	/**
	 * Unfortunately Jasmin expects ".end method" after each method. Thus we've
	 * to check for every of the method's attributes if it's the last one and
	 * print ".end method" then.
	 */
	private final void printEndMethod(Attribute attr) {
		Attribute[] attributes = _method.getAttributes();

	}

	public void visitDeprecated(Deprecated attribute) {
		printEndMethod(attribute);
	}

	public void visitSynthetic(Synthetic attribute) {
		if (_method != null)
			printEndMethod(attribute);
	}

	public void visitMethod(Method method) {
		this._method = method; // Remember for use in subsequent visitXXX calls

		out.println("\n  " + Utility.accessToString(_method.getAccessFlags())
				+ " " + _method.getName() + _method.getSignature());

	}

	public void visitExceptionTable(ExceptionTable e) {
		String[] names = e.getExceptionNames();
		for (int i = 0; i < names.length; i++)
			out.println("    throws " + names[i].replace('.', '/'));

		printEndMethod(e);
	}

	private Hashtable map;

	public void visitCode(Code code) {
		int label_counter = 0;

		MethodGen mg = new MethodGen(_method, clazzname, cp);
		InstructionList il = mg.getInstructionList();
		InstructionHandle[] ihs = il.getInstructionHandles();

		LocalVariableGen[] lvs = mg.getLocalVariables();

		CodeExceptionGen[] ehs = mg.getExceptionHandlers();

		for (int i = 0; i < lvs.length; i++) {
			LocalVariableGen l = lvs[i];
			out.println("    // var " + l.getIndex() + " is \"" + l.getName()
					+ "\" " + l.getType().getSignature() + " from "
					+ l.getStart().getPosition() + " to "
					+ l.getEnd().getPosition());
		}

		out.print("\n");

		for (int i = 0; i < ihs.length; i++) {
			InstructionHandle ih = ihs[i];
			Instruction inst = ih.getInstruction();

			out.print("    " + ih.getPosition());

			if (inst instanceof BranchInstruction) {
				if (inst instanceof Select) { // Special cases LOOKUPSWITCH and
											  // TABLESWITCH
					Select s = (Select) inst;
					int[] matchs = s.getMatchs();
					InstructionHandle[] targets = s.getTargets();

					if (s instanceof TABLESWITCH) {
						out.println("  tableswitch " + matchs[0] + " "
								+ matchs[matchs.length - 1]);

						for (int j = 0; j < targets.length; j++)
							out.println("        " + targets[j].getPosition());

					} else { // LOOKUPSWITCH
						out.println("  lookupswitch ");

						for (int j = 0; j < targets.length; j++)
							out.println("        " + matchs[j] + " : "
									+ targets[j].getPosition());
					}

					out.println("        default: " + s.getTarget()); // Applies
																	  // for
																	  // both
				} else {
					BranchInstruction bi = (BranchInstruction) inst;
					ih = bi.getTarget();
					//str = get(ih);
					out.println("  " + Constants.OPCODE_NAMES[bi.getOpcode()]
							+ " " + ih);
				}
			} else
				out.println("  " + inst.toString(cp.getConstantPool()));
		}

		out.print("\n");

		for (int i = 0; i < ehs.length; i++) {
			CodeExceptionGen c = ehs[i];
			ObjectType caught = c.getCatchType();
			String class_name = (caught == null) ? // catch any exception, used
												   // when compiling finally
					"all" : caught.getClassName().replace('.', '/');

			out.println("    catch " + class_name + " from "
					+ c.getStartPC().getPosition() + " to "
					+ c.getEndPC().getPosition() + " using "
					+ c.getHandlerPC().getPosition());
		}

		printEndMethod(code);
	}
}

