package cn.edu.tjut.ecg.ecgserver.MyAdapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;
import com.zhy.base.adapter.ViewHolder;
import com.zhy.base.adapter.abslistview.CommonAdapter;

import java.util.List;

import cn.edu.tjut.ecg.ecgserver.R;
import cn.edu.tjut.ecg.ecgserver.activity.HomeActivity;
import cn.edu.tjut.ecg.ecgserver.application.MyApplication;
import cn.edu.tjut.ecg.ecgserver.model.UserInfo;
import cn.edu.tjut.ecg.ecgserver.utils.MyJson;
import cn.edu.tjut.ecg.ecgserver.utils.PreferenceUtils;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Administrator on 2016/4/17 0017.
 */
public class LvUserAdapter extends CommonAdapter<UserInfo> {
    private List<UserInfo> mdatas;
    private Context mContext;
    private int mlayoutId;
    private SweetAlertDialog mPDialog;
    private PreferenceUtils mPreferenceUtils;

    public LvUserAdapter(Context context, int layoutId, List<UserInfo> datas) {
        super(context, layoutId, datas);
        this.mdatas=datas;
        this.mContext=context;
        this.mlayoutId=layoutId;
        mPreferenceUtils=new PreferenceUtils();
    }
    private static final int GETUSERLIST = 0x011;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GETUSERLIST:
                    HttpParams mParams1 = new HttpParams();
                    RxVolley.post(MyApplication.HOST+"servlet/sgetAllUserInfo", mParams1, new HttpCallback() {
                        @Override
                        public void onSuccess(String t) {
                            mdatas.clear();
                            mdatas.addAll(MyJson.json2UserInfoList(t));
                            notifyDataSetChanged();
                        }
                    });
                    mPDialog.dismiss();
                    break;

            }
        }
    };
    @Override
    public void convert(ViewHolder holder, final UserInfo userInfo) {
        final SwipeLayout swipeLayout=holder.getView(R.id.sl_user_item);
        TextView tv_userid=holder.getView(R.id.tv_userid);
        TextView tv_username=holder.getView(R.id.tv_username);
        RelativeLayout rl_user_manage=holder.getView(R.id.rl_user_manage);
        RelativeLayout rl_user_del=holder.getView(R.id.rl_user_del);
        tv_userid.setText("ID："+ userInfo.getUserid());
        tv_username.setText("姓名："+ userInfo.getName());
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, holder.getView(R.id.bottom_wrapper));
        swipeLayout.setRightSwipeEnabled(true);
        swipeLayout.setLeftSwipeEnabled(false);
        swipeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (swipeLayout.getOpenStatus()==SwipeLayout.Status.Close)
                    swipeLayout.open();
                else
                    swipeLayout.close();
            }
        });
        rl_user_manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext, HomeActivity.class);
                mPreferenceUtils.setPreferenceString("user",MyJson.user2Json(userInfo));
                mContext.startActivity(intent);
            }
        });
        rl_user_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPDialog = new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("您确定要删除该用户?")
                        .setContentText("删除后不可恢复，请谨慎！")
                        .setCancelText("不删除")
                        .setConfirmText("删除").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                HttpParams mParams = new HttpParams();
                                mParams.put("userid", String.valueOf( userInfo.getUserid()));
                                RxVolley.post(MyApplication.HOST+"servlet/sdeleteUser", mParams, new HttpCallback() {
                                    @Override
                                    public void onSuccess(String t) {
                                        int result=0;
                                        if (!t.isEmpty())
                                            result=Integer.parseInt(t.replaceAll("\\D+","").replaceAll("\r", "").replaceAll("\n", "").trim());
                                        if (result==1)
                                            handler.sendEmptyMessageDelayed(GETUSERLIST, 100);
                                    }
                                });
                            }
                        });
                mPDialog.show();
            }
        });
    }
}
