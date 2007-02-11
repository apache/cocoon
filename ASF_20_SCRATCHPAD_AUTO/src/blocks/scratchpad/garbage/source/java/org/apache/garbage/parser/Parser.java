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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.apache.garbage.tree.*;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The <a href="http://www.experimentalstuff.com/Technologies/JavaCC">JavaCC</a>
 * generated parser for <b>Garbage</b> templates.
 * <p>
 * This class is automatically generated, so, it might be non-obvious to determine
 * how to use it. To create a new parser and parse a document, do the following:
 * </p>
 * <p>
 * <code>InputSource source = new InputSource(...);</code><br />
 * <code>Parser parser = new Parser();</code><br />
 * <code>Events events = parser.parse(source);</code><br />
 * </p>
 * <p>
 * Note that instances of this class are <b>NOT THREAD SAFE</b>, meaning that two
 * threads cannot concurrently parse two documents using the same instance, but
 * at the same time, one thread can safely call the <code>parse(...)</code> method
 * several times (the parser will automatically re-initialize its state once the
 * method is invoked).
 * </p>
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 */
public class Parser implements Locator, ParserTables, ParserConstants {

  /** Our local InputSource */
  private InputSource source = null;

  /**
   * Create a new instance of this <code>Parser</code>.
   */
  public Parser() {
    this((Reader)null);
  }

  /**
   * Parse a specified <code>InputSource</code> and return the
   * <code>Events</code> representing the parsed document.
   *
   * @param source The <code>InputSource</code> to parse.
   * @throws SAXException In the source cannot be accessed or parsed.
   * @throws SAXParseException If an exception occurred parsing the source.
   * @throws IOException If an I/O error occurred.
   */
  public Tree parse(InputSource source)
  throws SAXException, SAXParseException, IOException {
    Tree tree = new Tree();
    this.source = source;

    if (source == null) {
      throw new SAXException("Null source specified");
    } else if (source.getCharacterStream() != null) {
      this.ReInit(source.getCharacterStream());
    } else if (source.getByteStream() != null) {
      this.ReInit(source.getByteStream());
    } else if (source.getSystemId() != null) {
      File file = new File(System.getProperty("user.dir")).getCanonicalFile();
      URL url = new URL(file.toURL(), source.getSystemId());
      InputStream in = url.openStream();
      this.ReInit(new InputStreamReader(in));
    } else {
      throw new SAXException("Cannot access source char or byte stream");
    }

    try {
      this.p_start(tree);
      this.ReInit((Reader)null);
    } catch (ParseException e) {
      if (e.currentToken != null) {
        throw new SAXParseException(e.getMessage(),
          this.getPublicId(), this.getSystemId(),
          e.currentToken.endLine, e.currentToken.endColumn, e);
      }
      throw new SAXParseException(e.getMessage(), this, e);
    } catch (TokenMgrError e) {
      throw new SAXParseException(e.getMessage(), this);
    } catch (TreeException e) {
      throw new SAXParseException(e.getMessage(), e, e);
    }

    return(tree);
  }

  /**
   * Return the public identifier for the current document event.
   *
   * @return A <code>String</code> containing the public identifier,
   *         or <b>null</b> if none is available.
   */
  public String getPublicId() {
    if (this.source != null) return(source.getPublicId());
    return(null);
  }

  /**
   * Return the system identifier for the current document event.
   *
   * @return A <code>String</code> containing the system identifier,
   *         or <b>null</b> if none is available.
   */
  public String getSystemId() {
    if (this.source != null) return(source.getSystemId());
    return(null);
  }

  /**
   * Return the line number where the current document event ends.
   *
   * @return The line number, or -1 if none is available.
   */
  public int getLineNumber() {
    if (this.token != null) return(token.endLine);
    return(-1);
  }

  /**
   * Return the column number where the current document event ends.
   *
   * @return The column number, or -1 if none is available.
   */
  public int getColumnNumber() {
    if (this.token != null) return(token.endColumn);
    return(-1);
  }

  /**
   * Return the next available token checking its kind.
   *
   * @param kind An array of integers specifying the possible kind
   *             of the next returned token.
   * @return The next available token with the correct kind.
   * @throws ParseException If the token kind doesn't match one of 
   *                        those specified.
   */
  final public Token getNextToken(int kind[])
  throws ParseException {
    Token prev_token = token;
    Token local_token = this.getNextToken();
    for (int x = 0; x < kind.length; x++) {
      if (local_token.kind == kind[x]) {
        return(local_token);
      }
    }
    int expected[][] = new int[kind.length][1];
    for (int x = 0; x < kind.length; x++) expected[x][0] = kind[x];
    throw new ParseException(prev_token, expected, tokenImage);
  }

