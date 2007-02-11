/* ============================================================================ *
 *                   The Apache Software License, Version 1.1                   *
 * ============================================================================ *
 *                                                                              *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved. *
 *                                                                              *
 * Redistribution and use in source and binary forms, with or without modifica- *
 * tion, are permitted provided that the following conditions are met:          *
 *                                                                              *
 * 1. Redistributions of  source code must  retain the above copyright  notice, *
 *    this list of conditions and the following disclaimer.                     *
 *                                                                              *
 * 2. Redistributions in binary form must reproduce the above copyright notice, *
 *    this list of conditions and the following disclaimer in the documentation *
 *    and/or other materials provided with the distribution.                    *
 *                                                                              *
 * 3. The end-user documentation included with the redistribution, if any, must *
 *    include  the following  acknowledgment:  "This product includes  software *
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)." *
 *    Alternately, this  acknowledgment may  appear in the software itself,  if *
 *    and wherever such third-party acknowledgments normally appear.            *
 *                                                                              *
 * 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be *
 *    used to  endorse or promote  products derived from  this software without *
 *    prior written permission. For written permission, please contact          *
 *    apache@apache.org.                                                        *
 *                                                                              *
 * 5. Products  derived from this software may not  be called "Apache", nor may *
 *    "Apache" appear  in their name,  without prior written permission  of the *
 *    Apache Software Foundation.                                               *
 *                                                                              *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND *
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE *
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, *
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU- *
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS *
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON *
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT *
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF *
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
 *                                                                              *
 * This software  consists of voluntary contributions made  by many individuals *
 * on  behalf of the Apache Software  Foundation.  For more  information on the *
 * Apache Software Foundation, please see <http://www.apache.org/>.             *
 *                                                                              *
 * ============================================================================ */
package org.apache.garbage.parser;

/**
 * Tables available to the parser.
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: ParserTables.java,v 1.1 2003/06/21 21:11:48 pier Exp $
 */
public interface ParserTables {

