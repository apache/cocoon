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

package org.apache.cocoon.components.flow.javascript.fom;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.environment.Environment;
import org.apache.excalibur.source.Source;
import org.apache.cocoon.matching.helpers.WildcardHelper;


/**
 * <p>
 * The <code>JavaScriptAspectWeaver</code> provides functionality to intercept 
 * JavaScript functions
 * </p>
 * <p>
 * Known restrictions, to be implemented, open questions:<br/>
 * <ul>
 *  <li>Add interception support for scripts loaded by 
 *      <code>"cocoon.load( uri, aspectFiles[] )"</code>
 *      <br/>
 *      So we have to find the best solution to provide access to a configured
 *      <code>JavaScriptAspectWeaver</code> within the cocoon.load
 *      method. Possible solutions:
 *      <ul>
 *        <li>Convert the <code>JavaScriptAspectWeaver</code> to a
 *            regular Avalon component which is configureable itself.</li>
 *        <li>Add the <code>JavaScriptAspectWeaver</code> to the
 *            <code>setup()</code> method of <code>AO_FOM_Cocoon</code></li>
 *      </ul>
 *      (RP) I prefer the second possibility to avoid another component
 *  </li>
 *  <li>the result script should be pretty printed
 *      and put into the directory of the base script
 *      if file protocol is used
 *      --&gt;enables easier debugging</li>
 *  <li>after() interceptions have to be added in reverse order, haven't they?</li>
 *  <li>add support for object property functions</li>
 *  <li>how to deal with more than one around interception?</li>
 *  <li>if applied scripts change they are not reloaded (a change in
        the base script is necessary)</li>
 *  <li>add syntax check for base script</li>
 *  <li>add syntax check for result scripts</li>
 *  <li>add syntax check for interception definitions</li>
 *  <li>pass the calling function name to the interception scripts</li>
 *  <li>review the naming of all classes and methods</li>
 *  <li>What's the purpose of the arguments in continueExecution(arguments)?
 *       See Stefano's proposal?</li>
 * </ul>
 * 
 * @author <a href="mailto:reinhard@apache.org">Reinhard Pötz</a> 
 * @since Sept, 2003
 * @version CVS $Id: JavaScriptAspectWeaver.java,v 1.9 2003/12/23 15:28:32 joerg Exp $
 */
public class JavaScriptAspectWeaver extends AbstractLogEnabled {
    
    /** All Interceptors in the right order */
    ArrayList interceptorGroups = new ArrayList();
    
    /** The Cocoon environment needed for SourceResolving */
    Environment environement = null;
    
    /** If debugging is true, the intercepted script is writen to filesystem */
    boolean serializeResultScript = false;

    /** Base script <code>org.apache.excalibur.source.Source</code>*/
    Source source = null; 
    
    /** The javascript repsented in <code>JSToken</code>*/
    JSTokenList baseScriptTokenList = null; 
    
    /** Does this base script contain applied scripts? */
    boolean areScriptsApplied = false;
    
    /** Provided configuration (part of the Interpreter configuration) **/
    ArrayList stopExecutionFunctions = null;

    /**
     * Set the base script (the script which is scanned for applied
     * intercepting scripts) and if scripts are applied those are
     * scanned the code is added to <code>interceptorGroups</code>
     */
    public void setBaseScript( Source source ) throws IOException {
        
        this.source = source;
        
        // create tokens for javascript code
        this.baseScriptTokenList = JSParser.parse( 
            readSourceIntoCharArray( source.getInputStream() ) );
            
        // read out all interceptor sources
        List scriptsApplied = baseScriptTokenList.getScriptsApplied();
        
        // set all interceptors   
        for( int i = 0; i < scriptsApplied.size(); i++ ) {
            this.addInterceptorGroup( (String) scriptsApplied.get(i) );
            areScriptsApplied = true;
        }       
    }
    
    /**
     * Get the intercepted base script (all interceptions found in 
     * cocoon.apply(..) are added to the script.
     */
    public Reader getInterceptedScriptAsReader() throws Exception {
        
        // comment out all cocoon.apply(..) parts
        this.baseScriptTokenList.commentScriptsApplied();
       
        // add interception events to make parsing easier
        this.baseScriptTokenList.addInterceptionEvents( this.stopExecutionFunctions );        
       
        // replace return statements with variable defintions and put
        // it to the end of the function
        this.baseScriptTokenList.replaceReturnStatements();

        // add the interceptions
        this.baseScriptTokenList.addInterceptions( this.interceptorGroups );

        // pretty print script
        // TODO tbd 

        // logging
        this.getLogger().info( "\n\n" + this.baseScriptTokenList.toString() + "\n" );
        
        // create a file to make debugging easier
        if( serializeResultScript ) {
            this.baseScriptTokenList.writeToFile( this.source );            
        }

        // return the intercepted script        
        return this.baseScriptTokenList.getScriptAsReader();       
    }

