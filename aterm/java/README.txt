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
