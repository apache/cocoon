/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.LockableSource;
import org.apache.cocoon.components.source.RestrictableSource;
import org.apache.cocoon.components.source.VersionableSource;
import org.apache.cocoon.components.source.helpers.GroupSourcePermission;
import org.apache.cocoon.components.source.helpers.PrincipalSourcePermission;
import org.apache.cocoon.components.source.helpers.SourceLock;
import org.apache.cocoon.components.source.helpers.SourcePermission;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This Generator augments the output of the TraversableGenerator.
 * <p>
 * It adds:
 * <ul>
 *  <li>version information if the Source implements VersionableSource.</li>
 *  <li>locking information if the Source implements Lockablesource.</li>
 *  <li>permission information if the Source implements RestrictableSource.</li>
 *  <li>and describes its SourceProperties if the Source implements InspectableSource.</li>
 * </ul>
 * </p>
 * <p>
 *  Sitemap parameters that can be specified to control processing are:
 *  <ul>
 *   <li><code>version</code> (<code>true</code>)
 *       whether to generate versioning information.</li>
 *   <li><code>locking</code> (<code>true</code>)
 *       whether to generate locking information.</li>
 *   <li><code>permission</code> (<code>true</code>)
 *       whether to generate permission information.</li>
 *   <li><code>properties</code> (<code>true</code>)
 *       whether to generate source property information.</li>
 *  </ul>
 * </p>
 *
 * @version $Id$
 */
public class TraversableSourceDescriptionGenerator extends TraversableGenerator {

    protected static final String MIME_TYPE_ATTR_NAME = "mimeType";

    private static final String REVISION_ATTR_NAME = "revision";
    private static final String REVISIONBRANCH_ATTR_NAME = "branch";

    private static final String PROPERTIES_NODE_NAME = "properties";
    private static final String PROPERTIES_NODE_QNAME = PREFIX + ":" + PROPERTIES_NODE_NAME;

    private static final String PERMISSIONS_NODE_NAME = "permissions";
    private static final String PERMISSIONS_NODE_QNAME = PREFIX + ":" + PERMISSIONS_NODE_NAME;
    private static final String PERMISSION_NODE_NAME = "permission";
    private static final String PERMISSION_NODE_QNAME = PREFIX + ":" + PERMISSION_NODE_NAME;

