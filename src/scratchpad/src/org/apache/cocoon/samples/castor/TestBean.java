package org.apache.cocoon.samples.castor;


public class TestBean {
  private String name;
  private String scope;

  public TestBean(){

  }

  public TestBean(String newName,String newScope){
    name= newName;
    scope=newScope;
  }
  public String getName() {
    return name;
  }
  public void setName(String newName) {
    name = newName;
  }
  public void setScope(String newScope) {
    scope = newScope;
  }
  public String getScope() {
    return scope;
  }


}
