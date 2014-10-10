package com.augmate.sdk.scanner.decoder.scandit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

import java.lang.ref.SoftReference;

//import com.mirasense.scanditsdk.requirements.ScanditSDKScanRequirement;
//import com.scandit.base.geometry.SbRectangle;

// TODO: add relevant copyright/license info
// reversed out of scandit sdk
public class ScanditSDKGlobals
{
    private static ScanditSDKGlobals instance = null;
    public static String usedFramework = "native";
    public static final String LOG_NAME = "ScanditSDK";
    public static final boolean EVALUATION_SDK = false;
    public static final boolean ONLY_REGISTRATION_SDK = false;
    public static final int LOCATION_POLL_INTERVAL = 10;
    private final String DEFAULT_INITIAL_SCAN_SCREEN_STATE = "Align code with box";
    private final String DEFAULT_BARCODE_PRESENCE_DETECTED = "Align code and hold still";
    private final String DEFAULT_BARCODE_DECODING_IN_PROGRESS = "Decoding ...";
    private final String DEFAULT_TITLE_MESSAGE = "Scan a barcode";
    private final String DEFAULT_LEFT_BUTTON_CAPTION = "KEYPAD";
    private final String DEFAULT_LEFT_BUTTON_CAPTION_WHEN_KEYPAD_VISIBLE = "OK";
    private final String DEFAULT_RIGHT_BUTTON_CAPTION = "CANCEL";
    private final String DEFAULT_RIGHT_BUTTON_CAPTION_WHEN_KEYPAD_VISIBLE = "CANCEL";
    private final String DEFAULT_SEARCH_BAR_PLACEHOLDER = "Scan barcode or enter it here";
    //private final SbRectangle DEFAULT_TORCH_RECT = new SbRectangle(0.05F, 0.01F, 67, 33);
    //private final SbRectangle DEFAULT_CAMERA_SWITCH_RECT = new SbRectangle(0.05F, 0.01F, 67, 33);
    private final int DEFAULT_SEARCH_BAR_KEYBOARD_TYPE = 2;
    private final float DEFAULT_VIEWFINDER_RED = 1.0F;
    private final float DEFAULT_VIEWFINDER_GREEN = 1.0F;
    private final float DEFAULT_VIEWFINDER_BLUE = 1.0F;
    private final float DEFAULT_VIEWFINDER_RECOGNIZED_RED = 0.222F;
    private final float DEFAULT_VIEWFINDER_RECOGNIZED_GREEN = 0.753F;
    private final float DEFAULT_VIEWFINDER_RECOGNIZED_BLUE = 0.8F;
    private final float DEFAULT_VIEWFINDER_WIDTH = 0.7F;
    private final float DEFAULT_VIEWFINDER_HEIGHT = 0.3F;
    private final float DEFAULT_VIEWFINDER_LANDSCAPE_WIDTH = 0.4F;
    private final float DEFAULT_VIEWFINDER_LANDSCAPE_HEIGHT = 0.3F;
    private final boolean DEFAULT_DRAW_VIEWFINDER = true;
    private final int DEFAULT_VIEWFINDER_CORNER_RADIUS = 5;
    private final int DEFAULT_VIEWFINDER_LINE_WIDTH = 2;
    //private final ScanditSDKScanRequirement DEFAULT_SCAN_REQUIREMENT = null;
    private final float DEFAULT_HOT_SPOT_X = 0.5F;
    private final float DEFAULT_HOT_SPOT_Y = 0.5F;
    private final float DEFAULT_HOT_SPOT_HEIGHT = 0.25F;
    private final boolean DEFAULT_RESTRICT_ACTIVE_SCANNING_AREA = false;
    private final int DEFAULT_LOGO_X_OFFSET = 0;
    private final int DEFAULT_LOGO_Y_OFFSET = 0;
    private final int DEFAULT_LOGO_X_LANDSCAPE_OFFSET = 0;
    private final int DEFAULT_LOGO_Y_LANDSCAPE_OFFSET = 0;
    private final boolean DEFAULT_BEEP_ENABLED = true;
    private final boolean DEFAULT_VIBRATE_ENABLED = true;
    private final boolean DEFAULT_TORCH_ENABLED = true;
    private final int DEFAULT_CAMERA_SWITCH_VISIBILITY = 0;
    private final boolean DEFAULT_SHOW_TITLE_BAR = true;
    private final boolean DEFAULT_SHOW_TOOL_BAR = true;
    private final int DEFAULT_PREVIEW_ROTATION = 90;
    private final int DEFAULT_MSI_PLESSEY_CHECKSUM_MOD = 1;
    private final boolean DEFAULT_IS_LEGACY = false;
    private boolean mSetWithNewViewfinderDimensionFunction = true;
    private String mInitialScanScreenState = "Align code with box";
    private String mBarcodePresenceDetected = "Align code and hold still";
    private String mBarcodeDecodingInProgress = "Decoding ...";
    private String mTitleMessage = "Scan a barcode";
    private String mLeftButtonCaption = "KEYPAD";
    private String mLeftButtonCaptionWhenKeypadVisible = "OK";
    private String mRightButtonCaption = "CANCEL";
    private String mRightButtonCaptionWhenKeypadVisible = "CANCEL";
    private String mSearchBarPlaceholder = "Scan barcode or enter it here";
    private int mSearchBarKeyboardType = 2;
    private int mCameraSwitchImageId = 0;
    private int mCameraSwitchImagePressedId = 0;
    private int mTorchOffImageId = 0;
    private int mTorchOffImagePressedId = 0;
    private int mTorchOnImageId = 0;
    private int mTorchOnImagePressedId = 0;
    private int mBannerImageId = 0;
    private int mSearchButtonIconId = 0;
    private Bitmap mCameraSwitchBitmap = null;
    private Bitmap mCameraSwitchBitmapPressed = null;
    private Bitmap mTorchOffBitmap = null;
    private Bitmap mTorchOffBitmapPressed = null;
    private Bitmap mTorchOnBitmap = null;
    private Bitmap mTorchOnBitmapPressed = null;
    private SoftReference<Bitmap> mCameraSwitchImage = null;
    private SoftReference<Bitmap> mCameraSwitchImagePressed = null;
    private SoftReference<Bitmap> mTorchOffImage = null;
    private SoftReference<Bitmap> mTorchOffImagePressed = null;
    private SoftReference<Bitmap> mTorchOnImage = null;
    private SoftReference<Bitmap> mTorchOnImagePressed = null;
    private SoftReference<Bitmap> mBannerImage = null;
    private SoftReference<Bitmap> mSearchButtonIcon = null;
    //private SbRectangle mTorchButtonRect = this.DEFAULT_TORCH_RECT;
    //private SbRectangle mCameraSwitchButtonRect = this.DEFAULT_CAMERA_SWITCH_RECT;
    private float mViewfinderRed = 1.0F;
    private float mViewfinderGreen = 1.0F;
    private float mViewfinderBlue = 1.0F;
    private float mViewfinderRecognizedRed = 0.222F;
    private float mViewfinderRecognizedGreen = 0.753F;
    private float mViewfinderRecognizedBlue = 0.8F;
    private float mViewfinderWidth = 0.7F;
    private float mViewfinderHeight = 0.3F;
    private float mViewfinderLandscapeWidth = 0.4F;
    private float mViewfinderLandscapeHeight = 0.3F;
    private boolean mDrawViewfinder = true;
    private int mViewfinderCornerRadius = 5;
    private int mViewfinderLineWidth = 2;
    //private ScanditSDKScanRequirement mScanRequirement = this.DEFAULT_SCAN_REQUIREMENT;
    private float mScanningHotSpotX = 0.5F;
    private float mScanningHotSpotY = 0.5F;
    private float mScanningHotSpotHeight = 0.25F;
    private boolean mRestrictActiveScanningArea = false;
    private int mLogoXOffset = 0;
    private int mLogoYOffset = 0;
    private int mLogoXLandscapeOffset = 0;
    private int mLogoYLandscapeOffset = 0;
    private boolean mBeepEnabled = true;
    private int mBeepResource = 0;
    private boolean mVibrateEnabled = true;
    private boolean mTorchEnabled = true;
    private int mCameraSwitchVisibility = 0;
    private boolean mShowTitleBar = true;
    private boolean mShowToolBar = true;
    private int mPreviewRotation = 90;
    private int mMsiPlesseyChecksumType = 1;
    private boolean mIsLegacy = false;

