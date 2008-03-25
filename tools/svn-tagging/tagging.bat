rem  Licensed to the Apache Software Foundation (ASF) under one or more
rem  contributor license agreements.  See the NOTICE file distributed with
rem  this work for additional information regarding copyright ownership.
rem  The ASF licenses this file to You under the Apache License, Version 2.0
rem  (the "License"); you may not use this file except in compliance with
rem  the License.  You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem  Unless required by applicable law or agreed to in writing, software
rem  distributed under the License is distributed on an "AS IS" BASIS,
rem  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem  See the License for the specific language governing permissions and
rem  limitations under the License.


REM  This script is necessary because an aggregated release doesn't create proper SVN tags.
REM  Using search'n'replace of your favorite text editor should make it simple to reuse the script
REM  for future releases.
REM
REM  Take care that this list is complete for your release of the core-modules. It might happen that
REM  new modules were added!

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-core/ https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core/cocoon-core-2.2.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-pipeline/cocoon-pipeline-api/ https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-pipeline-api/cocoon-pipeline-api-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-pipeline/cocoon-pipeline-impl/ https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-pipeline-impl/cocoon-pipeline-impl-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-pipeline/cocoon-pipeline-components/ https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-pipeline-components/cocoon-pipeline-components-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-sitemap/cocoon-sitemap-components/ https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-sitemap-components/cocoon-sitemap-components-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-sitemap/cocoon-sitemap-impl/ https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-sitemap-impl/cocoon-sitemap-impl-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-sitemap/cocoon-sitemap-api/ https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-sitemap-components/cocoon-sitemap-components-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-store/cocoon-store-impl/ https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-store-impl/cocoon-store-impl-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-thread/cocoon-thread-api/ https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-thread-api/cocoon-thread-api-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-thread/cocoon-thread-impl/ https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-thread-impl/cocoon-thread-impl-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-util/  https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-util/cocoon-util-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-xml/cocoon-xml-api/  https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-xml-api/cocoon-xml-api-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-xml/cocoon-xml-impl/  https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-xml-impl/cocoon-xml-impl-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-xml/cocoon-xml-resolver/  https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-xml-resolver/cocoon-xml-resolver-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-expression-language/cocoon-expression-language-api/  https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-expression-language-api/cocoon-expression-language-api-1.0.0/ -m "add tag for modules released during multi-pom process"

svn cp https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-core-modules/cocoon-core-modules-6/cocoon-expression-language/cocoon-expression-language-impl/  https://svn.apache.org/repos/asf/cocoon/tags/cocoon-2.2/cocoon-expression-language-impl/cocoon-expression-language-impl-1.0.0/ -m "add tag for modules released during multi-pom process"