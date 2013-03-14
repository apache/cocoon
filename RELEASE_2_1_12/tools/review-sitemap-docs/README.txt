These are some basic tools to assist with the
 Review of sitemap component documentation
 http://cocoon.apache.org/2.1/plan/review-sitemap-docs.html

------------------------------------------------------------------------
Keep the table synchronised with the java source
------------------------------------------------
Occasionally do the following ...
Find all relevant java source files ...
 cd cocoon-2_1_X
 tools/review-sitemap-docs/correlate-table.sh
Compare the output listings with last time you ran it ...
 diff components-source.txt components-source-20041201.txt
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
