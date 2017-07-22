package com.example.kwy2868.boostcamp_3rd.View;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kwy2868.boostcamp_3rd.DB.DBHelper;
import com.example.kwy2868.boostcamp_3rd.EnrollRestaurantByButtonClickListener;
import com.example.kwy2868.boostcamp_3rd.Model.Restaurant;
import com.example.kwy2868.boostcamp_3rd.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.M;


/**
 * Created by kwy2868 on 2017-07-17.
 */

public class GoogleMapFragment extends Fragment
        implements OnMapReadyCallback, EnrollRestaurantByButtonClickListener {

    // 맵프래그먼트 아래에 있는 다음(등록) 버튼.
    private Button enrollButton;

    private static final int REQUEST_CODE = 0;
    private static final String TAG = "GoogleMapFragment";

    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;

    private TextView mapText;

    private Geocoder geocoder;

    private static DBHelper dbHelper;

    // 검색한 장소가 지오코딩을 통해 변환이 가능한지 체크하기 위한 변수.
    private boolean restaurantExist = false;
    // 위의 변수를 사용하여 이 레스토랑 모델을 DB에 넣을지 말지 체크하자.
    private Restaurant enrollRestaurant;

    // 액티비티와 프래그먼트 통신을 위함.
    public interface MapFragmentListener{
        void MapClickEnroll(LatLng latLng);
        void AfterEnrollButtonClick();
        void AfterMarkerDrag(Address address);
    }

    MapFragmentListener mapFragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof MapFragmentListener)
            mapFragmentListener = (MapFragmentListener)context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapText = view.findViewById(R.id.map_text);
        enrollButton = view.findViewById(R.id.enrollButton);
        enrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnrollRestaurantByButtonClick();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        geocoder = new Geocoder(getContext(), Locale.KOREAN);
        dbHelper = new DBHelper(getContext(), null, null, 1);

        if (getArguments() != null) {
            enrollRestaurant = getArguments().getParcelable("RESTAURANT");
            // 입력한 주소를 지오코딩으로 변환이 가능한지 체크한다.
            if(existCheckLocation(enrollRestaurant)){
                restaurantExist = true;
            }
        }
        mapSetting();
    }

    // 마커 있으면 다 달아주자. DB에서 꺼내서!
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // 퍼미션이 있어야 맵을 띄워줄 수가 있게하자.
        permissonCheck();

        Cursor cursor = dbHelper.findAll();
        // 여기에 DB에서 꺼내서 마크 달아주자.
        addMarkerAll(cursor);

        ///// 앱 첫 실행해서 입력받은 레스토랑 마커와 텍스트뷰 처리하는 부분.
        Address address = null;
        // 여기서 좌표 변환이 필요함.
        try {
            List addressList = geocoder.getFromLocationName(enrollRestaurant.getAddress(), 5);
            address = (Address) addressList.get(0);
        } catch (Exception e) {
            Log.e("Error", e + " ");
        }
        if(address != null){
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(enrollRestaurant.getName());
            markerOptions.snippet(enrollRestaurant.getReply());
            markerOptions.draggable(true);

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            Marker marker = googleMap.addMarker(markerOptions);
            marker.showInfoWindow();
            mapText.setText(enrollRestaurant.getAddress());
        }
        ///// 앱 첫 실행해서 입력받은 레스토랑 마커와 텍스트뷰 처리하는 부분.

        // 클릭 리스너 달아주자.
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapFragmentListener.MapClickEnroll(latLng);
            }
        });

        // 마커 드래그 이벤트. 추가 기능으로 해주자.
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            // 여기서 원래 마커의 정보도 담아두어야 DB에서 갱신할 수 있겠지.
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            // 여기서 DB 내용 업데이트 해주자.
            @Override
            public void onMarkerDragEnd(Marker marker) {
                changeRestaurant(marker);
            }
        });
    }

    public void mapSetting() {
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public boolean existCheckLocation(Restaurant restaurant) {
        String restaurantAddress = null;

        if (restaurant != null) {
            restaurantAddress = restaurant.getAddress();
        } else
            return false;

        try {
            List addressList = geocoder.getFromLocationName(restaurantAddress, 5);

            // 지오코딩을 통해 가져온 게 있으면 제일 앞의 것을 써주자.
            if (addressList.size() > 0) {
                Toast.makeText(getContext(), "등록을 위해서는 다음 버튼을 클릭해 주세요.", Toast.LENGTH_LONG).show();
                return true;
            }
            // 지오코딩으로 가져오는게 없을 때.
            else {
                Toast.makeText(getContext(), "가장 마지막에 등록한 맛집으로 이동합니다.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Log.e("Error", e + " ");
        }
        return false;
    }

    public void permissonCheck() {
        // 퍼미션 체크. 권한이 있는 경우.
        // 현재 안드로이드 버전이 마시멜로 이상인 경우 퍼미션 체크가 추가로 필요함.
        Log.d("내 버전 정보", Build.VERSION.SDK_INT + " ");
        Log.d("마시멜로 정보", M + " ");
        if (Build.VERSION.SDK_INT >= M) {
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
            } catch (Exception e) {
                Log.e("Error", e + " ");
            }

            // 변화 없으면 문제가 생긴 거겠지?
            // 근데 이러한 경우는 없을듯 함 이제는!
            if (latitude == 0. && longitude == 0.) {
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
                    mapText.setText(restaurantAddress + "");
                }
            }
        }
    }

    // 등록 버튼을 눌렀을 때.
    @Override
    public void EnrollRestaurantByButtonClick() {
        addRestaurantToDB();
    }

    // 클릭했을 때 호출.
    public void addRestaurantToDB(){
        if(restaurantExist == true){
            Toast.makeText(getContext(), "정상적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
            dbHelper.addRestaurant(enrollRestaurant);
            restaurantExist = false;
            mapFragmentListener.AfterEnrollButtonClick();
        }
        // 이런 경우는 없겠지만.
        else{
            Toast.makeText(getContext(), "맛집을 등록할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 마커 드래그 이벤트.
    public void changeRestaurant(Marker marker){
        // 이거 안하면 저장 되어있는 값 때문에 마커가 두개씩 찍힘.
        marker.remove();
        Address afterAddress = null;
        // 변경된 마커 위치.
        LatLng latLng = marker.getPosition();

        // 변경된 마커의 좌표로 지오코딩.
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
            if (addressList != null) {
                // 처음 것을 이용한다.
                afterAddress = addressList.get(0);

                // 변경되는 창을 띄워주자.
                enrollRestaurant.setAddress(afterAddress.toString());
                mapText.setText(afterAddress.getAddressLine(0));
                mapFragmentListener.AfterMarkerDrag(afterAddress);
            }
        } catch (Exception e) {
            Log.e("Exception 발생", e + " ");
        }
    }
}
