/*-- $Id: Tokenizer.java,v 1.2 2000-03-20 21:14:16 stefano Exp $ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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

package org.apache.cocoon;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Replacement for StringTokenizer in java.util, beacuse of bug in the
 * Sun's implementation.
 *
 * @author <A HREF="mailto:moravek@pobox.sk">Peter Moravek</A>
 */
public class Tokenizer implements Enumeration {

  /**
   * Constructs a string tokenizer for the specified string. All characters
   * in the delim argument are the delimiters for separating tokens.
   * If the returnTokens flag is true, then the delimiter characters are
   * also returned as tokens. Each delimiter is returned as a string of
   * length one. If the flag is false, the delimiter characters are skipped
   * and only serve as separators between tokens.
   *
   * @param str           a string to be parsed
   * @param delim         the delimiters
   * @param returnTokens  flag indicating whether to return the delimiters
   *                      as tokens
   */
  public Tokenizer(String str, String delim, boolean returnTokens) {
    this.str = str;
    this.delim = delim;
    this.returnTokens = returnTokens;

    max = str.length();
  }

  /**
   * Constructs a string tokenizer for the specified string. The characters
   * in the delim argument are the delimiters for separating tokens.
   * Delimiter characters themselves will not be treated as tokens.
   *
   * @param str          a string to be parsed
   * @param delim        the delimiters
   */
  public Tokenizer(String str, String delim) {
    this(str, delim, false);
  }

  /**
   * Constructs a string tokenizer for the specified string. The character
   * in the delim argument is the delimiter for separating tokens.
   * Delimiter character themselves will not be treated as token.
   *
   * @param str          a string to be parsed
   * @param delim        the delimiter
   */
  public Tokenizer(String str, char delim) {
    this(str, String.valueOf(delim), false);
  }

  /**
   * Constructs a string tokenizer for the specified string. The tokenizer
   * uses the default delimiter set, which is " \t\n\r\f": the space
   * character, the tab character, the newline character, the carriage-return
   * character, and the form-feed character. Delimiter characters themselves
   * will not be treated as tokens.
   *
   * @param str          a string to be parsed
   */
  public Tokenizer(String str) {
    this(str, DEFAULT_DELIMITERS, false);
  }

  /**
   * Tests if there are more tokens available from this tokenizer's string.
   * If this method returns true, then a subsequent call to nextToken with
   * no argument will successfully return a token.
   *
   * @return true if and only if there is at least one token in the string
   * after the current position; false otherwise.
   */
  public boolean hasMoreTokens() {
    return ((current < max) ? (true) : 
      (((current == max) && (max == 0 
        || (returnTokens && delim.indexOf(str.charAt(previous)) >= 0)))));
  }
  
  /**
   * Returns the next token from this string tokenizer.
   *
   * @return the next token from this string tokenizer
   *
   * @exception NoSuchElementException  if there are no more tokens in this
   *                                    tokenizer's string
   */
  public String nextToken() throws NoSuchElementException {
    if (current == max
      && (max == 0
      || (returnTokens && delim.indexOf(str.charAt(previous)) >= 0))) {

      current++;
      return new String();
    }

    if (current >= max)
      throw new NoSuchElementException();

    int start = current;
    String result = null;

    if (delim.indexOf(str.charAt(start)) >= 0) {
      if (previous == -1 || (returnTokens && previous != current
        && delim.indexOf(str.charAt(previous)) >= 0)) {

        result = new String();
      }
      else if (returnTokens)
        result = str.substring(start, ++current);

      if (!returnTokens)
        current++;
    }

    previous = start;
    start = current;

    if (result == null)
      while (current < max && delim.indexOf(str.charAt(current)) < 0)
        current++;

    return result == null ? str.substring(start, current) : result;
  }

  /**
   * Returns the next token in this string tokenizer's string. First, the
   * set of characters considered to be delimiters by this Tokenizer
   * object is changed to be the characters in the string delim.
   * Then the next token in the string after the current position is
   * returned. The current position is advanced beyond the recognized token.
   * The new delimiter set remains the default after this call.
   *
   * @param delim the new delimiters
   *
   * @return the next token, after switching to the new delimiter set
   *
   * @exception NoSuchElementException  if there are no more tokens in this
   *                                    tokenizer's string.
   */
  public String nextToken(String delim) throws NoSuchElementException {
    this.delim = delim;
    return nextToken();
  }

  /**
   * Returns the same value as the hasMoreTokens method. It exists so that
   * this class can implement the Enumeration interface.
   *
   * @return true if there are more tokens; false otherwise.
   */
  public boolean hasMoreElements() {
    return hasMoreTokens();
  }

  /**
   * Returns the same value as the nextToken method, except that its
   * declared return value is Object rather than String. It exists so that
   * this class can implement the Enumeration interface.
   *
   * @return the next token in the string
   *
   * @exception NoSuchElementException  if there are no more tokens in this
   *                                    tokenizer's string
   */
  public Object nextElement() {
    return nextToken();
  }

  /**
   * Calculates the number of times that this tokenizer's nextToken method
   * can be called before it generates an exception. The current position
   * is not advanced.
   *
   * @return  the number of tokens remaining in the string using the
   *          current delimiter set
   */
  public int countTokens() {
    int curr = current;
    int count = 0;

    for (int i = curr; i < max; i++) {
      if (delim.indexOf(str.charAt(i)) >= 0)
        count++;

      curr++;
    }

    return count + (returnTokens ? count : 0) + 1;
  }

  /**
   * Resets this tokenizer's state so the tokenizing starts from the begin.
   */
  public void reset() {
    previous = -1;
    current = 0;
  }

  /**
   * Constructs a string tokenizer for the specified string. All characters
   * in the delim argument are the delimiters for separating tokens.
   * If the returnTokens flag is true, then the delimiter characters are
   * also returned as tokens. Each delimiter is returned as a string of
   * length one. If the flag is false, the delimiter characters are skipped
   * and only serve as separators between tokens. Then tokenizes the str
   * and return an String[] array with tokens.
   *
   * @param str           a string to be parsed
   * @param delim         the delimiters
   * @param returnTokens  flag indicating whether to return the delimiters
   *                      as tokens
   *
   * @return array with tokens
   */
  public static String[] tokenize(String str, String delim,
    boolean returnTokens) {

    Tokenizer tokenizer = new Tokenizer(str, delim, returnTokens);
    String[] tokens = new String[tokenizer.countTokens()];

    for (int i = 0; i < tokens.length; i++)
      tokens[i] = tokenizer.nextToken();

    return tokens;
  }

  /**
   * Default delimiters "\t\n\r\f":
   * the space character, the tab character, the newline character,
   * the carriage-return character, and the form-feed character.
   */
  public static final String DEFAULT_DELIMITERS = " \t\n\r\f";

  /**
   * String to tokenize.
   */
  private String str = null;

  /**
   * Delimiters.
   */
  private String delim = null;

  /**
   * Flag indicating whether to return the delimiters as tokens.
   */
  private boolean returnTokens = false;

  /**
   * Previous token start.
   */
  private int previous = -1;

  /**
   * Current position in str string.
   */
  private int current = 0;

  /**
   * Maximal position in str string.
   */
  private int max = 0;
}


