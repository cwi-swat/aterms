package aterm.pure.binary;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;
import aterm.pure.ATermApplImpl;
import aterm.pure.PureFactory;
import aterm.pure.binary.BinaryWriter.ApplSignature;

/**
 * Reconstructs an ATerm from the given (series of) buffer(s). It can be retrieved when the
 * construction of the term is done / when isDone() returns true.<br />
 * <br />
 * For example (yes I know this code is crappy, but it's simple):<blockquote><pre>
 * ByteBuffer buffer = ByteBuffer.allocate(8192);
 BinaryWriter bw = new BinaryWriter(aterm);
 while(!bw.isDone()){
   int bytesRead = channel.read(buffer); // Read the next chunk of data from the stream.
   if(!buffer.hasRemaining() || bytesRead == -1){
     bw.serialize(buffer);
     buffer.clear();
   }
 }
 * </pre></blockquote>
 * 
 * @author Arnold Lankamp
 */
public class BinaryReader{
	private final static byte ISSHAREDFLAG = -128;
	private final static byte TYPEMASK = 15;
	private final static byte ANNOSFLAG = 16;

	private final static byte APPLSIGNATURESHARED = 64;
	private final static byte APPLQUOTED = 32;

	private final static int STACKSIZE = 256;

	private PureFactory factory = null;

	private List<ATermConstruct> sharedTerms = null;
	private List<ApplSignature> applSignatures = null;

	private ATermConstruct[] stack = null;
	private int stackPosition = -1;
	
	private int tempType = -1;
	private byte[] tempBytes = null;
	private int tempBytesIndex = 0;

	private ByteBuffer currentBuffer = null;

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

		sharedTerms = new ArrayList<ATermConstruct>();
		applSignatures = new ArrayList<ApplSignature>();

		stack = new ATermConstruct[STACKSIZE];
		stackPosition = -1;
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
				ATermConstruct ac = sharedTerms.get(readInt());
				stack[++stackPosition] = ac;
				ATerm term = ac.term;