    public static ScanditSDKGlobals getInstance(Context context)
    {
        if (instance == null) {
            instance = new ScanditSDKGlobals(context);
        }
        return instance;
    }

    public static void deleteInstance()
    {
        if (instance != null) {
            instance = null;
        }
    }

    public String getInitialScanScreenState()
    {
        return this.mInitialScanScreenState;
    }

    public void setInitialScanScreenState(String text)
    {
        this.mInitialScanScreenState = text;
    }

    public String getBarcodePresenceDetected()
    {
        return this.mBarcodePresenceDetected;
    }

    public void setBarcodePresenceDetected(String text)
    {
        this.mBarcodePresenceDetected = text;
    }

    public String getBarcodeDecodingInProgress()
    {
        return this.mBarcodeDecodingInProgress;
    }

    public void setBarcodeDecodingInProgress(String text)
    {
        this.mBarcodeDecodingInProgress = text;
    }

    public String getTitleMessage()
    {
        return this.mTitleMessage;
    }

    public void setTitleMessage(String message)
    {
        this.mTitleMessage = message;
    }

    public String getLeftButtonCaption()
    {
        return this.mLeftButtonCaption;
    }

    public void setLeftButtonCaption(String caption)
    {
        this.mLeftButtonCaption = caption;
    }

    public String getLeftButtonCaptionWhenKeypadVisible()
    {
        return this.mLeftButtonCaptionWhenKeypadVisible;
    }

