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
package org.apache.cocoon.components.flow.java;

import java.util.EmptyStackException;

/**
 * Stack to store the frame information along the invocation trace.
 *
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: ContinuationStack.java,v 1.1 2004/03/29 17:47:21 stephan Exp $
 */
public class ContinuationStack implements java.io.Serializable {

    private int[] istack;
    private float[] fstack;
    private double[] dstack;
    private long[] lstack;
    private Object[] astack;
    private Object[] tstack;
    private int iTop, fTop, dTop, lTop, aTop, tTop;

    public ContinuationStack() {
        istack = new int[10];
        lstack = new long[5];
        dstack = new double[5];
        fstack = new float[5];
        astack = new Object[10];
        tstack = new Object[5];
    }

    public ContinuationStack(ContinuationStack parent) {
        istack = new int[parent.istack.length];
        lstack = new long[parent.lstack.length];
        dstack = new double[parent.dstack.length];
        fstack = new float[parent.fstack.length];
        astack = new Object[parent.astack.length];
        tstack = new Object[parent.tstack.length];
        System.arraycopy(parent.istack, 0, istack, 0, parent.istack.length);
        System.arraycopy(parent.lstack, 0, lstack, 0, parent.lstack.length);
        System.arraycopy(parent.dstack, 0, dstack, 0, parent.dstack.length);
        System.arraycopy(parent.fstack, 0, fstack, 0, parent.fstack.length);
        System.arraycopy(parent.astack, 0, astack, 0, parent.astack.length);
        System.arraycopy(parent.tstack, 0, tstack, 0, parent.tstack.length);
        iTop = parent.iTop;
        fTop = parent.fTop;
        dTop = parent.dTop;
        lTop = parent.lTop;
        aTop = parent.aTop;
        tTop = parent.tTop;
    }

    public double popDouble() {
        if (dTop==0)
            throw new EmptyStackException();
        double d = dstack[--dTop];
        //System.out.println("pop double "+d+" "+toString());
        return d;
    }

    public float popFloat() {
        if (fTop==0)
            throw new EmptyStackException();
        float f = fstack[--fTop];
        //System.out.println("pop float "+f+" "+toString());
        return f;
    }

    public int popInt() {
        if (iTop==0)
            throw new EmptyStackException();
        int i = istack[--iTop];
        //System.out.println("pop int "+i+" "+toString());
        return i;
    }

    public long popLong() {
        if (lTop==0)
            throw new EmptyStackException();
        long l = lstack[--lTop];
        //System.out.println("pop long "+l+" "+toString());
        return l;
    }

    public Object popObject() {
        if (aTop==0)
            throw new EmptyStackException();
        Object o = astack[--aTop];
        //System.out.println("pop object "+o+" "+toString());
        return o;
    }

    public Object popReference() {
        if (tTop==0)
            throw new EmptyStackException();
        Object o = tstack[--tTop];
        //System.out.println("pop reference "+o+" "+toString());
        return o;
    }

    public void pushDouble(double d) {
        dstack[dTop++] = d;
        //System.out.println("push double "+d+" "+toString());
        if (dTop == dstack.length) {
            double[] hlp = new double[dstack.length + 10];
            System.arraycopy(dstack, 0, hlp, 0, dstack.length);
            dstack = hlp;
        }
    }

    public void pushFloat(float f) {
        fstack[fTop++] = f;
        //System.out.println("push float "+f+" "+toString());
        if (fTop == fstack.length) {
            float[] hlp = new float[fstack.length + 10];
            System.arraycopy(fstack, 0, hlp, 0, fstack.length);
            fstack = hlp;
        }
    }

    public void pushInt(int i) {
        istack[iTop++] = i;
        //System.out.println("push int "+i+" "+toString());
        if (iTop == istack.length) {
            int[] hlp = new int[istack.length + 10];
            System.arraycopy(istack, 0, hlp, 0, istack.length);
            istack = hlp;
        }
    }

    public void pushLong(long l) {
        lstack[lTop++] = l;
        //System.out.println("push long "+l+" "+toString());
        if (lTop == lstack.length) {
            long[] hlp = new long[lstack.length + 10];
            System.arraycopy(lstack, 0, hlp, 0, lstack.length);
            lstack = hlp;
        }
    }

    public void pushObject(Object o) {
        astack[aTop++] = o;
        //System.out.println("push object "+o+" "+toString());
        if (aTop == astack.length) {
            Object[] hlp = new Object[astack.length + 10];
            System.arraycopy(astack, 0, hlp, 0, astack.length);
            astack = hlp;
        }
    }

    public void pushReference(Object o) {
        tstack[tTop++] = o;
        //System.out.println("push reference "+o+" "+toString());
        if (tTop == tstack.length) {
            Object[] hlp = new Object[tstack.length + 10];
            System.arraycopy(tstack, 0, hlp, 0, tstack.length);
            tstack = hlp;
        }
    }

    public String toString() {
        return "i="+iTop +
         ",l=" + lTop +
         ",d=" + dTop +
         ",f=" + fTop +
         ",a=" + aTop +
         ",t=" + tTop;
    }
}
