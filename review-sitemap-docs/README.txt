These are some basic tools to assist with the
 Review of sitemap component documentation
 http://cocoon.apache.org/2.1/plan/review-sitemap-docs.html

------------------------------------------------------------------------
Keep the table synchronised with the java source
------------------------------------------------
Occasionally do the following ...
 cd cocoon-2_1_X
 tools/review-sitemap-docs/find-component-java-files.sh
Compare with last time you ran it ...
 diff component-java-files-20041206.txt component-java-files-20041201.txt
Also compare with a listing of the files from Cocoon trunk.
Add new rows to the table with copy-and-paste.

------------------------------------------------------------------------
Add a new empty column to the table
-----------------------------------
Edit the sed script to define the next column, then run it.
 cd cocoon-2_1_X
 sed -f tools/review-sitemap-docs/add-column.sed \
   src/documentation/xdocs/plan/review-sitemap-docs.xml > tmp
 mv tmp src/documentation/xdocs/plan/review-sitemap-docs.xml

------------------------------------------------------------------------
