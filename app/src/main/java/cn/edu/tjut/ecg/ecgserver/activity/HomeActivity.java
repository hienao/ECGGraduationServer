package cn.edu.tjut.ecg.ecgserver.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.edu.tjut.ecg.ecgserver.MyAdapter.TagPagerAdapter;
import cn.edu.tjut.ecg.ecgserver.R;
import cn.edu.tjut.ecg.ecgserver.fragment.FileFragment;
import cn.edu.tjut.ecg.ecgserver.fragment.UserInfoFragment;
import cn.edu.tjut.ecg.ecgserver.fragment.YangbenFragment;
import cn.edu.tjut.ecg.ecgserver.utils.PreferenceUtils;

public class HomeActivity extends FragmentActivity implements
        ViewPager.OnPageChangeListener, RadioGroup.OnCheckedChangeListener{


    @Bind(R.id.viewPager)
    ViewPager mViewPager;
    @Bind(R.id.btn_user)
    RadioButton mBtnUser;
    @Bind(R.id.btn_file)
    RadioButton mBtnFile;
    @Bind(R.id.btn_yangben)
    RadioButton mBtnYangben;
    @Bind(R.id.radioGroup)
    RadioGroup mRadioGroup;

    private List<Fragment> fragments = new ArrayList<Fragment>();

    private String mUserInfogson;
    /**
     * 按钮的没选中显示的图标
     */
    private int[] unselectedIconIds = { R.mipmap.ic_tab_profile,
            R.mipmap.ic_tab_file, R.mipmap.ic_tab_yangben};
    /**
     * 按钮的选中显示的图标
     */
    private int[] selectedIconIds = { R.mipmap.ic_tab_profile_selected,
            R.mipmap.ic_tab_file_selected, R.mipmap.ic_tab_yangben_selected };

    private PreferenceUtils mPreferenceUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        mPreferenceUtils = new PreferenceUtils();
        mUserInfogson=mPreferenceUtils.getPreferenceString("user");
        init();
        mRadioGroup.setOnCheckedChangeListener(this);
        TagPagerAdapter tabPageAdapter = new TagPagerAdapter(
                getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(tabPageAdapter);
        mViewPager.setOnPageChangeListener(this);
    }

    /**
     * 初始化fragment
     */
    protected void init() {
        Fragment userInfoFragment = new UserInfoFragment();
        Fragment fileFragment = new FileFragment();
        Fragment yangbenFragment = new YangbenFragment();
        fragments.add(userInfoFragment);
        fragments.add(fileFragment);
        fragments.add(yangbenFragment);
    }

    /**
     * 选择某页
     * @param position 页面的位置
     */
    private void selectPage(int position) {
        // 将所有的tab的icon变成灰色的
        for (int i = 0; i < mRadioGroup.getChildCount(); i++) {
            Drawable gray = getResources().getDrawable(unselectedIconIds[i]);
            // 不能少，少了不会显示图片
            gray.setBounds(0, 0, gray.getMinimumWidth(),
                    gray.getMinimumHeight());
            RadioButton child = (RadioButton) mRadioGroup.getChildAt(i);
            child.setCompoundDrawables(null, gray, null, null);
            child.setTextColor(getResources().getColor(
                    R.color.Gray));
        }
        // 切换页面
        mViewPager.setCurrentItem(position, false);
        // 改变图标
        Drawable yellow = getResources().getDrawable(selectedIconIds[position]);
        yellow.setBounds(0, 0, yellow.getMinimumWidth(),
                yellow.getMinimumHeight());
        RadioButton select = (RadioButton) mRadioGroup.getChildAt(position);
        select.setCompoundDrawables(null, yellow, null, null);
        select.setTextColor(getResources().getColor(
                R.color.Yellow));
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.btn_user: // 信息选中
                selectPage(0);
                break;
            case R.id.btn_file: // 波形选中
                selectPage(1);
                break;
            case R.id.btn_yangben: // 样本选中
                selectPage(2);
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        selectPage(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
