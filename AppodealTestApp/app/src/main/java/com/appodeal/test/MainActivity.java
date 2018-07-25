package com.appodeal.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.NativeAd;
import com.appodeal.test.layout.AdTypeViewPager;
import com.appodeal.test.layout.SlidingTabLayout;

import java.util.List;

public class MainActivity extends FragmentActivity {

    public String APP_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        APP_KEY = getString(R.string.app_key);
        Appodeal.disableLocationPermissionCheck();
        Appodeal.initialize(this, APP_KEY, Appodeal.NATIVE | Appodeal.BANNER | Appodeal.REWARDED_VIDEO);

        if (Build.VERSION.SDK_INT >= 23 && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Appodeal.requestAndroidMPermissions(this, new AppodealPermissionCallbacks(this));
        }

        Appodeal.setTesting(true);

        Appodeal.setRewardedVideoCallbacks(new AppodealRewardedVideoCallbacks(this));

        Appodeal.setBannerViewId(R.id.appodealBannerView);
        Appodeal.setBannerCallbacks(new AppodealBannerCallbacks(this));

        Appodeal.setNativeCallbacks(new AppodealNativeCallbacks(this));
        Appodeal.setAutoCacheNativeIcons(true);
        Appodeal.setAutoCacheNativeMedia(true);

        ViewPager pager = (AdTypeViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(AdTypePages.values().length);
        pager.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if (child.findViewById(AdTypePages.RVideo.getId()) != null && child.getTag() == null) {
                    child.setTag(true);
                }

                if (child.findViewById(AdTypePages.Native.getId()) != null && child.getTag() == null) {
                    child.setTag(true);
                }

                if (child.findViewById(AdTypePages.Banner.getId()) != null && child.getTag() == null) {
                    child.setTag(true);
                }

            }

            @Override
            public void onChildViewRemoved(View parent, View child) {

            }
        });
        AdTypeAdapter adTypeAdapter = new AdTypeAdapter(getSupportFragmentManager());
        pager.setAdapter(adTypeAdapter);

        SlidingTabLayout slidingTabLayout = findViewById(R.id.slidingTabLayout);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(pager);
    }

    @Override
    public void onResume() {
        super.onResume();
        Appodeal.onResume(this, Appodeal.BANNER);
    }

    @Override
    public void onBackPressed() {
        ViewGroup root = findViewById(android.R.id.content);
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag.equals("appodeal")) {
                root.removeView(child);
                return;
            }
        }
        super.onBackPressed();
    }

    public void isRewardedVideoLoadedButton(View v) {
        if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {
            Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "false", Toast.LENGTH_SHORT).show();
        }
    }

    public void rewardedVideoShowButton(View v) {
        boolean isShown = Appodeal.show(this, Appodeal.REWARDED_VIDEO);
        Toast.makeText(this, String.valueOf(isShown), Toast.LENGTH_SHORT).show();
    }

    public void isBannerLoadedButton(View v) {
        if (Appodeal.isLoaded(Appodeal.BANNER)) {
            Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "false", Toast.LENGTH_SHORT).show();
        }
    }

    public void bannerShowButton(View v) {
        boolean isShown = Appodeal.show(this, Appodeal.BANNER_BOTTOM);
        Toast.makeText(this, String.valueOf(isShown), Toast.LENGTH_SHORT).show();
    }

    public void bannerHideButton(View v) {
        Appodeal.hide(this, Appodeal.BANNER);
    }

    public void nativeHideButton(View v) {
        hideNativeAds();
    }

    public void hideNativeAds() {
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

    public void isNativeLoadedButton(View v) {
        if (Appodeal.isLoaded(Appodeal.NATIVE)) {
            Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "false", Toast.LENGTH_SHORT).show();
        }
    }

    public void nativeShowButton(View v) {
        hideNativeAds();
        List<NativeAd> nativeAds = Appodeal.getNativeAds(1);

        LinearLayout nativeAdsListView = findViewById(R.id.nativeAdsListView);
        NativeListAdapter nativeListViewAdapter = new NativeListAdapter(nativeAdsListView, 1);
        for (NativeAd nativeAd : nativeAds) {
            nativeListViewAdapter.addNativeAd(nativeAd);
        }
        nativeAdsListView.setTag(nativeListViewAdapter);
        nativeListViewAdapter.rebuild();
    }

    public enum AdTypePages {
        Native(R.layout.native_ad, R.id.nativeLayout), Banner(R.layout.banner, R.id.bannerLayout),
        RVideo(R.layout.rewarded_video, R.id.rewardedVideoLayout);

        private final int mLayout;
        private final int mId;

        AdTypePages(int layout, int id) {
            mLayout = layout;
            mId = id;
        }

        public int getLayout() {
            return mLayout;
        }

        public int getId() {
            return mId;
        }
    }

    public static class AdTypeAdapter extends FragmentPagerAdapter {

        AdTypeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return AdTypePages.values().length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new AdTypeFragment();
            Bundle args = new Bundle();
            args.putInt("layout", AdTypePages.values()[position].getLayout());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return AdTypePages.values()[position].name();
        }
    }

    public static class AdTypeFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle args = getArguments();
            int layoutId = args.getInt("layout");
            return inflater.inflate(layoutId, container, false);
        }
    }
}