    /**
     * Add a group of interceptor tokens to the AspectWeaver. Note that the
     * order is important if more interceptions match. (The first applied
     * script is added first, ...)
     */
    protected void addInterceptorGroup( String source  ) 
        throws IOException {
        
        this.getLogger().info( "applied script: " + source );        
        JSTokenList interceptorsTokensList = JSParser.parse( 
            readSourceIntoCharArray( this.environement.resolveURI(source).getInputStream() ) );

        InterceptionList interceptors = interceptorsTokensList.readInterceptionTokens();
        interceptors.setSourceScript( source );
        for( int i = 0; i < interceptors.size(); i++ ) {
            Interceptor interceptor = (Interceptor) interceptors.get( i );
            // TODO logging here
            this.getLogger().info( "added Interceptor name[" + interceptor.getName() + 
                "], type[" + interceptor.getType() + "] value[" + new String(interceptor.stream()) + "]" );
        }
        interceptorGroups.add( interceptors );
    }
    
    /**
     * The access to the Cocoon environemnt 
     */
    public void setEnvironment( Environment env ) {
        this.environement = env;
    }

    /**
     * Should the JavaScriptAspectWeaver write the result script
     * into separate file in the same directory as the basescript?
     */
    public void setSerializeResultScriptParam( boolean serialize ) {
        this.serializeResultScript = serialize;
    }
    
    /** 
     * Provide configuration (part of the Interpreter configuration) 
     */    
    public void setStopExecutionFunctionsConf( Configuration conf ) throws ConfigurationException {
        this.stopExecutionFunctions = new ArrayList();
        Configuration stopExecutionFunctionsConf[] = conf.getChildren();
        for( int i = 0; i < stopExecutionFunctionsConf.length; i++ ) {
            stopExecutionFunctions.add( stopExecutionFunctionsConf[i].getValue() );
        }
    }
    
    /**
     * Reset the variable containing all interceptions
     *
     */
    protected void clearInterceptorGroups() {
        interceptorGroups.clear();
    }

    /**
     * Convert an input stream into an array of <code>char</code>
     */
    public static char[] readSourceIntoCharArray( InputStream is ) 
        throws IOException {
            
        BufferedReader br = new BufferedReader( new InputStreamReader(is) );
        char[] b = new char[1024];
        int n;
        char charComplete[] = new char[0];
    
        while( (n = br.read(b)) > 0) {
            char copy[] = new char[charComplete.length + n];
            System.arraycopy( charComplete, 0, copy, 0, charComplete.length);
            System.arraycopy( b, 0, copy, charComplete.length, n );
            charComplete = copy;
        }
        return charComplete;  
    }    
    
    public boolean areScriptsApplied() {
        return this.areScriptsApplied;
    }

    /**
     * parse JavaScript and get a <code>JSTokenList</code>
     */
    static class JSParser {

        public static int SPACES_FOR_TAB          =  4;           
            
        public static int AT_START                =  0;
        public static int IN_COMMENT              =  1;
        public static int IN_LINECOMMENT          =  2;    
        public static int IN_LF                   =  3;
        public static int IN_WHITESPACE           =  4;   
        public static int IN_CODE                 =  5;
        public static int IN_CODE_LIT             =  6; 
        public static int IN_DOUBLE_QUOTES        =  7;
        public static int IN_SINGLE_QUOTES        =  8;
        public static int IN_REGEXP               =  9;    

