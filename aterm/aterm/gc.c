/*{{{  includes */

#include <stdlib.h>
#include <time.h>
#include <limits.h>
#include <assert.h>
#include <setjmp.h>

#ifndef WIN32
#include <unistd.h>
#include <sys/times.h>
#endif

#include "_aterm.h"
#include "afun.h"
#include "memory.h"
#include "util.h"
#include "gc.h"
#include "debug.h"

/*}}}  */
/*{{{  globals */

char gc_id[] = "$Id$";

static ATerm *stackBot = NULL;

#define PRINT_GC_TIME           1
#define PRINT_GC_STATS          2

static int     flags               = 0;

int at_gc_count			   = 0;
static int     stack_depth[3]      = { 0, MYMAXINT, 0 };
static int     reclaim_perc[3]     = { 0, MYMAXINT, 0 };
extern int     mark_stats[3];
#ifdef WITH_STATS
static clock_t sweep_time[3]       = { 0, MYMAXINT, 0 };
static clock_t mark_time[3]        = { 0, MYMAXINT, 0 };
extern int     nr_marks;
#endif
static FILE *gc_f = NULL;

AFun at_parked_symbol = -1;

/*}}}  */

/*{{{  void AT_initGC(int argc, char *argv[], ATerm *bottomOfStack) */

/**
 * Initialize the garbage collector.
 */

void AT_initGC(int argc, char *argv[], ATerm *bottomOfStack)
{
  int i;

  stackBot = bottomOfStack;
    //gc_f = fopen("gc.stats", "wb");
  gc_f = stderr;
  
  for(i=1; i<argc; i++) {
    if(streq(argv[i], "-at-print-gc-time"))
      flags |= PRINT_GC_TIME;
    else if(streq(argv[i], "-at-print-gc-info"))
      flags |= (PRINT_GC_TIME | PRINT_GC_STATS);
    else if(strcmp(argv[i], "-at-help") == 0) {
      fprintf(stderr, "    %-20s: print non-intrusive gc information "
	      "after execution\n", "-at-print-gc-time");
      fprintf(stderr, "    %-20s: print elaborate gc information "
	      "after execution\n", "-at-print-gc-info");
    }
  }
}

/*}}}  */
/*{{{  void AT_setBottomOfStack(ATerm *bottomOfStack) */

/**
 * This function can be used to change the bottom of the stack.
 * Note that we only have one application that uses this fuction:
 * the Java ATerm wrapper interface, because here the garbage collector
 * can be called from different (but synchronized) threads, so at
 * the start of any operation that could start the garbage collector,
 * the bottomOfStack must be adjusted to point to the stack of
 * the calling thread.
 */

void AT_setBottomOfStack(ATerm *bottomOfStack)
{
  stackBot = bottomOfStack;
}

/*}}}  */

/*{{{  ATerm *stack_top() */

/**
 * Find the top of the stack.
 */

ATerm *stack_top()
{
  ATerm topOfStack;
  ATerm *top = &topOfStack;

  return top;
}

/*}}}  */

/*{{{  static void mark_memory(ATerm *start, ATerm *stop) */

//static int nb_cell_in_stack = 0;

static void mark_memory(ATerm *start, ATerm *stop)
{
  ATerm *cur, real_term;
#ifdef AT_64BIT
  ATerm odd_term;
  AFun odd_sym;
#endif
    //fprintf(stderr,"---> mark_memory phase [%x,%x]\n",start,stop);
    /* Traverse the stack */
  for(cur=start; cur<stop; cur++) {
    if(AT_isPotentialTerm(*cur)) {
      if(!IS_MARKED((*cur)->header)) {
        real_term = AT_isInsideValidTerm(*cur);
        if (real_term != NULL) {
          AT_markTerm(real_term);
            //printf("mark_memory: cur = %x\ttop sym = %s\n",cur,ATgetName(ATgetAFun(real_term)));
            //nb_cell_in_stack++;
        }
      }
    } else if (AT_isValidSymbol((Symbol)*cur)) {
        //fprintf(stderr,"mark_memory: AT_markSymbol(%d)\n",(Symbol)*cur);
      AT_markSymbol((Symbol)*cur);
        //nb_cell_in_stack++;
    }

#ifdef AT_64BIT
    odd_term = *((ATerm *)((MachineWord)cur)+4);
    real_term = AT_isInsideValidTerm(odd_term);
    if (real_term != NULL) {
      AT_markTerm(odd_term);
    }

    odd_sym = *((AFun *)((MachineWord)cur)+4);
    if (AT_isValidSymbol(odd_sym)) {
        //fprintf(stderr,"mark_memory: AT_markSymbol(%d)\n",odd_sym);
      AT_markSymbol(odd_sym);
    }
#endif
  }
}


#ifndef PO
static void mark_memory_young(ATerm *start, ATerm *stop) {
  ATerm *cur, real_term;
#ifdef AT_64BIT
  ATerm odd_term;
  AFun odd_sym;
#endif
    //fprintf(stderr,"---> mark_memory_young phase [%x,%x]\n",start,stop);
    /* Traverse the stack */
  for(cur=start; cur<stop; cur++) {
    if(AT_isPotentialTerm(*cur)) {
      if(!IS_MARKED((*cur)->header)) {
        real_term = AT_isInsideValidTerm(*cur);
        if (real_term != NULL) {
          AT_markTerm_young(real_term);
            //printf("mark_memory: cur = %x\ttop sym = %s\n",cur,ATgetName(ATgetAFun(real_term)));
            //nb_cell_in_stack++;
        }
      }
    } else if (AT_isValidSymbol((Symbol)*cur)) {
        //fprintf(stderr,"mark_memory_young: AT_markSymbol_young(%d)\n",(Symbol)*cur);
      AT_markSymbol_young((Symbol)*cur);
        //nb_cell_in_stack++;
    }

#ifdef AT_64BIT
    odd_term = *((ATerm *)((MachineWord)cur)+4);
    real_term = AT_isInsideValidTerm(odd_term);
    if (real_term != NULL) {
      AT_markTerm_young(odd_term);
    }

    odd_sym = *((AFun *)((MachineWord)cur)+4);
    if (AT_isValidSymbol(odd_sym)) {
        //fprintf(stderr,"mark_memory_young: AT_markSymbol_young(%d)\n",odd_sym);
      AT_markSymbol_young(odd_sym);
    }
#endif
  }
}
#endif

