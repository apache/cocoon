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
package org.apache.cocoon.components;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.servlet.multipart.Part;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.TraversableSource;

/**
 * @author stefano
 * @version CVS $Id$
 */
public class SourceRepository {

    public static final String FILE_NAME = "document";

    private static SourceRepository instance;

    private static ComponentManager manager;

    private SourceRepository() {
    	manager = CocoonComponentManager.getSitemapComponentManager();
    }

    public static SourceRepository getInstance() {
        if (instance == null) {
            instance = new SourceRepository();
        }
        return instance;
    }

    private static Source resolve(String uri)
    throws MalformedURLException, IOException {
        SourceResolver resolver = null;
        TraversableSource source;
        try {
            resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
            source = (TraversableSource) resolver.resolveURI(uri);
        } catch (ComponentException ce) {
            throw new IOException("ComponentException");
        } finally {
            manager.release((Component)resolver);
        }
        return source;
    }

    private static TraversableSource getCollection(String colName) {
    	TraversableSource source;
        try {
            source = (TraversableSource)resolve(colName);
        } catch (MalformedURLException e) {
            throw new RuntimeException("'unable to resolve source: malformed URL");
        } catch (IOException e) {
            throw new RuntimeException("'unable to resolve source: IOException");
        }
        if (!source.isCollection()) throw new RuntimeException(colName + " is not a collection!");
        return source;
    }

