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
package org.apache.cocoon.components.midi.xmidi;

/**
 * The MIDI file parsing parts of this class are based on code from the XMidi project, written
 * by  Peter Arthur Loeb (http://www.palserv.com/XMidi/) and used with permission.
 * The warranty disclaimer of the MIT license (http://www.opensource.org/licenses/mit-license.html)
 * applies to Peter Arthur Loeb's code.
 *
 * @author <a href="mailto:mark.leicester@energyintellect.com">Mark Leicester</a>
 * @author <a href="mailto:peter@palserv.com">Peter Loeb</a>
 */

import org.apache.cocoon.ProcessingException;

public final class Utils
{
	public static final String VERSION = "1.2";

  /**
  convert hex string to byte array
  */
  static public byte[] hexToBa(String h, int len) throws ProcessingException
  {
    int l = h.length();
    int cnt = 0;
    int bs = 0;
    int bc = 0;
    byte[] ba = new byte[len];
    for (int i = 0; i < l; i++)
    {
      String s = h.substring(i, i + 1);
      int x = " \n\t\r".indexOf(s);
      if (x != -1)
      {
        continue;
      }
      int tmp = "0123456789ABCDEF".indexOf(s.toUpperCase());
      if (bc == 0)
      {
        bs = tmp;
      }
      else
        if (bc == 1)
        {
          bs <<= 4;
          bs |= tmp;
        }
        else
        {
          throw new ProcessingException("hexToBa: internal error");
        }
      bc++;
      if (bc >= 2)
      {
        ba[cnt] = (byte) bs;
        cnt++;
        bc = 0;
      }
    }
    if (bc != 0)
    {
      throw new ProcessingException("un-even number of hex digits");
    }
    if (cnt != len)
    {
      throw new ProcessingException(
        "hexToBa: count (" + cnt + ") != length (" + len + ")");
    }
    return ba;
  }

  /**
  convert byte array to string (not hex)
  */
  public static String baToString(byte[] b, int strt, int end)
  {
    int l = end - strt + 1;
    char[] c = new char[l];
    for (int j = 0; j < l; j++)
    {
      c[j] = (char) b[j];
    }
    return new String(c);
  }

  /**
  convert byte array to hex string
  */
  public static String baToHex(byte[] b, int strt, int end)
    throws ProcessingException
  {
    int l = end - strt + 1;
    if (b.length < end)
    {
      throw new ProcessingException(
        "baToHex: length error; b.length="
          + b.length
          + ", strt="
          + strt
          + ", end="
          + end
          + ", l="
          + l);
    }
    StringBuffer sb = new StringBuffer("");
    for (int i = 0; i < l; i++)
    {
      int t = getUnsignedByte(b[strt + i]);
      int a = t / 16;
      int aa = t % 16;
      sb.append("0123456789ABCDEF".substring(a, a + 1));
      sb.append("0123456789ABCDEF".substring(aa, aa + 1));
    }
    return sb.toString();
  }

  /**
  convert int to byte array of length c
  */
  static public byte[] intToBa(int n, int c) throws ProcessingException
  {
    byte[] b = new byte[c];
    int t = n;
    for (int i = 0; i < c; i++)
    {
      int j = c - i - 1;
      int k = t % 256;
      b[j] = (byte) k;
      t /= 256;
    }
    if (t > 0)
    {
      throw new ProcessingException(
        "intToBa: t is " + t + ", n = " + n + ", c = " + c);
    }
    return b;
  }

  /**
  convert byte array to int
  */
  static public int baToInt(byte[] b, int strt, int end)
    throws ProcessingException
  {
    if (end > b.length - 1)
    {
      throw new ProcessingException(
        "baToInt: strt = "
          + strt
          + ", end = "
          + end
          + ", b.length = "
          + b.length);
    }
    int l = end - strt + 1;
    int i = 0;
    for (int j = 0; j < l; j++)
    {
      int p = strt + l - j - 1;
      // get int value of unsigned byte into k
      if (p > b.length - 1)
      {
        throw new ProcessingException(
          "baToInt: p = "
            + p
            + ", strt = "
            + strt
            + ", end = "
            + end
            + ", l = "
            + l
            + ", j = "
            + j
            + ", i = "
            + i);
      }
      int k = getUnsignedByte(b[p]);
      int n = pow(256, j);
      i += n * k;
    }
    return i;
  }

  /**
  convert byte (unsigned) to int
  */
  static public int getUnsignedByte(byte b)
  {
    int t = 0;
    if ((b & 128) == 128)
    {
      t = 1;
    }
    b &= 127;
    int k = b;
    k += t * 128;
    return k;
  }

