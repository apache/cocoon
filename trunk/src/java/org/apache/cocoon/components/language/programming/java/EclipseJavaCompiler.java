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
package org.apache.cocoon.components.language.programming.java;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.cocoon.components.language.programming.CompilerError;
import org.apache.cocoon.components.language.programming.LanguageCompiler;
import org.apache.cocoon.util.ClassUtils;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Eclipse Java Compiler
 *
 * @version CVS $Id: EclipseJavaCompiler.java,v 1.5 2004/03/08 13:58:32 cziegeler Exp $
 */
public class EclipseJavaCompiler implements LanguageCompiler, Recyclable {

    static boolean target14;
    static boolean source14;

    static {
        // Detect JDK version we are running under
        String version = System.getProperty("java.specification.version");
        try {
            source14 = target14 = Float.parseFloat(version) >= 1.4;
        } catch (NumberFormatException e) {
            source14 = target14 = false;
        }
    }

    boolean debug;

    String sourceDir;
    String sourceFile; 
    String destDir;
    String sourceEncoding;

    List errors = new LinkedList();


    public EclipseJavaCompiler() {
        this.debug = true;
        source14 = true;
    }

    public void recycle() {
        sourceFile = null;
        sourceDir = null;
        destDir = null;
        sourceEncoding = null;
        errors.clear();
    }

    public void setFile(String file) {
        // This is the absolute path to the file to be compiled
        this.sourceFile = file;
    }

    public void setSource(String srcDir) {
        // This is the "sourcepath" of the file to be
        // compiled
        this.sourceDir = srcDir;
    }

    public void setDestination(String destDir) {
        // This is the output directory)
        this.destDir = destDir;
    }

    public void setEncoding(String encoding) {
        this.sourceEncoding = encoding; 
    }

    /**
     * Eclipse Java compiler ignores class path setting and uses current
     * Java class loader
     * @param cp classpath to be ignored
     */
    public void setClasspath(String cp) {
        // Not used 
    }

