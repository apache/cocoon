
function betwixtTest() {
  var testBean = new Packages.org.apache.cocoon.samples.betwixt.TestBean();
  testBean.setName( "Donald Duck" );
  cocoon.request.setAttribute( "testBean", testBean );   
  cocoon.sendPageAndWait( "simple-betwixt.xml", {} );
}