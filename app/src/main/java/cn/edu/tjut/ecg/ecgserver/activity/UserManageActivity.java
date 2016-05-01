package cn.edu.tjut.ecg.ecgserver.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.edu.tjut.ecg.ecgserver.MyAdapter.LvUserAdapter;
import cn.edu.tjut.ecg.ecgserver.R;
import cn.edu.tjut.ecg.ecgserver.application.MyApplication;
import cn.edu.tjut.ecg.ecgserver.model.UserInfo;
import cn.edu.tjut.ecg.ecgserver.utils.MyJson;
import cn.edu.tjut.ecg.ecgserver.utils.PreferenceUtils;

public class UserManageActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.lv_user)
    ListView mLvUser;
    @Bind(R.id.refresh)
    SwipeRefreshLayout refresh;
    List<UserInfo> mUserInfoList = null;
    PreferenceUtils mPreferenceUtils;
    private static final int CODE = 0x011;
    @Bind(R.id.btn_add_user)
    Button mBtnAddUser;
    private HttpParams mParams;
    private LvUserAdapter mLvUserAdapter;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE:
                    //通知界面改变
                    mLvUserAdapter.notifyDataSetChanged();
                    //刷新状态改变了
                    refresh.setRefreshing(false);
                    break;

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);
        ButterKnife.bind(this);
        setTitle("用户管理");
        mUserInfoList = new ArrayList<UserInfo>();
        mPreferenceUtils = new PreferenceUtils();
        mParams = new HttpParams();
        getUserData();
        mBtnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(UserManageActivity.this,NewUserActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getUserData() {
        RxVolley.post(MyApplication.HOST + "servlet/sgetAllUserInfo", mParams, new HttpCallback() {
            @Override
            public void onSuccess(String t) {
                mUserInfoList.clear();
                mUserInfoList.addAll(MyJson.json2UserInfoList(t));
                //注册监听器
                refresh.setOnRefreshListener(UserManageActivity.this);
                //设置加载的颜色
                refresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
                mLvUserAdapter = new LvUserAdapter(UserManageActivity.this, R.layout.user_list_item, mUserInfoList);
                mLvUser.setAdapter(mLvUserAdapter);
            }
        });
    }

    @Override
    public void onRefresh() {
        RxVolley.post(MyApplication.HOST + "servlet/sgetAllUserInfo", mParams, new HttpCallback() {
            @Override
            public void onSuccess(String t) {
                mUserInfoList.clear();
                mUserInfoList.addAll(MyJson.json2UserInfoList(t));
                Iterator iterator = mUserInfoList.iterator();
            }
        });
        handler.sendEmptyMessageDelayed(CODE, 500);
    }
}
