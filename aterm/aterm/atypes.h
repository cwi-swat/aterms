#ifndef ATYPES_H
#define ATYPES_H

#include "abool.h"

typedef unsigned int ShortHashNumber;

#ifdef AT_64BIT
typedef long MachineWord;
typedef unsigned long HashNumber;
#define ADDR_TO_SHORT_HNR(a) (((ShortHashNumber)((long)(a) & 0xFFFF) >> 2) ^ (((long)(a) >> 32)))

#else
typedef int MachineWord;
typedef unsigned int HashNumber;
#define ADDR_TO_SHORT_HNR(a) (((ShortHashNumber)(a)) >> 2)

#endif /* AT_64BIT */

#define ADDR_TO_HNR(a) (((HashNumber)(a)) >> 2)

#endif /* ATYPES_H */
