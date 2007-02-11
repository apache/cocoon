#!/bin/bash

# Purpose:
# Ensure that the review-sitemap-docs.xml coordination table
# remains synchronised.
#
# Procedure:
# Scan the Cocoon repository and find sitemap components java files.
# Scan the coordinate table xdoc and list the javadoc source for each entry.
# Compare with diff.
#
# Usage:
# Run this script from the top-level of the repository.
#  cd /usr/local/svn/cocoon-2_1_X
#  tools/review-sitemap-docs/correlate-table.sh

echo "Scanning Cocoon repository to find sitemap components java source files."
echo " (See component-java-files.txt and do diff with that file from last run.)"
find src -name *Action.java -o -name *Generator.java \
      -o -name *Matcher.java -o -name *Reader.java \
      -o -name *Selector.java -o -name *Serializer.java \
      -o -name *Transformer.java \
| grep -v -f tools/review-sitemap-docs/find-component-java-files-exclude.txt \
> component-java-files.tmp
find src -name *.java | grep -f tools/review-sitemap-docs/grep-components.txt \
| grep -v -f tools/review-sitemap-docs/find-component-java-files-exclude.txt \
>> component-java-files.tmp
sort component-java-files.tmp | uniq > component-java-files.txt
rm -f component-java-files.tmp

echo "Listing the sitemap components java source files."
echo " (See components-source.txt)"
sed -n 's/src\/.*org\//org\//p' component-java-files.txt \
| sort > components-source.txt

echo "Scanning the coordinate table xdoc and list the javadoc sources."
echo " (See components-table.txt and do diff with last time.)"
grep "<\!-- 1 -->" src/documentation/xdocs/plan/review-sitemap-docs.xml \
| sed 's/  <!-- 1 --><td>//;s/<\/td>//' \
| sed 's/<link href="\.\.\/apidocs\///;s/\.html.*$/\.java/' \
| sort > components-table.txt

echo "Comparing the lists."
echo "Whitespace lines are table entries which are missing a javadoc reference."
echo "diff components-source.txt components-table.txt"
echo "----------"
diff components-source.txt components-table.txt
echo "----------"

echo "Counting the number of components in the table."
wc -l components-table.txt