    /**
     * The local table of entity reference names indexed by their String
     * value hash code (optimize for speed).
     */
    public static final int entityReferences[][] = {
        { "quot".hashCode(),       34 },
        { "num".hashCode(),        35 },
        { "dollar".hashCode(),     36 },
        { "amp".hashCode(),        38 },
        { "apos".hashCode(),       39 },
        { "lt".hashCode(),         60 },
        { "gt".hashCode(),         62 },
        { "lbrace".hashCode(),    123 },
        { "rbrace".hashCode(),    125 },
        { "pound".hashCode(),     163 },
        { "OElig".hashCode(),     338 },
        { "oelig".hashCode(),     339 },
        { "Scaron".hashCode(),    352 },
        { "scaron".hashCode(),    353 },
        { "Yuml".hashCode(),      376 },
        { "fnof".hashCode(),      402 },
        { "circ".hashCode(),      710 },
        { "tilde".hashCode(),     732 },
        { "Alpha".hashCode(),     913 },
        { "Beta".hashCode(),      914 },
        { "Gamma".hashCode(),     915 },
        { "Delta".hashCode(),     916 },
        { "Epsilon".hashCode(),   917 },
        { "Zeta".hashCode(),      918 },
        { "Eta".hashCode(),       919 },
        { "Theta".hashCode(),     920 },
        { "Iota".hashCode(),      921 },
        { "Kappa".hashCode(),     922 },
        { "Lambda".hashCode(),    923 },
        { "Mu".hashCode(),        924 },
        { "Nu".hashCode(),        925 },
        { "Xi".hashCode(),        926 },
        { "Omicron".hashCode(),   927 },
        { "Pi".hashCode(),        928 },
        { "Rho".hashCode(),       929 },
        { "Sigma".hashCode(),     931 },
        { "Tau".hashCode(),       932 },
        { "Upsilon".hashCode(),   933 },
        { "Phi".hashCode(),       934 },
        { "Chi".hashCode(),       935 },
        { "Psi".hashCode(),       936 },
        { "Omega".hashCode(),     937 },
        { "alpha".hashCode(),     945 },
        { "beta".hashCode(),      946 },
        { "gamma".hashCode(),     947 },
        { "delta".hashCode(),     948 },
        { "epsilon".hashCode(),   949 },
        { "zeta".hashCode(),      950 },
        { "eta".hashCode(),       951 },
        { "theta".hashCode(),     952 },
        { "iota".hashCode(),      953 },
        { "kappa".hashCode(),     954 },
        { "lambda".hashCode(),    955 },
        { "mu".hashCode(),        956 },
        { "nu".hashCode(),        957 },
        { "xi".hashCode(),        958 },
        { "omicron".hashCode(),   959 },
        { "pi".hashCode(),        960 },
        { "rho".hashCode(),       961 },
        { "sigmaf".hashCode(),    962 },
        { "sigma".hashCode(),     963 },
        { "tau".hashCode(),       964 },
        { "upsilon".hashCode(),   965 },
        { "phi".hashCode(),       966 },
        { "chi".hashCode(),       967 },
        { "psi".hashCode(),       968 },
        { "omega".hashCode(),     969 },
        { "thetasym".hashCode(),  977 },
        { "upsih".hashCode(),     978 },
        { "piv".hashCode(),       982 },
        { "ensp".hashCode(),     8194 },
        { "emsp".hashCode(),     8195 },
        { "thinsp".hashCode(),   8201 },
        { "zwnj".hashCode(),     8204 },
        { "zwj".hashCode(),      8205 },
        { "lrm".hashCode(),      8206 },
        { "rlm".hashCode(),      8207 },
        { "ndash".hashCode(),    8211 },
        { "mdash".hashCode(),    8212 },
        { "lsquo".hashCode(),    8216 },
        { "rsquo".hashCode(),    8217 },
        { "sbquo".hashCode(),    8218 },
        { "ldquo".hashCode(),    8220 },
        { "rdquo".hashCode(),    8221 },
        { "bdquo".hashCode(),    8222 },
        { "dagger".hashCode(),   8224 },
        { "Dagger".hashCode(),   8225 },
        { "bull".hashCode(),     8226 },
        { "hellip".hashCode(),   8230 },
        { "permil".hashCode(),   8240 },
        { "prime".hashCode(),    8242 },
        { "Prime".hashCode(),    8243 },
        { "lsaquo".hashCode(),   8249 },
        { "rsaquo".hashCode(),   8250 },
        { "oline".hashCode(),    8254 },
        { "frasl".hashCode(),    8260 },
        { "euro".hashCode(),     8364 },
        { "image".hashCode(),    8465 },
        { "weierp".hashCode(),   8472 },
        { "real".hashCode(),     8476 },
        { "trade".hashCode(),    8482 },
        { "alefsym".hashCode(),  8501 },
        { "larr".hashCode(),     8592 },
        { "uarr".hashCode(),     8593 },
        { "rarr".hashCode(),     8594 },
        { "darr".hashCode(),     8595 },
        { "harr".hashCode(),     8596 },
        { "crarr".hashCode(),    8629 },
        { "lArr".hashCode(),     8656 },
        { "uArr".hashCode(),     8657 },
        { "rArr".hashCode(),     8658 },
        { "dArr".hashCode(),     8659 },
        { "hArr".hashCode(),     8660 },
        { "forall".hashCode(),   8704 },
        { "part".hashCode(),     8706 },
        { "exist".hashCode(),    8707 },
        { "empty".hashCode(),    8709 },
        { "nabla".hashCode(),    8711 },
        { "isin".hashCode(),     8712 },
        { "notin".hashCode(),    8713 },
        { "ni".hashCode(),       8715 },
        { "prod".hashCode(),     8719 },
        { "sum".hashCode(),      8721 },
        { "minus".hashCode(),    8722 },
        { "lowast".hashCode(),   8727 },
        { "radic".hashCode(),    8730 },
        { "prop".hashCode(),     8733 },
        { "infin".hashCode(),    8734 },
        { "ang".hashCode(),      8736 },
        { "and".hashCode(),      8743 },
        { "or".hashCode(),       8744 },
        { "cap".hashCode(),      8745 },
        { "cup".hashCode(),      8746 },
        { "int".hashCode(),      8747 },
        { "there4".hashCode(),   8756 },
        { "sim".hashCode(),      8764 },
        { "cong".hashCode(),     8773 },
        { "asymp".hashCode(),    8776 },
        { "ne".hashCode(),       8800 },
        { "equiv".hashCode(),    8801 },
        { "le".hashCode(),       8804 },
        { "ge".hashCode(),       8805 },
        { "sub".hashCode(),      8834 },
        { "sup".hashCode(),      8835 },
        { "nsub".hashCode(),     8836 },
        { "sube".hashCode(),     8838 },
        { "supe".hashCode(),     8839 },
        { "oplus".hashCode(),    8853 },
        { "otimes".hashCode(),   8855 },
        { "perp".hashCode(),     8869 },
        { "sdot".hashCode(),     8901 },
        { "lceil".hashCode(),    8968 },
        { "rceil".hashCode(),    8969 },
        { "lfloor".hashCode(),   8970 },
        { "rfloor".hashCode(),   8971 },
        { "lang".hashCode(),     9001 },
        { "rang".hashCode(),     9002 },
        { "loz".hashCode(),      9674 },
        { "spades".hashCode(),   9824 },
        { "clubs".hashCode(),    9827 },
        { "hearts".hashCode(),   9829 },
        { "diams".hashCode(),    9830 }
    };
}
