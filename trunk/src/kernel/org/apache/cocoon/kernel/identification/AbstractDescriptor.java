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
package org.apache.cocoon.kernel.identification;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.cocoon.kernel.configuration.Configuration;
import org.apache.cocoon.kernel.configuration.ConfigurationException;

/**
 * <p>An {@link AbstractDescriptor} is an abstract class enclosing the generic
 * representation of a block descriptor.</p>
 *
 * <p>This class provides all common methods specified by the {@link Descriptor}
 * interface .</p>
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.4 $)
 */
public abstract class AbstractDescriptor extends ParsedIdentifier
implements Descriptor {

    /** <p>The configuration element associated with this instance.</p> */
    private Configuration configuration = null;

    /** <p>The {@link Identifer} of the block extended by this block.</p> */
    private Identifier extended = null;

    /** <p>The list of all private library {@link URL}s.</p> */
    private ArrayList privatelibs = new ArrayList();
    
    /** <p>The list of all protected library {@link URL}s.</p> */
    private ArrayList protectedlibs = new ArrayList();
    
    /** <p>The list of all public library {@link URL}s.</p> */
    private ArrayList publiclibs = new ArrayList();

    /* ====================================================================== */

    /** <p>A list of {@link Identifier}s of all implemented interfaces.</p> */
    protected ArrayList implemented = new ArrayList();
    
    /** <p>The {@link Map} of all our required blocks.</p> */
    protected Map required = new HashMap();
    
    /* ====================================================================== */
    
    /**
     * <p>Create a new {@link AbstractDescriptor} instance.</p>
     *
     * <p>Extensions of this abstract class must call this constructor to
     * process the {@link Identifier} of the block identified by this
     * {@link Descriptor}, and all {@link Identifier}s of the extended and
     * implemented blocks.</p>
     *
     * @param configuration a {@link Configuration} element with details of
     *                      this {@link Descriptor} characteristics.
     * @throws ConfigurationException if the specified {@link Configuration}
     *                                did not represent a valid descriptor.
     * @throws IdentificationException if the block identifier gathered from
     *                                 the configuration was not valid.
     * @throws NullPointerException if the {@link Configuration} was <b>null</b>.
     */
    public AbstractDescriptor(Configuration configuration)
    throws ConfigurationException, IdentificationException {
        super(configuration.getStringAttribute("id"));
        
        /* The identifier of the block we extend */
        String idstring = configuration.getStringAttribute("extends", null);
        if (idstring != null) try {
            this.extended = new ParsedIdentifier(idstring);
        } catch (IdentificationException exception) {
            /* Something went wrong parsing the extends identifier */
            throw new ConfigurationException("Unable to parse extended block "
                                             + "identifer \"" + idstring + "\"",
                                             configuration, exception);
        }

        /* Analyse the block requirements */
        Configuration current = configuration.child(NAMESPACE, "requirements");
        Iterator iterator = current.children(NAMESPACE, "requires");
        while (iterator.hasNext()) try {
            current = (Configuration)iterator.next();
            String name = current.getStringAttribute("name");
            idstring = current.getStringAttribute("block");
            Identifier id = new ParsedIdentifier(idstring);
            if (this.required.put(name, id) != null) {
                throw new ConfigurationException("Descriptor declares same "
                                                 + "block requirement \""
                                                 + name + "\" twice", current);
            }
        } catch (IdentificationException exception) {
            /* Something went wrong parsing an identifier */
            throw new ConfigurationException("Unable to parse required "
                                             + "block identifer \"" + idstring
                                             + "\"", current, exception);
        }
        
        /* The identifiers of all blocks we implement */
        current = configuration.child(NAMESPACE, "implementations");
        iterator = current.children(NAMESPACE, "implements");
        while (iterator.hasNext()) try {
            current = (Configuration)iterator.next();
            idstring = current.getStringAttribute("block");
            this.implemented.add(new ParsedIdentifier(idstring));
        } catch (IdentificationException exception) {
            /* Something went wrong parsing an identifier */
            throw new ConfigurationException("Unable to parse implemented "
                                             + "block identifer \"" + idstring
                                             + "\"", current, exception);
        }

        /* Process the libraries of this block */
        current = configuration.child(NAMESPACE, "libraries");
        iterator = current.children(NAMESPACE, "library");
        URL context = configuration.locationURL();
        
        /* Iterate through all the "library" children */
        while (iterator.hasNext()) {
            current = (Configuration)iterator.next();
            URL url = null;
            
            /* Check that we can resolve the library */
            try {
                String value = current.getStringAttribute("url");
                if (context == null) url = new URL(value);
                else url = new URL(context, value);
            } catch (MalformedURLException exception) {
                throw new ConfigurationException("Unable to resolve block "
                                                 + "library URL \"" + url
                                                 + "\"", current, exception);
            }
            
            /* Check access and store */
            String access = (this.isInterface() ? "public" : "private");
            access = current.getStringAttribute("access", access);
            if ("public".equals(access)) {
                /* Public libraries available to interfaces and implems */
                publiclibs.add(url);
            } else if ("protected".equals(access)) {
                /* Protected libraries available to interfaces and implems */
                protectedlibs.add(url);
            } else if (("private".equals(access)) && (!this.isInterface())) {
                /* Private libraries available to implementations only */
                privatelibs.add(url);
            } else {
                /* Problems with library access */
                StringBuffer message = new StringBuffer(512);
                message.append("Unknown or unsupported access \"");
                message.append(access);
                message.append("\" for library \"");
                message.append(url);
                message.append(this.isInterface() ?
                               "\" of interface block \"" :
                               "\" of implementation block \"");
                message.append(this);
                message.append("\"");
                throw new ConfigurationException(message.toString(), current);
            }
        }

        /* Save a locked copy of the configurations */
        this.configuration = ((Configuration)configuration.clone()).lock();
    }

    /* ====================================================================== */
    
    /**
     * <p>Return a human-readable representation of this {@link Descriptor}.</p>
     *
     * @return a <b>non null</b> {@link String}.
     */
    public String toString() {
        return((this.isInterface() ? "iface:" : "block:") + super.toString());
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the {@link Configuration} element used to create this 
     * {@link Descriptor}.</p>
     *
     * @return a <b>non null</b> {@link Configuration} instance.
     */
    public Configuration configuration() {
        return(this.configuration);
    }
    
    /* ====================================================================== */

    /**
     * <p>Return the unique {@link Identifier} of the block this descriptor
     * is claiming to extend.
     *
     * @return a <b>non null</b> {@link Identifier}
     */
    public Identifier extendedBlock() {
        return(this.extended);
    }

    /**
     * <p>Return a list of all unique {@link Identifier}s of the blocks this
     * descriptor is claiming to implement.
     *
     * @return an {@link Iterator} of {@link Identifier} instances.
     */
    public Iterator implementedBlocks() {
        /* Return a protected iterator */
        return(new ArrayList(this.implemented).iterator());
    }
    
    /**
     * <p>Return an iterator over all {@link Identifier}s of the blocks this
     * descriptor is claiming to require.
     *
     * <p>Note that only non-interface blocks can require external blocks.</p>
     *
     * @return an {@link Iterator} over {@link Identifier} instances.
     */
    public Iterator requiredBlocks() {
        return(new ArrayList(this.required.values()).iterator());
    }
    
    /**
     * <p>Return an array of {@link URL}s of all libraries declared in this
     * {@link Descriptor} having the specified access level.</p>
     *
     * @param level the access level of the libraries to return.
     * @return a <b>non null</b> {@link URL} array.
     */
    public URL[] libraries(int level) {
        URL array[] = new URL[0];
        if (level == ACCESS_PUBLIC) {
            return((URL [])this.publiclibs.toArray(array));
        } else if (level == ACCESS_PROTECTED) {
            return((URL [])this.protectedlibs.toArray(array));
        } else if (level == ACCESS_PRIVATE) {
            return((URL [])this.privatelibs.toArray(array));
        }
        return(array);
    }
}
