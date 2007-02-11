// the applied scripts are called in the order found at the basescript
cocoon.apply( "aspects1.js" );
cocoon.apply( "aspects2.js" );

function interceptionTest() {
  var uri = "page/info";
  cocoon.log.error ( "interceptionTest - baseScript" );
  var x = callAnotherFunction();
  cocoon.sendPageAndWait(uri, { });
}

function callAnotherFunction() {
  var x = 1;
  cocoon.log.error ( "callAnotherFunction() - baseScript" );     
  if( x == 2 ) {
    return 72;
  }
  return 27;
}

function testSendPageAndWait() { 
  woody.send( "bla" );    
  cocoon.sendPageAndWait("xxx",{});
  cocoon.sendPageAndWait("yyy",{});
  cocoon.sendPageAndWait( "zzz", {} );
}
