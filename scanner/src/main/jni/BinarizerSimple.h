#ifndef __INCLUDE_BINARIZER_SIMPLE_H
#define __INCLUDE_BINARIZER_SIMPLE_H

void binarizerSimpleByteToIntArray(unsigned char* src, unsigned int* dst, unsigned int width, unsigned int height);
void binarizerSimpleByteArray(unsigned char* src, unsigned char* dst, unsigned int width, unsigned int height);

void binarizerGlobalHistogramByteToPackedIntArray(unsigned char* src, unsigned int* dst, unsigned int width, unsigned int height);
int estimateBlackPointExport(int* buckets, int numBuckets);

#endif