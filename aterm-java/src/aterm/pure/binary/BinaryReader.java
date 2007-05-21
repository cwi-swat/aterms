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

package aterm.pure.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import aterm.AFun;
import aterm.ATerm;
import aterm.ATermList;
import aterm.pure.PureFactory;

/**
 * Reconstructs an ATerm from the given (series of) buffer(s). It can be retrieved when the
 * construction of the term is done / when isDone() returns true.<br />
 * <br />
 * For example (yes I know this code is crappy, but it's simple):<blockquote><pre>
 * ByteBuffer buffer = ByteBuffer.allocate(8192);
 BinaryWriter bw = new BinaryWriter(aterm);
 while(!bw.isDone()) {
   int bytesRead = channel.read(buffer); // Read the next chunk of data from the stream.
   if(!buffer.hasRemaining() || bytesRead == -1) {
     bw.serialize(buffer);
     buffer.clear();
   }
 }
 * </pre></blockquote>
 * 
 * @author Arnold Lankamp
 */
public class BinaryReader{
	private final static int ISSHAREDFLAG = 0x00000080;
	private final static int TYPEMASK = 0x0000000f;
	private final static int ANNOSFLAG = 0x00000010;

	private final static int ISFUNSHARED = 0x00000040;
	private final static int APPLQUOTED = 0x00000020;
	
	private final static int INITIALSHAREDTERMSARRAYSIZE = 1024;

	private final static int STACKSIZE = 256;

	private final PureFactory factory;

	private int sharedTermIndex;
	private ATerm[] sharedTerms;
	private List<AFun> applSignatures;

	private ATermConstruct[] stack;
	private int stackPosition;
	
	private int tempType = -1;
	private byte[] tempBytes = null;
	private int tempBytesIndex = 0;
	private int tempArity = -1;
	private boolean tempIsQuoted = false;

	private ByteBuffer currentBuffer;

	private boolean isDone = false;
	
	/**
	 * Constructor.
	 * 
	 * @param factory
	 *            The factory to use for reconstruction of the ATerm.
	 */
	public BinaryReader(PureFactory factory){
		super();

		this.factory = factory;

		sharedTerms = new ATerm[INITIALSHAREDTERMSARRAYSIZE];
		applSignatures = new ArrayList<AFun>();
		sharedTermIndex = 0;

		stack = new ATermConstruct[STACKSIZE];
		stackPosition = -1;
	}
	
	/**
	 * Resizes the shared terms array when needed. When we're running low on space the capacity
	 * will be doubled.
	 */
	private void ensureSharedTermsCapacity(){
		int sharedTermsArraySize = sharedTerms.length;
		if(sharedTermIndex + 1 >= sharedTermsArraySize){
			ATerm[] newSharedTermsArray = new ATerm[sharedTermsArraySize << 1];
			System.arraycopy(sharedTerms, 0, newSharedTermsArray, 0, sharedTermsArraySize);
			sharedTerms = newSharedTermsArray;
		}
	}

	/**
	 * Constructs (a part of) the ATerm from the binary representation present in the given buffer.
	 * This method will 'remember' where it was left.
	 * 
	 * @param buffer
	 *            The buffer that contains (a part of) the binary representation of the ATerm.
	 */
	public void deserialize(ByteBuffer buffer){
		currentBuffer = buffer;
		
		if(tempType != -1) readData();

		while(buffer.hasRemaining()){
			byte header = buffer.get();

			if((header & ISSHAREDFLAG) == ISSHAREDFLAG){
				int index = readInt();
				ATerm term = sharedTerms[index];
				stackPosition++;
				
				linkTerm(term);
			}else{
				int type = (header & TYPEMASK);

				ATermConstruct ac = new ATermConstruct();
				ac.type = type;
				ac.hasAnnos = ((header & ANNOSFLAG) == ANNOSFLAG);
				
				ac.termIndex = sharedTermIndex++;
				ensureSharedTermsCapacity();

				stack[++stackPosition] = ac;

				TYPECHECK: switch(type){
					case ATerm.APPL:
						touchAppl(header);
						break TYPECHECK;
					case ATerm.LIST:
						touchList();
						break TYPECHECK;
					case ATerm.INT:
						touchInt();
						break TYPECHECK;
					case ATerm.REAL:
						touchReal();
						break TYPECHECK;
					case ATerm.LONG:
						touchLong();
						break TYPECHECK;
					case ATerm.BLOB:
						touchBlob();
						break TYPECHECK;
					case ATerm.PLACEHOLDER:
						touchPlaceholder();
						break TYPECHECK;
					default:
						throw new RuntimeException("Unknown type id: " + type + ". Current buffer position: " + currentBuffer.position());
				}
			}
			
			// Make sure the stack remains large enough
			ensureStackCapacity();
		}
	}
	
