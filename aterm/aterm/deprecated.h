
#ifndef DEPRECATED_H
#define DEPRECATED_H

ATerm ATmakeTerm(ATerm pat, ...);
ATerm ATvmakeTerm(ATerm pat, va_list args);
ATbool ATmatchTerm(ATerm t, ATerm pat, ...);
ATbool ATvmatchTerm(ATerm t, ATerm pat, va_list args);

#endif
