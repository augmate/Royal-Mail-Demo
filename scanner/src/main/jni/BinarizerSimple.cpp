#include <stdio.h>
#include <string.h>
#include "Helpers.h"

// configuration for black-point estimation
const int LUMINANCE_BITS = 5;
const int LUMINANCE_SHIFT = 8 - LUMINANCE_BITS;
const int LUMINANCE_BUCKETS = 1 << LUMINANCE_BITS;

int estimateBlackPoint(int* buckets, int numBuckets);
int* globalHistogramBuckets = new int[LUMINANCE_BUCKETS];

// native C rewrite of the GlobalHistogramBinarizer.java from ZXing
void binarizerGlobalHistogramByteToPackedIntArrayImpl(unsigned char* luminances, unsigned int* dst, unsigned int width, unsigned int height) {
    int packedRowSize = (width + 31) >> 5; // should be same as ceil(width / 32)

    for (int x = 0; x < LUMINANCE_BUCKETS; x++)
          globalHistogramBuckets[x] = 0;

    // histogram buckets array is a 4x5 downsampled version of the image

    // load 4 scanlines (evenly spaced out throughout the image)
    for (int y = 1; y < 5; y++) {
        int row = height * y / 5;
        unsigned char* localLuminances = &luminances[row * width];
        int right = (width << 2) / 5;
        // grab every 5th pixel in the row
        for (int x = width / 5; x < right; x++) {
            int pixel = localLuminances[x] & 0xff;
            globalHistogramBuckets[pixel >> LUMINANCE_SHIFT]++;
        }
    }

    // calculate blackpoint
    int blackPoint = estimateBlackPoint(globalHistogramBuckets, LUMINANCE_BUCKETS);

    if(blackPoint == -1) {
        return;
    }

    //memset(dst, 0, 4 * height * packedRowSize);

    for(int y = 0; y < height; y++)
    {
        int offsetSrc = y * width;
        int offsetDst = y * packedRowSize;
        for(int x = 0; x < width; x ++)
        {
            int value = (luminances[offsetSrc+x] & 0xFF);

            if(value < blackPoint)
                dst[offsetDst + (x >> 5)] |= 1 << (x & 0x1f);
        }
    }
}


int estimateBlackPoint(int* buckets, int numBuckets) {
    // Find the tallest peak in the histogram.
    int maxBucketCount = 0;
    int firstPeak = 0;
    int firstPeakSize = 0;
    for (int x = 0; x < numBuckets; x++) {
      if (buckets[x] > firstPeakSize) {
        firstPeak = x;
        firstPeakSize = buckets[x];
      }
      if (buckets[x] > maxBucketCount) {
        maxBucketCount = buckets[x];
      }
    }

    // Find the second-tallest peak which is somewhat far from the tallest peak.
    int secondPeak = 0;
    int secondPeakScore = 0;
    for (int x = 0; x < numBuckets; x++) {
      int distanceToBiggest = x - firstPeak;
      // Encourage more distant second peaks by multiplying by square of distance.
      int score = buckets[x] * distanceToBiggest * distanceToBiggest;
      if (score > secondPeakScore) {
        secondPeak = x;
        secondPeakScore = score;
      }
    }

    // Make sure firstPeak corresponds to the black peak.
    if (firstPeak > secondPeak) {
      int temp = firstPeak;
      firstPeak = secondPeak;
      secondPeak = temp;
    }

    // If there is too little contrast in the image to pick a meaningful black point, throw rather
    // than waste time trying to decode the image, and risk false positives.
    if (secondPeak - firstPeak <= numBuckets >> 4) {
      return -1;
    }

    // Find a valley between them that is low and closer to the white peak.
    int bestValley = secondPeak - 1;
    int bestValleyScore = -1;
    for (int x = secondPeak - 1; x > firstPeak; x--) {
      int fromFirst = x - firstPeak;
      int score = fromFirst * fromFirst * (secondPeak - x) * (maxBucketCount - buckets[x]);
      if (score > bestValleyScore) {
        bestValley = x;
        bestValleyScore = score;
      }
    }

    return bestValley << LUMINANCE_SHIFT;
  }

  extern "C" {

      // manipulates an integer array
      void binarizerSimpleByteToIntArray(unsigned char* src, unsigned int* dst, unsigned int width, unsigned int height) {
          int i;
          for(i = 0; i < width * height; i ++) {
              int value = (src[i] & 0xFF) < 80 ? 0 : 255;
              dst[i] = 0xff000000 | ((value << 8) & 0x0000ff00);
          }
      }

      // manipulates a byte array
      void binarizerSimpleByteArray(unsigned char* src, unsigned char* dst, unsigned int width, unsigned int height) {
          int i;
          for(i = 0; i < width * height; i ++) {
              int value = (src[i] & 0xFF) < 80 ? 0 : 255;
              dst[i] = value & 0xFF;
          }
      }

      void binarizerGlobalHistogramByteToPackedIntArray(unsigned char* luminances, unsigned int* dst, unsigned int width, unsigned int height) {
          binarizerGlobalHistogramByteToPackedIntArrayImpl(luminances, dst, width, height);
      }

      int estimateBlackPointExport(int* buckets, int numBuckets) {
        return estimateBlackPoint(buckets, numBuckets);
      }
  }