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
package org.apache.cocoon.components.xpointer.parser;

public class XPointerFrameworkParserTokenManager
    implements XPointerFrameworkParserConstants {
    public java.io.PrintStream debugStream = System.out;
    public void setDebugStream(java.io.PrintStream ds) {
        debugStream = ds;
    }
    private final int jjStopAtPos(int pos, int kind) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        return pos + 1;
    }
    private final int jjMoveStringLiteralDfa0_0() {
        switch (curChar) {
            case 40 :
                return jjStopAtPos(0, 10);
            case 41 :
                return jjStopAtPos(0, 11);
            default :
                return jjMoveNfa_0(0, 0);
        }
    }
    private final void jjCheckNAdd(int state) {
        if (jjrounds[state] != jjround) {
            jjstateSet[jjnewStateCnt++] = state;
            jjrounds[state] = jjround;
        }
    }
    private final void jjAddStates(int start, int end) {
        do {
            jjstateSet[jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }
    private final void jjCheckNAddTwoStates(int state1, int state2) {
        jjCheckNAdd(state1);
        jjCheckNAdd(state2);
    }
    private final void jjCheckNAddStates(int start, int end) {
        do {
            jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }
    static final long[] jjbitVec0 =
        { 0x0L, 0xffffffffffffc000L, 0xfffff0007fffffffL, 0x7fffffL };
    static final long[] jjbitVec2 = { 0x0L, 0x0L, 0x0L, 0xff7fffffff7fffffL };
    static final long[] jjbitVec3 =
        {
            0x7ff3ffffffffffffL,
            0x7ffffffffffffdfeL,
            0xffffffffffffffffL,
            0xfc31ffffffffe00fL };
    static final long[] jjbitVec4 =
        { 0xffffffL, 0xffffffffffff0000L, 0xf80001ffffffffffL, 0x3L };
    static final long[] jjbitVec5 =
        { 0x0L, 0x0L, 0xfffffffbffffd740L, 0xffffd547f7fffL };
    static final long[] jjbitVec6 =
        {
            0xffffffffffffdffeL,
            0xffffffffdffeffffL,
            0xffffffffffff0003L,
            0x33fcfffffff199fL };
    static final long[] jjbitVec7 =
        { 0xfffe000000000000L, 0xfffffffe027fffffL, 0x7fL, 0x707ffffff0000L };
    static final long[] jjbitVec8 =
        {
            0x7fffffe00000000L,
            0xfffe0000000007feL,
            0x7cffffffffffffffL,
            0x60002f7fffL };
    static final long[] jjbitVec9 =
        {
            0x23ffffffffffffe0L,
            0x3ff000000L,
            0x3c5fdfffff99fe0L,
            0x30003b0000000L };
    static final long[] jjbitVec10 =
        {
            0x36dfdfffff987e0L,
            0x1c00005e000000L,
            0x23edfdfffffbafe0L,
            0x100000000L };
    static final long[] jjbitVec11 =
        { 0x23cdfdfffff99fe0L, 0x3b0000000L, 0x3bfc718d63dc7e0L, 0x0L };
    static final long[] jjbitVec12 =
        { 0x3effdfffffddfe0L, 0x300000000L, 0x3effdfffffddfe0L, 0x340000000L };
    static final long[] jjbitVec13 =
        { 0x3fffdfffffddfe0L, 0x300000000L, 0x0L, 0x0L };
    static final long[] jjbitVec14 =
        { 0xd7ffffffffffeL, 0x3fL, 0x200d6caefef02596L, 0x1fL };
    static final long[] jjbitVec15 = { 0x0L, 0x3fffffffeffL, 0x0L, 0x0L };
    static final long[] jjbitVec16 =
        { 0x0L, 0x0L, 0xffffffff00000000L, 0x7fffffffff003fL };
    static final long[] jjbitVec17 =
        {
            0x500000000007daedL,
            0x2c62ab82315001L,
            0xf580c90040000000L,
            0x201080000000007L };
    static final long[] jjbitVec18 =
        {
            0xffffffffffffffffL,
            0xffffffffffffffffL,
            0xffffffff0fffffffL,
            0x3ffffffffffffffL };
    static final long[] jjbitVec19 =
        {
            0xffffffff3f3fffffL,
            0x3fffffffaaff3f3fL,
            0x5fdfffffffffffffL,
            0x1fdc1fff0fcf1fdcL };
    static final long[] jjbitVec20 = { 0x4c4000000000L, 0x0L, 0x7L, 0x0L };
    static final long[] jjbitVec21 =
        {
            0x3fe00000080L,
            0xfffffffffffffffeL,
            0xfffffffe001fffffL,
            0x7ffffffffffffffL };
    static final long[] jjbitVec22 = { 0x1fffffffffe0L, 0x0L, 0x0L, 0x0L };
    static final long[] jjbitVec23 =
        { 0xffffffffffffffffL, 0xffffffffffffffffL, 0x3fffffffffL, 0x0L };
    static final long[] jjbitVec24 =
        { 0xffffffffffffffffL, 0xffffffffffffffffL, 0xfffffffffL, 0x0L };
    static final long[] jjbitVec25 =
        { 0x0L, 0x0L, 0x80000000000000L, 0xff7fffffff7fffffL };
    static final long[] jjbitVec26 =
        { 0xffffffL, 0xffffffffffff0000L, 0xf80001ffffffffffL, 0x30003L };
    static final long[] jjbitVec27 =
        {
            0xffffffffffffffffL,
            0x30000003fL,
            0xfffffffbffffd7c0L,
            0xffffd547f7fffL };
    static final long[] jjbitVec28 =
        {
            0xffffffffffffdffeL,
            0xffffffffdffeffffL,
            0xffffffffffff007bL,
            0x33fcfffffff199fL };
    static final long[] jjbitVec29 =
        {
            0xfffe000000000000L,
            0xfffffffe027fffffL,
            0xbbfffffbfffe007fL,
            0x707ffffff0016L };
    static final long[] jjbitVec30 =
        {
            0x7fffffe00000000L,
            0xffff03ff0007ffffL,
            0x7cffffffffffffffL,
            0x3ff3dffffef7fffL };
    static final long[] jjbitVec31 =
        {
            0xf3ffffffffffffeeL,
            0xffcfff1e3fffL,
            0xd3c5fdfffff99feeL,
            0x3ffcfb080399fL };
    static final long[] jjbitVec32 =
        {
            0xd36dfdfffff987e4L,
            0x1fffc05e003987L,
            0xf3edfdfffffbafeeL,
            0xffc100003bbfL };
    static final long[] jjbitVec33 =
        {
            0xf3cdfdfffff99feeL,
            0xffc3b0c0398fL,
            0xc3bfc718d63dc7ecL,
            0xff8000803dc7L };
    static final long[] jjbitVec34 =
        {
            0xc3effdfffffddfeeL,
            0xffc300603ddfL,
            0xc3effdfffffddfecL,
            0xffc340603ddfL };
    static final long[] jjbitVec35 =
        { 0xc3fffdfffffddfecL, 0xffc300803dcfL, 0x0L, 0x0L };
    static final long[] jjbitVec36 =
        { 0x7ff7ffffffffffeL, 0x3ff7fffL, 0x3bff6caefef02596L, 0x3ff3f5fL };
    static final long[] jjbitVec37 =
        { 0xc2a003ff03000000L, 0xfffe03fffffffeffL, 0x2fe3ffffebf0fdfL, 0x0L };
    static final long[] jjbitVec38 = { 0x0L, 0x0L, 0x0L, 0x21fff0000L };
    static final long[] jjbitVec39 =
        {
            0x3efffe000000a0L,
            0xfffffffffffffffeL,
            0xfffffffe661fffffL,
            0x77ffffffffffffffL };
    private final int jjMoveNfa_0(int startState, int curPos) {
        int startsAt = 0;
        jjnewStateCnt = 7;
        int i = 1;
        jjstateSet[0] = startState;
        int kind = 0x7fffffff;
        for (;;) {
            if (++jjround == 0x7fffffff)
                ReInitRounds();
            if (curChar < 64) {
                long l = 1L << curChar;
                MatchLoop : do {
                    switch (jjstateSet[--i]) {
                        case 0 :
                            if ((0x100002600L & l) != 0L)
                                kind = 8;
                            break;
                        case 2 :
                            if ((0x3ff600000000000L & l) == 0L)
                                break;
                            if (kind > 7)
                                kind = 7;
                            jjstateSet[jjnewStateCnt++] = 2;
                            break;
                        case 3 :
                            if ((0x3ff600000000000L & l) != 0L)
                                jjAddStates(0, 1);
                            break;
                        case 4 :
                            if (curChar == 58)
                                jjstateSet[jjnewStateCnt++] = 5;
                            break;
                        case 6 :
                            if ((0x3ff600000000000L & l) == 0L)
                                break;
                            if (kind > 9)
                                kind = 9;
                            jjstateSet[jjnewStateCnt++] = 6;
                            break;
                        default :
                            break;
                    }
                } while (i != startsAt);
            } else if (curChar < 128) {
                long l = 1L << (curChar & 077);
                MatchLoop : do {
                    switch (jjstateSet[--i]) {
                        case 0 :
                            if ((0x7fffffe87fffffeL & l) == 0L)
                                break;
                            if (kind > 7)
                                kind = 7;
                            jjCheckNAddStates(2, 5);
                            break;
                        case 2 :
                            if ((0x7fffffe87fffffeL & l) == 0L)
                                break;
                            if (kind > 7)
                                kind = 7;
                            jjCheckNAdd(2);
                            break;
                        case 3 :
                            if ((0x7fffffe87fffffeL & l) != 0L)
                                jjCheckNAddTwoStates(3, 4);
                            break;
                        case 5 :
                        case 6 :
                            if ((0x7fffffe87fffffeL & l) == 0L)
                                break;
                            if (kind > 9)
                                kind = 9;
                            jjCheckNAdd(6);
                            break;
                        default :
                            break;
                    }
                } while (i != startsAt);
            } else {
                int hiByte = (curChar >> 8);
                int i1 = hiByte >> 6;
                long l1 = 1L << (hiByte & 077);
                int i2 = (curChar & 0xff) >> 6;
                long l2 = 1L << (curChar & 077);
                MatchLoop : do {
                    switch (jjstateSet[--i]) {
                        case 0 :
                            if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                break;
                            if (kind > 7)
                                kind = 7;
                            jjCheckNAddStates(2, 5);
                            break;
                        case 2 :
                            if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                                break;
                            if (kind > 7)
                                kind = 7;
                            jjCheckNAdd(2);
                            break;
                        case 3 :
                            if (jjCanMove_1(hiByte, i1, i2, l1, l2))
                                jjCheckNAddTwoStates(3, 4);
                            break;
                        case 5 :
                            if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                break;
                            if (kind > 9)
                                kind = 9;
                            jjCheckNAdd(6);
                            break;
                        case 6 :
                            if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                                break;
                            if (kind > 9)
                                kind = 9;
                            jjCheckNAdd(6);
                            break;
                        default :
                            break;
                    }
                } while (i != startsAt);
            }
            if (kind != 0x7fffffff) {
                jjmatchedKind = kind;
                jjmatchedPos = curPos;
                kind = 0x7fffffff;
            }
            ++curPos;
            if ((i = jjnewStateCnt)
                == (startsAt = 7 - (jjnewStateCnt = startsAt)))
                return curPos;
            try {
                curChar = input_stream.readChar();
            } catch (java.io.IOException e) {
                return curPos;
            }
        }
    }
    private final int jjStopStringLiteralDfa_1(int pos, long active0) {
        switch (pos) {
            default :
                return -1;
        }
    }
    private final int jjStartNfa_1(int pos, long active0) {
        return jjMoveNfa_1(jjStopStringLiteralDfa_1(pos, active0), pos + 1);
    }

    private final int jjMoveStringLiteralDfa0_1() {
        switch (curChar) {
            case 40 :
                return jjStopAtPos(0, 10);
            case 41 :
                return jjStopAtPos(0, 11);
            case 94 :
                return jjMoveStringLiteralDfa1_1(0x7000L);
            default :
                return jjMoveNfa_1(0, 0);
        }
    }
    private final int jjMoveStringLiteralDfa1_1(long active0) {
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_1(0, active0);
            return 1;
        }
        switch (curChar) {
            case 40 :
                if ((active0 & 0x1000L) != 0L)
                    return jjStopAtPos(1, 12);
                break;
            case 41 :
                if ((active0 & 0x2000L) != 0L)
                    return jjStopAtPos(1, 13);
                break;
            case 94 :
                if ((active0 & 0x4000L) != 0L)
                    return jjStopAtPos(1, 14);
                break;
            default :
                break;
        }
        return jjStartNfa_1(0, active0);
    }
    static final long[] jjbitVec40 =
        {
            0xfffffffffffffffeL,
            0xffffffffffffffffL,
            0xffffffffffffffffL,
            0xffffffffffffffffL };
    static final long[] jjbitVec41 =
        { 0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL };
    private final int jjMoveNfa_1(int startState, int curPos) {
        int startsAt = 0;
        jjnewStateCnt = 1;
        int i = 1;
        jjstateSet[0] = startState;
        int kind = 0x7fffffff;
        for (;;) {
            if (++jjround == 0x7fffffff)
                ReInitRounds();
            if (curChar < 64) {
                long l = 1L << curChar;
                MatchLoop : do {
                    switch (jjstateSet[--i]) {
                        case 0 :
                            if ((0xfffffcffffffffffL & l) != 0L)
                                kind = 15;
                            break;
                        default :
                            break;
                    }
                } while (i != startsAt);
            } else if (curChar < 128) {
                long l = 1L << (curChar & 077);
                MatchLoop : do {
                    switch (jjstateSet[--i]) {
                        case 0 :
                            if ((0xffffffffbfffffffL & l) != 0L)
                                kind = 15;
                            break;
                        default :
                            break;
                    }
                } while (i != startsAt);
            } else {
                int hiByte = (curChar >> 8);
                int i1 = hiByte >> 6;
                long l1 = 1L << (hiByte & 077);
                int i2 = (curChar & 0xff) >> 6;
                long l2 = 1L << (curChar & 077);
                MatchLoop : do {
                    switch (jjstateSet[--i]) {
                        case 0 :
                            if (jjCanMove_2(hiByte, i1, i2, l1, l2)
                                && kind > 15)
                                kind = 15;
                            break;
                        default :
                            break;
                    }
                } while (i != startsAt);
            }
            if (kind != 0x7fffffff) {
                jjmatchedKind = kind;
                jjmatchedPos = curPos;
                kind = 0x7fffffff;
            }
            ++curPos;
            if ((i = jjnewStateCnt)
                == (startsAt = 1 - (jjnewStateCnt = startsAt)))
                return curPos;
            try {
                curChar = input_stream.readChar();
            } catch (java.io.IOException e) {
                return curPos;
            }
        }
    }
    static final int[] jjnextStates = { 3, 4, 2, 3, 4, 6, };
    private static final boolean jjCanMove_0(
        int hiByte,
        int i1,
        int i2,
        long l1,
        long l2) {
        switch (hiByte) {
            case 0 :
                return ((jjbitVec2[i2] & l2) != 0L);
            case 1 :
                return ((jjbitVec3[i2] & l2) != 0L);
            case 2 :
                return ((jjbitVec4[i2] & l2) != 0L);
            case 3 :
                return ((jjbitVec5[i2] & l2) != 0L);
            case 4 :
                return ((jjbitVec6[i2] & l2) != 0L);
            case 5 :
                return ((jjbitVec7[i2] & l2) != 0L);
            case 6 :
                return ((jjbitVec8[i2] & l2) != 0L);
            case 9 :
                return ((jjbitVec9[i2] & l2) != 0L);
            case 10 :
                return ((jjbitVec10[i2] & l2) != 0L);
            case 11 :
                return ((jjbitVec11[i2] & l2) != 0L);
            case 12 :
                return ((jjbitVec12[i2] & l2) != 0L);
            case 13 :
                return ((jjbitVec13[i2] & l2) != 0L);
            case 14 :
                return ((jjbitVec14[i2] & l2) != 0L);
            case 15 :
                return ((jjbitVec15[i2] & l2) != 0L);
            case 16 :
                return ((jjbitVec16[i2] & l2) != 0L);
            case 17 :
                return ((jjbitVec17[i2] & l2) != 0L);
            case 30 :
                return ((jjbitVec18[i2] & l2) != 0L);
            case 31 :
                return ((jjbitVec19[i2] & l2) != 0L);
            case 33 :
                return ((jjbitVec20[i2] & l2) != 0L);
            case 48 :
                return ((jjbitVec21[i2] & l2) != 0L);
            case 49 :
                return ((jjbitVec22[i2] & l2) != 0L);
            case 159 :
                return ((jjbitVec23[i2] & l2) != 0L);
            case 215 :
                return ((jjbitVec24[i2] & l2) != 0L);
            default :
                if ((jjbitVec0[i1] & l1) != 0L)
                    return true;
                return false;
        }
    }
    private static final boolean jjCanMove_1(
        int hiByte,
        int i1,
        int i2,
        long l1,
        long l2) {
        switch (hiByte) {
            case 0 :
                return ((jjbitVec25[i2] & l2) != 0L);
            case 1 :
                return ((jjbitVec3[i2] & l2) != 0L);
            case 2 :
                return ((jjbitVec26[i2] & l2) != 0L);
            case 3 :
                return ((jjbitVec27[i2] & l2) != 0L);
            case 4 :
                return ((jjbitVec28[i2] & l2) != 0L);
            case 5 :
                return ((jjbitVec29[i2] & l2) != 0L);
            case 6 :
                return ((jjbitVec30[i2] & l2) != 0L);
            case 9 :
                return ((jjbitVec31[i2] & l2) != 0L);
            case 10 :
                return ((jjbitVec32[i2] & l2) != 0L);
            case 11 :
                return ((jjbitVec33[i2] & l2) != 0L);
            case 12 :
                return ((jjbitVec34[i2] & l2) != 0L);
            case 13 :
                return ((jjbitVec35[i2] & l2) != 0L);
            case 14 :
                return ((jjbitVec36[i2] & l2) != 0L);
            case 15 :
                return ((jjbitVec37[i2] & l2) != 0L);
            case 16 :
                return ((jjbitVec16[i2] & l2) != 0L);
            case 17 :
                return ((jjbitVec17[i2] & l2) != 0L);
            case 30 :
                return ((jjbitVec18[i2] & l2) != 0L);
            case 31 :
                return ((jjbitVec19[i2] & l2) != 0L);
            case 32 :
                return ((jjbitVec38[i2] & l2) != 0L);
            case 33 :
                return ((jjbitVec20[i2] & l2) != 0L);
            case 48 :
                return ((jjbitVec39[i2] & l2) != 0L);
            case 49 :
                return ((jjbitVec22[i2] & l2) != 0L);
            case 159 :
                return ((jjbitVec23[i2] & l2) != 0L);
            case 215 :
                return ((jjbitVec24[i2] & l2) != 0L);
            default :
                if ((jjbitVec0[i1] & l1) != 0L)
                    return true;
                return false;
        }
    }
    private static final boolean jjCanMove_2(
        int hiByte,
        int i1,
        int i2,
        long l1,
        long l2) {
        switch (hiByte) {
            case 0 :
                return ((jjbitVec41[i2] & l2) != 0L);
            default :
                if ((jjbitVec40[i1] & l1) != 0L)
                    return true;
                return false;
        }
    }
    public static final String[] jjstrLiteralImages =
        {
            "",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "\50",
            "\51",
            "\136\50",
            "\136\51",
            "\136\136",
            null,
            };
    public static final String[] lexStateNames = { "DEFAULT", "IN_SCHEME", };
    public static final int[] jjnewLexState =
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, };
    protected SimpleCharStream input_stream;
    private final int[] jjrounds = new int[7];
    private final int[] jjstateSet = new int[14];
    protected char curChar;
    public XPointerFrameworkParserTokenManager(SimpleCharStream stream) {
        if (SimpleCharStream.staticFlag)
            throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
        input_stream = stream;
    }
    public XPointerFrameworkParserTokenManager(
        SimpleCharStream stream,
        int lexState) {
        this(stream);
        SwitchTo(lexState);
    }
    public void ReInit(SimpleCharStream stream) {
        jjmatchedPos = jjnewStateCnt = 0;
        curLexState = defaultLexState;
        input_stream = stream;
        ReInitRounds();
    }
    private final void ReInitRounds() {
        int i;
        jjround = 0x80000001;
        for (i = 7; i-- > 0;)
            jjrounds[i] = 0x80000000;
    }
    public void ReInit(SimpleCharStream stream, int lexState) {
        ReInit(stream);
        SwitchTo(lexState);
    }
    public void SwitchTo(int lexState) {
        if (lexState >= 2 || lexState < 0)
            throw new TokenMgrError(
                "Error: Ignoring invalid lexical state : "
                    + lexState
                    + ". State unchanged.",
                TokenMgrError.INVALID_LEXICAL_STATE);
        else
            curLexState = lexState;
    }

    protected Token jjFillToken() {
        Token t = Token.newToken(jjmatchedKind);
        t.kind = jjmatchedKind;
        String im = jjstrLiteralImages[jjmatchedKind];
        t.image = (im == null) ? input_stream.GetImage() : im;
        t.beginLine = input_stream.getBeginLine();
        t.beginColumn = input_stream.getBeginColumn();
        t.endLine = input_stream.getEndLine();
        t.endColumn = input_stream.getEndColumn();
        return t;
    }

    int curLexState = 0;
    int defaultLexState = 0;
    int jjnewStateCnt;
    int jjround;
    int jjmatchedPos;
    int jjmatchedKind;

    public Token getNextToken() {
        Token matchedToken;
        int curPos = 0;

        EOFLoop : for (;;) {
            try {
                curChar = input_stream.BeginToken();
            } catch (java.io.IOException e) {
                jjmatchedKind = 0;
                matchedToken = jjFillToken();
                return matchedToken;
            }

            switch (curLexState) {
                case 0 :
                    jjmatchedKind = 0x7fffffff;
                    jjmatchedPos = 0;
                    curPos = jjMoveStringLiteralDfa0_0();
                    break;
                case 1 :
                    jjmatchedKind = 0x7fffffff;
                    jjmatchedPos = 0;
                    curPos = jjMoveStringLiteralDfa0_1();
                    break;
            }
            if (jjmatchedKind != 0x7fffffff) {
                if (jjmatchedPos + 1 < curPos)
                    input_stream.backup(curPos - jjmatchedPos - 1);
                matchedToken = jjFillToken();
                if (jjnewLexState[jjmatchedKind] != -1)
                    curLexState = jjnewLexState[jjmatchedKind];
                return matchedToken;
            }
            int error_line = input_stream.getEndLine();
            int error_column = input_stream.getEndColumn();
            String error_after = null;
            boolean EOFSeen = false;
            try {
                input_stream.readChar();
                input_stream.backup(1);
            } catch (java.io.IOException e1) {
                EOFSeen = true;
                error_after = curPos <= 1 ? "" : input_stream.GetImage();
                if (curChar == '\n' || curChar == '\r') {
                    error_line++;
                    error_column = 0;
                } else
                    error_column++;
            }
            if (!EOFSeen) {
                input_stream.backup(1);
                error_after = curPos <= 1 ? "" : input_stream.GetImage();
            }
            throw new TokenMgrError(
                EOFSeen,
                curLexState,
                error_line,
                error_column,
                error_after,
                curChar,
                TokenMgrError.LEXICAL_ERROR);
        }
    }

}
