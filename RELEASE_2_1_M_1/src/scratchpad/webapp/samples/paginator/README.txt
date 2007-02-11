
CVS: $Id: README.txt,v 1.1 2003/03/09 00:10:31 pier Exp $


To get started, read the How-To for this component:
   http://localhost:8080/cocoon/howto/howto-paginator-transformer.html

For those who don't like to read docs:

Make sure you have a version 2.0.3 or greater of Cocoon. The PaginatorTransformer component
source code is located in the scratchpad area. Therefore, you need to use the following
command to build a deployable cocoon.war which includes the scratchpad libraries.

  ./build.[ sh | bat ] run 

During the build process, the necessary configuration details for the PaginatorTransformer
component are copied to cocoon.xconf of cocoon.war. This means that you don't need to
manually configure cocoon.xconf.

To get going with Cocoon 2.0.3, access:
      http://localhost:8080/cocoon/mount/paginator/list(1)

To get going with Cocoon 2.1, access:
      http://localhost:8080/cocoon/samples/paginator/list(1)


Have fun!