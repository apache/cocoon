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
    include  the following  acknowledgment:   "This product includes software
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

package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.LockableSource;
import org.apache.cocoon.components.source.RestrictableSource;
import org.apache.cocoon.components.source.SourceInspector;
import org.apache.cocoon.components.source.TraversableSource;
import org.apache.cocoon.components.source.VersionableSource;
import org.apache.cocoon.components.source.helpers.GroupSourcePermission;
import org.apache.cocoon.components.source.helpers.PrincipalSourcePermission;
import org.apache.cocoon.components.source.helpers.SourceLock;
import org.apache.cocoon.components.source.helpers.SourcePermission;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Generates a description from a source of a repository.
 * The generator is a combination of a DirectoryGenerator and a generator
 * for retrieving SourceProperties, SourcePermission etc.
 *
 * @deprecated  use TraversableSourceDescriptionGenerator instead
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: SourceDescriptionGenerator.java,v 1.9 2003/12/22 13:35:06 joerg Exp $
 */
public class SourceDescriptionGenerator extends ServiceableGenerator
  implements CacheableProcessingComponent, Recyclable {

    /** Namespace of the source description. */
    private static final String SOURCE_NS = "http://apache.org/cocoon/description/2.0";

    /** The namespace prefix of the source description. */
    private static final String SOURCE_PREFIX = "source";

    private static final String SOURCE_NODE_NAME = "source";
    private static final String SOURCE_NODE_QNAME = SOURCE_PREFIX+":"+
                                                    SOURCE_NODE_NAME;

    private static final String NAME_ATTR_NAME = "name";
    private static final String URI_ATTR_NAME = "uri";
    private static final String MIMETYPE_ATTR_NAME = "mime-type";
    private static final String CONTENTLENGTH_ATTR_NAME = "contentlength";
    private static final String LASTMODIFIED_ATTR_NAME = "lastmodified";
    private static final String COLLECTION_ATTR_NAME = "collection";
    private static final String PARENT_ATTR_NAME = "parent";
    private static final String REVISION_ATTR_NAME = "revision";
    private static final String REVISIONBRANCH_ATTR_NAME = "branch";

    private static final String PROPERTIES_NODE_NAME = "properties";
    private static final String PROPERTIES_NODE_QNAME = SOURCE_PREFIX+":"+
                                                        PROPERTIES_NODE_NAME;
    private static final String PROPERTY_TYPE_ATTR_NAME = "type";

    private static final String PERMISSIONS_NODE_NAME = "permissions";
    private static final String PERMISSIONS_NODE_QNAME = SOURCE_PREFIX+":"+
                                                         PERMISSIONS_NODE_NAME;
    private static final String PERMISSION_NODE_NAME = "permission";
    private static final String PERMISSION_NODE_QNAME = SOURCE_PREFIX+":"+
                                                        PERMISSION_NODE_NAME;

    private static final String LOCKS_NODE_NAME = "locks";
    private static final String LOCKS_NODE_QNAME = SOURCE_PREFIX+":"+
                                                   LOCKS_NODE_NAME;
    private static final String LOCK_NODE_NAME = "lock";
    private static final String LOCK_NODE_QNAME = SOURCE_PREFIX+":"+
                                                  LOCK_NODE_NAME;

    private static final String CHILDREN_NODE_NAME = "children";
    private static final String CHILDREN_NODE_QNAME = SOURCE_PREFIX+":"+
                                                      CHILDREN_NODE_NAME;

    private static final String PRINCIPAL_ATTR_NAME = "principal";
    private static final String GROUP_ATTR_NAME = "group";
    private static final String PRIVILEGE_ATTR_NAME = "privilege";
    private static final String INHERITABLE_ATTR_NAME = "inheritable";
    private static final String NEGATIVE_ATTR_NAME = "negative";

    private static final String TYPE_ATTR_NAME = "type";
    private static final String EXPIRATION_ATTR_NAME = "expiration";
    private static final String EXCLUSIVE_ATTR_NAME = "exclusive";

    /** Include properties into the description */
    private boolean properties = true;

    /** Include permissions into the description */
    private boolean permissions = true;

    /** Include locks into the description */
    private boolean locks = true;

    /** Include version into the description */
    private boolean version = true;

    /** How deep the generator traverse the source */
    private int deep = 1;

    /** Traversed source for the keys and validities */
    private Hashtable cachedsources = new Hashtable();

    /** The queryString of the location including the "?" */
    private String queryString;

    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     *
     * @param resolver Source Resolver
     * @param objectModel Object model.
     * @param location Location of the source.
     * @param parameters Parameters for the generator.
     */
    public void setup(SourceResolver resolver, Map objectModel,
                      String location,
                      Parameters parameters)
                        throws ProcessingException, SAXException,
                               IOException {

        int idx = location.indexOf('?');

        if (idx!=-1) {
            this.queryString = location.substring(idx);
            location = location.substring(0, idx);
        } else {
            this.queryString = "";
        }

        super.setup(resolver, objectModel, location, parameters);

        this.properties = parameters.getParameterAsBoolean("properties",
            true);
        this.permissions = parameters.getParameterAsBoolean("permissions",
            true);
        this.locks = parameters.getParameterAsBoolean("locks", true);
        this.version = parameters.getParameterAsBoolean("version", true);

        collectSources(this.cachedsources, this.source, this.deep);
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public Serializable getKey() {

        StringBuffer key = new StringBuffer();

        key.append("SDG(");

        Source source;

        for (Enumeration e = cachedsources.elements(); e.hasMoreElements(); ) {
            source = (Source) e.nextElement();

            key.append(source.getURI());
            if (e.hasMoreElements()) {
                key.append(";");
            }
        }

        key.append(")");

        return key.toString();
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {

        AggregatedValidity validity = new AggregatedValidity();

        Source source;

        for (Enumeration e = cachedsources.elements(); e.hasMoreElements(); ) {
            source = (Source) e.nextElement();

            validity.add(source.getValidity());
        }

        return validity;
    }

    /**
     * Traverse the source tree and retrieve all sources.
     *
     * @param sources Collection of sources.
     * @param uri Uri of the source.
     * @param deep Deep of the hirachy, which should traversed.
     */
    private void collectSources(Hashtable sources, String uri,
                                int deep) throws ProcessingException {
        Source source = null;

        try {
            source = this.resolver.resolveURI(uri+this.queryString);
        } catch (Exception e) {
            if (sources.isEmpty()) {
                throw new ProcessingException("Could not retrieve source with the uri '"+
                                              uri+"'", e);
            }

            getLogger().debug("Could not retrieve source with the uri '"+uri+
                              "'", e);
            return;
        }

        sources.put(uri, source);

        if (source instanceof TraversableSource) {
            TraversableSource traversablesource = (TraversableSource) source;

            try {
                if (traversablesource.isSourceCollection() && (deep>0)) {
                    for (int i = 0; i<traversablesource.getChildSourceCount();
                        i++)
                        collectSources(sources,
                                       traversablesource.getChildSource(i),
                                       deep-1);
                }
            } catch (SourceException se) {
                getLogger().warn("Could not traverse source", se);
            }
        }
    }

    /**
     * Generate XML data.
     */
    public void generate()
      throws IOException, SAXException, ProcessingException {

        try {
            this.contentHandler.startDocument();
            this.contentHandler.startPrefixMapping(SOURCE_PREFIX, SOURCE_NS);

            if (((Source) this.cachedsources.get(this.source))==null) {
                throw new ProcessingException("Could not retrieve source with the uri '"+
                                              this.source+"'");
            }
            pushSourceDescription(this.source, this.deep);

            this.contentHandler.endPrefixMapping(SOURCE_PREFIX);
            this.contentHandler.endDocument();

        } catch (SourceException se) {
            throw new ProcessingException("Could not get source", se);
        }
    }

    /**
     * Push a XML description of specified source.
     *
     * @param uri Uniform resource identifier of the source.
     * @param deep Deep of the hirachy, which should traversed.
     */
    private void pushSourceDescription(String uri,
                                       int deep)
                                         throws SAXException,
                                                SourceException,
                                                ProcessingException,
                                                IOException {

        Source source = (Source) this.cachedsources.get(uri);

        if (source==null) {
            return;
        }

        try {
            AttributesImpl attributes = new AttributesImpl();

            attributes.addAttribute("", URI_ATTR_NAME, URI_ATTR_NAME,
                                    "CDATA", source.getURI());

            String name = source.getURI();

            if (name.endsWith("://")) {
                attributes.addAttribute("", NAME_ATTR_NAME,
                                        NAME_ATTR_NAME, "CDATA", "");
            } else if (name.endsWith("/")) {
                name = name.substring(1, name.length()-1);
                attributes.addAttribute("", NAME_ATTR_NAME,
                                        NAME_ATTR_NAME, "CDATA",
                                        name.substring(name.lastIndexOf("/")+
                                                       1, name.length()));
            } else {
                attributes.addAttribute("", NAME_ATTR_NAME,
                                        NAME_ATTR_NAME, "CDATA",
                                        name.substring(name.lastIndexOf("/")+
                                                       1, name.length()));
            }

            if ((source.getMimeType()!=null) &&
                (source.getMimeType().length()>0)) {
                attributes.addAttribute("", MIMETYPE_ATTR_NAME,
                                        MIMETYPE_ATTR_NAME, "CDATA",
                                        source.getMimeType());
            }

            if (source.getContentLength()>=0) {
                attributes.addAttribute("", CONTENTLENGTH_ATTR_NAME,
                                        CONTENTLENGTH_ATTR_NAME, "CDATA",
                                        String.valueOf(source.getContentLength()));
            }

            if (source.getLastModified()>0) {
                attributes.addAttribute("", LASTMODIFIED_ATTR_NAME,
                                        LASTMODIFIED_ATTR_NAME, "CDATA",
                                        String.valueOf(source.getLastModified()));
            }

            if (this.version && (source instanceof VersionableSource)) {
                VersionableSource versionablesource = (VersionableSource) source;

                if (versionablesource.isVersioned()) {
                    if ((versionablesource.getSourceRevision()!=null) &&
                        (versionablesource.getSourceRevision().length()>0)) {
                        attributes.addAttribute("",
                                                REVISION_ATTR_NAME,
                                                REVISION_ATTR_NAME, "CDATA",
                                                versionablesource.getSourceRevision());
                    }

                    if ((versionablesource.getSourceRevisionBranch()!=null) &&
                        (versionablesource.getSourceRevisionBranch().length()>
                         0)) {
                        attributes.addAttribute("",
                                                REVISIONBRANCH_ATTR_NAME,
                                                REVISIONBRANCH_ATTR_NAME,
                                                "CDATA",
                                                versionablesource.getSourceRevisionBranch());
                    }
                }
            }

            boolean isCollection = false;
            TraversableSource traversablesource = null;

            if (source instanceof TraversableSource) {
                traversablesource = (TraversableSource) source;

                isCollection = traversablesource.isSourceCollection();

                attributes.addAttribute("", COLLECTION_ATTR_NAME,
                                        COLLECTION_ATTR_NAME, "CDATA",
                                        String.valueOf(isCollection));

                String parent = traversablesource.getParentSource();

                if ((parent!=null) && (parent.length()>0)) {
                    attributes.addAttribute("", PARENT_ATTR_NAME,
                                            PARENT_ATTR_NAME, "CDATA",
                                            parent);
                }
            }

            this.contentHandler.startElement(SOURCE_NS, SOURCE_NODE_NAME,
                                             SOURCE_NODE_QNAME, attributes);

            if (this.properties && (source instanceof InspectableSource)) {
                pushLiveSourceProperties((InspectableSource) source);
            }

            if (this.properties) {
                pushComputedSourceProperties(source);
            }

            if (this.permissions) {
                try {
                    if (source instanceof RestrictableSource) {
                        pushSourcePermissions((RestrictableSource) source);
                    }
                } catch (SourceException se) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Could not retrieve source permissions",
                                          se);
                    }
                }
            }

            if (this.locks && (source instanceof LockableSource)) {
                pushSourceLocks((LockableSource) source);
            }

            if ((isCollection) && (deep>0)) {
                this.contentHandler.startElement(SOURCE_NS,
                                                 CHILDREN_NODE_NAME,
                                                 CHILDREN_NODE_QNAME,
                                                 new AttributesImpl());
                for (int i = 0; i<traversablesource.getChildSourceCount();
                    i++) {
                    try {
                        pushSourceDescription(traversablesource.getChildSource(i),
                                              deep-1);
                    } catch (SourceException se) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Could not retrieve source",
                                              se);
                        }
                    }
                }
                this.contentHandler.endElement(SOURCE_NS, CHILDREN_NODE_NAME,
                                               CHILDREN_NODE_QNAME);
            }

            this.contentHandler.endElement(SOURCE_NS, SOURCE_NODE_NAME,
                                           SOURCE_NODE_QNAME);

        } catch (SAXException saxe) {
            throw saxe;
        }
    }

    /**
     * Push a XML description about all properties, which
     * the source owns.
     *
     * @param source Source.
     */
    private void pushLiveSourceProperties(InspectableSource source)
      throws SAXException, SourceException {
        SourceProperty[] properties = source.getSourceProperties();
        SourceProperty property;

        AttributesImpl attributes = new AttributesImpl();

        attributes.addAttribute("", PROPERTY_TYPE_ATTR_NAME,
                                PROPERTY_TYPE_ATTR_NAME, "CDATA", "live");
        this.contentHandler.startElement(SOURCE_NS, PROPERTIES_NODE_NAME,
                                         PROPERTIES_NODE_QNAME, attributes);

        IncludeXMLConsumer consumer = new IncludeXMLConsumer(this.contentHandler);

        for (int i = 0; i<properties.length; i++) {
            property = properties[i];

            this.contentHandler.startPrefixMapping("",
                                                   property.getNamespace());
            property.toSAX(consumer);
            this.contentHandler.endPrefixMapping("");
        }

        this.contentHandler.endElement(SOURCE_NS, PROPERTIES_NODE_NAME,
                                       PROPERTIES_NODE_QNAME);
    }

    /**
     * Push a XML description about all properties, which
     * were computed by source inspectors.
     *
     * @param source Source
     */
    private void pushComputedSourceProperties(Source source)
      throws SAXException, SourceException {
        AttributesImpl attributes = new AttributesImpl();

        attributes.addAttribute("", PROPERTY_TYPE_ATTR_NAME,
                                PROPERTY_TYPE_ATTR_NAME, "CDATA", "computed");
        this.contentHandler.startElement(SOURCE_NS, PROPERTIES_NODE_NAME,
                                         PROPERTIES_NODE_QNAME, attributes);

        SourceInspector inspector = null;

        try {
            inspector = (SourceInspector) this.manager.lookup(SourceInspector.ROLE);

            SourceProperty[] properties = inspector.getSourceProperties(source);
            IncludeXMLConsumer consumer = new IncludeXMLConsumer(this.contentHandler);

            for (int i = 0; i<properties.length; i++) {
                this.contentHandler.startPrefixMapping("", properties[i].getNamespace());
                properties[i].toSAX(consumer);
                this.contentHandler.endPrefixMapping("");
            }
        } catch (ServiceException ce) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Could not retrieve source inspector", ce);
            }
        } finally {
            if (inspector!=null) {
                this.manager.release(inspector);
            }
        }

        this.contentHandler.endElement(SOURCE_NS, PROPERTIES_NODE_NAME,
                                       PROPERTIES_NODE_QNAME);
    }

    /**
     * Push a XML description of all permissions of a source.
     *
     * @param source Source
     */
    private void pushSourcePermissions(RestrictableSource source)
      throws SAXException, SourceException {
        SourcePermission[] permissions = source.getSourcePermissions();

        if ((permissions!=null) && (permissions.length>0)) {
            this.contentHandler.startElement(SOURCE_NS,
                                             PERMISSIONS_NODE_NAME,
                                             PERMISSIONS_NODE_QNAME,
                                             new AttributesImpl());

            for (int i = 0; i<permissions.length; i++) {
                AttributesImpl attributes = new AttributesImpl();

                if (permissions[i] instanceof PrincipalSourcePermission) {
                    attributes.addAttribute("", PRINCIPAL_ATTR_NAME,
                                            PRINCIPAL_ATTR_NAME, "CDATA",
                                            ((PrincipalSourcePermission) permissions[i]).getPrincipal());
                } else if (permissions[i] instanceof GroupSourcePermission) {
                    attributes.addAttribute("", GROUP_ATTR_NAME,
                                            GROUP_ATTR_NAME, "CDATA",
                                            ((GroupSourcePermission) permissions[i]).getGroup());
                }

                attributes.addAttribute("", PRIVILEGE_ATTR_NAME,
                                        PRIVILEGE_ATTR_NAME, "CDATA",
                                        permissions[i].getPrivilege());
                attributes.addAttribute("", INHERITABLE_ATTR_NAME,
                                        INHERITABLE_ATTR_NAME, "CDATA",
                                        String.valueOf(permissions[i].isInheritable()));
                attributes.addAttribute("", NEGATIVE_ATTR_NAME,
                                        NEGATIVE_ATTR_NAME, "CDATA",
                                        String.valueOf(permissions[i].isNegative()));

                this.contentHandler.startElement(SOURCE_NS,
                                                 PERMISSION_NODE_NAME,
                                                 PERMISSION_NODE_QNAME,
                                                 attributes);
                this.contentHandler.endElement(SOURCE_NS,
                                               PERMISSION_NODE_NAME,
                                               PERMISSION_NODE_QNAME);
            }

            this.contentHandler.endElement(SOURCE_NS, PERMISSIONS_NODE_NAME,
                                           PERMISSIONS_NODE_QNAME);
        }
    }

    /**
     * Push a XML description about all locks of a source.
     *
     * @param source Source
     */
    public void pushSourceLocks(LockableSource source)
      throws SAXException, SourceException {
        SourceLock[] locks = source.getSourceLocks();

        if (locks != null && locks.length > 0) {
            this.contentHandler.startElement(SOURCE_NS, LOCKS_NODE_NAME,
                                             LOCKS_NODE_QNAME,
                                             new AttributesImpl());

            for (int i = 0; locks.length > 0; i++) {
                SourceLock lock = locks[i];

                AttributesImpl attributes = new AttributesImpl();

                attributes = new AttributesImpl();
                attributes.addAttribute("", PRINCIPAL_ATTR_NAME,
                                        PRINCIPAL_ATTR_NAME, "CDATA",
                                        lock.getSubject());
                attributes.addAttribute("", TYPE_ATTR_NAME, TYPE_ATTR_NAME,
                                        "CDATA", lock.getType());
                attributes.addAttribute("", EXPIRATION_ATTR_NAME,
                                        EXPIRATION_ATTR_NAME, "CDATA",
                                        lock.getExpiration().toString());
                attributes.addAttribute("", INHERITABLE_ATTR_NAME,
                                        INHERITABLE_ATTR_NAME, "CDATA",
                                        String.valueOf(lock.isInheritable()));
                attributes.addAttribute("", EXCLUSIVE_ATTR_NAME,
                                        EXCLUSIVE_ATTR_NAME, "CDATA",
                                        String.valueOf(lock.isExclusive()));

                this.contentHandler.startElement(SOURCE_NS, LOCK_NODE_NAME,
                                                 LOCK_NODE_QNAME, attributes);
                this.contentHandler.endElement(SOURCE_NS, LOCK_NODE_NAME,
                                               LOCK_NODE_QNAME);

            }

            this.contentHandler.endElement(SOURCE_NS, LOCKS_NODE_NAME,
                                           LOCKS_NODE_QNAME);
        }
    }

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {

        Object uri;

        for (Enumeration e = cachedsources.keys(); e.hasMoreElements(); ) {
            uri = e.nextElement();
            this.resolver.release((Source) cachedsources.get(uri));
            cachedsources.remove(uri);
        }
        super.recycle();
    }

    /**
     * Release all resources.
     */
    public void dispose() {
        recycle();

        super.dispose();
    }
}

