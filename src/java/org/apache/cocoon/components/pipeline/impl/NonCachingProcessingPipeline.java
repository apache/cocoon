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
package org.apache.cocoon.components.pipeline.impl;

import org.apache.cocoon.components.pipeline.AbstractProcessingPipeline;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;

/**
 * Thi is the implementation of the non caching processing pipeline
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: NonCachingProcessingPipeline.java,v 1.4 2004/03/08 14:01:56 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingPipeline
 * @x-avalon.lifestyle type=pooled
 */
public class NonCachingProcessingPipeline
       extends AbstractProcessingPipeline implements ProcessingPipeline {
}
