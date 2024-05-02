//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.video;

import org.opencv.video.Tracker;
import org.opencv.video.TrackerVit;
import org.opencv.video.TrackerVit_Params;

// C++: class TrackerVit
/**
 * the VIT tracker is a super lightweight dnn-based general object tracking.
 *
 * VIT tracker is much faster and extremely lightweight due to special model structure, the model file is about 767KB.
 * Model download link: https://github.com/opencv/opencv_zoo/tree/main/models/object_tracking_vittrack
 * Author: PengyuLiu, 1872918507@qq.com
 */
public class TrackerVit extends Tracker {

    protected TrackerVit(long addr) { super(addr); }

    // internal usage only
    public static TrackerVit __fromPtr__(long addr) { return new TrackerVit(addr); }

    //
    // C++: static Ptr_TrackerVit cv::TrackerVit::create(TrackerVit_Params parameters = TrackerVit::Params())
    //

    /**
     * Constructor
     *     @param parameters vit tracker parameters TrackerVit::Params
     * @return automatically generated
     */
    public static TrackerVit create(TrackerVit_Params parameters) {
        return TrackerVit.__fromPtr__(create_0(parameters.nativeObj));
    }

    /**
     * Constructor
     * @return automatically generated
     */
    public static TrackerVit create() {
        return TrackerVit.__fromPtr__(create_1());
    }


    //
    // C++:  float cv::TrackerVit::getTrackingScore()
    //

    /**
     * Return tracking score
     * @return automatically generated
     */
    public float getTrackingScore() {
        return getTrackingScore_0(nativeObj);
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++: static Ptr_TrackerVit cv::TrackerVit::create(TrackerVit_Params parameters = TrackerVit::Params())
    private static native long create_0(long parameters_nativeObj);
    private static native long create_1();

    // C++:  float cv::TrackerVit::getTrackingScore()
    private static native float getTrackingScore_0(long nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
