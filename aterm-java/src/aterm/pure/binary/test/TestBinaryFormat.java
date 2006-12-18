package aterm.pure.binary.test;

import java.nio.ByteBuffer;

import jjtraveler.VisitFailure;

import aterm.ATerm;
import aterm.pure.PureFactory;
import aterm.pure.binary.BinaryReader;
import aterm.pure.binary.BinaryWriter;

public class TestBinaryFormat{
	private PureFactory pureFactory = null;
	
	public TestBinaryFormat(){
		super();
		
		setUp();
	}
	
	public void setUp(){
		pureFactory = new PureFactory();
	}
	
	public void testWriting() throws Exception{
		// A term
		ATerm input = pureFactory.parse("line(box(rect(2), square(4, 3)), circle(6))");
		byte[] expectedResult = new byte[]{3, 2, 4, 108, 105, 110, 101, 3, 2, 3, 98, 111, 120, 3, 1, 4, 114, 101, 99, 116, 1, 2, 3, 2, 6, 115, 113, 117, 97, 114, 101, 1, 4, 1, 3, 3, 1, 6, 99, 105, 114, 99, 108, 101, 1, 6};
		write(input, expectedResult);
		
		// A shared term
		input = pureFactory.parse("line(line(), line())");
		expectedResult = new byte[]{3, 2, 4, 108, 105, 110, 101, 3, 0, 4, 108, 105, 110, 101, -128, 1};
		write(input, expectedResult);
		
		// A term with signature sharing
		input = pureFactory.parse("line(line(0), line(1))");
		expectedResult = new byte[]{3, 2, 4, 108, 105, 110, 101, 3, 1, 4, 108, 105, 110, 101, 1, 0, 67, 1, 1, 1};
		write(input, expectedResult);
		
		// A term with annotations
		input = pureFactory.parse("line(10, 11{childAnno}){termAnno{annoOfAnno}}");
		expectedResult = new byte[]{19, 2, 4, 108, 105, 110, 101, 1, 10, 17, 11, 4, 1, 3, 0, 9, 99, 104, 105, 108, 100, 65, 110, 110, 111, 4, 1, 19, 0, 8, 116, 101, 114, 109, 65, 110, 110, 111, 4, 1, 3, 0, 10, 97, 110, 110, 111, 79, 102, 65, 110, 110, 111};
		write(input, expectedResult);
		
		// Signed integer
		input = pureFactory.parse("integer(-1)");
		expectedResult = new byte[]{3, 1, 7, 105, 110, 116, 101, 103, 101, 114, 1, -1, -1, -1, -1, 15};
		write(input, expectedResult);
		
		// Signed double
		input = pureFactory.parse("real(-1.0)");
		expectedResult = new byte[]{3, 1, 4, 114, 101, 97, 108, 2, 0, 0, 0, 0, 0, 0, -16, -65};
		write(input, expectedResult);
	}
	
	public void write(ATerm input, byte[] expectedResult) throws Exception{
		ByteBuffer buffer = ByteBuffer.allocate(expectedResult.length + 10);
		BinaryWriter bw = new BinaryWriter(input);
		bw.serialize(buffer);
		byte[] result = new byte[buffer.limit()];
		buffer.get(result);
		
		int expectedResultLength = expectedResult.length;
		int resultLength = result.length;
		if(expectedResultLength != resultLength){
			log("The number of written bytes didn't match the number of expected bytes. Was: "+resultLength+", expected: "+expectedResultLength);
			return;
		}
		for(int i = 0; i < resultLength; i++){
			if(result[i] != expectedResult[i]){
				log("Written data didn't match the expected data. Index: "+i+", was "+result[i]+", expected "+expectedResult[i]);
				return;
			}
		}
		
		log("Writing OK for: "+input);
	}
	
