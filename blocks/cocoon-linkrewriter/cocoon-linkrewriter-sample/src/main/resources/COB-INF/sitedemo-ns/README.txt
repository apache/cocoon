This LinkRewriterTransformer demo is what I used for primary testing.  It
implements a 'linkmap' as described in this Forrest RT:

http://marc.theaimsgroup.com/?l=forrest-dev&m=103444028129281&w=2

Specifically, the file linkmap.xml is first absolutized, then relativized, then
used to resolve links like <link href="site:index">.

To install:

1) run 'build.sh webapp-local'
2) copy this directory into build/cocoon/webapp/samples/
3) Edit build/cocoon/webapp/WEB-INF/cocoon.xconf, and add the following section
inside /cocoon/input-modules:


      <component-instance
        class="org.apache.cocoon.components.modules.input.XMLFileModule"
        logger="core.modules.xml" name="linkmap">
        <file src="cocoon://samples/link/linkmap"/>
        <!-- Shouldn't this be the default? -->
        <reloadable>true</reloadable>
      </component-instance>
       <component-instance
         class="org.apache.cocoon.components.modules.input.SimpleMappingMetaModule"
         logger="core.modules.mapper" name="site">
         <input-module name="linkmap"/>
         <prefix>/site/</prefix>
         <suffix>/@href</suffix>
       </component-instance>


4) Restart your webserver, and request http://localhost:8080/cocoon/samples/linkrewriter-sitedemo/welcome

You ought to get the following XML back:

<samples>
  <group name="Raw XML containing hopefully-rewritten links">
    <sample name="Back" href="..">

      linkmap:/site/index/@href 
      <link href="index.html"/>

      site:index 
      <link href="index.html"/>

      site:faq/how_can_I_help 
      <link href="faq.html#how_can_I_help"/>
    </sample>
  </group>
</samples>


The href's are the things that were rewritten.

Then add an arbitrary path before the /welcome, eg
http://localhost:8080/cocoon/samples/linkrewriter-sitedemo/foo/bar/welcome
You should get back 'relativized' links:

<samples>
  <group name="Raw XML containing hopefully-rewritten links">
    <sample name="Back" href="..">

      linkmap:/site/index/@href 
      <link href="index.html"/>

      site:index 
      <link href="../../index.html"/>

      site:faq/how_can_I_help 
      <link href="../../faq.html#how_can_I_help"/>
    </sample>
  </group>
</samples>


Change the @src in line 14 to '{src}' to make the first one also be relative.