  /**
   * Return the next available token checking its kind.
   *
   * @param kind The requited kind of the next returned token.
   * @return The next available token with the correct kind.
   * @throws ParseException If the token kind doesn't match.
   */
  final public Token getNextToken(int kind)
  throws ParseException {
    int kinds[] = { kind };
    return (this.getNextToken(kinds));
  }

  /**
   * Return the next available token in a specific lexical state
   * checking its kind.
   *
   * @param kind An array of integers specifying the possible kind
   *             of the next returned token.
   * @param state The state in which the next token should be matched.
   * @return The next available token with the correct kind.
   * @throws ParseException If the token kind doesn't match one of 
   *                        those specified.
   */
  final public Token getNextToken(int kind[], int state)
  throws ParseException {
    this.pushState(state);
    Token local_token = this.getNextToken(kind);
    this.popState();
    return(local_token);
  }

  /**
   * Return the next available token in a specific lexical state
   * checking its kind.
   *
   * @param kind The requited kind of the next returned token.
   * @param state The state in which the next token should be matched.
   * @return The next available token with the correct kind.
   * @throws ParseException If the token kind doesn't match.
   */
  final public Token getNextToken(int kind, int state)
  throws ParseException {
    int kinds[] = { kind };
    return (this.getNextToken(kinds, state));
  }

  /**
   * Generate a parser internal error exception.
   *
   * @throws ParseException Every time this method is called.
   */
  public void generateParseInternalError()
  throws ParseException {
    this.generateParseInternalError(null);
  }

  /**
   * Generate a parser internal error exception.
   *
   * @param message An optional message for the exception.
   * @throws ParseException Every time this method is called.
   */
  public void generateParseInternalError(String message)
  throws ParseException {
    if (message == null) {
      message = "Unrecoverable internal error";
    }

    if (token != null) {
      message += " at line " + token.beginLine + ", column "
                 + token.beginColumn + ".";
    } else {
      message += ".";
    }

    ParseException exception = new ParseException(message);
    exception.currentToken = token;
    exception.tokenImage = tokenImage;
    throw (exception);
  }


  /* The parser lexical-state stack */
  public int statesStk[] = new int[4096];

  /* The position parser lexical-state stack */
  int statesPos = 0;

  /**
   * Push the current lexical state in the stack and switch to new state.
   */
  private final void pushState(int state) {
    statesStk[statesPos++] = token_source.curLexState;
    token_source.SwitchTo(state);
  }

  /**
   * Pop the last lexical state from the stack and revert to it.
   */
  private final void popState() {
    int state = statesStk[--statesPos];
    token_source.SwitchTo(state);
  }

  /**
   * Convert the name of an entity reference specified as a
   * <code>String</code> to its <code>char</code> equivalent.
   *
   * @param name The entity reference name.
   * @return A <code>char</code> character.
   * @throws NumberFormatException If the specified <code>name</code> was
   *                               not found in the entities table.
   */
  protected char p_entityref_byname(String name)
  throws NumberFormatException {
    int hash = name.hashCode();
    for (int x = 0; x < entityReferences.length; x++) {
      if (hash == entityReferences[x][0]) {
        return ((char) entityReferences[x][1]);
      }
    }

    String extra = (name.length() == 0 ?" (zero length)": "");
    throw new NumberFormatException ("Invalid entity name supplied: \""
                                     + name + "\"" + extra + ".");
  }

/* ============================================================================ *
 * Main parser routines                                                         *
 * ============================================================================ */

/**
 * Starts parsing.
 */
  final public void p_start(Events events) throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case T_DOCTYPE:
      p_doctype(events);
      break;
    default:
      jj_la1[0] = jj_gen;
      ;
    }
    p_block(events);
    jj_consume_token(0);
  }