				linkTerm(term);
			}else{
				int type = (header & TYPEMASK);

				ATermConstruct ac = new ATermConstruct();
				ac.type = type;
				ac.hasAnnos = ((header & ANNOSFLAG) == ANNOSFLAG);

				sharedTerms.add(ac);

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
		if(stackPosition + 1 == stackSize){
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

		return sharedTerms.get(0).term;
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
				ATermConstruct ac = stack[stackPosition];
				AFun fun = ((ATermAppl) ac.term).getAFun();
				
				int arity = fun.getArity();
				boolean isQuoted = fun.isQuoted();
				String name = new String(tempBytes);
				
				ATerm term = factory.makeAppl(factory.makeAFun(name, arity, isQuoted));
				ac.term = term;
				
				ApplSignature as = new ApplSignature();
				as.arity = arity;
				as.isQuoted = isQuoted;
				as.name = name;

				applSignatures.add(as);
				
				if(arity == 0 && !ac.hasAnnos) linkTerm(term);
			}else if(tempType == ATerm.BLOB){
				ATermConstruct ac = stack[stackPosition];
				ATerm term = factory.makeBlob(tempBytes);
				ac.term = term;

				if(!ac.hasAnnos) linkTerm(term);
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
		int arity = 0;
		boolean isQuoted = false;
		String name = "";
		if((header & APPLSIGNATURESHARED) == APPLSIGNATURESHARED){
			int key = readInt();

			ApplSignature as = applSignatures.get(key);
			arity = as.arity;
			isQuoted = as.isQuoted;
			name = as.name;
			
			ATermConstruct ac = stack[stackPosition];
			ac.subTerms = new ATerm[arity];
			ATerm term = factory.makeAppl(factory.makeAFun(name, arity, isQuoted));
			ac.term = term;
			
			if(arity == 0 && !ac.hasAnnos){
				linkTerm(term);
			}
		}else{
			isQuoted = ((header & APPLQUOTED) == APPLQUOTED);
			arity = readInt();
			int nameLength = readInt();
			
			tempType = ATerm.APPL;
			tempBytes = new byte[nameLength];
			tempBytesIndex = 0;
			
			ATermConstruct ac = stack[stackPosition];
			ac.subTerms = new ATerm[arity];
			ATerm term = factory.makeAppl(factory.makeAFun(name, arity, isQuoted));
			ac.term = term;
			
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
			ac.term = term;

			if(!ac.hasAnnos) linkTerm(term);
		}
	}

	/**
	 * Deserialializes an int.
	 */
	private void touchInt(){
		int value = readInt();

		ATermConstruct ac = stack[stackPosition];
		ATerm term = factory.makeInt(value);
		ac.term = term;

		if(!ac.hasAnnos) linkTerm(term);
	}

	/**
	 * Deserialializes a real.
	 */
	private void touchReal(){
		double value = readDouble();

		ATermConstruct ac = stack[stackPosition];
		ATerm term = factory.makeReal(value);
		ac.term = term;

		if(!ac.hasAnnos) linkTerm(term);
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
	 * Links the given term with it's parent.
	 * 
	 * @param term
	 *            The term that needs to be linked.
	 */
	private void linkTerm(ATerm term){
		if(stackPosition == 0){
			isDone = true;
			return;
		}

		ATermConstruct ac = stack[--stackPosition];

		boolean fullyConstructed = false;

		ATerm[] subTerms = ac.subTerms;
		boolean hasAnnos = ac.hasAnnos;
		if(subTerms != null && subTerms.length > ac.subTermIndex){
			subTerms[ac.subTermIndex++] = term;

			if(subTerms.length == ac.subTermIndex && !hasAnnos){
				ac.annos = factory.makeList();

				fullyConstructed = true;
			}
		}else if(hasAnnos && (term instanceof ATermList)){
			ac.annos = (ATermList) term;
			ac.term = ac.term.setAnnotations(ac.annos);

			fullyConstructed = true;
		}else{
			throw new RuntimeException("Encountered a term that didn't fit anywhere. Type: " + term.getType());
		}

		if(fullyConstructed){
			int type = ac.type;
			if(type == ATerm.APPL){
				ATermApplImpl appl = (ATermApplImpl) ac.term;
				ac.term = factory.makeAppl(appl.getAFun(), subTerms, ac.annos);
			}else if(type == ATerm.LIST){
				ATermList list = factory.makeList();
				for(int i = subTerms.length -1; i >= 0; i--){
					list = factory.makeList(subTerms[i], list);
				}

				if(hasAnnos) list = (ATermList) list.setAnnotations(ac.annos);

				ac.term = list;
			}else if(type == ATerm.PLACEHOLDER){
				ATerm placeholder = factory.makePlaceholder(subTerms[0]);

				ac.term = placeholder;
			}
			// Don't need to do anything special for the other terms.

			linkTerm(ac.term);
		}
	}

	/**
	 * A structure that contains all information we need for reconstructing a term.
	 * 
	 * @author Arnold Lankamp
	 */
	private static class ATermConstruct{
		public int type;

		public ATerm term = null;

		public int subTermIndex = 0;
		public ATerm[] subTerms = null;

		public boolean hasAnnos;
		public ATermList annos = null;
	}

	private final static byte SEVENBITS = 127;
	private final static int SIGNBYTE = -128;
	private final static int BYTEMASK = 0xFF;
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
		int result = 0;

		boolean signed = false;
		int i = 0;
		do{
			byte part = currentBuffer.get();

			result |= ((part & SEVENBITS) << i);

			signed = ((part & SIGNBYTE) == SIGNBYTE);

			i += 7;
		}while(signed);

		return result;
	}

	/**
	 * Reconstructs a double from the following 8 bytes in the buffer.
	 * 
	 * @return The reconstructed double.
	 */
	private double readDouble(){
		long result = 0;
		for(int i = 0; i < LONGBITS; i++){
			result |= ((((long) currentBuffer.get()) & BYTEMASK) << (i * BYTEBITS));
		}

		return Double.longBitsToDouble(result);
	}
}
