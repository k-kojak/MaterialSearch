package hu.kojak.android.materialtoolbarset.sample;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.kojak.android.materialtoolbarset.CircularRevealView;
import hu.kojak.android.materialtoolbarset.SimpleSearch;
import hu.kojak.android.materialtoolbarset.sample.data.DataProvider;
import hu.kojak.android.materialtoolbarset.sample.data.ListAdapter;
import hu.kojak.android.materialtoolbarset.sample.data.Person;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class ToolbarSample extends AppCompatActivity {

    @BindView(R.id.list)
    RecyclerView mList;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.circular_view)
    CircularRevealView mCircularReveal;
    @BindView(R.id.simple_search)
    SimpleSearch mSimpleSearch;

    private ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_sample);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
//        mCircularReveal.setTargetView(mAppBarLayout);
        mCircularReveal.restoreState(savedInstanceState);

        mSimpleSearch.setSearchListener(new SimpleSearch.SearchListener() {
            @Override
            public void onBackPressed() {
                mAdapter.setFilter("");
            }

            @Override
            public void onClearPressed() {
                mAdapter.setFilter("");
            }
        });

        RxTextView
                .afterTextChangeEvents(mSimpleSearch.getSearchInput())
                .debounce(600, TimeUnit.MILLISECONDS)
                .map(new Func1<TextViewAfterTextChangeEvent, String>() {
                    @Override
                    public String call(TextViewAfterTextChangeEvent textViewAfterTextChangeEvent) {
                        return textViewAfterTextChangeEvent.editable().toString();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String filter) {
                        mAdapter.setFilter(filter);
                    }
                });

        List<Person> personList = DataProvider.getData(this);
        mAdapter = new ListAdapter(ListAdapter.Type.NAME, personList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mList.getContext(),
                layoutManager.getOrientation());
        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(mAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mCircularReveal.retainState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                mCircularReveal.reveal(mToolbar.findViewById(item.getItemId()));
                break;
            case R.id.action_fake:
                Snackbar.make(findViewById(android.R.id.content), "Foo bar", Snackbar.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (mCircularReveal.isViewing()) {
            mCircularReveal.hide();
        } else {
            super.onBackPressed();
        }
    }
}