    public static void save(Request request, String dirName) throws Exception {
        TraversableSource collection = getCollection(dirName);
        ModifiableTraversableSource result;

        Enumeration params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String name = (String) params.nextElement();
            if (name.indexOf("..") > -1) throw new Exception("We are under attack!!");
//System.out.println("[param] " + name);
            if (name.startsWith("save:")) {
                Part part = (Part) request.get(name);
                String code = name.substring(5);
                if (!(collection instanceof ModifiableSource)) {
                	throw new RuntimeException("Cannot modify the given source");
                }
                result = (ModifiableTraversableSource)resolve(collection.getURI() + "/" + code);

                save(part, result);
            } else if (name.startsWith("delete:")) {
                String value = request.getParameter(name);
                if (value.length() > 0) {
                    String code = name.substring(7);
					result = (ModifiableTraversableSource)resolve(collection + "/" + code);
                    remove(result);
                }
            }
        }
    }

    public static void save(Request request, String param, String dest) throws Exception {
        Part part = (Part) request.get(param);
        save(part, (ModifiableTraversableSource)resolve(dest));
    }

    public static void save(Part part, ModifiableTraversableSource destination) throws Exception {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = part.getInputStream();
            out = destination.getOutputStream();
            copy(in, out);
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

    public static OutputStream getOutputStream(String collection) throws IOException {
        String mainResource = collection + "/" + FILE_NAME + ".xml";
        String versionedResource = collection + "/" + FILE_NAME + "." + getVersionID(collection) + ".xml";
        copy(mainResource, versionedResource);
        return ((ModifiableSource)resolve(mainResource)).getOutputStream();
    }

    public static void revertFrom(String collection, int version) throws IOException {
        String mainResource = collection + "/" + FILE_NAME + ".xml";
        String versionedResource = collection + "/" + FILE_NAME + "." + version + ".xml";
        copy(versionedResource,mainResource);
    }

    /**
     * Returns the highest version id of the files included in the given
     * directory.
     */
    public static int getVersionID(String colName) {
        TraversableSource collection = getCollection(colName);
        int id = 0;
        Collection contents;
        try {
            contents = collection.getChildren();
        } catch (SourceException se) {
        	throw new RuntimeException("Unable to list contents for collection " + colName);
        }
        for (Iterator iter = contents.iterator(); iter.hasNext();) {
            TraversableSource content = (TraversableSource) iter.next();
            if (!content.isCollection()) {
				try {
					int localid = getVersion(content.getName());
					if (localid > id) id = localid;
				} catch (Exception e) {}

            }
        }

        return ++id;
    }

    public static Object[] getVersions(String colName) {
    	TraversableSource collection = getCollection(colName);
        ArrayList versions = new ArrayList();

		Collection contents;
		try {
			contents = collection.getChildren();
		} catch (SourceException se) {
			throw new RuntimeException("Unable to list contents for collection " + colName);
		}

        for (Iterator iter = contents.iterator(); iter.hasNext();) {
            TraversableSource content = (TraversableSource) iter.next();
			if (!content.isCollection())  {
				 try {
					 int version = getVersion(content.getName());
					 if (version > 0) {
						 versions.add(new Integer(version));
					 }
				 } catch (Exception e) {}
			 }

        }

        return versions.toArray();
    }

    /**
     * Return the version encoded into the name as a numeric subextension of
     * an .xml extension.
     *
     * Example:
     *  anything.123.xml -> 123
     *  document.3.xml -> 3
     *  document.0.xml -> 0
     *  document.xml -> -1
     *  image.0.jpg -> -1
     */
    private static int getVersion(String name) {
        int extIndex = name.lastIndexOf(".xml");
        if (extIndex > 0) {
            String nameWithoutExtension = name.substring(0,extIndex);
            int dotIndex = nameWithoutExtension.lastIndexOf('.');
            if (dotIndex > 0) {
                String localidString = nameWithoutExtension.substring(dotIndex + 1);
                return Integer.parseInt(localidString);
            }
        }
        return -1;
    }

    public static int getID(String colName) {
        TraversableSource collection = getCollection(colName);

        int id = 0;
		Collection contents;
		try {
			contents = collection.getChildren();
		} catch (SourceException se) {
			throw new RuntimeException("Unable to list contents for collection " + colName);
		}

		for (Iterator iter = contents.iterator(); iter.hasNext();) {
            TraversableSource content = (TraversableSource) iter.next();
			if (content.isCollection())  {
				try {
					String name = content.getName();
					int localid = Integer.parseInt(name);
					if (localid > id) id = localid;
				} catch (Exception e) {}
			}
        }
        return ++id;
    }

    public static boolean remove(String resourceName) {
        try {
            return remove((ModifiableTraversableSource)resolve(resourceName));
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException e) {
			return false;
        }

    }

    public static boolean remove(ModifiableTraversableSource resource) {
        boolean success = true;

        if (resource.isCollection()) {
			Collection contents;
			try {
				contents = resource.getChildren();
			} catch (SourceException se) {
				throw new RuntimeException("Unable to list contents for collection " + resource);
			}
			for (Iterator iter = contents.iterator(); iter.hasNext();) {
                ModifiableTraversableSource element = (ModifiableTraversableSource) iter.next();
                success = remove(element);
            }

        }
        try {
            resource.delete();
            return success;
        } catch (SourceException e) {
        	return false;
        }

    }

    public static void copy(String from, String to) throws IOException {
        copy((ModifiableTraversableSource)resolve(from), (ModifiableTraversableSource)resolve(to));
    }

    public static void copy(ModifiableTraversableSource from, ModifiableTraversableSource to) throws IOException {

        if (!from.exists()) {
            throw new IOException("Cannot find source file/folder");
        }

        if (from.isCollection()) {
            to.makeCollection();
			Collection contents;
			try {
				contents = from.getChildren();
			} catch (SourceException se) {
				throw new RuntimeException("Unable to list contents for collection " + from);
			}
			for (Iterator iter = contents.iterator(); iter.hasNext();) {
				ModifiableTraversableSource src = (ModifiableTraversableSource) iter.next();
				SourceUtil.copy(src, resolve(to.getURI() + "/" + src.getName()));

			}
        } else {
            to = (ModifiableTraversableSource)resolve(to.getURI());
            InputStream in = null;
            OutputStream out = null;
            try {
                in = from.getInputStream();
                out = to.getOutputStream();
                copy(in,out);
            } finally {
                if (out != null) out.close();
                if (in != null) in.close();
            }
        }
    }

    public static void copy(InputStream from, OutputStream to) throws IOException {
        byte[] buffer = new byte[64 * 1024];
        int count = 0;
        do {
            to.write(buffer, 0, count);
            count = from.read(buffer, 0, buffer.length);
        } while (count != -1);
    }

}