/*}}}  */
/*{{{  void mark_phase() */

/**
 * Mark all terms reachable from the root set.
 */

#ifdef WIN32
#define VOIDCDECL void __cdecl
#else
#define VOIDCDECL void
#endif

VOIDCDECL mark_phase()
{
  int i, j;
  int stack_size;
  ATerm *stackTop;
  ATerm *start, *stop;
  ProtEntry *prot;

#ifdef AT_64BIT
  ATerm oddTerm;
  AFun oddSym;
#endif

#ifdef WIN32
    /*{{{  Process registers on WIN32 platform */

  unsigned int r_eax, r_ebx, r_ecx, r_edx, \
    r_esi, r_edi, r_esp, r_ebp;
  ATerm reg[8];

  __asm {
      /* Get the registers into local variables to check them
         for aterms later. */
    mov r_eax, eax
      mov r_ebx, ebx
      mov r_ecx, ecx
      mov r_edx, edx
      mov r_esi, esi
      mov r_edi, edi
      mov r_esp, esp
      mov r_ebp, ebp
      }
    /* Put the register-values into an array */
  reg[0] = (ATerm) r_eax;
  reg[1] = (ATerm) r_ebx;
  reg[2] = (ATerm) r_ecx;
  reg[3] = (ATerm) r_edx;
  reg[4] = (ATerm) r_esi;
  reg[5] = (ATerm) r_edi;
  reg[6] = (ATerm) r_esp;
  reg[7] = (ATerm) r_ebp;

  nr_stack_terms = 0;
  nr_stack_syms  = 0;
  nr_reg_terms   = 0;
  nr_reg_syms    = 0;

  for(i=0; i<8; i++) {
    real_term = AT_isInsideValidTerm(reg[i]);
    if (real_term != NULL) {
      AT_markTerm(real_term);
    }
    if (AT_isValidSymbol((Symbol)reg[i])) {
      AT_markSymbol((Symbol)reg[i]);
    }
  }

    /* The register variables are on the stack aswell
       I set them to zero so they won't be processed again when
       the stack is traversed. The reg-array is also in the stack
       but that will be adjusted later */
  r_eax = 0;
  r_ebx = 0;
  r_ecx = 0;
  r_edx = 0;
  r_esi = 0;
  r_edi = 0;
  r_esp = 0;
  r_ebp = 0;

    /*}}}  */
#else
    /*{{{  Process registers on UNIX platform */

  sigjmp_buf env;

    /* Traverse possible register variables */
  sigsetjmp(env,0);

  start = (ATerm *)env;
  stop  = ((ATerm *)(((char *)env) + sizeof(sigjmp_buf)));
  mark_memory(start, stop);

    /*}}}  */
#endif

  stackTop = stack_top();

    /*{{{  Mark the stack */

  start = MIN(stackTop, stackBot);
  stop  = MAX(stackTop, stackBot);

  stack_size = stop-start;
  STATS(stack_depth, stack_size);

  mark_memory(start, stop);

    /*}}}  */

    /*{{{  Traverse protected terms */

    /* Traverse protected terms */
  for(i=0; i<at_prot_table_size; i++) {
    ProtEntry *cur = at_prot_table[i];
    while(cur) {
      for(j=0; j<cur->size; j++) {
	if(cur->start[j])
	  AT_markTerm(cur->start[j]);
      }
      cur = cur->next;
    }
  }

    /*}}}  */
    /*{{{  Traverse protected memory */

  for (prot=at_prot_memory; prot != NULL; prot=prot->next) {
    mark_memory((ATerm *)prot->start, (ATerm *)(((char *)prot->start) + prot->size));
  }

    /*}}}  */

    /* Mark protected symbols */
  AT_markProtectedSymbols();

    /* Mark 'parked' symbol */
  if (AT_isValidSymbol(at_parked_symbol)) {
      //fprintf(stderr,"mark_phase: AT_markSymbol(%d)\n",at_parked_symbol);
    AT_markSymbol(at_parked_symbol);
  }
}

