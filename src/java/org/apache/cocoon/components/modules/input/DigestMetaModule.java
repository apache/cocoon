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
package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.util.HashMap;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Iterator;
import java.util.Map;
import java.io.UnsupportedEncodingException;

/** Meta module that obtains values from other module and returns
 * message digest of value. Very useful for storing and checking
 * passwords. Input module configured through nested element
 * "input-module", message digest algorithm, security provider, salt,
 * and URL encoded output configurable through elements "algorithm",
 * "provider", "salt", "encode". Defaults are "sha", null, "salt", and
 * "false". Available value for encode are "none" (returns byte[]),
 * "string" (return hash as string), "url" (returns url encoded
 * string), "hex" (returns string of hex values).
 *
 * @author <a href="mailto:haul@informatik.tu-darmstadt.de">Christian Haul</a>
 * @version CVS $Id: DigestMetaModule.java,v 1.2 2004/03/08 13:58:30 cziegeler Exp $
 */
public class DigestMetaModule extends AbstractMetaModule implements ThreadSafe {

    private String defaultAlgorithm = "SHA";
    private String defaultProvider = null;
    private String defaultSalt = "salt";
    private String defaultEncode = "false";

    /** output encoding none */
    static final int ENCODING_NONE = 0;
    /** output encoding url encoding */
    static final int ENCODING_STR = 1;
    /** output encoding hex */
    static final int ENCODING_URL = 2;
    /** output encoding hex */
    static final int ENCODING_HEX = 3;

    private static final HashMap encodingNames;
    /** setup mapping tables */
    static {
        HashMap names = new HashMap();
        names.put("false", new Integer(ENCODING_NONE));
        names.put("no", new Integer(ENCODING_NONE));
        names.put("none", new Integer(ENCODING_NONE));
        names.put("string", new Integer(ENCODING_STR));
        names.put("yes", new Integer(ENCODING_URL));
        names.put("true", new Integer(ENCODING_URL));
        names.put("hex", new Integer(ENCODING_HEX));
        encodingNames = names;
        names = null;
    }


    public void configure(Configuration config) throws ConfigurationException {

        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name", this.defaultInput);

        this.defaultAlgorithm = this.inputConf.getAttribute("algorithm",this.defaultAlgorithm);
        this.defaultProvider = this.inputConf.getAttribute("provider",this.defaultProvider);
        this.defaultSalt = this.inputConf.getAttribute("salt",this.defaultSalt);
        this.defaultEncode = this.inputConf.getAttribute("encode","false");

        // preferred
        this.defaultAlgorithm = config.getChild("algorithm").getValue(this.defaultAlgorithm);
        this.defaultProvider = config.getChild("provider").getValue(this.defaultProvider);
        this.defaultSalt = config.getChild("salt").getValue(this.defaultSalt);
        this.defaultEncode = config.getChild("encode").getValue(this.defaultEncode);

        if (encodingNames.get(this.defaultEncode) == null) {
            if (getLogger().isErrorEnabled())
                getLogger().error("Requested encoding is unknown: "+this.defaultEncode);
            this.defaultEncode="false";
        }
    }


    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        // obtain correct configuration objects
        // default vs dynamic
        Configuration inputConfig = null;
        String inputName=null;
        String algorithm = this.defaultAlgorithm;
        String provider  = this.defaultProvider;
        String salt  = this.defaultSalt;
        int encode = ((Integer) encodingNames.get(this.defaultEncode)).intValue();
        if (modeConf!=null) {
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
            // read necessary parameters
            algorithm = modeConf.getAttribute("algorithm", algorithm);
            provider  = modeConf.getAttribute("provider", provider);
            salt  = modeConf.getAttribute("salt", salt);
            encode = ((Integer) encodingNames.get(modeConf.getAttribute("encode", this.defaultEncode))).intValue();

            // preferred
            algorithm = modeConf.getChild("algorithm").getValue(algorithm);
            provider  = modeConf.getChild("provider").getValue(provider);
            salt  = modeConf.getChild("salt").getValue(salt);
            encode = ((Integer) encodingNames.get(modeConf.getChild("encode").getValue(this.defaultEncode))).intValue();
        }


        Object value = getValue(name, objectModel,
                                this.input, this.defaultInput, this.inputConf,
                                null, inputName, inputConfig);
        
        if (value != null)
            try {
                MessageDigest md = (provider==null ? MessageDigest.getInstance(algorithm) : 
                                    MessageDigest.getInstance(algorithm,provider));
                
                md.update((salt+(value instanceof String? (String)value : value.toString())).getBytes());
                return encodeByteArray(md.digest(),encode);

            } catch (NoSuchAlgorithmException nsae) {
                if (getLogger().isWarnEnabled()) 
                    getLogger().warn("A problem occurred acquiring digest algorithm '" + algorithm 
                                     + (provider==null?"":"' from '"+provider) +"': " + nsae.getMessage());
            } catch (NoSuchProviderException nspe) {
                if (getLogger().isWarnEnabled()) 
                    getLogger().warn("A problem occurred acquiring digest algorithm '" + algorithm 
                                     + (provider==null?"":"' from '"+provider) +"': " + nspe.getMessage());
            }

