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

package org.apache.garbage.parser;

public class ParserTokenManager implements ParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_6(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_6(int pos, long active0)
{
   return jjMoveNfa_6(jjStopStringLiteralDfa_6(pos, active0), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjStartNfaWithStates_6(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_6(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_6()
{
   switch(curChar)
   {
      case 34:
         return jjStopAtPos(0, 28);
      case 38:
         return jjStopAtPos(0, 31);
      case 39:
         return jjStopAtPos(0, 29);
      case 123:
         return jjStopAtPos(0, 30);
      default :
         return jjMoveNfa_6(0, 0);
   }
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private final int jjMoveNfa_6(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 1;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xefffff3bffffffffL & l) == 0L)
                     break;
                  kind = 32;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xd7ffffffffffffffL & l) == 0L)
                     break;
                  kind = 32;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 32)
                     kind = 32;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_5(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_5(int pos, long active0)
{
   return jjMoveNfa_5(jjStopStringLiteralDfa_5(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_5(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_5(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_5()
{
   switch(curChar)
   {
      case 34:
         return jjStopAtPos(0, 28);
      case 38:
         return jjStopAtPos(0, 31);
      case 39:
         return jjStopAtPos(0, 29);
      case 123:
         return jjStopAtPos(0, 30);
      default :
         return jjMoveNfa_5(3, 0);
   }
}
private final int jjMoveNfa_5(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 3:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(0, 1);
                  else if (curChar == 61)
                  {
                     if (kind > 27)
                        kind = 27;
                     jjCheckNAdd(2);
                  }
                  break;
               case 0:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(0, 1);
                  break;
               case 1:
                  if (curChar != 61)
                     break;
                  kind = 27;
                  jjCheckNAdd(2);
                  break;
               case 2:
                  if ((0x100002600L & l) == 0L)
                     break;
                  if (kind > 27)
                     kind = 27;
                  jjCheckNAdd(2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x142000000000000L) != 0L)
            return 23;
         if ((active0 & 0x124000000000L) != 0L)
            return 13;
         return -1;
      case 1:
         if ((active0 & 0x140000000000000L) != 0L)
            return 28;
         if ((active0 & 0x4000000000L) != 0L)
            return 9;
         return -1;
      case 2:
         if ((active0 & 0x40000000000000L) != 0L)
            return 27;
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 35:
         return jjMoveStringLiteralDfa1_0(0x142000000000000L);
      case 38:
         return jjStopAtPos(0, 33);
      case 60:
         return jjMoveStringLiteralDfa1_0(0x124000000000L);
      default :
         return jjMoveNfa_0(12, 0);
   }
}
private final int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 33:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000000000L);
      case 63:
         if ((active0 & 0x100000000000L) != 0L)
            return jjStopAtPos(1, 44);
         break;
      case 91:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000000000L);
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0x140000000000000L);
      case 123:
         if ((active0 & 0x2000000000000L) != 0L)
            return jjStopAtPos(1, 49);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 45:
         return jjMoveStringLiteralDfa3_0(active0, 0x4000000000L);
      case 67:
         return jjMoveStringLiteralDfa3_0(active0, 0x20000000000L);
      case 108:
         return jjMoveStringLiteralDfa3_0(active0, 0x40000000000000L);
      case 110:
         return jjMoveStringLiteralDfa3_0(active0, 0x100000000000000L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private final int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 45:
         if ((active0 & 0x4000000000L) != 0L)
            return jjStopAtPos(3, 38);
         break;
      case 68:
         return jjMoveStringLiteralDfa4_0(active0, 0x20000000000L);
      case 100:
         if ((active0 & 0x100000000000000L) != 0L)
            return jjStopAtPos(3, 56);
         break;
      case 115:
         return jjMoveStringLiteralDfa4_0(active0, 0x40000000000000L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
private final int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa5_0(active0, 0x20000000000L);
      case 101:
         if ((active0 & 0x40000000000000L) != 0L)
            return jjStopAtPos(4, 54);
         break;
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
private final int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(3, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0);
      return 5;
   }
   switch(curChar)
   {
      case 84:
         return jjMoveStringLiteralDfa6_0(active0, 0x20000000000L);
      default :
         break;
   }
   return jjStartNfa_0(4, active0);
}
private final int jjMoveStringLiteralDfa6_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(4, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0);
      return 6;
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa7_0(active0, 0x20000000000L);
      default :
         break;
   }
   return jjStartNfa_0(5, active0);
}
private final int jjMoveStringLiteralDfa7_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(5, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(6, active0);
      return 7;
   }
   switch(curChar)
   {
      case 91:
         if ((active0 & 0x20000000000L) != 0L)
            return jjStopAtPos(7, 41);
         break;
      default :
         break;
   }
   return jjStartNfa_0(6, active0);
}
static final long[] jjbitVec3 = {
   0x0L, 0xffffffffffffc000L, 0xfffff0007fffffffL, 0x7fffffL
};
static final long[] jjbitVec4 = {
   0x0L, 0x0L, 0x0L, 0xff7fffffff7fffffL
};
static final long[] jjbitVec5 = {
   0x7ff3ffffffffffffL, 0x7ffffffffffffdfeL, 0xffffffffffffffffL, 0xfc31ffffffffe00fL
};
static final long[] jjbitVec6 = {
   0xffffffL, 0xffffffffffff0000L, 0xf80001ffffffffffL, 0x3L
};
static final long[] jjbitVec7 = {
   0x0L, 0x0L, 0xfffffffbffffd740L, 0xffffd547f7fffL
};
static final long[] jjbitVec8 = {
   0xffffffffffffdffeL, 0xffffffffdffeffffL, 0xffffffffffff0003L, 0x33fcfffffff199fL
};
static final long[] jjbitVec9 = {
   0xfffe000000000000L, 0xfffffffe027fffffL, 0x7fL, 0x707ffffff0000L
};
static final long[] jjbitVec10 = {
   0x7fffffe00000000L, 0xfffe0000000007feL, 0x7cffffffffffffffL, 0x60002f7fffL
};
static final long[] jjbitVec11 = {
   0x23ffffffffffffe0L, 0x3ff000000L, 0x3c5fdfffff99fe0L, 0x30003b0000000L
};
static final long[] jjbitVec12 = {
   0x36dfdfffff987e0L, 0x1c00005e000000L, 0x23edfdfffffbafe0L, 0x100000000L
};
static final long[] jjbitVec13 = {
   0x23cdfdfffff99fe0L, 0x3b0000000L, 0x3bfc718d63dc7e0L, 0x0L
};
static final long[] jjbitVec14 = {
   0x3effdfffffddfe0L, 0x300000000L, 0x3effdfffffddfe0L, 0x340000000L
};
static final long[] jjbitVec15 = {
   0x3fffdfffffddfe0L, 0x300000000L, 0x0L, 0x0L
};
static final long[] jjbitVec16 = {
   0xd7ffffffffffeL, 0x3fL, 0x200d6caefef02596L, 0x1fL
};
static final long[] jjbitVec17 = {
   0x0L, 0x3fffffffeffL, 0x0L, 0x0L
};
static final long[] jjbitVec18 = {
   0x0L, 0x0L, 0xffffffff00000000L, 0x7fffffffff003fL
};
static final long[] jjbitVec19 = {
   0x500000000007daedL, 0x2c62ab82315001L, 0xf580c90040000000L, 0x201080000000007L
};
static final long[] jjbitVec20 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffff0fffffffL, 0x3ffffffffffffffL
};
static final long[] jjbitVec21 = {
   0xffffffff3f3fffffL, 0x3fffffffaaff3f3fL, 0x5fdfffffffffffffL, 0x1fdc1fff0fcf1fdcL
};
static final long[] jjbitVec22 = {
   0x4c4000000000L, 0x0L, 0x7L, 0x0L
};
static final long[] jjbitVec23 = {
   0x3fe00000080L, 0xfffffffffffffffeL, 0xfffffffe001fffffL, 0x7ffffffffffffffL
};
static final long[] jjbitVec24 = {
   0x1fffffffffe0L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec25 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0x3fffffffffL, 0x0L
};
static final long[] jjbitVec26 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0xfffffffffL, 0x0L
};
static final long[] jjbitVec27 = {
   0x0L, 0x0L, 0x80000000000000L, 0xff7fffffff7fffffL
};
static final long[] jjbitVec28 = {
   0xffffffL, 0xffffffffffff0000L, 0xf80001ffffffffffL, 0x30003L
};
static final long[] jjbitVec29 = {
   0xffffffffffffffffL, 0x30000003fL, 0xfffffffbffffd7c0L, 0xffffd547f7fffL
};
static final long[] jjbitVec30 = {
   0xffffffffffffdffeL, 0xffffffffdffeffffL, 0xffffffffffff007bL, 0x33fcfffffff199fL
};
static final long[] jjbitVec31 = {
   0xfffe000000000000L, 0xfffffffe027fffffL, 0xbbfffffbfffe007fL, 0x707ffffff0016L
};
static final long[] jjbitVec32 = {
   0x7fffffe00000000L, 0xffff03ff0007ffffL, 0x7cffffffffffffffL, 0x3ff3dffffef7fffL
};
static final long[] jjbitVec33 = {
   0xf3ffffffffffffeeL, 0xffcfff1e3fffL, 0xd3c5fdfffff99feeL, 0x3ffcfb080399fL
};
static final long[] jjbitVec34 = {
   0xd36dfdfffff987e4L, 0x1fffc05e003987L, 0xf3edfdfffffbafeeL, 0xffc100003bbfL
};
static final long[] jjbitVec35 = {
   0xf3cdfdfffff99feeL, 0xffc3b0c0398fL, 0xc3bfc718d63dc7ecL, 0xff8000803dc7L
};
static final long[] jjbitVec36 = {
   0xc3effdfffffddfeeL, 0xffc300603ddfL, 0xc3effdfffffddfecL, 0xffc340603ddfL
};
static final long[] jjbitVec37 = {
   0xc3fffdfffffddfecL, 0xffc300803dcfL, 0x0L, 0x0L
};
static final long[] jjbitVec38 = {
   0x7ff7ffffffffffeL, 0x3ff7fffL, 0x3bff6caefef02596L, 0x3ff3f5fL
};
static final long[] jjbitVec39 = {
   0xc2a003ff03000000L, 0xfffe03fffffffeffL, 0x2fe3ffffebf0fdfL, 0x0L
};
static final long[] jjbitVec40 = {
   0x0L, 0x0L, 0x0L, 0x21fff0000L
};
static final long[] jjbitVec41 = {
   0x3efffe000000a0L, 0xfffffffffffffffeL, 0xfffffffe661fffffL, 0x77ffffffffffffffL
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 46;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 23:
                  if (curChar == 36)
                     jjstateSet[jjnewStateCnt++] = 40;
                  break;
               case 13:
                  if (curChar == 47)
                     jjstateSet[jjnewStateCnt++] = 17;
                  else if (curChar == 58)
                  {
                     if (kind > 22)
                        kind = 22;
                     jjCheckNAdd(14);
                  }
                  else if (curChar == 33)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 12:
                  if ((0xefffffb7ffffffffL & l) != 0L)
                  {
                     if (kind > 11)
                        kind = 11;
                     jjCheckNAdd(0);
                  }
                  else if (curChar == 35)
                     jjAddStates(0, 3);
                  else if (curChar == 60)
                     jjCheckNAddTwoStates(10, 16);
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(1, 11);
                  else if (curChar == 60)
                     jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 0:
                  if ((0xefffffb7ffffffffL & l) == 0L)
                     break;
                  if (kind > 11)
                     kind = 11;
                  jjCheckNAdd(0);
                  break;
               case 1:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(1, 11);
                  break;
               case 3:
                  if ((0x100002600L & l) == 0L)
                     break;
                  if (kind > 12)
                     kind = 12;
                  jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 10:
                  if (curChar == 33)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 11:
                  if (curChar == 60)
                     jjCheckNAdd(10);
                  break;
               case 14:
                  if ((0x7ff600000000000L & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAdd(14);
                  break;
               case 15:
                  if (curChar == 60)
                     jjCheckNAddTwoStates(10, 16);
                  break;
               case 16:
                  if (curChar == 47)
                     jjstateSet[jjnewStateCnt++] = 17;
                  break;
               case 17:
                  if (curChar != 58)
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjCheckNAdd(18);
                  break;
               case 18:
                  if ((0x7ff600000000000L & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjCheckNAdd(18);
                  break;
               case 19:
                  if (curChar == 35)
                     jjAddStates(0, 3);
                  break;
               case 21:
                  if ((0x100002600L & l) != 0L)
                     jjAddStates(4, 5);
                  break;
               case 25:
                  if ((0x100002600L & l) != 0L)
                     jjAddStates(6, 7);
                  break;
               case 31:
                  if ((0x100002600L & l) != 0L)
                     jjAddStates(8, 9);
                  break;
               case 40:
                  if (curChar == 58)
                     jjCheckNAddStates(10, 12);
                  break;
               case 41:
                  if ((0x7ff600000000000L & l) != 0L)
                     jjCheckNAddStates(10, 12);
                  break;
               case 42:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(42, 43);
                  break;
               case 43:
                  if (curChar == 61)
                     jjCheckNAddTwoStates(44, 45);
                  break;
               case 44:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(44, 45);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 23:
                  if (curChar == 102)
                     jjstateSet[jjnewStateCnt++] = 37;
                  else if (curChar == 101)
                     jjstateSet[jjnewStateCnt++] = 28;
                  else if (curChar == 105)
                     jjstateSet[jjnewStateCnt++] = 20;
                  break;
               case 13:
               case 14:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAdd(14);
                  break;
               case 12:
               case 0:
                  if (kind > 11)
                     kind = 11;
                  jjCheckNAdd(0);
                  break;
               case 2:
                  if (curChar == 69)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 4:
                  if (curChar == 80)
                     jjstateSet[jjnewStateCnt++] = 2;
                  break;
               case 5:
                  if (curChar == 89)
                     jjstateSet[jjnewStateCnt++] = 4;
                  break;
               case 6:
                  if (curChar == 84)
                     jjstateSet[jjnewStateCnt++] = 5;
                  break;
               case 7:
                  if (curChar == 67)
                     jjstateSet[jjnewStateCnt++] = 6;
                  break;
               case 8:
                  if (curChar == 79)
                     jjstateSet[jjnewStateCnt++] = 7;
                  break;
               case 9:
                  if (curChar == 68)
                     jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 17:
               case 18:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjCheckNAdd(18);
                  break;
               case 20:
                  if (curChar == 102)
                     jjAddStates(4, 5);
                  break;
               case 22:
                  if (curChar == 123 && kind > 52)
                     kind = 52;
                  break;
               case 24:
                  if (curChar == 102)
                     jjAddStates(6, 7);
                  break;
               case 26:
                  if (curChar == 123 && kind > 53)
                     kind = 53;
                  break;
               case 27:
                  if (curChar == 105)
                     jjstateSet[jjnewStateCnt++] = 24;
                  break;
               case 28:
                  if (curChar == 108)
                     jjstateSet[jjnewStateCnt++] = 27;
                  break;
               case 29:
                  if (curChar == 101)
                     jjstateSet[jjnewStateCnt++] = 28;
                  break;
               case 30:
                  if (curChar == 104)
                     jjAddStates(8, 9);
                  break;
               case 32:
                  if (curChar == 123 && kind > 55)
                     kind = 55;
                  break;
               case 33:
                  if (curChar == 99)
                     jjstateSet[jjnewStateCnt++] = 30;
                  break;
               case 34:
                  if (curChar == 97)
                     jjstateSet[jjnewStateCnt++] = 33;
                  break;
               case 35:
                  if (curChar == 101)
                     jjstateSet[jjnewStateCnt++] = 34;
                  break;
               case 36:
                  if (curChar == 114)
                     jjstateSet[jjnewStateCnt++] = 35;
                  break;
               case 37:
                  if (curChar == 111)
                     jjstateSet[jjnewStateCnt++] = 36;
                  break;
               case 38:
                  if (curChar == 102)
                     jjstateSet[jjnewStateCnt++] = 37;
                  break;
               case 40:
               case 41:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddStates(10, 12);
                  break;
               case 45:
                  if (curChar == 123 && kind > 57)
                     kind = 57;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 13:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAdd(14);
                  break;
               case 12:
               case 0:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 11)
                     kind = 11;
                  jjCheckNAdd(0);
                  break;
               case 14:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAdd(14);
                  break;
               case 17:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjCheckNAdd(18);
                  break;
               case 18:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 23)
                     kind = 23;
                  jjCheckNAdd(18);
                  break;
               case 40:
                  if (jjCanMove_1(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(10, 12);
                  break;
               case 41:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(10, 12);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 46 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_9(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x40000000000L) != 0L)
            return 2;
         return -1;
      case 1:
         if ((active0 & 0x40000000000L) != 0L)
            return 3;
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_9(int pos, long active0)
{
   return jjMoveNfa_9(jjStopStringLiteralDfa_9(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_9(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_9(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_9()
{
   switch(curChar)
   {
      case 93:
         return jjMoveStringLiteralDfa1_9(0x40000000000L);
      default :
         return jjMoveNfa_9(5, 0);
   }
}
private final int jjMoveStringLiteralDfa1_9(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_9(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 93:
         return jjMoveStringLiteralDfa2_9(active0, 0x40000000000L);
      default :
         break;
   }
   return jjStartNfa_9(0, active0);
}
private final int jjMoveStringLiteralDfa2_9(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_9(0, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_9(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 62:
         if ((active0 & 0x40000000000L) != 0L)
            return jjStopAtPos(2, 42);
         break;
      default :
         break;
   }
   return jjStartNfa_9(1, active0);
}
private final int jjMoveNfa_9(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 5;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 5:
               case 0:
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddStates(13, 15);
                  break;
               case 2:
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddStates(13, 15);
                  break;
               case 3:
                  if ((0xbfffffffffffffffL & l) == 0L)
                     break;
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddStates(13, 15);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 5:
                  if ((0xffffffffdfffffffL & l) != 0L)
                  {
                     if (kind > 43)
                        kind = 43;
                     jjCheckNAddStates(13, 15);
                  }
                  else if (curChar == 93)
                     jjstateSet[jjnewStateCnt++] = 2;
                  if (curChar == 93)
                     jjCheckNAdd(0);
                  break;
               case 2:
                  if ((0xffffffffdfffffffL & l) != 0L)
                  {
                     if (kind > 43)
                        kind = 43;
                     jjCheckNAddStates(13, 15);
                  }
                  else if (curChar == 93)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 0:
                  if ((0xffffffffdfffffffL & l) == 0L)
                     break;
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddStates(13, 15);
                  break;
               case 1:
                  if (curChar == 93)
                     jjCheckNAdd(0);
                  break;
               case 3:
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddStates(13, 15);
                  break;
               case 4:
                  if (curChar == 93)
                     jjstateSet[jjnewStateCnt++] = 2;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 5:
               case 0:
               case 3:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddStates(13, 15);
                  break;
               case 2:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddStates(13, 15);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 5 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_2(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_2(int pos, long active0)
{
   return jjMoveNfa_2(jjStopStringLiteralDfa_2(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_2(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_2(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_2()
{
   switch(curChar)
   {
      case 34:
         return jjStopAtPos(0, 18);
      case 39:
         return jjStopAtPos(0, 19);
      default :
         return jjMoveNfa_2(0, 0);
   }
}
private final int jjMoveNfa_2(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 1;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xffffff7bffffffffL & l) == 0L)
                     break;
                  kind = 20;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  kind = 20;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_3(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_3(int pos, long active0)
{
   return jjMoveNfa_3(jjStopStringLiteralDfa_3(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_3(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_3(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_3()
{
   switch(curChar)
   {
      case 34:
         return jjStopAtPos(0, 18);
      case 39:
         return jjStopAtPos(0, 19);
      default :
         return jjMoveNfa_3(0, 0);
   }
}
private final int jjMoveNfa_3(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 1;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xafffff3b00002400L & l) == 0L)
                     break;
                  kind = 21;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x7fffffe87ffffffL & l) == 0L)
                     break;
                  kind = 21;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_10(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_10(int pos, long active0)
{
   return jjMoveNfa_10(jjStopStringLiteralDfa_10(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_10(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_10(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_10()
{
   switch(curChar)
   {
      case 63:
         return jjMoveStringLiteralDfa1_10(0x200000000000L);
      default :
         return jjMoveNfa_10(1, 0);
   }
}
private final int jjMoveStringLiteralDfa1_10(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_10(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 62:
         if ((active0 & 0x200000000000L) != 0L)
            return jjStopAtPos(1, 45);
         break;
      default :
         break;
   }
   return jjStartNfa_10(0, active0);
}
private final int jjMoveNfa_10(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if ((0x100002600L & l) != 0L)
                  {
                     if (kind > 46)
                        kind = 46;
                     jjCheckNAdd(0);
                  }
                  else if (curChar == 58)
                  {
                     if (kind > 47)
                        kind = 47;
                     jjCheckNAdd(2);
                  }
                  break;
               case 0:
                  if ((0x100002600L & l) == 0L)
                     break;
                  kind = 46;
                  jjCheckNAdd(0);
                  break;
               case 2:
                  if ((0x7ff600000000000L & l) == 0L)
                     break;
                  if (kind > 47)
                     kind = 47;
                  jjCheckNAdd(2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
               case 2:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 47)
                     kind = 47;
                  jjCheckNAdd(2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 47)
                     kind = 47;
                  jjCheckNAdd(2);
                  break;
               case 2:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 47)
                     kind = 47;
                  jjCheckNAdd(2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_12(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_12(int pos, long active0)
{
   return jjMoveNfa_12(jjStopStringLiteralDfa_12(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_12(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_12(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_12()
{
   switch(curChar)
   {
      case 125:
         return jjStopAtPos(0, 50);
      default :
         return jjMoveNfa_12(0, 0);
   }
}
private final int jjMoveNfa_12(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 1;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  kind = 51;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xd7ffffffffffffffL & l) == 0L)
                     break;
                  kind = 51;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 51)
                     kind = 51;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjMoveStringLiteralDfa0_4()
{
   return jjMoveNfa_4(3, 0);
}
private final int jjMoveNfa_4(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 9;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 3:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddStates(16, 21);
                  else if (curChar == 47)
                     jjstateSet[jjnewStateCnt++] = 1;
                  else if (curChar == 62)
                  {
                     if (kind > 24)
                        kind = 24;
                  }
                  break;
               case 0:
                  if (curChar == 62 && kind > 24)
                     kind = 24;
                  break;
               case 1:
                  if (curChar == 62 && kind > 25)
                     kind = 25;
                  break;
               case 2:
                  if (curChar == 47)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 4:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(4, 0);
                  break;
               case 5:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(5, 2);
                  break;
               case 6:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(6, 7);
                  break;
               case 7:
                  if (curChar != 58)
                     break;
                  if (kind > 26)
                     kind = 26;
                  jjCheckNAdd(8);
                  break;
               case 8:
                  if ((0x7ff600000000000L & l) == 0L)
                     break;
                  if (kind > 26)
                     kind = 26;
                  jjCheckNAdd(8);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 7:
               case 8:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 26)
                     kind = 26;
                  jjCheckNAdd(8);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 7:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 26)
                     kind = 26;
                  jjCheckNAdd(8);
                  break;
               case 8:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 26)
                     kind = 26;
                  jjCheckNAdd(8);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 9 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_8(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x8000000000L) != 0L)
            return 0;
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_8(int pos, long active0)
{
   return jjMoveNfa_8(jjStopStringLiteralDfa_8(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_8(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_8(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_8()
{
   switch(curChar)
   {
      case 45:
         return jjMoveStringLiteralDfa1_8(0x8000000000L);
      default :
         return jjMoveNfa_8(2, 0);
   }
}
private final int jjMoveStringLiteralDfa1_8(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_8(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 45:
         return jjMoveStringLiteralDfa2_8(active0, 0x8000000000L);
      default :
         break;
   }
   return jjStartNfa_8(0, active0);
}
private final int jjMoveStringLiteralDfa2_8(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_8(0, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_8(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 62:
         if ((active0 & 0x8000000000L) != 0L)
            return jjStopAtPos(2, 39);
         break;
      default :
         break;
   }
   return jjStartNfa_8(1, active0);
}
private final int jjMoveNfa_8(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 2;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 2:
                  if ((0xffffdfffffffffffL & l) != 0L)
                  {
                     if (kind > 40)
                        kind = 40;
                     jjCheckNAddTwoStates(0, 1);
                  }
                  else if (curChar == 45)
                     jjCheckNAdd(0);
                  break;
               case 0:
                  if ((0xffffdfffffffffffL & l) == 0L)
                     break;
                  kind = 40;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               case 1:
                  if (curChar == 45)
                     jjCheckNAdd(0);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 2:
               case 0:
                  kind = 40;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 2:
               case 0:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 40)
                     kind = 40;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 2 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_7(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_7(int pos, long active0)
{
   return jjMoveNfa_7(jjStopStringLiteralDfa_7(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_7(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_7(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_7()
{
   switch(curChar)
   {
      case 59:
         return jjStopAtPos(0, 34);
      default :
         return jjMoveNfa_7(0, 0);
   }
}
private final int jjMoveNfa_7(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 7;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (curChar == 58)
                  {
                     if (kind > 37)
                        kind = 37;
                     jjCheckNAdd(6);
                  }
                  else if (curChar == 35)
                     jjstateSet[jjnewStateCnt++] = 2;
                  if (curChar == 35)
                     jjCheckNAdd(1);
                  break;
               case 1:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 35)
                     kind = 35;
                  jjCheckNAdd(1);
                  break;
               case 3:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 36)
                     kind = 36;
                  jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 4:
                  if (curChar == 35)
                     jjstateSet[jjnewStateCnt++] = 2;
                  break;
               case 5:
                  if (curChar != 58)
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(6);
                  break;
               case 6:
                  if ((0x7ff600000000000L & l) == 0L)
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(6);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 6:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(6);
                  break;
               case 2:
                  if (curChar == 120)
                     jjCheckNAdd(3);
                  break;
               case 3:
                  if ((0x7e0000007eL & l) == 0L)
                     break;
                  if (kind > 36)
                     kind = 36;
                  jjCheckNAdd(3);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(6);
                  break;
               case 6:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(6);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 7 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_1(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_1(int pos, long active0)
{
   return jjMoveNfa_1(jjStopStringLiteralDfa_1(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_1(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_1(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_1()
{
   switch(curChar)
   {
      case 34:
         return jjStopAtPos(0, 18);
      case 39:
         return jjStopAtPos(0, 19);
      default :
         return jjMoveNfa_1(1, 0);
   }
}
private final int jjMoveNfa_1(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 20;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if ((0x100002600L & l) != 0L)
                  {
                     if (kind > 17)
                        kind = 17;
                     jjCheckNAddStates(22, 28);
                  }
                  else if (curChar == 58)
                  {
                     if (kind > 14)
                        kind = 14;
                     jjCheckNAdd(2);
                  }
                  else if (curChar == 62)
                  {
                     if (kind > 13)
                        kind = 13;
                  }
                  break;
               case 0:
                  if (curChar == 62)
                     kind = 13;
                  break;
               case 2:
                  if ((0x7ff600000000000L & l) == 0L)
                     break;
                  if (kind > 14)
                     kind = 14;
                  jjCheckNAdd(2);
                  break;
               case 3:
                  if ((0x100002600L & l) == 0L)
                     break;
                  if (kind > 17)
                     kind = 17;
                  jjCheckNAddStates(22, 28);
                  break;
               case 4:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(4, 0);
                  break;
               case 5:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(5, 11);
                  break;
               case 12:
                  if ((0x100002600L & l) != 0L)
                     jjCheckNAddTwoStates(12, 18);
                  break;
               case 19:
                  if ((0x100002600L & l) == 0L)
                     break;
                  if (kind > 17)
                     kind = 17;
                  jjCheckNAdd(19);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
               case 2:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 14)
                     kind = 14;
                  jjCheckNAdd(2);
                  break;
               case 6:
                  if (curChar == 77 && kind > 15)
                     kind = 15;
                  break;
               case 7:
                  if (curChar == 69)
                     jjstateSet[jjnewStateCnt++] = 6;
                  break;
               case 8:
                  if (curChar == 84)
                     jjstateSet[jjnewStateCnt++] = 7;
                  break;
               case 9:
                  if (curChar == 83)
                     jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 10:
                  if (curChar == 89)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 11:
                  if (curChar == 83)
                     jjstateSet[jjnewStateCnt++] = 10;
                  break;
               case 13:
                  if (curChar == 67 && kind > 16)
                     kind = 16;
                  break;
               case 14:
                  if (curChar == 73)
                     jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 15:
                  if (curChar == 76)
                     jjstateSet[jjnewStateCnt++] = 14;
                  break;
               case 16:
                  if (curChar == 66)
                     jjstateSet[jjnewStateCnt++] = 15;
                  break;
               case 17:
                  if (curChar == 85)
                     jjstateSet[jjnewStateCnt++] = 16;
                  break;
               case 18:
                  if (curChar == 80)
                     jjstateSet[jjnewStateCnt++] = 17;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 14)
                     kind = 14;
                  jjCheckNAdd(2);
                  break;
               case 2:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 14)
                     kind = 14;
                  jjCheckNAdd(2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 20 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjMoveStringLiteralDfa0_11()
{
   return jjMoveNfa_11(3, 0);
}
private final int jjMoveNfa_11(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 3:
                  if ((0x7fffffffffffffffL & l) != 0L)
                  {
                     if (kind > 48)
                        kind = 48;
                     jjCheckNAddTwoStates(0, 1);
                  }
                  else if (curChar == 63)
                     jjstateSet[jjnewStateCnt++] = 2;
                  break;
               case 0:
                  if ((0x7fffffffffffffffL & l) == 0L)
                     break;
                  if (kind > 48)
                     kind = 48;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               case 1:
                  if (curChar == 63)
                     jjstateSet[jjnewStateCnt++] = 2;
                  break;
               case 2:
                  if ((0xbfffffffffffffffL & l) == 0L)
                     break;
                  if (kind > 48)
                     kind = 48;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 3:
               case 0:
               case 2:
                  if (kind > 48)
                     kind = 48;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 3:
               case 0:
               case 2:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 48)
                     kind = 48;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   23, 29, 38, 39, 21, 22, 25, 26, 31, 32, 41, 42, 43, 0, 1, 4, 
   4, 0, 5, 2, 6, 7, 4, 0, 5, 11, 12, 18, 19, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default : 
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec4[i2] & l2) != 0L);
      case 1:
         return ((jjbitVec5[i2] & l2) != 0L);
      case 2:
         return ((jjbitVec6[i2] & l2) != 0L);
      case 3:
         return ((jjbitVec7[i2] & l2) != 0L);
      case 4:
         return ((jjbitVec8[i2] & l2) != 0L);
      case 5:
         return ((jjbitVec9[i2] & l2) != 0L);
      case 6:
         return ((jjbitVec10[i2] & l2) != 0L);
      case 9:
         return ((jjbitVec11[i2] & l2) != 0L);
      case 10:
         return ((jjbitVec12[i2] & l2) != 0L);
      case 11:
         return ((jjbitVec13[i2] & l2) != 0L);
      case 12:
         return ((jjbitVec14[i2] & l2) != 0L);
      case 13:
         return ((jjbitVec15[i2] & l2) != 0L);
      case 14:
         return ((jjbitVec16[i2] & l2) != 0L);
      case 15:
         return ((jjbitVec17[i2] & l2) != 0L);
      case 16:
         return ((jjbitVec18[i2] & l2) != 0L);
      case 17:
         return ((jjbitVec19[i2] & l2) != 0L);
      case 30:
         return ((jjbitVec20[i2] & l2) != 0L);
      case 31:
         return ((jjbitVec21[i2] & l2) != 0L);
      case 33:
         return ((jjbitVec22[i2] & l2) != 0L);
      case 48:
         return ((jjbitVec23[i2] & l2) != 0L);
      case 49:
         return ((jjbitVec24[i2] & l2) != 0L);
      case 159:
         return ((jjbitVec25[i2] & l2) != 0L);
      case 215:
         return ((jjbitVec26[i2] & l2) != 0L);
      default : 
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_2(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec27[i2] & l2) != 0L);
      case 1:
         return ((jjbitVec5[i2] & l2) != 0L);
      case 2:
         return ((jjbitVec28[i2] & l2) != 0L);
      case 3:
         return ((jjbitVec29[i2] & l2) != 0L);
      case 4:
         return ((jjbitVec30[i2] & l2) != 0L);
      case 5:
         return ((jjbitVec31[i2] & l2) != 0L);
      case 6:
         return ((jjbitVec32[i2] & l2) != 0L);
      case 9:
         return ((jjbitVec33[i2] & l2) != 0L);
      case 10:
         return ((jjbitVec34[i2] & l2) != 0L);
      case 11:
         return ((jjbitVec35[i2] & l2) != 0L);
      case 12:
         return ((jjbitVec36[i2] & l2) != 0L);
      case 13:
         return ((jjbitVec37[i2] & l2) != 0L);
      case 14:
         return ((jjbitVec38[i2] & l2) != 0L);
      case 15:
         return ((jjbitVec39[i2] & l2) != 0L);
      case 16:
         return ((jjbitVec18[i2] & l2) != 0L);
      case 17:
         return ((jjbitVec19[i2] & l2) != 0L);
      case 30:
         return ((jjbitVec20[i2] & l2) != 0L);
      case 31:
         return ((jjbitVec21[i2] & l2) != 0L);
      case 32:
         return ((jjbitVec40[i2] & l2) != 0L);
      case 33:
         return ((jjbitVec22[i2] & l2) != 0L);
      case 48:
         return ((jjbitVec41[i2] & l2) != 0L);
      case 49:
         return ((jjbitVec24[i2] & l2) != 0L);
      case 159:
         return ((jjbitVec25[i2] & l2) != 0L);
      case 215:
         return ((jjbitVec26[i2] & l2) != 0L);
      default : 
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, "\42", "\47", null, null, null, null, null, null, null, 
null, "\42", "\47", "\173", "\46", null, "\46", "\73", null, null, null, 
"\74\41\55\55", "\55\55\76", null, "\74\133\103\104\101\124\101\133", "\135\135\76", null, 
"\74\77", "\77\76", null, null, null, "\43\173", "\175", null, null, null, 
"\43\145\154\163\145", null, "\43\145\156\144", null, };
public static final String[] lexStateNames = {
   "DEFAULT", 
   "DOCTYPE", 
   "DOCTYPE_SYSTEM", 
   "DOCTYPE_PUBLIC", 
   "ELEMENT", 
   "ATTRIBUTE", 
   "ATTRIBUTE_DATA", 
   "ENTITYREF", 
   "COMMENT", 
   "CDATA", 
   "PROCINSTR", 
   "PROCINSTR_DATA", 
   "EXPRESSION", 
};
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, 4, 4, 0, 
   0, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 0, -1, 9, 0, -1, 10, 0, 11, -1, 10, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, 
};
protected JavaCharStream input_stream;
private final int[] jjrounds = new int[46];
private final int[] jjstateSet = new int[92];
protected char curChar;
public ParserTokenManager(JavaCharStream stream)
{
   if (JavaCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public ParserTokenManager(JavaCharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(JavaCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 46; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(JavaCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 13 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
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

public Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   switch(curLexState)
   {
     case 0:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_0();
       break;
     case 1:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_1();
       break;
     case 2:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_2();
       break;
     case 3:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_3();
       break;
     case 4:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_4();
       break;
     case 5:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_5();
       break;
     case 6:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_6();
       break;
     case 7:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_7();
       break;
     case 8:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_8();
       break;
     case 9:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_9();
       break;
     case 10:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_10();
       break;
     case 11:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_11();
       break;
     case 12:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_12();
       break;
   }
     if (jjmatchedKind != 0x7fffffff)
     {
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
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}