#ifndef PO
VOIDCDECL mark_phase_young() {
  int i, j;
  int stack_size;
  ATerm *stackTop;
  ATerm *start, *stop;
  ProtEntry *prot;

#ifdef AT_64BIT
  ATerm oddTerm;
  AFun oddSym;
#endif

#ifdef WIN32
    /*{{{  Process registers on WIN32 platform */

  unsigned int r_eax, r_ebx, r_ecx, r_edx, \
    r_esi, r_edi, r_esp, r_ebp;
  ATerm reg[8];

  __asm {
      /* Get the registers into local variables to check them
         for aterms later. */
    mov r_eax, eax
      mov r_ebx, ebx
      mov r_ecx, ecx
      mov r_edx, edx
      mov r_esi, esi
      mov r_edi, edi
      mov r_esp, esp
      mov r_ebp, ebp
      }
    /* Put the register-values into an array */
  reg[0] = (ATerm) r_eax;
  reg[1] = (ATerm) r_ebx;
  reg[2] = (ATerm) r_ecx;
  reg[3] = (ATerm) r_edx;
  reg[4] = (ATerm) r_esi;
  reg[5] = (ATerm) r_edi;
  reg[6] = (ATerm) r_esp;
  reg[7] = (ATerm) r_ebp;

  nr_stack_terms = 0;
  nr_stack_syms  = 0;
  nr_reg_terms   = 0;
  nr_reg_syms    = 0;

  for(i=0; i<8; i++) {
    real_term = AT_isInsideValidTerm(reg[i]);
    if (real_term != NULL) {
      AT_markTerm_young(real_term);
    }
    if (AT_isValidSymbol((Symbol)reg[i])) {
       AT_markSymbol_young((Symbol)reg[i]);
    }
  }

    /* The register variables are on the stack aswell
       I set them to zero so they won't be processed again when
       the stack is traversed. The reg-array is also in the stack
       but that will be adjusted later */
  r_eax = 0;
  r_ebx = 0;
  r_ecx = 0;
  r_edx = 0;
  r_esi = 0;
  r_edi = 0;
  r_esp = 0;
  r_ebp = 0;

    /*}}}  */
#else
    /*{{{  Process registers on UNIX platform */

  sigjmp_buf env;

    /* Traverse possible register variables */
  sigsetjmp(env,0);

  start = (ATerm *)env;
  stop  = ((ATerm *)(((char *)env) + sizeof(sigjmp_buf)));
  mark_memory_young(start, stop);

    /*}}}  */
#endif

  stackTop = stack_top();

    /*{{{  Mark the stack */

  start = MIN(stackTop, stackBot);
  stop  = MAX(stackTop, stackBot);

  stack_size = stop-start;
  STATS(stack_depth, stack_size);

  mark_memory_young(start, stop);

    /*}}}  */

    /*{{{  Traverse protected terms */

    /* Traverse protected terms */
  for(i=0; i<at_prot_table_size; i++) {
    ProtEntry *cur = at_prot_table[i];
    while(cur) {
      for(j=0; j<cur->size; j++) {
	if(cur->start[j])
	   AT_markTerm_young(cur->start[j]);
      }
      cur = cur->next;
    }
  }

    /*}}}  */
    /*{{{  Traverse protected memory */

  for (prot=at_prot_memory; prot != NULL; prot=prot->next) {
    mark_memory_young((ATerm *)prot->start, (ATerm *)(((char *)prot->start) + prot->size));
  }

    /*}}}  */

    /* Mark protected symbols */
  AT_markProtectedSymbols_young();

    /* Mark 'parked' symbol */
  if (AT_isValidSymbol(at_parked_symbol)) {
      //fprintf(stderr,"mark_phase_young: AT_markSymbol_young(%d)\n",at_parked_symbol);
     AT_markSymbol_young(at_parked_symbol);
  }
}
#endif

/*}}}  */


/*{{{  void sweep_phase() */

/**
 * Sweep all unmarked terms into the appropriate free lists.
 */

void po_sweep_phase();
void major_sweep_phase_old();
void major_sweep_phase_young();
void minor_sweep_phase_young();
void check_unmarked_block(Block **blocks);

#ifdef NDEBUG
#define CHECK_UNMARKED_BLOCK(blocks)
#else
#define CHECK_UNMARKED_BLOCK(blocks) check_unmarked_block(blocks)
#endif

void sweep_phase() {
#ifndef PO
  int size;
    // empty the AT_freelist[size]
  for(size=MIN_TERM_SIZE; size<MAX_TERM_SIZE; size++) {
    at_freelist[size] = NULL;
  }
  old_bytes_in_young_blocks_after_last_major = 0;
  old_bytes_in_old_blocks_after_last_major = 0;

    // Warning: freelist[size] is empty
    // Warning: do not sweep fresh promoted block
  major_sweep_phase_old();
  major_sweep_phase_young();
  CHECK_UNMARKED_BLOCK(at_blocks);
  CHECK_UNMARKED_BLOCK(at_old_blocks);
  
#else
  po_sweep_phase();
#endif
}

#ifndef PO
int gc_min_number_of_blocks;
int max_freeblocklist_size;
int min_nb_minor_since_last_major;
int good_gc_ratio;
int small_allocation_rate_ratio;
int old_increase_rate_ratio;

void AT_init_gc_parameters(ATbool low_memory) {
  if(low_memory) {
    gc_min_number_of_blocks = 2;
    max_freeblocklist_size  = 30;
    min_nb_minor_since_last_major = 2;

    good_gc_ratio = 50;
    small_allocation_rate_ratio = 25;
    old_increase_rate_ratio = 50;

  } else {
#ifndef NO_SHARING
      // 20MB for 10 sizes in average
    gc_min_number_of_blocks = 2*(20*1024*1024)/(10*sizeof(Block));
    max_freeblocklist_size  = 100;
    min_nb_minor_since_last_major = 10;
    good_gc_ratio = 50;
    small_allocation_rate_ratio = 25;
    old_increase_rate_ratio = 50;
#else
      // 20MB for 10 sizes in average
    gc_min_number_of_blocks = 5*(20*1024*1024)/(10*sizeof(Block));
    max_freeblocklist_size  = 1000;
    min_nb_minor_since_last_major = 100;
    good_gc_ratio = 40;
    small_allocation_rate_ratio = 15;
    old_increase_rate_ratio = 60;
#endif
#ifdef GC_VERBOSE
    fprintf(stderr,"gc_min_number_of_blocks = %d\n",gc_min_number_of_blocks);
#endif
  }
}

