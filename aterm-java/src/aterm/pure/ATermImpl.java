/*
 * Copyright (c) 2002-2007, CWI and INRIA
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package aterm.pure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import jjtraveler.VisitFailure;
import shared.SharedObject;
import shared.SharedObjectWithID;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermFactory;
import aterm.ATermList;
import aterm.ATermPlaceholder;

public abstract class ATermImpl extends ATermVisitableImpl implements ATerm, SharedObjectWithID{
	private ATermList annotations;

	protected PureFactory factory;

	private int hashCode;

	private int uniqueId;

	protected ATermImpl(PureFactory factory){
		super();
		this.factory = factory;
	}

	public final int hashCode(){
		return this.hashCode;
	}

	protected void setHashCode(int hashcode){
		this.hashCode = hashcode;
	}

	protected void internSetAnnotations(ATermList annos){
		this.annotations = annos;
	}

	protected void init(int hashCode, ATermList annos){
		this.hashCode = hashCode;
		this.annotations = annos;
	}

	public boolean equivalent(SharedObject obj){
		try{

			if(((ATerm) obj).getType() == getType()){
				return ((ATerm) obj).getAnnotations().equals(getAnnotations());
			}
			return false;
		}catch(ClassCastException e){
			return false;
		}
	}

	public ATermFactory getFactory(){
		return factory;
	}

	protected PureFactory getPureFactory(){
		return (PureFactory) getFactory();
	}
	
	public boolean hasAnnotations(){
		return (annotations != null && !annotations.isEmpty());
	}

	public ATerm setAnnotation(ATerm label, ATerm anno){
		ATermList new_annos = annotations.dictPut(label, anno);
		ATerm result = setAnnotations(new_annos);

		return result;
	}

	public ATerm removeAnnotation(ATerm label){
		return setAnnotations(annotations.dictRemove(label));
	}

	public ATerm getAnnotation(ATerm label){
		return annotations.dictGet(label);
	}

	public ATerm removeAnnotations(){
		return setAnnotations(((PureFactory) getFactory()).getEmpty());
	}

	public ATermList getAnnotations(){
		return annotations;
	}

	public List match(String pattern){
		return match(factory.parsePattern(pattern));
	}

	public List match(ATerm pattern){
		List list = new LinkedList();
		if(match(pattern, list)){
			return list;
		}
		return null;
	}

	public boolean isEqual(ATerm term){
		if(term instanceof ATermImpl){
			return this == term;
		}

		return factory.isDeepEqual(this, term);
	}

	public boolean equals(Object obj){
		if(obj instanceof ATermImpl){
			return this == obj;
		}

		if(obj instanceof ATerm){
			return factory.isDeepEqual(this, (ATerm) obj);
		}

		return false;
	}

	boolean match(ATerm pattern, List list){
		if(pattern.getType() == PLACEHOLDER){
			ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
			if(type.getType() == ATerm.APPL){
				ATermAppl appl = (ATermAppl) type;
				AFun afun = appl.getAFun();
				if(afun.getName().equals("term") && afun.getArity() == 0 && !afun.isQuoted()){
					list.add(this);
					return true;
				}
			}
		}

		return false;
	}

	public ATerm make(List list){
		return this;
	}

	public void writeToTextFile(ATermWriter writer) throws IOException{
		try{
			writer.voidVisitChild(this);
			writer.getStream().flush();
		}catch(VisitFailure e){
			throw new IOException(e.getMessage());
		}
	}

	public void writeToSharedTextFile(OutputStream stream) throws IOException{
		ATermWriter writer = new ATermWriter(stream);
		writer.initializeSharing();
		stream.write('!');
		writeToTextFile(writer);
	}

	public void writeToTextFile(OutputStream stream) throws IOException{
		ATermWriter writer = new ATermWriter(stream);
		writeToTextFile(writer);
	}

	public String toString(){
		try{
			OutputStream stream = new ByteArrayOutputStream();
			ATermWriter writer = new ATermWriter(stream);
			writeToTextFile(writer);
			
			return stream.toString();
		}catch(IOException e){
			throw new RuntimeException("IOException: " + e.getMessage());
		}
	}

	public int getNrSubTerms(){
		return 0;
	}

	public ATerm getSubTerm(int index){
		throw new RuntimeException("no children!");
	}

	public ATerm setSubTerm(int index, ATerm t){
		throw new RuntimeException("no children!");
	}

	public int getUniqueIdentifier(){
		return uniqueId;
	}

	public void setUniqueIdentifier(int uniqueId){
		this.uniqueId = uniqueId;
	}

}
