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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import jjtraveler.VisitFailure;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermBlob;
import aterm.ATermFwdVoid;
import aterm.ATermInt;
import aterm.ATermList;
import aterm.ATermLong;
import aterm.ATermPlaceholder;
import aterm.ATermReal;

/**
 * Writes the given ATerm to a (streamable) binary format. Supply the constructor of this class with
 * a ATerm and keep calling the serialize method until the finished() method returns true.<br />
 * <br />
 * For example (yes I know this code is crappy, but it's simple):<blockquote><pre>
 * ByteBuffer buffer = ByteBuffer.allocate(8192);
 BinaryWriter bw = new BinaryWriter(aterm);
 while(!bw.isFinished()) {
   buffer.clear();
   bw.serialize(buffer);
   while(buffer.hasRemaining()) channel.write(buffer); // Write the chunk of data to a channel
 }
 * </pre></blockquote>
 * 
 * @author Arnold Lankamp
 */
public class BinaryWriter extends ATermFwdVoid{
	private final static int ISSHAREDFLAG = 0x00000080;
	private final static int ANNOSFLAG = 0x00000010;

	private final static int ISFUNSHARED = 0x00000040;
	private final static int APPLQUOTED = 0x00000020;

	private final static int STACKSIZE = 256;

	private final static int MINIMUMFREESPACE = 10;

	private final Map<ATerm, Integer> sharedTerms;
	private int currentKey;
	private final Map<AFun, Integer> applSignatures;
	private int sigKey;

	private ATermMapping[] stack;
	private int stackPosition;
	private ATerm currentTerm;
	private int indexInTerm;
	private byte[] tempNameWriteBuffer;

	private ByteBuffer currentBuffer;

	/**
	 * Constructor.
	 * 
	 * @param root
	 *            The ATerm that needs to be serialized.
	 */
	public BinaryWriter(ATerm root){
		super();

		sharedTerms = new HashMap<ATerm, Integer>();
		currentKey = 0;
		applSignatures = new HashMap<AFun, Integer>();
		sigKey = 0;

		stack = new ATermMapping[STACKSIZE];
		stackPosition = 0;

		ATermMapping tm = new ATermMapping();
		tm.term = root;

		stack[stackPosition] = tm;
		currentTerm = root;
		
		indexInTerm = 0;
		tempNameWriteBuffer = null;
	}

	/**
	 * Serializes the term from the position where it left of the last time this method was called.
	 * Note that the buffer will be flipped before returned.
	 * 
	 * @param buffer
	 *            The buffer that will be filled with data.
	 * @throws VisitFailure
	 *             Never thrown.
	 */
	public void serialize(ByteBuffer buffer) throws VisitFailure{
		currentBuffer = buffer;

		while(currentTerm != null){
			if(buffer.remaining() < MINIMUMFREESPACE) break;

			Integer id = sharedTerms.get(currentTerm);
			if(id != null){
				buffer.put((byte) ISSHAREDFLAG);
				writeInt(id.intValue());

				stackPosition--; // Pop the term from the stack, since it's subtree is shared.
			}else{
				visit(currentTerm);
				
				if(currentTerm.getType() == ATerm.LIST) stack[stackPosition].nextPartOfList = (ATermList) currentTerm; // <- for ATermList->next optimizaton.

				// Don't add the term to the shared list until we are completely done with it.
				if(indexInTerm == 0) sharedTerms.put(currentTerm, new Integer(currentKey++));
				else break;
			}

			currentTerm = getNextTerm();
		}

		buffer.flip();
	}

	/**
	 * Checks if we are done serializing.
	 * 
	 * @return true when we are done serializing; false otherwise.
	 */
	public boolean isFinished(){
		return (currentTerm == null);
	}

	/**
	 * Finds the next term we are going to serialize, based on the current state of the stack.
	 * 
	 * @return The next term we are going to serialize.
	 */
	private ATerm getNextTerm(){
		ATerm next = null;
		
		// Make sure the stack remains large enough
		ensureStackCapacity();

		while(next == null && stackPosition > -1){
			ATermMapping current = stack[stackPosition];
			ATerm term = current.term;

			if(term.getChildCount() > current.subTermIndex + 1){
				if(term.getType() != ATerm.LIST){
					next = (ATerm) term.getChildAt(++current.subTermIndex);
				}else{
					ATermList nextList = current.nextPartOfList;
					next = nextList.getFirst();
					current.nextPartOfList = nextList.getNext();
					
					current.subTermIndex++;
				}
				
				ATermMapping child = new ATermMapping();
				child.term = next;
				stack[++stackPosition] = child;
			}else if(!current.annosDone && term.hasAnnotations()){
				next = term.getAnnotations();

				ATermMapping annos = new ATermMapping();
				annos.term = next;
				stack[++stackPosition] = annos;

				current.annosDone = true;
			}else{
				stackPosition--;
			}
		}
		
		return next;
	}
	