#ifndef NO_SHARING
#define TO_OLD_RATIO   75
#define TO_YOUNG_RATIO 25
#else
#define TO_OLD_RATIO   60
#define TO_YOUNG_RATIO 25
#endif

static void reclaim_empty_block(Block **blocks, int size, Block *removed_block, Block *prev_block) { 
  nb_reclaimed_blocks_during_last_gc[size]++;

    /*
     * Step 1:
     *
     * remove cells from at_freelist[size]
     * remove the block from at_blocks[size]
     *
     */
    
#ifdef GC_VERBOSE
  fprintf(stdout,"block %x is empty\n",(unsigned int)removed_block);
#endif
  at_nrblocks[size]--;
  removed_block->size = 0;
  if(prev_block == NULL) {
      //fprintf(stderr,"restore_block: remove first\n");
    blocks[size] = removed_block->next_by_size;
    if(blocks==at_blocks && at_blocks[size]) {
      top_at_blocks[size] = at_blocks[size]->end;
    }
  } else {
      //fprintf(stderr,"restore_block: remove middle\n");
    prev_block->next_by_size = removed_block->next_by_size;
  }

    /*
     * Step 2:
     *
     * put the block into at_freeblocklist
     *
     */
  removed_block->next_by_size = at_freeblocklist;
  at_freeblocklist = removed_block;
  at_freeblocklist_size++;
        
    //printf("at_freeblocklist_size = %d\n",at_freeblocklist_size);
    /*
     * Step 3:
     *
     * remove the block from block_table
     * free the memory
     *
     */
  if(at_freeblocklist_size > max_freeblocklist_size) {
    int idx, next_idx;
    Block *cur;
    Block *prev = NULL;
          
      //printf("destroy block %d\n",removed_block);
    assert(removed_block != NULL);
          
    idx = ADDR_TO_BLOCK_IDX(removed_block);
    next_idx = (idx+1)%BLOCK_TABLE_SIZE;
    for(cur=block_table[idx].first_after; cur ; prev=cur, cur=cur->next_after) {
      if(removed_block == cur) {
          //printf("block found\n");
        break;
      }
    }
    if(!cur) {
      ATabort("### block %d not found\n",removed_block);
    }

    if(prev==NULL) {
      block_table[idx].first_after       = removed_block->next_after;
      block_table[next_idx].first_before = removed_block->next_after;
    } else {
      prev->next_after  = removed_block->next_after;
      prev->next_before = removed_block->next_before;
    }
          
    at_freeblocklist_size--;
    at_freeblocklist = at_freeblocklist->next_by_size;
#ifdef GC_VERBOSE
    fprintf(stderr,"free block %d\n",(int)removed_block);
#endif
    free(removed_block);
  }
}

static void promote_block_to_old(int size, Block *block, Block *prev_block) {
#ifdef GC_VERBOSE
  printf("move block %x to old_blocks\n",(unsigned int)block);
#endif
  assert(block!=NULL);
  if(prev_block == NULL) {
    at_blocks[size] = block->next_by_size;
    if(at_blocks[size]) {
      top_at_blocks[size] = at_blocks[size]->end;
    }
   
  } else {
    prev_block->next_by_size = block->next_by_size;
  }
  block->next_by_size = at_old_blocks[size];
  at_old_blocks[size] = block;
}

static void promote_block_to_young(int size, Block *block, Block *prev_block) {
#ifdef GC_VERBOSE
  printf("move block %x to young_blocks\n",(unsigned int)block);
#endif
  assert(block!=NULL);
  if(prev_block == NULL) {
    at_old_blocks[size] = block->next_by_size;
  } else {
    prev_block->next_by_size = block->next_by_size;
  }
  if(at_blocks[size]) {
    block->next_by_size = at_blocks[size]->next_by_size;
    at_blocks[size]->next_by_size = block;
  } else {
    block->next_by_size = NULL;
    at_blocks[size] = block;
    top_at_blocks[size] = block->end;
    assert(at_blocks[size] != NULL);
  }
}

void check_unmarked_block(Block **blocks) {
  int size;
  fprintf(stderr,"check_unmarked_block\n");
  
  for(size=MIN_TERM_SIZE; size<MAX_TERM_SIZE; size++) {
    Block *block = blocks[size];
    header_type *end = NULL;

    if(blocks == at_blocks) {
      end = top_at_blocks[size];
    } else {
      if(block) {
        end = block->end;
      } 
    }
    
    while(block) {
      header_type *cur;
      for(cur=block->data ; cur<end ; cur+=size) {
	ATerm t = (ATerm)cur;

        if(IS_MARKED(t->header)) {
#ifdef GC_VERBOSE
          fprintf(stderr,"block = %p\tdata = %p\tblock->end = %p\tend = %p\n",block,block->data,block->end,end);
          fprintf(stderr,"type = %d\n",GET_TYPE(t->header));
          fprintf(stderr,"t = %p\n",t);
#endif
        }

        if(blocks==at_old_blocks) {
          assert(GET_TYPE(t->header)==AT_FREE || IS_OLD(t->header));
        }
        
        assert(!IS_MARKED(t->header));
      }
      block = block->next_by_size;
      if(block) {
        end = block->end;
      }
    }
  }
}

