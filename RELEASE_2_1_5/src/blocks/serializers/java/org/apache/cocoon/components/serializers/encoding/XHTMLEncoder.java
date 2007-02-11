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
package org.apache.cocoon.components.serializers.encoding;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: XHTMLEncoder.java,v 1.1 2004/04/27 18:35:21 pier Exp $
 */
public class XHTMLEncoder extends XMLEncoder {

    /**
     * Create a new instance of this <code>XHTMLEncoder</code>.
     */
    public XHTMLEncoder() {
        super("X-W3C-XHTML");
    }
    
    /**
     * Create a new instance of this <code>XHTMLEncoder</code>.
     *
     * @param name A name for this <code>Encoding</code>.
     * @throws NullPointerException If one of the arguments is <b>null</b>.
     */
    protected XHTMLEncoder(String name) {
        super(name);
    }
    
    /**
     * Return true or false wether this encoding can encode the specified
     * character or not.
     * <p>
     * This method will return true for the following character range:
     * <br />
     * <code>
     *   <nobr>#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD]</nobr>
     * </code>
     * </p>
     *
     * @see <a href="http://www.w3.org/TR/REC-xml#charsets">W3C XML 1.0</a>
     */
    protected boolean compile(char c) {
        for (int x = 0; x < ENCODINGS.length; x++) {
            if (ENCODINGS[x][0][0] == c) return(false);
        }
        return(super.compile(c));
    }

    /**
     * Return an array of characters representing the encoding for the
     * specified character.
     */
    public char[] encode(char c) {
        for (int x = 0; x < ENCODINGS.length; x++) {
            if (ENCODINGS[x][0][0] == c) return(ENCODINGS[x][1]);
        }
        return(super.encode(c));
    }

