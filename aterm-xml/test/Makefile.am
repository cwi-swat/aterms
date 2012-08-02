# set location of tested tools:
XML2ATERM=${top_builddir}/xml2aterm/xml2aterm.test
ATERM2XML=${top_builddir}/aterm2xml/aterm2xml
ATDIFF=${ATERM_PREFIX}/bin/atdiff

# all test files should be in the distribution
EXTRA_DIST=$(wildcard *.trm) $(wildcard *.xml) $(wildcard *.ref)

# which files are tested
#TEST_FILES=slashdot pi title eve aterm wap wml
TEST_FILES=aterm

# TESTS is a special automake variable. It enumarates the
# scripts that it should call when 'gmake check' is done:
TESTS=$(foreach f, ${TEST_FILES}, $f.x2a.sh $f.a2x.sh) 

# Files to clean when 'gmake clean' is called 
CLEANFILES = *.out *.sh

# For each test file we generate 2 .sh scripts, which are called
# by gmake check:
%.x2a.sh: Makefile
	@(echo 'set -e';\
          echo '${XML2ATERM} -i ${srcdir}/$*.xml -e -o $*.xml.out';\
          echo '${ATDIFF} ${srcdir}/$*.trm $*.xml.out';\
          echo 'rm $*.xml.out') > $@ && chmod +x $@
          
%.a2x.sh: Makefile
	@(echo 'set -e';\
          echo '${ATERM2XML} -i ${srcdir}/$*.trm -o $*.trm.out -e -t';\
          echo 'diff ${srcdir}/$*.trm.ref $*.trm.out';\
          echo 'rm $*.trm.out') > $@ && chmod +x $@
