package com.unionad.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.unionad.sdk.ad.AdClient;
import com.unionad.sdk.ad.AdError;
import com.unionad.sdk.ad.AdRequest;
import com.unionad.sdk.ad.AdType;
import com.unionad.sdk.ad.feedlist.UnifiedAd;
import com.unionad.sdk.ad.feedlist.UnifiedAdListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UnifiedActivity extends BaseActivity implements UnifiedAdListener {
    private UnifiedAd unifiedAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified);
        setTitle("信息流广告");
        dismissLoadAndShowBtn();
        showControllView();
    }
    @Override
    protected void onBtnLoadClick() {
        super.onBtnLoadClick();
        loadAd();
    }

    @Override
    protected void onBtnShowClick(){
        super.onBtnShowClick();
        showAd(unifiedAd);
    }

    void loadAd(){
        AdRequest adRequest = AdClient.makeAdRequestBuilder(this)
                .setAdCount(1)
                .setUnifiedAdListener(this)
                .setType(AdType.INFORMATION_FLOW)
                .setPlacementId("D2110010")
                .build();
        AdClient.loadAd(adRequest);
    }

    void showAd(UnifiedAd ad){
        if(ad == null){
            return;
        }
        unifiedAd = ad;
        Log.e("UNIFIED","ecpm = "+unifiedAd.getExtraData().get("ecpm"));
        ViewGroup adContainer = findViewById(R.id.adContainer);
        ViewGroup adView = findViewById(R.id.adView);
        ImageView imageView = findViewById(R.id.ad_image);
        TextView textView = findViewById(R.id.ad_title);
        adContainer.removeAllViews();
        textView.setText(ad.getTitle());
        List<View> clickViews = new ArrayList<>();
        clickViews.add(adView);
        clickViews.add(imageView);
        clickViews.add(textView);
        View  view = ad.bindAdToView(this, adView, null, clickViews, findViewById(R.id.skip), new UnifiedAd.AdEventListener() {
            @Override
            public void onAdExposed() {
                Toast.makeText(UnifiedActivity.this, "广告展示成功！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClicked() {
                Toast.makeText(UnifiedActivity.this, "广告被点击", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdError(AdError error) {
                Toast.makeText(UnifiedActivity.this, "展示广告失败", Toast.LENGTH_SHORT).show();
                Log.e("UNIFIED","show ad error: " + error.toString());
            }
        });
        adContainer.addView(view);
        new Thread(()->{
            try {
                //Log.e("DEBUG", ad.getImageUrl());
                URL url = new URL(ad.getImageUrl());
                Log.e("DD", url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.getDoInput();
                conn.connect();
                Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
                new Handler(Looper.getMainLooper()).post(()->{
                   imageView.setImageBitmap(bitmap);
                });

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Override
    public void onAdLoaded(List<UnifiedAd> ads) {
        Toast.makeText(this, "加载广告成功！", Toast.LENGTH_SHORT).show();
        if(ads != null && ads.size() > 0){
            unifiedAd = ads.get(0);
        }
    }

    @Override
    public void onAdError(AdError error) {
        Toast.makeText(this, "加载广告失败", Toast.LENGTH_SHORT).show();
        Log.e("UNIFIED", "onAdError:" + error.toString());
    }
}