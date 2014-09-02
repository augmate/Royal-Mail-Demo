#if 0
#include <stdio.h>
#include "Helpers.h"

#include <zxing/common/Counted.h>
#include <zxing/Binarizer.h>
#include <zxing/MultiFormatReader.h>
#include <zxing/Result.h>
#include <zxing/ReaderException.h>
#include <zxing/common/GlobalHistogramBinarizer.h>
#include <zxing/common/HybridBinarizer.h>
#include <zxing/common/GreyscaleLuminanceSource.h>
#include <exception>
#include <zxing/Exception.h>
#include <zxing/common/IllegalArgumentException.h>
#include <zxing/BinaryBitmap.h>
#include <zxing/DecodeHints.h>

#include <zxing/qrcode/QRCodeReader.h>
#include <zxing/multi/qrcode/QRCodeMultiReader.h>
#include <zxing/multi/ByQuadrantReader.h>
#include <zxing/multi/MultipleBarcodeReader.h>
#include <zxing/multi/GenericMultipleBarcodeReader.h>

#include <zxing/qrcode/detector/Detector.h>

using namespace std;
using namespace zxing;
using namespace zxing::multi;
using namespace zxing::qrcode;

namespace {
    bool more = false;
    bool test_mode = false;
    bool try_harder = false;
    bool search_multi = false;
    bool use_hybrid = false;
    bool use_global = false;
    bool verbose = false;
}

vector<Ref<Result> > decode(Ref<BinaryBitmap> image, DecodeHints hints) {
  Ref<Reader> reader(new MultiFormatReader);
  return vector<Ref<Result> >(1, reader->decode(image, hints));
}

vector<Ref<Result> > decode_multi(Ref<BinaryBitmap> image, DecodeHints hints) {
  MultiFormatReader delegate;
  GenericMultipleBarcodeReader reader(delegate);
  return reader.decodeMultiple(image, hints);
}

bool runNativeZXingPort(Ref<LuminanceSource> source) {

    // HACK: throwing an zxing exception here causes some magic to happen which allows their native
    // scanner to work when linked against JNI. yes, really.

    try {
        LOGD("Practicing throwing a ReaderException..\n");
        throw zxing::Exception("practice");
    }
    catch(std::exception& err) {
        LOGD("Caught practice throw. Moving onto better things..\n");
    }

    // TODO: create a replacement simple binarizer :)
    //Ref<Binarizer> binarizer(new HybridBinarizer(source));
    Ref<Binarizer> binarizer(new GlobalHistogramBinarizer(source));

    // QR only
    DecodeHints hints(DecodeHints::QR_CODE_HINT);

    bool bGotResult = false;
    Ref<Result> result;

    try {
        LOGD("Setting up binary bitmap reference..\n");
        Ref<BinaryBitmap> binary(new BinaryBitmap(binarizer));
        LOGD("Spawning native qrcode decoder..\n");
        Ref<zxing::Reader> qrDecoder(new zxing::qrcode::QRCodeReader());
        LOGD("Decodering..\n");
        result = qrDecoder->decode(binary, hints);
        LOGD("Decodering.. Success\n");

        bGotResult = true;
    }
    catch(ReaderException& e) {
        LOGD("Caught ReaderException from decoder.\n");
    }
    catch(std::exception& e) {
        LOGD("Caught a generic exception from decoder.\n");
    }

    if(bGotResult) {
        LOGD("Binarizer succeeded\n");
        LOGD("Format: %s\n", BarcodeFormat::barcodeFormatNames[result->getBarcodeFormat()]);
        LOGD("Result value: %s\n", result->getText()->getText().c_str());
    }

    return bGotResult;
}

void zxingNativeDecodeImpl(unsigned char* src, unsigned int width, unsigned int height) {
    zxing::ArrayRef<char> image = zxing::ArrayRef<char>((char*) src, width * height);
    zxing::GreyscaleLuminanceSource* luminance = new zxing::GreyscaleLuminanceSource(image, width, height, 0, 0, width, height);
    zxing::Ref<LuminanceSource> luminanceSource = zxing::Ref<LuminanceSource>(luminance);
    runNativeZXingPort(luminanceSource);
}

// exportable wrappers for linkage
extern "C" {

    void zxingNativeDecode(unsigned char* src, unsigned int width, unsigned int height) {
        LOGD("Executing native decoder under: %s\n", ABI);
        zxingNativeDecodeImpl(src, width, height);
        LOGD("Executed native zxing!\n");
    }

    int main() {
        printf("Testing native zxing decoder in CLI\n");

        int width = 640;
        int height = 360;
        unsigned char* src = new unsigned char[width * height];

        zxingNativeDecodeImpl(src, width, height);

        LOGD("ZXing completed successfully.\n");

        return 0;
    }
}
#endif