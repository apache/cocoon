#!/usr/bin/perl -W

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


#Use this script to generate the archetype descriptor, as the format does not allow wildcards yet
# perl makeDescriptor.pl > META-INF/archetype.xml

use Data::Dumper;

#find all files
my @fileList=`find . -type d -name '.svn' -prune -o -type f -print` ;
my @sources;
my @resources; 
my @testResources; 
my @testSources;
foreach my $file (@fileList){
  next unless $file=~/\.\/archetype-resources/ ;
  #attempt to skip binary files alltogether as they make the archetype deployer barf sometimes
  next if $file=~/\.ico$/;
  next if $file=~/\.png$/;
  next if $file=~/\.jpg$/;
  next if $file=~/\.gif$/;
  chomp ($file);
  $file =~ s/\.\/archetype-resources\///;

  if ($file=~/src\/main\/java/){
    push(@sources, $file);
  }
  
  if ($file=~/src\/main\/webapp/){
    push (@resources, $file);
  }

  if ($file=~/src\/test\/java/){
    push (@testSources, $file);
  }

  if ($file=~/src\/\/test\/resources/){
    push (@testResources, $file);
  }

  if ($file=~/src\/main\/resources/){
    push (@resources, $file);
  }
}

# ARCHETYPE
print "<archetype>\n";
print "<id>cocoon-archetype-block</id>\n";

# SOURCES
print "<sources>\n";
foreach my $source (@sources) {
  print "<source>$source</source>\n";
} 
print "</sources>\n";

# TESTSOURCES
print "<testSources>\n";
foreach my $testSource (@testSources) {
  print "<testSource>$testSource</testSource>\n";
} 
print "</testSources>\n";

# RESOURCES
print "<resources>\n";
foreach my $resource (@resources) {
  print "<resource>$resource</resource>\n";
} 
print "</resources>\n";

print "</archetype>\n";