void major_sweep_phase_old() {
  int size, perc;
  int reclaiming = 0;
  int alive = 0;

  for(size=MIN_TERM_SIZE; size<MAX_TERM_SIZE; size++) {
    Block *prev_block = NULL;
    Block *next_block;

    Block *block = at_old_blocks[size];

    while(block) {
        // set empty = 0 to avoid recycling
      int empty = 1;
      int alive_in_block = 0;
      int dead_in_block  = 0;
      int free_in_block  = 0;
      int capacity = ((block->end)-(block->data))/size;
      header_type *cur;

      assert(block->size == size);

        //fprintf(stderr,"major_sweep_phase_old: block (%x)\tsize = %d\n",(unsigned int)block,size);

      for(cur=block->data ; cur<block->end ; cur+=size) {
          // TODO: Optimisation
	ATerm t = (ATerm)cur;
	if(IS_MARKED(t->header)) {
	  CLR_MARK(t->header);
          alive_in_block++;
          empty = 0;
          assert(IS_OLD(t->header));
	} else {
	  switch(ATgetType(t)) {
              case AT_FREE:
                assert(IS_YOUNG(t->header));
                free_in_block++;
                break;
              case AT_INT:
              case AT_REAL:
              case AT_APPL:
              case AT_LIST:
              case AT_PLACEHOLDER:
              case AT_BLOB:
                assert(IS_OLD(t->header));
                  //fprintf(stderr,"*** AT_freeTerm term[%d] = %x\theader = %x\n",size,(unsigned int)t,t->header);
                  //fprintf(stderr,"MAJOR OLD FREE(%x)\n",(unsigned int)t);

                AT_freeTerm(size, t);
                t->header=FREE_HEADER;
                dead_in_block++;
                break;
              case AT_SYMBOL:
                assert(IS_OLD(t->header));
                AT_freeSymbol((SymEntry)t);
                t->header=FREE_HEADER;
                dead_in_block++;
                break;

              default:
                ATabort("panic in sweep phase\n");
	  }
	}
      }
      assert(alive_in_block + dead_in_block + free_in_block == capacity);
      
      next_block = block->next_by_size;
      
#ifndef NDEBUG
      if(empty) {
        for(cur=block->data; cur<block->end; cur+=size) {
          assert(ATgetType((ATerm)cur) == AT_FREE);
        }
      }
#endif
      
      if(empty) {
          // DO NOT RESTORE THE FREE LIST: free cells have not been inserted
          // at_freelist[size] = old_freelist;
        assert(top_at_blocks[size] < block->data || top_at_blocks[size] > block->end);
#ifdef GC_VERBOSE
        fprintf(stderr,"MAJOR OLD: reclaim empty block %p\n",block);
#endif
        reclaim_empty_block(at_old_blocks, size, block, prev_block);
      } else if(0 && 100*alive_in_block/capacity <= TO_YOUNG_RATIO) {
        promote_block_to_young(size, block, prev_block);
        old_bytes_in_young_blocks_after_last_major += (alive_in_block*SIZE_TO_BYTES(size));
      } else {
        old_bytes_in_old_blocks_after_last_major += (alive_in_block*SIZE_TO_BYTES(size));
        
          // DO NOT FORGET THIS LINE
          // update the previous block
        prev_block = block;
      }

      block = next_block;
        //end   = (block->data) + rounded_block_size;
      alive += alive_in_block;
      reclaiming += dead_in_block;
    }
  }
  if(alive) {
    perc = (100*reclaiming)/alive;
    STATS(reclaim_perc, perc);
  }
}