	public void testReading(){
		// A term
		byte[] input = new byte[]{3, 2, 4, 108, 105, 110, 101, 3, 2, 3, 98, 111, 120, 3, 1, 4, 114, 101, 99, 116, 1, 2, 3, 2, 6, 115, 113, 117, 97, 114, 101, 1, 4, 1, 3, 3, 1, 6, 99, 105, 114, 99, 108, 101, 1, 6};
		ATerm expectedResult = pureFactory.parse("line(box(rect(2), square(4, 3)), circle(6))");
		read(input, expectedResult);
		
		// A shared term
		input = new byte[]{3, 2, 4, 108, 105, 110, 101, 3, 0, 4, 108, 105, 110, 101, -128, 1};
		expectedResult = pureFactory.parse("line(line(), line())");
		read(input, expectedResult);
		
		// A term with signature sharing
		input = new byte[]{3, 2, 4, 108, 105, 110, 101, 3, 1, 4, 108, 105, 110, 101, 1, 0, 67, 1, 1, 1};
		expectedResult = pureFactory.parse("line(line(0), line(1))");
		read(input, expectedResult);
		
		// A term with annotations
		input = new byte[]{19, 2, 4, 108, 105, 110, 101, 1, 10, 17, 11, 4, 1, 3, 0, 9, 99, 104, 105, 108, 100, 65, 110, 110, 111, 4, 1, 19, 0, 8, 116, 101, 114, 109, 65, 110, 110, 111, 4, 1, 3, 0, 10, 97, 110, 110, 111, 79, 102, 65, 110, 110, 111};
		expectedResult = pureFactory.parse("line(10, 11{childAnno}){termAnno{annoOfAnno}}");
		read(input, expectedResult);
		
		// Signed integer
		input = new byte[]{3, 1, 7, 105, 110, 116, 101, 103, 101, 114, 1, -1, -1, -1, -1, 15};
		expectedResult = pureFactory.parse("integer(-1)");
		read(input, expectedResult);
		
		// Signed double
		input = new byte[]{3, 1, 4, 114, 101, 97, 108, 2, 0, 0, 0, 0, 0, 0, -16, -65};
		expectedResult = pureFactory.parse("real(-1.0)");
		read(input, expectedResult);
	}
	
	public void read(byte[] input, ATerm expectedResult){
		ByteBuffer buffer = ByteBuffer.allocate(input.length);
		buffer.put(input);
		buffer.flip();
		
		BinaryReader binaryReader = new BinaryReader(pureFactory);
		binaryReader.deserialize(buffer);
		ATerm result = binaryReader.getRoot();
		
		if(result != expectedResult){
			log("The result didn't match the expected result.");
			return;
		}
		
		log("Reading OK for: "+expectedResult);
	}
	
	public void testChunkification() throws VisitFailure{
		ATerm in = makeBigDummyTerm(2500);
		ByteBuffer buffer = ByteBuffer.allocate(1000);
		BinaryWriter bw = new BinaryWriter(in);
		BinaryReader binaryReader = new BinaryReader(pureFactory);
		
		while(!binaryReader.isDone()){
			buffer.clear();
			bw.serialize(buffer);
			binaryReader.deserialize(buffer);
		}
		
		ATerm result = binaryReader.getRoot();
		
		if(result == in) log("Chunkification OK");
		else log("Chunkification FAILED");
	}
	
	private ATerm makeBigDummyTerm(int x){
		byte[] b = new byte[x];
		
		for(int i = 2; i < b.length - 1; i++){
			b[i] = 'x';
		}
		b[0] = 'a';
		b[1] = '(';
		b[b.length - 1] = ')';
		
		String s = new String(b);
		
		return pureFactory.parse(s);
	}
	
	private static void log(String message){
		System.out.println(message);
	}
	
	public static void main(String[] args) throws Exception{
		TestBinaryFormat tbf = new TestBinaryFormat();
		tbf.testWriting();
		log("");
		tbf.testReading();
		log("");
		tbf.testChunkification();
	}
}
