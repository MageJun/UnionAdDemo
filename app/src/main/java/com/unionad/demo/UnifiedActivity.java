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
import com.unionad.sdk.ad.video.UnifiedAdVideoListener;
import com.unionad.sdk.ad.video.UnifiedAdVideoView;
import com.unionad.sdk.ad.video.UnifiedVideoOptions;

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

    /**
     * 特别重要！！！
     * 要在Activity#onResu方法中，调用UnifiedAd#resume方法，
     * 防止点击之后广告状态错乱
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(unifiedAd!=null){
            unifiedAd.resume();
        }
    }
    void loadAd(){
        AdRequest adRequest = AdClient.makeAdRequestBuilder(this)//展示广告的Activity
                .setPlacementId("D2110019")//广告ID 测试ID：视频信息流——D2110002；图文信息流——D2110010
                .setAdCount(1)//广告请求数
                .setType(AdType.INFORMATION_FLOW)//广告类型
                .setUnifiedAdListener(this)//广告状态监听器
                .setVideoOptions(getVideoOptions())
                .build();
        AdClient.loadAd(adRequest);
    }


    private UnifiedVideoOptions getVideoOptions() {
        UnifiedVideoOptions.Builder options =new  UnifiedVideoOptions.Builder();
        options.setAutoPlayMuted(true);//默认true，是否静音播放
        options.setAutoPlayPolicy(UnifiedVideoOptions.AutoPlayPolicy.ALWAYS);//自动播放策略,任何情况下/WIFI情况下/从不，默认任何情况下自动播放
        options.setEnableDetailPage(true);//点击是否跳转详情预览页，设置false直接触发点击事件，开始下载或者打开落地页
        options.setDetailPageMuted(true);//视频详情页是否默认静音
        options.setEnableUserControl(true);//设置是否允许用户在预览页点击视频播放器区域控制视频的暂停或播放，默认为false，用户点击时的表现与点击clickableViews一致；如果为true，用户点击时将收到NativeADMediaListener.onVideoClicked回调，而不是NativeADEventListener.onADClicked回调，因为此时并不是广告点击
        options.setNeedCoverImage(true);//设置是否显示封面，默认true
        options.setNeedProgressBar(true);//设置是否显示进度条，默认true
        return options.build();
    }

    void showAd(UnifiedAd ad){
        if(ad == null){
            return;
        }
        unifiedAd = ad;
        ViewGroup adContainer = findViewById(R.id.adContainer);
        UnifiedAdVideoView mediaView = findViewById(R.id.media_view);
        ViewGroup adView = findViewById(R.id.adView);
        ImageView imageView = findViewById(R.id.ad_image);
        TextView textView = findViewById(R.id.ad_title);
        adContainer.removeAllViews();
        imageView.setVisibility(View.GONE);
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
        if(ad.isVideoAd()){
            Log.e("UNIFIED","video Ad");
            ad.bindMediaAdToView(mediaView, new UnifiedAdVideoListener() {
                @Override
                public void onVideoInit() {

                }

                @Override
                public void onVideoLoading() {

                }

                @Override
                public void onVideoReady() {

                }

                @Override
                public void onVideoLoaded(int videoDuration) {

                }

                @Override
                public void onVideoStart() {

                }

                @Override
                public void onVideoPause() {

                }

                @Override
                public void onVideoResume() {

                }

                @Override
                public void onVideoCompleted() {

                }

                @Override
                public void onVideoError(AdError var1) {

                }

                @Override
                public void onVideoStop() {

                }

                @Override
                public void onVideoClicked() {

                }
            });
        }else{
            imageView.setVisibility(View.VISIBLE);
        }
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