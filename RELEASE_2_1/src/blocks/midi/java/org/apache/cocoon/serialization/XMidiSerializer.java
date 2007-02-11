/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
package org.apache.cocoon.serialization;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.midi.xmidi.Constants;
import org.apache.cocoon.components.midi.xmidi.Utils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Takes SAX Events and serializes them as a standard MIDI file.
 *
 * The MIDI file generation parts of this class are based on code from the XMidi project, written
 * by  Peter Arthur Loeb (http://www.palserv.com/XMidi/) and used with permission.
 * The warranty disclaimer of the MIT license (http://www.opensource.org/licenses/mit-license.html)
 * applies to Peter Arthur Loeb's code.
 *
 * @author <a href="mailto:mark.leicester@energyintellect.com">Mark Leicester</a>
 * @author <a href="mailto:peter@palserv.com">Peter Loeb</a>
 */

public class XMidiSerializer extends AbstractSerializer 
{
	private final static String mimeType = "audio/x-midi";

  private final int OUTSIDE_XMIDI = 0;
  private final int INSIDE_XMIDI = 1;
  private final int INSIDE_CHUNK = 2;
  private final int INSIDE_MTHD = 3;
  private final int INSIDE_MTRK = 4;
  private final int INSIDE_DELTA = 5;
  private final int INSIDE_STATUS = 6;
  private final int INSIDE_DELTA_CHANNEL = 7;
  private final int INSIDE_STATUS_CHANNEL = 8;

  private int expectedBytes;
  private boolean preventDataWrite;
  private int state;
  private StringBuffer buffer;
  private boolean buffering = false;

  /* (non-Javadoc)
  * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
  */
  public void recycle()
  {
    preventDataWrite = false;
    state = OUTSIDE_XMIDI;
    super.recycle();
  }

	/* (non-Javadoc)
	 * @see org.apache.cocoon.sitemap.SitemapOutputComponent#getMimeType()
	 */
	public String getMimeType()
	{
		return(mimeType);
	}

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(
    String namespaceURI,
    String localName,
    String qName,
    Attributes atts)
    throws SAXException
  {
    try
    {
      if (localName.equals("XMidi"))
      {
        if (state != OUTSIDE_XMIDI)
        {
          throw new SAXException("XMidi element not expected here");
        }
        state = INSIDE_XMIDI;
        String version = atts.getValue("VERSION");
        if (version == null)
        {
          throw new SAXException("XMidi element has no version attribute");
        }
        else
          if (!version.equals(Constants.VERSION))
          {
            throw new SAXException(
              "XMidi element has wrong version: expecting "
                + Constants.VERSION
                + ", got "
                + version);
          }
        this.getLogger().debug("Found XMidi element, version " + version);
      }
      else
        if (localName.equals("CHUNK"))
        {
          if (state != INSIDE_XMIDI)
          {
            throw new SAXException(
              localName + " element not expected here, state = " + state);
          }
          state = INSIDE_CHUNK;
          writeString(atts.getValue("TYPE"), 4);
          Integer iLen = new Integer(atts.getValue("LENGTH"));
          writeFullWord(iLen.intValue());
          this.getLogger().debug(
            "chunk type is: " + atts.getValue("TYPE") + " with length " + atts.getValue("LENGTH"));
        }
        else
          if (localName.equals("MThd"))
          {
            if (state != INSIDE_XMIDI)
            {
              throw new SAXException(
                localName + " element not expected here, state = " + state);
            }
            state = INSIDE_MTHD;
            writeString(atts.getValue("TYPE"), 4);
            writeFullWord(Utils.stringToInt(atts.getValue("LENGTH")));
            this.getLogger().debug("we have MThd chunk; len = " + atts.getValue("LENGTH"));
          }
          else
            if (localName.equals("MTrk"))
            {
              if (state != INSIDE_XMIDI)
              {
                throw new SAXException(
                  localName + " element not expected here, state = " + state);
              }
              state = INSIDE_MTRK;
              writeString(atts.getValue("TYPE"), 4);
              writeFullWord(Utils.stringToInt(atts.getValue("LENGTH")));
              this.getLogger().debug("we have MTrk chunk; len = " + atts.getValue("LENGTH"));
            }

            else
              if (localName.equals("DELTA"))
              {
                if (state != INSIDE_MTRK)
                {
                  throw new SAXException(
                    localName + " element not expected here, state = " + state);
                }
                state = INSIDE_DELTA;
                String dtime = atts.getValue("DTIME");
                byte[] hdt = Utils.hexToBa(dtime, 4);
                byte[] dt = Utils.intToDelta(hdt);
                this.getLogger().debug(
                  "Delta: "
                    + dtime
                    + ", out = "
                    + Utils.baToHex(dt, 0, dt.length - 1));
                this.output.write(dt);

              }
              else
                if (localName.equals("STATUS"))
                {
                  if (state != INSIDE_DELTA)
                  {
                    throw new SAXException(
                      localName
                        + " element not expected here, state = "
                        + state);
                  }
                  state = INSIDE_STATUS;
                  String sval = atts.getValue("SVAL");
                  writeHex(sval, 1);
                  String sl = atts.getValue("SLEN");
                  expectedBytes = Utils.stringToInt(sl);
                  this.getLogger().debug("Status: " + sval + ", len = " + expectedBytes);
                  if (sval.equals("FF"))
                  {
                    String nmd = atts.getValue("SNMT");
                    writeHex(nmd, 1);
                    byte[] hdt = Utils.intToBa(expectedBytes, 4);
                    byte[] xdt = Utils.intToDelta(hdt);
                    this.output.write(xdt);
                    if (expectedBytes == 0)
                    {
                      preventDataWrite = true;
                    }
                    this.getLogger().debug("Non-midi: " + nmd);
                  }
                  else
                    if (sval.equals("F0"))
                    {
                      byte[] hdt = Utils.intToBa(Utils.stringToInt(sl), 4);
                      this.output.write(Utils.intToDelta(hdt));
                      this.getLogger().debug("Sysex");
                    }
                    else
                      if (sval.equals("F6")
                        | sval.equals("F8")
                        | sval.equals("FA")
                        | sval.equals("FB")
                        | sval.equals("FC")
                        | sval.equals("FE"))
                      {
                        preventDataWrite = true;
                        this.getLogger().debug("no data");
                      }
                }
                else
                  if (localName.equals("CHANNEL"))
                  {
                    if ((state != INSIDE_DELTA) && (state != INSIDE_STATUS))
                    {
                      throw new SAXException(
                        localName
                          + " element not expected here, state = "
                          + state);
                    }
                    if (state == INSIDE_DELTA)
                    {
                      state = INSIDE_DELTA_CHANNEL;
                    }
                    else
                      if (state == INSIDE_STATUS)
                      {
                        state = INSIDE_STATUS_CHANNEL;
                      }
                    this.getLogger().debug("Channel");
                  }
                  else
                    if (localName.equals("NOTE_OFF"))
                    {
                      if ((state != INSIDE_DELTA_CHANNEL)
                        && (state != INSIDE_STATUS_CHANNEL))
                      {
                        throw new SAXException(
                          localName
                            + " element not expected here, state = "
                            + state);
                      }
                      String pitch = atts.getValue("PITCH");
                      this.output.write(Utils.stringToInt(pitch));
                      String vel = atts.getValue("VELOCITY");
                      this.output.write(Utils.stringToInt(vel));
                      this.getLogger().debug(
                        "Note off - " + pitch + ", " + vel);
                    }
                    else
                      if (localName.equals("NOTE_ON"))
                      {
                        if ((state != INSIDE_DELTA_CHANNEL)
                          && (state != INSIDE_STATUS_CHANNEL))
                        {
                          throw new SAXException(
                            localName
                              + " element not expected here, state = "
                              + state);
                        }
                        String pitch = atts.getValue("PITCH");
                        this.output.write(Utils.stringToInt(pitch));
                        String vel = atts.getValue("VELOCITY");
                        this.output.write(Utils.stringToInt(vel));
                        this.getLogger().debug(
                          "Note on - " + pitch + ", " + vel);
                      }
                      else
                        if (localName.equals("AFTER"))
                        {
                          if ((state != INSIDE_DELTA_CHANNEL)
                            && (state != INSIDE_STATUS_CHANNEL))
                          {
                            throw new SAXException(
                              localName
                                + " element not expected here, state = "
                                + state);
                          }
                          String pitch = atts.getValue("PITCH");
                          this.output.write(Utils.stringToInt(pitch));
                          String pres = atts.getValue("PRESSURE");
                          this.output.write(Utils.stringToInt(pres));
                          this.getLogger().debug(
                            "AFTER - " + pitch + ", " + pres);
                        }
                        else
                          if (localName.equals("CONTROL"))
                          {
                            if ((state != INSIDE_DELTA_CHANNEL)
                              && (state != INSIDE_STATUS_CHANNEL))
                            {
                              throw new SAXException(
                                localName
                                  + " element not expected here, state = "
                                  + state);
                            }
                            String cnum = atts.getValue("NUMBER");
                            this.output.write(Utils.stringToInt(cnum));
                            String val = atts.getValue("VALUE");
                            this.output.write(Utils.stringToInt(val));
                            this.getLogger().debug(
                              "CONTROL - " + cnum + ", " + val);
                          }
                          else
                            if (localName.equals("PROGRAM"))
                            {
                              if ((state != INSIDE_DELTA_CHANNEL)
                                && (state != INSIDE_STATUS_CHANNEL))
                              {
                                throw new SAXException(
                                  localName
                                    + " element not expected here, state = "
                                    + state);
                              }
                              String patch = atts.getValue("NUMBER");
                              this.output.write(Utils.stringToInt(patch));
                              this.getLogger().debug("PATCH - " + patch);
                            }
                            else
                              if (localName.equals("PRESSURE"))
                              {
                                if ((state != INSIDE_DELTA_CHANNEL)
                                  && (state != INSIDE_STATUS_CHANNEL))
                                {
                                  throw new SAXException(
                                    localName
                                      + " element not expected here, state = "
                                      + state);
                                }

                                String amt = atts.getValue("AMOUNT");
                                this.output.write(Utils.stringToInt(amt));
                                this.getLogger().debug("PRESSURE - " + amt);
                              }
                              else
                                if (localName.equals("WHEEL"))
                                {
                                  if ((state != INSIDE_DELTA_CHANNEL)
                                    && (state != INSIDE_STATUS_CHANNEL))
                                  {
                                    throw new SAXException(
                                      localName
                                        + " element not expected here, state = "
                                        + state);
                                  }

                                  String amt = atts.getValue("AMOUNT");
                                  int a = Utils.stringToInt(amt);
                                  int b = a;
                                  int c = a;
                                  b &= 127;
                                  c >>= 7;
                                  this.output.write(c);
                                  this.output.write(b);
                                  this.getLogger().debug(
                                    "Wheel - " + a + ": (" + c + "," + a + ")");
                                }
                                else
                                  if (localName.equals("EDATA"))
                                  {
                                    if ((state != INSIDE_DELTA)
                                      && (state != INSIDE_STATUS))
                                    {
                                      throw new SAXException(
                                        localName
                                          + " element not expected here, state = "
                                          + state);
                                    }
                                    buffer = new StringBuffer();
                                    buffering = true;
                                    this.getLogger().debug(
                                      "EDATA (element, not text)");
                                  }
                                  else
                                    if (localName.equals("FORMAT")
                                      || localName.equals("TRACKS")
                                      || localName.equals("PPNQ"))
                                    {
                                      if (state != INSIDE_MTHD)
                                      {
                                        throw new SAXException(
                                          localName
                                            + " element not expected here, state = "
                                            + state);
                                      }
                                      buffer = new StringBuffer();
                                      buffering = true;
                                      this.getLogger().debug(
                                        localName + " element");
                                    }
                                    else
                                    {
                                      this.getLogger().debug(
                                        "Found "
                                          + localName
                                          + ", in state "
                                          + state);
                                    }

    }
    catch (ProcessingException e)
    {
      throw new SAXException(e);
    }
    catch (IOException e)
    {
      throw new SAXException(e);
    }
  }

  /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
  public void endElement(String namespaceURI, String localName, String qName)
    throws SAXException
  {
    try
    {
      if (localName.equals("CHUNK"))
      {
        state = INSIDE_XMIDI;
      }
      else
        if (localName.equals("MThd") || localName.equals("MTrk"))
        {
          state = INSIDE_XMIDI;
        }
        else
          if (localName.equals("DELTA"))
          {
            state = INSIDE_MTRK;
          }
          else
            if (localName.equals("STATUS"))
            {
              state = INSIDE_DELTA;
            }
            else
              if (localName.equals("CHANNEL"))
              {
                if (state == INSIDE_STATUS_CHANNEL)
                {
                  state = INSIDE_STATUS;
                }
                else
                  if (state == INSIDE_DELTA_CHANNEL)
                  {
                    state = INSIDE_DELTA;
                  }
              }
              else
                if (localName.equals("EDATA"))
                {
                	if (!preventDataWrite) {
                		writeHex(buffer.toString(), expectedBytes);
                  	this.getLogger().debug("EDATA: " + buffer.toString());
                	} else {
										preventDataWrite = false;
                	}
                  buffering = false;
                }
                else
                  if (localName.equals("FORMAT"))
                  {
                    String typ = buffer.toString();
                    for (int i = 0; i < typ.length(); i++)
                    {
                      if (typ.substring(i, i + 1).compareTo("0")
                        < 0 | typ.substring(i, i + 1).compareTo("9")
                        > 0)
                      {
                        throw new ProcessingException(
                          "Invalid numeric midi format: " + typ);
                      }
                    }
                    int midiFormat = Utils.stringToInt(typ);
                    writeHalfWord(midiFormat);
                    this.getLogger().debug("Format is " + midiFormat);
                    buffering = false;
                  }
                  else
                    if (localName.equals("TRACKS"))
                    {
                      String sNum = buffer.toString();
                      Integer iNum = new Integer(sNum);
                      writeHalfWord(iNum.intValue());
                      this.getLogger().debug(iNum + " tracks");
                      buffering = false;
                    }
                    else
                      if (localName.equals("PPNQ"))
                      {
                        String sPNQ = buffer.toString();
                        writeHex(sPNQ, 2);
                        this.getLogger().debug("PPNQ is " + sPNQ);
                        buffering = false;
                      }
                      else
                        if (localName.equals("HEXDATA"))
                        {
                          writeHex(buffer.toString(), buffer.length() / 2);
                          buffering = false;
                        }
    }
    catch (ProcessingException e)
    {
      throw new SAXException(e);
    }
    catch (IOException e)
    {
      throw new SAXException(e);
    }
  }
  /*
    void processElement()
    {
      String parent = xe.getParentNode().getNodeName();
      if (xe.getNodeType() == Node.ELEMENT_NODE)
      {
        Element elem = (Element) xe;
        localName = elem.getTagName();
   
  
      if (xe.getNodeType() == Node.TEXT_NODE)
      {
  
        if (parent.equals("FORMAT"))
        {
          if (!chunkFlag)
          {
            throw new ProcessingException("FORMAT element only valid within a chunk");
          }
          if (!chunkType.equals("MThd"))
          {
            throw new ProcessingException("FORMAT element must be in MThd chunk");
          }
          String typ = xe.getNodeValue();
          if (typ == null)
          {
            throw new ProcessingException("null value for MThd->FORMAT");
          }
          typ = typ.trim();
          if (typ == null)
          {
            throw new ProcessingException("null value for MThd->FORMAT (after trim)");
          }
          for (int i = 0; i < typ.length(); i++)
          {
            if (typ.substring(i, i + 1).compareTo("0")
              < 0 | typ.substring(i, i + 1).compareTo("9")
              > 0)
            {
              throw new ProcessingException(
                "invalid numeric midi format: " + typ);
            }
          }
          this.getLogger().debug(
            "chunk->format is " + typ + " (parent is " + parent + ")");
          midiFormat = Utils.stringToInt(typ);
          writeHalfWord(midiFormat);
        }
  
        if (parent.equals("TRACKS"))
        {
          if (!chunkFlag)
          {
            throw new ProcessingException("TRACKS element only valid within a chunk");
          }
          if (!chunkType.equals("MThd"))
          {
            throw new ProcessingException("TRACKS element must be in MThd chunk");
          }
          String sNum = xe.getNodeValue();
          if (sNum == null)
          {
            throw new ProcessingException("null value for MThd->TRACKS");
          }
          sNum = sNum.trim();
          if (sNum == null)
          {
            throw new ProcessingException("null value for MThd->TRACKS (after trim)");
          }
          this.getLogger().debug("chunk->tracks is " + sNum);
          Integer iNum = new Integer(sNum);
          numTracks = iNum.intValue();
          writeHalfWord(numTracks);
        }
  
        if (parent.equals("PPNQ"))
        {
          if (!chunkFlag)
          {
            throw new ProcessingException("PPNQ element only valid within a chunk");
          }
          if (!chunkType.equals("MThd"))
          {
            throw new ProcessingException("PPNQ element must be in MThd chunk");
          }
          String pnq = xe.getNodeValue();
          if (pnq == null)
          {
            throw new ProcessingException("null value for MThd->PPNQ");
          }
          pnq = pnq.trim();
          if (pnq == null)
          {
            throw new ProcessingException("null value for MThd->PPNQ (after trim)");
          }
          this.getLogger().debug("chunk->ppnq is " + pnq);
          writeHex(pnq, 2);
        }
  
        if (parent.equals("HEXDATA"))
        {
          if (!chunkFlag)
          {
            throw new ProcessingException("HEXDATA element only valid within CHUNK");
          }
          String xdata = xe.getNodeValue();
          if (xdata == null)
          {
            throw new ProcessingException("null value for CHUNK->HEXDATA");
          }
          writeHex(xdata, chunkLen);
        }
  
      }
    }
  */
  void writeString(String s, int len) throws IOException, ProcessingException
  {
    int l = s.length();
    if (l != len)
    {
      throw new ProcessingException(
        "writeString; string length ("
          + l
          + ") != expected length ("
          + len
          + ")");
    }
    this.output.write(s.getBytes());
  }

  void writeHex(String h, int len) throws ProcessingException, IOException
  {
    int l = h.length();
    int cnt = 0;
    int bs = 0;
    int bc = 0;
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
          throw new ProcessingException("writeHex; internal error");
        }
      bc++;
      if (bc >= 2)
      {
        this.output.write(bs);
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
        "writeHex count (" + cnt + ") != length (" + len + ")");
    }
  }

  void writeFullWord(int f) throws ProcessingException, IOException
  {
    byte[] b = Utils.intToBa(f, 4);
    this.output.write(b);
  }

  void writeHalfWord(int h) throws ProcessingException, IOException
  {
    byte[] b = Utils.intToBa(h, 2);
    this.output.write(b);
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  public void characters(char[] str, int arg1, int arg2) throws SAXException
  {
    if (buffering)
    {
      buffer.append(str, arg1, arg2);
    }
  }

}
