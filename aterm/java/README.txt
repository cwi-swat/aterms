
    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

* Contents:

  This directory will contain the (Java) interface for the ATerm library.

  Two implementations of this interface will find their way in here as well:
   - A native implementation, providing a Java layer on the native C-lib.
   - A pure   implementation, much like the one in ../java1.2 now.

  Packages: aterm, aterm.native, aterm.pure

* Status:

  The following steps will be taken:
  1. Extract the interface, sticking as close as possible to the
     C-interface and what is already present in ../java1.2
	 Documentation through the use of javadoc directives will be
	 in this interface as well.
  2. Implement the Java-native layer.
  3. Implement the Java-pure layer.


* Coding Convention

  Java code in this package abides by the Sun Coding Convention,
  see http://java.sun.com/docs/codeconv.


* Maintainer (aka Executive Producer ;-)

  Hayco de Jong
  Email: jong@cwi.nl
  Phone: cwi, x4090
