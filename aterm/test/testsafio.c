#include "aterm2.h"
#include "byteencoding.h"
#include "safio.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

static void testIntegerEncoding(){
	char *buffer = (char*) malloc(8 * sizeof(char));
	unsigned int count = 0;
	
	/* Test a lot of integers between -2^32 and 2^32-1 at PRIME intervals */
	int i = -2147483647;
	int interval = 37;
	int end = 2147483647 - interval;
	do{
		int bytesWritten = BEserializeMultiByteInt(i, buffer);
		int result = BEdeserializeMultiByteInt(buffer, &count);
		
		if(result != i){
			printf("Integer encoding or decoding is b0rked; result was: %d, expected: %d.\n", result, i);
			printf("Buffer content was: %hhx %hhx %hhx %hhx %hhx, bytesWritten was: %d\n", buffer[0], buffer[1], buffer[2], buffer[3], buffer[4], bytesWritten);
			return;
		}
	}while((i += interval) < end);
	printf("Integer encoding and decoding OK.\n");
}

static void testDoubleEncoding(){
	char *buffer = (char*) malloc(8 * sizeof(char));
	
	double d = -10000000;
	for(; d < 10000000; d++){
		BEserializeDouble(d, buffer);
		double result = BEdeserializeDouble(buffer);
		if(result != d){
			printf("Fixed point double encoding or decoding is b0rked; result was: %.64f, expected: %.64f.\n", result, d);
			return;
		}
	}
	printf("Fixed point double encoding and decoding OK.\n");
	
	int i = 1;
	d = 1;
	for(; i < 500000; i++){
		d += (((double) 1) / (1 << i));
		
		BEserializeDouble(d, buffer);
		double result = BEdeserializeDouble(buffer);
		if(result != d){
			printf("Floating point double encoding or decoding is b0rked; result was: %.64f, expected: %.64f.\n", result, d);
			return;
		}
	}
	
	printf("Floating point double encoding and decoding OK.\n");
}

static ATerm makeBigDummyTerm(int x){
	char *b = (char*) malloc(x * sizeof(char));
	
	int i = 2;
	for(; i < x - 1; i++){
		b[i] = 'x';
	}
	b[0] = 'a';
	b[1] = '(';
	b[x - 1] = ')';
	
	ATerm term = ATreadFromString(b);
	
	free(b);
	
	return term;
}

static void read(const char *input, ATerm expectedResult, int size){
	ByteBuffer buffer = ATcreateByteBuffer(size);
	memcpy(buffer->buffer, input, size);
	
	BinaryReader binaryReader = ATcreateBinaryReader();
	
	ATdeserialize(binaryReader, buffer);
	ATerm result = ATgetRoot(binaryReader);
	
	if(result != expectedResult){
		printf("The result didn't match the expected result.\n");
		printf("Expected: %s.\n", ATwriteToString(expectedResult));
		printf("But got: %s.\n", ATwriteToString(result));
		return;
	}
	
	printf("Reading OK for: %s\n", ATwriteToString(expectedResult));
}

static void write(ATerm input, const char *expectedResult, int size){
	ByteBuffer buffer = ATcreateByteBuffer(size + 10);
	BinaryWriter bw = ATcreateBinaryWriter(input);
	
	ATserialize(bw, buffer);
	char *result = (char*) malloc(buffer->limit * sizeof(char));
	memcpy(result, buffer->buffer, buffer->limit);
	
	int expectedResultLength = size;
	int resultLength = buffer->limit;
	if(expectedResultLength != resultLength){
		printf("The number of written bytes didn't match the number of expected bytes. Was: %d, expected: %d\n", resultLength, expectedResultLength);
		return;
	}
	int i = 0;
	for(; i < resultLength; i++){
		if(result[i] != expectedResult[i]){
			printf("Written data didn't match the expected data. Index: %d, was %d, expected %d\n", i, result[i], expectedResult[i]);
			return;
		}
	}
	
	free(result);
	
	printf("Writing OK for: %s\n", ATwriteToString(input));
}

static void testWriting(){
	/* A term */
	ATerm input = ATreadFromString("line(box(rect(2), square(4, 3)), circle(6))");
	char expectedResult1[] = {1, 2, 4, 108, 105, 110, 101, 1, 2, 3, 98, 111, 120, 1, 1, 4, 114, 101, 99, 116, 2, 2, 1, 2, 6, 115, 113, 117, 97, 114, 101, 2, 4, 2, 3, 1, 1, 6, 99, 105, 114, 99, 108, 101, 2, 6};
	write(input, expectedResult1, 46);
	
	/* A shared term */
	input = ATreadFromString("line(line(), line())");
	char expectedResult2[] = {1, 2, 4, 108, 105, 110, 101, 1, 0, 4, 108, 105, 110, 101, -128, 1};
	write(input, expectedResult2, 16);
	
	/* A term with signature sharing */
	input = ATreadFromString("line(line(0), line(1))");
	char expectedResult3[] = {1, 2, 4, 108, 105, 110, 101, 1, 1, 4, 108, 105, 110, 101, 2, 0, 65, 1, 2, 1};
	write(input, expectedResult3, 20);
	
	/* A term with annotations */
	input = ATreadFromString("line(10, 11{childAnno}){termAnno{annoOfAnno}}");
	char expectedResult4[] = {17, 2, 4, 108, 105, 110, 101, 2, 10, 18, 11, 4, 1, 1, 0, 9, 99, 104, 105, 108, 100, 65, 110, 110, 111, 4, 1, 17, 0, 8, 116, 101, 114, 109, 65, 110, 110, 111, 4, 1, 1, 0, 10, 97, 110, 110, 111, 79, 102, 65, 110, 110, 111};
	write(input, expectedResult4, 53);
	
	/* Signed integer */
	input = ATreadFromString("integer(-1)");
	char expectedResult5[] = {1, 1, 7, 105, 110, 116, 101, 103, 101, 114, 2, -1, -1, -1, -1, 15};
	write(input, expectedResult5, 16);
	
	/* Signed double */
	input = ATreadFromString("real(-1.0)");
	char expectedResult6[] = {1, 1, 4, 114, 101, 97, 108, 3, 0, 0, 0, 0, 0, 0, -16, -65};
	write(input, expectedResult6, 16);
	
	/* Int as annotation */
	input = ATreadFromString("line(0){1}");
	char expectedResult7[] = {17, 1, 4, 108, 105, 110, 101, 2, 0, 4, 1, 2, 1};
	write(input, expectedResult7, 13);
}

