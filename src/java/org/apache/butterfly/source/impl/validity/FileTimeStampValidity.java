/*
 * Copyright 2004, Ugo Cei.
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.butterfly.source.impl.validity;

import java.io.File;

import org.apache.butterfly.source.SourceValidity;


/**
 * Description of FileTimeStampValidity.
 * 
 * @version CVS $Id: FileTimeStampValidity.java,v 1.1 2004/07/23 08:47:20 ugo Exp $
 */
public class FileTimeStampValidity implements SourceValidity {
    private long timeStamp;
    private File file;

    public FileTimeStampValidity( final String filename )
    {
        this( new File( filename ) );
    }

    public FileTimeStampValidity( final File file )
    {
        this( file, file.lastModified() );
    }

    public FileTimeStampValidity( final File file,
                                  final long timeStamp )
    {
        this.file = file;
        this.timeStamp = timeStamp;
    }

    /**
     * Check if the component is still valid.
     * If <code>0</code> is returned the isValid(SourceValidity) must be
     * called afterwards!
     * If -1 is returned, the component is not valid anymore and if +1
     * is returnd, the component is valid.
     */
    public int isValid()
    {
        return ( this.file.lastModified() == this.timeStamp ? 1 : -1 );
    }

    public int isValid( final SourceValidity newValidity )
    {
        if( newValidity instanceof FileTimeStampValidity )
        {
            final long timeStamp =
                ( (FileTimeStampValidity)newValidity ).getTimeStamp();
            return ( this.timeStamp == timeStamp ? 1 : -1);
        }
        return -1;
    }

    public File getFile()
    {
        return this.file;
    }

    public long getTimeStamp()
    {
        return this.timeStamp;
    }

    public String toString()
    {
        return "FileTimeStampValidity: " + this.file.getPath() + ": " + this.timeStamp;
    }
}