void major_sweep_phase_young() {
  int perc;
  int reclaiming = 0;
  int alive = 0;
  int size;

  old_bytes_in_young_blocks_since_last_major = 0;
  
  for(size=MIN_TERM_SIZE; size<MAX_TERM_SIZE; size++) {
    Block *prev_block = NULL;
    Block *next_block;
    ATerm old_freelist;

    Block *block      = at_blocks[size];
    header_type *end  = top_at_blocks[size];

    while(block) {
      int empty = 1;
      int alive_in_block = 0;
      int dead_in_block  = 0;
      int free_in_block  = 0;
      int old_in_block   = 0;
      int young_in_block = 0;
      int capacity = (end-(block->data))/size;
      header_type *cur;
      
      assert(block->size == size);

        //fprintf(stderr,"major_sweep_phase_young: block (%x)\tsize = %d\n",(unsigned int)block,size);

      old_freelist = at_freelist[size];
      for(cur=block->data ; cur<end ; cur+=size) {
          // TODO: Optimisation
	ATerm t = (ATerm)cur;
	if(IS_MARKED(t->header)) {
	  CLR_MARK(t->header);
          alive_in_block++;
          empty = 0;
          if(IS_OLD(t->header)) {
            old_in_block++;
          } else {
            young_in_block++;
          }
	} else {
	  switch(ATgetType(t)) {
              case AT_FREE:
                t->next = at_freelist[size];
                at_freelist[size] = t;
                free_in_block++;
                break;
              case AT_INT:
              case AT_REAL:
              case AT_APPL:
              case AT_LIST:
              case AT_PLACEHOLDER:
              case AT_BLOB:

                  //fprintf(stderr,"*** AT_freeTerm term[%d] = %x\theader = %x\n",size,(unsigned int)t,t->header);
                  //fprintf(stderr,"MAJOR YOUNG FREE(%x)\n",(unsigned int)t);

                AT_freeTerm(size, t);
                t->header = FREE_HEADER;
                t->next  = at_freelist[size];
                at_freelist[size] = t;
                
                dead_in_block++;
                break;
              case AT_SYMBOL:
                AT_freeSymbol((SymEntry)t);
                t->header = FREE_HEADER;
                t->next = at_freelist[size];
                at_freelist[size] = t;
                
                dead_in_block++;
                break;

              default:
                ATabort("panic in sweep phase\n");
	  }
	}
      }
      assert(alive_in_block + dead_in_block + free_in_block == capacity);
      
      next_block = block->next_by_size;

#ifndef NDEBUG
      if(empty) {
        for(cur=block->data; cur<end; cur+=size) {
          assert(ATgetType((ATerm)cur) == AT_FREE);
        }
      }
#endif

#ifdef GC_VERBOSE
        //fprintf(stderr,"old_cell_in_young_block ratio = %d\n",100*old_in_block/capacity);
#endif
       
      if(end==block->end && empty) {
#ifdef GC_VERBOSE
        fprintf(stderr,"MAJOR YOUNG: reclaim empty block %p\n",block);
#endif
        at_freelist[size] = old_freelist;
	reclaim_empty_block(at_blocks, size, block, prev_block);
      } else if(end==block->end &&
                young_in_block == 0 &&
                100*old_in_block/capacity >= TO_OLD_RATIO) {

          /*
        fprintf(stderr,"block %p\tcapacity = %d\n",block,capacity);
        fprintf(stderr,"alive = %d\n",alive_in_block);
        fprintf(stderr,"dead  = %d\n",dead_in_block);
        fprintf(stderr,"free  = %d\n",free_in_block);
        fprintf(stderr,"young = %d\n",young_in_block);
        fprintf(stderr,"old   = %d\n",old_in_block);
          */
        
        at_freelist[size] = old_freelist;
        
        if(young_in_block == 0) {
#ifdef GC_VERBOSE
          fprintf(stderr,"MAJOR YOUNG: promote block %p to old\n",block);
#endif
          promote_block_to_old(size, block, prev_block);
          old_bytes_in_old_blocks_after_last_major += (old_in_block*SIZE_TO_BYTES(size));
        } else {
#ifdef GC_VERBOSE
          fprintf(stderr,"MAJOR YOUNG: freeze block %p\n",block);
#endif
          old_bytes_in_young_blocks_after_last_major += (old_in_block*SIZE_TO_BYTES(size));
          prev_block = block;
        }
      } else {
        old_bytes_in_young_blocks_after_last_major += (old_in_block*SIZE_TO_BYTES(size));
        
          // DO NOT FORGET THIS LINE
          // update the previous block
        prev_block = block;
      }

      block = next_block;
      if(block) {
        end = block->end;
      }

      alive += alive_in_block;
      reclaiming += dead_in_block;
    }

#ifndef NDEBUG
    if(at_freelist[size]) {
      ATerm data;
      for(data = at_freelist[size] ; data ; data=data->next) {
        assert(EQUAL_HEADER(data->header,FREE_HEADER)); 
        assert(ATgetType(data) == AT_FREE);   
      } 
    }
#endif
    
  }
  if(alive) {
    perc = (100*reclaiming)/alive;
    STATS(reclaim_perc, perc);
  }
}

void minor_sweep_phase_young() {
  int size, perc;
  int reclaiming = 0;
  int alive = 0;

  old_bytes_in_young_blocks_since_last_major = 0;
  
  for(size=MIN_TERM_SIZE; size<MAX_TERM_SIZE; size++) {
    Block *prev_block = NULL;
    Block *next_block;
    ATerm old_freelist;

    Block *block = at_blocks[size];
    header_type *end = top_at_blocks[size];

      // empty the freelist
    at_freelist[size] = NULL;
        
    while(block) {
        // set empty = 0 to avoid recycling
      int empty = 1;
      int alive_in_block = 0;
      int dead_in_block  = 0;
      int free_in_block  = 0;
      int old_in_block  = 0;
      int capacity = (end-(block->data))/size;
      header_type *cur;
      
      assert(block->size == size);
        //fprintf(stderr,"minor_sweep_phase_young: block (%x)\tsize = %d\n",(unsigned int)block,size);
      
      old_freelist = at_freelist[size];
      for(cur=block->data ; cur<end ; cur+=size) {
          // TODO: Optimisation
	ATerm t = (ATerm)cur;
	if(IS_MARKED(t->header) || IS_OLD(t->header)) {
          if(IS_OLD(t->header)) {
            old_in_block++;
          }
	  CLR_MARK(t->header);
          alive_in_block++;
          empty = 0;
          assert(!IS_MARKED(t->header));
	} else {
	  switch(ATgetType(t)) {
              case AT_FREE:
                  // AT_freelist[size] is not empty: so DO NOT ADD t
                t->next = at_freelist[size];
                at_freelist[size] = t;
                  //fprintf(stderr,"AT_FREE term[%d] = %x\theader = %x\n",size,t,t->header);
                free_in_block++;
                break;
              case AT_INT:
              case AT_REAL:
              case AT_APPL:
              case AT_LIST:
              case AT_PLACEHOLDER:
              case AT_BLOB:

                  //fprintf(stderr,"MINOR YOUNG FREE(%x)\n",(unsigned int)t);
                  //fprintf(stderr,"AT_freeTerm term[%d] = %x\theader = %x\n",size,t,t->header);
                
                AT_freeTerm(size, t);
                
                t->header = FREE_HEADER;
                t->next   = at_freelist[size];
                at_freelist[size] = t;
                
                dead_in_block++;
                break;
              case AT_SYMBOL:

                  //fprintf(stderr,"AT_freeSymbol term[%d] = %x\theader = %x\n",size,t,t->header);
                AT_freeSymbol((SymEntry)t);
                
                t->header = FREE_HEADER;
                t->next   = at_freelist[size];
                at_freelist[size] = t;

                dead_in_block++;
                break;

              default:
                ATabort("panic in sweep phase\n");
	  }
          assert(!IS_MARKED(t->header));
	}
      }

        /*
      printf("block %x\tcapacity = %d\n\t",(unsigned int)block,capacity);
      printf("alive = %d\t",alive_in_block);
      printf("dead = %d\t",dead_in_block);
      printf("free = %d\n",free_in_block);
        */
      assert(alive_in_block + dead_in_block + free_in_block == capacity);
      
      next_block    = block->next_by_size;


#ifndef NDEBUG
      if(empty) {
        for(cur=block->data; cur<end; cur+=size) {
          assert(ATgetType((ATerm)cur) == AT_FREE);
        }
      }
#endif
      
        // TODO: create freeList Old
      if(0 && empty) {
        at_freelist[size] = old_freelist;
        reclaim_empty_block(at_blocks, size, block, prev_block);
      } else if(0 && 100*old_in_block/capacity >= TO_OLD_RATIO) {
        fprintf(stderr,"MINOR YOUNG: promote block %p to old\n",block);
        promote_block_to_old(size, block, prev_block);
      } else {
        old_bytes_in_young_blocks_since_last_major += (old_in_block*SIZE_TO_BYTES(size));
        
          // DO NOT FORGET THIS LINE
          // update the previous block
        prev_block = block;
      }

      block = next_block;
      if(block) {
        end = block->end;
      }
      alive += alive_in_block;
      reclaiming += dead_in_block;
    }

#ifndef NDEBUG
    if(at_freelist[size]) {
      ATerm data;
      fprintf(stderr,"minor_sweep_phase_young: ensure empty freelist[%d]\n",size);
      for(data = at_freelist[size] ; data ; data=data->next) {
        if(!EQUAL_HEADER(data->header,FREE_HEADER)) {
          fprintf(stderr,"data = %x header = %x\n",(unsigned int)data,data->header);
        }
        assert(EQUAL_HEADER(data->header,FREE_HEADER)); 
        assert(ATgetType(data) == AT_FREE);   
      }
    }
#endif
    
  }
  if(alive) {
    perc = (100*reclaiming)/alive;
    STATS(reclaim_perc, perc);
  }
}
#endif