    public void setLeftButtonCaptionWhenKeypadVisible(String caption)
    {
        this.mLeftButtonCaptionWhenKeypadVisible = caption;
    }

    public String getRightButtonCaption()
    {
        return this.mRightButtonCaption;
    }

    public void setRightButtonCaption(String caption)
    {
        this.mRightButtonCaption = caption;
    }

    public String getRightButtonCaptionWhenKeypadVisible()
    {
        return this.mRightButtonCaptionWhenKeypadVisible;
    }

    public void setRightButtonCaptionWhenKeypadVisible(String caption)
    {
        this.mRightButtonCaptionWhenKeypadVisible = caption;
    }

    public String getSearchBarPlaceholder()
    {
        return this.mSearchBarPlaceholder;
    }

    public void setSearchBarPlaceholder(String text)
    {
        this.mSearchBarPlaceholder = text;
    }

    public int getSearchBarKeyboardType()
    {
        return this.mSearchBarKeyboardType;
    }

    public void setSearchBarKeyboardType(int type)
    {
        this.mSearchBarKeyboardType = type;
    }

//    public SbRectangle getTorchButtonRect()
//    {
//        return this.mTorchButtonRect;
//    }
//
//    public void setTorchButtonRect(SbRectangle rect)
//    {
//        this.mTorchButtonRect = rect;
//    }
//
//    public SbRectangle getCameraSwitchButtonRect()
//    {
//        return this.mCameraSwitchButtonRect;
//    }
//
//    public void setCameraSwitchButtonRect(SbRectangle rect)
//    {
//        this.mCameraSwitchButtonRect = rect;
//    }

    public Bitmap getCameraSwitchImage(Context context)
    {
        if (this.mCameraSwitchBitmap != null) {
            return this.mCameraSwitchBitmap;
        }
        return getBitmapFromReference(context, this.mCameraSwitchImage, this.mCameraSwitchImageId);
    }

    public void setCameraSwitchImage(int imageRefId)
    {
        this.mCameraSwitchImageId = imageRefId;
    }

    public void setCameraSwitchImage(Bitmap image)
    {
        this.mCameraSwitchBitmap = image;
    }

    public Bitmap getCameraSwitchImagePressed(Context context)
    {
        if (this.mCameraSwitchBitmapPressed != null) {
            return this.mCameraSwitchBitmapPressed;
        }
        return getBitmapFromReference(context, this.mCameraSwitchImagePressed, this.mCameraSwitchImagePressedId);
    }

    public void setCameraSwitchImagePressed(int imageRefId)
    {
        this.mCameraSwitchImagePressedId = imageRefId;
    }

    public void setCameraSwitchImagePressed(Bitmap image)
    {
        this.mCameraSwitchBitmapPressed = image;
    }

