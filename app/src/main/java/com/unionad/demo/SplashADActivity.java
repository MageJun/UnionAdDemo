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

public class SplashADActivity extends BaseActivity implements SplashAdListener {
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
        AdRequest adRequest = AdClient.makeAdRequestBuilder(MainActivity.sContext)
                .setPlacementId("D2110001")
                .setSpalshLoadAdOnly(isLoadOnly)
                .setSplashAdContainer(findViewById(R.id.adContainer))
                //.setSplashSkipView(findViewById(R.id.skip))
                .setType(AdType.SPLASH)
                .setSplashAdListener(this)
                .build();
        AdClient.loadAd(adRequest);
    }
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


    void moveOutOfFullScreen(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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

    @Override
    public void onAdDismissed() {
        showTolast("广告关闭");
        next();
    }
    /*
    @Override
    public void onAdPresent() {
        Toast.makeText(this, "广告开始展示", Toast.LENGTH_SHORT).show();
    }

     */

    @Override
    public void onAdClicked() {
        Toast.makeText(this, "广告被点击", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdExposed() {
        canBack = false;
        Toast.makeText(this, "广告展示成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdTick(long millisUntilFinished) {
        ((TextView)findViewById(R.id.skip)).setText("跳过 " + Math.round(millisUntilFinished/1000f));
    }



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

    @Override
    public void onAdError(AdError error) {
        Log.e("SPLASHDEMO", error.toString());
        showTolast("广告发生错误：" + error.getCode() + " " + error.getMessage());
        next();
    }
}