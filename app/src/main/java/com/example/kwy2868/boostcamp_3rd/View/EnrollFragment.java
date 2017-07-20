package com.example.kwy2868.boostcamp_3rd.View;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.kwy2868.boostcamp_3rd.DB.DBHelper;
import com.example.kwy2868.boostcamp_3rd.Model.Restaurant;
import com.example.kwy2868.boostcamp_3rd.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by kwy2868 on 2017-07-17.
 */

public class EnrollFragment extends Fragment implements TextWatcher {
    @BindView(R.id.restaurant_name)
    EditText restaurantName;
    @BindView(R.id.restaurant_address)
    EditText restaurantAddress;
    @BindView(R.id.restaurant_number)
    EditText restaurantNumber;
    @BindView(R.id.restaurant_reply)
    EditText restaurantReply;

    @BindView(R.id.text_count)
    TextView textCount;

    @BindView(R.id.prev_btn)
    Button prevButton;
    @BindView(R.id.next_btn)
    Button nextButton;

    private Unbinder unbinder;
    private static String TAG = "LOCATION";
    private static final int FRAGMENT_CONTATINER = R.id.fragment_show;

    private Geocoder geocoder;
    private Address address;

    private static DBHelper dbHelper;

    public static EnrollFragment newInstance(Address address) {
        EnrollFragment enrollFragment = new EnrollFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(TAG, address);
        enrollFragment.setArguments(bundle);

        return enrollFragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enroll, container, false);
        unbinder = ButterKnife.bind(this, view);
        restaurantReply.addTextChangedListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dbHelper = new DBHelper(getContext(), null, null, 1);

        // 전달받는 내용이 있으면.
        // 근데 이제 이거 필요없음.
        if (getArguments() != null) {
            address = getArguments().getParcelable(TAG);
            Log.d("위치 잘 받아온다.", address + " ");
            restaurantAddress.setText(address.getAddressLine(0).toString());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    // 맛집 정보 등록을 위해 다음 버튼을 눌렀을 때.
    // 등록 하면 다시 맵 프래그먼트로 돌아가야겠지.
    // DB에도 데이터 넣어주고.
    @OnClick(R.id.next_btn)
    void enroll() {
        Log.d("등록한다", "등록을 하자");

        // 입력한 맛집 정보들.
        String rName = restaurantName.getText().toString();
        String rAddress = restaurantAddress.getText().toString();
        String rNumber = restaurantNumber.getText().toString();
        String rReply = restaurantReply.getText().toString();
        // 하나라도 빈칸 있을 때 예외처리. 테스트를 위해 일단은 풀어두자.
//        if ( (rName.equals("")) || (rAddress.equals("")) || (rNumber.equals("")) || (rReply.equals("")) ) {
//            Toast.makeText(getContext(), "모든 항목을 바르게 입력하여 주세요.", Toast.LENGTH_SHORT).show();
//        } else {
            Restaurant restaurant = new Restaurant(rName, rAddress, rNumber, rReply);
            showMapFragment(restaurant);
//        }
//        Toast.makeText(getContext(), "등록한다", Toast.LENGTH_SHORT).show();
    }

    public void showMapFragment(Restaurant restaurant) {
        // DB에서 꺼내오는 거면 사실상 이거도 필요 없을 것 같긴하다..?
        GoogleMapFragment googleMapFragment = GoogleMapFragment.newInstance(restaurant);
//        Toast.makeText(getContext(), "프래그먼트 바꿔줘야지", Toast.LENGTH_SHORT).show();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(FRAGMENT_CONTATINER, googleMapFragment, "LOCATION");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void updateRestaurantList() {
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        textCount.setText("글자수 : " + String.valueOf(charSequence.length()) + "/500");
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
