package com.unionad.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;


public class BaseActivity extends Activity {

    private ViewGroup rootContenView;
    private View controllLayout,titleLayout;
    private Button btnLoad,btnShow,btnLoadAndShow;
    private TextView baseTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);
        askFullScreen();
        initRootView();
        dismissControllView();
    }

    private void initRootView() {
        rootContenView = findViewById(R.id.root_content);
        controllLayout = findViewById(R.id.load_only_view);
        titleLayout = findViewById(R.id.base_title_layout);
        baseTitle = findViewById(R.id.base_title);
        btnLoad = findViewById(R.id.btn_loadOnly);
        btnShow = findViewById(R.id.btn_show);
        btnLoadAndShow = findViewById(R.id.btn_loadAndShow);
    }

    void askFullScreen(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater.from(this).inflate(layoutResID,rootContenView);
    }

    public void onBaseClick(View view){
        switch (view.getId()) {
            case R.id.btn_loadOnly :
                onBtnLoadClick();
                break;
            case R.id.btn_show:
                onBtnShowClick();
                break;

            case R.id.btn_loadAndShow:
                onBtnLoadShowClick();
                break;
        }
    }

    protected void onBtnLoadClick(){

    }
    protected void onBtnShowClick(){

    }
    protected void onBtnLoadShowClick(){

    }

    @Override
    protected void onResume() {
        super.onResume();
        askFullScreen();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(MotionEvent.ACTION_DOWN == event.getAction()){
            askFullScreen();
        }
        return super.onTouchEvent(event);
    }

    protected void setTitle(String title){
        titleLayout.setVisibility(View.VISIBLE);
        baseTitle.setText(title);
    }

    protected void dismissControllView(){
        controllLayout.setVisibility(View.GONE);
    }
    protected void dismissTitleView(){
        titleLayout.setVisibility(View.GONE);
    }

    protected void dismissLoadAndShowBtn(){
        btnLoadAndShow.setVisibility(View.GONE);
    }
    protected void showControllView(){
        controllLayout.setVisibility(View.VISIBLE);
    }

    protected void showTolast(String msg){
        if(!TextUtils.isEmpty(msg)){
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
        }
    }
}
