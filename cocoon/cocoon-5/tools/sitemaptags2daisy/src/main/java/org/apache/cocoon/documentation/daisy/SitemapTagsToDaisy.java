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
package org.apache.cocoon.documentation.daisy;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.DocletTag;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.outerj.daisy.htmlcleaner.HtmlCleanerFactory;
import org.outerj.daisy.htmlcleaner.HtmlCleanerTemplate;
import org.outerj.daisy.repository.*;
import org.outerj.daisy.repository.query.QueryHelper;
import org.outerj.daisy.repository.clientimpl.RemoteRepositoryManager;
import org.xml.sax.InputSource;
import org.outerx.daisy.x10.SearchResultDocument;

/**
 * A tool to sync the information about sitemap components from the Java
 * sources to the Daisy documentation, supports custom annotations using
 * cocoon.sitemap.* tags. See the README.txt for more details.
 *
 * <p>Reuses some ideas from the Ant SitemapTask in Cocoon 2.1.
 */
public class SitemapTagsToDaisy {
    private static final String GENERATOR = "org.apache.cocoon.generation.Generator";
    private static final String TRANSFORMER = "org.apache.cocoon.transformation.Transformer";
    private static final String SERIALIZER = "org.apache.cocoon.serialization.Serializer";
    private static final String READER = "org.apache.cocoon.reading.Reader";
    private static final String MATCHER = "org.apache.cocoon.matching.Matcher";
    private static final String SELECTOR = "org.apache.cocoon.selection.Selector";
    private static final String ACTION = "org.apache.cocoon.acting.Action";
    private static final String PIPELINE = "org.apache.cocoon.components.pipeline.ProcessingPipeline";

    private static final String CACHEABLE = "org.apache.cocoon.caching.CacheableProcessingComponent";
    private static final String DEPRECATED_CACHEABLE = "org.apache.cocoon.caching.Cacheable";

    /** The name of the component in the sitemap (required) */
    public static final String NAME_TAG   = "cocoon.sitemap.component.name";
    /** If this tag is specified no documentation is generated (optional) */
    public static final String NO_DOC_TAG = "cocoon.sitemap.component.documentation.disabled";
    /** The documentation (optional) */
    public static final String DOC_TAG    = "cocoon.sitemap.component.documentation";
    /** Caching info (optional) */
    public static final String CACHING_INFO_TAG = "cocoon.sitemap.component.documentation.caching";

    private static final String DAISY_BLOCK_FIELD = "CocoonBlock";
    private static final String DAISY_CLASSNAME_FIELD = "JavaClassName";
    private static final String DAISY_COMPONENT_TYPE_FIELD = "CocoonComponentReference";
    private static final String DAISY_CACHEABLE_FIELD = "SitemapComponentCacheabilityInfo";
    private static final String DAISY_DEPRECATED_FIELD = "SitemapComponentDeprecationInfo";
    private static final String DAISY_COMPONENT_NAME_FIELD = "SitemapComponentName";
    private static final String DAISY_SHORTDESCR_PART = "SitemapComponentShortDescription";
    private static final String DAISY_DOCTYPE = "SitemapComponent";

    private static final String COLLECTION_PREFIX = "cdocs-";

    private HtmlCleanerTemplate htmlCleanerTemplate;
    private Repository daisyRepository;
    private boolean simulate;

    public static void main(String[] args) throws Exception {
        new SitemapTagsToDaisy().run();
    }