    public Bitmap getTorchOffImage(Context context)
    {
        if (this.mTorchOffBitmap != null) {
            return this.mTorchOffBitmap;
        }
        return getBitmapFromReference(context, this.mTorchOffImage, this.mTorchOffImageId);
    }

    public void setTorchOffImage(int imageRefId)
    {
        this.mTorchOffImageId = imageRefId;
    }

    public void setTorchOffImage(Bitmap image)
    {
        this.mTorchOffBitmap = image;
    }

    public Bitmap getTorchOffImagePressed(Context context)
    {
        if (this.mTorchOffBitmapPressed != null) {
            return this.mTorchOffBitmapPressed;
        }
        return getBitmapFromReference(context, this.mTorchOffImagePressed, this.mTorchOffImagePressedId);
    }

    public void setTorchOffImagePressed(int imageRefId)
    {
        this.mTorchOffImagePressedId = imageRefId;
    }

    public void setTorchOffImagePressed(Bitmap image)
    {
        this.mTorchOffBitmapPressed = image;
    }

    public Bitmap getTorchOnImage(Context context)
    {
        if (this.mTorchOnBitmap != null) {
            return this.mTorchOnBitmap;
        }
        return getBitmapFromReference(context, this.mTorchOnImage, this.mTorchOnImageId);
    }

    public void setTorchOnImage(int imageRefId)
    {
        this.mTorchOnImageId = imageRefId;
    }

    public void setTorchOnImage(Bitmap image)
    {
        this.mTorchOnBitmap = image;
    }

    public Bitmap getTorchOnImagePressed(Context context)
    {
        if (this.mTorchOnBitmapPressed != null) {
            return this.mTorchOnBitmapPressed;
        }
        return getBitmapFromReference(context, this.mTorchOnImagePressed, this.mTorchOnImagePressedId);
    }

    public void setTorchOnImagePressed(int imageRefId)
    {
        this.mTorchOnImagePressedId = imageRefId;
    }

    public void setTorchOnImagePressed(Bitmap image)
    {
        this.mTorchOnBitmapPressed = image;
    }

    public Bitmap getBannerImage(Context context, int width, int height)
    {
        if ((this.mBannerImage != null) &&
                (this.mBannerImage.get() != null)) {
            return (Bitmap)this.mBannerImage.get();
        }
        if (this.mBannerImageId > 0)
        {
            Bitmap newBitmap = BitmapFactory.decodeResource(context.getResources(), this.mBannerImageId);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(newBitmap, width, height, true);
            this.mBannerImage = new SoftReference(scaledBitmap);
            return newBitmap;
        }
        return null;
    }

    public void setBannerImage(int imageRefId)
    {
        this.mBannerImageId = imageRefId;
    }

    public Bitmap getSearchButtonIcon(Context context)
    {
        return getBitmapFromReference(context, this.mSearchButtonIcon, this.mSearchButtonIconId);
    }

    public void setSearchButtonIcon(int imageRefId)
    {
        this.mSearchButtonIconId = imageRefId;
    }

    public float getViewfinderRed()
    {
        return this.mViewfinderRed;
    }

    public float getViewfinderGreen()
    {
        return this.mViewfinderGreen;
    }

    public float getViewfinderBlue()
    {
        return this.mViewfinderBlue;
    }

    public void setViewfinderColor(float r, float g, float b)
    {
        this.mViewfinderRed = r;
        this.mViewfinderGreen = g;
        this.mViewfinderBlue = b;
    }

    public float getViewfinderRecognizedRed()
    {
        return this.mViewfinderRecognizedRed;
    }

    public float getViewfinderRecognizedGreen()
    {
        return this.mViewfinderRecognizedGreen;
    }

    public float getViewfinderRecognizedBlue()
    {
        return this.mViewfinderRecognizedBlue;
    }

    public void setViewfinderRecognizedColor(float r, float g, float b)
    {
        this.mViewfinderRecognizedRed = r;
        this.mViewfinderRecognizedGreen = g;
        this.mViewfinderRecognizedBlue = b;
    }

    public double getViewfinderWidth()
    {
        return this.mViewfinderWidth;
    }

