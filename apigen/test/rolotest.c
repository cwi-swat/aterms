
#include <stdio.h>
#include <assert.h>

#include "rolodex.h"

static void testRolodex()
{
  String  names[2];
  PhoneNumber phone[2];
  Rolodex rolo[2];
  ATerm t;

  names[0] = ATparse("Pieter");
  names[1] = ATparse("CWI");

  phone[0] = makePhoneNumberFromTerm(ATparse("voice(1234)"));
  phone[1] = makePhoneNumberFax(ATparse("5678"));

  rolo[0] = makeRolodexHome(names[0], phone[0]);
  rolo[1] = makeRolodexWork(names[1], phone[1]);

  assert(ATisEqual(makeRolodexFromTerm(makeTermFromRolodex(rolo[0])), 
		   rolo[0]));

  assert(isValidRolodex(rolo[0]));
  assert(isValidRolodex(rolo[1]));

  assert(!isRolodexWork(rolo[0]));
  assert(!isRolodexHome(rolo[1]));

  assert(ATisEqual(getRolodexName(rolo[0]),names[0]));
  assert(ATisEqual(getRolodexCompany(rolo[1]),names[1]));
  
  rolo[1] = setRolodexCompany(rolo[1], names[0]);
  assert(ATisEqual(getRolodexCompany(rolo[1]),names[0]));
}


int main(int argc, char *argv[])
{
  ATerm bottomOfStack;

  ATinit(argc, argv, &bottomOfStack);
  initRolodexApi();

  testRolodex();

  return 0;
}
