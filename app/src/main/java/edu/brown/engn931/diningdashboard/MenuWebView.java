package edu.brown.engn931.diningdashboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class MenuWebView extends WebView {

    public MenuWebView(Context context) {
        super(context);
    }

    public MenuWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(event);
    }
}