    public int getViewfinderWidthForView(int width, int height)
    {
        if (height > width) {
            return (int)(width * this.mViewfinderWidth);
        }
        if (!this.mSetWithNewViewfinderDimensionFunction) {
            return (int)(height * this.mViewfinderWidth);
        }
        return (int)(width * this.mViewfinderLandscapeWidth);
    }

    public double getViewfinderHeight()
    {
        return this.mViewfinderHeight;
    }

    public int getViewfinderHeightForView(int width, int height)
    {
        if (height > width) {
            return (int)(height * this.mViewfinderHeight);
        }
        if (!this.mSetWithNewViewfinderDimensionFunction) {
            return (int)(width * this.mViewfinderHeight);
        }
        return (int)(height * this.mViewfinderLandscapeHeight);
    }

    public void setViewfinderDimension(float width, float height)
    {
        this.mViewfinderWidth = width;
        this.mViewfinderHeight = height;
        this.mSetWithNewViewfinderDimensionFunction = false;
    }

    public void setViewfinderDimension(float width, float height, float landscapeWidth, float landscapeHeight)
    {
        this.mViewfinderWidth = width;
        this.mViewfinderHeight = height;
        this.mViewfinderLandscapeWidth = landscapeWidth;
        this.mViewfinderLandscapeHeight = landscapeHeight;
        this.mSetWithNewViewfinderDimensionFunction = true;
    }

    public boolean isViewfinderHeightChanged()
    {
        return (this.mViewfinderHeight != 0.3F) || (this.mViewfinderLandscapeHeight != 0.3F);
    }

    public boolean getDrawViewfinder()
    {
        return this.mDrawViewfinder;
    }

    public void setDrawViewfinder(boolean draw)
    {
        this.mDrawViewfinder = draw;
    }

    public int getViewfinderCornerRadius()
    {
        return this.mViewfinderCornerRadius;
    }

    public void setViewfinderCornerRadius(int radius)
    {
        this.mViewfinderCornerRadius = radius;
    }

    public int getViewfinderLineWidth()
    {
        return this.mViewfinderLineWidth;
    }

    public void setViewfinderLineWidth(int width)
    {
        this.mViewfinderLineWidth = width;
    }

//    public boolean isUsingMultiScan()
//    {
//        return (this.mScanRequirement != null) && (this.mScanRequirement.getRequiredCodes().size() > 1);
//    }

//    public ScanditSDKScanRequirement getScanRequirement()
//    {
//        return this.mScanRequirement;
//    }
//
//    public void setScanRequirement(ScanditSDKScanRequirement requirement)
//    {
//        this.mScanRequirement = requirement;
//    }

    public float getScanningHotSpotX()
    {
        return this.mScanningHotSpotX;
    }

    public void setScanningHotSpotX(float relX)
    {
        this.mScanningHotSpotX = relX;
    }

    public float getScanningHotSpotY()
    {
        return this.mScanningHotSpotY;
    }

    public void setScanningHotSpotY(float relY)
    {
        this.mScanningHotSpotY = relY;
    }

    public float getScanningHotSpotHeight()
    {
        return this.mScanningHotSpotHeight;
    }

    public void setScanningHotSpotHeight(float height)
    {
        this.mScanningHotSpotHeight = height;
    }

    public boolean getRestrictActiveScanningArea()
    {
        return this.mRestrictActiveScanningArea;
    }

    public void setRestrictActiveScanningArea(boolean restrict)
    {
        this.mRestrictActiveScanningArea = restrict;
    }

    public int getLogoXOffset()
    {
        return this.mLogoXOffset;
    }

    public void setLogoXOffset(int offset)
    {
        this.mLogoXOffset = offset;
    }

    public int getLogoYOffset()
    {
        return this.mLogoYOffset;
    }

    public void setLogoYOffset(int offset)
    {
        this.mLogoYOffset = offset;
    }

    public int getLogoXLandscapeOffset()
    {
        return this.mLogoXLandscapeOffset;
    }

    public void setLogoXLandscapeOffset(int offset)
    {
        this.mLogoXLandscapeOffset = offset;
    }

    public int getLogoYLandscapeOffset()
    {
        return this.mLogoYLandscapeOffset;
    }

    public void setLogoYLandscapeOffset(int offset)
    {
        this.mLogoYLandscapeOffset = offset;
    }