	/**
	 * Resizes the stack when needed. When we're running low on stack space the capacity will be
	 * doubled.
	 */
	private void ensureStackCapacity(){
		int stackSize = stack.length;
		if(stackPosition + 1 >= stackSize){
			ATermConstruct[] newStack = new ATermConstruct[(stackSize << 1)];
			System.arraycopy(stack, 0, newStack, 0, stack.length);
			stack = newStack;
		}
	}

	/**
	 * Checks if we are done serializing.
	 * 
	 * @return True if we are done; false otherwise.
	 */
	public boolean isDone(){
		return isDone;
	}

	/**
	 * Returns the reconstructed ATerm. A RuntimeException will be thrown when we are not yet done
	 * with the reconstruction of the ATerm.
	 * 
	 * @return The reconstructed ATerm.
	 */
	public ATerm getRoot(){
		if(!isDone) throw new RuntimeException("Can't retrieve the root of the tree while it's still being constructed.");

		return sharedTerms[0];
	}
	
	/**
	 * Resets the temporary data. We don't want to hold it if it's not nessecarry.
	 */
	private void resetTemp(){
		tempType = -1;
		tempBytes = null;
		tempBytesIndex = 0;
	}
	
	/**
	 * Reads a series of bytes from the buffer. When the nessecary amount of bytes is read a term
	 * of the corresponding type will be constructed and (if possible) linked with it's parent.
	 */
	private void readData(){
		int length = tempBytes.length;
		int bytesToRead = (length - tempBytesIndex);
		int remaining = currentBuffer.remaining();
		if(remaining < bytesToRead) bytesToRead = remaining;
		
		currentBuffer.get(tempBytes, tempBytesIndex, bytesToRead);
		tempBytesIndex += bytesToRead;
		
		if(tempBytesIndex == length){
			if(tempType == ATerm.APPL){
				AFun fun = factory.makeAFun(new String(tempBytes), tempArity, tempIsQuoted);
				applSignatures.add(fun);
				
				ATermConstruct ac = stack[stackPosition];
				if(tempArity == 0 && !ac.hasAnnos){
					ATerm term = factory.makeAppl(fun);
					sharedTerms[ac.termIndex] = term;
					linkTerm(term);
				}else{
					ac.tempTerm = fun;
					ac.subTerms = new ATerm[tempArity];
				}
			}else if(tempType == ATerm.BLOB){
				ATermConstruct ac = stack[stackPosition];
				ATerm term = factory.makeBlob(tempBytes);
				
				if(!ac.hasAnnos){
					sharedTerms[ac.termIndex] = term;
					linkTerm(term);
				}else{
					ac.tempTerm = term;
				}
			}else{
				throw new RuntimeException("Unsupported chunkified type: "+tempType);
			}
			
			resetTemp();
		}
	}

	/**
	 * Starts the deserialization process of a appl.
	 * 
	 * @param header
	 *            The header of the appl.
	 */
	private void touchAppl(byte header){
		if((header & ISFUNSHARED) == ISFUNSHARED){
			int key = readInt();

			AFun fun = applSignatures.get(key);
			
			int arity = fun.getArity();
			
			ATermConstruct ac = stack[stackPosition];
			
			if(arity == 0 && !ac.hasAnnos){
				ATerm term = factory.makeAppl(fun);
				sharedTerms[ac.termIndex] = term;
				linkTerm(term);
			}else{
				ac.tempTerm = fun;
				ac.subTerms = new ATerm[arity];
			}
		}else{
			tempIsQuoted = ((header & APPLQUOTED) == APPLQUOTED);
			tempArity = readInt();
			int nameLength = readInt();
			
			tempType = ATerm.APPL;
			tempBytes = new byte[nameLength];
			tempBytesIndex = 0;
			
			readData();
		}
	}

