package com.appsters.flexx.Utils;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {

    public BarcodeGraphicTracker.BarcodeDetectorListener mDetectorListener;

    public BarcodeTrackerFactory(BarcodeGraphicTracker.BarcodeDetectorListener detectorListener) {
        mDetectorListener = detectorListener;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        BarcodeGraphicTracker graphicTracker = new BarcodeGraphicTracker();
        if (mDetectorListener != null)
            graphicTracker.setDetectorListener(mDetectorListener);
        return graphicTracker;
    }
}