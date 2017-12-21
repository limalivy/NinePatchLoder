package com.yy.ninepatchloader;

import java.util.List;

/**
 * @author linmin1, YY Inc
 * @version 2017/12/4 14:20.
 */
//flex
class NinePatchInfo {
    public List<Div> mLeftDivs;
    public List<Div> mTopDivs;
    public Div mRightDiv;
    public Div mBottomDiv;
    public byte colorSize;

    public static class Div{
        public int start;
        public int end;
    }
}
