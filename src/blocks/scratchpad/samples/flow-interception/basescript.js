// the applied scripts are called in the order found at the basescript
cocoon.apply( "aspects1.js" );
cocoon.apply( "aspects2.js" );

function interceptionTest() {
  var uri = "page/info";
  cocoon.log.error ( "interceptionTest - baseScript" );
  callAnotherFunction();
  cocoon.sendPageAndWait(uri, { });
}

function callAnotherFunction() {
  cocoon.log.error ( "callAnotherFunction() - baseScript" );       
}