/**
 * Parse a block.
 */
  final public void p_block(Events events) throws ParseException {
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case T_CHARACTERS:
        p_characters(events);
        break;
      case T_ELEMENT_OPEN:
      case T_ELEMENT_CLOSE:
        p_element(events);
        break;
      case T_ENTITYREF:
        p_entityref(events);
        break;
      case T_COMMENT:
        p_comment(events);
        break;
      case T_CDATA:
        p_cdata(events);
        break;
      case T_PROCINSTR:
        p_procinstr(events);
        break;
      case T_EXPRESSION:
        p_expression(events);
        break;
      case T_TEMPLATE_IF:
        p_template_if(events);
        break;
      case T_TEMPLATE_FOREACH:
        p_template_foreach(events);
        break;
      case T_TEMPLATE_VARIABLE:
        p_template_variable(events);
        break;
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case T_CHARACTERS:
      case T_ELEMENT_OPEN:
      case T_ELEMENT_CLOSE:
      case T_ENTITYREF:
      case T_COMMENT:
      case T_CDATA:
      case T_PROCINSTR:
      case T_EXPRESSION:
      case T_TEMPLATE_IF:
      case T_TEMPLATE_FOREACH:
      case T_TEMPLATE_VARIABLE:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_1;
      }
    }
  }

/* ---------------------------------------------------------------------------- */

/**
 * Parse characters outside of any tag, element...
 */
  final public void p_characters(Events events) throws ParseException {
    jj_consume_token(T_CHARACTERS);
      events.append(new Characters(this, token.image));
  }

/* ---------------------------------------------------------------------------- */

/**
 * Parse a <code>&lt;!DOCTYPE ...&gt;</code> declaration.
 */
  final public void p_doctype(Events events) throws ParseException {
  String name = null;
  String system_id = null;
  String public_id = null;
    jj_consume_token(T_DOCTYPE);
    jj_consume_token(T_DOCTYPE_NAME);
    name = token.image;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case T_DOCTYPE_SYSTEM:
    case T_DOCTYPE_PUBLIC:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case T_DOCTYPE_SYSTEM:
        jj_consume_token(T_DOCTYPE_SYSTEM);
      system_id = p_doctype_id(DOCTYPE_SYSTEM);
        break;
      case T_DOCTYPE_PUBLIC:
        jj_consume_token(T_DOCTYPE_PUBLIC);
      public_id = p_doctype_id(DOCTYPE_PUBLIC);
      system_id = p_doctype_id(DOCTYPE_SYSTEM);
        break;
      default:
        jj_la1[3] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[4] = jj_gen;
      ;
    }
    jj_consume_token(T_DOCTYPE_END);
    events.append(new DocType(this, name, public_id, system_id));
  }

/**
 * Parse the SYSTEM or PUBLIC id in a <code>&lt;!DOCTYPE ...&gt;</code>
 * declaration.
 *
 * @param state Either DOCTYPE_SYSTEM or DOCTYPE_PUBLIC
 */
  final public String p_doctype_id(int state) throws ParseException {
  Token local = null;
  int kind = -1;

  switch (state) {
    case DOCTYPE_SYSTEM: kind = T_DOCTYPE_SYSTEM_DATA; break;
    case DOCTYPE_PUBLIC: kind = T_DOCTYPE_PUBLIC_DATA; break;
    default: generateParseInternalError("Invalid state " + state + "specified");
  }
    jj_consume_token(T_DOCTYPE_S);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case T_DOCTYPE_QUOT:
      jj_consume_token(T_DOCTYPE_QUOT);
      break;
    case T_DOCTYPE_APOS:
      jj_consume_token(T_DOCTYPE_APOS);
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      StringBuffer value  = new StringBuffer();
      int start_kind = token.kind;
      int expect[] = { kind, T_DOCTYPE_QUOT, T_DOCTYPE_APOS };

      pushState(state);
      local = getNextToken(expect);
      while (local.kind != start_kind) {
        value.append(local.image);
        local = getNextToken(expect);
      }
      popState();

      {if (true) return(value.toString());}
    throw new Error("Missing return statement in function");
  }

/* ---------------------------------------------------------------------------- */

/**
 * Parse an element (<code>&lt;name ...&gt;</code>, <code>&lt;name
 * .../&gt;</code> or <code>&lt;/name &gt;</code>) declaration.
 */
  final public void p_element(Events events) throws ParseException {
  ElementStart element = null;
  String name = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case T_ELEMENT_CLOSE:
      jj_consume_token(T_ELEMENT_CLOSE);
      events.append(new ElementEnd(this, token.image.substring(2)));
      jj_consume_token(T_ELEMENT_END);
      break;
    case T_ELEMENT_OPEN:
      jj_consume_token(T_ELEMENT_OPEN);
      name = token.image.substring(1);
      element = new ElementStart(this, name);
      label_2:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case T_ATTRIBUTE:
          ;
          break;
        default:
          jj_la1[6] = jj_gen;
          break label_2;
        }
        p_attribute(element);
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case T_ELEMENT_END:
        jj_consume_token(T_ELEMENT_END);
        break;
      case T_ELEMENT_SINGLE:
        jj_consume_token(T_ELEMENT_SINGLE);
        break;
      default:
        jj_la1[7] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      events.append(element);
      if (token.kind == T_ELEMENT_SINGLE) {
          events.append(new ElementEnd(this, name));
      }
      break;
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

