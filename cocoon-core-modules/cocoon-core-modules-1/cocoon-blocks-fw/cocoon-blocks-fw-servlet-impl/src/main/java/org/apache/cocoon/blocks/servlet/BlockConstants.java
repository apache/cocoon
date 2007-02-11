/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.blocks.servlet;

/**
 * BlockConstants used in the blocks framework
 * @version $Id$
 */
public final class BlockConstants {
    
    /** Name of the meta directory of a webapp */
    public static final String WEBAPP_META_DIR = "WEB-INF";

    /** Name of the meta directory of a block */
    public static final String BLOCK_META_DIR = "META-INF";

    /** Name of the resource directory of a block */
    public static final String BLOCK_RESOURCES_DIR = "";
    
    /** Path to the wiring file in the context */
    public static final String WIRING = "/" + WEBAPP_META_DIR + "/wiring.xml";

    /** Path to the block configuration file in the block context */
    public static final String BLOCK_CONF = "/" + BLOCK_META_DIR + "/block.xml";
}
