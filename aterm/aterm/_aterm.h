
#ifndef _ATERM_H
#define _ATERM_H

#include "aterm2.h"

void AT_markTerm(ATerm t);
int  AT_calcTextSize(ATerm t);
int  AT_calcCoreSize(ATerm t);
void AT_writeToStringBuffer(ATerm t, char *buffer);

#endif