	/**
	 * Deserialializes a list.
	 */
	private void touchList(){
		int size = readInt();

		ATermConstruct ac = stack[stackPosition];
		ac.subTerms = new ATerm[size];

		if(size == 0){
			ATerm term = factory.makeList();

			if(!ac.hasAnnos){
				sharedTerms[ac.termIndex] = term;
				linkTerm(term);
			}else{
				ac.tempTerm = term;
			}
		}
	}

	/**
	 * Deserialializes an int.
	 */
	private void touchInt(){
		int value = readInt();

		ATermConstruct ac = stack[stackPosition];
		ATerm term = factory.makeInt(value);

		if(!ac.hasAnnos){
			sharedTerms[ac.termIndex] = term;
			linkTerm(term);
		}else{
			ac.tempTerm = term;
		}
	}

	/**
	 * Deserialializes a real.
	 */
	private void touchReal(){
		double value = readDouble();

		ATermConstruct ac = stack[stackPosition];
		ATerm term = factory.makeReal(value);

		if(!ac.hasAnnos){
			sharedTerms[ac.termIndex] = term;
			linkTerm(term);
		}else{
			ac.tempTerm = term;
		}
	}
	
	/**
	 * Deserialializes a long.
	 */
	private void touchLong(){
		long value = readLong();

		ATermConstruct ac = stack[stackPosition];
		ATerm term = factory.makeLong(value);

		if(!ac.hasAnnos){
			sharedTerms[ac.termIndex] = term;
			linkTerm(term);
		}else{
			ac.tempTerm = term;
		}
	}
	
	/**
	 * Starts the deserialization process for a BLOB.
	 */
	private void touchBlob(){
		int length = readInt();
		
		tempType = ATerm.BLOB;
		tempBytes = new byte[length];
		tempBytesIndex = 0;

		readData();
	}

	/**
	 * Deserialializes a placeholder.
	 */
	private void touchPlaceholder(){
		// A placeholder doesn't have content

		ATermConstruct ac = stack[stackPosition];
		ac.subTerms = new ATerm[1];
	}
	
	/**
	 * Constructs a term from the given structure.
	 * 
	 * @param ac
	 *            A structure that contains all the nessecary data to contruct the associated term.
	 * @return The constructed aterm.
	 */
	private ATerm buildTerm(ATermConstruct ac){
		ATerm constructedTerm;
		ATerm[] subTerms = ac.subTerms;
		
		int type = ac.type;
		if(type == ATerm.APPL){
			AFun fun = (AFun) ac.tempTerm;
			constructedTerm = factory.makeAppl(fun, subTerms, ac.annos);
		}else if(type == ATerm.LIST){
			ATermList list = factory.makeList();
			for(int i = subTerms.length - 1; i >= 0; i--){
				list = factory.makeList(subTerms[i], list);
			}

			if(ac.hasAnnos) list = (ATermList) list.setAnnotations(ac.annos);

			constructedTerm = list;
		}else if(type == ATerm.PLACEHOLDER){
			ATerm placeholder = factory.makePlaceholder(subTerms[0]);

			constructedTerm = placeholder;
		}else if(ac.hasAnnos){
			constructedTerm = ac.tempTerm.setAnnotations(ac.annos);
		}else{
			throw new RuntimeException("Unable to construct term.\n");
		}
		
		return constructedTerm;
	}

	/**
	 * Links the given term with it's parent.
	 * 
	 * @param aTerm
	 *            The term that needs to be linked.
	 */
	private void linkTerm(ATerm aTerm){
		ATerm term = aTerm;
		
		while(stackPosition != 0){
			ATermConstruct parent = stack[--stackPosition];
	
			ATerm[] subTerms = parent.subTerms;
			boolean hasAnnos = parent.hasAnnos;
			if(subTerms != null && subTerms.length > parent.subTermIndex) {
				subTerms[parent.subTermIndex++] = term;
				
				if(parent.subTerms.length != parent.subTermIndex || hasAnnos) return;
				
				if(!hasAnnos) parent.annos = factory.makeList();
			}else if(hasAnnos && (term instanceof ATermList)){
				parent.annos = (ATermList) term;
			} else {
				throw new RuntimeException("Encountered a term that didn't fit anywhere. Type: " + term.getType());
			}
			
			term = buildTerm(parent);
			
			sharedTerms[parent.termIndex] = term;
		}
		
		if(stackPosition == 0) isDone = true;
	}

