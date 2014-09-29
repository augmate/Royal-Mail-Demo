#ifndef __INCLUDE_NATIVE_ZXING_WRAPPER_H
#define __INCLUDE_NATIVE_ZXING_WRAPPER_H

int zxingNativeDecode(unsigned char* src, unsigned int width, unsigned int height, char* resultBuffer);

#endif