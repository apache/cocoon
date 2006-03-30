This demo shows a LinkRewriterTransformer reading from a relativized book.xml
file, to resolve links in HTML.

Please read ../linkrewriter-sitedemo/README.txt for more information.


For this sample to work, the following section must be added to cocoon.xconf,
inside /cocoon/input-modules:
         
      <component-instance
        class="org.apache.cocoon.components.modules.input.XMLFileModule"
        logger="core.modules.xml" name="book-raw">
        <!-- Shouldn't this be the default? -->
        <file src="cocoon://samples/linkrewriter/docs/book.xlm"/>
        <reloadable>true</reloadable>
      </component-instance>
       <component-instance
         class="org.apache.cocoon.components.modules.input.SimpleMappingMetaModule"
         logger="core.modules.mapper" name="book">
         <input-module name="book-raw"/>
       </component-instance>
   
