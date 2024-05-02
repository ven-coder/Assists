//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.video;

import org.opencv.core.Scalar;

// C++: class Params

public class TrackerVit_Params {

    protected final long nativeObj;
    protected TrackerVit_Params(long addr) { nativeObj = addr; }

    public long getNativeObjAddr() { return nativeObj; }

    // internal usage only
    public static TrackerVit_Params __fromPtr__(long addr) { return new TrackerVit_Params(addr); }

    //
    // C++:   cv::TrackerVit::Params::Params()
    //

    public TrackerVit_Params() {
        nativeObj = TrackerVit_Params_0();
    }


    //
    // C++: string TrackerVit_Params::net
    //

    public String get_net() {
        return get_net_0(nativeObj);
    }


    //
    // C++: void TrackerVit_Params::net
    //

    public void set_net(String net) {
        set_net_0(nativeObj, net);
    }


    //
    // C++: int TrackerVit_Params::backend
    //

    public int get_backend() {
        return get_backend_0(nativeObj);
    }


    //
    // C++: void TrackerVit_Params::backend
    //

    public void set_backend(int backend) {
        set_backend_0(nativeObj, backend);
    }


    //
    // C++: int TrackerVit_Params::target
    //

    public int get_target() {
        return get_target_0(nativeObj);
    }


    //
    // C++: void TrackerVit_Params::target
    //

    public void set_target(int target) {
        set_target_0(nativeObj, target);
    }


    //
    // C++: Scalar TrackerVit_Params::meanvalue
    //

    public Scalar get_meanvalue() {
        return new Scalar(get_meanvalue_0(nativeObj));
    }


    //
    // C++: void TrackerVit_Params::meanvalue
    //

    public void set_meanvalue(Scalar meanvalue) {
        set_meanvalue_0(nativeObj, meanvalue.val[0], meanvalue.val[1], meanvalue.val[2], meanvalue.val[3]);
    }


    //
    // C++: Scalar TrackerVit_Params::stdvalue
    //

    public Scalar get_stdvalue() {
        return new Scalar(get_stdvalue_0(nativeObj));
    }


    //
    // C++: void TrackerVit_Params::stdvalue
    //

    public void set_stdvalue(Scalar stdvalue) {
        set_stdvalue_0(nativeObj, stdvalue.val[0], stdvalue.val[1], stdvalue.val[2], stdvalue.val[3]);
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:   cv::TrackerVit::Params::Params()
    private static native long TrackerVit_Params_0();

    // C++: string TrackerVit_Params::net
    private static native String get_net_0(long nativeObj);

    // C++: void TrackerVit_Params::net
    private static native void set_net_0(long nativeObj, String net);

    // C++: int TrackerVit_Params::backend
    private static native int get_backend_0(long nativeObj);

    // C++: void TrackerVit_Params::backend
    private static native void set_backend_0(long nativeObj, int backend);

    // C++: int TrackerVit_Params::target
    private static native int get_target_0(long nativeObj);

    // C++: void TrackerVit_Params::target
    private static native void set_target_0(long nativeObj, int target);

    // C++: Scalar TrackerVit_Params::meanvalue
    private static native double[] get_meanvalue_0(long nativeObj);

    // C++: void TrackerVit_Params::meanvalue
    private static native void set_meanvalue_0(long nativeObj, double meanvalue_val0, double meanvalue_val1, double meanvalue_val2, double meanvalue_val3);

    // C++: Scalar TrackerVit_Params::stdvalue
    private static native double[] get_stdvalue_0(long nativeObj);

    // C++: void TrackerVit_Params::stdvalue
    private static native void set_stdvalue_0(long nativeObj, double stdvalue_val0, double stdvalue_val1, double stdvalue_val2, double stdvalue_val3);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