	/**
	 * A structure that contains all information we need for reconstructing a term.
	 * 
	 * @author Arnold Lankamp
	 */
	private static class ATermConstruct{
		public int type;

		public int termIndex = 0;
		public ATerm tempTerm = null;

		public int subTermIndex = 0;
		public ATerm[] subTerms = null;

		public boolean hasAnnos;
		public ATermList annos;
	}

	private final static int SEVENBITS = 0x0000007f;
	private final static int SIGNBIT = 0x00000080;
	private final static int BYTEMASK = 0x000000ff;
	private final static int BYTEBITS = 8;
	private final static int LONGBITS = 8;

	/**
	 * Reconstructs an integer from the following 1 to 5 bytes in the buffer (depending on how many
	 * we used to represent the value). See the documentation of
	 * aterm.binary.BinaryWriter#writeInt(int) for more information.
	 * 
	 * @return The reconstructed integer.
	 */
	private int readInt(){
		byte part = currentBuffer.get();
		int result = (part & SEVENBITS);
		
		if((part & SIGNBIT) == 0) return result;
			
		part = currentBuffer.get();
		result |= ((part & SEVENBITS) << 7);
		if((part & SIGNBIT) == 0) return result;
			
		part = currentBuffer.get();
		result |= ((part & SEVENBITS) << 14);
		if((part & SIGNBIT) == 0) return result;
			
		part = currentBuffer.get();
		result |= ((part & SEVENBITS) << 21);
		if((part & SIGNBIT) == 0) return result;
			
		part = currentBuffer.get();
		result |= ((part & SEVENBITS) << 28);
		return result;
	}

	/**
	 * Reconstructs a double from the following 8 bytes in the buffer.
	 * 
	 * @return The reconstructed double.
	 */
	private double readDouble(){
		long result = readLong();
		return Double.longBitsToDouble(result);
	}

	/**
	 * Reconstructs a long from the following 8 bytes in the buffer.
	 * 
	 * @return The reconstructed long.
	 */
	private long readLong(){
		long result = 0;
		for(int i = 0; i < LONGBITS; i++) {
			result |= ((((long) currentBuffer.get()) & BYTEMASK) << (i * BYTEBITS));
		}
		return result;
	}
	
	public static ATerm readTermFromSAFFile(PureFactory pureFactory, File file) throws IOException{
		BinaryReader binaryReader = new BinaryReader(pureFactory);
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(65536);
		ByteBuffer sizeBuffer = ByteBuffer.allocate(2);
		
		FileInputStream fis = null;
		FileChannel fc = null;
		try{
			fis = new FileInputStream(file);
			fc = fis.getChannel();
			
			// Consume the SAF identification token.
			byteBuffer.limit(1);
			int bytesRead = fc.read(byteBuffer);
			if(bytesRead != 1) throw new IOException("Unable to read SAF identification token.\n");
			
			do{
				sizeBuffer.clear();
				bytesRead = fc.read(sizeBuffer);
				if(bytesRead <= 0) break;
				else if(bytesRead != 2) throw new IOException("Unable to read block size bytes from file: "+bytesRead+".\n");
				sizeBuffer.flip();
				
				int blockSize = (sizeBuffer.get() & 0x000000ff) + ((sizeBuffer.get() & 0x000000ff) << 8);

				byteBuffer.clear();
				byteBuffer.limit(blockSize);
				bytesRead = fc.read(byteBuffer);
				byteBuffer.flip();
				if(bytesRead != blockSize) throw new IOException("Unable to read bytes from file "+bytesRead+" vs "+blockSize+".");
				
				binaryReader.deserialize(byteBuffer);
			}while(bytesRead > 0);
			
			if(!binaryReader.isDone()) throw new IOException("Term incomplete, missing data.\n");
		}finally{
			if(fc != null){
				fc.close();
			}
			if(fis != null){
				fis.close();
			}
		}
		
		return binaryReader.getRoot();
	}
}

