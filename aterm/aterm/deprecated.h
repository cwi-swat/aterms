
#ifndef DEPRECATED_H
#define DEPRECATED_H

ATerm ATmakeTerm(ATerm pat, ...);
ATbool ATmatchTerm(ATerm t, ATerm pat, ...);
ATerm ATvmake(const char *pat);
ATerm ATvmakeTerm(ATerm pat);
void AT_vmakeSetArgs(va_list *args);
ATbool ATvmatch(ATerm t, const char *pat);
ATbool ATvmatchTerm(ATerm t, ATerm pat);

#endif
