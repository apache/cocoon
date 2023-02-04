/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.jcr.transforming;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Represents a selection of a property or of a subnode and its properties.
 * 
 * @version $Id$ 
 */
public class JCRSelection {

	/**
	 * Name of the property or subnode.
	 */
	private QName name;
	/**
	 * In case it is a subnode, other subnodes or properties can be added.
	 */
	private Set subnodes;
	
	public JCRSelection() {}
	
	/**
	 * @param namespaceURI Namespace URI of the property or subnode name
	 * @param localname Local name (excluding prefix) of the property or subnode.
	 */
	public JCRSelection(String namespaceURI, String localname) {
		this.setName(namespaceURI, localname);
	}
	
	/**
	 * @return The name of the property of subnode.
	 */
	public QName getName() {
		return name;
	}
	public void setName(QName name) {
		this.name = name;
	}
	
	public void setName(String namespaceURI, String localname) {
		this.name = new QName(namespaceURI, localname);
	}
	
	/**
	 * @return The set of properties and/or subnodes.
	 */
	public Set getSubnodes() {
		return subnodes;
	}
	public void setSubnodes(Set subnodes) {
		this.subnodes = subnodes;
	}
	
	/**
	 * Adds a subnode or property.
	 * @param subnode The subnode or poerty to add
	 */
	public void addSubnode(JCRSelection subnode) {
		if (this.subnodes == null) this.subnodes = new HashSet();
		this.subnodes.add(subnode);
	}
	public boolean hasSubnodes() {
		return this.subnodes != null && this.subnodes.size() > 0;
	}
	
	/**
	 * @return A set of all namespaces URIs used in this node and its subnodes.
	 */
	public Set getNamespaces() {
		Set ret = new HashSet();
		fillNamespaces(ret);
		return ret;
	}

	private void fillNamespaces(Set ret) {
		if (this.name != null) ret.add(this.name.getNamespaceURI());
		if (this.subnodes != null) {
			for (Iterator iter = this.subnodes.iterator(); iter.hasNext();) {
				JCRSelection subnode = (JCRSelection) iter.next();
				subnode.fillNamespaces(ret);
			}
		}
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof JCRSelection)) return false;
		JCRSelection oth = (JCRSelection) other;
		return oth.name == null ? this.name == null : this.name == null ? false : this.name.equals(oth.name);
	}
	
	public int hashCode() {
		if (this.name == null) return 0;
		return this.name.hashCode();
	}
	
	

}
