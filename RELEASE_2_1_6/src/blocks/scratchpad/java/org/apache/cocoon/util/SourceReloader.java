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

import org.apache.excalibur.source.Source;

/**
 * Load data from a source and return an object.
 * 
 * @since 2.1.4
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SourceReloader.java,v 1.3 2004/03/05 10:07:26 bdelacretaz Exp $
 */
public interface SourceReloader {

	/**
	 * Load data from a source and return an object.
	 * 
	 * @param src A source.
	 * @param parameter A parameter.
	 * @return An object.
	 */
	Object reload(Source src, Object parameter);

}
