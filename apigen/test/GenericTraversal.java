package test;

import aterm.*;

/*
 * @author pem
 *
 * adapted from Jtom
 */
public class GenericTraversal {

	/*
	 * Traverse a subject and replace
	 */
	 
	public ATerm genericTraversal(ATerm subject, Replace replace) {
		ATerm res = subject;
		try {
		  if(subject instanceof ATermAppl) { 
			res = genericMapterm((ATermAppl) subject, replace);
		  } else if(subject instanceof ATermList) {
			res = genericMap((ATermList) subject, replace);
		  } else if(subject instanceof ATermInt) {
			res = subject;
		  }
		} catch(Exception e) {
		  e.printStackTrace();
		  System.out.println("Please, extend genericTraversal");
		  System.exit(0);
		}
		return res;
	  } 

 
		/*
		 * Apply a function to each element of a list
		 */
	  private ATermList genericMap(ATermList subject, Replace replace) {
		ATermList res = subject;
		try {
		  if(!subject.isEmpty()) {
			ATerm term = replace.apply(subject.getFirst());
			ATermList list = genericMap(subject.getNext(),replace);
			res = list.insert(term);
		  }
		} catch(Exception e) {
		  e.printStackTrace();
		  System.out.println("Please, extend genericMap");
		  System.exit(0);
		}
		return res;
	  }

		/*
		 * Apply a function to each subterm of a term
		 */
	  private ATermAppl genericMapterm(ATermAppl subject, Replace replace) {
		try {
		  ATerm newSubterm;
		  for(int i=0 ; i<subject.getArity() ; i++) {
			newSubterm = replace.apply(subject.getArgument(i));
			if(newSubterm != subject.getArgument(i)) {
			  subject = subject.setArgument(newSubterm,i);
			}
		  }
		} catch(Exception e) {
		  e.printStackTrace();
		  System.out.println("Please, extend genericMapterm");
		  System.exit(0);
		}
		return subject;
	  }

}
