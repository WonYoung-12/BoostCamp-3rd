package com.example.kwy2868.boostcamp_3rd.View;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kwy2868.boostcamp_3rd.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity{
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    // 프래그먼트 전환하면서 바꿔줄 부분.
    @BindView(R.id.fragment_show)
    FrameLayout fragment_show;
    @BindView(R.id.close)
    TextView closeButton;

    private static final int REQUEST_CODE = 0;
    // 이거 재사용 해보자.
    private static GoogleMapFragment googleMapFragment;
    // 이거도 재사용 해보자.
    private static EnrollFragment enrollFragment;

    private FragmentManager fragmentManager;
    private Fragment fragment;
    private FragmentTransaction fragmentTransaction;

    private static final int FRAGMENT_CONTATINER = R.id.fragment_show;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 버터나이프와 툴바, 첫 프래그먼트 부착등의 초기 작업.
        init();
    }

    public void init(){
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.project_name);
        attachEnrollFragment();
    }

    // 이거 오버라이딩해서 하면 바로 종료 안시키고 토스트 메시지 띄우고 잘 되지않을까.
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.close)
    public void finishApp(){
        finish();
    }

    // 사용자가 무엇을 선택하던지 실행하게 되는 메소드.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // requestPermission 메소드의 requestCode와 일치하는지 확인.
        if(requestCode == REQUEST_CODE){
            Log.d("퍼미션 요구", "퍼미션 요구");
            // 요구하는 퍼미션이 한개이기 때문에 하나만 확인한다.
            // 해당 퍼미션이 승낙된 경우.
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("퍼미션 승인", "퍼미션 승인");
                if(googleMapFragment != null)
                    googleMapFragment.permissonCheck();
            }
            // 해당 퍼미션이 거절된 경우.
            else{
                Log.d("퍼미션 거절", "퍼미션 거절");
                Toast.makeText(this, "퍼미션을 승인 해주셔야 이용이 가능합니다", Toast.LENGTH_SHORT).show();
                finish();

                // 앱 정보 화면을 통해 퍼미션을 다시 요구해보자.
                requestPermissionInSettings();
            }
        }
    }

    public void attachEnrollFragment() {
        // 프래그먼트 붙였다 뗐다.
        fragmentManager = getSupportFragmentManager();
        // 부착되어 있는 프래그먼트를 확인.
        // 여기가 널 떠.
        fragment = fragmentManager.findFragmentById(FRAGMENT_CONTATINER);
        // 당연히 아무것도 안 붙어 있어야지.
//        Log.d("Fragment 잘 있나", fragment+" ");

        fragmentTransaction = fragmentManager.beginTransaction();

//        googleMapFragment = new GoogleMapFragment();
        enrollFragment = new EnrollFragment();

        // 배열에 담아 놓고 나중에 리턴해줄까?
//        fragmentArray[MAP_FRAGMENT] = googleMapFragment;

        fragmentTransaction.add(FRAGMENT_CONTATINER, enrollFragment);
        fragmentTransaction.commit();
    }

    // 사용자에게 설정 창으로 넘어가게 하여 퍼미션 설정하도록 유도.
    public void requestPermissionInSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, REQUEST_CODE);
    }
}