package aterm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

public interface ATermFactory
{
  static byte START_OF_SHARED_TEXT_FILE = (byte)'!';

  ATerm parse(String trm);
  ATerm make(String pattern, List args);
  ATerm make(ATerm pattern, List args);
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
	     Object arg4, Object arg5, Object arg6);
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
	     Object arg4, Object arg5);
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
	     Object arg4);
  ATerm make(String pattern, Object arg1, Object arg2, Object arg3);
  ATerm make(String pattern, Object arg1, Object arg2);
  ATerm make(String pattern, Object arg1);

  ATermInt makeInt(int val);
  ATermReal makeReal(double val);
  ATermList makeList();
  ATermList makeList(ATerm single);
  ATermList makeList(ATerm first, ATermList next);
  ATermPlaceholder makePlaceholder(ATerm type);
  ATermBlob makeBlob(byte[] data);

  AFun  makeAFun(String name, int arity, boolean isQuoted);
  ATermAppl makeAppl(AFun fun);
  ATermAppl makeAppl(AFun fun, ATerm arg);
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2);
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3);
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4);
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4, ATerm arg5);
  ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, 
		     ATerm arg4, ATerm arg5, ATerm arg6);
  ATermAppl makeAppl(AFun fun, ATerm[] args);
  ATermAppl makeAppl(AFun fun, ATermList args);

  ATerm readFromTextFile(InputStream stream) throws IOException;
  ATerm readFromSharedTextFile(InputStream stream) throws IOException;
  ATerm readFromBinaryFile(InputStream stream) throws IOException;
  ATerm readFromFile(InputStream stream) throws IOException;

  ATerm readFromFile(String file) throws IOException;

  ATerm importTerm(ATerm term);
}