	/**
	 * Resizes the stack when needed. When we're running low on stack space the capacity will be
	 * doubled.
	 */
	private void ensureStackCapacity(){
		int stackSize = stack.length;
		if(stackPosition + 1 == stackSize){
			ATermMapping[] newStack = new ATermMapping[(stackSize << 1)];
			System.arraycopy(stack, 0, newStack, 0, stack.length);
			stack = newStack;
		}
	}

	/**
	 * Returns a header for the given term.
	 * 
	 * @param term
	 *            The term we are requesting a header for.
	 * @return The constructed header.
	 */
	private byte getHeader(ATerm term){
		byte header = (byte) term.getType();
		if(term.hasAnnotations()) header = (byte) (header | ANNOSFLAG);

		return header;
	}

	/**
	 * Structure that holds information about the state of the contained term.
	 * 
	 * @author Arnold Lankamp
	 */
	private static class ATermMapping{
		public ATerm term;
		public int subTermIndex = -1;
		public boolean annosDone = false;
		
		public ATermList nextPartOfList = null; // This is for a ATermList 'nextTerm' optimalization only.
	}

	/**
	 * Serializes the given appl. The function name of the appl can be serialized in chunks.
	 * 
	 * @see aterm.ATermFwdVoid#voidVisitAppl(ATermAppl)
	 * @throws VisitFailure
	 *             Never thrown.
	 */
	public void voidVisitAppl(ATermAppl arg) throws VisitFailure{
		if(indexInTerm == 0){
			byte header = getHeader(arg);

			AFun fun = arg.getAFun();
			Integer key = applSignatures.get(fun);
			if(key == null){
				if(arg.isQuoted()) header = (byte) (header | APPLQUOTED);
				currentBuffer.put(header);

				writeInt(arg.getArity());
				
				String name = fun.getName();
				byte[] nameBytes = name.getBytes();
				int length = nameBytes.length;
				writeInt(length);

				int endIndex = length;
				int remaining = currentBuffer.remaining();
				if(remaining < endIndex) endIndex = remaining;
				
				currentBuffer.put(nameBytes, 0, endIndex);

				if(endIndex != length){
					indexInTerm = endIndex;
					tempNameWriteBuffer = nameBytes;
				}

				applSignatures.put(fun, new Integer(sigKey++));
			}else{
				header = (byte) (header | ISFUNSHARED);
				currentBuffer.put(header);

				writeInt(key.intValue());
			}
		}else{
			int length = tempNameWriteBuffer.length;
			
			int endIndex = length;
			int remaining = currentBuffer.remaining();
			if((indexInTerm + remaining) < endIndex) endIndex = (indexInTerm + remaining);
			
			currentBuffer.put(tempNameWriteBuffer, indexInTerm, (endIndex - indexInTerm));
			indexInTerm = endIndex;

			if(indexInTerm == length){
				indexInTerm = 0;
				tempNameWriteBuffer = null;
			}
		}
	}

	/**
	 * Serializes the given blob. A blob can be serialized in chunks.
	 * 
	 * @see aterm.ATermFwdVoid#voidVisitBlob(ATermBlob)
	 * @throws VisitFailure
	 *             Never thrown.
	 */
	public void voidVisitBlob(ATermBlob arg) throws VisitFailure{
		int size = arg.getBlobSize();
		if(indexInTerm == 0) {
			currentBuffer.put(getHeader(arg));

			writeInt(size);
		}

		byte[] blobBytes = arg.getBlobData();

		int bytesToWrite = size - indexInTerm;
		int remaining = currentBuffer.remaining();
		if(remaining < bytesToWrite){
			bytesToWrite = remaining;
		}

		currentBuffer.put(blobBytes, indexInTerm, bytesToWrite);
		indexInTerm += bytesToWrite;

		if(indexInTerm == size) indexInTerm = 0;
	}

	/**
	 * Serializes the given int. Ints will always be serialized in one piece.
	 * 
	 * @see aterm.ATermFwdVoid#voidVisitInt(ATermInt)
	 * @throws VisitFailure
	 *             Never thrown.
	 */
	public void voidVisitInt(ATermInt arg) throws VisitFailure{
		currentBuffer.put(getHeader(arg));

		writeInt(arg.getInt());
	}

	/**
	 * Serializes the given long.
	 * 
	 * @see aterm.ATermFwdVoid#voidVisitLong(ATermLong)
	 * @throws VisitFailure
	 *             Never thrown.
	 */
	public void voidVisitLong(ATermLong arg) throws VisitFailure{
		currentBuffer.put(getHeader(arg));

		writeLong(arg.getLong());
	}

	/**
	 * Serializes the given list. List information will always be serialized in one piece.
	 * 
	 * @see aterm.ATermFwdVoid#voidVisitList(ATermList)
	 * @throws VisitFailure
	 *             Never thrown.
	 */
	public void voidVisitList(ATermList arg) throws VisitFailure{
		byte header = getHeader(arg);
		currentBuffer.put(header);

		writeInt(arg.getLength());
	}

