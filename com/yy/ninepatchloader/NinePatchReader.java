package com.yy.ninepatchloader;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;


/**
 * @author linmin1, YY Inc
 * @version 2017/12/4 11:16.
 */

class NinePatchReader {
    /**
     * 不可拉伸区域
     */
    private static final int FLEXED = 0x00000000;
    /**
     * 可拉伸区域
     */
    private static final int STRETCHABLE = 0xff000000;

    public static NinePatchInfo readFormBitmap(Bitmap bmp) throws Exception {
        if (bmp == null) {
            return null;
        }
        if(bmp.getConfig()!= Bitmap.Config.ARGB_8888 ){
            throw new Exception("this image maybe not .9 format");
        }
        NinePatchInfo info = new NinePatchInfo();
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int[] leftOrRightPixels = new int[h];
        //加载左边
        bmp.getPixels(leftOrRightPixels, 0, 1, 0, 0, 1, h);
        info.mLeftDivs = readLeftOrTopFromPixels(leftOrRightPixels);
        int yS = readDivCount(leftOrRightPixels);
        //加载右边
        bmp.getPixels(leftOrRightPixels, 0, 1, w - 1, 0, 1, h);
        info.mRightDiv = readRightOrBottomFromPixels(leftOrRightPixels, info.mLeftDivs);
        //清空
        leftOrRightPixels = null;
        int[] topOrBottomPixels = new int[w];
        //加载上边
        bmp.getPixels(topOrBottomPixels, 0, w, 0, 0, w, 1);
        info.mTopDivs = readLeftOrTopFromPixels(topOrBottomPixels);
        int xS = readDivCount(topOrBottomPixels);
        //加载下边
        bmp.getPixels(topOrBottomPixels, 0, w, 0, h - 1, w, 1);
        info.mBottomDiv = readRightOrBottomFromPixels(topOrBottomPixels, info.mTopDivs);

        info.colorSize = (byte) (xS * yS);
        return info;
    }

    private static int readDivCount(int[] pixels) {
        int result = 1;
        int nowColor = pixels[1];
        for (int i = 1; i < pixels.length - 1; ++i) {
            if (nowColor != pixels[i]) {
                result++;
                nowColor = pixels[i];
            }
        }
        return result;
    }

    private static List<NinePatchInfo.Div> readLeftOrTopFromPixels(int[] pixels) throws Exception {
        //检查是否是.9图
        if (!checkTopOrLeft(pixels)) {
            throw new Exception("this image maybe not .9 format");
        }
        List<NinePatchInfo.Div> divs = new ArrayList<>();
        boolean shouldNewStart = true;
        NinePatchInfo.Div nowEditDiv = null;
        for (int i = 1, size = pixels.length - 1; i < size; ++i) {
            if (shouldNewStart == true && pixels[i] == STRETCHABLE) {
                nowEditDiv = new NinePatchInfo.Div();
                nowEditDiv.start = i - 1;
                shouldNewStart = false;
            } else if (shouldNewStart == false && pixels[i] == STRETCHABLE && (i + 1 >= size || pixels[i + 1] == FLEXED)) {
                nowEditDiv.end = i;
                shouldNewStart = true;
                divs.add(nowEditDiv);
            }
        }
        return divs;
    }

    private static boolean checkTopOrLeft(int[] pixels) {
        if (pixels == null || pixels.length <= 2)
            return false;
        for (int i = 1, size = pixels.length - 1; i < size; ++i) {
            if (!(pixels[i] == FLEXED || pixels[i] == STRETCHABLE))
                return false;
        }
        return true;
    }

    private static boolean checkBottomOrRight(int[] pixels) {
        if (pixels == null || pixels.length <= 2)
            return false;
        int colors = 1;
        int nowColor = pixels[1];
        for (int i = 1, size = pixels.length - 1; i < size; ++i) {
            if (nowColor != pixels[i]) {
                colors++;
                nowColor = pixels[i];
            }
        }
        if (colors > 3) {
            return false;
        }
        return true;
    }

    private static NinePatchInfo.Div readRightOrBottomFromPixels(int[] pixels, List<NinePatchInfo.Div> topOrLeftDivs) throws Exception {
        //检查是否是.9图
        if (!checkBottomOrRight(pixels)) {
            throw new Exception("this image maybe not .9 format");
        }
        NinePatchInfo.Div div = new NinePatchInfo.Div();
        div.start = 0;
        div.end = 0;
        boolean hasStretchable = false;
        for (int i = 1, size = pixels.length - 1; i < size; ++i) {
            if (!hasStretchable && pixels[i] == STRETCHABLE) {
                div.start = i - 1;
                hasStretchable = true;
            } else if (hasStretchable && pixels[i] == STRETCHABLE && (i + 1 >= size || pixels[i + 1] == FLEXED)) {
                div.end = size - i - 1;
            }
        }
        if (!hasStretchable) {
            if (topOrLeftDivs.isEmpty()) {
                return div;
            }
            int minStart = Integer.MAX_VALUE;
            int maxEnd = Integer.MIN_VALUE;
            for (NinePatchInfo.Div item : topOrLeftDivs) {
                if (item.start < minStart) {
                    minStart = item.start;
                }
                if (item.end > maxEnd) {
                    maxEnd = item.end;
                }
            }
            div.start = minStart;
            div.end = pixels.length - maxEnd - 2;
        }
        return div;
    }
}