static void testReading(){
	/* A term */
	char input1[] = {1, 2, 4, 108, 105, 110, 101, 1, 2, 3, 98, 111, 120, 1, 1, 4, 114, 101, 99, 116, 2, 2, 1, 2, 6, 115, 113, 117, 97, 114, 101, 2, 4, 2, 3, 1, 1, 6, 99, 105, 114, 99, 108, 101, 2, 6};
	ATerm expectedResult = ATreadFromString("line(box(rect(2), square(4, 3)), circle(6))");
	read(input1, expectedResult, 46);
	
	/* A shared term */
	char input2[] = {1, 2, 4, 108, 105, 110, 101, 1, 0, 4, 108, 105, 110, 101, -128, 1};
	expectedResult = ATreadFromString("line(line(), line())");
	read(input2, expectedResult, 16);
	
	/* A term with signature sharing */
	char input3[] = {1, 2, 4, 108, 105, 110, 101, 1, 1, 4, 108, 105, 110, 101, 2, 0, 65, 1, 2, 1};
	expectedResult = ATreadFromString("line(line(0), line(1))");
	read(input3, expectedResult, 20);
	
	/* A term with annotations */
	char input4[] = {17, 2, 4, 108, 105, 110, 101, 2, 10, 18, 11, 4, 1, 1, 0, 9, 99, 104, 105, 108, 100, 65, 110, 110, 111, 4, 1, 17, 0, 8, 116, 101, 114, 109, 65, 110, 110, 111, 4, 1, 1, 0, 10, 97, 110, 110, 111, 79, 102, 65, 110, 110, 111};
	expectedResult = ATreadFromString("line(10, 11{childAnno}){termAnno{annoOfAnno}}");
	read(input4, expectedResult, 53);
	
	/* Signed integer */
	char input5[] = {1, 1, 7, 105, 110, 116, 101, 103, 101, 114, 2, -1, -1, -1, -1, 15};
	expectedResult = ATreadFromString("integer(-1)");
	read(input5, expectedResult, 16);
	
	/* Signed double */
	char input6[] = {1, 1, 4, 114, 101, 97, 108, 3, 0, 0, 0, 0, 0, 0, -16, -65};
	expectedResult = ATreadFromString("real(-1.0)");
	read(input6, expectedResult, 16);
	
	/* Int as annotation */
	char input7[] = {17, 1, 4, 108, 105, 110, 101, 2, 0, 4, 1, 2, 1};
	expectedResult = ATreadFromString("line(0){1}");
	read(input7, expectedResult, 13);
}

static void testChunkification(){
	ATerm in = makeBigDummyTerm(2500);
	ByteBuffer buffer = ATcreateByteBuffer(1000);
	BinaryWriter bw = ATcreateBinaryWriter(in);
	BinaryReader binaryReader = ATcreateBinaryReader();
	
	while(ATisFinishedReading(binaryReader) != 1){
		ATresetByteBuffer(buffer);
		ATserialize(bw, buffer);
		ATdeserialize(binaryReader, buffer);
	}
	
	ATerm result = ATgetRoot(binaryReader);
	
	if(result == in) printf("Chunkification OK\n");
	else printf("Chunkification FAILED\n");
}

static void testDeepNesting(){
	ATerm result;
	char *serialTerm;
	
	
	AFun f = ATmakeAFun("f",1,ATfalse);
	ATerm t = (ATerm) ATmakeAppl0(ATmakeAFun("g", 0, ATfalse));
	
	int i, s;
	for(i = 0; i < 256; i++){
		t = (ATerm) ATmakeAppl1(f, t);
	}
	
	serialTerm = ATwriteToSAFString((ATerm) t, &s);
	
	result = ATreadFromSAFString(serialTerm, s);
	
	if(result == t) printf("Deep nesting OK\n");
	else printf("Deep nesting FAILED\n");
}

int main(int argc, char **argv){
	ATerm bottomOfStack;
  	ATinit(argc, argv, &bottomOfStack);
  	
  	printf("\n");
  	
  	testIntegerEncoding();
  	printf("\n");
  	
  	testDoubleEncoding();
  	printf("\n");
  	
  	testWriting();
  	printf("\n");
  	
  	testReading();
  	printf("\n");
  	
  	testChunkification();
	printf("\n");
	
	testDeepNesting();
	printf("\n");
	
	return 0;
}
