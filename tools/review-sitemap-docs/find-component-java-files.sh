#!/bin/bash

# Scan the Cocoon repository and find sitemap components

find src -name *Action.java -o -name *Generator.java \
      -o -name *Matcher.java -o -name *Reader.java \
      -o -name *Selector.java -o -name *Serializer.java \
      -o -name *Transformer.java \
| grep -v -f tools/review-sitemap-docs/find-component-java-files-exclude.txt \
| sort > component-java-files.txt
