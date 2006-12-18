package aterm.pure.binary;

import java.nio.ByteBuffer;
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
import aterm.ATermPlaceholder;
import aterm.ATermReal;

/**
 * Writes the given ATerm to a (streamable) binary format. Supply the constructor of this class with
 * a ATerm and keep calling the serialize method until the finished() method returns true.<br />
 * <br />
 * For example (yes I know this code is crappy, but it's simple):<blockquote><pre>
 * ByteBuffer buffer = ByteBuffer.allocate(8192);
 BinaryWriter bw = new BinaryWriter(aterm);
 while(!bw.isFinished()){
   buffer.clear();
   bw.serialize(buffer);
   while(buffer.hasRemaining()) channel.write(buffer); // Write the chunk of data to a channel
 }
 * </pre></blockquote>
 * 
 * @author Arnold Lankamp
 */
public class BinaryWriter extends ATermFwdVoid{
	private final static byte ISSHAREDFLAG = -128;
	private final static byte ANNOSFLAG = 16;

	private final static byte APPLSIGNATURESHARED = 64;
	private final static byte APPLQUOTED = 32;

	private final static int STACKSIZE = 256;

	private final static int MINIMUMFREESPACE = 10;

	private Map<ATerm, Integer> sharedTerms = null;
	private int currentKey = 0;
	private Map<ApplSignature, Integer> applSignatures = null;
	private int sigKey = 0;

	private ATermMapping[] stack = null;
	private int stackPosition = 0;
	private ATerm currentTerm = null;
	private int indexInTerm = 0;

	private ByteBuffer currentBuffer = null;

	/**
	 * Constructor.
	 * 
	 * @param root
	 *            The ATerm that needs to be serialized.
	 */
	public BinaryWriter(ATerm root){
		super();

		sharedTerms = new HashMap<ATerm, Integer>();

		applSignatures = new HashMap<ApplSignature, Integer>();

		stack = new ATermMapping[STACKSIZE];
		stackPosition = 0;

		ATermMapping tm = new ATermMapping();
		tm.term = root;

		stack[stackPosition] = tm;
		currentTerm = root;
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
				buffer.put(ISSHAREDFLAG);
				writeInt(id.intValue());

				stackPosition--; // Pop the term from the stack, since it's subtree is shared.
			}else{
				visit(currentTerm);

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

		while(next == null && stackPosition > -1){
			ATermMapping current = stack[stackPosition];
			ATerm term = current.term;

			if(term.getChildCount() > current.subTerm + 1){
				next = (ATerm) term.getChildAt(++current.subTerm);

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
		
		// Make sure the stack remains large enough
		ensureStackCapacity();
		
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
		public int subTerm = -1;
		public boolean annosDone = false;
	}

	/**
	 * Class that contains the signature of an appl. This signature consists of the name, the arity
	 * and the 'quoted' flag.
	 * 
	 * @author Arnold Lankamp
	 */
	protected static class ApplSignature{
		public String name;
		public int arity;
		public boolean isQuoted;

		/**
		 * Custom hash function. Needed for matching in a hashmap.
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode(){
			return ((name.length() > 0 ? (name.charAt(0) << 8) : 0) | ((name.length() & 15) << 4) | (arity & 15));
		}

		/**
		 * Checks for equality of this object against the given object.
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj){
			boolean equal = false;
			if(obj instanceof ApplSignature){
				ApplSignature as = (ApplSignature) obj;
				if(as.arity == arity && as.isQuoted == isQuoted && as.name.equals(name)) equal = true;
			}
			return equal;
		}
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
			String name = fun.getName();
			int arity = arg.getArity();
			boolean isQuoted = arg.isQuoted();

			ApplSignature as = new ApplSignature();
			as.name = name;
			as.arity = arity;
			as.isQuoted = isQuoted;

			Integer key = applSignatures.get(as);
			if(key == null){
				if(isQuoted) header = (byte) (header | APPLQUOTED);
				currentBuffer.put(header);

				writeInt(arity);

				int length = name.length();
				writeInt(length);

				int endIndex = length;
				int remaining = currentBuffer.remaining();
				if(remaining < endIndex) endIndex = remaining;

				byte[] nameBytes = name.substring(0, endIndex).getBytes();
				currentBuffer.put(nameBytes);

				if(endIndex != length) indexInTerm = endIndex;

				applSignatures.put(as, new Integer(sigKey++));
			}else{
				header = (byte) (header | APPLSIGNATURESHARED);
				currentBuffer.put(header);

				writeInt(key.intValue());
			}
		}else{
			AFun fun = arg.getAFun();
			String name = fun.getName();
			int length = name.length();

			int endIndex = length;
			int remaining = currentBuffer.remaining();
			if((indexInTerm + remaining) < endIndex) endIndex = (indexInTerm + remaining);

			byte[] nameBytes = name.substring(indexInTerm, endIndex).getBytes();
			currentBuffer.put(nameBytes);
			indexInTerm = endIndex;

			if(indexInTerm == length) indexInTerm = 0;
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
		if(indexInTerm == 0){
			currentBuffer.put(getHeader(arg));

			writeInt(size);
		}

		byte[] blobBytes = arg.getBlobData();

		int bytesToWrite = size;
		int remaining = currentBuffer.remaining();
		if((indexInTerm + remaining) < size) bytesToWrite = remaining;

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

	private final static byte SEVENBITS = 127;
	private final static int SIGNBYTE = -128;
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
		do{
			byte x = (byte) (intValue & SEVENBITS);
			intValue >>>= 7;

			if(intValue != 0) x |= SIGNBYTE;

			currentBuffer.put(x);
		}while(intValue != 0);
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
		for(int i = 0; i < LONGBITS; i++){
			currentBuffer.put((byte) (longValue >>> (i * 8)));
		}
	}
}