    public boolean isBeepEnabled()
    {
        return this.mBeepEnabled;
    }

    public void setBeepEnabled(boolean enabled)
    {
        this.mBeepEnabled = enabled;
    }

    public int getBeepResource()
    {
        return this.mBeepResource;
    }

    public void setBeepResource(int res)
    {
        this.mBeepResource = res;
    }

    public boolean isVibrateEnabled()
    {
        return this.mVibrateEnabled;
    }

    public void setVibrateEnabled(boolean enabled)
    {
        this.mVibrateEnabled = enabled;
    }

    public boolean isTorchEnabled()
    {
        return this.mTorchEnabled;
    }

    public void setTorchEnabled(boolean enabled)
    {
        this.mTorchEnabled = enabled;
    }

    public int getCameraSwitchVisibility()
    {
        return this.mCameraSwitchVisibility;
    }

    public void setCameraSwitchVisibility(int visibility)
    {
        this.mCameraSwitchVisibility = visibility;
    }

    public boolean isTitleBarShown()
    {
        return this.mShowTitleBar;
    }

    public void setShowTitleBar(boolean show)
    {
        this.mShowTitleBar = show;
    }

    public boolean isToolBarShown()
    {
        return this.mShowToolBar;
    }

    public void setShowToolBar(boolean show)
    {
        this.mShowToolBar = show;
    }

    public int getPreviewRotation()
    {
        return this.mPreviewRotation;
    }

    public void setPreviewRotation(int rotation)
    {
        this.mPreviewRotation = rotation;
    }

    public boolean isInPortraitMode()
    {
        if ((getPreviewRotation() == 90) || (getPreviewRotation() == 270)) {
            return true;
        }
        return false;
    }

    public int getMsiPlesseyChecksumType()
    {
        return this.mMsiPlesseyChecksumType;
    }

    public void setMsiPlesseyChecksumType(int checksumType)
    {
        this.mMsiPlesseyChecksumType = checksumType;
    }

    public boolean isLegacy()
    {
        return this.mIsLegacy;
    }

    public void setIsLegacy(boolean isLegacy)
    {
        this.mIsLegacy = isLegacy;
    }

