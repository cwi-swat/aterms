#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#ifndef WIN32
#include <sys/times.h>
#endif
#include <time.h>
#include <limits.h>

#include <aterm2.h>
#include <util.h>
#include <_aterm.h>


/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
#ifndef WIN32
  struct tms  start, end;
#endif
  ATerm top = NULL;
  ATerm t, t2;
  ATbool dobafsize = ATfalse;
  int i, subterms, symbols, unique, depth;
  int incore, textsize, bafsize, textread, textwrite;
  FILE *tmp_file;

  for(i=1; i<argc; i++) {
    if(streq(argv[i], "-bafsize"))
      dobafsize = ATtrue;
  }

  ATinit(argc, argv, &top);

#ifndef WIN32
  times(&start);
#endif
  t = ATreadFromFile(stdin);
#ifndef WIN32
  times(&end);
  textread = end.tms_utime-start.tms_utime;
#endif

  tmp_file = tmpfile();
#ifndef WIN32
  times(&start);
#endif
  ATwriteToTextFile(t, tmp_file);
#ifndef WIN32
  times(&end);
  textwrite = end.tms_utime-start.tms_utime;
#endif

  subterms = AT_calcSubterms(t);
  symbols  = AT_calcUniqueSymbols(t);
  unique   = AT_calcUniqueSubterms(t);
  depth    = AT_calcTermDepth(t);
  incore   = AT_calcCoreSize(t);
  textsize = AT_calcTextSize(t);

  printf("nr of subterms  : %8d\n", subterms);
  printf("unique symbols  : %8d\n", symbols);
  printf("unique subterms : %8d\n", unique);
  printf("sharing ratio   : %8.2f%%\n", 100.0-((double)unique*100)/(double)subterms);
  printf("term depth      : %8d\n", depth);
  printf("in-core size    : %8d\n", incore);
  printf("  bytes p/node  : %8.2f\n", ((double)incore)/((double)subterms));
  printf("text size       : %8d\n",textsize);
  printf("  bytes p/node  : %8.2f\n", ((double)textsize)/((double)subterms));

#ifndef WIN32
  printf("text read time  : %8.2fs\n", ((double)textread)/((double)CLOCKS_PER_SEC));
  printf("  per node      : %8.2fus\n", ((double)textread*1000000.0/subterms)/((double)CLOCKS_PER_SEC));
  printf("text write time : %8.2fs\n", ((double)textwrite)/((double)CLOCKS_PER_SEC));
  printf("  per node      : %8.2fus\n", ((double)textwrite*1000000.0/subterms)/((double)CLOCKS_PER_SEC));
#endif

  if(dobafsize) {
    struct stat stats;
#ifndef WIN32
    clock_t bafread, bafwrite;
#endif
    FILE *file = fopen("/tmp/test.baf", "wb+");
    int fd = fileno(file);

#ifndef WIN32
    times(&start);
#endif
    ATwriteToBinaryFile(t, file);
#ifndef WIN32
    times(&end);
    bafwrite = end.tms_utime-start.tms_utime;
#endif
    fflush(file);
    fstat(fd, &stats);
    bafsize = (int)stats.st_size;
    fseek(file, 0, SEEK_SET);
#ifndef WIN32
    times(&start);
#endif
    t2 = ATreadFromBinaryFile(file);
#ifndef WIN32
    times(&end);
    bafread = end.tms_utime-start.tms_utime;
#endif
    printf("baf size        : %8d\n", bafsize);
    printf("  bytes p/node  : %8.2f\n", ((double)bafsize)/((double)subterms));
    printf("  bits p/node   : %8.2f\n", ((double)bafsize*8)/((double)subterms));
    printf("  comp.wrs.text : %8.2f%%\n", 100.0-((double)bafsize*100)/((textsize)));
#ifndef WIN32
    printf("baf write time  : %8.2fs\n", ((double)bafwrite)/((double)CLOCKS_PER_SEC));
    printf("  per node      : %8.2fus\n", ((double)bafwrite*1000000.0/subterms)/((double)CLOCKS_PER_SEC));
    printf("baf read time   : %8.2fs\n", ((double)bafread)/((double)CLOCKS_PER_SEC));
    printf("  per node      : %8.2fus\n", ((double)bafread*1000000.0/subterms)/((double)CLOCKS_PER_SEC));
#endif
    fclose(file);
  }

  return 0;
}

/*}}}  */
