#!/bin/bash

# Purpose:
# Ensure that the review-sitemap-docs.xml coordination table
# remains synchronised.
#
# Procedure:
# Scan the Cocoon repository and find sitemap components java files.
# Scan the coordinate table xdoc and list the javadoc source for each entry.
# Compare using diff.
#
# Usage:
# Run this script from the top-level of the repository.
#  cd /usr/local/svn/cocoon-2_1_X
#  tools/review-sitemap-docs/correlate-table.sh

echo "Scanning Cocoon javadocs to find sitemap components java source files."
echo " (See components-source.txt and do diff with that file from last run.)"
find build/cocoon-2.1.7-dev/javadocs -name *.html \
| grep -v -f tools/review-sitemap-docs/find-component-java-files-exclude.txt \
| xargs grep -l -f tools/review-sitemap-docs/grep-components.txt \
| sed 's/build\/.*org\//org\//;s/\.html//' \
| sort > components-source.txt

echo "Scanning the coordinaton table xdoc and list the javadoc sources."
echo " (See components-table.txt and do diff with last time.)"
grep "<\!-- 1 -->" src/documentation/xdocs/plan/review-sitemap-docs.xml \
| sed 's/  <!-- 1 --><td>//;s/<\/td>//' \
| sed 's/<link href="\.\.\/apidocs\///;s/\.html.*$//' \
| sort > components-table.txt

echo "Comparing the lists."
echo "Whitespace lines are table entries which are missing a javadoc reference."
echo "diff components-source.txt components-table.txt"
echo "----------"
diff components-source.txt components-table.txt
echo "----------"

echo "Counting the number of components in the table."
wc -l components-table.txt
