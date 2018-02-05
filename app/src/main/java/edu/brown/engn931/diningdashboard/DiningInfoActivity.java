package edu.brown.engn931.diningdashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import edu.brown.engn931.diningdashboard.InfoActivity;


public class DiningInfoActivity extends InfoActivity implements ObservableScrollViewCallbacks {

    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;
    private static final boolean TOOLBAR_IS_STICKY = true;

    private View mToolbar;
    private ImageView mImageView;
    private View mOverlayView;
    private ObservableScrollView mScrollView;
    private TextView mTitleView;
    private View mFab;
    private int mActionBarSize;
    private int mFlexibleSpaceShowFabOffset;
    private int mFlexibleSpaceImageHeight;
    private int mFabMargin;
    private int mToolbarColor;
    private boolean mFabIsShown;
    LinearLayout infoLayout;
    ProgressBar menuProgress;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_info);


        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = getIntent().getStringExtra("edu.brown.engn931.diningdashboard.title");

        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mFlexibleSpaceShowFabOffset = getResources().getDimensionPixelSize(R.dimen.flexible_space_show_fab_offset);
        mActionBarSize = getActionBarSize();
        mToolbarColor = getResources().getColor(R.color.red_primary);

        mToolbar = findViewById(R.id.toolbar);
        if (!TOOLBAR_IS_STICKY) {
            mToolbar.setBackgroundColor(Color.TRANSPARENT);
        }
        mImageView = (ImageView) findViewById(R.id.image);
        infoLayout = (LinearLayout) findViewById(R.id.infoLayout);
        mOverlayView = findViewById(R.id.overlay);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
        mScrollView.setScrollViewCallbacks(this);
        mTitleView = (TextView) findViewById(R.id.title);
        mTitleView.setText(title);
        setTitle(null);
        setupDetails(title);
        mFab = findViewById(R.id.fab);
        mFabMargin = getResources().getDimensionPixelSize(R.dimen.margin_standard);
        ViewHelper.setScaleX(mFab, 0);
        ViewHelper.setScaleY(mFab, 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showFab();
            }
        }, 300);

        ScrollUtils.addOnGlobalLayoutListener(mScrollView, new Runnable() {
            @Override
            public void run() {
                mScrollView.scrollTo(0, mFlexibleSpaceImageHeight - mActionBarSize);

                mScrollView.scrollTo(0, 0);
                onScrollChanged(0, false, false);

                // This causes scroll change from 1 to 0.
                //mScrollView.scrollTo(0, 1);
                //mScrollView.scrollTo(0, 0);
            }
        });

        /*getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        getWindow().setStatusBarColor(Color.TRANSPARENT);*/
    }

    public void setupDetails(String eatery) {
        int resId;
        String url = "http://www.brown.edu/Student_Services/Food_Services/";
        boolean loadWebview = false;
        switch (eatery) {
            case "Ratty":
                resId = R.drawable.bk_ratty;
                new RattyMenuTask(this).execute();
                break;
            case "Andrews Commons":
                resId = R.drawable.bk_andrews2;
                loadWebview = true;
                url = "http://www.brown.edu/Student_Services/Food_Services/eateries/andrews.php";
                break;
            case "Blue Room":
                resId = R.drawable.bk_blueroom2;
                loadWebview = true;
                url = "http://www.brown.edu/Student_Services/Food_Services/eateries/blueroom.php";
                break;
            case "Josiah's":
                resId = R.drawable.bk_jos;
                loadWebview = true;
                url = "http://www.brown.edu/Student_Services/Food_Services/eateries/josiahs.php";
                break;
            case "V-Dub":
                resId = R.drawable.bk_vdub2;
                new VDubMenuTask(this).execute();
                break;
            default:
                resId = R.drawable.bk_ratty;
                Toast.makeText(this, "Invalid Eatery", Toast.LENGTH_LONG).show();
                break;
        }
        if (loadWebview) {
            menuProgress = (ProgressBar) findViewById(R.id.menuProgress);
            MenuWebView webView = (MenuWebView) findViewById(R.id.menuWebView);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
            WebSettings webSettings = webView.getSettings();
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setJavaScriptEnabled(true);
            webSettings.setGeolocationEnabled(true);
            webSettings.setSupportMultipleWindows(true);
            webView.scrollTo(webView.getScrollX() + 1000, webView.getScrollY());
            mScrollView.requestDisallowInterceptTouchEvent(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return false;
                }
            });

            webView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    if (progress < 100 && menuProgress.getVisibility() == ProgressBar.GONE) {
                        menuProgress.setVisibility(ProgressBar.VISIBLE);
                    }
                    menuProgress.setProgress(progress);
                    if (progress == 100) {
                        menuProgress.setVisibility(ProgressBar.GONE);
                    }
                }

                @Override
                public void onReceivedTitle(WebView view, String title) {
                    getWindow().setTitle(title); //Set Activity tile to page title.
                }
            });
        }

        mImageView.setImageResource(resId);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dining_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        // Translate overlay and image
        float flexibleRange = mFlexibleSpaceImageHeight - mActionBarSize;
        int minOverlayTransitionY = mActionBarSize - mOverlayView.getHeight();
        ViewHelper.setTranslationY(mOverlayView, ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        ViewHelper.setTranslationY(mImageView, ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        // Change alpha of overlay
        ViewHelper.setAlpha(mOverlayView, ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));

        // Scale title text
        float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        ViewHelper.setPivotX(mTitleView, 0);
        ViewHelper.setPivotY(mTitleView, 0);
        ViewHelper.setScaleX(mTitleView, scale);
        ViewHelper.setScaleY(mTitleView, scale);

        // Translate title text
        int maxTitleTranslationX = 70;
        int maxTitleTranslationY = (int) (mFlexibleSpaceImageHeight - mTitleView.getHeight() * scale);

        int titleTranslationX = scrollY / 4;
        int titleTranslationY = maxTitleTranslationY - scrollY;
        if (TOOLBAR_IS_STICKY) {
            titleTranslationY = Math.max(0, titleTranslationY);
        }
        ViewHelper.setTranslationX(mTitleView, Math.min(maxTitleTranslationX, titleTranslationX));
        ViewHelper.setTranslationY(mTitleView, titleTranslationY);

        // Translate FAB
        int fabSize = dpToPx(56);
        int maxFabTranslationY = mFlexibleSpaceImageHeight - fabSize / 2;
        float fabTranslationY = ScrollUtils.getFloat(
                -scrollY + mFlexibleSpaceImageHeight - fabSize / 2,
                mActionBarSize - fabSize / 2,
                maxFabTranslationY);
        ViewHelper.setTranslationX(mFab, mOverlayView.getWidth() - mFabMargin - fabSize);
        ViewHelper.setTranslationY(mFab, fabTranslationY);

        // Show/hide FAB
        //if (ViewHelper.getTranslationY(mFab) < mFlexibleSpaceShowFabOffset) {
        //    hideFab();
        //} else {
        //    showFab();
        //}

        if (TOOLBAR_IS_STICKY) {
            // Change alpha of toolbar background
            if (-scrollY + mFlexibleSpaceImageHeight <= mActionBarSize) {
                mToolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(1, mToolbarColor));
            } else {
                mToolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(0, mToolbarColor));
            }
        } else {
            // Translate Toolbar
            if (scrollY < mFlexibleSpaceImageHeight) {
                ViewHelper.setTranslationY(mToolbar, 0);
            } else {
                ViewHelper.setTranslationY(mToolbar, -scrollY);
            }
        }
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(1).scaleY(1).setDuration(200).start();
            mFabIsShown = true;
        }
    }

    private void hideFab() {
        if (mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            //ViewPropertyAnimator.animate(mFab).scaleX(0).scaleY(0).setDuration(200).start();
            //ViewPropertyAnimator.animate(mFab).translationY(950).setDuration(200).start();
            mFabIsShown = false;
        }
    }


    public void share(View view) {
        StringBuilder sb = new StringBuilder("Let's go to ");
        switch (title) {
            case "Andrews Commons":
                sb.append("Andrews Commons");
                break;
            case "Blue Room":
                sb.append("the Blue Room");
                break;
            case "Josiah's":
                sb.append("Jo's");
                break;
            case "V-Dub":
                sb.append("the Vdub");
                break;
            case "Ratty":
                sb.append("the Ratty");
                break;
            default:
                sb.append("eat");
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.append(".").toString());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
}
