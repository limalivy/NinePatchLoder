package com.yy.ninepatchloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.NinePatchDrawable;
import android.view.View;

/**
 * @author linmin1, YY Inc
 * @version 2017/12/9 17:17.
 */

public class NinePatchLoader {

    public static void loadFromPath(Context context, View view, String path){
        loadFromPath(context,view,path,null);
    }

    public static void loadFromBitmap(Context context,View view,Bitmap bitmap){
        loadFromBitmap(context, view, bitmap,null);
    }

    public static void loadFromResources(Context context,View view,int id){
        loadFromResources(context, view, id,null);
    }

    public static void loadFromPath(Context context, View view, String path, NinePatchLoadErrorCall errorCall){
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(bitmap == null){
            if(errorCall!=null){
                errorCall.onError(new RuntimeException(path + " cannot load to bitmap."));
            }
            return;
        }
        loadFromBitmap(context,view,bitmap,errorCall);
    }

    public static void loadFromBitmap(Context context,View view,Bitmap bitmap,NinePatchLoadErrorCall errorCall){
        NinePatchDrawableWithInfo drawableWithInfo = decodeBitmap(context,bitmap,errorCall);
        if(drawableWithInfo!=null){
            view.setBackgroundDrawable(drawableWithInfo.drawable);
            view.setPadding(dp2px(context,drawableWithInfo.info.mBottomDiv.start),
                    dp2px(context,drawableWithInfo.info.mRightDiv.start),
                    dp2px(context,drawableWithInfo.info.mBottomDiv.end),
                    dp2px(context,drawableWithInfo.info.mRightDiv.end));
        }
    }

    public static void loadFromResources(Context context,View view,int id,NinePatchLoadErrorCall errorCall){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),id);
        loadFromBitmap(context,view,bitmap,errorCall);
    }

    private static NinePatchDrawableWithInfo decodeBitmap(Context context, Bitmap bitmap,NinePatchLoadErrorCall call){
        NinePatchDrawableWithInfo drawableWithInfo = null;
        try{
            NinePatchInfo ninePatchInfo = NinePatchReader.readFormBitmap(bitmap);
            if(ninePatchInfo == null)
                return null;
            byte[] chunk = NinePatchChunkCreater.create(context,ninePatchInfo);
            Matrix matrix = new Matrix();
            int scale = dp2px(context,1);
            matrix.postScale(scale,scale);
            Bitmap bmp2 = Bitmap.createBitmap(bitmap,1,1,bitmap.getWidth()-2,bitmap.getHeight()-2,matrix,true);
            NinePatchDrawable drawable = new NinePatchDrawable(bmp2,chunk,null,null);
            drawableWithInfo = new NinePatchDrawableWithInfo(drawable,ninePatchInfo);
        }catch (Exception e){
            if(call!=null){
                call.onError(e);
            }
            return null;
        }
        return drawableWithInfo;
    }

    private static int dp2px(Context context,float dp){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private static class NinePatchDrawableWithInfo{
        public NinePatchDrawableWithInfo(NinePatchDrawable drawable, NinePatchInfo info) {
            this.drawable = drawable;
            this.info = info;
        }

        public NinePatchDrawable drawable;
        public NinePatchInfo info;
    }
}