	/**
	 * Serializes the given placeholder. Placeholders will always be serialized in one piece.
	 * 
	 * @see aterm.ATermFwdVoid#voidVisitPlaceholder(ATermPlaceholder)
	 * @throws VisitFailure
	 *             Never thrown.
	 */
	public void voidVisitPlaceholder(ATermPlaceholder arg) throws VisitFailure{
		currentBuffer.put(getHeader(arg));

		// Do nothing, serializing its header is enough.
	}

	/**
	 * Serializes the given real. Reals will always be serialized in one peice.
	 * 
	 * @see aterm.ATermFwdVoid#voidVisitReal(ATermReal)
	 * @throws VisitFailure
	 *             Never thrown.
	 */
	public void voidVisitReal(ATermReal arg) throws VisitFailure{
		currentBuffer.put(getHeader(arg));

		writeDouble(arg.getReal());
	}

	private final static int SEVENBITS = 0x0000007f;
	private final static int SIGNBIT = 0x00000080;
	private final static int LONGBITS = 8;

	/**
	 * Splits the given integer in separate bytes and writes it to the buffer. It will occupy the
	 * smallest amount of bytes possible. This is done in the following way: the sign bit will be
	 * used to indicate that more bytes coming, if this is set to 0 we know we are done. Since we
	 * are mostly writing small values, this will save a considerable amount of space. On the other
	 * hand a large number will occupy 5 bytes instead of the regular 4.
	 * 
	 * @param value
	 *            The integer that needs to be split and written.
	 */
	private void writeInt(int value){
		int intValue = value;
		
		if((intValue & 0xffffff80) == 0){
			currentBuffer.put((byte) (intValue & SEVENBITS));
			return;
		}
		currentBuffer.put((byte) ((intValue & SEVENBITS) | SIGNBIT));
		
		if((intValue & 0xffffc000) == 0){
			currentBuffer.put((byte) ((intValue >>> 7) & SEVENBITS));
			return;
		}
		currentBuffer.put((byte) (((intValue >>> 7) & SEVENBITS) | SIGNBIT));
		
		if((intValue & 0xffe00000) == 0){
			currentBuffer.put((byte) ((intValue >>> 14) & SEVENBITS));
			return;
		}
		currentBuffer.put((byte) (((intValue >>> 14) & SEVENBITS) | SIGNBIT));
		
		if((intValue & 0xf0000000) == 0){
			currentBuffer.put((byte) ((intValue >>> 21) & SEVENBITS));
			return;
		}
		currentBuffer.put((byte) (((intValue >>> 21) & SEVENBITS) | SIGNBIT));
		
		currentBuffer.put((byte) ((intValue >>> 28) & SEVENBITS));
	}

	/**
	 * Splits the given double in separate bytes and writes it to the buffer. Doubles will always
	 * occupy 8 bytes, since the convertion of a floating point number to a long will always cause
	 * the high order bits to be occupied.
	 * 
	 * @param value
	 *            The integer that needs to be split and written.
	 */
	private void writeDouble(double value){
		long longValue = Double.doubleToLongBits(value);
		writeLong(longValue);
	}

	private void writeLong(long value){
		for(int i = 0; i < LONGBITS; i++) {
			currentBuffer.put((byte) (value >>> (i * 8)));
		}
	}
	
	/**
	 * Writes the given aterm to the given file. Blocks of 65536 bytes will be used.
	 * 
	 * @param aTerm
	 *             The ATerm that needs to be writen to file.
	 * @param file 
	 *             The file to write to.
	 * @throws IOException
	 *             Thrown when an error occurs while attempting to write to the given file.
	 * @throws VisitFailure
	 *             This never happens
	 */
	public static void writeTermToSAFFile(ATerm aTerm, File file) throws IOException, VisitFailure{
		BinaryWriter binaryWriter = new BinaryWriter(aTerm);
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(65536);
		ByteBuffer sizeBuffer = ByteBuffer.allocate(2);
		
		FileOutputStream fos = null;
		FileChannel fc = null;
		try{
			fos = new FileOutputStream(file);
			fc = fos.getChannel();
			
			byteBuffer.put((byte) '?');
			byteBuffer.flip();
			fc.write(byteBuffer);
			
			do{
				byteBuffer.clear();
				binaryWriter.serialize(byteBuffer);
				
				int blockSize = byteBuffer.limit();
				sizeBuffer.clear();
				sizeBuffer.put((byte) (blockSize & 0x000000ff));
				sizeBuffer.put((byte) ((blockSize >>> 8) & 0x000000ff));
				sizeBuffer.flip();
				
				fc.write(sizeBuffer);
				
				fc.write(byteBuffer);
			}while(!binaryWriter.isFinished());
		}finally{
			if(fc != null){
				fc.close();
			}
			if(fos != null){
				fos.close();
			}
		}
	}
}