    private String makeClassName(String fileName) throws IOException {
        File origFile = new File(fileName);
        String canonical = null;
        if (origFile.exists()) {
            canonical = origFile.getCanonicalPath().replace('\\', '/');
        }
        String str = fileName;
        str = str.replace('\\', '/');
        if (sourceDir != null) {
            String prefix = 
                new File(sourceDir).getCanonicalPath().replace('\\', '/');
            if (canonical != null) {
                if (canonical.startsWith(prefix)) {
                    String result = canonical.substring(prefix.length() + 1,
                                                        canonical.length() -5);
                    result = result.replace('/', '.');
                    return result;
                }
            } else {
                File t = new File(sourceDir, fileName);
                if (t.exists()) {
                    str = t.getCanonicalPath().replace('\\', '/');
                    String result = str.substring(prefix.length()+1,
                                                  str.length() - 5).replace('/', '.');
                    return result;
                }
            }
        }
        if (fileName.endsWith(".java")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        fileName = fileName.replace('\\', '.');
        return fileName.replace('/', '.');
    }

    public boolean compile() throws IOException {
        final String targetClassName = makeClassName(sourceFile);
        final ClassLoader classLoader = ClassUtils.getClassLoader();
        String[] fileNames = new String[] {sourceFile};
        String[] classNames = new String[] {targetClassName};
        class CompilationUnit implements ICompilationUnit {

            String className;
            String sourceFile;

            CompilationUnit(String sourceFile, String className) {
                this.className = className;
                this.sourceFile = sourceFile;
            }

            public char[] getFileName() {
                return className.toCharArray();
            }
            
            public char[] getContents() {
                char[] result = null;
                try {
                    Reader reader = new BufferedReader(new FileReader(sourceFile));
                    if (reader != null) {
                        char[] chars = new char[8192];
                        StringBuffer buf = new StringBuffer();
                        int count;
                        while ((count = reader.read(chars, 0, 
                                                    chars.length)) > 0) {
                            buf.append(chars, 0, count);
                        }
                        result = new char[buf.length()];
                        buf.getChars(0, result.length, result, 0);
                    }
                } catch (IOException e) {
                    handleError(className, -1, -1, 
                                e.getMessage());
                    //e.printStackTrace();
                }
                return result;
            }
            
            public char[] getMainTypeName() {
                int dot = className.lastIndexOf('.');
                if (dot > 0) {
                    return className.substring(dot + 1).toCharArray();
                }
                return className.toCharArray();
            }
            
            public char[][] getPackageName() {
                StringTokenizer izer = 
                    new StringTokenizer(className, ".");
                char[][] result = new char[izer.countTokens()-1][];
                for (int i = 0; i < result.length; i++) {
                    String tok = izer.nextToken();
                    result[i] = tok.toCharArray();
                }
                return result;
            }
        }


        final INameEnvironment env = new INameEnvironment() {

                public NameEnvironmentAnswer 
                    findType(char[][] compoundTypeName) {
                    String result = "";
                    String sep = "";
                    for (int i = 0; i < compoundTypeName.length; i++) {
                        result += sep;
                        result += new String(compoundTypeName[i]);
                        sep = ".";
                    }
                    return findType(result);
                }

                public NameEnvironmentAnswer 
                    findType(char[] typeName, 
                             char[][] packageName) {
                        String result = "";
                        String sep = "";
                        for (int i = 0; i < packageName.length; i++) {
                            result += sep;
                            result += new String(packageName[i]);
                            sep = ".";
                        }
                        result += sep;
                        result += new String(typeName);
                        return findType(result);
                }
                
                private NameEnvironmentAnswer findType(String className) {

                    try {
                        if (className.equals(targetClassName)) {
                            ICompilationUnit compilationUnit = 
                                new CompilationUnit(sourceFile, className);
                            return 
                                new NameEnvironmentAnswer(compilationUnit);
                        }
                        String resourceName = 
                            className.replace('.', '/') + ".class";
                        InputStream is = 
                            classLoader.getResourceAsStream(resourceName);
                        if (is != null) {
                            byte[] classBytes;
                            byte[] buf = new byte[8192];
                            ByteArrayOutputStream baos = 
                                new ByteArrayOutputStream(buf.length);
                            int count;
                            while ((count = is.read(buf, 0, buf.length)) > 0) {
                                baos.write(buf, 0, count);
                            }
                            baos.flush();
                            classBytes = baos.toByteArray();
                            char[] fileName = className.toCharArray();
                            ClassFileReader classFileReader = 
                                new ClassFileReader(classBytes, fileName, 
                                                    true);
                            return 
                                new NameEnvironmentAnswer(classFileReader);
                        }
                    } catch (IOException exc) {
                        handleError(className, -1, -1, 
                                    exc.getMessage());
                    } catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException exc) {
                        handleError(className, -1, -1, 
                                    exc.getMessage());
                    }
                    return null;
                }

                private boolean isPackage(String result) {
                    if (result.equals(targetClassName)) {
                        return false;
                    }
                    String resourceName = result.replace('.', '/') + ".class";
                    InputStream is = 
                        classLoader.getResourceAsStream(resourceName);
                    return is == null;
                }

                public boolean isPackage(char[][] parentPackageName, 
                                         char[] packageName) {
                    String result = "";
                    String sep = "";
                    if (parentPackageName != null) {
                        for (int i = 0; i < parentPackageName.length; i++) {
                            result += sep;
                            String str = new String(parentPackageName[i]);
                            result += str;
                            sep = ".";
                        }
                    }
                    String str = new String(packageName);
                    if (Character.isUpperCase(str.charAt(0))) {
                        if (!isPackage(result)) {
                            return false;
                        }
                    }
                    result += sep;
                    result += str;
                    return isPackage(result);
                }

                public void cleanup() {
                }

            };
        final IErrorHandlingPolicy policy = 
            DefaultErrorHandlingPolicies.proceedWithAllProblems();
        final Map settings = new HashMap();
        settings.put(CompilerOptions.OPTION_LineNumberAttribute,
                     CompilerOptions.GENERATE);
        settings.put(CompilerOptions.OPTION_SourceFileAttribute,
                     CompilerOptions.GENERATE);
        settings.put(CompilerOptions.OPTION_ReportDeprecation,
                     CompilerOptions.IGNORE);
        if (sourceEncoding != null) {
            settings.put(CompilerOptions.OPTION_Encoding,
                         sourceEncoding);
        }
        if (debug) {
            settings.put(CompilerOptions.OPTION_LocalVariableAttribute,
                         CompilerOptions.GENERATE);
        }
        if (source14) {
            settings.put(CompilerOptions.OPTION_Source,
                         CompilerOptions.VERSION_1_4);
        }
        if (target14) {
            settings.put(CompilerOptions.OPTION_TargetPlatform,
                         CompilerOptions.VERSION_1_4);
        }
        final IProblemFactory problemFactory = 
            new DefaultProblemFactory(Locale.getDefault());

        final ICompilerRequestor requestor = new ICompilerRequestor() {
                public void acceptResult(CompilationResult result) {
                    try {
                        if (result.hasProblems()) {
                            IProblem[] problems = result.getProblems();
                            for (int i = 0; i < problems.length; i++) {
                                IProblem problem = problems[i];
                                String name = 
                                    new String(problems[i].getOriginatingFileName());
                                handleError(name,
                                            problem.getSourceLineNumber(),
                                            -1,
                                            problem.getMessage());
                            }
                        } else {
                            ClassFile[] classFiles = result.getClassFiles();
                            for (int i = 0; i < classFiles.length; i++) {
                                ClassFile classFile = classFiles[i];
                                char[][] compoundName = 
                                    classFile.getCompoundName();
                                String className = "";
                                String sep = "";
                                for (int j = 0; 
                                     j < compoundName.length; j++) {
                                    className += sep;
                                    className += new String(compoundName[j]);
                                    sep = ".";
                                }
                                byte[] bytes = classFile.getBytes();
                                String outFile = destDir + "/" + 
                                    className.replace('.', '/') + ".class";
                                FileOutputStream fout = 
                                    new FileOutputStream(outFile);
                                BufferedOutputStream bos = 
                                    new BufferedOutputStream(fout);
                                bos.write(bytes);
                                bos.close();
                            }
                        }
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                }
            };
        ICompilationUnit[] compilationUnits = 
            new ICompilationUnit[classNames.length];
        for (int i = 0; i < compilationUnits.length; i++) {
            String className = classNames[i];
            compilationUnits[i] = new CompilationUnit(fileNames[i], className);
        }
        Compiler compiler = new Compiler(env,
                                         policy,
                                         settings,
                                         requestor,
                                         problemFactory);
        compiler.compile(compilationUnits);
        return errors.size() == 0;
    }

    void handleError(String className, int line, int column, Object errorMessage) {
        String fileName = 
            className.replace('.', File.separatorChar) + ".java";
        if (column < 0) column = 0;
        errors.add(new CompilerError(fileName,
                                     true,
                                     line,
                                     column,
                                     line,
                                     column,
                                     errorMessage.toString()));
    }

    public List getErrors() throws IOException {
        return errors;
    }
}
