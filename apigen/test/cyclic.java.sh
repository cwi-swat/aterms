#! /bin/sh
CLASSPATH=.:..:/ufs/jurgenv/glt/installed/1-5/share/aterm-1.6.jar:/ufs/jurgenv/glt/installed/1-5/share/shared-objects-1.4.jar:/ufs/jurgenv/glt/installed/1-5/share/jjtraveler-0.4.3.jar
export CLASSPATH
java test.CyclicTest ../test
