package hu.kojak.android.materialtoolbarset.sample;

import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.kojak.android.materialtoolbarset.CircularRevealView;
import hu.kojak.android.materialtoolbarset.sample.data.ListAdapter;
import hu.kojak.android.materialtoolbarset.sample.widget.SlidingTabLayout;

public class ToolbarWithViewpagerSample extends AppCompatActivity implements SearchbarProvider {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.circular_view)
    CircularRevealView mCircularReveal;

    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_with_viewpager);

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mCircularReveal.setTargetView(mAppBarLayout);

        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setAdapter(adapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mCircularReveal.isViewing()) {
                    mCircularReveal.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        SlidingTabLayout tabs = ButterKnife.findById(this, R.id.tabs);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(ToolbarWithViewpagerSample.this, R.color.colorAccent);
            }

            @Override
            public int getDividerColor(int position) {
                return ContextCompat.getColor(ToolbarWithViewpagerSample.this, android.R.color.transparent);
            }
        });
        tabs.setViewPager(mPager);

    }

    @Override
    public CircularRevealView provideRevealView() {
        return mCircularReveal;
    }

    @Override
    public Toolbar provideToolbar() {
        return mToolbar;
    }

    @Override
    public void onBackPressed() {
        if (mCircularReveal.isViewing()) {
            mCircularReveal.hide();
        } else {
            super.onBackPressed();
        }
    }

    private static class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ListFragment.newInstance(ListAdapter.Type.values()[position]);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return ListAdapter.Type.values()[position].toString();
        }

        @Override
        public int getCount() {
            return ListAdapter.Type.values().length;
        }

    }

}
