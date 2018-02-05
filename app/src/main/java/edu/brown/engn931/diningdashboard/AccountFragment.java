package edu.brown.engn931.diningdashboard;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


public class AccountFragment extends Fragment {

    WebView webView;
    ProgressBar webProgress;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        setupWebview(view);
        return view;
    }

    public void setupWebview(View view) {
        webProgress = (ProgressBar) view.findViewById(R.id.webProgress);
        webView = (WebView) view.findViewById(R.id.accountWebview);
        //webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://get.cbord.com/brown/full/funds_home.php");
        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && webProgress.getVisibility() == ProgressBar.GONE) {
                    webProgress.setVisibility(ProgressBar.VISIBLE);
                }
                webProgress.setProgress(progress);
                if (progress == 100) {
                    webProgress.setVisibility(ProgressBar.GONE);
                }
            }
        });

    }


}
