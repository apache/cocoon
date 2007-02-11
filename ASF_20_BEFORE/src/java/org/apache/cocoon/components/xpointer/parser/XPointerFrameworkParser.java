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
package org.apache.cocoon.components.xpointer.parser;

import java.util.HashMap;

import org.apache.cocoon.components.xpointer.ElementPathPart;
import org.apache.cocoon.components.xpointer.ShorthandPart;
import org.apache.cocoon.components.xpointer.UnsupportedPart;
import org.apache.cocoon.components.xpointer.XPointer;
import org.apache.cocoon.components.xpointer.XPointerPart;
import org.apache.cocoon.components.xpointer.XmlnsPart;

public class XPointerFrameworkParser
    implements XPointerFrameworkParserConstants {
    private XPointer xpointer = new XPointer();
    private HashMap namespaces = new HashMap();

    public static void main(String[] args) throws Exception {
        System.out.println("will parse this: " + args[0]);
        XPointerFrameworkParser xfp =
            new XPointerFrameworkParser(new java.io.StringReader(args[0]));
        xfp.pointer();
    }

    public static XPointer parse(String xpointer) throws ParseException {
        XPointerFrameworkParser xfp =
            new XPointerFrameworkParser(new java.io.StringReader(xpointer));
        try {
            xfp.pointer();
        } catch (TokenMgrError e) {
            // Rethrow TokenMgrErrors as ParseExceptions, because errors aren't caught by Cocoon,
            // and mistyping in a xpointer isn't such a grave error
            throw new ParseException(e.getMessage());
        }
        return xfp.getXPointer();
    }

    public XPointer getXPointer() {
        return xpointer;
    }

    private String unescape(String data) throws ParseException {
        StringBuffer result = new StringBuffer(data.length());
        boolean inCircumflex = false;
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (inCircumflex) {
                switch (c) {
                    case '^' :
                    case '(' :
                    case ')' :
                        result.append(c);
                        inCircumflex = false;
                        break;
                    default :
                        throw new ParseException(
                            "Incorrect use of circumflex character at position "
                                + i
                                + " in the string "
                                + data);
                }
            } else if (c == '^') {
                inCircumflex = true;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    final public void pointer() throws ParseException {
        if (jj_2_1(2)) {
            schemeBased();
        } else {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case NCName :
                    shortHand();
                    break;
                default :
                    jj_la1[0] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
        }
    }

    final public void shortHand() throws ParseException {
        Token x;
        x = jj_consume_token(NCName);
        xpointer.addPart(new ShorthandPart(x.image));
    }

    final public void schemeBased() throws ParseException {
        pointerPart();
        label_1 : while (true) {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case NCName :
                case WS :
                case QName :
                    break;
                default :
                    jj_la1[1] = jj_gen;
                    break label_1;
            }
            label_2 : while (true) {
                switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                    case WS :
                        break;
                    default :
                        jj_la1[2] = jj_gen;
                        break label_2;
                }
                jj_consume_token(WS);
            }
            pointerPart();
        }
    }

    final public void pointerPart() throws ParseException {
        Token x;
        String schemeName;
        String schemeData;
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case NCName :
                x = jj_consume_token(NCName);
                break;
            case QName :
                x = jj_consume_token(QName);
                break;
            default :
                jj_la1[3] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        jj_consume_token(LBRACE);
        // when going inside the scheme data, swith to a different lexical state
        token_source.SwitchTo(IN_SCHEME);

        // store the scheme name
        schemeName = x.image;
        schemeData = schemeData();
        jj_consume_token(RBRACE);
        // when going outside the scheme data, swith back to the default lexical state
        token_source.SwitchTo(DEFAULT);

        // parse schemeName in prefix and localName
        String schemeNamespace = null, schemeLocalName = null;
        int colonPos = schemeName.indexOf(':');
        if (colonPos != -1) {
            String schemePrefix = schemeName.substring(0, colonPos);
            schemeNamespace = (String) namespaces.get(schemePrefix);
            schemeLocalName = schemeName.substring(colonPos + 1);
        } else {
            schemeLocalName = schemeName;
        }

        // add the pointer part
        if (schemeNamespace == null && schemeLocalName.equals("xmlns")) {
            int eqPos = schemeData.indexOf("=");
            if (eqPos == -1) {
                if (true)
                    throw new ParseException("xmlns scheme data should contain an equals sign");
            }

            // Note: the trimming below is not entirely correct, since space is only allowed left
            // and right of the equal sign, but not at the beginning and end of the schemeData
            String prefix = schemeData.substring(0, eqPos).trim();
            String namespace =
                schemeData.substring(eqPos + 1, schemeData.length()).trim();
            xpointer.addPart(new XmlnsPart(prefix, namespace));
            namespaces.put(prefix, namespace);
        } else if (
            schemeNamespace == null && schemeLocalName.equals("xpointer")) {
            xpointer.addPart(new XPointerPart(schemeData));
        } else if (
            "http://apache.org/cocoon/xpointer".equals(schemeNamespace)
                && schemeLocalName.equals("elementpath")) {
            xpointer.addPart(new ElementPathPart(schemeData));
        } else {
            xpointer.addPart(new UnsupportedPart(schemeName));
        }
    }

    final public String schemeData() throws ParseException {
        String temp;
        StringBuffer schemeData = new StringBuffer();
        label_3 : while (true) {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case LBRACE :
                case CIRC_LBRACE :
                case CIRC_RBRACE :
                case DOUBLE_CIRC :
                case NormalChar :
                    break;
                default :
                    jj_la1[4] = jj_gen;
                    break label_3;
            }
            temp = escapedData();
            schemeData.append(temp);
        }
        {
            if (true)
                return unescape(schemeData.toString());
        }
        throw new Error("Missing return statement in function");
    }

    final public String escapedData() throws ParseException {
        Token x;
        String temp;
        StringBuffer data = new StringBuffer();
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case NormalChar :
                x = jj_consume_token(NormalChar);
                data.append(x.image);
                break;
            case CIRC_LBRACE :
                x = jj_consume_token(CIRC_LBRACE);
                data.append(x.image);
                break;
            case CIRC_RBRACE :
                x = jj_consume_token(CIRC_RBRACE);
                data.append(x.image);
                break;
            case DOUBLE_CIRC :
                x = jj_consume_token(DOUBLE_CIRC);
                data.append(x.image);
                break;
            case LBRACE :
                x = jj_consume_token(LBRACE);
                data.append(x.image);
                temp = schemeData();
                data.append(temp);
                x = jj_consume_token(RBRACE);
                data.append(x.image);
                break;
            default :
                jj_la1[5] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {
            if (true)
                return data.toString();
        }
        throw new Error("Missing return statement in function");
    }

    final private boolean jj_2_1(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        boolean retval = !jj_3_1();
        jj_save(0, xla);
        return retval;
    }

    final private boolean jj_3R_6() {
        if (jj_scan_token(NCName))
            return true;
        if (jj_la == 0 && jj_scanpos == jj_lastpos)
            return false;
        return false;
    }

    final private boolean jj_3R_4() {
        if (jj_3R_5())
            return true;
        if (jj_la == 0 && jj_scanpos == jj_lastpos)
            return false;
        return false;
    }

    final private boolean jj_3R_5() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_6()) {
            jj_scanpos = xsp;
            if (jj_3R_7())
                return true;
            if (jj_la == 0 && jj_scanpos == jj_lastpos)
                return false;
        } else if (jj_la == 0 && jj_scanpos == jj_lastpos)
            return false;
        if (jj_scan_token(LBRACE))
            return true;
        if (jj_la == 0 && jj_scanpos == jj_lastpos)
            return false;
        return false;
    }

    final private boolean jj_3R_7() {
        if (jj_scan_token(QName))
            return true;
        if (jj_la == 0 && jj_scanpos == jj_lastpos)
            return false;
        return false;
    }

    final private boolean jj_3_1() {
        if (jj_3R_4())
            return true;
        if (jj_la == 0 && jj_scanpos == jj_lastpos)
            return false;
        return false;
    }

    public XPointerFrameworkParserTokenManager token_source;
    SimpleCharStream jj_input_stream;
    public Token token, jj_nt;
    private int jj_ntk;
    private Token jj_scanpos, jj_lastpos;
    private int jj_la;
    public boolean lookingAhead = false;
    private int jj_gen;
    final private int[] jj_la1 = new int[6];
    static private int[] jj_la1_0;
    static {
        jj_la1_0();
    }
    private static void jj_la1_0() {
        jj_la1_0 = new int[] { 0x80, 0x380, 0x100, 0x280, 0xf400, 0xf400, };
    }
    final private JJCalls[] jj_2_rtns = new JJCalls[1];
    private boolean jj_rescan = false;
    private int jj_gc = 0;

    public XPointerFrameworkParser(java.io.InputStream stream) {
        jj_input_stream = new SimpleCharStream(stream, 1, 1);
        token_source = new XPointerFrameworkParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++)
            jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++)
            jj_2_rtns[i] = new JJCalls();
    }

    public void ReInit(java.io.InputStream stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++)
            jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++)
            jj_2_rtns[i] = new JJCalls();
    }

    public XPointerFrameworkParser(java.io.Reader stream) {
        jj_input_stream = new SimpleCharStream(stream, 1, 1);
        token_source = new XPointerFrameworkParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++)
            jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++)
            jj_2_rtns[i] = new JJCalls();
    }

    public void ReInit(java.io.Reader stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++)
            jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++)
            jj_2_rtns[i] = new JJCalls();
    }

    public XPointerFrameworkParser(XPointerFrameworkParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++)
            jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++)
            jj_2_rtns[i] = new JJCalls();
    }

    public void ReInit(XPointerFrameworkParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++)
            jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++)
            jj_2_rtns[i] = new JJCalls();
    }

    final private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken;
        if ((oldToken = token).next != null)
            token = token.next;
        else
            token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        if (token.kind == kind) {
            jj_gen++;
            if (++jj_gc > 100) {
                jj_gc = 0;
                for (int i = 0; i < jj_2_rtns.length; i++) {
                    JJCalls c = jj_2_rtns[i];
                    while (c != null) {
                        if (c.gen < jj_gen)
                            c.first = null;
                        c = c.next;
                    }
                }
            }
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }

    final private boolean jj_scan_token(int kind) {
        if (jj_scanpos == jj_lastpos) {
            jj_la--;
            if (jj_scanpos.next == null) {
                jj_lastpos =
                    jj_scanpos = jj_scanpos.next = token_source.getNextToken();
            } else {
                jj_lastpos = jj_scanpos = jj_scanpos.next;
            }
        } else {
            jj_scanpos = jj_scanpos.next;
        }
        if (jj_rescan) {
            int i = 0;
            Token tok = token;
            while (tok != null && tok != jj_scanpos) {
                i++;
                tok = tok.next;
            }
            if (tok != null)
                jj_add_error_token(kind, i);
        }
        return (jj_scanpos.kind != kind);
    }

    final public Token getNextToken() {
        if (token.next != null)
            token = token.next;
        else
            token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        jj_gen++;
        return token;
    }

    final public Token getToken(int index) {
        Token t = lookingAhead ? jj_scanpos : token;
        for (int i = 0; i < index; i++) {
            if (t.next != null)
                t = t.next;
            else
                t = t.next = token_source.getNextToken();
        }
        return t;
    }

    final private int jj_ntk() {
        if ((jj_nt = token.next) == null)
            return (jj_ntk = (token.next = token_source.getNextToken()).kind);
        else
            return (jj_ntk = jj_nt.kind);
    }

    private java.util.Vector jj_expentries = new java.util.Vector();
    private int[] jj_expentry;
    private int jj_kind = -1;
    private int[] jj_lasttokens = new int[100];
    private int jj_endpos;

    private void jj_add_error_token(int kind, int pos) {
        if (pos >= 100)
            return;
        if (pos == jj_endpos + 1) {
            jj_lasttokens[jj_endpos++] = kind;
        } else if (jj_endpos != 0) {
            jj_expentry = new int[jj_endpos];
            for (int i = 0; i < jj_endpos; i++) {
                jj_expentry[i] = jj_lasttokens[i];
            }
            boolean exists = false;
            for (java.util.Enumeration enum = jj_expentries.elements();
                enum.hasMoreElements();
                ) {
                int[] oldentry = (int[]) (enum.nextElement());
                if (oldentry.length == jj_expentry.length) {
                    exists = true;
                    for (int i = 0; i < jj_expentry.length; i++) {
                        if (oldentry[i] != jj_expentry[i]) {
                            exists = false;
                            break;
                        }
                    }
                    if (exists)
                        break;
                }
            }
            if (!exists)
                jj_expentries.addElement(jj_expentry);
            if (pos != 0)
                jj_lasttokens[(jj_endpos = pos) - 1] = kind;
        }
    }

    public ParseException generateParseException() {
        jj_expentries.removeAllElements();
        boolean[] la1tokens = new boolean[16];
        for (int i = 0; i < 16; i++) {
            la1tokens[i] = false;
        }
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 6; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 16; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.addElement(jj_expentry);
            }
        }
        jj_endpos = 0;
        jj_rescan_token();
        jj_add_error_token(0, 0);
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = (int[]) jj_expentries.elementAt(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }

    final public void enable_tracing() {
    }

    final public void disable_tracing() {
    }

    final private void jj_rescan_token() {
        jj_rescan = true;
        for (int i = 0; i < 1; i++) {
            JJCalls p = jj_2_rtns[i];
            do {
                if (p.gen > jj_gen) {
                    jj_la = p.arg;
                    jj_lastpos = jj_scanpos = p.first;
                    switch (i) {
                        case 0 :
                            jj_3_1();
                            break;
                    }
                }
                p = p.next;
            } while (p != null);
        }
        jj_rescan = false;
    }

    final private void jj_save(int index, int xla) {
        JJCalls p = jj_2_rtns[index];
        while (p.gen > jj_gen) {
            if (p.next == null) {
                p = p.next = new JJCalls();
                break;
            }
            p = p.next;
        }
        p.gen = jj_gen + xla - jj_la;
        p.first = token;
        p.arg = xla;
    }

    static final class JJCalls {
        int gen;
        Token first;
        int arg;
        JJCalls next;
    }

}
