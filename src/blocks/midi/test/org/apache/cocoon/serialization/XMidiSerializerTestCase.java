/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

package org.apache.cocoon.serialization;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

/**
 * Test case for the MIDISerializer
 * @author Mark Leicester
 */
public class XMidiSerializerTestCase extends SitemapComponentTestCase
{

  public XMidiSerializerTestCase(String name)
  {
    super(name);
  }

  public void testMIDISerializer() throws Exception
  {
    String type = "midi";
    String input = "resource://org/apache/cocoon/generation/prelude.xmi";
    Parameters parameters = new Parameters();
    String control = "resource://org/apache/cocoon/generation/prelude.mid";

		assertIdentical(loadByteArray(control), serialize(type, parameters, load(input)));
  }
}
