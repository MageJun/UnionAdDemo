package com.unionad.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.unionad.sdk.ad.AdClient;
import com.unionad.sdk.ad.AdError;
import com.unionad.sdk.ad.AdRequest;
import com.unionad.sdk.ad.AdType;
import com.unionad.sdk.ad.splash.SplashAd;
import com.unionad.sdk.ad.splash.SplashAdListener;

import java.util.ArrayList;
import java.util.List;

/**
 * ----------注意事项----------
 * <p>
 * 聚合SDK请求开屏的时间通常在500ms-1.5S内(和当前网络有关),
 * 请尽量保证聚合SDK的请求时间充足，
 * 所以开发者应用初始化到聚合SDK开始请求这之间的逻辑一定要尽快完成，
 * 这样才能保证SDK请求时间充足，也能提高填充率和收益。
 * <p>
 * 1、开发者通常会定义从应用初始化到开屏广告返回的总超时时间，这个总超时时间太短的话就会影响收益；
 *
 * 2、开屏广告有可能部分素材展示不全，SDK只对素材进行裁剪不进行压缩；
 *
 * 3、开屏广告要达到1S以上才算是进行了曝光；
 *
 * 4、开屏广告的显示区域其高度一定要大于设备高度的75%
 *
 * 5、开屏View不能进行缓存，每次开屏需要都需要发起新的请求去获取;
 */
public class SplashADActivity extends BaseActivity implements SplashAdListener {
    /**
     * canJump字段，用来控制点击落地页广告时，跳转主页面的时机
     *
     */
    public boolean canJump = false;
    private boolean isLoadOnly = false;
    private SplashAd mSplashAd;
    private boolean canBack = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_ad);
        setTitle("开屏广告");
        showControllView();
    }

    @Override
    protected void onBtnLoadClick() {
        super.onBtnLoadClick();
        isLoadOnly = true;
        loadAd();
    }

    @Override
    protected void onBtnLoadShowClick() {
        super.onBtnLoadShowClick();
        isLoadOnly = false;
        loadAd();
    }

    @Override
    protected void onBtnShowClick() {
        super.onBtnShowClick();
        if(mSplashAd!=null){
            dismissControllView();
            dismissTitleView();
            mSplashAd.show();
        }

    }

    private void loadAd(){
        if (Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermission();
        } else {
            // 如果是Android6.0以下的机器，默认在安装时获得了所有权限，可以直接调用SDK
            fetchSplashAD();
        }
    }

    void fetchSplashAD(){
        findViewById(R.id.skip).setVisibility(View.GONE);
        AdRequest adRequest = AdClient.makeAdRequestBuilder(this)//展示广告的activity
                .setSplashAdContainer(findViewById(R.id.adContainer)) //展示广告的大容器
                .setPlacementId("D2110001")// 广告位ID
                .setSpalshLoadAdOnly(isLoadOnly)//是否请求和展示分离
                .setType(AdType.SPLASH)//广告类型
                .setSplashAdListener(this)//开屏广告状态监听器
                .build();
        AdClient.loadAd(adRequest);
    }

    /**
     *
     * ----------非常重要----------
     *
     * Android6.0以上的权限适配简单示例：
     *
     * 如果targetSDKVersion >= 23，那么建议动态申请相关权限，再调用SDK
     *
     * SDK不强制校验下列权限（即:无下面权限sdk也可正常工作），但建议开发者申请下面权限，尤其是READ_PHONE_STATE权限
     *
     * READ_PHONE_STATE权限用于允许SDK获取用户标识,
     * 针对单媒体的用户，允许获取权限的，投放定向广告；不允许获取权限的用户，投放通投广告，媒体可以选择是否把用户标识数据提供给优量汇，并承担相应广告填充和eCPM单价下降损失的结果。
     *
     * Demo代码里是一个基本的权限申请示例，请开发者根据自己的场景合理地编写这部分代码来实现权限申请。
     * 注意：下面的`checkSelfPermission`和`requestPermissions`方法都是在Android6.0的SDK中增加的API，如果您的App还没有适配到Android6.0以上，则不需要调用这些方法，直接调用广点通SDK即可。
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        List<String> lackedPermission = new ArrayList<String>();
        if (!(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // 权限都已经有了，那么直接调用SDK
        if (lackedPermission.size() == 0) {
            fetchSplashAD();
        } else {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用SDK，否则不要调用SDK。
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, 1024);
        }
    }
    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1024 && hasAllPermissionsGranted(grantResults)) {
            fetchSplashAD();
        } else {
            // 如果用户没有授权，那么应该说明意图，引导用户去设置里面授权。
            Toast.makeText(this, "应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            finish();
        }
    }


    private void next() {
        if (canJump) {

            this.finish();
        } else {
            canJump = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        canJump = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJump) {
            next();
        }
        canJump = true;
    }



    /** 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            if(canBack){
                return  super.onKeyDown(keyCode,event);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     * ----------非常重要----------
     *   这个回调是一定触发的  可以在这里 finish.Splash Go to MainActivity
     *
     *   这个回调代码广告流程的结束，在这之前不允许任何释放关闭广告的操作！！！
     */
    @Override
    public void onAdDismissed() {
        showTolast("广告关闭");
        next();
    }
    /**
     * ----------非常重要 针对所有广告----------
     *   广告点击之后会触发
     *   开发过程中一定要确保 点击广告时候有这个回调 否则会影响收益
     *
     *   媒体可以在该方法中统计广告点击记录
     *
     */
    @Override
    public void onAdClicked() {
        Toast.makeText(this, "广告被点击", Toast.LENGTH_SHORT).show();
    }
    /**
     * ----------非常重要 针对所有广告----------
     *   广告曝光
     *
     *   媒体自己的曝光统计也要在这个地方做
     *
     *   开发过程中一定要确保有这个回调 否则会影响收益
     *   测试时候一定要看这个
     */
    @Override
    public void onAdExposed() {
        canBack = false;
        Toast.makeText(this, "广告展示成功", Toast.LENGTH_SHORT).show();
    }
    /**
     *广告倒计时回调
     */
    @Override
    public void onAdTick(long millisUntilFinished) {
        ((TextView)findViewById(R.id.skip)).setText("跳过 " + Math.round(millisUntilFinished/1000f));
    }


    /**
     *
     * 广告数据加载成功，
     *
     * 该方法回调表示广告请求成功，
     *
     * 媒体可以在这个方法中统计广告请求成功记录，
     *
     * 同时媒体自己的请求超时倒计时可以在这个方法中关闭
     *
     */
    @Override
    public void onAdLoaded(List<SplashAd> ads) {
        showTolast("广告加载完成");
        if(!isLoadOnly){
            dismissControllView();
            dismissTitleView();
        }
        if(ads!=null && ads.size()>0){
            mSplashAd = ads.get(0);
        }
    }
    /**
     * 常见错误
     *      4004	开屏广告容器不可见 (99%原因是上述 <注意事项> 中提到 超时时间过短 拉取到广告来不及展示已经跳过)
     *      5004、102006	广告无填充
     * @param error
     */
    @Override
    public void onAdError(AdError error) {
        Log.e("SPLASHDEMO", error.toString());
        showTolast("广告发生错误：" + error.getCode() + " " + error.getMessage());
        next();
    }
}