        /**
         * Parse an array of <code>char</code> and get a list
         * of <code>JSToken</code> objects within a <code>JSTokensList</code>
         */
        public static JSTokenList parse( char[] c ) {
            int parsingState = AT_START;
            JSTokenList tokenList = new JSTokenList();
            JSToken curToken = null;
        
            // add start token
            // tokenList.add( new JSToken( JSToken.START ) );
        
            // parse char array and create a list with all tokens
            for( int i = 0; i < c.length; i++ ) {
                char thisChar = c[i];
                char nextChar = 0;
                if( i < c.length - 1 ) nextChar = c[i+1];
            
                if( parsingState == IN_COMMENT ) {
                    if( thisChar == '*' && nextChar == '/' ) {
                        curToken.append('*').append('/');
                        tokenList.add( curToken.getClone() );
                        parsingState = AT_START;
                        i++;
                        continue;
                    }
                    else {
                        curToken.append( thisChar );
                        continue;   
                    }
                }
                else if( parsingState == IN_LINECOMMENT ) {
                   if( thisChar == '\n' ) {
                       curToken.append( thisChar );
                       if( nextChar == '\r' ) {
                           // don't append win LF characters
                           i++;
                       }
                       tokenList.add( curToken.getClone() );
                       parsingState = AT_START;
                   }
                   else {
                        curToken.append( thisChar );   
                   }
                }
                else if( parsingState == IN_CODE ) {            
                    if(! isJSIdentifierPartCharacter( thisChar ) ) {
                        tokenList.add( curToken.getClone() );
                        parsingState = AT_START;
                    }
                    else {
                        curToken.append( thisChar );                    
                    }
                }
            
                else if( parsingState == IN_WHITESPACE ) {
                    if( thisChar != ' ' || thisChar != '\t' ) {
                        tokenList.add( curToken.getClone() );
                        parsingState = AT_START;
                    }
                    else if( thisChar == ' ' ) {
                        curToken.append( thisChar );                    
                    }
                    else if( thisChar == '\t' ) {
                        for( int count = 1; count <= SPACES_FOR_TAB; count++ ) {
                            curToken.append( ' ' );
                        }
                    }
                }
                else if( parsingState == IN_SINGLE_QUOTES ) {
                    curToken.append( thisChar );
                    if( thisChar == '\'' ) {
                        tokenList.add( curToken.getClone() );
                        parsingState = AT_START;
                        continue;
                    }
                }
                else if( parsingState == IN_DOUBLE_QUOTES ) {
                    curToken.append( thisChar );
                    if( thisChar == '\"' ) {
                        tokenList.add( curToken.getClone() );
                        parsingState = AT_START;
                        continue;
                    }
                }   
                else if( parsingState == IN_REGEXP ) {
                    curToken.append( thisChar );
                    if( thisChar == '/' ) {
                        tokenList.add( curToken.getClone() );
                        parsingState = AT_START;
                        continue;   
                    }         
                }

                // at start or token has been finished
                if( parsingState == AT_START ) {          
                    // check for comments
                    if( thisChar == '/' && nextChar == '*' ) {
                        JSToken t = new JSToken( JSToken.COMMENT );
                        curToken = t.append('/').append('*');
                        parsingState = IN_COMMENT;
                        i++;
                        continue;
                    }
                    else if( thisChar == '/' && nextChar == '/' ) {                
                        JSToken t = new JSToken( JSToken.LINE_COMMENT );
                        curToken = t.append( thisChar ).append( nextChar );
                        parsingState = IN_LINECOMMENT;
                        i++;
                        continue;                    
                    }
                    else if( isJSIdentifierStartCharacter( thisChar ) ) {
                        JSToken t = new JSToken( JSToken.CODE );
                        curToken = t.append( thisChar );
                        parsingState = IN_CODE;
                        continue;
                    }
                    else if( thisChar == ' ' ) {
                        JSToken t = new JSToken( JSToken.WHITESPACE );
                        curToken = t.append( thisChar );
                        parsingState = IN_WHITESPACE;   
                        continue;
                    }  
                    else if( thisChar == '\t' ) {
                        JSToken t = new JSToken( JSToken.WHITESPACE );
                        curToken = t;
                        for( int count = 1; count <= SPACES_FOR_TAB; count++ ) {
                            curToken.append( ' ' );
                        }
                        parsingState = IN_WHITESPACE;
                        continue;   
                    }
                    else if( thisChar == '\"' ) {
                        JSToken t = new JSToken( JSToken.CODE_LITERAL );
                        curToken = t.append( thisChar );
                        parsingState = IN_DOUBLE_QUOTES;
                        continue;
                    }
                    else if( thisChar == '\'' ) {
                        JSToken t = new JSToken( JSToken.CODE_LITERAL );
                        curToken = t.append( thisChar );
                        parsingState = IN_SINGLE_QUOTES;
                        continue;
                    }  
                    else if( thisChar == '\r' ) {
                        // jump over win LFs
                        parsingState = AT_START;
                        continue;                           
                    }
                    else if( thisChar == '\n' ) {
                        JSToken t = new JSToken( JSToken.LF );
                        curToken = t.append( thisChar );
                        if( nextChar == '\r' ) {
                            // t.append( nextChar );
                            i++;   
                        }
                        tokenList.add( t.getClone() );
                        parsingState = AT_START;
                        continue;
                    }   
                    else if( thisChar == '/' ) {
                        JSToken t = new JSToken( JSToken.REGEXP );
                        curToken = t.append( thisChar );
                        parsingState = IN_REGEXP;
                        continue;
                    }        
                
                    // single character strings
                    else if( thisChar == '(' ) {
                        JSToken t = new JSToken( JSToken.BRACKET_LEFT );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;  
                    }   
                    else if( thisChar == ')' ) {
                        JSToken t = new JSToken( JSToken.BRACKET_RIGHT );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;  
                    }    
                    else if( thisChar == '[' ) {
                        JSToken t = new JSToken( JSToken.BRACKET1_LEFT );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;  
                    }       
                    else if( thisChar == ']' ) {
                        JSToken t = new JSToken( JSToken.BRACKET1_RIGHT );
                        curToken = t.append( thisChar );
                        parsingState = AT_START;   
                        continue;  
                    }   
                    else if( thisChar == '{' ) {
                        JSToken t = new JSToken( JSToken.BRACKET2_LEFT );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;  
                    }   
                    else if( thisChar == '}' ) {
                        JSToken t = new JSToken( JSToken.BRACKET2_RIGHT );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;  
                    }       
                    else if( thisChar == '.' ) {
                        JSToken t = new JSToken( JSToken.POINT );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;  
                    }  
                    else if( thisChar == ';' ) {
                        JSToken t = new JSToken( JSToken.SEMICOLON );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;  
                    }    
                    else if( thisChar == '=' ) {
                        JSToken t = new JSToken( JSToken.EQUAL_SIGN );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;  
                    }   
                    else if( thisChar == ',' ) {
                        JSToken t = new JSToken( JSToken.COMMA );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;  
                    }     
                    else if( thisChar == ':' ) {
                        JSToken t = new JSToken( JSToken.COLON );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;                      
                    }    
                    else if( thisChar == '*' ) {
                        JSToken t = new JSToken( JSToken.ASTERISK );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;                      
                    }                         
                    else {
                        JSToken t = new JSToken( JSToken.UNKNOWN );
                        tokenList.add( t.append( thisChar ) );
                        parsingState = AT_START;   
                        continue;                       
                    }    
                         
                }  
            }
            return tokenList;
        }  

