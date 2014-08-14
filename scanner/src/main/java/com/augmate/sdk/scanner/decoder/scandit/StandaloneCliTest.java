package com.augmate.sdk.scanner.decoder.scandit;

// ** running on local linux
// * must be using 32-bit java
// * compile and move things into proper folders
// * from scanner/src/main/java/com/augmate/sdk/scanner/scandit_decoder folder:
//    ~/Downloads/jdk1.7.0_67/bin/jjavac StandaloneCliTest.java ScanditSDKBarcodeReader.java
//    mkdir -p com/mirasense/scanditsdk/
//    mv BarcodeReader.class ../../../../mirasense/scanditsdk
// * to execute drop down to the /scanner/src/main/java/ path
// * we must force a library path for NDK libs used by scandit native library to resolve
//    LD_LIBRARY_PATH=~/android-ndk/platforms/android-19/arch-x86/usr/lib/ ~/Downloads/jdk1.7.0_67/bin/java -Djava.library.path=../../../libs/x86 -cp . com.augmate.sdk.scanner.decoder.scandit.StandaloneCliTest
// * prepend "strace -e open -f j" to debug path issues
// * sadly it crashes :(
//
// ** running on android
// * must create a copy of libs called lib with just the native scandit .so files
// * create a zip called scandit-libs.jar in libs/ path (which will be included in the build process)
// * from the scanner/ path
//    mkdir lib
//    cp libs lib -r
//    zip libs/scandit-libs.jar lib/*/*.so -r
// * wooo it works:
//        08-08 02:13:52.504    2312-2327/com.augmate.cycle.ups D/Augmate﹕ 672be28ec4178d40 | #1exsfi | decoder   | DecoderQR::testScandit(); reader provided non-null results
//        08-08 02:13:52.594    2312-2327/com.augmate.cycle.ups D/Augmate﹕ 672be28ec4178d40 | #1exsfi | decoder   | DecoderQR::testScandit();   fetched bytes contains data!
//        08-08 02:13:52.594    2312-2327/com.augmate.cycle.ups D/Augmate﹕ 672be28ec4178d40 | #1exsfi | decoder   | DecoderQR::testScandit(); reader code center = null

public class StandaloneCliTest {
    public static void main(String[] args) {
        System.out.println("Trying to spawn scandit reader..");
        com.mirasense.scanditsdk.ScanditSDKBarcodeReader reader = new com.mirasense.scanditsdk.ScanditSDKBarcodeReader();
        boolean text = reader.shouldIndicatorStayAtDefaultLocation();
        System.out.println("Got something back from barcode reader: " + text);
    }
}