        return null;        
    }


    public Iterator getAttributeNames( Configuration modeConf, Map objectModel )
        throws ConfigurationException {

         if (!this.initialized) {
             this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        // obtain correct configuration objects
        // default vs dynamic
        Configuration inputConfig = null;
        String inputName=null;
        if (modeConf!=null) {
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
        }

        Iterator names = getNames(objectModel, 
                                  this.input, this.defaultInput, this.inputConf, 
                                  null, inputName, inputConfig);
        
        return names;

   }


    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel )
        throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        // obtain correct configuration objects
        // default vs dynamic
        Configuration inputConfig = null;
        String inputName=null;
        String algorithm = this.defaultAlgorithm;
        String provider  = this.defaultProvider;
        String salt  = this.defaultSalt;
        int encode = ((Integer) encodingNames.get(this.defaultEncode)).intValue();
        if (modeConf!=null) {
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
            // read necessary parameters
            algorithm = modeConf.getAttribute("algorithm", algorithm);
            provider  = modeConf.getAttribute("provider" , provider );
            salt  = modeConf.getAttribute("salt" , salt );
            encode = ((Integer) encodingNames.get(modeConf.getAttribute("encode" , this.defaultEncode))).intValue();

            // preferred
            algorithm = modeConf.getChild("algorithm").getValue(algorithm);
            provider  = modeConf.getChild("provider").getValue(provider);
            salt  = modeConf.getChild("salt").getValue(salt);
            encode = ((Integer) encodingNames.get(modeConf.getChild("encode").getValue(this.defaultEncode))).intValue();
        }

        Object[] values = getValues(name, objectModel, 
                                    this.input, this.defaultInput, this.inputConf, 
                                    null, inputName, inputConfig);
        Object[] result = null;

        if (values != null) {
            try {
                MessageDigest md = (provider==null ? MessageDigest.getInstance(algorithm) : 
                                    MessageDigest.getInstance(algorithm,provider));
                
                result = new Object[values.length];
                for (int i=0; i<values.length; i++) {
                    md.update((salt + (values[i] instanceof String? (String)values[i] : 
                                       values[i].toString())).getBytes());
                    result[i] = encodeByteArray(md.digest(), encode);
                }
                return result;
            } catch (NoSuchAlgorithmException nsae) {
                if (getLogger().isWarnEnabled()) 
                    getLogger().warn("A problem occurred acquiring digest algorithm '" + algorithm 
                                     + (provider==null?"":"' from '"+provider) +"': " + nsae.getMessage());
            } catch (NoSuchProviderException nspe) {
                if (getLogger().isWarnEnabled()) 
                    getLogger().warn("A problem occurred acquiring digest algorithm '" + algorithm 
                                     + (provider==null?"":"' from '"+provider) +"': " + nspe.getMessage());
            }
        }
        return result;
    }


    /**
     * Create the output representation.
     * @param b a <code>byte[]</code>
     * @param encode an <code>int</code>, one of {@link #ENCODING_NONE},{@link #ENCODING_URL},{@link #ENCODING_HEX}
     * @return an <code>Object</code>
     */
    Object encodeByteArray(byte[] b, int encode) {
        Object result = null;
        switch(encode) {
        case ENCODING_HEX:
            result = byte2Hex(b);
            break;
        case ENCODING_STR:
            try {
                result = new String(b, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                if (getLogger().isErrorEnabled())
                    getLogger().error("UTF-8 not supported -- cannot convert message digest to String.");
            }
            break;
        case ENCODING_URL:
            try {
                String str = new String(b, "UTF-8");
                result = URLEncoder.encode(str);
            } catch (UnsupportedEncodingException uee) {
                if (getLogger().isErrorEnabled())
                    getLogger().error("UTF-8 not supported -- cannot convert message digest to String.");
            }
            break;
        case ENCODING_NONE:
            // nothing to do
            break;
        default:
            // should not happen
        }
        return result;
    }

    /**
     * Create a hex representation of a byte array.
     *
     * @param b a <code>byte[]</code> value
     * @return a <code>String</code> value
     */
    static String byte2Hex ( byte[] b ) {
        StringBuffer sb = new StringBuffer( b.length * 2 );
        for ( int i=0 ; i < b.length ; i++ ) {
            sb.append( hexChar [ ( b[i] & 0xf0 ) >>> 4 ] ) ;
            sb.append( hexChar [ ( b[i] & 0x0f )       ] ) ;
        }
        return sb.toString() ;
    }


    /** 
     * hex digits lookup table
     */
    static char[] hexChar = {
        '0' , '1' , '2' , '3' ,
        '4' , '5' , '6' , '7' ,
        '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' 
    };

}
