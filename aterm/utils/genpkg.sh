#! /bin/sh

url=http://www.cwi.nl/projects/MetaEnv/aterm

keywords='meta-environment, library, baf, aterm, term, tree, sharing'

desc() {
cat <<ENDCAT
ATerm (short for Annotated Term) is an abstract data type
designed for the exchange of tree-like data structures
between distributed applications.
ENDCAT
}

# {{{  extract package-name and package-version

pkg_version=`grep AM_INIT_AUTOMAKE "configure.in" \
             | sed -e "s/AM_INIT_AUTOMAKE(//" \
                   -e "s/, / /" \
	           -e "s/)$//"`

pkg=`echo $pkg_version | cut -d ' ' -f1`
ver=`echo $pkg_version | cut -d ' ' -f2`

# }}}
# {{{  extract interfaces

iface=`grep -- '--' configure.in \
     | tr -s [:space:] \
     | sed -e "s/[ \t\[]*//" \
           -e "s/--with-//" \
           -e "s/],$//" \
     | awk '{printf "%s\t#%s#\n", $1, substr($0, length($1)+2)}' \
     | tr '#' "'"`

# }}}

# {{{  create package file

cat <<ENDCAT
package
identification
  name=$pkg
  version=$ver
  location=$url
  info=$url
  description='`desc`'

  keywords=$keywords

interface
$iface

requires

configuration
  dbg

ENDCAT

# }}}
