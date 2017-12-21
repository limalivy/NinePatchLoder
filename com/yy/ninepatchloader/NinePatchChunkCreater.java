package com.yy.ninepatchloader;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * @author linmin1, YY Inc
 * @version 2017/12/4 15:12.
 */
/**hide*/
class NinePatchChunkCreater {
    private static final int NO_COLOR = 0x00000001;

    public static byte[] create(Context context, NinePatchInfo info) {
        return getByteBuffer(context,info).array();
    }


    private static ByteBuffer getByteBuffer(Context context, NinePatchInfo info) {
        final byte colorSize = info.colorSize;
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 * 7 + 4 * 2 * info.mTopDivs.size() + 4 * 2 * info.mLeftDivs.size() + 4 * colorSize)
                .order(ByteOrder.nativeOrder());
        buffer.put((byte) 0x01);
        //x div
        buffer.put((byte) (info.mTopDivs.size() * 2));
        //y div
        buffer.put((byte) (info.mLeftDivs.size() * 2));
        //color 先设置9色
        buffer.put(colorSize);

        //skip
        buffer.putInt(0);
        buffer.putInt(0);

        //padding
        buffer.putInt(dp(context, info.mBottomDiv.start));
        buffer.putInt(dp(context, info.mBottomDiv.end));
        buffer.putInt(dp(context, info.mRightDiv.start));
        buffer.putInt(dp(context, info.mRightDiv.end));

        //skip
        buffer.putInt(0);

        for (NinePatchInfo.Div div : info.mTopDivs) {
            buffer.putInt(dp(context, div.start));
            buffer.putInt(dp(context, div.end));
        }

        for (NinePatchInfo.Div div : info.mLeftDivs) {
            buffer.putInt(dp(context, div.start));
            buffer.putInt(dp(context, div.end));
        }
        //[1, -872415232, -872415232, -872415232, 1, 1, -872415232, -872415232, -872415232, 1, 1, 1, 1, 1, 1]}
        for(int i = 0 ; i <colorSize;++i){
            buffer.putInt(NO_COLOR);
        }
        return buffer;
    }

    private static ByteBuffer getByteBuffer(List<NinePatchInfo.Div> rangeListX, List<NinePatchInfo.Div> rangeListY) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 * 7 + 4 * 2 * rangeListX.size() + 4 * 2 * rangeListY.size() + 4 * 9)
                .order(ByteOrder.nativeOrder());
        // was serialised
        buffer.put((byte) 0x01);
        // x div
        buffer.put((byte) (rangeListX.size() * 2));
        // y div
        buffer.put((byte) (rangeListY.size() * 2));
        // color
        buffer.put((byte) 0x09);

        // skip
        buffer.putInt(0);
        buffer.putInt(0);

        // padding
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);

        // skip 4 bytes
        buffer.putInt(0);

        for (NinePatchInfo.Div range : rangeListX) {
            buffer.putInt(range.start);
            buffer.putInt(range.end);
        }
        for (NinePatchInfo.Div range : rangeListY) {
            buffer.putInt(range.start);
            buffer.putInt(range.end);
        }
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);

        return buffer;


    }

    private static int dp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