        /**
         * Check if a character is a valid JavaScript identifier
         * character.
         * 
         * TODO implement it according the ECMA spec
         */
        private static boolean isJSIdentifierPartCharacter( char c ) {
            if( Character.isJavaIdentifierPart( c ) && 
                    c != '(' &&
                    c != ')' &&
                    c != '[' &&
                    c != ']' &&
                    c != '{' &&
                    c != '}') {
                return true;
            }                   
            return false;
        }

        /**
         * Check if a character is a valid *starting* JavaScript identifier
         * character.
         *  
         * TODO implement it according the ECMA spec
         */    
        private static boolean isJSIdentifierStartCharacter( char c ) {
            if( Character.isJavaIdentifierStart( c ) ) return true;
            return false;
        }        

    }

    /**
     * A JavaScript file represented as linked list of tokens. It
     * has serveral methods implemented to get information (e.g. names
     * of scripts applied) or to change the content (e.g. comment out all
     * occurencies of <code>cocoon.apply( .. )</code>)
     */
    static class JSTokenList extends LinkedList {

        public static String RETURN_VARIABLE = "____interceptionReturn____"; 

        /**
         * Token ids of all cocoon object occurencies followed by '.apply'
         * ( cocoon.apply( "bla" ); )
         */
        ArrayList cocoonPositions = new ArrayList();   
        
        /**
         * List of all functions that lead to new continuations
         */
        List stopExecutionFunctions = null;

        
        /**
         * Add the code fragement of the passed interceptions at the right places
         */
        protected void addInterceptions( ArrayList interceptionsList ) {
                                           
            ListIterator li = this.listIterator();
            boolean inAround = false;
            while( li.hasNext() ) {
                Object o = li.next();
                // remove all tokens if there is an "AROUND" interceptors
                if( inAround ) {
                    li.remove();
                }
                if( o instanceof InterceptorEvent ) {
                    InterceptorEvent ie = (InterceptorEvent) o;
                    int type = ie.getType();
                    if( type == InterceptorEvent.FNC_START ) {
                        // add all before interceptors  
                        addInterceptionTokens( interceptionsList, 
                                               ie.getName(), 
                                               Interceptor.STR_BEFORE, 
                                               li );
                        // add all around interceptors   
                        inAround = addInterceptionTokens( interceptionsList, 
                                                          ie.getName(), 
                                                          Interceptor.STR_AROUND, 
                                                          li );
                    }
                    else if( type == InterceptorEvent.FNC_END ) {
                        // add all after interceptors
                        addInterceptionTokens( interceptionsList, 
                                               ie.getName(), 
                                               Interceptor.STR_AFTER, 
                                               li );                        
                        inAround = false;
                    }
                    else if( type == InterceptorEvent.CONT_EXEC ) {
                        addInterceptionTokens( interceptionsList, 
                                               ie.getName(), 
                                               Interceptor.STR_CONT_EXEC, 
                                               li );                           
                    }
                    else if( type == InterceptorEvent.STOP_EXEC ) {
                        addInterceptionTokens( interceptionsList, 
                                               ie.getName(), 
                                               Interceptor.STR_STOP_EXEC, 
                                               li );                               
                    }                    
                }
            }
        }
        
        public void writeToFile( Source source ) throws Exception {
            if( source.getScheme().equals( "file" ) ) {
                String filename = source.getURI().substring("file:/".length() ) + 
                    AO_FOM_JavaScriptInterpreter.INTERCEPTION_POSTFIX;
                FileOutputStream fos = new FileOutputStream( filename );
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                char completeChar[] = this.getScriptAsCharArray();
                for( int i = 0; i < completeChar.length; i++ ) {
                    bs.write( completeChar[i] );
                }
                fos.write( bs.toByteArray() );
                fos.close();
             }            
        }

        /**
         * Add all tokens in the correct order 
         * 
         * @param interceptionGroupList - sorted list of all available interceptions
         * @param functionName - name of the intercepted function
         * @param eventType - event type
         * @param tokensListIt - ListIterator where the new tokens found 
         *        in the interceptionsList are added
         * @return true if the those elements replace other JSTokens
         */
        private boolean addInterceptionTokens( ArrayList interceptionGroupList, 
                                               String functionName,
                                               String eventType, 
                                               ListIterator tokensListIt 
                                              ) {
                                                  
            JSToken t = new JSToken( JSToken.COMMENT );
            ArrayList matchingInterceptors = new ArrayList();

            // read out all matching interceptions from interceptionsList
            for( int i = 0; i < interceptionGroupList.size(); i++ ) {
                InterceptionList interceptionList = (InterceptionList) interceptionGroupList.get(i);
                ListIterator li = interceptionList.listIterator();
                while( li.hasNext() ) {
                    Interceptor interceptor = (Interceptor) li.next();
                    interceptor.setBaseScript( interceptionList.getSourceScript() );                
                    boolean success = WildcardHelper.match( 
                        new HashMap(), functionName + Interceptor.DELIMITER + eventType, 
                        WildcardHelper.compilePattern( interceptor.getName() ));
                    if( success ) {
                        matchingInterceptors.add( interceptor );                        
                    }
                }
            }
            if( matchingInterceptors.size() > 0 ) {
                // add a comment showing the interception type
                t.append( "\n\n/* " + eventType + "():" + "         */\n" );      
                tokensListIt.add( t); 
                ListIterator ili = matchingInterceptors.listIterator();
                while( ili.hasNext() ) {
                    Interceptor interceptor = (Interceptor) ili.next();
                    // add a comment where the interception is defined                    
                    t = new JSToken( JSToken.COMMENT );
                    t.append( "\n// interception from: "  + interceptor.getName() + " [" + interceptor.getBaseScript() + "]\n" );                    
                    tokensListIt.add( t.getClone() );
                    // add all tokens that the interception definition contains
                    ListIterator interceptorTokensIt = interceptor.getTokens().listIterator();
                    while( interceptorTokensIt.hasNext() ) {
                        tokensListIt.add( interceptorTokensIt.next() );
                    }       
                }
                // end comment
                t = new JSToken( JSToken.COMMENT );                
                t.append( "\n/* end " + eventType + "():" + "     */\n\n" );   
                tokensListIt.add( t.getClone() );                    
                return true;
            }
            
            return false;
        }


