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
/**
 * 
 */
package org.apache.cocoon.deployer.block.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.cocoon.deployer.block.BinaryBlock;
import org.apache.cocoon.deployer.generated.block.x10.Block;

public class Block10 implements BinaryBlock {

	private String id;
	private String namespace;
	private InputStream inputStream;
	private Block blockDescriptor;
	private org.apache.cocoon.deployer.generated.deploy.x10.Block deployDescriptor;
	
	public void setNameSpace(String namespace) {
		this.namespace = namespace;
	}
	
	public String getNamespace() {
		return this.namespace;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public InputStream getInputStream() throws IOException {
		return this.inputStream;
	}

	public void setBlockDescriptor(Block blockDescriptor) {
		this.blockDescriptor = blockDescriptor;
	}
	
	public Block getBlockDescriptor() {
		return this.blockDescriptor;
	}

	public void setDeployDescriptor(org.apache.cocoon.deployer.generated.deploy.x10.Block deployDescriptor) {
		this.deployDescriptor = deployDescriptor;
	}
	
	public org.apache.cocoon.deployer.generated.deploy.x10.Block getDeployDescriptor() {
		return this.deployDescriptor;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}

}
