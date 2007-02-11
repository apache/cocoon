The anteater project has been retired.  Please write new tests in the htmlunit 
framework.  See src/test/htmlunit/org/apache/cocoon/HtmlUnitTestCase.java
for details. 

NB this concerns only for tests on the reponse of a running Cocoon server.
Unit tests for Cocoon classes are as ever written as JUnit tests.

To run htmlunit tests you have to download htmlunit 1.5 from
http://sourceforge.net/project/showfiles.php?group_id=47038.  
Unpack it into a convenient directory and set the path in your 
local.build.properties, for example:

    htmlunit.home = /my/htmlunit-1.5

Then build Cocoon, start the server, and run the tests:

    build.sh webapp
    cocoon.sh servlet
    build.sh htmlunit-tests

With htmlunit-1.5 the test RedirectTestCase.testSendStatus fails with an NPE.
This is already fixed in CVS.  For release information, see 
https://sourceforge.net/tracker/?func=detail&atid=448268&aid=1166652&group_id=47038
