package cn.edu.tjut.ecg.ecgserver.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.ProgressListener;
import com.socks.library.KLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.edu.tjut.ecg.ecgserver.R;
import cn.edu.tjut.ecg.ecgserver.application.MyApplication;
import cn.edu.tjut.ecg.ecgserver.model.UserInfo;
import cn.edu.tjut.ecg.ecgserver.utils.PreferenceUtils;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class SplashActivity extends Activity {

    @Bind(R.id.iv_welcome_img)
    ImageView mIvWelcomeImg;
    @Bind(R.id.tv_server)
    TextView mTvServer;
    @Bind(R.id.tv_version)
    TextView mTvVersion;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.tv_downloadprogress)
    TextView mTvDownloadprogress;
    @Bind(R.id.rl_root)
    RelativeLayout mRlRoot;
    private static final int CODE_UPDATE_DIALOG = 0;
    private static final int CODE_UPDATE_ERROR = 2;
    private static final int CODE_JSON_ERROR = 3;
    private static final int CODE_ENTER_HOME = 4;
    private String mVersionName, mDescription, mDownloadUrl;//软件版本名称、软件更新描述、软件下载地址
    private int mVersionCode;//软件版本号
    private PreferenceUtils preferenceUtils;
    private UserInfo userInfo;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_UPDATE_DIALOG:
                    showUpdateDialog();
                    break;
                case CODE_UPDATE_ERROR:
                    Toast.makeText(SplashActivity.this, "检测更新失败", Toast.LENGTH_SHORT).show();
                    enterCollectData();
                    break;
                case CODE_JSON_ERROR:
                    Toast.makeText(SplashActivity.this, "JSON解析错误", Toast.LENGTH_SHORT).show();
                    enterCollectData();
                    break;
                case CODE_ENTER_HOME:
                    enterCollectData();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private File mSaveFile;
    private Message mMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置页面全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        mTvVersion.setText("版本号：" + getVersionName());
        checkVersion();
        preferenceUtils = new PreferenceUtils();

        // 渐变的动画效果
        AlphaAnimation anim = new AlphaAnimation(0.3f, 1f);
        anim.setDuration(2000);
        mRlRoot.startAnimation(anim);
    }

    private String getVersionName() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            String versionName = packageInfo.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private int getVersionCode() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void checkVersion() {
        final long starttime = System.currentTimeMillis();
        mMsg = Message.obtain();
        //get请求简洁版实现
        RxVolley.get(MyApplication.HOST + "serverupdate.json", new HttpCallback() {
            @Override
            public void onSuccess(String t) {
                try {
                    KLog.v(MyApplication.TAG, t);
                    //解析json
                    JSONObject jsonObject = new JSONObject(t);
                    mVersionName = jsonObject.getString("versionName");
                    mVersionCode = jsonObject.getInt("versionCode");
                    mDescription = jsonObject.getString("description");
                    mDownloadUrl = jsonObject.getString("downloadUrl");
                    if (mVersionCode > getVersionCode()) {//有更新
                        mMsg.what = CODE_UPDATE_DIALOG;
                    } else {
                        mMsg.what = CODE_ENTER_HOME;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mMsg.what = CODE_JSON_ERROR;
                } finally {
                    long endtime = System.currentTimeMillis();
                    final long timeused = endtime - starttime;
                    if (timeused < 2000) {
                        Timer timer = new Timer();
                        TimerTask tast = new TimerTask() {
                            @Override
                            public void run() {
                                mHandler.sendMessage(mMsg);
                            }
                        };
                        timer.schedule(tast, 2000 - timeused);

                    } else {
                        mHandler.sendMessage(mMsg);
                    }
                }
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
                mMsg.what = CODE_UPDATE_ERROR;
                mHandler.sendMessage(mMsg);
            }
        });
    }

    private void downloadApk() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String target = this.getExternalFilesDir("apk") + "/serverupdate.apk";
            KLog.v(MyApplication.TAG, target);
            mSaveFile = new File(target);
            //下载进度(可选参数，不需要可不传)
            ProgressListener listener = new ProgressListener() {
                @Override
                public void onProgress(long transferredBytes, long totalSize) {
                    mTvDownloadprogress.setText("下载进度：" + (transferredBytes / totalSize) * 100 + "%");
                }
            };
            //下载回调，内置了很多方法，详细请查看源码
            // 包括在异步响应的onSuccessInAsync():注不能做UI操作
            // 下载成功时的回调onSuccess()
            // 下载失败时的回调onFailure():例如无网络，服务器异常等
            HttpCallback callback = new HttpCallback() {
                @Override
                public void onSuccessInAsync(byte[] t) {
                }

                @Override
                public void onSuccess(String t) {
                    Toast.makeText(getBaseContext(), "下载成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setDataAndType(Uri.fromFile(mSaveFile), "application/vnd.android.package-archive");
                    startActivityForResult(intent, 0);
                }

                @Override
                public void onFailure(int errorNo, String strMsg) {
                    Toast.makeText(getBaseContext(), "下载失败", Toast.LENGTH_SHORT).show();
                }
            };
            RxVolley.download(this.getExternalFilesDir("apk") + "/serverupdate.apk",
                    mDownloadUrl,
                    listener, callback);
        } else {
            Toast.makeText(SplashActivity.this, "没有找到SD卡！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        enterCollectData();
    }

    /**
     * 弹出升级对话框
     */
    private void showUpdateDialog() {
        SweetAlertDialog pDialog = new SweetAlertDialog(this);
        pDialog.setTitleText("最新版本：" + mVersionName);
        pDialog.setContentText("版本描述：" + mDescription);
        pDialog.setConfirmText("立即更新");
        pDialog.setCancelText("以后再说");
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                mTvDownloadprogress.setVisibility(View.VISIBLE);
                downloadApk();
                KLog.v(MyApplication.TAG, "立即更新");
            }
        });
        pDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                enterCollectData();
            }
        });
        pDialog.show();
    }

    /**
     * 进入主页面
     */
    public void enterCollectData() {

        Intent intent = new Intent(this, UserManageActivity.class);
        startActivity(intent);
        finish();
    }
}
