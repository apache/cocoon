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

*/

package org.apache.cocoon.components.flow.javascript.fom;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.tempuri.javac.JavaCompiler;
import org.tempuri.javac.JavaCompilerErrorHandler;
import org.tempuri.javac.JavaClassWriter;
import org.tempuri.javac.JavaClassWriterFactory;
import org.tempuri.javac.JavaClassReader;
import org.tempuri.javac.JavaClassReaderFactory;
import org.tempuri.javac.JavaSourceReader;
import org.tempuri.javac.JavaSourceReaderFactory;
import org.tempuri.javacImpl.eclipse.JavaCompilerImpl;

public class CompilingClassLoader extends ClassLoader {

    SourceResolver sourceResolver;
    JavaCompiler compiler;
    Map output = new HashMap();
    List sourcePath = new LinkedList();
    HashSet sourceListeners = new HashSet();

    public interface SourceListener {
        public void sourceCompiled(Source src);
        public void sourceCompilationError(Source src, String error);
    }

    protected Class findClass(String className) 
        throws ClassNotFoundException {
        byte[] bytes = compile(className);
        return defineClass(className, bytes, 0, bytes.length);
    }

    public CompilingClassLoader(ClassLoader parent,
                                SourceResolver sourceResolver) {
        super(parent);
        this.sourceResolver = sourceResolver;
        this.compiler = new JavaCompilerImpl();
        this.sourcePath.add("");
    }

    static class ClassCompilationException extends ClassNotFoundException {

        public ClassCompilationException(String msg) {
            super(msg);
        }

    }

    public void addSourceListener(SourceListener listener) {
        synchronized (sourceListeners) {
            sourceListeners.add(listener);
        }
    }

    public void removeSourceListener(SourceListener listener) {
        synchronized (sourceListeners) {
            sourceListeners.remove(listener);
        }
    }

    private void notifyListeners(Source src, String err) {
        SourceListener arr[];
        synchronized (sourceListeners) {
            arr = new SourceListener[sourceListeners.size()];
            sourceListeners.toArray(arr);
        }
        if (err != null) {
            for (int i = 0; i < arr.length; i++) {
                arr[i].sourceCompilationError(src, err);
            }
        } else {
            for (int i = 0; i < arr.length; i++) {
                arr[i].sourceCompiled(src);
            }
        }
    }

    public void setSourcePath(String[] path) {
        synchronized (sourcePath) {
            sourcePath.clear();
            for (int i = 0; i < path.length; i++) {
                sourcePath.add(path[i]);
            }
            sourcePath.add("");
        }
    }

    
    private Source getSource(String className) {
        synchronized (sourcePath) {
            Iterator iter = sourcePath.iterator();
            while (iter.hasNext()) {
                String prefix = (String)iter.next();
                if (prefix.length() > 0) {
                    if (!prefix.endsWith("/")) {
                        prefix = prefix + "/";
                    }
                }
                String uri = prefix + className.replace('.', '/') + ".java";
                Source src;
                try {
                    src = sourceResolver.resolveURI(uri);
                } catch (MalformedURLException ignored) {
                    continue;
                } catch (IOException ignored) {
                    continue;
                }
                if (src.exists()) {
                    return src;
                }
                releaseSource(src);
            }
            return null;
        }
    }

    private void releaseSource(Source src) {
        sourceResolver.release(src);
    }

    class SourceReaderFactory implements JavaSourceReaderFactory {
        public JavaSourceReader 
            getSourceReader(final String className) 
            throws IOException {
            Source src = getSource(className);
            if (src == null) return null;
            try {
                InputStream is = src.getInputStream();
                byte[] buf = new byte[8192];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int count;
                while ((count = is.read(buf, 0, buf.length)) > 0) {
                    baos.write(buf, 0, count);
                }
                baos.flush();
                final Reader reader = 
                    new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())));;
                return new JavaSourceReader() {
                        public Reader getReader() {
                            return reader;
                        }
                        public String getClassName() {
                            return className;
                        }
                    };
            } finally {
                releaseSource(src);
            }
        }
    }

    private String makeFileName(String className) 
        throws IOException {
        Source src = getSource(className);
        if (src != null) {
            String result = src.getURI();
            releaseSource(src);
            return result;
        }
        return className;
    }
    
    class ClassReaderFactory 
        implements JavaClassReaderFactory {
        
        public JavaClassReader 
            getClassReader(final String className) 
		throws IOException {
		final byte[] bytes = 
		    (byte[])output.get(className);
		if (bytes != null) {
		    return new JavaClassReader() {
			    public String getClassName() {
				return className;
			    }
			    
			    public InputStream getInputStream() {
				
				return new ByteArrayInputStream(bytes);
			    }
			};
		}
                String classFile = className.replace('.', '/') + ".class";
                final InputStream is = getResourceAsStream(classFile);
                if (is == null) {
                    return null;
                }
                return new JavaClassReader() {
                        public String getClassName() {
                            return className;
                        }
                        
                        public InputStream getInputStream() {
                            return is;
                        }
                    };
	    }
	}

	class ClassWriterFactory 
	    implements JavaClassWriterFactory {

	    public JavaClassWriter 
		getClassWriter(final String className) {
		return new JavaClassWriter() {
			public String 
			    getClassName() {
			    return className;
			}
			public void writeClass(InputStream contents) 
			    throws IOException {
			    byte[] buf = new byte[2048];
			    ByteArrayOutputStream s =
				new ByteArrayOutputStream();
			    int count;
			    while ((count = 
				    contents.read(buf, 0, 
						  buf.length)) > 0) {
				s.write(buf, 0, count);
			    }
			    s.flush();
			    System.out.println("Compiled: " + className);
			    output.put(className,
				    s.toByteArray());
                            Source src = getSource(className);
                            notifyListeners(src, null);
                            releaseSource(src);
			}
		    };
	    }
	}
	
	class ErrorHandler implements JavaCompilerErrorHandler {

            List errList = new LinkedList();
            
	    public void handleError(String className,
				    int line,
				    int column,
				    Object errorMessage) {
		String msg = className;
		try {
		    // try to it convert to a file name
		    msg =
			makeFileName(className);
		} catch (Exception ignored) {
		    // oh well, I tried
		}
		if (line > 0) {
		    msg += ": Line " + line;
		}
		if (column >= 0) {
		    msg += "." + column;
		}
		msg += ": ";
		msg += errorMessage;
                errList.add(msg);
	    }

            public List getErrorList() {
                return errList;
            }
	}


	private byte[] compile(String className) 
            throws ClassNotFoundException {
	    byte[] result = (byte[])output.get(className);
	    if (result != null) {
		return result;
	    }
            Source src = getSource(className);
            if (src == null) {
                throw new ClassNotFoundException(className);
            }
            try {
                // try to compile it
                ErrorHandler errorHandler = new ErrorHandler();
                compiler.compile(new String[] {className},
                                 new SourceReaderFactory(),
                                 new ClassReaderFactory(),
                                 new ClassWriterFactory(),
                                 errorHandler);
                List errorList = errorHandler.getErrorList();
                if (errorList.size() > 0) {
                    String msg = "Failed to compile Java class " + className +": ";
                    Iterator iter = errorList.iterator();
                    while (iter.hasNext()) {
                        msg += "\n";
                        msg += (String)iter.next();
                    }
                    notifyListeners(src, msg);
                    throw new ClassCompilationException(msg);
                    
                }
                return (byte[])output.get(className);
            } finally {
                releaseSource(src);
            }
	}
}