        /**
         * help method to get a function name
         * 
         * TODO add support for property functions
         */
        private String getFunctionName( int functionPosition ) { 
            String functionName = "";
            boolean foundName = false;
            ListIterator li = this.listIterator( functionPosition );

            // search for 'function myFunc(..) {'
            while( li.hasNext() && ! foundName ) {
                JSToken t = (JSToken) li.next();
                if( t.getType() == JSToken.CODE ) {
                    functionName = t.toString();
                    foundName = true;           
                }
                else if( t.getType() == JSToken.BRACKET_LEFT ) {
                    break;
                }
            }

            // search for 'x.prototype.myFunc = function(args) {'            
            /*if(! foundName ) {
               not implemented yet --> do we need it?
            }*/
            return functionName;
        }
        
        /**
         * Get a list of all occurencies of "function"
         */
        private List getFunctionPositions() {
            ArrayList functionPosition = new ArrayList();
            ListIterator li = this.listIterator();
            while( li.hasNext() ) {
                JSToken t = (JSToken) li.next();
                if( t.getType() == JSToken.CODE && t.equals( "function" ) ) {
                    functionPosition.add( new Integer(li.nextIndex() - 1 ) );
                }
            }
            return functionPosition;
        }
        
        /**
         * Check a token if it is function call that leads to a stop of
         * execution (continuation is created)
         */
        private String isStopFunction( JSToken curToken, ListIterator liTokens ) {
            JSToken n1token = (JSToken) liTokens.next();
            JSToken n2token = (JSToken) liTokens.next();
            liTokens.previous();
            liTokens.previous();    
                       
            ListIterator li = this.stopExecutionFunctions.listIterator();
            while( li.hasNext() ) {
                String object = "";
                String function = "";
                StringTokenizer st = new StringTokenizer( (String) li.next(), ".");
                int countTokens = st.countTokens();

                if( countTokens == 1 ) {
                   function = st.nextToken();
                   function = function.substring( 0, function.indexOf("(") );                   
                   if( function.equals( curToken.toString() ) ) return function;
                }
                else if( countTokens == 2 ) {
                   object = st.nextToken();
                   function = st.nextToken();
                   function = function.substring( 0, function.indexOf("(") );
                   if( object.equals( curToken.toString() ) && 
                       n1token.getType() == JSToken.POINT &&
                       function.equals( n2token.toString() ) ) {
                       return object + "." + function;
                   } 
                }
                else {
                    // not allowed! 
                }
                
            }
            return null;
        }
        
        
        /**
         * to make intercepting easier events are added (start function, 
         * stop function, stop execution, continue execution)
         */
        protected void addInterceptionEvents( List stopExecutionFunctions ) {

            this.stopExecutionFunctions = stopExecutionFunctions;
            
            List functionPositions = this.getFunctionPositions();
            // count all added interceptor events to jump into the right
            // position of the tokens list

            int diff = 1; 
            // loop over all functions
            for( int i = 0; i < functionPositions.size(); i++ ) {
                int pos = ((Integer) functionPositions.get( i )).intValue();
                String functionName = getFunctionName( pos + diff );
                ListIterator li = this.listIterator( pos + diff );
                boolean functionStartSet = false;
                boolean isExecStopFunctionSet = false;
                int countOpenBrackets = 0;
                int countCurToken = pos - 1;  // get the current index
                
                // continue in the token list to find opening and closing brackets
                while( li.hasNext() ) {
                    countCurToken++;                    
                    JSToken t = (JSToken) li.next();
                    int type = t.getType();
                    // function start is found
                    if( type == JSToken.BRACKET2_LEFT &&  ! functionStartSet ) {
                        li.add( new InterceptorEvent( InterceptorEvent.FNC_START, 
                                                      functionName ) );
                        diff++;
                        functionStartSet = true;
                        continue;
                    }
                    // within a function
                    if( functionStartSet ) {
                        // end of a function is reached
                        if( type == JSToken.BRACKET2_RIGHT && countOpenBrackets == 0 ) {
                            li.previous();
                            li.add( new InterceptorEvent( InterceptorEvent.FNC_END, 
                                                          functionName ) );
                            diff++;
                            break;                         
                        }
                        // count brackets
                        if( type == JSToken.BRACKET2_LEFT ) {
                            countOpenBrackets++;
                        }
                        else if ( type == JSToken.BRACKET2_RIGHT ) {
                            countOpenBrackets--;
                        }
                        // end of a continuation creating function reached
                        else if( isExecStopFunctionSet ) {
                            if( type == JSToken.SEMICOLON ) {
                                li.add( new InterceptorEvent( 
                                            InterceptorEvent.CONT_EXEC,
                                            functionName ));
                                diff++;
                                isExecStopFunctionSet = false;                          
                            }
                            continue;
                        }                        
                        // check if a function is reached that creates continuations
                        else if( type == JSToken.CODE ) { 
                            String fnc = isStopFunction( t, li );
                            if( null != fnc ) {
                                li.previous();                   
                                li.add( new InterceptorEvent( InterceptorEvent.STOP_EXEC,
                                   functionName ));
                                diff++;                           
                                isExecStopFunctionSet = true;
                                continue;
                            }
                        }
                    }
                } // end while
            } // end for
        } // end method
        
