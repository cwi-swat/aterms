#include <AsFix-expand.h>
#include <AsFix-init-patterns.h>
#include <AsFix.h>

char asfixtest_id[] = "$Id$";

int main(int argc, char **argv)
{
  char *fname;
  ATerm mod, expmod;
  FILE *output, *input;

  ATerm bottomOfStack;

  fname = "Test.asfix";

  AFinit(argc, argv, &bottomOfStack);
  AFinitExpansionTerms();
  AFinitAsFixPatterns();

  input = fopen(fname,"r");
  if(!input)
	ATerror("cannot open file %s for reading, giving up.\n", fname);
  mod = ATreadFromTextFile(input);
  fclose(input);
  fname = "Test.asfix.asfix";
  expmod = AFexpandModuleToAsFix(mod,fname);
  ATprintf("Writing: %s\n", fname);
  output = fopen(fname,"w");
  if(!output)
    ATerror("cannot open file %s for writing, giving up.\n", fname);
  ATwriteToTextFile(expmod, output);
  fclose(output);

  ATprintf("AsFixTest was successful\n");

  return 0;
}
