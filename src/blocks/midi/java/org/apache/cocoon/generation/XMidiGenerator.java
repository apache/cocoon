/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.midi.xmidi.ByteLen;
import org.apache.cocoon.components.midi.xmidi.Utils;
import org.apache.cocoon.components.midi.xmidi.Constants;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Reads a standard MIDI file and generates SAX Events.
 *
 * The MIDI file parsing parts of this class are based on code from the XMidi project, written
 * by  Peter Arthur Loeb (http://www.palserv.com/XMidi/) and used with permission.
 * The warranty disclaimer of the MIT license (http://www.opensource.org/licenses/mit-license.html)
 * applies to Peter Arthur Loeb's code.
 *
 * @author <a href="mailto:mark.leicester@energyintellect.com">Mark Leicester</a>
 * @author <a href="mailto:peter@palserv.com">Peter Loeb</a>
 */

public class XMidiGenerator
    extends AbstractGenerator
    implements Parameterizable {

    /** The input source */
    protected Source inputSource;
    private boolean global_verbose;
    private boolean local_verbose;

    private static final boolean VERBOSE_DEFAULT = true;

    private boolean validateVerbosity(String verbose) {
        if (verbose.equalsIgnoreCase("TRUE")) {
            return true;
        } else if (verbose.equalsIgnoreCase("FALSE")) {
            return false;
        }
        return VERBOSE_DEFAULT;
    }

    public void parameterize(Parameters parameters) throws ParameterException {
        global_verbose =  validateVerbosity(parameters.getParameter(
                    "verbose",
                    String.valueOf(VERBOSE_DEFAULT)));
    }

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        if (null != this.inputSource) {
            super.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        // Reinitialize variables
        initializeVariables();
        super.recycle();
    }

    /**
     * Setup the MIDI file generator.
     * Try to get the last modification date of the source for caching.
     */
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        // Initialize lookup tables
        initializeLookupTables();
        try {
            this.inputSource = resolver.resolveURI(src);
        } catch (SourceException se) {
            throw SourceUtil.handle("Error resolving '" + src + "'.", se);
        }
        local_verbose =
            validateVerbosity(
                parameters.getParameter(
                    "verbose",
                    String.valueOf(global_verbose)));
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public java.io.Serializable getKey() {
        return this.inputSource.getURI();
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return this.inputSource.getValidity();
    }

    /**
     * Generate XML data.
     */
    public void generate()
        throws IOException, SAXException, ProcessingException {
        try {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("processing file " + super.source);
                this.getLogger().debug(
                    "file resolved to " + this.inputSource.getURI());
            }
            parseMIDI(this.inputSource, super.xmlConsumer);

        } catch (SAXException e) {
            final Exception cause = e.getException();
            if (cause != null) {
                if (cause instanceof ProcessingException)
                    throw (ProcessingException) cause;
                if (cause instanceof IOException)
                    throw (IOException) cause;
                if (cause instanceof SAXException)
                    throw (SAXException) cause;
                throw new ProcessingException(
                    "Could not read resource " + this.inputSource.getURI(),
                    cause);
            }
            throw e;
        }
    }

    private int midiFormat;
    private Hashtable ffHash;
    private String[] chanArray;
    private String[] fArray;
    private int numTracks;
    private int trkCount;
    private String[] notes;
    private int[] register;
    private Hashtable contHash;

    /**
     * @param source
     * @param consumer
     */
    private void parseMIDI(Source source, XMLConsumer consumer)
        throws
            SAXException,
            SourceNotFoundException,
            IOException,
            ProcessingException {
        InputStream inputStream = source.getInputStream();

        AttributesImpl attr = new AttributesImpl();
        String text = "";
        this.contentHandler.startDocument();
        attr.addAttribute("", "VERSION", "VERSION", "CDATA", Constants.VERSION);
        this.contentHandler.startElement("", "XMidi", "XMidi", attr);

        boolean chunkFlag = true;
        while (chunkFlag) { // for each chunk
            byte[] hdr = new byte[8];

            int r;

            r = inputStream.read(hdr);
            if (r == -1) {
                chunkFlag = false;
                continue;
            }
            if (r < 8) {
                this.getLogger().debug("Getting header");
            }

            // get chunk id
            String cid = Utils.baToString(hdr, 0, 3);

            // get chunk length
            int len = Utils.baToInt(hdr, 4, 7);

            // get rest of chunk
            byte[] dta = new byte[len];
            r = inputStream.read(dta);
            if (r < len) {
                throw new ProcessingException("Getting data");
            }

            if (cid.equals("MThd")) {
                attr.clear();
                attr.addAttribute("", "LENGTH", "LENGTH", "CDATA", "" + len);
                attr.addAttribute("", "TYPE", "TYPE", "CDATA", cid);
                this.contentHandler.startElement("", "MThd", "MThd", attr);

                midiFormat = Utils.baToInt(dta, 0, 1);
                numTracks = Utils.baToInt(dta, 2, 3);
                String pnq = Utils.baToHex(dta, 4, 5);

                attr.clear();

                this.contentHandler.startElement("", "FORMAT", "FORMAT", attr);
                text = "" + midiFormat;
                this.contentHandler.characters(
                    text.toCharArray(),
                    0,
                    text.length());
                this.contentHandler.endElement("", "FORMAT", "FORMAT");

                this.contentHandler.startElement("", "TRACKS", "TRACKS", attr);
                text = "" + numTracks;
                this.contentHandler.characters(
                    text.toCharArray(),
                    0,
                    text.length());
                this.contentHandler.endElement("", "TRACKS", "TRACKS");

                this.contentHandler.startElement("", "PPNQ", "PPNQ", attr);
                text = pnq;
                this.contentHandler.characters(
                    text.toCharArray(),
                    0,
                    text.length());
                this.contentHandler.endElement("", "PPNQ", "PPNQ");

                this.contentHandler.endElement("", "MThd", "MThd");

            } else if (cid.equals("MTrk")) {
                trkCount++;
                if (trkCount > numTracks) {
                    throw new ProcessingException("too many tracks");
                }
                attr.clear();
                attr.addAttribute("", "LENGTH", "LENGTH", "CDATA", "" + len);
                attr.addAttribute("", "TYPE", "TYPE", "CDATA", cid);
                this.contentHandler.startElement("", "MTrk", "MTrk", attr);
                doTrack(dta, len);
                this.contentHandler.endElement("", "MTrk", "MTrk");
            } else {
                attr.clear();
                attr.addAttribute("", "LENGTH", "LENGTH", "CDATA", "" + len);
                attr.addAttribute("", "TYPE", "TYPE", "CDATA", cid);
                this.contentHandler.startElement("", "CHUNK", "CHUNK", attr);
                doHexData(dta, len);
                this.contentHandler.endElement("", "CHUNK", "CHUNK");
            }

        }

        this.contentHandler.endElement("", "XMidi", "XMidi");
        this.contentHandler.endDocument();
    }

    void initializeVariables() {
        numTracks = 0;
        trkCount = 0;
    }

    void initializeLookupTables() {
        ffHash = new Hashtable();
        ffHash.put("00", "Sequence Number");
        ffHash.put("01", "Text");
        ffHash.put("02", "Copyright");
        ffHash.put("03", "Sequence/Track Name");
        ffHash.put("04", "Instrument");
        ffHash.put("05", "Lyric");
        ffHash.put("06", "Marker");
        ffHash.put("07", "Cue Point");
        ffHash.put("20", "MIDI Channel");
        ffHash.put("21", "MIDI Port");
        ffHash.put("2F", "End of Track");
        ffHash.put("51", "Tempo");
        ffHash.put("54", "SMPTE Offset");
        ffHash.put("58", "Time Signature");
        ffHash.put("59", "Key Signature");
        ffHash.put("7F", "Proprietary Event");

        chanArray = new String[7];
        chanArray[0] = "Note Off";
        chanArray[1] = "Note On";
        chanArray[2] = "After Touch";
        chanArray[3] = "Control Change";
        chanArray[4] = "Program Change";
        chanArray[5] = "Channel Pressure";
        chanArray[6] = "Pitch Wheel";

        fArray = new String[16];
        fArray[0] = "SYSEX";
        fArray[1] = "MTC Quarter Frame Message";
        fArray[2] = "Song Position Pointer";
        fArray[3] = "Song Select";
        fArray[4] = "Undefined";
        fArray[5] = "Undefined";
        fArray[6] = "Tune Request";
        fArray[7] = "Unsupported";
        fArray[8] = "MIDI Clock";
        fArray[9] = "Undefined";
        fArray[10] = "MIDI Start";
        fArray[11] = "MIDI Continue";
        fArray[12] = "MIDI Stop";
        fArray[13] = "Undefined";
        fArray[14] = "Active Sense";
        fArray[15] = "NotUnderstood";

        contHash = new Hashtable();
        contHash.put("0", "Bank Select");
        contHash.put("1", "Modulation Wheel (coarse)");
        contHash.put("2", "Breath controller (coarse)");
        contHash.put("4", "Foot Pedal (coarse)");
        contHash.put("5", "Portamento Time (coarse)");
        contHash.put("6", "Data Entry (coarse)");
        contHash.put("7", "Volume (coarse)");
        contHash.put("8", "Balance (coarse)");
        contHash.put("10", "Pan position (coarse)");
        contHash.put("11", "Expression (coarse)");
        contHash.put("12", "Effect Control 1 (coarse)");
        contHash.put("13", "Effect Control 2 (coarse)");
        contHash.put("16", "General Purpose Slider 1");
        contHash.put("17", "General Purpose Slider 2");
        contHash.put("18", "General Purpose Slider 3");
        contHash.put("19", "General Purpose Slider 4");
        contHash.put("32", "Bank Select (fine)");
        contHash.put("33", "Modulation Wheel (fine)");
        contHash.put("34", "Breath controller (fine)");
        contHash.put("36", "Foot Pedal (fine)");
        contHash.put("37", "Portamento Time (fine)");
        contHash.put("38", "Data Entry (fine)");
        contHash.put("39", "Volume (fine)");
        contHash.put("40", "Balance (fine)");
        contHash.put("42", "Pan position (fine)");
        contHash.put("43", "Expression (fine)");
        contHash.put("44", "Effect Control 1 (fine)");
        contHash.put("45", "Effect Control 2 (fine)");
        contHash.put("64", "Hold Pedal (on/off)");
        contHash.put("65", "Portamento (on/off)");
        contHash.put("66", "Sustenuto Pedal (on/off)");
        contHash.put("67", "Soft Pedal (on/off)");
        contHash.put("68", "Legato Pedal (on/off)");
        contHash.put("69", "Hold 2 Pedal (on/off)");
        contHash.put("70", "Sound Variation");
        contHash.put("71", "Sound Timbre");
        contHash.put("72", "Sound Release Time");
        contHash.put("73", "Sound Attack Time");
        contHash.put("74", "Sound Brightness");
        contHash.put("75", "Sound Control 6");
        contHash.put("76", "Sound Control 7");
        contHash.put("77", "Sound Control 8");
        contHash.put("78", "Sound Control 9");
        contHash.put("79", "Sound Control 10");
        contHash.put("80", "General Purpose Button 1 (on/off)");
        contHash.put("81", "General Purpose Button 2 (on/off)");
        contHash.put("82", "General Purpose Button 3 (on/off)");
        contHash.put("83", "General Purpose Button 4 (on/off)");
        contHash.put("91", "Effects Level");
        contHash.put("92", "Tremulo Level");
        contHash.put("93", "Chorus Level");
        contHash.put("94", "Celeste Level");
        contHash.put("95", "Phaser Level");
        contHash.put("96", "Data Button increment");
        contHash.put("97", "Data Button decrement");
        contHash.put("98", "Non-registered Parameter (fine)");
        contHash.put("99", "Non-registered Parameter (coarse)");
        contHash.put("100", "Registered Parameter (fine)");
        contHash.put("101", "Registered Parameter (coarse)");
        contHash.put("120", "All Sound Off");
        contHash.put("121", "All Controllers Off");
        contHash.put("122", "Local Keyboard (on/off)");
        contHash.put("123", "All Notes Off");
        contHash.put("124", "Omni Mode Off");
        contHash.put("125", "Omni Mode On");
        contHash.put("126", "Mono Operation");
        contHash.put("127", "Poly Operation");

        notes = new String[128];
        register = new int[128];
        notes[0] = "C";
        register[0] = -5;
        notes[1] = "C#";
        register[1] = -5;
        notes[2] = "D";
        register[2] = -5;
        notes[3] = "Eb";
        register[3] = -5;
        notes[4] = "E";
        register[4] = -5;
        notes[5] = "F";
        register[5] = -5;
        notes[6] = "F#";
        register[6] = -5;
        notes[7] = "G";
        register[7] = -5;
        notes[8] = "Ab";
        register[8] = -5;
        notes[9] = "A";
        register[9] = -5;
        notes[10] = "Bb";
        register[10] = -5;
        notes[11] = "B";
        register[11] = -5;
        notes[12] = "C";
        register[12] = -4;
        notes[13] = "C#";
        register[13] = -4;
        notes[14] = "D";
        register[14] = -4;
        notes[15] = "Eb";
        register[15] = -4;
        notes[16] = "E";
        register[16] = -4;
        notes[17] = "F";
        register[17] = -4;
        notes[18] = "F#";
        register[18] = -4;
        notes[19] = "G";
        register[19] = -4;
        notes[20] = "Ab";
        register[20] = -4;
        notes[21] = "A";
        register[21] = -4;
        notes[22] = "Bb";
        register[22] = -4;
        notes[23] = "B";
        register[23] = -4;
        notes[24] = "C";
        register[24] = -3;
        notes[25] = "C#";
        register[25] = -3;
        notes[26] = "D";
        register[26] = -3;
        notes[27] = "Eb";
        register[27] = -3;
        notes[28] = "E";
        register[28] = -3;
        notes[29] = "F";
        register[29] = -3;
        notes[30] = "F#";
        register[30] = -3;
        notes[31] = "G";
        register[31] = -3;
        notes[32] = "Ab";
        register[32] = -3;
        notes[33] = "A";
        register[33] = -3;
        notes[34] = "Bb";
        register[34] = -3;
        notes[35] = "B";
        register[35] = -3;
        notes[36] = "C";
        register[36] = -2;
        notes[37] = "C#";
        register[37] = -2;
        notes[38] = "D";
        register[38] = -2;
        notes[39] = "Eb";
        register[39] = -2;
        notes[40] = "E";
        register[40] = -2;
        notes[41] = "F";
        register[41] = -2;
        notes[42] = "F#";
        register[42] = -2;
        notes[43] = "G";
        register[43] = -2;
        notes[44] = "Ab";
        register[44] = -2;
        notes[45] = "A";
        register[45] = -2;
        notes[46] = "Bb";
        register[46] = -2;
        notes[47] = "B";
        register[47] = -2;
        notes[48] = "C";
        register[48] = -1;
        notes[49] = "C#";
        register[49] = -1;
        notes[50] = "D";
        register[50] = -1;
        notes[51] = "Eb";
        register[51] = -1;
        notes[52] = "E";
        register[52] = -1;
        notes[53] = "F";
        register[53] = -1;
        notes[54] = "F#";
        register[54] = -1;
        notes[55] = "G";
        register[55] = -1;
        notes[56] = "Ab";
        register[56] = -1;
        notes[57] = "A";
        register[57] = -1;
        notes[58] = "Bb";
        register[58] = -1;
        notes[59] = "B";
        register[59] = -1;
        notes[60] = "C";
        register[60] = 0;
        notes[61] = "C#";
        register[61] = 0;
        notes[62] = "D";
        register[62] = 0;
        notes[63] = "Eb";
        register[63] = 0;
        notes[64] = "E";
        register[64] = 0;
        notes[65] = "F";
        register[65] = 0;
        notes[66] = "F#";
        register[66] = 0;
        notes[67] = "G";
        register[67] = 0;
        notes[68] = "Ab";
        register[68] = 0;
        notes[69] = "A";
        register[69] = 0;
        notes[70] = "Bb";
        register[70] = 0;
        notes[71] = "B";
        register[71] = 0;
        notes[72] = "C";
        register[72] = 1;
        notes[73] = "C#";
        register[73] = 1;
        notes[74] = "D";
        register[74] = 1;
        notes[75] = "Eb";
        register[75] = 1;
        notes[76] = "E";
        register[76] = 1;
        notes[77] = "F";
        register[77] = 1;
        notes[78] = "F#";
        register[78] = 1;
        notes[79] = "G";
        register[79] = 1;
        notes[80] = "Ab";
        register[80] = 1;
        notes[81] = "A";
        register[81] = 1;
        notes[82] = "Bb";
        register[82] = 1;
        notes[83] = "B";
        register[83] = 1;
        notes[84] = "C";
        register[84] = 2;
        notes[85] = "C#";
        register[85] = 2;
        notes[86] = "D";
        register[86] = 2;
        notes[87] = "Eb";
        register[87] = 2;
        notes[88] = "E";
        register[88] = 2;
        notes[89] = "F";
        register[89] = 2;
        notes[90] = "F#";
        register[90] = 2;
        notes[91] = "G";
        register[91] = 2;
        notes[92] = "Ab";
        register[92] = 2;
        notes[93] = "A";
        register[93] = 2;
        notes[94] = "Bb";
        register[94] = 2;
        notes[95] = "B";
        register[95] = 2;
        notes[96] = "C";
        register[96] = 3;
        notes[97] = "C#";
        register[97] = 3;
        notes[98] = "D";
        register[98] = 3;
        notes[99] = "Eb";
        register[99] = 3;
        notes[100] = "E";
        register[100] = 3;
        notes[101] = "F";
        register[101] = 3;
        notes[102] = "F#";
        register[102] = 3;
        notes[103] = "G";
        register[103] = 3;
        notes[104] = "Ab";
        register[104] = 3;
        notes[105] = "A";
        register[105] = 3;
        notes[106] = "Bb";
        register[106] = 3;
        notes[107] = "B";
        register[107] = 3;
        notes[108] = "C";
        register[108] = 4;
        notes[109] = "C#";
        register[109] = 4;
        notes[110] = "D";
        register[110] = 4;
        notes[111] = "Eb";
        register[111] = 4;
        notes[112] = "E";
        register[112] = 4;
        notes[113] = "F";
        register[113] = 4;
        notes[114] = "F#";
        register[114] = 4;
        notes[115] = "G";
        register[115] = 4;
        notes[116] = "Ab";
        register[116] = 4;
        notes[117] = "A";
        register[117] = 4;
        notes[118] = "Bb";
        register[118] = 4;
        notes[119] = "B";
        register[119] = 4;
        notes[120] = "C";
        register[120] = 5;
        notes[121] = "C#";
        register[121] = 5;
        notes[122] = "D";
        register[122] = 5;
        notes[123] = "Eb";
        register[123] = 5;
        notes[124] = "E";
        register[124] = 5;
        notes[125] = "F";
        register[125] = 5;
        notes[126] = "F#";
        register[126] = 5;
        notes[127] = "G";
        register[127] = 5;
    }

    /**
    add track data to DOM structure
    */
    void doTrack(byte[] dta, int len)
        throws SAXException, ProcessingException {
        AttributesImpl attr = new AttributesImpl();
        String text = "";

        boolean tFlag = true;
        int offset = 0;

        // initialize variables
        String edata = null;
        String snam = null;
        String nmData = "";
        String ctag = null;
        String ctagAttr[] = new String[4];
        String ctagAttrVal[] = new String[4];
        int ctagnum = 0;
        String cnum = null;
        String ctyp = null;
        int slen = 0;
        int chanType = 0;
        int hiName = 0;
        int status = 0;
        int noff = 0;
        String sval = null;
        boolean ecFlag = true; // assume edata
        boolean nFlag = true; // use slen for noff
        boolean firstTime = true;

        while (tFlag) {
            // do delta
            ByteLen bl = Utils.deltaToInt(dta, offset);
            offset += bl.len;
            String deltaLen = Utils.baToHex(bl.ba, 0, 3);

            nFlag = true; // assume simple (slen) offset
            // may or may not be status
            boolean statFlag = false;
            int first = Utils.baToInt(dta, offset, offset);
            this.getLogger().debug(
                "doTrack: in loop; deltaLen="
                    + deltaLen
                    + ", len="
                    + bl.len
                    + ", first="
                    + first);

            if ((first & 128) == 128) {
                // it is a status byte
                statFlag = true;
                sval = Utils.baToHex(dta, offset, offset);
                status = first;
                this.getLogger().debug("doTrack: have status: " + sval);
                if (status < 240 && status > 127) {
                    ecFlag = false;
                    chanType = (status - 128) / 16;
                    snam = chanArray[chanType];
                    ctyp = sval.substring(0, 1);
                    cnum = sval.substring(1, 2);
                } else {
                    ecFlag = true;
                    if (status > 239 && status < 256) {
                        hiName = status - 240;
                        snam = fArray[hiName];
                    } else {
                        throw new ProcessingException(
                            "Invalid status: " + status);
                    }
                }
                offset++;
            } else {
                this.getLogger().debug("doTrack: running status");
            }

            nmData = "";
            if (firstTime) {
                firstTime = false;
                if (!statFlag) {
                    throw new ProcessingException(
                        "first time, but no status; first = " + first);
                }
            }

            // offset points to the byte after the status
            // or first byte of "running status"

            attr.clear();
            attr.addAttribute("", "DTIME", "DTIME", "CDATA", "" + deltaLen);
            this.contentHandler.startElement("", "DELTA", "DELTA", attr);

            if (status > 127 && status < 144) {
                // note off
                slen = 2;
                // set up tag
                int pitch = Utils.baToInt(dta, offset, offset);
                int vel = Utils.baToInt(dta, offset + 1, offset + 1);
                ctag = "NOTE_OFF";
                ctagAttr[0] = "PITCH";
                ctagAttrVal[0] = "" + pitch;
                ctagAttr[1] = "VELOCITY";
                ctagAttrVal[1] = "" + vel;
                ctagnum = 2;
                if (local_verbose) {
                    ctagAttr[2] = "NAME";
                    ctagAttrVal[2] = notes[pitch];
                    ctagAttr[3] = "REGISTER";
                    ctagAttrVal[3] = "" + register[pitch];
                    ctagnum = 4;
                }
            } else if (status > 143 && status < 160) {
                // note on
                slen = 2;
                int pitch = Utils.baToInt(dta, offset, offset);
                int vel = Utils.baToInt(dta, offset + 1, offset + 1);
                ctag = "NOTE_ON";
                ctagAttr[0] = "PITCH";
                ctagAttrVal[0] = "" + pitch;
                ctagAttr[1] = "VELOCITY";
                ctagAttrVal[1] = "" + vel;
                ctagnum = 2;
                if (local_verbose) {
                    ctagAttr[2] = "NAME";
                    ctagAttrVal[2] = notes[pitch];
                    ctagAttr[3] = "REGISTER";
                    ctagAttrVal[3] = "" + register[pitch];
                    ctagnum = 4;
                }
            } else if (status > 159 && status < 176) {
                // after touch
                slen = 2;
                int pitch = Utils.baToInt(dta, offset, offset);
                int pres = Utils.baToInt(dta, offset + 1, offset + 1);
                ctag = "AFTER";
                ctagAttr[0] = "PITCH";
                ctagAttrVal[0] = "" + pitch;
                ctagAttr[1] = "PRESSURE";
                ctagAttrVal[1] = "" + pres;
                ctagnum = 2;
                if (local_verbose) {
                    ctagAttr[2] = "NAME";
                    ctagAttrVal[2] = notes[pitch];
                    ctagAttr[3] = "REGISTER";
                    ctagAttrVal[3] = "" + register[pitch];
                    ctagnum = 4;
                }
            } else if (status > 175 && status < 192) {
                // control change
                slen = 2;
                int contnum = Utils.baToInt(dta, offset, offset);
                int contval = Utils.baToInt(dta, offset + 1, offset + 1);
                ctag = "CONTROL";
                ctagAttr[0] = "NUMBER";
                ctagAttrVal[0] = "" + contnum;
                ctagAttr[1] = "VALUE";
                ctagAttrVal[1] = "" + contval;
                ctagnum = 2;
                if (local_verbose) {
                    ctagAttr[2] = "NAME";
                    ctagAttrVal[2] = (String) contHash.get("" + contnum);
                    ctagnum = 3;
                }
            } else if (status > 191 && status < 208) {
                // program (patch) change
                slen = 1;
                int patch = Utils.baToInt(dta, offset, offset);
                ctag = "PROGRAM";
                ctagAttr[0] = "NUMBER";
                ctagAttrVal[0] = "" + patch;
                ctagnum = 1;
            } else if (status > 207 && status < 224) {
                // channel pressure
                slen = 1;
                int pamt = Utils.baToInt(dta, offset, offset);
                ctag = "PRESSURE";
                ctagAttr[0] = "AMOUNT";
                ctagAttrVal[0] = "" + pamt;
                ctagnum = 1;
            } else if (status > 223 && status < 240) {
                // pitch wheel
                slen = 2;
                int pwamt = Utils.getPW(dta, offset);
                ctag = "WHEEL";
                ctagAttr[0] = "AMOUNT";
                ctagAttrVal[0] = "" + pwamt;
                ctagnum = 1;
            } else if (status == 240) {
                // sysex
                bl = Utils.deltaToInt(dta, offset);
                slen = Utils.baToInt(bl.ba, 0, 3);
                noff = bl.len;
                nFlag = false;
                edata =
                    Utils.baToHex(dta, offset + noff, offset + noff + slen - 1);
                noff += slen;
            } else if (status == 255) {
                // non midi (reset only in "live" midi")
                nmData = Utils.baToHex(dta, offset, offset);
                //						nmData = "SNMT=\""+nmDta+"\" ";
                snam = "non-MIDI";
                String nmNam = (String) ffHash.get(nmData);
                if (nmNam != null) {
                    snam += " - " + nmNam;
                } else {
                    snam += " - Unknown";
                }
                //						int nmt = baToInt(dta,offset+1,offset+1);
                bl = Utils.deltaToInt(dta, offset + 1);
                slen = Utils.baToInt(bl.ba, 0, 3);
                noff = bl.len + 1;
                nFlag = false;
                if (slen == 0) {
                    edata = "No data";
                } else {
                    edata =
                        Utils.baToHex(
                            dta,
                            offset + noff,
                            offset + noff + slen - 1);
                    noff += slen;
                }
                this.getLogger().debug(
                    "doTrack: status FF" + nmData + ", edata = " + edata);
            } else if (status == 241 || status == 243) {
                int tcv = dta[offset];
                Integer tc = new Integer(tcv);
                edata = tc.toString();
                slen = 1;
            } else if (status == 242) {
                int tcv = Utils.getPW(dta, offset);
                Integer tc = new Integer(tcv);
                edata = tc.toString();
                slen = 2;
            } else if (
                status == 246
                    || status == 248
                    || status == 250
                    || status == 251
                    || status == 252
                    || status == 254) {
                edata = "No data for " + snam;
                slen = 0;
            } else { // really unknown
                int ol = Utils.getNextHiOrd(dta, offset);
                edata = Utils.baToHex(dta, offset + 1, ol);
                ol -= offset + 1;
                slen = ol;
            }

            if (ecFlag) {
                if (statFlag) {
                    attr.clear();
                    attr.addAttribute("", "SLEN", "SLEN", "CDATA", "" + slen);
                    attr.addAttribute("", "SNAM", "SNAM", "CDATA", snam);
                    if (!nmData.equals("")) {
                        attr.addAttribute("", "SNMT", "SNMT", "CDATA", nmData);
                    }
                    attr.addAttribute("", "SVAL", "SVAL", "CDATA", sval);
                    this.contentHandler.startElement(
                        "",
                        "STATUS",
                        "STATUS",
                        attr);

                    attr.clear();
                    this.contentHandler.startElement(
                        "",
                        "EDATA",
                        "EDATA",
                        attr);
                    text = edata;
                    this.contentHandler.characters(
                        text.toCharArray(),
                        0,
                        text.length());
                    this.contentHandler.endElement("", "EDATA", "EDATA");

                    this.contentHandler.endElement("", "STATUS", "STATUS");
                } else {
                    attr.clear();
                    this.contentHandler.startElement(
                        "",
                        "EDATA",
                        "EDATA",
                        attr);
                    text = edata;
                    this.contentHandler.characters(
                        text.toCharArray(),
                        0,
                        text.length());
                    this.contentHandler.endElement("", "EDATA", "EDATA");
                }
            } else {

                if (statFlag) {
                    attr.clear();
                    attr.addAttribute("", "SLEN", "SLEN", "CDATA", "" + slen);
                    attr.addAttribute("", "SNAM", "SNAM", "CDATA", snam);
                    if (!nmData.equals("")) {
                        attr.addAttribute("", "SNMT", "SNMT", "CDATA", nmData);
                    }
                    attr.addAttribute("", "SVAL", "SVAL", "CDATA", sval);
                    this.contentHandler.startElement(
                        "",
                        "STATUS",
                        "STATUS",
                        attr);

                    attr.clear();
                    attr.addAttribute("", "NUMBER", "NUMBER", "CDATA", cnum);
                    attr.addAttribute("", "TYPE", "TYPE", "CDATA", ctyp);
                    this.contentHandler.startElement(
                        "",
                        "CHANNEL",
                        "CHANNEL",
                        attr);

                    attr.clear();
                    for (int c = 0; c < ctagnum; c++) {
                        attr.addAttribute(
                            "",
                            ctagAttr[c],
                            ctagAttr[c],
                            "CDATA",
                            ctagAttrVal[c]);
                    }
                    this.contentHandler.startElement("", ctag, ctag, attr);
                    this.contentHandler.endElement("", ctag, ctag);

                    this.contentHandler.endElement("", "CHANNEL", "CHANNEL");
                    this.contentHandler.endElement("", "STATUS", "STATUS");
                } else {
                    attr.clear();
                    attr.addAttribute("", "NUMBER", "NUMBER", "CDATA", cnum);
                    attr.addAttribute("", "TYPE", "TYPE", "CDATA", ctyp);
                    this.contentHandler.startElement(
                        "",
                        "CHANNEL",
                        "CHANNEL",
                        attr);

                    attr.clear();
                    for (int c = 0; c < ctagnum; c++) {
                        attr.addAttribute(
                            "",
                            ctagAttr[c],
                            ctagAttr[c],
                            "CDATA",
                            ctagAttrVal[c]);
                    }
                    this.contentHandler.startElement("", ctag, ctag, attr);
                    this.contentHandler.endElement("", ctag, ctag);

                    this.contentHandler.endElement("", "CHANNEL", "CHANNEL");
                }

            }

            this.contentHandler.endElement("", "DELTA", "DELTA");

            if (nFlag) {
                offset += slen;
            } else {
                offset += noff;
            }
            if (offset >= len) {
                tFlag = false;
            }
        }
    }

    /**
    write formatted hex data to file
    */

    void doHexData(byte[] dta, int len)
        throws ProcessingException, SAXException {
        AttributesImpl attr = new AttributesImpl();
        String text = "";

        this.contentHandler.startElement("", "HEXDATA", "HEXDATA", attr);

        StringBuffer bth = new StringBuffer("");
        int bpl = 32;
        int r = len % bpl;
        int n = len / bpl;
        for (int i = 0; i < n; i++) {
            int strt = i * bpl;
            bth.append(Utils.baToHex(dta, strt, strt + bpl - 1));
        }
        if (r > 0) {
            int strt = n * bpl;
            bth.append(Utils.baToHex(dta, strt, strt + r - 1));
        }

        text = bth.toString();
        this.contentHandler.characters(text.toCharArray(), 0, text.length());

        this.contentHandler.endElement("", "HEXDATA", "HEXDATA");
    }

}
