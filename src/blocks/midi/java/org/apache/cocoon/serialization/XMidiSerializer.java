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

public class XMidiSerializer extends AbstractSerializer {
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
    public void recycle() {
        preventDataWrite = false;
        state = OUTSIDE_XMIDI;
        super.recycle();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapOutputComponent#getMimeType()
     */
    public String getMimeType() {
        return (mimeType);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts)
        throws SAXException {
        try {
            if (localName.equals("XMidi")) {
                if (state != OUTSIDE_XMIDI) {
                    throw new SAXException("XMidi element not expected here");
                }
                state = INSIDE_XMIDI;
                String version = atts.getValue("VERSION");
                if (version == null) {
                    throw new SAXException("XMidi element has no version attribute");
                } else if (!version.equals(Constants.VERSION)) {
                    throw new SAXException(
                        "XMidi element has wrong version: expecting "
                            + Constants.VERSION
                            + ", got "
                            + version);
                }
                this.getLogger().debug(
                    "Found XMidi element, version " + version);
            } else if (localName.equals("CHUNK")) {
                if (state != INSIDE_XMIDI) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }
                state = INSIDE_CHUNK;
                writeString(atts.getValue("TYPE"), 4);
                Integer iLen = new Integer(atts.getValue("LENGTH"));
                writeFullWord(iLen.intValue());
                this.getLogger().debug(
                    "chunk type is: "
                        + atts.getValue("TYPE")
                        + " with length "
                        + atts.getValue("LENGTH"));
            } else if (localName.equals("MThd")) {
                if (state != INSIDE_XMIDI) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }
                state = INSIDE_MTHD;
                writeString(atts.getValue("TYPE"), 4);
                writeFullWord(Utils.stringToInt(atts.getValue("LENGTH")));
                this.getLogger().debug(
                    "we have MThd chunk; len = " + atts.getValue("LENGTH"));
            } else if (localName.equals("MTrk")) {
                if (state != INSIDE_XMIDI) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }
                state = INSIDE_MTRK;
                writeString(atts.getValue("TYPE"), 4);
                writeFullWord(Utils.stringToInt(atts.getValue("LENGTH")));
                this.getLogger().debug(
                    "we have MTrk chunk; len = " + atts.getValue("LENGTH"));
            } else if (localName.equals("DELTA")) {
                if (state != INSIDE_MTRK) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
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

            } else if (localName.equals("STATUS")) {
                if (state != INSIDE_DELTA) {
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
                this.getLogger().debug(
                    "Status: " + sval + ", len = " + expectedBytes);
                if (sval.equals("FF")) {
                    String nmd = atts.getValue("SNMT");
                    writeHex(nmd, 1);
                    byte[] hdt = Utils.intToBa(expectedBytes, 4);
                    byte[] xdt = Utils.intToDelta(hdt);
                    this.output.write(xdt);
                    if (expectedBytes == 0) {
                        preventDataWrite = true;
                    }
                    this.getLogger().debug("Non-midi: " + nmd);
                } else if (sval.equals("F0")) {
                    byte[] hdt = Utils.intToBa(Utils.stringToInt(sl), 4);
                    this.output.write(Utils.intToDelta(hdt));
                    this.getLogger().debug("Sysex");
                } else if (
                    sval.equals("F6")
                        | sval.equals("F8")
                        | sval.equals("FA")
                        | sval.equals("FB")
                        | sval.equals("FC")
                        | sval.equals("FE")) {
                    preventDataWrite = true;
                    this.getLogger().debug("no data");
                }
            } else if (localName.equals("CHANNEL")) {
                if ((state != INSIDE_DELTA) && (state != INSIDE_STATUS)) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }
                if (state == INSIDE_DELTA) {
                    state = INSIDE_DELTA_CHANNEL;
                } else if (state == INSIDE_STATUS) {
                    state = INSIDE_STATUS_CHANNEL;
                }
                this.getLogger().debug("Channel");
            } else if (localName.equals("NOTE_OFF")) {
                if ((state != INSIDE_DELTA_CHANNEL)
                    && (state != INSIDE_STATUS_CHANNEL)) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }
                String pitch = atts.getValue("PITCH");
                this.output.write(Utils.stringToInt(pitch));
                String vel = atts.getValue("VELOCITY");
                this.output.write(Utils.stringToInt(vel));
                this.getLogger().debug("Note off - " + pitch + ", " + vel);
            } else if (localName.equals("NOTE_ON")) {
                if ((state != INSIDE_DELTA_CHANNEL)
                    && (state != INSIDE_STATUS_CHANNEL)) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }
                String pitch = atts.getValue("PITCH");
                this.output.write(Utils.stringToInt(pitch));
                String vel = atts.getValue("VELOCITY");
                this.output.write(Utils.stringToInt(vel));
                this.getLogger().debug("Note on - " + pitch + ", " + vel);
            } else if (localName.equals("AFTER")) {
                if ((state != INSIDE_DELTA_CHANNEL)
                    && (state != INSIDE_STATUS_CHANNEL)) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }
                String pitch = atts.getValue("PITCH");
                this.output.write(Utils.stringToInt(pitch));
                String pres = atts.getValue("PRESSURE");
                this.output.write(Utils.stringToInt(pres));
                this.getLogger().debug("AFTER - " + pitch + ", " + pres);
            } else if (localName.equals("CONTROL")) {
                if ((state != INSIDE_DELTA_CHANNEL)
                    && (state != INSIDE_STATUS_CHANNEL)) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }
                String cnum = atts.getValue("NUMBER");
                this.output.write(Utils.stringToInt(cnum));
                String val = atts.getValue("VALUE");
                this.output.write(Utils.stringToInt(val));
                this.getLogger().debug("CONTROL - " + cnum + ", " + val);
            } else if (localName.equals("PROGRAM")) {
                if ((state != INSIDE_DELTA_CHANNEL)
                    && (state != INSIDE_STATUS_CHANNEL)) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }
                String patch = atts.getValue("NUMBER");
                this.output.write(Utils.stringToInt(patch));
                this.getLogger().debug("PATCH - " + patch);
            } else if (localName.equals("PRESSURE")) {
                if ((state != INSIDE_DELTA_CHANNEL)
                    && (state != INSIDE_STATUS_CHANNEL)) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }

                String amt = atts.getValue("AMOUNT");
                this.output.write(Utils.stringToInt(amt));
                this.getLogger().debug("PRESSURE - " + amt);
            } else if (localName.equals("WHEEL")) {
                if ((state != INSIDE_DELTA_CHANNEL)
                    && (state != INSIDE_STATUS_CHANNEL)) {
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
            } else if (localName.equals("EDATA")) {
                if ((state != INSIDE_DELTA) && (state != INSIDE_STATUS)) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }
                buffer = new StringBuffer();
                buffering = true;
                this.getLogger().debug("EDATA (element, not text)");
            } else if (
                localName.equals("FORMAT")
                    || localName.equals("TRACKS")
                    || localName.equals("PPNQ")) {
                if (state != INSIDE_MTHD) {
                    throw new SAXException(
                        localName
                            + " element not expected here, state = "
                            + state);
                }
                buffer = new StringBuffer();
                buffering = true;
                this.getLogger().debug(localName + " element");
            } else {
                this.getLogger().debug(
                    "Found " + localName + ", in state " + state);
            }

        } catch (ProcessingException e) {
            throw new SAXException(e);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /* (non-Javadoc)
       * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
       */
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {
        try {
            if (localName.equals("CHUNK")) {
                state = INSIDE_XMIDI;
            } else if (localName.equals("MThd") || localName.equals("MTrk")) {
                state = INSIDE_XMIDI;
            } else if (localName.equals("DELTA")) {
                state = INSIDE_MTRK;
            } else if (localName.equals("STATUS")) {
                state = INSIDE_DELTA;
            } else if (localName.equals("CHANNEL")) {
                if (state == INSIDE_STATUS_CHANNEL) {
                    state = INSIDE_STATUS;
                } else if (state == INSIDE_DELTA_CHANNEL) {
                    state = INSIDE_DELTA;
                }
            } else if (localName.equals("EDATA")) {
                if (!preventDataWrite) {
                    writeHex(buffer.toString(), expectedBytes);
                    this.getLogger().debug("EDATA: " + buffer.toString());
                } else {
                    preventDataWrite = false;
                }
                buffering = false;
            } else if (localName.equals("FORMAT")) {
                String typ = buffer.toString();
                for (int i = 0; i < typ.length(); i++) {
                    if (typ.substring(i, i + 1).compareTo("0")
                        < 0 | typ.substring(i, i + 1).compareTo("9")
                        > 0) {
                        throw new ProcessingException(
                            "Invalid numeric midi format: " + typ);
                    }
                }
                int midiFormat = Utils.stringToInt(typ);
                writeHalfWord(midiFormat);
                this.getLogger().debug("Format is " + midiFormat);
                buffering = false;
            } else if (localName.equals("TRACKS")) {
                String sNum = buffer.toString();
                Integer iNum = new Integer(sNum);
                writeHalfWord(iNum.intValue());
                this.getLogger().debug(iNum + " tracks");
                buffering = false;
            } else if (localName.equals("PPNQ")) {
                String sPNQ = buffer.toString();
                writeHex(sPNQ, 2);
                this.getLogger().debug("PPNQ is " + sPNQ);
                buffering = false;
            } else if (localName.equals("HEXDATA")) {
                writeHex(buffer.toString(), buffer.length() / 2);
                buffering = false;
            }
        } catch (ProcessingException e) {
            throw new SAXException(e);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    void writeString(String s, int len)
        throws IOException, ProcessingException {
        int l = s.length();
        if (l != len) {
            throw new ProcessingException(
                "writeString; string length ("
                    + l
                    + ") != expected length ("
                    + len
                    + ")");
        }
        this.output.write(s.getBytes());
    }

    void writeHex(String h, int len) throws ProcessingException, IOException {
        int l = h.length();
        int cnt = 0;
        int bs = 0;
        int bc = 0;
        for (int i = 0; i < l; i++) {
            String s = h.substring(i, i + 1);
            int x = " \n\t\r".indexOf(s);
            if (x != -1) {
                continue;
            }
            int tmp = "0123456789ABCDEF".indexOf(s.toUpperCase());
            if (bc == 0) {
                bs = tmp;
            } else if (bc == 1) {
                bs <<= 4;
                bs |= tmp;
            } else {
                throw new ProcessingException("writeHex; internal error");
            }
            bc++;
            if (bc >= 2) {
                this.output.write(bs);
                cnt++;
                bc = 0;
            }
        }
        if (bc != 0) {
            throw new ProcessingException("un-even number of hex digits");
        }
        if (cnt != len) {
            throw new ProcessingException(
                "writeHex count (" + cnt + ") != length (" + len + ")");
        }
    }

    void writeFullWord(int f) throws ProcessingException, IOException {
        byte[] b = Utils.intToBa(f, 4);
        this.output.write(b);
    }

    void writeHalfWord(int h) throws ProcessingException, IOException {
        byte[] b = Utils.intToBa(h, 2);
        this.output.write(b);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] str, int arg1, int arg2)
        throws SAXException {
        if (buffering) {
            buffer.append(str, arg1, arg2);
        }
    }

}