        /**
         * Get a list of sources as string (the src attribute of the 
         * cocoon.apply( src) function) of all scripts to apply
         */
        protected List getScriptsApplied() {
            ArrayList scriptsApplied = new ArrayList();            
            boolean foundCocoon = false;
            boolean foundApply = false;

            int curCocoonPosition = 0;
            
            ListIterator li = this.listIterator();
            while( li.hasNext() ) {
                JSToken t = (JSToken) li.next();
                if( t.getType() == JSToken.CODE ) {
                    if( t.equals( "cocoon") ) { 
                        foundCocoon = true; 
                        curCocoonPosition = li.previousIndex() + 1;
                    } 
                    else if( foundCocoon && t.equals( "apply") ) {
                        foundApply = true;
                    }
                    else {
                        foundCocoon = false;
                        foundApply = false;
                        curCocoonPosition = 0;
                    }
                }
                else if( foundApply && t.getType() == JSToken.CODE_LITERAL ) {
                    String script = t.toString();                    
                    script = script.substring( 1, script.length() - 1 );
                    scriptsApplied.add( script );
                    cocoonPositions.add( new Integer(curCocoonPosition) );
                    foundCocoon = false;
                    foundApply = false;
                }
            }

            return scriptsApplied;
        }
    
        /**
         * comment out all occurencies of cocoon.apply( "..." );
         */
        protected void commentScriptsApplied() {
            int diff = -1;
            for( int i = 0; i < this.cocoonPositions.size(); i++ ) {
                int pos = ((Integer) this.cocoonPositions.get(i)).intValue();
                ListIterator li = this.listIterator(pos + diff);
                JSToken t = new JSToken( JSToken.COMMENT );
                t.append("/* -> not needed in result script: ");
                li.add( t );
                diff++;                
                while( li.hasNext() ) {
                    JSToken tt = (JSToken) li.next();
                    t.append(tt.stream());
                    li.remove();
                    diff--;
                    if( tt.getType() == JSToken.SEMICOLON ) {
                        break;
                    }
                }
                t.append('*').append('/');
            }
        }
        
        /**
         * Replace all occurencies of return (except those which
         * are in separte code blocks)
         */
        protected void replaceReturnStatements() {
            ListIterator li = this.listIterator();
            int countOpenBrackets = 0;
            boolean inFunction = false;
            boolean foundReturnStatement = false;
            while( li.hasNext() ) {
                JSToken t = (JSToken) li.next();
                int type = t.getType();                
                if( t instanceof InterceptorEvent ) {
                    InterceptorEvent ie  =(InterceptorEvent) t;
                    int ieType = ie.getType();
                    if( ieType == InterceptorEvent.FNC_START ) {
                        inFunction = true;    
                        foundReturnStatement = false;
                    } 
                    else if( ieType == InterceptorEvent.FNC_END ) {
                        if( foundReturnStatement ) {
                            li.add( new JSToken( JSToken.COMMENT, "/* moved return statement   */" ));      
                            li.add( new JSToken( JSToken.LF, "\n" ));    
                            li.add( new JSToken( JSToken.CODE, "return" ));                   
                            li.add( new JSToken( JSToken.WHITESPACE, " " ));                            
                            li.add( new JSToken( JSToken.CODE, RETURN_VARIABLE ));     
                            li.add( new JSToken( JSToken.SEMICOLON, ";" ));   
                            li.add( new JSToken( JSToken.LF, "\n" ));                               
                        }   
                        inFunction = false;
                    }
                }                
                else if( inFunction ) {
                    if( type == JSToken.BRACKET2_LEFT ) {
                        countOpenBrackets++;
                    }   
                    else if( type == JSToken.BRACKET2_RIGHT ) {
                        countOpenBrackets--;   
                    }
                    else if( countOpenBrackets == 0 && 
                             type == JSToken.CODE && 
                             t.equals( "return") ) {
                        
                        li.remove();
                        li.add( new JSToken( JSToken.CODE, "var" ));    
                        li.add( new JSToken( JSToken.WHITESPACE, " " ));                            
                        li.add( new JSToken( JSToken.CODE, RETURN_VARIABLE ));
                        li.add( new JSToken( JSToken.WHITESPACE, " " ));      
                        li.add( new JSToken( JSToken.EQUAL_SIGN, "=" ));                      
                        foundReturnStatement = true;                     
                    }
                }                 
            }   
        }

        // ------------ TokenList of appield files ---------------------------