    /**
     * The table of all configured HTML/4.0 character encodings.
     */
    private static final char ENCODINGS[][][] = {
        { { 160 } , "&nbsp;".toCharArray() },
        { { 161 } , "&iexcl;".toCharArray() },
        { { 162 } , "&cent;".toCharArray() },
        { { 163 } , "&pound;".toCharArray() },
        { { 164 } , "&curren;".toCharArray() },
        { { 165 } , "&yen;".toCharArray() },
        { { 166 } , "&brvbar;".toCharArray() },
        { { 167 } , "&sect;".toCharArray() },
        { { 168 } , "&uml;".toCharArray() },
        { { 169 } , "&copy;".toCharArray() },
        { { 170 } , "&ordf;".toCharArray() },
        { { 171 } , "&laquo;".toCharArray() },
        { { 172 } , "&not;".toCharArray() },
        { { 173 } , "&shy;".toCharArray() },
        { { 174 } , "&reg;".toCharArray() },
        { { 175 } , "&macr;".toCharArray() },
        { { 176 } , "&deg;".toCharArray() },
        { { 177 } , "&plusmn;".toCharArray() },
        { { 178 } , "&sup2;".toCharArray() },
        { { 179 } , "&sup3;".toCharArray() },
        { { 180 } , "&acute;".toCharArray() },
        { { 181 } , "&micro;".toCharArray() },
        { { 182 } , "&para;".toCharArray() },
        { { 183 } , "&middot;".toCharArray() },
        { { 184 } , "&cedil;".toCharArray() },
        { { 185 } , "&sup1;".toCharArray() },
        { { 186 } , "&ordm;".toCharArray() },
        { { 187 } , "&raquo;".toCharArray() },
        { { 188 } , "&frac14;".toCharArray() },
        { { 189 } , "&frac12;".toCharArray() },
        { { 190 } , "&frac34;".toCharArray() },
        { { 191 } , "&iquest;".toCharArray() },
        { { 192 } , "&Agrave;".toCharArray() },
        { { 193 } , "&Aacute;".toCharArray() },
        { { 194 } , "&Acirc;".toCharArray() },
        { { 195 } , "&Atilde;".toCharArray() },
        { { 196 } , "&Auml;".toCharArray() },
        { { 197 } , "&Aring;".toCharArray() },
        { { 198 } , "&AElig;".toCharArray() },
        { { 199 } , "&Ccedil;".toCharArray() },
        { { 200 } , "&Egrave;".toCharArray() },
        { { 201 } , "&Eacute;".toCharArray() },
        { { 202 } , "&Ecirc;".toCharArray() },
        { { 203 } , "&Euml;".toCharArray() },
        { { 204 } , "&Igrave;".toCharArray() },
        { { 205 } , "&Iacute;".toCharArray() },
        { { 206 } , "&Icirc;".toCharArray() },
        { { 207 } , "&Iuml;".toCharArray() },
        { { 208 } , "&ETH;".toCharArray() },
        { { 209 } , "&Ntilde;".toCharArray() },
        { { 210 } , "&Ograve;".toCharArray() },
        { { 211 } , "&Oacute;".toCharArray() },
        { { 212 } , "&Ocirc;".toCharArray() },
        { { 213 } , "&Otilde;".toCharArray() },
        { { 214 } , "&Ouml;".toCharArray() },
        { { 215 } , "&times;".toCharArray() },
        { { 216 } , "&Oslash;".toCharArray() },
        { { 217 } , "&Ugrave;".toCharArray() },
        { { 218 } , "&Uacute;".toCharArray() },
        { { 219 } , "&Ucirc;".toCharArray() },
        { { 220 } , "&Uuml;".toCharArray() },
        { { 221 } , "&Yacute;".toCharArray() },
        { { 222 } , "&THORN;".toCharArray() },
        { { 223 } , "&szlig;".toCharArray() },
        { { 224 } , "&agrave;".toCharArray() },
        { { 225 } , "&aacute;".toCharArray() },
        { { 226 } , "&acirc;".toCharArray() },
        { { 227 } , "&atilde;".toCharArray() },
        { { 228 } , "&auml;".toCharArray() },
        { { 229 } , "&aring;".toCharArray() },
        { { 230 } , "&aelig;".toCharArray() },
        { { 231 } , "&ccedil;".toCharArray() },
        { { 232 } , "&egrave;".toCharArray() },
        { { 233 } , "&eacute;".toCharArray() },
        { { 234 } , "&ecirc;".toCharArray() },
        { { 235 } , "&euml;".toCharArray() },
        { { 236 } , "&igrave;".toCharArray() },
        { { 237 } , "&iacute;".toCharArray() },
        { { 238 } , "&icirc;".toCharArray() },
        { { 239 } , "&iuml;".toCharArray() },
        { { 240 } , "&eth;".toCharArray() },
        { { 241 } , "&ntilde;".toCharArray() },
        { { 242 } , "&ograve;".toCharArray() },
        { { 243 } , "&oacute;".toCharArray() },
        { { 244 } , "&ocirc;".toCharArray() },
        { { 245 } , "&otilde;".toCharArray() },
        { { 246 } , "&ouml;".toCharArray() },
        { { 247 } , "&divide;".toCharArray() },
        { { 248 } , "&oslash;".toCharArray() },
        { { 249 } , "&ugrave;".toCharArray() },
        { { 250 } , "&uacute;".toCharArray() },
        { { 251 } , "&ucirc;".toCharArray() },
        { { 252 } , "&uuml;".toCharArray() },
        { { 253 } , "&yacute;".toCharArray() },
        { { 254 } , "&thorn;".toCharArray() },
        { { 255 } , "&yuml;".toCharArray() },
        { { 338 } , "&OElig;".toCharArray() },
        { { 339 } , "&oelig;".toCharArray() },
        { { 352 } , "&Scaron;".toCharArray() },
        { { 353 } , "&scaron;".toCharArray() },
        { { 376 } , "&Yuml;".toCharArray() },
        { { 402 } , "&fnof;".toCharArray() },
        { { 710 } , "&circ;".toCharArray() },
        { { 732 } , "&tilde;".toCharArray() },
        { { 913 } , "&Alpha;".toCharArray() },
        { { 914 } , "&Beta;".toCharArray() },
        { { 915 } , "&Gamma;".toCharArray() },
        { { 916 } , "&Delta;".toCharArray() },
        { { 917 } , "&Epsilon;".toCharArray() },
        { { 918 } , "&Zeta;".toCharArray() },
        { { 919 } , "&Eta;".toCharArray() },
        { { 920 } , "&Theta;".toCharArray() },
        { { 921 } , "&Iota;".toCharArray() },
        { { 922 } , "&Kappa;".toCharArray() },
        { { 923 } , "&Lambda;".toCharArray() },
        { { 924 } , "&Mu;".toCharArray() },
        { { 925 } , "&Nu;".toCharArray() },
        { { 926 } , "&Xi;".toCharArray() },
        { { 927 } , "&Omicron;".toCharArray() },
        { { 928 } , "&Pi;".toCharArray() },
        { { 929 } , "&Rho;".toCharArray() },
        { { 931 } , "&Sigma;".toCharArray() },
        { { 932 } , "&Tau;".toCharArray() },
        { { 933 } , "&Upsilon;".toCharArray() },
        { { 934 } , "&Phi;".toCharArray() },
        { { 935 } , "&Chi;".toCharArray() },
        { { 936 } , "&Psi;".toCharArray() },
        { { 937 } , "&Omega;".toCharArray() },
        { { 945 } , "&alpha;".toCharArray() },
        { { 946 } , "&beta;".toCharArray() },
        { { 947 } , "&gamma;".toCharArray() },
        { { 948 } , "&delta;".toCharArray() },
        { { 949 } , "&epsilon;".toCharArray() },
        { { 950 } , "&zeta;".toCharArray() },
        { { 951 } , "&eta;".toCharArray() },
        { { 952 } , "&theta;".toCharArray() },
        { { 953 } , "&iota;".toCharArray() },
        { { 954 } , "&kappa;".toCharArray() },
        { { 955 } , "&lambda;".toCharArray() },
        { { 956 } , "&mu;".toCharArray() },
        { { 957 } , "&nu;".toCharArray() },
        { { 958 } , "&xi;".toCharArray() },
        { { 959 } , "&omicron;".toCharArray() },
        { { 960 } , "&pi;".toCharArray() },
        { { 961 } , "&rho;".toCharArray() },
        { { 962 } , "&sigmaf;".toCharArray() },
        { { 963 } , "&sigma;".toCharArray() },
        { { 964 } , "&tau;".toCharArray() },
        { { 965 } , "&upsilon;".toCharArray() },
        { { 966 } , "&phi;".toCharArray() },
        { { 967 } , "&chi;".toCharArray() },
        { { 968 } , "&psi;".toCharArray() },
        { { 969 } , "&omega;".toCharArray() },
        { { 977 } , "&thetasym;".toCharArray() },
        { { 978 } , "&upsih;".toCharArray() },
        { { 982 } , "&piv;".toCharArray() },
        { { 8194 } , "&ensp;".toCharArray() },
        { { 8195 } , "&emsp;".toCharArray() },
        { { 8201 } , "&thinsp;".toCharArray() },
        { { 8204 } , "&zwnj;".toCharArray() },
        { { 8205 } , "&zwj;".toCharArray() },
        { { 8206 } , "&lrm;".toCharArray() },
        { { 8207 } , "&rlm;".toCharArray() },
        { { 8211 } , "&ndash;".toCharArray() },
        { { 8212 } , "&mdash;".toCharArray() },
        { { 8216 } , "&lsquo;".toCharArray() },
        { { 8217 } , "&rsquo;".toCharArray() },
        { { 8218 } , "&sbquo;".toCharArray() },
        { { 8220 } , "&ldquo;".toCharArray() },
        { { 8221 } , "&rdquo;".toCharArray() },
        { { 8222 } , "&bdquo;".toCharArray() },
        { { 8224 } , "&dagger;".toCharArray() },
        { { 8225 } , "&Dagger;".toCharArray() },
        { { 8226 } , "&bull;".toCharArray() },
        { { 8230 } , "&hellip;".toCharArray() },
        { { 8240 } , "&permil;".toCharArray() },
        { { 8242 } , "&prime;".toCharArray() },
        { { 8243 } , "&Prime;".toCharArray() },
        { { 8249 } , "&lsaquo;".toCharArray() },
        { { 8250 } , "&rsaquo;".toCharArray() },
        { { 8254 } , "&oline;".toCharArray() },
        { { 8260 } , "&frasl;".toCharArray() },
        { { 8364 } , "&euro;".toCharArray() },
        { { 8465 } , "&image;".toCharArray() },
        { { 8472 } , "&weierp;".toCharArray() },
        { { 8476 } , "&real;".toCharArray() },
        { { 8482 } , "&trade;".toCharArray() },
        { { 8501 } , "&alefsym;".toCharArray() },
        { { 8592 } , "&larr;".toCharArray() },
        { { 8593 } , "&uarr;".toCharArray() },
        { { 8594 } , "&rarr;".toCharArray() },
        { { 8595 } , "&darr;".toCharArray() },
        { { 8596 } , "&harr;".toCharArray() },
        { { 8629 } , "&crarr;".toCharArray() },
        { { 8656 } , "&lArr;".toCharArray() },
        { { 8657 } , "&uArr;".toCharArray() },
        { { 8658 } , "&rArr;".toCharArray() },
        { { 8659 } , "&dArr;".toCharArray() },
        { { 8660 } , "&hArr;".toCharArray() },
        { { 8704 } , "&forall;".toCharArray() },
        { { 8706 } , "&part;".toCharArray() },
        { { 8707 } , "&exist;".toCharArray() },
        { { 8709 } , "&empty;".toCharArray() },
        { { 8711 } , "&nabla;".toCharArray() },
        { { 8712 } , "&isin;".toCharArray() },
        { { 8713 } , "&notin;".toCharArray() },
        { { 8715 } , "&ni;".toCharArray() },
        { { 8719 } , "&prod;".toCharArray() },
        { { 8721 } , "&sum;".toCharArray() },
        { { 8722 } , "&minus;".toCharArray() },
        { { 8727 } , "&lowast;".toCharArray() },
        { { 8730 } , "&radic;".toCharArray() },
        { { 8733 } , "&prop;".toCharArray() },
        { { 8734 } , "&infin;".toCharArray() },
        { { 8736 } , "&ang;".toCharArray() },
        { { 8743 } , "&and;".toCharArray() },
        { { 8744 } , "&or;".toCharArray() },
        { { 8745 } , "&cap;".toCharArray() },
        { { 8746 } , "&cup;".toCharArray() },
        { { 8747 } , "&int;".toCharArray() },
        { { 8756 } , "&there4;".toCharArray() },
        { { 8764 } , "&sim;".toCharArray() },
        { { 8773 } , "&cong;".toCharArray() },
        { { 8776 } , "&asymp;".toCharArray() },
        { { 8800 } , "&ne;".toCharArray() },
        { { 8801 } , "&equiv;".toCharArray() },
        { { 8804 } , "&le;".toCharArray() },
        { { 8805 } , "&ge;".toCharArray() },
        { { 8834 } , "&sub;".toCharArray() },
        { { 8835 } , "&sup;".toCharArray() },
        { { 8836 } , "&nsub;".toCharArray() },
        { { 8838 } , "&sube;".toCharArray() },
        { { 8839 } , "&supe;".toCharArray() },
        { { 8853 } , "&oplus;".toCharArray() },
        { { 8855 } , "&otimes;".toCharArray() },
        { { 8869 } , "&perp;".toCharArray() },
        { { 8901 } , "&sdot;".toCharArray() },
        { { 8968 } , "&lceil;".toCharArray() },
        { { 8969 } , "&rceil;".toCharArray() },
        { { 8970 } , "&lfloor;".toCharArray() },
        { { 8971 } , "&rfloor;".toCharArray() },
        { { 9001 } , "&lang;".toCharArray() },
        { { 9002 } , "&rang;".toCharArray() },
        { { 9674 } , "&loz;".toCharArray() },
        { { 9824 } , "&spades;".toCharArray() },
        { { 9827 } , "&clubs;".toCharArray() },
        { { 9829 } , "&hearts;".toCharArray() },
        { { 9830 } , "&diams;".toCharArray() },
    };
}
