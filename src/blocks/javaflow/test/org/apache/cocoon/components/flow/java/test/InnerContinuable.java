package org.apache.cocoon.components.flow.java.test;

import org.apache.cocoon.components.flow.java.ContinuationHelper;

public class InnerContinuable {
	
	private String result2 ="test2";
	
	public void doInnerClassTest1() {
		InnerClass ic = new InnerClass();
		ic.sendResult1();
	}
	
	public void doInnerClassTest2() {
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