  /**
  convert delta time to byte array
  a delta time is expressed as a
  byte array, length unknown (4 or less)
  */
  static public ByteLen deltaToInt(byte[] b, int offset)
    throws ProcessingException
  {
    /*
    - capture up to four bytes including first with hi-ord
    	bit off
    - turn off hi-ord bits
    - accumulate 4 groups of 7 into 28 bit number with
    	hi-ord 4 bits zero.
    */
    int j = 0;
    byte[] ba = new byte[4];
    boolean jFlag = true;
    while (jFlag)
    {
      if ((j + offset) > b.length)
      {
        throw new ProcessingException(
          "deltaToInt: length error; j = "
            + j
            + ", offset = "
            + offset
            + ", b.length = "
            + b.length);
      }
      ba[j] = b[j + offset];
      if (ba[j] >= 0)
      {
        jFlag = false;
      }
      else
      {
        ba[j] &= 127;
      }
      /*this.getLogger().debug(
      	"deltaToInt: j = " + j + ", ba = " + baToInt(ba, 0, j));*/
      j++;
    }
    int s = 0;
    for (int i = 0; i < j; i++)
    {
      int k = j - i - 1;
      int p = pow(128, i);
      int m = ba[k];
      s += m * p;
      /*this.getLogger().debug(
      	"deltaToInt: in loop: s = "
      		+ s
      		+ ", i = "
      		+ i
      		+ ", j = "
      		+ j
      		+ ", k = "
      		+ k
      		+ ", p = "
      		+ p
      		+ ", m = "
      		+ m);*/
    }
    /*this.getLogger().debug("deltaToInt: s = " + s);*/
    ByteLen bl = new ByteLen(intToBa(s, 4), j);
    return bl;
  }

  /**
  compute b to the e power (b ** e)
  */
  static public int pow(int b, int e)
  {
    int a = 1;
    for (int i = 0; i < e; i++)
    {
      a *= b;
    }
    return a;
  }

  public static int getPW(byte[] dta, int offset) throws ProcessingException
  {
    int hi = baToInt(dta, offset, offset);
    int lo = baToInt(dta, offset + 1, offset + 1);
    hi <<= 7;
    lo |= hi;
    return lo;
  }

  public static int getNextHiOrd(byte[] dta, int offset)
    throws ProcessingException
  {
    int ol = 0;
    boolean tflag = true;
    for (int o = offset + 1; o < dta.length - 1; o++)
    {
      if (tflag)
      {
        int x = baToInt(dta, o, o);
        if ((x & 128) == 128)
        {
          tflag = false;
          ol = o;
        }
      }
    }
    if (tflag)
    {
      ol = offset + dta.length;
    }
    return ol;
  }

  /**
  convert byte array to delta time
  a delta time is expressed as a
  byte array, length unknown (4 or less)
  */
  public static byte[] intToDelta(byte[] t) throws ProcessingException
  {
    /*
    from fullword binary value to variable (midi) value:
    - split number into 5 bit groups of 4 7 7 7 7
    - just deal with the four groups of 7 (ignore the 4)
    - put each group of 7 into a byte
    - case 1: whole full word is zero.
    	return one byte of zero.
    - case 2: some non-zero bit (after first four bits)
    	- work from left to right
    	- throw away bytes which are zero until non-zero byte
    		encountered
    	- turn on hi-ord bit on all but last byte
    */
    int i1 = baToInt(t, 0, t.length - 1);
    //this.getLogger().debug("intToDelta: i1 = " + i1 + ", t = " + XMidiUtils.baToHex(t, 0, t.length - 1));
    if (i1 == 0)
    {
      byte[] b = new byte[1];
      b[0] = 0;
      return b;
    }
    int i2 = i1;
    byte[] b1 = new byte[4];
    for (int i = 0; i < 4; i++)
    {
      int j = 4 - i - 1;
      int k = i2 % 128;
      i2 /= 128;
      b1[j] = (byte) k;
    }
    int j = -1;
    boolean bFlag = false;
    for (int i = 0; i < 4; i++)
    {
      if (bFlag)
      {
        continue;
      }
      if (b1[i] != 0)
      {
        j = i;
        bFlag = true;
      }
    }
    int k = 4 - j;
    byte[] b2 = new byte[k];
    for (int i = 0; i < k; i++)
    {
      b2[i] = b1[i + j];
      if (i != (k - 1))
      {
        b2[i] |= 128;
      }
    }
    return b2;
  }

	public static int stringToInt(String s)
  {
    Integer i = new Integer(s);
    int r = i.intValue();
    return r;
  }

}
