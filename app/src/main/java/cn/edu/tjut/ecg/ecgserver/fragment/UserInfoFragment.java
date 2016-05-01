package cn.edu.tjut.ecg.ecgserver.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kymjs.core.bitmap.client.BitmapCore;
import com.kymjs.rxvolley.client.HttpCallback;
import com.socks.library.KLog;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.edu.tjut.ecg.ecgserver.R;
import cn.edu.tjut.ecg.ecgserver.application.MyApplication;
import cn.edu.tjut.ecg.ecgserver.model.UserInfo;
import cn.edu.tjut.ecg.ecgserver.utils.MyJson;
import cn.edu.tjut.ecg.ecgserver.utils.PreferenceUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link UserInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @Bind(R.id.tv_userid)
    TextView mTvUserid;
    @Bind(R.id.iv_photo)
    ImageView mIvPhoto;
    @Bind(R.id.tv_username)
    TextView mTvUsername;
    @Bind(R.id.tv_usersex)
    TextView mTvUsersex;
    @Bind(R.id.tv_userage)
    TextView mTvUserage;
    @Bind(R.id.tv_userdianhua)
    TextView mTvUserdianhua;
    private UserInfo mUserInfo;
    private PreferenceUtils mPreferenceUtils;
    HttpCallback callback;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public UserInfoFragment() {
        // Required empty public constructor
        mPreferenceUtils=new PreferenceUtils();

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserInfoFragment newInstance(String param1, String param2) {
        UserInfoFragment fragment = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        callback=new HttpCallback() {
            @Override
            public void onPreStart() {
                super.onPreStart();
            }

            @Override
            public void onPreHttp() {
                super.onPreHttp();
            }

            @Override
            public void onSuccessInAsync(byte[] t) {
                super.onSuccessInAsync(t);
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
            }

            @Override
            public void onSuccess(Map<String, String> headers, byte[] t) {
                super.onSuccess(headers, t);
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }

            @Override
            public void onSuccess(Map<String, String> headers, Bitmap bitmap) {
                super.onSuccess(headers, bitmap);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        ButterKnife.bind(this, view);
        mUserInfo= MyJson.json2User(mPreferenceUtils.getPreferenceString("user"));
        mTvUserid.setText(String.valueOf(mUserInfo.getUserid()));
        mTvUsername.setText(mUserInfo.getName());
        mTvUserage.setText(String.valueOf(mUserInfo.getAge()));
        mTvUsersex.setText(mUserInfo.getSex());
        mTvUserdianhua.setText(String.valueOf(mUserInfo.getPhone()));
        KLog.v(MyApplication.TAG,mUserInfo.getPhotopath());
        if (!mUserInfo.getPhotopath().isEmpty()){
            new BitmapCore.Builder()
                    .url(mUserInfo.getPhotopath())
                    .callback(callback)
                    .view(mIvPhoto)
                    .loadResId(R.mipmap.ic_launcher)
                    .errorResId(R.mipmap.ic_launcher)
                    .doTask();
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
