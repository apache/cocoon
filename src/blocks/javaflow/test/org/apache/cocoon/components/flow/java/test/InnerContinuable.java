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
package org.apache.cocoon.components.flow.java.test;

import org.apache.cocoon.components.flow.java.ContinuationHelper;

public class InnerContinuable {
	
	private String result2 ="test2";
	
	public void doInnerClassTest1() {
		InnerClass ic = new InnerClass();
		ic.sendResult1();
	}
	
	public void doInnerClassTest2() {
	    
	    LinkerContinuable.init(null, null, false);
	    
		InnerClass ic = new InnerClass();
		ic.sendResult2();
	}
	
	public class InnerClass {
		public void sendResult1() {
			ContinuationHelper.sendPageAndWait("test1");
		}
		
		public void sendResult2() {
			ContinuationHelper.sendPageAndWait(result2);
		}
	}
}
