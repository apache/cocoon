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
package org.apache.cocoon.maven.deployer.utils;

import java.io.File;
import java.io.IOException;

import org.apache.cocoon.maven.deployer.monolithic.DeploymentException;

/**
 * Utitily class to handle ZIP archives.
 * 
 * @version $Id$
 */
public class FileUtils {

	/**
	 * Delete a directory recursivly
	 * @param directory
	 * @return true if deletation went okay
	 */
    public static boolean deleteDirRecursivly(File directory) {
        if (directory.isDirectory()) {
            String[] children = directory.list();
            for (int i=0; i < children.length; i++) {
                boolean success = deleteDirRecursivly(new File(directory, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return directory.delete();
    }	

	/**
	 * Create the directories of a non-exisiting file.
	 */
	public static File createDirectory(File file) {
		if(file.isDirectory() || file.exists()) {
			return file;
		}
		String absolutePath;
        try {
            absolutePath = file.getCanonicalPath();
        } catch (IOException e) {
            throw new DeploymentException("A problem occured while reading the canonical path of '" + file.getAbsolutePath() + "'");
        }
		String absolutePathDir = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
		File absolutePathDirFile = new File(absolutePathDir);
		if(absolutePathDirFile.exists()) {
			return file;
		}
		if(!new File(absolutePathDir).mkdirs()) {
			throw new DeploymentException("Can't create directory '" + absolutePathDir + "'");
		}
		return file;
	}


	
}
