
/**
  * list.c: implementation of list functions.
  */

#include <stdlib.h>
#include <stdio.h>
#include "list.h"
#include "aterm2.h"

#define DEFAULT_LIST_BUFFER 256
#define RESIZE_BUFFER(n) if(n > buffer_size) resize_buffer(n)

static int buffer_size = 0;
static ATerm *buffer;

/*{{{  void AT_initList(int argc, char *argv[]) */

/**
  * Initialize list operations
  */

void AT_initList(int argc, char *argv[])
{
  buffer_size = DEFAULT_LIST_BUFFER;
  buffer = (ATerm *)malloc(buffer_size*sizeof(ATerm));
  if(!buffer) {
    ATerror("AT_initLists: cannot allocate list buffer of size %d\n", 
	    DEFAULT_LIST_BUFFER);
  }
}

/*}}}  */
/*{{{  static void resize_buffer(int n) */

/**
  * Make sure the buffer is large enough for a particular operation
  */

static void resize_buffer(int n)
{
  free(buffer);
  buffer_size = n;
  buffer = (ATerm *)malloc(buffer_size*sizeof(ATerm));
  if(!buffer) {
    ATerror("resize_buffer: cannot allocate list buffer of size %d\n",
	    DEFAULT_LIST_BUFFER);
  }
}

/*}}}  */

/*{{{  ATermList ATgetPrefix(ATermList list) */

/**
  * Build a new list containing all elements of list, except the last one.
  */

ATermList ATgetPrefix(ATermList list)
{
  int i, size = ATgetLength(list)-1;
  ATermList result = ATmakeList0();

  RESIZE_BUFFER(size);
  for(i=0; i<size; i++) {
    buffer[i] = ATgetFirst(list);
    list = ATgetNext(list);
  }

  for(--i; i>=0; i--)
    result = ATinsert(result, buffer[i]);

  return result;
}

/*}}}  */
/*{{{  ATerm ATgetLast(ATermList list) */

/**
  * Retrieve the last element of 'list'.
  * When 'list' is the empty list, NULL is returned.
  */

ATerm ATgetLast(ATermList list)
{
  ATermList old = ATempty;

  while(!ATisEmpty(list)) {
    old = list;
    list = ATgetNext(list);
  }
  return ATgetFirst(old);
}

/*}}}  */
/*{{{  ATermList ATgetSlice(ATermList list, int start, int end) */

/**
  * Retrieve a slice of elements from list.
  * The first element in the slice is the element at position start.
  * The last element is the element at end-1.
  */

ATermList ATgetSlice(ATermList list, int start, int end)
{
  int i, size = end-start;
  ATermList result = ATmakeList0();

  RESIZE_BUFFER(size);

  for(i=0; i<start; i++)
    list = ATgetNext(list);
 
  for(i=0; i<size; i++) {
    buffer[i] = ATgetFirst(list);
    list = ATgetNext(list);
  }
  
  for(--i; i>=0; i--)
    result = ATinsert(result, buffer[i]);

  return result;
}

/*}}}  */
/*{{{  ATermList ATinsertAt(ATermList list, ATerm el, int index) */

/**
  * Insert 'el' at position 'index' in 'list'.
  */

ATermList ATinsertAt(ATermList list, ATerm el, int index)
{
  int i;
  ATermList result;

  RESIZE_BUFFER(index);

  /* First collect the prefix in buffer */
  for(i=0; i<index; i++) {
    buffer[i] = ATgetFirst(list);
    list = ATgetNext(list);
  }

  /* Build a list consisting of 'el' and the postfix of the list */
  result = ATinsert(list, el);

  /* Insert elements before 'index' */
  for(--i; i>=0; i--)
    result = ATinsert(result, buffer[i]);

  return result;
}

/*}}}  */
/*{{{  ATermlist ATappend(ATermList list, ATerm el) */

/**
  * Append 'el' to the end of 'list'
  */

ATermList ATappend(ATermList list, ATerm el)
{
  int i, len = ATgetLength(list);
  ATermList result;

  RESIZE_BUFFER(len);
  /* Collect all elements of list in buffer */
  for(i=0; i<len; i++) {
    buffer[i] = ATgetFirst(list);
    list = ATgetNext(list);
  }

  result = ATmakeList1(el);

  /* Insert elements at the front of the list */
  for(--i; i>=0; i--)
    result = ATinsert(result, buffer[i]);

  return result;
}

/*}}}  */
/*{{{  ATermList ATconcat(ATermList list1, ATermList list2) */

/**
  * Concatenate list2 to the end of list1.
  */

ATermList ATconcat(ATermList list1, ATermList list2)
{
  int i, len = ATgetLength(list1);
  ATermList result = list2;

  RESIZE_BUFFER(len);

  /* Collect the elments of list1 in buffer */
  for(i=0; i<len; i++) {
    buffer[i] = ATgetFirst(list1);
    list1 = ATgetNext(list1);
  }

  /* Insert elements at the front of the list */
  for(--i; i>=0; i--)
    result = ATinsert(result, buffer[i]);

  return result;
}

/*}}}  */
/*{{{  int ATindexOf(ATermList list, ATerm el, int start) */

/**
  * Return the index of the first occurence of 'el' in 'list',
  * Start searching at index 'start'.
  * Return -1 when 'el' is not present after 'start'.
  * Note that 'start' must indicate a valid position in 'list'.
  */

int ATindexOf(ATermList list, ATerm el, int start)
{
  int i;

  for(i=0; i<start; i++)
    list = ATgetNext(list);

  while(!ATisEmpty(list) && !ATisEqual(ATgetFirst(list), el)) {
    list = ATgetNext(list);
    ++i;
  }

  return (ATisEmpty(list) ? -1 : i);
}

/*}}}  */
/*{{{  int ATlastIndexOf(ATermList list, ATerm el, int start) */

/**
  * Search backwards for 'el' in 'list'. Start searching at
  * index 'start'. Return the index of the first occurence of 'l'
  * encountered, or -1 when 'el' is not present before 'start'.
  */

int ATlastIndexOf(ATermList list, ATerm el, int start)
{
  int i, len = start+1;

  RESIZE_BUFFER(len);
  for(i=0; i<len; i++) {
    buffer[i] = ATgetFirst(list);
    list = ATgetNext(list);
  }
  for(--i; i>=0; i--) {
    if(ATisEqual(buffer[i], el))
      return i;
  }

  return -1;
}

/*}}}  */
/*{{{  ATerm ATelementAt(ATermList list, int index) */

/**
  * Retrieve the element at 'index' from 'list'.
  * Return NULL when index not in list.
  */

ATerm ATelementAt(ATermList list, int index)
{
  for(; index > 0 && !ATisEmpty(list); index--)
    list = ATgetNext(list);

  if(ATisEmpty(list))
    return NULL;

  return ATgetFirst(list);
}

/*}}}  */

/*{{{  ATermList ATgetArguments(ATermAppl appl) */

/**
  * Retrieve the list of arguments of a function application.
  * This function facilitates porting of old aterm-lib or ToolBus code.
  */

ATermList ATgetArguments(ATermAppl appl)
{
  Symbol s = ATgetSymbol(appl);
  int i, len = ATgetArity(s);
  ATermList result = ATempty;

  RESIZE_BUFFER(len);
  for(i=0; i<len; i++)
    buffer[i] = ATgetArgument(appl, i);

  for(--i; i>=0; i--)
    result = ATinsert(result, buffer[i]);

  return result;
}

/*}}}  */