    public ScanditSDKGlobals(Context context)
    {
        this.mInitialScanScreenState = "Align code with box";
        this.mBarcodePresenceDetected = "Align code and hold still";
        this.mBarcodeDecodingInProgress = "Decoding ...";

        this.mTitleMessage = "Scan a barcode";
        this.mLeftButtonCaption = "KEYPAD";
        this.mLeftButtonCaptionWhenKeypadVisible = "OK";
        this.mRightButtonCaption = "CANCEL";
        this.mRightButtonCaptionWhenKeypadVisible = "CANCEL";

        this.mSearchBarPlaceholder = "Scan barcode or enter it here";
        this.mSearchBarKeyboardType = 2;

//        this.mTorchButtonRect = this.DEFAULT_TORCH_RECT;
//        this.mCameraSwitchButtonRect = this.DEFAULT_CAMERA_SWITCH_RECT;

        this.mViewfinderRed = 1.0F;
        this.mViewfinderGreen = 1.0F;
        this.mViewfinderBlue = 1.0F;
        this.mViewfinderRecognizedRed = 0.222F;
        this.mViewfinderRecognizedGreen = 0.753F;
        this.mViewfinderRecognizedBlue = 0.8F;
        this.mViewfinderHeight = 0.3F;
        this.mViewfinderWidth = 0.7F;
        this.mDrawViewfinder = true;

        this.mViewfinderCornerRadius = 5;
        this.mViewfinderLineWidth = 2;

//        this.mScanRequirement = this.DEFAULT_SCAN_REQUIREMENT;

        this.mScanningHotSpotX = 0.5F;
        this.mScanningHotSpotY = 0.5F;
        this.mScanningHotSpotHeight = 0.25F;
        this.mRestrictActiveScanningArea = false;

        this.mLogoXOffset = 0;
        this.mLogoYOffset = 0;
        this.mLogoXLandscapeOffset = 0;
        this.mLogoYLandscapeOffset = 0;

        this.mBeepEnabled = true;
        this.mVibrateEnabled = true;
        this.mTorchEnabled = true;
        this.mCameraSwitchVisibility = 0;

        this.mShowTitleBar = true;
        this.mShowToolBar = true;

        this.mPreviewRotation = 90;

        this.mMsiPlesseyChecksumType = 1;



        String pack = context.getPackageName();
        String type = "raw";


        setInitialScanScreenState(getStringRes(context, pack, "scanditsdk_initial_scan_screen_state", getInitialScanScreenState()));



        setBarcodePresenceDetected(getStringRes(context, pack, "scanditsdk_barcode_presence_detected", getBarcodePresenceDetected()));



        setBarcodeDecodingInProgress(getStringRes(context, pack, "scanditsdk_barcode_decoding_in_progress", getBarcodeDecodingInProgress()));



        setSearchBarPlaceholder(getStringRes(context, pack, "scanditsdk_searchbar_placeholder", getSearchBarPlaceholder()));


        setTitleMessage(getStringRes(context, pack, "scanditsdk_title_message", getTitleMessage()));



        setLeftButtonCaption(getStringRes(context, pack, "scanditsdk_left_button_caption", getLeftButtonCaption()));



        setLeftButtonCaptionWhenKeypadVisible(getStringRes(context, pack, "scanditsdk_left_button_caption_when_keypad_visible", getLeftButtonCaptionWhenKeypadVisible()));



        setRightButtonCaption(getStringRes(context, pack, "scanditsdk_right_button_caption", getRightButtonCaption()));



        setRightButtonCaptionWhenKeypadVisible(getStringRes(context, pack, "scanditsdk_right_button_caption_when_keypad_visible", getRightButtonCaptionWhenKeypadVisible()));
        try
        {
            int beepId = context.getResources().getIdentifier("beep", type, pack);
            if (beepId > 0) {
                setBeepResource(beepId);
            }
            String suffix = "";

            DisplayMetrics metrics = new DisplayMetrics();
            Activity activity = (Activity)context;
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            if (metrics.densityDpi == 120) {
                suffix = "_ldpi";
            } else if ((metrics.densityDpi == 160) || (metrics.densityDpi == 160)) {
                suffix = "_mdpi";
            } else if ((metrics.densityDpi == 240) || (metrics.densityDpi == 213)) {
                suffix = "_hdpi";
            }
            setCameraSwitchImage(getResIdentifier(context, "camera_swap_icon", suffix, type, pack));

            setCameraSwitchImagePressed(getResIdentifier(context, "camera_swap_icon_pressed", suffix, type, pack));

            setTorchOffImage(getResIdentifier(context, "flashlight_turn_on_icon", suffix, type, pack));

            setTorchOffImagePressed(getResIdentifier(context, "flashlight_turn_on_icon_pressed", suffix, type, pack));

            setTorchOnImage(getResIdentifier(context, "flashlight_turn_off_icon", suffix, type, pack));

            setTorchOnImagePressed(getResIdentifier(context, "flashlight_turn_off_icon_pressed", suffix, type, pack));


            setBannerImage(context.getResources().getIdentifier("poweredby2x", type, pack));

            setSearchButtonIcon(context.getResources().getIdentifier("ic_btn_search", type, pack));
        }
        catch (Exception e) {}
    }

    private Bitmap getBitmapFromReference(Context context, SoftReference<Bitmap> bitmapRef, int imageRefId)
    {
        if ((bitmapRef != null) &&
                (bitmapRef.get() != null)) {
            return (Bitmap)bitmapRef.get();
        }
        if (imageRefId > 0)
        {
            Bitmap newBitmap = BitmapFactory.decodeResource(context.getResources(), imageRefId);

            bitmapRef = new SoftReference(newBitmap);
            return newBitmap;
        }
        return null;
    }

    private static String getStringRes(Context context, String pack, String key, String defaultString)
    {
        int initialScanScreenStateId = context.getResources().getIdentifier(key, "string", pack);
        if (initialScanScreenStateId > 0) {
            return context.getString(initialScanScreenStateId);
        }
        return defaultString;
    }

    private static int getResIdentifier(Context context, String name, String suffix, String type, String pack)
    {
        int id = context.getResources().getIdentifier(name + suffix, type, pack);
        if (id <= 0) {
            id = context.getResources().getIdentifier(name, type, pack);
        }
        return id;
    }
}
