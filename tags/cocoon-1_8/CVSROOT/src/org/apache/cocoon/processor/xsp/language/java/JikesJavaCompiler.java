/*-- $Id: JikesJavaCompiler.java,v 1.1 2000-08-18 22:43:59 stefano Exp $ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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

 Code borrowed from the Jakarta Tomcat Project

 */

package org.apache.cocoon.processor.xsp.language.java;

import java.io.*;

public class JikesJavaCompiler
    implements JavaCompiler
{
    static final int OUTPUT_BUFFER_SIZE = 1024;
    static final int BUFFER_SIZE = 512;
    String encoding;
    String classpath;
    String compilerPath;
    String outdir;
    OutputStream out;

    class StreamPumper extends Thread
    {

        public void pumpStream()
            throws IOException
        {
            byte buf[] = new byte[BUFFER_SIZE];
            if(!endOfStream)
            {
                int bytesRead = stream.read(buf, 0, BUFFER_SIZE);
                if(bytesRead > 0)
                    out.write(buf, 0, bytesRead);
                else
                if(bytesRead == -1)
                    endOfStream = true;
            }
        }

        public void run()
        {
            try
            {
                while(!endOfStream) 
                {
                    pumpStream();
                    Thread.sleep(SLEEP_TIME);
                }

            }
            catch(InterruptedException ex) {}
            catch(IOException ex) {}
        }

        private BufferedInputStream stream;
        private boolean endOfStream;
        private boolean stopSignal;
        private int SLEEP_TIME;
        private OutputStream out;

        public StreamPumper(BufferedInputStream is, OutputStream out)
        {
            endOfStream = false;
            stopSignal = false;
            SLEEP_TIME = 5;
            stream = is;
            this.out = out;
        }
    }


    public JikesJavaCompiler()
    {
        compilerPath = "jikes";
    }

    public boolean compile(String source)
    {
        int exitValue = -1;
        String compilerCmd[] = {
            compilerPath, "-classpath", classpath, "-d", outdir, "-nowarn", source
        };
        ByteArrayOutputStream tmpErr = new ByteArrayOutputStream(OUTPUT_BUFFER_SIZE);
        try
        {
            Process p = Runtime.getRuntime().exec(compilerCmd);
            BufferedInputStream compilerErr = new BufferedInputStream(p.getErrorStream());
            StreamPumper errPumper = new StreamPumper(compilerErr, tmpErr);
            errPumper.start();
            p.waitFor();
            exitValue = p.exitValue();
            errPumper.join();
            compilerErr.close();
            p.destroy();
            tmpErr.close();
            tmpErr.writeTo(out);
        }
        catch(IOException ex)
        {
            return false;
        }
        catch(InterruptedException ex)
        {
            return false;
        }
        boolean isOkay = exitValue == 0;
        if(tmpErr.size() > 0)
            isOkay = false;
        return isOkay;
    }

    public void setClasspath(String classpath)
    {
        this.classpath = classpath;
    }

    public void setCompilerPath(String compilerPath)
    {
        this.compilerPath = compilerPath;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public void setMsgOutput(OutputStream out)
    {
        this.out = out;
    }

    public void setOutputDir(String outdir)
    {
        this.outdir = outdir;
    }
}