    public void run() throws Exception {
        System.out.println();
        System.out.println("===================================================================================");
        System.out.println("This is the source annotations to Daisy documents sync tool for sitemap components.");
        System.out.println("===================================================================================");
        System.out.println();

        String rootPath = prompt("Enter the path to the Cocoon source directory:");
        File rootDir = new File(rootPath);
        File cocoonCoreDir = new File(new File(rootDir, "core"), "cocoon-core");
        if (!cocoonCoreDir.exists()) {
            System.out.println("This is not a valid Cocoon source directory, did not find the following path: " + cocoonCoreDir.getPath());
            System.exit(1);
        }
        // Normalize path
        rootPath = rootDir.toURL().getPath();

        setupHtmlCleanerTemplate();

        String daisyServer = prompt("Enter the address of the Daisy repository server, e.g. http://cocoon.zones.apache.org:9263 :");
        String daisyUser = prompt("Enter you Daisy user name (you need to have the Administrator role): ");
        String daisyPwd = prompt("Enter you Daisy password: ");
        setupDaisyRepositoryClient(daisyServer, daisyUser, daisyPwd);

        while (true) {
            String simulateInput = prompt("Run in simulate mode (won't update documents in Daisy) [yes/no] : ");
            if (simulateInput.equals("yes") || simulateInput.equals("no")) {
                simulate = simulateInput.equals("yes");
                break;
            } else {
                System.out.println("Enter yes or no.");
            }
        }

        JavaDocBuilder javaDocBuilder = getJavaDocBuilder(rootPath);

        JavaClass[] allClasses = javaDocBuilder.getClasses();
        int newDocs = 0;
        int updatedDocs = 0;
        int unmodifiedDocs = 0;

        try {
            for (int i = 0; i < allClasses.length; i++) {
                JavaClass currentClass = allClasses[i];
                if (!currentClass.isAbstract() && !currentClass.isPrivate() && !currentClass.isProtected()
                        &&!currentClass.isInterface() &&!currentClass.isInner()) {
                    String componentType = getComponentTypeName(currentClass);
                    if (componentType != null) {
                        if (currentClass.getTagByName(NO_DOC_TAG) != null)
                            continue;

                        String blockName = determineBlock(rootPath, currentClass);
                        if (blockName == null)
                            continue;

                        ComponentDocs docs = getComponentDocs(currentClass, componentType, blockName);

                        Document daisyDoc = getDaisyDoc(docs);
                        if (daisyDoc == null) {
                            System.out.println("Creating the document for " + currentClass.getFullyQualifiedName());
                            createDocument(docs, currentClass.getName());
                            newDocs++;
                        } else {
                            ComponentDocs oldDocs = getComponentDocs(daisyDoc);
                            boolean collectionsNeedUpdating = updateCollections(daisyDoc, blockName);
                            if (collectionsNeedUpdating || !docs.equals(oldDocs)) {
                                System.out.println("Will update the document for " + currentClass.getFullyQualifiedName());
                                updateDocument(daisyDoc, docs);
                                updatedDocs++;
                            } else {
                                System.out.println("No update required for " + currentClass.getFullyQualifiedName());
                                unmodifiedDocs++;
                            }
                        }
                    }
                }
            }
        } finally {
            System.out.println("New documents: " + newDocs);
            System.out.println("Updated documents: " + updatedDocs);
            System.out.println("Unmodified documents: " + unmodifiedDocs);
        }
    }

    private JavaDocBuilder getJavaDocBuilder(String rootPath) {
        File rootFile = new File(rootPath);

        System.out.println("Searching java directories in " + rootPath + " ...");
        List javaDirs = new ArrayList(100);
        collectJavaDirs(javaDirs, rootFile);
        System.out.println("Found " + javaDirs.size() + " java directories.");

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        Iterator javaDirsIt = javaDirs.iterator();
        while (javaDirsIt.hasNext()) {
            File currentDir = (File)javaDirsIt.next();
            System.out.println("Parsing sources in " + currentDir.getPath());
            javaDocBuilder.addSourceTree(currentDir);
        }
        System.out.println("Done parsing sources.");

        return javaDocBuilder;
    }

