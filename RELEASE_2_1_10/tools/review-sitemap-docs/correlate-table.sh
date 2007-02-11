#!/bin/bash

# Copyright 2005 The Apache Software Foundation or its licensors,
# as applicable.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


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

if [ ! -e cocoon.sh ]; then
 echo "Please run from top-level of cocoon source."
 echo " cd cocoon-2_1_X; tools/review-sitemap-docs/correlate-table.sh"
 exit
fi

if [ ! -e build/cocoon-2.1.7-dev/javadocs ]; then
 echo "Please run 'build javadocs' first"
 exit
fi

echo
echo "Scanning Cocoon javadocs to find sitemap components java source files."
echo " (See components-source.txt and do diff with that file from last run.)"
find build/cocoon-2.1.7-dev/javadocs -name *.html \
| grep -v -f tools/review-sitemap-docs/find-component-java-files-exclude.txt \
| xargs grep -l -f tools/review-sitemap-docs/grep-components.txt \
| sed 's/build\/.*org\//org\//;s/\.html//' \
| sort > components-source.txt

echo
echo "Scanning the coordinaton table xdoc and list the javadoc sources."
echo " (See components-table.txt and do diff with last time.)"
grep "<\!-- 1 -->" src/documentation/xdocs/plan/review-sitemap-docs.xml \
| sed 's/  <!-- 1 --><td>//;s/<\/td>//' \
| sed 's/<link href="\.\.\/apidocs\///;s/\.html.*$//' \
| sort > components-table.txt

echo
echo "Comparing the table list with the list obtained via javadocs."
echo "Whitespace lines are table entries which are missing a javadoc reference."
echo "doing 'diff components-source.txt components-table.txt'"
echo " (See components-javadoc-diff.txt)"
echo "[localhost]$ diff components-source.txt components-table.txt" > components-javadoc-diff.txt
echo "                  <                     >" >> components-javadoc-diff.txt
diff components-source.txt components-table.txt >> components-javadoc-diff.txt

echo
echo "Comparing the list obtained via javadocs with that obtained via SitemapTask."
echo "doing 'diff components-source.txt components-sitemaptask.txt'"
echo " (See components-javadoc-sitemaptask-diff.txt)"
echo "[localhost]$ diff components-source.txt components-sitemaptask.txt" > components-javadoc-sitemaptask-diff.txt
echo "                  <                     >" >> components-javadoc-sitemaptask-diff.txt
cat build/all-sitemap-components.txt build/all-sitemap-components-blocks.txt \
| sed 's/\./\//g' \
| grep -v -f tools/review-sitemap-docs/grep-sitemaptask-exclude.txt \
| sort > components-sitemaptask.txt
diff components-source.txt components-sitemaptask.txt \
>> components-javadoc-sitemaptask-diff.txt

echo
echo "Comparing the table list with the list obtained via SitemapTask."
echo "Whitespace lines are table entries which are missing a javadoc reference."
echo "doing 'diff components-sitemaptask.txt components-table.txt'"
echo " (See components-sitemaptask-diff.txt)"
echo "[localhost]$ diff components-sitemaptask.txt components-table.txt" > components-sitemaptask-diff.txt
echo "                  <                          >" >> components-sitemaptask-diff.txt
diff components-sitemaptask.txt components-table.txt >> components-sitemaptask-diff.txt

echo
echo "Counting the number of lines in components data files."
wc -l components-*.txt
