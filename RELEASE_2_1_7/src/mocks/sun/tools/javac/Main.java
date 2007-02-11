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
package sun.tools.javac;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Main.java,v 1.2 2004/03/05 13:03:02 bdelacretaz Exp $
 */
public class Main {

	public Main(OutputStream err, String string) {
       throw new UnsupportedOperationException("This is a mock object");
	}

	public boolean compile(String[] strings) throws IOException {
	   throw new UnsupportedOperationException("This is a mock object");
	}
}