        /**
         * help method to read an intercepting function name - the difference
         * to getFunctionName( pos ) is the wildcards "*"
         */
        private String getInterceptorFunctionName( int functionPosition ) { 
            StringBuffer functionName = new StringBuffer();
            boolean partOfFunctionFound = false;
            ListIterator li = this.listIterator( functionPosition + 1 );
            while( li.hasNext() ) {
                JSToken t = (JSToken) li.next();
                int type = t.getType();
                // search for any combination of code and asterisks
                if( type == JSToken.CODE || type == JSToken.ASTERISK ) {
                    functionName.append( t.stream() ); 
                    partOfFunctionFound = true;
                }
                else if( partOfFunctionFound ) {
                    break;
                }
            }
            return functionName.toString();
        }        

        /**
         * Get a list of all defined interceptors. Search for:
         * <ul>
         *   <li>before(): { ... }</li>
         *   <li>after():  { ... }</li>
         *   <li>around(): { ... }</li> 
         * </ul>
         */
        protected InterceptionList readInterceptionTokens() {
            InterceptionList interceptors = new InterceptionList();
            List functionPositions = this.getFunctionPositions();   
            // loop over all intercepting functions
            for( int i = 0; i < functionPositions.size(); i++ ) {
                int pos = ((Integer) functionPositions.get( i )).intValue();
                String interceptingFunctionName = getInterceptorFunctionName( pos ); 
                
                boolean inFunction = false;
                boolean inInterceptor = false;
                Interceptor curInterceptor = null;
                String interceptorType = "";
                int countOpenBrackets = 0;
                ListIterator li = this.listIterator( pos + 2 );
                while( li.hasNext() ) {
                     JSToken t = (JSToken) li.next();
                     int type = t.getType();
                     // find the start of the function
                     if( type == JSToken.BRACKET2_LEFT && ! inFunction ) {
                         inFunction = true;
                     }
                     // within an intercepting function
                     else if( inFunction ) {
                         // find an interception definition
                         if( !inInterceptor && type == JSToken.CODE ) {
                                 
                             if( t.equals( Interceptor.STR_BEFORE ) ) {
                                 interceptorType = Interceptor.STR_BEFORE;
                             }
                             else if( t.equals( Interceptor.STR_AFTER ) ) {
                                 interceptorType = Interceptor.STR_AFTER;
                             }   
                             else if( t.equals( Interceptor.STR_AROUND ) ) {
                                 interceptorType = Interceptor.STR_AROUND;
                             }    
                             else if( t.equals( Interceptor.STR_STOP_EXEC )) {
                                 interceptorType = Interceptor.STR_STOP_EXEC;   
                             }
                             else if( t.equals( Interceptor.STR_CONT_EXEC )) {
                                 interceptorType = Interceptor.STR_CONT_EXEC;   
                             }                             
                         }
                         // after interception defintion is found 
                         else if( !inInterceptor && type == JSToken.BRACKET2_LEFT ) {    
                                                     
                             inInterceptor = true;
                             curInterceptor = new Interceptor( interceptingFunctionName, 
                                                               interceptorType );
                             countOpenBrackets++;
                         }
                         // and add it to the list
                         else if( inInterceptor && type == JSToken.BRACKET2_RIGHT &&
                                  countOpenBrackets == 0 ) {

                         }                        
                         // within intercepting code add all tokens
                         else if( inInterceptor ) {
                             // count brackets
                             if( type == JSToken.BRACKET2_LEFT) {
                                 countOpenBrackets++; 
                             }
                             else if( type == JSToken.BRACKET2_RIGHT ) {
                                 countOpenBrackets--;                            
                             }
                             // end of intercepting code is reached - clone interception token                              
                             if( type == JSToken.BRACKET2_RIGHT && countOpenBrackets == 0) {
                                 interceptors.add( curInterceptor.getClone() );
                                 inInterceptor = false;
                                 interceptorType = "";
                                 curInterceptor = null;                                
                             }
                             // within intercepting code
                             else {
                                 curInterceptor.addToken( t );
                             }
                         }
                     }                     
                }
            }
            return interceptors;
        }
        
        // ------------ common methods  ---------------------------

        /**
         * return the tokens as Reader
         */
        public Reader getScriptAsReader() {       
            return new CharArrayReader( getScriptAsCharArray() );    
        }

        public char[] getScriptAsCharArray() {
            ListIterator li = this.listIterator();
            char charComplete[] = new char[0];
            while( li.hasNext() ) {
                JSToken t = (JSToken) li.next();
                char theChar[] = t.stream();                                  
                char copy[] = new char[charComplete.length + theChar.length];
                System.arraycopy( charComplete, 0, copy, 0, charComplete.length);
                System.arraycopy( theChar, 0, copy, charComplete.length, theChar.length );      
                charComplete = copy;
            }           
            return charComplete;    
        }        
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            ListIterator li = this.listIterator();
            while( li.hasNext() ) {
                JSToken t = (JSToken) li.next();            
                sb.append( t.stream() );
            }                   
            return sb.toString();
        }

