/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.deployer.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.cocoon.deployer.block.BinaryBlock;
import org.apache.commons.transaction.file.FileResourceManager;
import org.apache.commons.transaction.file.ResourceManagerException;

/**
 * Utitily class to handle ZIP archives.
 */
public class ZipUtils {

	/**
	 * Get the block descriptor as <code>java.io.InputStream</code>.
	 * @param blockStream
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
    public static InputStream getDescriptorInputStream(ZipInputStream blockStream) 
		throws IOException, FileNotFoundException {
    
	    boolean found = false;
	    ZipEntry document = null;
	    
	    do {
	        document = blockStream.getNextEntry();
	        if (document != null) {
	            if (document.getName().equals(BinaryBlock.BLOCK_DESCRIPTOR_LOCATION)) {
	                found = true;
	            } else {
	                // go to next entry
	                blockStream.closeEntry();
	            }
	        }
	    } while (document != null && found == false);        
	    
	    if(found == false) {
	        String debugMsg = "The ZIP file doesn't contain " + BinaryBlock.BLOCK_DESCRIPTOR_LOCATION;
	        throw new FileNotFoundException(debugMsg);
	    }
	    
	    // now we will extract the document and write it into a byte array
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
	        byte[] buffer = new byte[8192];
	        int length = -1;
	        while (blockStream.available() > 0) {
	            length = blockStream.read(buffer, 0, 8192);
	            if (length > 0) {
	                baos.write(buffer, 0, length);
	            }
	        }
	    } finally {
	        blockStream.close();
	    }
	    baos.flush();
	    
	    return new ByteArrayInputStream(baos.toByteArray());        
	}

	public static InputStream getBlockDescriptorIs(File blockArchive) throws FileNotFoundException, IOException {
		return getDescriptorInputStream(new ZipInputStream(new FileInputStream(blockArchive)));
	}	
	
    /**
     * Write (extract) a zip file into an <code>java.io.OutputStream</code>
     * 
     * @param blockStream the <code>java.util.zip.ZipInputStream</code> that should be extracted
     * @param out - the extractionTarget
     * @throws IOException - if problems during extracting occur
     * @throws ResourceManagerException 
     */
    public static void extractZip(ZipInputStream blockStream, FileResourceManager frm, String txId, String targetDir) 
        throws IOException, ResourceManagerException {
        ZipEntry document = null;
        try {
            do {
                document = blockStream.getNextEntry();
                if (document != null) {
                    // skip directories (only files have to be written)
                    if (document.isDirectory()) {
                        blockStream.closeEntry();
                        continue;
                    }
                    OutputStream out = frm.writeResource(
                            txId, targetDir + File.separator + document.getName());                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    // loop over ZIP entry stream
                    byte[] buffer = new byte[8192];
                    int length = -1;
                    while (blockStream.available() > 0) {
                        length = blockStream.read(buffer, 0, 8192);
                        if (length > 0) {
                            baos.write(buffer, 0, length);
                        }
                    }
                    // write it to the output stream provided by the file resource manager
                    out.write(baos.toByteArray());
                    // go to next entry
                    blockStream.closeEntry();
                }
            } while (document != null);
        } finally {
            blockStream.close();
        }
    }	
	
	
}
