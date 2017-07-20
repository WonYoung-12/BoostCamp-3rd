package com.example.kwy2868.boostcamp_3rd.View;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kwy2868.boostcamp_3rd.DB.DBHelper;
import com.example.kwy2868.boostcamp_3rd.MapClickListener;
import com.example.kwy2868.boostcamp_3rd.Model.Restaurant;
import com.example.kwy2868.boostcamp_3rd.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;


/**
 * Created by kwy2868 on 2017-07-17.
 */

public class GoogleMapFragment extends Fragment
        implements OnMapReadyCallback, MapClickListener {

    private static final int REQUEST_CODE = 0;
    private static final String TAG = "GoogleMapFragment";

    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;

    private TextView mapText;

    private static final int FRAGMENT_CONTATINER = R.id.fragment_show;

    // 프래그먼트 전환 되었을 때 사용하는 변수.
    private UiSettings uiSettings;

    private Geocoder geocoder;

    private static DBHelper dbHelper;


    public GoogleMapFragment() {
    }

    public static GoogleMapFragment newInstance(Restaurant restaurant) {
        GoogleMapFragment googleMapFragment = new GoogleMapFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("RESTAURANT", restaurant);
        googleMapFragment.setArguments(bundle);

        return googleMapFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapText = view.findViewById(R.id.map_text);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        geocoder = new Geocoder(getContext(), Locale.KOREAN);
        dbHelper = new DBHelper(getContext(), null, null, 1);

        if (getArguments() != null) {
            Restaurant restaurant = getArguments().getParcelable("RESTAURANT");
            // 입력한 주소를 지오코딩으로 변환이 가능한지 체크한다.
            existCheckLocation(restaurant);
        }
        mapSetting();
    }

    public void mapSetting() {
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
//        Log.d("MapFragment", mapFragment + "");
        mapFragment.getMapAsync(this);
    }

    public void existCheckLocation(Restaurant restaurant) {
        String restaurantAddress = null;

        if (restaurant != null) {
            restaurantAddress = restaurant.getAddress();
        } else
            return;

        try {
            List addressList = geocoder.getFromLocationName(restaurantAddress, 5);

            // 지오코딩을 통해 가져온 게 있으면 제일 앞의 것을 써주자.
            if (addressList.size() > 0) {
                dbHelper.addRestaurant(restaurant);
            }
            // 지오코딩으로 가져오는게 없을 때.
            else {
                Toast.makeText(getContext(), "지오코딩 변환 실패, 가장 마지막에 등록한 맛집으로 이동합니다.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("Error", e + " ");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    // 마커 있으면 다 달아주자. DB에서 꺼내서!
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("시발", "맵 뜬다");
        this.googleMap = googleMap;

        permissonCheck();

        Cursor cursor = dbHelper.findAll();
        // 여기에 DB에서 꺼내서 마크 달아주자.
        addMarkerAll(cursor);

        // 클릭 리스너 달아주자.
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("맵 클릭", latLng + " ");
                mapClick(latLng);
            }
        });

        // 마커 드래그 이벤트. 추가 기능으로 해주자.
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });
        // 구글맵의 UI 환경 가져온다.
        uiSettings = googleMap.getUiSettings();
    }


    public void permissonCheck() {
        // 현재 위치 중심으로.
        // 현재 내 위치 중심으로.
        // 퍼미션 체크. 권한이 있는 경우.
        // 현재 안드로이드 버전이 마시멜로 이상인 경우 퍼미션 체크가 추가로 필요함.
        Log.d("내 버전 정보", Build.VERSION.SDK_INT + " ");
        Log.d("마시멜로 정보", Build.VERSION_CODES.M + " ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 퍼미션이 없는 경우 퍼미션을 요구해야겠지?
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_DENIED) {
                // 사용자가 다시 보지 않기에 체크 하지 않고, 퍼미션 체크를 거절한 이력이 있는 경우. (처음 거절한 경우에도 들어감.)
                // 최초 요청시에는 false를 리턴해서 아래 else에 들어간다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.d("다시 물어본다", "다시 물어본다.");
                }
                // 사용자가 다시 보지 않기에 체크하고, 퍼미션 체크를 거절한 이력이 있는 경우.
                // 퍼미션을 요구하는 새로운 창을 띄워줘야 겠지.
                // 최초 요청시에도 들어가게 됨. 다시 보지 않기에 체크하는 창은 물어보지 않음.
                else {
                    Log.d("다시 물어보지 않는다", "다시 물어보지 않는다.");
                }
                // 액티비티, permission String 배열, requestCode를 인자로 받음.
                // 퍼미션을 요구하는 다이얼로그 창을 띄운다.
                // requestCode 다르게 하면 다르게 처리할 수 있을듯?
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }
            // 퍼미션이 있는 경우.
            else {
                googleMap.setMyLocationEnabled(true);
            }
        }
    }

    public void addMarkerAll(Cursor cursor) {
        Address address = null;
        Double latitude = 0.;
        Double longitude = 0.;

        // 데이터 다 꺼내온다.
        while (cursor.moveToNext()) {
            String restaurantName = cursor.getString(cursor.getColumnIndex("name"));
            String restaurantAddress = cursor.getString(cursor.getColumnIndex("address"));
            String restaurantReply = cursor.getString(cursor.getColumnIndex("reply"));
            Log.d("꺼내오는 restaurantAddress", restaurantAddress);

            try {
                List addressList = geocoder.getFromLocationName(restaurantAddress, 5);
                address = (Address) addressList.get(0);
                latitude = address.getLatitude();
                longitude = address.getLongitude();
                Log.d("Latitude 널 테스트", latitude.toString());
                Log.d("Longitude 널 테스트", longitude.toString());
            } catch (Exception e) {
                Log.e("Error", e + " ");
            }

            // 변화 없으면 문제가 생긴 거겠지?
            // 근데 이러한 경우는 없을듯 함 이제는!
            if (latitude == 0. && longitude == 0.) {
                Log.d("변화가 왜 없을까", latitude + ", " + longitude);
            } else {
                LatLng latLng = new LatLng(latitude, longitude);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(restaurantName);
                markerOptions.snippet(restaurantReply);
                markerOptions.draggable(true);

                if (cursor.isLast()) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
                    // 이렇게 하면 마커에 대한 정보가 바로 뜬다.
                    Marker marker = googleMap.addMarker(markerOptions);
                    // 마지막 등록한 마커 띄워준다.
                    marker.showInfoWindow();
                    // 텍스트뷰에 주소 뿌려준다.
                    Log.d("Map상의 텍스트뷰 널 테스트", mapText + "");
                    mapText.setText(restaurantAddress + "");
                }
            }
        }
    }


    // 맵 클릭하면 위치 정보가 전달.
    @Override
    public void mapClick(LatLng latLng) {
        Address address = null;

        // 지오코더 사용.
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
            if (addressList != null) {
                // 처음 것을 이용한다.
                address = addressList.get(0);
                // 맛집 등록 창으로 전환.
                showEnrollFragment(address);
            }
        } catch (Exception e) {
            Log.e("Exception 발생", e + " ");
        }

    }

    // 창 새로 띄워주자.
    public void showEnrollFragment(Address address) {
        Log.d("화면 전환", " 등록 창으로 넘어가자");

        EnrollFragment enrollFragment = EnrollFragment.newInstance(address);
//        Toast.makeText(getContext(), "프래그먼트 바꿔줘야지", Toast.LENGTH_SHORT).show();
        //백버튼 눌렀을 때 처리가 추가로 필요할 듯.
        FragmentManager fragmentManager = getFragmentManager();
        // 현재 붙어 있는 프래그먼트 가져온다.
        Fragment fragment = fragmentManager.findFragmentById(FRAGMENT_CONTATINER);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Log.d("Fragment", fragment.toString());

        fragmentTransaction.replace(FRAGMENT_CONTATINER, enrollFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
