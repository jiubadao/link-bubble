package com.chrislacy.linkload;

import android.animation.ObjectAnimator;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.jawsware.core.share.OverlayService;
import com.jawsware.core.share.OverlayView;

/**
 * Created with IntelliJ IDEA.
 * User: chrislacy
 * Date: 5/1/2013
 * Time: 10:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class LinkViewContentView extends OverlayView {

    LinkViewOverlayService mService;
    private View mContentView;
    private ContentWebView mWebView;

    enum LoadingState {
        NotSet,
        Loading,
        Loaded,
    }

    private LoadingState mLoadingState;
    private Uri mUri;

    public LinkViewContentView(OverlayService service) {
        super(service, R.layout.content, 1);
        mLoadingState = LoadingState.NotSet;
        mService = (LinkViewOverlayService) service;
    }

    public int getGravity() {
        return Gravity.TOP + Gravity.RIGHT;
    }

    @Override
    protected void onInflateView() {
        mWebView = (ContentWebView)findViewById(R.id.web_view);
        mWebView.setOnKeyDownListener(new ContentWebView.OnKeyDownListener() {

            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_BACK == keyCode) {
                    //setLoadingState(LoadingState.Loading);
                    mService.showLoading();
                    return true;
                }
                return false;
            }
        });

        mContentView = findViewById(R.id.content);

        ImageView closeButton = (ImageView) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkViewOverlayService.stop();
            }
        });

    }

    static final Interpolator DECELERATE_CUBIC = new DecelerateInterpolator(1.5f);

    void setLoadingState(LoadingState loadingState) {

        if (mLoadingState != loadingState) {
            mLoadingState = loadingState;

            switch (mLoadingState) {
                case Loading:
                    mContentView.setVisibility(View.INVISIBLE);
                    if (LinkViewOverlayService.mInstance != null) {
                        LinkViewOverlayService.mInstance.endAppPolling();
                    }
                    break;

                case Loaded:
                    mContentView.setVisibility(View.VISIBLE);
                    if (LinkViewOverlayService.mInstance != null) {
                        LinkViewOverlayService.mInstance.beginAppPolling(new LinkViewOverlayService.AppPollingListener() {
                            @Override
                            public void onAppChanged() {
                                setLoadingState(LoadingState.Loading);
                            }
                        });
                    }

                    WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                    Display display = windowManager.getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;

                    ObjectAnimator animator = ObjectAnimator.ofFloat(mContentView, "x", width, 0);
                    animator.setDuration(300);
                    animator.setInterpolator(DECELERATE_CUBIC);
                    animator.start();

                    break;
            }

            updateViewLayout();
        }
    }

    public void setUri(Uri uri) {
        mUri = uri;

        mLoadingState = LoadingState.Loading;

        /*
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(uri.toString());
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {

                mLoadingState = LoadingState.Loaded;
                mContentView.setVisibility(View.VISIBLE);
                mLoadingView.setVisibility(View.INVISIBLE);

                updateViewLayout();

                LinkViewOverlayService.mInstance.cancelNotification();

                Intent intent = new Intent(LinkViewOverlayService.mInstance.getApplication(), LinkViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra(LinkViewActivity.LINK_VIEW_URL, url);
                LinkViewOverlayService.mInstance.getApplication().startActivity(intent);

                LinkViewOverlayService.stop();

                mWebView.stopLoading();


                //updateViewLayout();

                //int delay = 5000;


//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mLoadingState = LoadingState.Loaded;
//                        mContentView.setVisibility(View.VISIBLE);
//                        mLoadingView.setVisibility(View.INVISIBLE);
//
//                        updateViewLayout();
//
//                        WebReaderOverlayService.mInstance.cancelNotification();
//                    }
//                }, 0);

            }

            public boolean shouldOverrideUrlLoading(WebView view, String url){

                PackageManager packageManager = getContext().getPackageManager();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                final ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);
                if (resolveInfo != null && resolveInfo.activityInfo != null) {
                    String name = resolveInfo.activityInfo.name;
                    if (//!name.contains("com.android.internal")
                        //    && !name.contains("ResolverActivity")
                        //    && !name.contains("com.chrislacy.linkload")) {
                        !name.contains("com.chrislacy.linkload")) {
                        ComponentName componentName = new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName, name);
                        intent.setComponent(componentName);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        LinkViewOverlayService.mInstance.getApplication().startActivity(intent);
                        LinkViewOverlayService.stop();
                        return true;
                    }
                    // TODO: Hard-code for YouTube, Instragram, Facebook and Twitter
                }

                view.loadUrl(url);
                return false; // then it is not handled by default action
            }
        });
        */
    }

    @Override
    protected void onSetupLayoutParams() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width;
        int height;
        int flags;
        if (mLoadingState == LoadingState.Loaded) {
            // TODO: Come up with something better here
            height = (int) (size.y - Utilities.convertDpToPixel(24, getContext())) - 300;
            width = size.x;
            mContentView.setVisibility(View.VISIBLE);
            flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        } else {
            width = getResources().getDimensionPixelSize(R.dimen.loading_content_width);
            height = getResources().getDimensionPixelSize(R.dimen.loading_content_height);
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }

        layoutParams = new WindowManager.LayoutParams(width, height, WindowManager.LayoutParams.TYPE_PHONE, flags, PixelFormat.TRANSLUCENT);
        //layoutParams.layoutAnimationParameters
        layoutParams.gravity = getDefaultLayoutGravity();
    }

}
