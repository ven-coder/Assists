package com.ven.assists.base

interface GestureListener {
    /**
     * 手势执行前，可用于判断手势执行前判断执行位置是否位于浮窗范围，如果位于浮窗范围可隐藏浮窗或设置浮窗为不可触摸
     * @param startLocation 手势开始位置
     * @param endLocation 手势结束位置
     * @return 需要延迟执行的时间，毫秒，默认0
     */
    fun onGestureBegin(startLocation: FloatArray, endLocation: FloatArray): Long {
        return 0
    }

    /**
     * 手势取消（在手势执行过程用户在操作或者有其他手势同步执行会回调此方法）
     */
    fun onGestureCancelled() {}

    /**
     * 手势执行完成
     */
    fun onGestureCompleted() {}

    /**
     * 手势结束，手势取消或完成后都会回调
     * 可用于恢复手势开始前[GestureListener.onGestureBegin]所作的逻辑
     */
    fun onGestureEnd() {}
}