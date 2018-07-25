package com.appodeal.support.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.InterstitialCallbacks;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeCallbacks;

import java.util.List;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    public static final String EXTRA_mTimerBanner = "mTimerBanner";
    public static final String EXTRA_mTimerInterstitial = "mTimerInterstitial";
    public static final String EXTRA_mTimerBannerRunning = "mTimerBannerRunning";
    public static final String EXTRA_mTimerInterstitialRunning = "mTimerInterstitialRunning";

    private static boolean isFirstShowBanner = true;
    private static boolean isShowInterstitial = true;
    private static boolean isFirstShowInterstitial = true;
    public String APP_KEY;
    private TextView tvTimer;
    private Button btn;
    private CountDownTimer countDownTimerInterstitial;
    private CountDownTimer countDownTimerBanner;

    private int mTimerBanner;
    private int mTimerInterstitial;
    private boolean mTimerBannerRunning = false;
    private boolean mTimerInterstitialRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTimer = findViewById(R.id.timer);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(this);

        APP_KEY = getString(R.string.app_key);
        Appodeal.disableLocationPermissionCheck();
        Appodeal.initialize(this, APP_KEY, Appodeal.INTERSTITIAL | Appodeal.BANNER | Appodeal.NATIVE);

        if (Build.VERSION.SDK_INT >= 23 && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Appodeal.requestAndroidMPermissions(this, new AppodealPermissionCallbacks(this));
        }

        Appodeal.setTesting(true);

        Appodeal.setNativeCallbacks(new NativeCallbacks() {
            @Override
            public void onNativeLoaded() {
                Utils.showToast(MainActivity.this, "onNativeLoaded");
            }

            @Override
            public void onNativeFailedToLoad() {
                Utils.showToast(MainActivity.this, "onNativeFailedToLoad");
            }

            @Override
            public void onNativeShown(NativeAd nativeAd) {
                Utils.showToast(MainActivity.this, "onNativeShown");
            }

            @Override
            public void onNativeClicked(NativeAd nativeAd) {
                Utils.showToast(MainActivity.this, "onNativeClicked");
            }
        });
        Appodeal.setAutoCacheNativeIcons(true);
        Appodeal.setAutoCacheNativeMedia(true);
    }


    @Override
    public void onResume() {
        super.onResume();
        Appodeal.onResume(this, Appodeal.BANNER);
        showAndHideBanner();
        initStaticInterstitial();
    }

    public void showAndHideBanner() {
        Appodeal.setBannerCallbacks(new BannerCallbacks() {
            @Override
            public void onBannerLoaded(int height, boolean isPrecache) {
                Utils.showToast(MainActivity.this, String.format("onBannerLoaded, %sdp, isPrecache: %s", height, isPrecache));
            }

            @Override
            public void onBannerFailedToLoad() {
                Utils.showToast(MainActivity.this, "onBannerFailedToLoad");
            }

            @Override
            public void onBannerShown() {
                Utils.showToast(MainActivity.this, "onBannerShown");
                if (!mTimerBannerRunning) {
                    mTimerBanner = 5;
                }
                countDownTimerBanner = new CountDownTimer(mTimerBanner * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        mTimerBanner = (int) millisUntilFinished / 1000;
                    }

                    @Override
                    public void onFinish() {
                        mTimerBannerRunning = false;
                        Appodeal.hide(MainActivity.this, Appodeal.BANNER);
                    }
                };
                mTimerBannerRunning = true;
                countDownTimerBanner.start();
            }

            @Override
            public void onBannerClicked() {
                Utils.showToast(MainActivity.this, "onBannerClicked");
            }
        });
        if (isFirstShowBanner) {
            Appodeal.show(MainActivity.this, Appodeal.BANNER_TOP);
            isFirstShowBanner = false;
        }
    }

    public void initStaticInterstitial() {
        Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
            @Override
            public void onInterstitialLoaded(boolean isPrecache) {
                Utils.showToast(MainActivity.this, String.format("onInterstitialLoaded, isPrecache: %s", isPrecache));
            }

            @Override
            public void onInterstitialFailedToLoad() {
                Utils.showToast(MainActivity.this, "onInterstitialFailedToLoad");
            }

            @Override
            public void onInterstitialShown() {
                Utils.showToast(MainActivity.this, "onInterstitialShown");
            }

            @Override
            public void onInterstitialClicked() {
                Utils.showToast(MainActivity.this, "onInterstitialClicked");
            }

            @Override
            public void onInterstitialClosed() {
                Utils.showToast(MainActivity.this, "onInterstitialClosed");
                isFirstShowInterstitial = false;
                showStaticInterstitial();
            }
        });
        showStaticInterstitial();
    }

    public void showStaticInterstitial() {
        if (isShowInterstitial) {
            if (!mTimerInterstitialRunning) {
                mTimerInterstitial = 30;
            }
            countDownTimerInterstitial = new CountDownTimer(mTimerInterstitial * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mTimerInterstitial = (int) millisUntilFinished / 1000;
                    tvTimer.setText(String.valueOf(mTimerInterstitial) + " seconds");
                }

                @Override
                public void onFinish() {
                    mTimerInterstitialRunning = false;
                    Appodeal.show(MainActivity.this, Appodeal.INTERSTITIAL);
                }
            };
            mTimerInterstitialRunning = true;
            countDownTimerInterstitial.start();
        }
    }

    public void showNativeButton(View v) {
        hideNative();
        List<NativeAd> nativeAds = Appodeal.getNativeAds(1);

        LinearLayout nativeAdsListView = findViewById(R.id.nativeAdsListView);
        NativeListAdapter nativeListViewAdapter = new NativeListAdapter(nativeAdsListView, 1);
        for (NativeAd nativeAd : nativeAds) {
            nativeListViewAdapter.addNativeAd(nativeAd);
        }
        nativeAdsListView.setTag(nativeListViewAdapter);
        nativeListViewAdapter.rebuild();
    }

    public void hideNativeButton(View v) {
        hideNative();
    }

    public void hideNative() {
        LinearLayout nativeListView = findViewById(R.id.nativeAdsListView);
        nativeListView.removeAllViews();
        NativeListAdapter nativeListViewAdapter = (NativeListAdapter) nativeListView.getTag();
        if (nativeListViewAdapter != null) {
            for (int i = 0; i < nativeListViewAdapter.getCount(); i++) {
                NativeAd nativeAd = (NativeAd) nativeListViewAdapter.getItem(i);
                nativeAd.unregisterViewForInteraction();
            }
            nativeListViewAdapter.clear();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn:
                if (isFirstShowInterstitial && isShowInterstitial) {
                    isShowInterstitial = false;
                    mTimerInterstitialRunning = false;
                    countDownTimerInterstitial.cancel();
                    tvTimer.setText("Canceled");
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (countDownTimerBanner != null) {
            countDownTimerBanner.cancel();
        }
        if (countDownTimerInterstitial != null) {
            countDownTimerInterstitial.cancel();
        }
        outState.putInt(EXTRA_mTimerBanner, mTimerBanner);
        outState.putInt(EXTRA_mTimerInterstitial, mTimerInterstitial);
        outState.putBoolean(EXTRA_mTimerBannerRunning, mTimerBannerRunning);
        outState.putBoolean(EXTRA_mTimerInterstitialRunning, mTimerInterstitialRunning);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        mTimerBanner = savedInstanceState.getInt(EXTRA_mTimerBanner);
        mTimerInterstitial = savedInstanceState.getInt(EXTRA_mTimerInterstitial);
        mTimerBannerRunning = savedInstanceState.getBoolean(EXTRA_mTimerBannerRunning);
        mTimerInterstitialRunning = savedInstanceState.getBoolean(EXTRA_mTimerInterstitialRunning);
    }
}
