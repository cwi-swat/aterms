#include <expand2asfix.h>
#include <init-patterns.h>
#include <asfix.h>

int main(int argc, char **argv)
{
  char *fname;
  ATerm mod, expmod;
  FILE *output, *input;

  int bottomOfStack;

  fname = "/home/markvdb/NEW-META/aterm/test/Test.asfix";

  ATinit(argc,argv, NULL, &bottomOfStack);

  AFinit();
  AFinitExpansionTerms();
  AFinitAsFixPatterns();

  input = fopen(fname,"r");
  if(input) {
    mod = ATreadFromTextFile(input);
    fclose(input);
  }
  fname = "/home/markvdb/NEW-META/aterm/test/Test.asfix.asfix";
  expmod = AFexpandModuleToAsFix(mod,fname);
  ATprintf("Writing: %s\n", fname);
  output = fopen(fname,"w");
  ATwriteToTextFile(expmod, output);
  fclose(output);

  ATprintf("AsFixTest was successful\n");

  return 0;
}
