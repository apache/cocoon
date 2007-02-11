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
package org.apache.cocoon.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.servlet.multipart.Part;

/**
 * @author stefano
 */
public class Repository {
    
    public static final String FILE_NAME = "document";
    
    private static Repository instance;
    
    private Repository() {
        // do nothing;
    }
    
    public static Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }

    private static File getDir(String dirName) {
        File dir = new File(dirName);
        if (!dir.isDirectory()) throw new RuntimeException("'"+ dirName + "' is not a directory!");
        return dir;
    }

    public static void save(Request request, String dirName) throws Exception {
        File dir = getDir(dirName);
        
        Enumeration params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String name = (String) params.nextElement();
            if (name.indexOf("..") > -1) throw new Exception("We are under attack!!");
//System.out.println("[param] " + name);
            if (name.startsWith("save:")) {
                Part part = (Part) request.get(name);
                String code = name.substring(5);
                File file = new File(dir, code);
                save(part,file);
            } else if (name.startsWith("delete:")) {
                String value = request.getParameter(name);
                if (value.length() > 0) {               
                    String code = name.substring(7);
                    File file = new File(dir, code);
//System.out.println("[delete] " + file);
                    remove(file);
                }
            }
        }
    }

	public static void fomSave(FOM_Cocoon cocoon, String dirName) throws Exception {
		save(cocoon.getRequest(), dirName);
	}
    
    public static void save(Request request, String param, String file) throws Exception {
        Part part = (Part) request.get(param);
        save(part,new File(file));
    }
    
    public static void save(Part part, File file) throws Exception {
//System.out.println("[upload] " + part.getFileName() + " -> " + file);
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = part.getInputStream();
            out = new FileOutputStream(file);
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
    
    public static OutputStream getOutputStream(String dir) throws IOException {
        String mainFile = dir + "/" + FILE_NAME + ".xml";
        String versionedFile = dir + "/" + FILE_NAME + "." + getVersionID(dir) + ".xml";
        copy(mainFile, versionedFile);
        return new FileOutputStream(mainFile);
    }

    public static void revertFrom(String dir, int version) throws IOException {
        String mainFile = dir + "/" + FILE_NAME + ".xml";
        String versionedFile = dir + "/" + FILE_NAME + "." + version + ".xml";
        copy(versionedFile,mainFile);
    }
    
    /**
     * Returns the highest version id of the files included in the given 
     * directory.
     */
    public static int getVersionID(String dirName) {
        File dir = getDir(dirName);
        
        File[] content = dir.listFiles();
        int id = 0;
        for (int i = 0; i < content.length; i++) {
            if (content[i].isFile())  {
                try {
                    int localid = getVersion(content[i].getName());
                    if (localid > id) id = localid;
                } catch (Exception e) {}
            }
        }

        return ++id;
    }

    public static Object[] getVersions(String dirName) {
        File dir = getDir(dirName);
        ArrayList versions = new ArrayList();

        File[] content = dir.listFiles();
        for (int i = 0; i < content.length; i++) {
            if (content[i].isFile())  {
                try {
                    int version = getVersion(content[i].getName());
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
    
    public static int getID(String dirName) {
        File dir = getDir(dirName);

        File[] content = dir.listFiles();
        int id = 0;
        for (int i = 0; i < content.length; i++) {
            if (content[i].isDirectory())  {
                try {
                    String name = content[i].getName();
                    int localid = Integer.parseInt(name);
                    if (localid > id) id = localid;
                } catch (Exception e) {}
            }
        }

        return ++id;
    }
    
    public static boolean remove(String fileName) {
        return remove(new File(fileName));
    }
    
    public static boolean remove(File file) {
        boolean success = true;
        
        if (file.isDirectory()) {
            File[] content = file.listFiles();
            for (int i = 0; i < content.length; i++) {
                success = remove(content[i]);
            }
            
        }
        
//System.out.println("[delete] " + file);
        success = file.delete();
        
        return success;     
    }
    
    public static void copy(String from, String to) throws IOException {
        copy(new File(from),new File(to));
    }    

    public static void copy(File from, File to) throws IOException {

//System.out.println("[copy] " + from + " -> " + to);
        
        if (!from.exists()) {
            throw new IOException("Cannot find source file/folder");
        }
        
        if (from.isDirectory()) {
            to.mkdirs();
            File[] content = from.listFiles();
            for (int i = 0; i < content.length; i++) {
                File src = content[i];
                copy(src,new File(to, src.getName()));
            }            
        } else {
            to.createNewFile();
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                in = new FileInputStream(from);
                out = new FileOutputStream(to);
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
