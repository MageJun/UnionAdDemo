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


/**
 *
 * 信息流广告
 * ----------注意事项----------
 *
 * 1、如果开发者需要缓存自渲染信息流数据，必须对缓存数据进行有效期的控制(缓存时长具体和运营沟通，一般最长支持45分钟)，一旦超过最大缓存时长则不能再次进行曝光；
 *
 * 2、开发者通过自渲染数据UnifiedAd构建完广告View之后，
 *
 *    <<<<<<<bindAdToView>>>>>>>，否则会导致无点击事件等异常，
 *
 * 3、同时开发者需要将 bindAdToView 接口所返回的 View 进行展示，
 *
 *    <<<<<<<而不是开发者自己构建的广告 View>>>>>>>;
 *
 * 4、如果使用的是广点通广告，开发者使用 UnifiedAd 所构造出的广告 View，这个View及其子View中
 *
 *    <<<<<<<不能包含 com.qq.e.ads.nativ.widget.NativeAdContainer>>>>>>>，否则会影响点击和计费
 *
 * 5、自渲染信息流发起请求示例，构建builder直接使用applicationContext
 *
 * 6、AdRequest 每次请求对应一个新的 new AdRequest 本次请求一旦释放，将不能再次使用，必须重新 new 否则会出现运行时异常.
 */
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
        AdRequest adRequest = AdClient.makeAdRequestBuilder(this)//展示广告的Activity
                .setPlacementId("D2110010")//广告ID
                .setAdCount(1)//广告请求数
                .setType(AdType.INFORMATION_FLOW)//广告类型
                .setUnifiedAdListener(this)//广告状态监听器
                .build();
        AdClient.loadAd(adRequest);
    }

    void showAd(UnifiedAd ad){
        if(ad == null){
            return;
        }
        unifiedAd = ad;
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