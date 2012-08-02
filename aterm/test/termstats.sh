#!/bin/sh

cat | tee /tmp/test.trm | termstats -silent -bafsize -at-symboltable 256019
/usr/bin/time gzip -f /tmp/test.trm 2> /tmp/time.out
TIME=`grep user /tmp/time.out | awk '{ print $2 }'`
GZIPSIZE=`ls -l /tmp/test.trm.gz | awk '{ print $5 }'`
printf "gzipped size    : %8d\n" $GZIPSIZE
printf "  gzip time     : %8s\n" $TIME