/**
 * Parse an attribute (<code>name = &quot;...&quot;</code>) inside an element
 * declaration.
 */
  final public void p_attribute(ElementStart element) throws ParseException {
  String name = null;
  Attribute attribute = null;
    jj_consume_token(T_ATTRIBUTE);
    attribute = new Attribute(this, token.image.trim());
    p_attribute_data(attribute);
    element.put(attribute);
    token_source.SwitchTo(ELEMENT);
  }

/**
 * Parse the value of an element attribute.
 */
  final public void p_attribute_data(Attribute attribute) throws ParseException {
  Token local = null;
    jj_consume_token(T_ATTRIBUTE_EQUALS);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case T_ATTRIBUTE_QUOT:
      jj_consume_token(T_ATTRIBUTE_QUOT);
      break;
    case T_ATTRIBUTE_APOS:
      jj_consume_token(T_ATTRIBUTE_APOS);
      break;
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      int start_kind = token.kind;
      int expect[] = {
        T_ATTRIBUTE_DATA, T_ATTRIBUTE_QUOT, T_ATTRIBUTE_APOS,
        T_ATTRIBUTE_EREF, T_ATTRIBUTE_EXPR
      };

      pushState(ATTRIBUTE_DATA);
      local = getNextToken(expect);
      while (local.kind != start_kind) {
        switch (local.kind) {

          case T_ATTRIBUTE_EREF:
            pushState(ENTITYREF);
            attribute.append(new Characters(p_entityref_data()));
            popState();
            break;

          case T_ATTRIBUTE_EXPR:
            pushState(EXPRESSION);
            attribute.append(new Expression(p_expression_data()));
            popState();
            break;

          default:
            attribute.append(new Characters(local.image));
            break;
        }
        local = getNextToken(expect);
      }
      popState();
  }

/* ---------------------------------------------------------------------------- */

/**
 * Parse an entity reference (<code>&amp;...;</code>) declaration.
 */
  final public void p_entityref(Events events) throws ParseException {
  char value = 0;
    jj_consume_token(T_ENTITYREF);
    /* Push the state manually, as we can come from different states */
    pushState(ENTITYREF);
    value = p_entityref_data();
    events.append(new Characters(this, value));
    popState();
  }

/**
 * Parse the value of an entity reference and return its character value.
 */
  final public char p_entityref_data() throws ParseException {
  int value = -1;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case T_ENTITYREF_NUM:
      jj_consume_token(T_ENTITYREF_NUM);
      break;
    case T_ENTITYREF_HEX:
      jj_consume_token(T_ENTITYREF_HEX);
      break;
    case T_ENTITYREF_NAME:
      jj_consume_token(T_ENTITYREF_NAME);
      break;
    default:
      jj_la1[10] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    try {
      switch (token.kind) {
        case T_ENTITYREF_NUM:
          value = Integer.parseInt(token.image.substring(1));
          break;
        case T_ENTITYREF_HEX:
          value = Integer.parseInt(token.image.substring(2), 16);
          break;
        case T_ENTITYREF_NAME:
          value = p_entityref_byname(token.image);
          break;
      }
    } catch (NumberFormatException e) {
      value = -1;
    }

    if ((value < Character.MIN_VALUE) || (value > Character.MAX_VALUE)) {
      this.generateParseInternalError("Invalid entity reference &"
          + token.image + ";");
    }
    jj_consume_token(T_ENTITYREF_END);
    {if (true) return((char)value);}
    throw new Error("Missing return statement in function");
  }

/* ---------------------------------------------------------------------------- */

/**
 * Parse a comment (<code>&lt;!-- ... --&gt;</code>) section.
 */
  final public void p_comment(Events events) throws ParseException {
  String data = "";
    jj_consume_token(T_COMMENT);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case T_COMMENT_DATA:
      jj_consume_token(T_COMMENT_DATA);
      data = token.image;
      break;
    default:
      jj_la1[11] = jj_gen;
      ;
    }
    jj_consume_token(T_COMMENT_END);
    events.append(new Comment(this, data));
  }

/* ---------------------------------------------------------------------------- */