void po_sweep_phase()
{
  int size, idx;
  int total = 0;
  int reclaiming = 0, perc;
  for(size=MIN_TERM_SIZE; size<MAX_TERM_SIZE; size++) {
    Block *block = at_blocks[size];
    int end = BLOCK_SIZE - (BLOCK_SIZE % size);
    while(block) {
      assert(block->size == size);
      for(idx=0; idx<end; idx+=size) {
	ATerm t = (ATerm)&(block->data[idx]);
	if(IS_MARKED(t->header)) {
	  CLR_MARK(t->header);
	} else {
	  switch(ATgetType(t)) {
              case AT_FREE:
                break;
              case AT_INT:
              case AT_REAL:
              case AT_APPL:
              case AT_LIST:
              case AT_PLACEHOLDER:
              case AT_BLOB:
                  //printf("AT_freeTerm block = %d\tterm = %d\n",block,t);
                AT_freeTerm(size, t);
                reclaiming++;
                break;
              case AT_SYMBOL:
                AT_freeSymbol((SymEntry)t);
                break;

              default:
                ATabort("panic in sweep phase\n");
	  }
	}
      }
      block = block->next_by_size;
    }
    total += at_nrblocks[size]*(BLOCK_SIZE/size);
  }
  perc = (100*reclaiming)/total;
  STATS(reclaim_perc, perc);
}

/*}}}  */

/*{{{  void AT_collect() */

/**
 * Collect all garbage
 */

#ifdef WIN32
void AT_collect()
/* The timing/STATS parts haven't been tested yet (on NT)
   but without the info things seem to work fine */
{
  clock_t start, mark, sweep;
  clock_t user;
  FILE *file = gc_f;
  int size;

      // snapshop
  for(size=MIN_TERM_SIZE; size<MAX_TERM_SIZE; size++) {
    nb_live_blocks_before_last_gc[size] = at_nrblocks[size];
    nb_reclaimed_blocks_during_last_gc[size]=0;
    nb_reclaimed_cells_during_last_gc[size]=0;
  }

  at_gc_count++;
  if (!silent)
  {
    fprintf(file, "collecting garbage..(%d)",at_gc_count);
    fflush(file);
  }
  start = clock();
  mark_phase();
  mark = clock();
  user = mark - start;
  STATS(mark_time, user);

  sweep_phase();
  sweep = clock();
  user = sweep - mark;
  STATS(sweep_time, user);

  if (!silent)
    fprintf(file, "..\n");
}
#else
void AT_collect()
{
  struct tms start, mark, sweep;
  clock_t user;
  FILE *file = gc_f;
  int size;

    // snapshop
  for(size=MIN_TERM_SIZE; size<MAX_TERM_SIZE; size++) {
    nb_live_blocks_before_last_gc[size] = at_nrblocks[size];
    nb_reclaimed_blocks_during_last_gc[size]=0;
    nb_reclaimed_cells_during_last_gc[size]=0;
  }
  
  at_gc_count++;
  if (!silent)
  {
    fprintf(file, "collecting garbage..(%d)",at_gc_count);
    fflush(file);
  }
  times(&start);
  CHECK_UNMARKED_BLOCK(at_blocks);
  CHECK_UNMARKED_BLOCK(at_old_blocks);
    //nb_cell_in_stack=0;
  mark_phase();
    //fprintf(stderr,"AT_collect: nb_cell_in_stack = %d\n",nb_cell_in_stack++);
  times(&mark);
  user = mark.tms_utime - start.tms_utime;
  STATS(mark_time, user);

#ifndef PO
    sweep_phase();

        //fprintf(stderr,"Warning: sweep_phase_young instead of sweep_phase\n");
    //sweep_phase_young();
#else
  sweep_phase();
#endif
  
  times(&sweep);
  user = sweep.tms_utime - mark.tms_utime;
  STATS(sweep_time, user);

  if (!silent)
    fprintf(file, "..\n");
}
#endif