        public String debugToString() {
            StringBuffer sb = new StringBuffer();
            ListIterator li = this.listIterator();
            while( li.hasNext() ) {
                JSToken t = (JSToken) li.next();   
                sb.append("\n------------------------------------\n");
                sb.append("type: ").append(t.getType()).append("\n");   
                sb.append( t.stream() );
            }                   
            return sb.toString();
        }        
                   
    }    
    
    /**
     * A collection of interceptors with the possibility to
     * set the source of the script where they are read from
     */
    static class InterceptionList extends ArrayList {
           
        String sourceScript;
        
        public void setSourceScript( String source ) {
            this.sourceScript = source;
        }
        
        public String getSourceScript() {
            return this.sourceScript;   
        }
        
    }

    /**
     * A special JSToken - the Interceptor
     */
    static class Interceptor implements Cloneable {
        
        public static String STR_BEFORE            = "before";   
        public static String STR_AFTER             = "after";          
        public static String STR_AROUND            = "around";  
        public static String STR_STOP_EXEC         = "stopExecution";
        public static String STR_CONT_EXEC         = "continueExecution";        
        
        public static String DELIMITER             = ":";        
        
        String name;
        String type;
        JSTokenList tokens = new JSTokenList();
        String baseScript;
        
        public Interceptor( String functionName, String type ) {
            this.name = functionName + DELIMITER + type;
            this.type = type;
        }

        public void setBaseScript(String src) {
            this.baseScript = src;
        }
        
        public String getBaseScript() {
            return this.baseScript;
        }

        public void addToken( JSToken token ) {
            tokens.add( token );
        }

        public JSTokenList getTokens() {
            return this.tokens;
        }        

        public String getName() {
            return this.name;
        }

        public String getType() {
            return this.type;
        }
        
        public char[] stream() {
            char charComplete[] = new char[0];
            ListIterator li = this.tokens.listIterator();
            while( li.hasNext() ) {
                JSToken t = (JSToken) li.next();
                char theChar[] = t.stream();
                char copy[] = new char[charComplete.length + theChar.length];
                System.arraycopy( charComplete, 0, copy, 0, charComplete.length);
                System.arraycopy( theChar, 0, copy, charComplete.length, theChar.length );     
                charComplete = copy;           
            }
            return charComplete;
        }
        

        // cloning
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public Interceptor getClone() {
            Interceptor interceptor = null;
            try {
                interceptor = (Interceptor) this.clone();
            } catch (CloneNotSupportedException e) {}
            return interceptor;
        }        
        
    }
     
     /**
      * A JavaScript token like whitespace, code, brackets, ...
      */
     static class JSToken implements Cloneable {

         public static int START                   =  0;    
         public static int COMMENT                 =  1;
         public static int LINE_COMMENT            =  2;
         public static int LF                      =  3;
         public static int WHITESPACE              =  4;    
         public static int BRACKET_LEFT            =  5;
         public static int BRACKET_RIGHT           =  6;
         public static int BRACKET1_LEFT           =  7;
         public static int BRACKET1_RIGHT          =  8;
         public static int BRACKET2_LEFT           =  9;
         public static int BRACKET2_RIGHT          = 10;
         public static int CODE                    = 11;
         public static int CODE_LITERAL            = 12;
         public static int SEMICOLON               = 13;
         public static int POINT                   = 14;
         public static int EQUAL_SIGN              = 15;
         public static int COMMA                   = 16;
         public static int COLON                   = 17;
         public static int REGEXP                  = 18;
         public static int UNKNOWN                 = 19;   
         public static int ASTERISK                = 20;  

         char token[];
         int type;

         public JSToken(int type) {
             this.type = type;
             token = new char[0];
         }
         
         public JSToken( int type, String tokenValue ) {
             this.type = type;
             this.token = tokenValue.toCharArray();
         }

         public void setType(int type) {
             this.type = type;
         }

         public int getType() {
             return this.type;
         }

         public boolean equals(String comp) {
             if ((new String(token)).equals(comp)) {
                 return true;
             }
             return false;
         }

         public char[] stream() {
             return token;
         }
         
         public int size() {
             return token.length;
         }

         public JSToken append(char c) {
             char copy[] = new char[token.length + 1];
             System.arraycopy(token, 0, copy, 0, token.length);
             token = copy;
             token[token.length - 1] = c;
             return this;
         }

         public JSToken append(char c[]) {
             char copy[] = new char[token.length + c.length];
             System.arraycopy( token, 0, copy, 0, token.length );
             System.arraycopy( c, 0, copy, token.length, c.length );
             token = copy;
             return this;
         }
        
         public JSToken append( String str ) {
             char c[] = str.toCharArray();
             this.append(c);
             return this;
         }
            
         public String toString() {
             StringBuffer sb = new StringBuffer();
             for( int i = 0; i < token.length; i++ ) {
                 sb.append( token[i] );
             }
             return sb.toString();
         }
    
         // cloning
         public Object clone() throws CloneNotSupportedException {
             return super.clone();
         }
    
         public JSToken getClone() {
             JSToken t = null;
             try {
                 t = (JSToken) this.clone();
             } catch (CloneNotSupportedException e) {}
             return t;
         }
    
     }    

    /**
     * A token representing the start or the end of a function. This
     * is needed to make adding of the interception code snippets easier.
     * 
     * It extends JSToken by the function name and two new status.
     */
    static class InterceptorEvent extends JSToken {

        public static int FNC_START = 51;
        public static int FNC_END   = 52;     
        public static int STOP_EXEC = 53;
        public static int CONT_EXEC = 54;  

        String interceptorName;
        int type;
                
        public InterceptorEvent( int type ) {
            super( type );
        }
        
        public InterceptorEvent( int type, String name ) {
            super( type );
            this.interceptorName = name;
        }
        
        public String getName() {
            return this.interceptorName;
        }

     }
        
}
