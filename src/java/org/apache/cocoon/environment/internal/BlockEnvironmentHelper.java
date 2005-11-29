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
package org.apache.cocoon.environment.internal;

import java.net.URL;

import org.apache.cocoon.blocks.Block;

/**
 * Hack used for geting hold on the current block manager without
 * making core code dependent of the experimental blocks code.
 *
 * @version $Id$
 * @since 2.2 
 */
public class BlockEnvironmentHelper
    extends EnvironmentHelper {

    // Hack for getting it to compile
    private BlockEnvironmentHelper(URL context) {
        super(context);
    }

    public static Block getCurrentBlock() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if ( stack != null && !stack.isEmpty()) {
            for (int i = stack.getOffset(); i >= 0; i--) {
                final EnvironmentInfo info = (EnvironmentInfo)stack.get(i);
                if (info.processor instanceof Block) {
                    return (Block)info.processor;
                }
            }
        }
        return null;
    }
}
