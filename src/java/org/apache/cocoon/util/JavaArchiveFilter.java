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
package org.apache.cocoon.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Implements a filter for java archives.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</A>
 * @version CVS $Id: JavaArchiveFilter.java,v 1.2 2004/03/05 13:03:00 bdelacretaz Exp $
 */

public class JavaArchiveFilter implements FileFilter {

    public boolean accept(File file) {
        String name = file.getName().toLowerCase();
        return (name.endsWith(".jar") || name.endsWith(".zip"));
    }

}