/**
 * Parse a <code>&lt;[CDATA[ ... ]&gt;</code> section.
 */
  final public void p_cdata(Events events) throws ParseException {
  String data = "";
    jj_consume_token(T_CDATA);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case T_CDATA_DATA:
      jj_consume_token(T_CDATA_DATA);
      data = token.image;
      break;
    default:
      jj_la1[12] = jj_gen;
      ;
    }
    jj_consume_token(T_CDATA_END);
    events.append(new CData(this, data));
  }

/* ---------------------------------------------------------------------------- */

/**
 * Parse a Processing Instruction (<code>&lt;?name ...?&gt;</code>).
 */
  final public void p_procinstr(Events events) throws ParseException {
  String target = null;
  String data = null;
    jj_consume_token(T_PROCINSTR);
    jj_consume_token(T_PROCINSTR_TARGET);
    target = token.image;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case T_PROCINSTR_SEP:
      jj_consume_token(T_PROCINSTR_SEP);
      jj_consume_token(T_PROCINSTR_DATA);
      data = token.image;
      break;
    default:
      jj_la1[13] = jj_gen;
      ;
    }
    jj_consume_token(T_PROCINSTR_END);
    events.append(new ProcessingInstruction(this, target, data));
  }

/* ---------------------------------------------------------------------------- */

/**
 * Parse an Expression (<code>{...}</code>).
 */
  final public void p_expression(Events events) throws ParseException {
  String expression = null;
    jj_consume_token(T_EXPRESSION);
    pushState(EXPRESSION);
    expression = p_expression_data();
    events.append(new Expression(this, expression));
    popState();
  }

/**
 * Parse the data contained in an Expression.
 */
  final public String p_expression_data() throws ParseException {
  String expression = null;
    jj_consume_token(T_EXPRESSION_DATA);
    expression = token.image;
    jj_consume_token(T_EXPRESSION_END);
    {if (true) return(expression);}
    throw new Error("Missing return statement in function");
  }

/**
 * Parse a template foreach block (<code>&quot;#foreach...#end</code>).
 */
  final public void p_template_foreach(Events events) throws ParseException {
  TemplateFor event = null;
    jj_consume_token(T_TEMPLATE_FOREACH);
    pushState(EXPRESSION);
    event = new TemplateFor(this, p_expression_data());
    popState();
    p_block(event);
    jj_consume_token(T_TEMPLATE_END);
    events.append(event);
  }

/**
 * Parse a template variable declaration (<code>&quot;#$name = {...}</code>).
 */
  final public void p_template_variable(Events events) throws ParseException {
  String variable = null;
  String expression = null;
    jj_consume_token(T_TEMPLATE_VARIABLE);
    variable = token.image.substring(2, token.image.indexOf('=')).trim();
    pushState(EXPRESSION);
    expression = p_expression_data();
    popState();

    events.append(new TemplateVar(this, variable, expression));
    System.err.println("Processed #$" + variable + "={" + expression + "}");
  }

/**
 * Parse a template if block (<code>&quot;#if...#end</code>).
 */
  final public void p_template_if(Events events) throws ParseException {
  String expression = null;
  TemplateIf event = new TemplateIf(this);
    jj_consume_token(T_TEMPLATE_IF);
    pushState(EXPRESSION);
    expression = p_expression_data();
    popState();
    p_block(event.addCondition(this, expression));
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case T_TEMPLATE_ELIF:
        ;
        break;
      default:
        jj_la1[14] = jj_gen;
        break label_3;
      }
      jj_consume_token(T_TEMPLATE_ELIF);
      pushState(EXPRESSION);
      expression = p_expression_data();
      popState();
      p_block(event.addCondition(this, expression));
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case T_TEMPLATE_ELSE:
      jj_consume_token(T_TEMPLATE_ELSE);
      p_block(event.addCondition(this));
      break;
    default:
      jj_la1[15] = jj_gen;
      ;
    }
    jj_consume_token(T_TEMPLATE_END);
    events.append(event);
  }

  public ParserTokenManager token_source;
  JavaCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[16];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_0();
      jj_la1_1();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x1000,0xc00800,0xc00800,0x18000,0x18000,0xc0000,0x4000000,0x3000000,0xc00000,0x30000000,0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x0,0x2921242,0x2921242,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x38,0x100,0x800,0x4000,0x200000,0x400000,};
   }

  public Parser(java.io.InputStream stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new ParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public Parser(java.io.Reader stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new ParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public Parser(ParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public void ReInit(ParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[58];
    for (int i = 0; i < 58; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 16; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 58; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

}
