#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/times.h>
#include <time.h>
#include <limits.h>

#include <aterm2.h>
#include <util.h>
#include <_aterm.h>


/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  struct tms  start, end;
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

  times(&start);
  t = ATreadFromFile(stdin);
  times(&end);
  textread = end.tms_utime-start.tms_utime;

  tmp_file = tmpfile();
  times(&start);
  ATwriteToTextFile(t, tmp_file);
  times(&end);
  textwrite = end.tms_utime-start.tms_utime;

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
  printf("text read time  : %8.2fs\n", ((double)textread)/((double)CLK_TCK));
  printf("  per node      : %8.2fus\n", ((double)textread*1000000.0/subterms)/((double)CLK_TCK));
  printf("text write time : %8.2fs\n", ((double)textwrite)/((double)CLK_TCK));
  printf("  per node      : %8.2fus\n", ((double)textwrite*1000000.0/subterms)/((double)CLK_TCK));

  if(dobafsize) {
    struct stat stats;
    clock_t bafread, bafwrite;
    FILE *file = fopen("/tmp/test.baf", "wb+");
    int fd = fileno(file);

    times(&start);
    ATwriteToBinaryFile(t, file);
    times(&end);
    bafwrite = end.tms_utime-start.tms_utime;
    fflush(file);
    fstat(fd, &stats);
    bafsize = (int)stats.st_size;
    fseek(file, 0, SEEK_SET);
    times(&start);
    t2 = ATreadFromBinaryFile(file);
    times(&end);
    bafread = end.tms_utime-start.tms_utime;
    printf("baf size        : %8d\n", bafsize);
    printf("  bytes p/node  : %8.2f\n", ((double)bafsize)/((double)subterms));
    printf("  bits p/node   : %8.2f\n", ((double)bafsize*8)/((double)subterms));
    printf("  comp.wrs.text : %8.2f%%\n", 100.0-((double)bafsize*100)/((textsize)));
    printf("baf write time  : %8.2fs\n", ((double)bafwrite)/((double)CLK_TCK));
    printf("  per node      : %8.2fus\n", ((double)bafwrite*1000000.0/subterms)/((double)CLK_TCK));
    printf("baf read time   : %8.2fs\n", ((double)bafread)/((double)CLK_TCK));
    printf("  per node      : %8.2fus\n", ((double)bafread*1000000.0/subterms)/((double)CLK_TCK));
    fclose(file);
  }

  return 0;
}

/*}}}  */
