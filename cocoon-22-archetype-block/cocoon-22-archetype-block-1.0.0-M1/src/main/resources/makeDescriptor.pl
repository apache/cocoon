#!/usr/bin/perl -W

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
