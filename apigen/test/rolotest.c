
#include <stdio.h>
#include <assert.h>
#include <string.h>

#define streq(a,b) (strcmp((a),(b)) == 0)

#include "rolodex.h"

static Rolodex acceptRolodex(Rolodex rolo)
{
  /*ATfprintf(stderr, "acceptRolodex: %t\n", rolo);*/
  return rolo;
}

static void testRolodex()
{
  char * names[2];
  PhoneNumber phone[2];
  Rolodex rolo[2];
  RoloList list;

  names[0] = "Pieter";
  names[1] = "CWI";

  phone[0] = makePhoneNumberFromTerm(ATparse("voice(1234)"));
  phone[1] = makePhoneNumberFax(5678);

  rolo[0] = makeRolodexHome(names[0], phone[0]);
  rolo[1] = makeRolodexWork(names[1], phone[1]);

  list = makeRoloListEmpty();
  list = makeRoloListMulti(rolo[1], list);
  list = makeRoloListMulti(rolo[0], list);

  list = visitRoloList(list, acceptRolodex);

  assert(ATisEqual(makeRolodexFromTerm(makeTermFromRolodex(rolo[0])), 
		   rolo[0]));

  assert(isValidRolodex(rolo[0]));
  assert(isValidRolodex(rolo[1]));

  assert(!isRolodexWork(rolo[0]));
  assert(!isRolodexHome(rolo[1]));

  assert(streq(getRolodexName(rolo[0]),names[0]));
  assert(streq(getRolodexCompany(rolo[1]),names[1]));
  
  rolo[1] = setRolodexCompany(rolo[1], names[0]);
  assert(streq(getRolodexCompany(rolo[1]),names[0]));
}


int main(int argc, char *argv[])
{
  ATerm bottomOfStack;

  ATinit(argc, argv, &bottomOfStack);
  initRolodexApi();

  testRolodex();

  return 0;
}