#ifndef PO
#ifdef WIN32
void AT_collect_minor()
/* The timing/STATS parts haven't been tested yet (on NT)
   but without the info things seem to work fine */
{
  clock_t start, mark, sweep;
  clock_t user;
  FILE *file = gc_f;
  int size;

      // snapshop
  for(size=MIN_TERM_SIZE; size<MAX_TERM_SIZE; size++) {
    nb_live_blocks_before_last_gc[size] = at_nrblocks[size];
    nb_reclaimed_blocks_during_last_gc[size]=0;
    nb_reclaimed_cells_during_last_gc[size]=0;
  }

  at_gc_count++;
  if (!silent)
  {
    fprintf(file, "young collecting garbage..(%d)",at_gc_count);
    fflush(file);
  }
  start = clock();
  minor_mark_phase_young();
  mark = clock();
  user = mark - start;
  STATS(mark_time, user);

  minor_sweep_phase_young();
  sweep = clock();
  user = sweep - mark;
  STATS(sweep_time, user);

  if (!silent)
    fprintf(file, "..\n");
}
#else
void AT_collect_minor()
{
  struct tms start, mark, sweep;
  clock_t user;
  FILE *file = gc_f;
  int size;
  
    // snapshop
  for(size=MIN_TERM_SIZE; size<MAX_TERM_SIZE; size++) {
    nb_live_blocks_before_last_gc[size] = at_nrblocks[size];
    nb_reclaimed_blocks_during_last_gc[size]=0;
    nb_reclaimed_cells_during_last_gc[size]=0;
  }
  
  at_gc_count++;
  if (!silent)
  {
    fprintf(file, "young collecting garbage..(%d)",at_gc_count);
    fflush(file);
  }
  times(&start);
  CHECK_UNMARKED_BLOCK(at_blocks);
  CHECK_UNMARKED_BLOCK(at_old_blocks);
    //nb_cell_in_stack=0;
  mark_phase_young();
    //fprintf(stderr,"AT_collect_young: nb_cell_in_stack = %d\n",nb_cell_in_stack++);
  times(&mark);
  user = mark.tms_utime - start.tms_utime;
  STATS(mark_time, user);

  minor_sweep_phase_young();
  CHECK_UNMARKED_BLOCK(at_blocks);
  CHECK_UNMARKED_BLOCK(at_old_blocks);

  times(&sweep);
  user = sweep.tms_utime - mark.tms_utime;
  STATS(sweep_time, user);

  if (!silent)
    fprintf(file, "..\n");
}
#endif
#endif


/*}}}  */

/*{{{  void AT_cleanupGC() */

/**
 * Print garbage collection information
 */

#ifdef WIN32
#define CLOCK_DIVISOR CLOCKS_PER_SEC
#else
#define CLOCK_DIVISOR CLK_TCK
#endif

void AT_cleanupGC()
{
  FILE *file = gc_f;
  if(flags & PRINT_GC_TIME) {
    fprintf(file, "%d garbage collects,\n", at_gc_count);
#ifdef WITH_STATS
    fprintf(file, "(all statistics are printed min/avg/max)\n");
    if(at_gc_count > 0) {
      if(nr_marks > 0) {
	fprintf(file, "  mark stack needed: %d/%d/%d (%d marks)\n", 
		mark_stats[IDX_MIN],
                mark_stats[IDX_TOTAL]/nr_marks, 
		mark_stats[IDX_MAX], nr_marks);
      }
      fprintf(file, "  marking  took %.2f/%.2f/%.2f seconds, total: %.2f\n", 
	      ((double)mark_time[IDX_MIN])/(double)CLOCK_DIVISOR,
	      (((double)mark_time[IDX_TOTAL])/(double)at_gc_count)/(double)CLOCK_DIVISOR,
	      ((double)mark_time[IDX_MAX])/(double)CLOCK_DIVISOR,
	      ((double)mark_time[IDX_TOTAL])/(double)CLOCK_DIVISOR);
      fprintf(file, "  sweeping took %.2f/%.2f/%.2f seconds, total: %.2f\n", 
	      ((double)sweep_time[IDX_MIN])/(double)CLOCK_DIVISOR,
	      (((double)sweep_time[IDX_TOTAL])/(double)at_gc_count)/(double)CLOCK_DIVISOR,
	      ((double)sweep_time[IDX_MAX])/(double)CLOCK_DIVISOR,
	      ((double)sweep_time[IDX_TOTAL])/(double)CLOCK_DIVISOR);
    }
#ifdef WIN32
    fprintf(file, "Note: WinNT times are absolute, and might be influenced by other processes.\n");
#endif
#endif
  }
  
  if(flags & PRINT_GC_STATS) {
    if(at_gc_count > 0) {
      fprintf(file, "\n  stack depth: %d/%d/%d words\n", 
	      stack_depth[IDX_MIN],  
	      stack_depth[IDX_TOTAL]/at_gc_count,
	      stack_depth[IDX_MAX]);
      fprintf(file, "\n  reclamation percentage: %d/%d/%d\n",
	      reclaim_perc[IDX_MIN],
	      reclaim_perc[IDX_TOTAL]/at_gc_count,
	      reclaim_perc[IDX_MAX]);
    }
  }
}

/*}}}  */