    private static final String LOCKS_NODE_NAME = "locks";
    private static final String LOCKS_NODE_QNAME = PREFIX + ":" + LOCKS_NODE_NAME;
    private static final String LOCK_NODE_NAME = "lock";
    private static final String LOCK_NODE_QNAME = PREFIX + ":" + LOCK_NODE_NAME;

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
        throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, location, parameters);

        this.properties = parameters.getParameterAsBoolean("properties", true);
        super.cacheKeyParList.add(String.valueOf(String.valueOf(this.properties)));

        this.permissions = parameters.getParameterAsBoolean("permissions", true);
        super.cacheKeyParList.add(String.valueOf(this.permissions));

        this.locks = parameters.getParameterAsBoolean("locks", true);
        super.cacheKeyParList.add(String.valueOf(this.locks));

        this.version = parameters.getParameterAsBoolean("version", true);
        super.cacheKeyParList.add(String.valueOf(this.version));

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("properties: " + this.properties);
            getLogger().debug("permissions: " + this.permissions);
            getLogger().debug("locks: " + this.locks);
            getLogger().debug("version: " + this.version);
        }
    }

    /**
     * Augments source nodes with additional information.
     *
     * @param source  the Source to describe.
     */
    protected final void addContent(TraversableSource source)
        throws SAXException, ProcessingException {

        super.addContent(source);
        try {
            if (this.properties && (source instanceof InspectableSource)) {
                pushSourceProperties((InspectableSource) source);
            }
            if (this.permissions && (source instanceof RestrictableSource)) {
                pushSourcePermissions((RestrictableSource) source);
            }
            if (this.locks && (source instanceof LockableSource)) {
                pushSourceLocks((LockableSource) source);
            }
        } catch (SourceException e) {
            throw new ProcessingException(e);
        }

    }

    /**
     * Augments source node elements with additional attributes describing the Source.
     * The additional attributes are a <code>mimeType</code> attribute,
     * and iff the Source is an instance of VersionableSource and the generator
     * is configured to output versioning information two attributes:
     * <code>revision</code> and <code>branch</code>.
     *
     * @param source  the Source to describe.
     */
    protected void setNodeAttributes(TraversableSource source) throws SAXException, ProcessingException {
        super.setNodeAttributes(source);
        if (!source.isCollection()) {
            String mimeType = source.getMimeType();
            if (mimeType != null) {
                attributes.addAttribute("", MIME_TYPE_ATTR_NAME, MIME_TYPE_ATTR_NAME,
                                        "CDATA", source.getMimeType());
            }
        }
        if (this.version && source instanceof VersionableSource) {
            try {
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
            } catch (SourceException e) {
                throw new ProcessingException(e);
            }
        }
    }

    /**
     * Push a XML description about all properties, which
     * the source owns.
     *
     * @param source  the Source to describe.
     */
    private void pushSourceProperties(InspectableSource source)
    throws SAXException, SourceException {

        SourceProperty[] properties = source.getSourceProperties();
        if (properties != null && properties.length > 0) {
            this.contentHandler.startElement(URI, PROPERTIES_NODE_NAME,
                                             PROPERTIES_NODE_QNAME, XMLUtils.EMPTY_ATTRIBUTES);
            for (int i = 0; i < properties.length; i++) {
                SourceProperty property = properties[i];
                property.toSAX(this.contentHandler);
            }
            this.contentHandler.endElement(URI, PROPERTIES_NODE_NAME,
                                           PROPERTIES_NODE_QNAME);
        }
    }


    /**
     * Push a XML description of all permissions of a source.
     *
     * @param source the Source to describe.
     */
    private void pushSourcePermissions(RestrictableSource source)
      throws SAXException, SourceException {
        SourcePermission[] permissions = source.getSourcePermissions();

        if (permissions != null && permissions.length > 0) {
            this.contentHandler.startElement(URI,
                                             PERMISSIONS_NODE_NAME,
                                             PERMISSIONS_NODE_QNAME,
                                             XMLUtils.EMPTY_ATTRIBUTES);

            for (int i = 0; i < permissions.length; i++) {
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

                this.contentHandler.startElement(URI,
                                                 PERMISSION_NODE_NAME,
                                                 PERMISSION_NODE_QNAME,
                                                 attributes);
                this.contentHandler.endElement(URI,
                                               PERMISSION_NODE_NAME,
                                               PERMISSION_NODE_QNAME);
            }

            this.contentHandler.endElement(URI, PERMISSIONS_NODE_NAME,
                                           PERMISSIONS_NODE_QNAME);
        }
    }

    /**
     * Push a XML description about all locks of a source.
     *
     * @param source the Source to describe.
     */
    public void pushSourceLocks(LockableSource source)
      throws SAXException, SourceException {
        SourceLock[] locks = source.getSourceLocks();

        if (locks != null && locks.length > 0) {
            this.contentHandler.startElement(URI, LOCKS_NODE_NAME,
                                             LOCKS_NODE_QNAME,
                                             XMLUtils.EMPTY_ATTRIBUTES);

            for (int i = 0; locks.length > 0; i++) {
                SourceLock lock = locks[i];

                AttributesImpl attributes = new AttributesImpl();
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

                this.contentHandler.startElement(URI, LOCK_NODE_NAME,
                                                 LOCK_NODE_QNAME, attributes);
                this.contentHandler.endElement(URI, LOCK_NODE_NAME,
                                               LOCK_NODE_QNAME);
            }

            this.contentHandler.endElement(URI, LOCKS_NODE_NAME,
                                           LOCKS_NODE_QNAME);
        }
    }

}
