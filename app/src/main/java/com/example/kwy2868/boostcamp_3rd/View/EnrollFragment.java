package com.example.kwy2868.boostcamp_3rd.View;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    private static String TAG = "ADDRESS";
    private static final int FRAGMENT_CONTATINER = R.id.fragment_show;

    private Geocoder geocoder;
    private Address address;

    private static DBHelper dbHelper;

    RestaurantEnrollListener enrollListener;

    // 이렇게 해야 프래그먼트 독립적으로 사용할 수 있다더라.
    public interface RestaurantEnrollListener{
        void RestaurantEnroll(Restaurant restaurant);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof RestaurantEnrollListener)
            enrollListener = (RestaurantEnrollListener)context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null) {
            address = getArguments().getParcelable(TAG);
            restaurantAddress.setText(address.getAddressLine(0).toString());
        }
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

        // 입력한 맛집 정보들.
        String rName = restaurantName.getText().toString();
        String rAddress = restaurantAddress.getText().toString();
        String rNumber = restaurantNumber.getText().toString();
        String rReply = restaurantReply.getText().toString();
        // 하나라도 빈칸 있을 때 예외처리. 테스트를 위해 일단은 풀어두자.
        if ( (rName.equals("")) || (rAddress.equals("")) || (rNumber.equals("")) || (rReply.equals("")) ) {
            Toast.makeText(getContext(), "모든 항목을 바르게 입력하여 주세요.", Toast.LENGTH_SHORT).show();
        } else {
            Restaurant restaurant = new Restaurant(rName, rAddress, rNumber, rReply);
            enrollListener.RestaurantEnroll(restaurant);
        }
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
