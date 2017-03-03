package com.clam314.pathmeasuretry;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by clam314 on 2017/3/3
 */

public class StickyRefreshLayout extends ViewGroup{

    public StickyRefreshLayout(Context context) {
        super(context);
    }

    public StickyRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickyRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
