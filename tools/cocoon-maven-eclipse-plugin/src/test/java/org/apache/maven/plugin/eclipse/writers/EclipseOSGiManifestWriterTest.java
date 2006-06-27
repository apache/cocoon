/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.maven.plugin.eclipse.writers;

import java.io.File;

import junit.framework.TestCase;

public class EclipseOSGiManifestWriterTest extends TestCase 
{

    public void testFileParsing() throws Exception {
        EclipseOSGiManifestWriter writer = new EclipseOSGiManifestWriter(null, null, null, null);
        System.out.println(writer.rewriteManifest(new File("F:/os/cocoon/c30/core/cocoon-core/META-INF/MANIFEST.MF"), "lib"));
    }
    
}
