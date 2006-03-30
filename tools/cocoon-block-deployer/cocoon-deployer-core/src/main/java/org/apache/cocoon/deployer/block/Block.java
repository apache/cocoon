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
package org.apache.cocoon.deployer.block;


/**
 * @version $Id:$
 */
public interface Block {

	public static final String BLOCK_NS_10 = "http://apache.org/cocoon/blocks/cob/1.0";
	String BLOCK_DESCRIPTOR_LOCATION = "META-INF/block.xml";
	
	/**
	 * @return the block namespace
	 */
	public String getNamespace();
	
	/**
	 * @ return the block id (from the block descriptor and NOT from the deployment descriptor)
	 */
	public String getId();
	
	/**
	 * @return the block descriptor (contains informatin like version, author, dependencies, ...)
	 */
	public org.apache.cocoon.deployer.generated.block.x10.Block getBlockDescriptor();
	
	/**
	 * @return the deployment information for the block
	 */
	public org.apache.cocoon.deployer.generated.deploy.x10.Block getDeployDescriptor();
	
}