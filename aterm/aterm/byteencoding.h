#ifndef BYTEENCODING_H_
#define BYTEENCODING_H_

int BEserializeMultiByteInt(int i, char *c);

void BEserializeDouble(double d, char *c);


int BEdeserializeMultiByteInt(char *c, unsigned int *i);

double BEdeserializeDouble(char *c);

#endif /*BYTEENCODING_H_*/