    private void collectJavaDirs(List javaDirs, File currentDir) {
        File[] files = currentDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                if (files[i].getName().equals("java")) {
                    javaDirs.add(files[i]);
                } else {
                    collectJavaDirs(javaDirs, files[i]);
                }
            }
        }
    }

    private String getComponentTypeName(JavaClass javaClass) {
        if (javaClass.isA(GENERATOR)) {
            return "Generator";
        } else if (javaClass.isA(TRANSFORMER)) {
            return "Transformer";
        } else if (javaClass.isA(READER)) {
            return "Reader";
        } else if (javaClass.isA(SERIALIZER)) {
            return "Serializer";
        } else if (javaClass.isA(ACTION)) {
            return "Action";
        } else if (javaClass.isA(MATCHER)) {
            return "Matcher";
        } else if (javaClass.isA(SELECTOR)) {
            return "Selector";
        } else if (javaClass.isA(PIPELINE)) {
            return "Pipe";
        } else {
            return null;
        }
    }

    /**
     * Extracts the block name from the directory name.
     */
    private String determineBlock(String rootPath, JavaClass javaClass) {
        String path = javaClass.getSource().getURL().getPath();
        if (!path.startsWith(rootPath))
            return null;

        String subPath = path.substring(rootPath.length());
        if (subPath.startsWith("/"))
            subPath = subPath.substring(1);

        if (subPath.startsWith("blocks/"))
            subPath = subPath.substring("blocks/".length());
        else if (subPath.startsWith("blocks-tobeconverted/"))
            subPath = subPath.substring("blocks-tobeconverted/".length());
        else if (subPath.startsWith("core/"))
            return "core"; // all subprojects below core are considered to be block "core"
        else
            return null;

        if (!subPath.startsWith("cocoon-"))
            return null;

        int slashPos = subPath.indexOf("/");
        if (slashPos == -1) // very unlikely
            return null;

        String blockName = subPath.substring("cocoon-".length(), slashPos);
        return blockName;
    }

    /**
     * This class contains the information that is synced from the source file
     * to a Daisy document.
     */
    static class ComponentDocs {
        /** The name of the block in which this component occurs. */
        String blockName;
        /** Fully-qualified Java class name of the component. */
        String className;
        /** Component type: generator, transformer, etc. */
        String componentType;
        /** Short component name as used in the sitemamp. */
        String componentName;
        /** The short description of this component, as HTML. */
        String documentation;
        /** Information about the caceability of this component. */
        String cacheInfo;
        /** Deprecated info about this component.*/
        String deprecated;

        public boolean equals(Object obj) {
            ComponentDocs other = (ComponentDocs)obj;
            if (!safeComp(blockName, other.blockName))
                return false;
            if (!safeComp(className, other.className))
                return false;
            if (!safeComp(componentType, other.componentType))
                return false;
            if (!safeComp(componentName, other.componentName))
                return false;
            if (!safeComp(documentation, other.documentation))
                return false;
            if (!safeComp(cacheInfo, other.cacheInfo))
                return false;
            if (!safeComp(deprecated, other.deprecated))
                return false;
            return true;
        }

        private boolean safeComp(String value1, String value2) {
            if (value1 == null && value2 == null)
                return true;
            if (value1 == null || value2 == null)
                return false;
            else
                return value1.equals(value2);
        }

        public void dump() {
            System.out.println("blockName: " + blockName);
            System.out.println("className: " + className);
            System.out.println("componentType: " + componentType);
            System.out.println("componentName: " + componentName);
            System.out.println("documentation: " + documentation);
            System.out.println("cacheInfo: " + cacheInfo);
            System.out.println("deprecated: " + deprecated);
        }
    }

    /**
     * Gets a {@link ComponentDocs} instance by extracting info from a Java source file.
     */
    private ComponentDocs getComponentDocs(JavaClass javaClass, String componentType, String blockName) throws Exception {
        ComponentDocs componentDocs = new ComponentDocs();
        componentDocs.componentType = componentType;
        componentDocs.className = javaClass.getFullyQualifiedName();
        componentDocs.blockName = blockName;

        DocletTag documentationTag = javaClass.getTagByName(DOC_TAG);
        if (documentationTag != null) {
            String documentation = documentationTag.getValue();
            if (documentation != null) {
                componentDocs.documentation = cleanupDocumentation(documentation);
            }
        }

        // Sitemap component name
        DocletTag nameTag = javaClass.getTagByName(NAME_TAG);
        if (nameTag != null)
            componentDocs.componentName = nameTag.getValue();

        // Deprecated
        DocletTag deprecatedTag = javaClass.getTagByName("deprecated");
        if (deprecatedTag != null) {
            if (deprecatedTag.getValue() != null) {
                componentDocs.deprecated = limitLength("Yes: " + deprecatedTag.getValue());
            } else {
                componentDocs.deprecated = "Yes.";
            }
        }

        // Caching
        if (javaClass.isA(GENERATOR) || javaClass.isA(TRANSFORMER) || javaClass.isA(SERIALIZER) || javaClass.isA(READER)) {
            String cacheInfo;
            if (javaClass.isA(CACHEABLE)) {
                cacheInfo = getTagValue(javaClass, CACHING_INFO_TAG, null);
                if ( cacheInfo != null ) {
                    cacheInfo = limitLength("Yes - " + cacheInfo);
                } else {
                    cacheInfo = "Yes";
                }
            } else if (javaClass.isA(DEPRECATED_CACHEABLE)) {
                cacheInfo = getTagValue(javaClass, CACHING_INFO_TAG, null);
                if ( cacheInfo != null ) {
                    cacheInfo = limitLength("Yes (2.0 Caching) - " + cacheInfo);
                } else {
                    cacheInfo = "Yes (2.0 Caching)";
                }
            } else {
                cacheInfo = "No";
            }
            componentDocs.cacheInfo = cacheInfo;
        }

        return componentDocs;
    }

    private String getTagValue(JavaClass javaClass, String tagName, String defaultValue) {
        final DocletTag tag = javaClass.getTagByName( tagName );
        if (tag != null) {
            return tag.getValue();
        }
        return defaultValue;
    }

    private String limitLength(String value) {
        if (value.length() > 254)
            return value.substring(0, 254);
        else
            return value;
    }

    private String cleanupDocumentation(String documentation) throws Exception {
        // <code> tag is often used but not handled by Daisy, translate it to <tt>
        documentation = documentation.replaceAll("<code>", "<tt>");
        documentation = documentation.replaceAll("</code>", "</tt>");

        // make it into a complete HTML doc
        documentation = "<html><body>" + documentation + "</body></html>";

        // pull it through a HTMLCleaner
        return htmlCleanerTemplate.newHtmlCleaner().cleanToString(documentation);
    }

    private void setupHtmlCleanerTemplate() throws Exception {
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream("org/apache/cocoon/documentation/daisy/htmlcleaner.xml");
            htmlCleanerTemplate = new HtmlCleanerFactory().buildTemplate(new InputSource(is));
        } finally {
            if (is != null)
                is.close();
        }
    }

    private void setupDaisyRepositoryClient(String url, String username, String password) throws Exception {
        RepositoryManager repositoryManager = new RemoteRepositoryManager(url, new Credentials(username, password));
        this.daisyRepository = repositoryManager.getRepository(new Credentials(username, password));
    }

    /**
     * Gets the Daisy document for a certain component, based on a field containing
     * the fully qualified class name. Returns null if there is no such document, throws
     * an exception if there are multiple such documents.
     */
    private Document getDaisyDoc(ComponentDocs docs) throws Exception {
        String query = "select id where documentType = " + QueryHelper.formatString(DAISY_DOCTYPE)
                + " and $JavaClassName = " + QueryHelper.formatString(docs.className);
        SearchResultDocument.SearchResult.Rows.Row[] rows = daisyRepository.getQueryManager().performQuery(query, Locale.US).getSearchResult().getRows().getRowArray();
        if (rows.length == 0) {
            return null;
        } else if (rows.length > 1) {
            throw new Exception("There are multiple documents in Daisy for the class " + docs.className + ". Please correct this first.");
        } else {
            return daisyRepository.getDocument(rows[0].getDocumentId(), rows[0].getBranchId(), rows[0].getLanguageId(), true);
        }
    }

    /**
     * Gets a {@link ComponentDocs} instance from a Daisy document.
     */
    private ComponentDocs getComponentDocs(Document daisyDoc) throws Exception {
        ComponentDocs docs = new ComponentDocs();

        if (daisyDoc.hasField(DAISY_BLOCK_FIELD))
            docs.blockName = (String)daisyDoc.getField(DAISY_BLOCK_FIELD).getValue();

        if (daisyDoc.hasField(DAISY_CACHEABLE_FIELD))
            docs.cacheInfo = (String)daisyDoc.getField(DAISY_CACHEABLE_FIELD).getValue();

        if (daisyDoc.hasField(DAISY_CLASSNAME_FIELD))
            docs.className = (String)daisyDoc.getField(DAISY_CLASSNAME_FIELD).getValue();

        if (daisyDoc.hasField(DAISY_COMPONENT_NAME_FIELD))
            docs.componentName = (String)daisyDoc.getField(DAISY_COMPONENT_NAME_FIELD).getValue();

        if (daisyDoc.hasField(DAISY_COMPONENT_TYPE_FIELD))
            docs.componentType = (String)daisyDoc.getField(DAISY_COMPONENT_TYPE_FIELD).getValue();

        if (daisyDoc.hasField(DAISY_DEPRECATED_FIELD))
            docs.deprecated = (String)daisyDoc.getField(DAISY_DEPRECATED_FIELD).getValue();

        if (daisyDoc.hasPart(DAISY_SHORTDESCR_PART))
            docs.documentation = new String(daisyDoc.getPart(DAISY_SHORTDESCR_PART).getData(), "UTF-8");

        return docs;
    }

    /**
     * Updates the Daisy collections the document belongs too, if needed, and returns true if updated.
     */
    private boolean updateCollections(Document daisyDoc, String blockName) throws Exception {
        if (blockName != null) {
            boolean belongsToRequiredCollection = false;
            boolean updated = false;
            String expectedCollection = COLLECTION_PREFIX + blockName;

            DocumentCollection[] collections = daisyDoc.getCollections().getArray();
            for (int i = 0; i < collections.length; i++) {
                String collectionName = collections[i].getName();
                if (collectionName.equals(expectedCollection)) {
                    belongsToRequiredCollection = true;
                } else if (collectionName.startsWith(COLLECTION_PREFIX)) {
                    // belongs to cdocs- collection it shouldn't belong too
                    updated = true;
                    daisyDoc.removeFromCollection(collections[i]);
                }
            }

            if (!belongsToRequiredCollection) {
                DocumentCollection collection = null;
                CollectionManager collectionManager = daisyRepository.getCollectionManager();
                try {
                    try {
                        collection = collectionManager.getCollectionByName(expectedCollection, false);
                    } catch (CollectionNotFoundException e) {
                        // ok, collection will be null
                    }

                    if (collection == null) {
                        System.out.println("Will create collection " + expectedCollection);
                        if (!simulate) {
                            collection = collectionManager.createCollection(expectedCollection);
                            collection.save();
                        }
                    }
                } catch (RepositoryException e) {
                    throw new Exception("Error trying to get or create the collection " + expectedCollection, e);
                }

                if (!simulate) // when in simulate mode, collection might not have been created
                    daisyDoc.addToCollection(collection);

                updated = true;
            }

            return updated;
        } else {
            return false;
        }
    }

    private void updateDocument(Document daisyDoc, ComponentDocs docs) throws Exception {
        updateField(daisyDoc, DAISY_BLOCK_FIELD, docs.blockName);
        updateField(daisyDoc, DAISY_CACHEABLE_FIELD, docs.cacheInfo);
        updateField(daisyDoc, DAISY_CLASSNAME_FIELD, docs.className);
        updateField(daisyDoc, DAISY_COMPONENT_NAME_FIELD, docs.componentName);
        updateField(daisyDoc, DAISY_COMPONENT_TYPE_FIELD, docs.componentType);
        updateField(daisyDoc, DAISY_DEPRECATED_FIELD, docs.deprecated);

        if (docs.documentation != null) {
            boolean setPart = false;
            if (daisyDoc.hasPart(DAISY_SHORTDESCR_PART)) {
                String oldDescr = new String(daisyDoc.getPart(DAISY_SHORTDESCR_PART).getData(), "UTF-8");
                // Note: for parts we need to check ourselves if the content has not changed
                //  (in contrasts to fields, for which Daisy does the comparison itself)
                if (!oldDescr.equals(docs.documentation)) {
                    setPart = true;
                }
            } else {
                setPart = true;
            }
            if (setPart)
                daisyDoc.setPart(DAISY_SHORTDESCR_PART, "text/xml", docs.documentation.getBytes("UTF-8"));
        } else if (daisyDoc.hasPart(DAISY_SHORTDESCR_PART)) {
            daisyDoc.deletePart(DAISY_SHORTDESCR_PART);
        }

        if (!simulate)
            daisyDoc.save();
    }

    private void createDocument(ComponentDocs docs, String name) throws Exception {
        Document daisyDoc = daisyRepository.createDocument(name, DAISY_DOCTYPE);
        updateCollections(daisyDoc, docs.blockName);
        updateDocument(daisyDoc, docs);
    }

    private void updateField(Document daisyDoc, String fieldName, String value) throws Exception {
        if (value != null)
            daisyDoc.setField(fieldName, value);
        else if (daisyDoc.hasField(fieldName))
            daisyDoc.deleteField(fieldName);
    }

    public static String prompt(String message) throws Exception {
        System.out.println(message);
        System.out.flush();
        String input = null;
        while (input == null || input.trim().equals("")) {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            try {
                input = in.readLine();
            } catch (IOException e) {
                throw new Exception("Error reading input from console.", e);
            }
        }
        return input;
    }
}
