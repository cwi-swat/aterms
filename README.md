ATerms are an efficient symbolic computation and exchange format. The core of the ATerm library is the C code 
that efficiently implements maximally shared (persistent, hash-consed) trees and lists, including an automatic generational 
garbage collector, and fast textual and binary serialization.

This library was developed at [CWI](http://www.cwi.nl/sen1) with large contributions from INRIA Nancy and TU Eindhoven. It has been around for more than a decade. ATerm are used mostly in software products that heavily depend on symbolic evaluation,
such as term rewriting (ASF+SDF, Stratego, ELAN, TOM) and model checking (mCRL2, haRVey).

Publications on ATerm library:
  * [Efficient Annotated Terms](http://dx.doi.org/10.1002/%28SICI%291097-024X%28200003%2930:3%3C259::AID-SPE298%3E3.0.CO;2-Y)
  * [ATerms for the manipulation of structured data](http://dx.doi.org/10.1016/j.infsof.2006.08.009)
  * [Generation of abstract programming interfaces from syntax definitions](http://dx.doi.org/10.1016/j.jlap.2003.12.002)
  * [A generator of efficient strongly typed abstract syntax trees in Java](http://dx.doi.org/10.1049/ip-sen:20041181)

This aterms repository is a collection of components, all related to ATerms:

  - apigen: generates C and Java code from ADT descriptions to wrap the lower level ATerm API in a typed abstraction
  - aterm: code ATerm library in C
  - aterm-csharp: experimental ATerm implementation in C#
  - aterm-java: full ATerm implementation in Java
  - aterm-xml: command-line tools for mapping between ATerms and XML
  - balanced-binary-aterms: a balanced binary tree implementation on top of ATerms
  - relational-aterms: an implementation of relational calculus on top of ATerms
  - shared-objects: a core Java library for fast maximal sharing based on the factory and prototype design patterns
  - shared-objects-csharp: shared-objects ported to C#

