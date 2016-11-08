package hu.kojak.android.materialtoolbarset.sample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.kojak.android.materialtoolbarset.sample.data.DataProvider;
import hu.kojak.android.materialtoolbarset.sample.data.ListAdapter;
import hu.kojak.android.materialtoolbarset.sample.data.Person;

/**
 * Created by Tamas Kojedzinszky on 08/11/2016.
 */

public class ListFragment extends Fragment {

    private static final String TYPE_KEY = "type_key";

    private ListAdapter.Type mType;
    private ListAdapter mAdapter;
    private SearchbarProvider mProvider;

    @BindView(R.id.list)
    RecyclerView mList;

    public static ListFragment newInstance(ListAdapter.Type type) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(TYPE_KEY, type);

        ListFragment fragment = new ListFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mType = (ListAdapter.Type) getArguments().get(TYPE_KEY);

        if (mType == ListAdapter.Type.COMPANY) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        List<Person> personList = DataProvider.getData(getContext());
        mAdapter = new ListAdapter(mType, personList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false);
        mList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mList.getContext(),
                layoutManager.getOrientation());
        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(mAdapter);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mProvider = (SearchbarProvider) context;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sample, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                mProvider.provide().reveal(item);
                break;
            case R.id.action_fake:
                Snackbar.make(getView(), "Foo bar", Snackbar.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
