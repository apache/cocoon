################################################################################
# Makefile
#
# N.B. You will need GNU make to use this make file.
################################################################################

################################################################################
# Targets:
#
# - jar = creates the jar archive with the Java binary code.
#
# - javadoc = creates the javadoc API documentation
#
# - clean = clears the distribution
#
# - dist = creates the distribution
#
################################################################################

################################################################################
# Common definitions.  Edit these settings for your site.  
#
# JAVAC        : the java compiler
# JAVACOPTS    : the java compiler options
# JAVADOC      : the javadoc program
# JAVADOCOPT   : the javadoc program options
# JAR          : the jar program
# BINDIR       : the directory where the cocoon.jar file goes when compiled
# DOCDIR       : the directory where the javadoc generated doc files go
################################################################################

JAVAC        = javac
JAVACOPTS    = -O
BINDIR       = ../bin

################################################################################
# You should not need to change anything below here.
################################################################################

NAME       = Cocoon
VERSION    = 1.6-dev

PACKAGES  = \
	$(BASE) \
	$(BASE).dcp \
	$(BASE).cache \
	$(BASE).store \
	$(BASE).parser \
	$(BASE).example \
	$(BASE).producer \
	$(BASE).formatter \
	$(BASE).formatter.pdf \
	$(BASE).formatter.xml \
	$(BASE).formatter.wml \
	$(BASE).formatter.html \
	$(BASE).formatter.xhtml \
	$(BASE).framework \
	$(BASE).processor \
	$(BASE).processor.dcp \
	$(BASE).processor.sql \
	$(BASE).processor.xslt \
	$(BASE).interpreter \
	$(BASE).interpreter.java \
	$(BASE).interpreter.ecmascript \

BASE       = org.apache.cocoon
MANIFEST   = MANIFEST
LOG        = make.log

JAR          = jar
DOCS         = ../docs
APIDOCS      = $(DOCS)/api
JAVADOC      = javadoc
JAVADOCOPTS  = -d $(APIDOCS) -use -author -version -private \
               -windowtitle $(WINDOWTITLE) \
               -doctitle $(DOCTITLE) \
               -bottom $(BOTTOM) \
               -stylesheetfile $(STYLESHEET)

WINDOWTITLE = "Cocoon API Specification"
DOCTITLE    = "<img src="../images/cocoon.jpg">"
BOTTOM      = "<font size="-1">Copyright 1999 Apache XML Project. All Rights Reserved.</font>"
STYLESHEET  = "../docs/stylesheet.css"

DIRECTORIES   = $(subst .,/,$(PACKAGES))
BASEDIRECTORY = $(subst .,/,$(BASE))
ALLSOURCES    = $(foreach dir,$(DIRECTORIES),$(wildcard $(dir)/*.java))

.PHONY : jar javadoc clean dist

jar:
	@echo
	@echo Creating $(NAME).jar package...
	@test -d classes || mkdir classes
	@echo .compiling $(NAME) using "$(JAVAC) $(JAVACOPTS)"
	@$(JAVAC) $(JAVACOPTS) -d classes $(ALLSOURCES) 2> $(LOG)
	@echo .copying properties
	@cp $(BASEDIRECTORY)/cocoon.properties classes/$(BASEDIRECTORY)/cocoon.properties
	@cp $(BASEDIRECTORY)/interpreter/ecmascript/initScript.es classes/$(BASEDIRECTORY)/interpreter/ecmascript/initScript.es
	@echo .building package $(NAME).jar
	@(cd classes; $(JAR) cfm ../$(NAME).jar ../$(MANIFEST) .)
	@echo .moving $(NAME).jar into $(BINDIR)
	@test -d $(BINDIR) || mkdir $(BINDIR)
	@mv $(NAME).jar $(BINDIR)/$(NAME).jar

javadoc:
	@echo
	@echo Building API documentation...
	@test -d $(APIDOCS) || mkdir $(APIDOCS)
	@echo .creating the docs using $(JAVADOC)
	@$(JAVADOC) $(JAVADOCOPTS) $(PACKAGES) 2>> $(LOG)

clean:
	@echo
	@echo Cleaning up the distribution...
	@rm -rf temp classes $(LOG) $(BINDIR)/$(NAME).jar $(NAME)_$(VERSION).jar
	@(cd $(APIDOCS); find . -not -path */CVS* -not -name '.' -exec rm -rf '{}' ';')
	@cp $(DOCS)/api.html $(APIDOCS)/index.html

dist: clean jar javadoc
	@echo
	@echo Creating the $(NAME) distribution...
	@echo .copying the files into a temporary directory
	@cp -Rf .. temp
	@cp -Rf temp/src/org/apache/cocoon/cocoon.properties temp/bin/.
	@echo .removing temporary files
	@rm -rRf temp/src/$(LOG) temp/src/classes
	@echo .removing CVS files
	@(cd temp; \
	find . -name Root | xargs -r rm -f; \
	find . -name Entries | xargs -r rm -f; \
	find . -name Repository | xargs -r rm -f; \
	find . -name CVS | xargs -r rmdir)
	@echo .creating the complete jar archive
	@(cd temp; $(JAR) cf ../$(NAME)_$(VERSION).jar .)
	@echo
	@echo Done.

test:
	@echo ALLSOURCES: $(ALLSOURCES)

