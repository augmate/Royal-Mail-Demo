package com.augmate.sdk.scanner.decoder.zxing;

/*
 * Copyright 2007 ZXing authors
 * TODO: import the full copyright information here
 */

public final class FinderPatternInfo {

    private final FinderPattern bottomLeft;
    private final FinderPattern topLeft;
    private final FinderPattern topRight;

    public FinderPatternInfo(FinderPattern[] patternCenters) {
        this.bottomLeft = patternCenters[0];
        this.topLeft = patternCenters[1];
        this.topRight = patternCenters[2];
    }

    public FinderPattern getBottomLeft() {
        return bottomLeft;
    }

    public FinderPattern getTopLeft() {
        return topLeft;
    }

    public FinderPattern getTopRight() {
        return topRight;
    }

}