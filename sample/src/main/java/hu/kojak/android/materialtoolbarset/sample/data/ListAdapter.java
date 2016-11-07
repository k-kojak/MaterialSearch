package hu.kojak.android.materialtoolbarset.sample.data;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.kojak.android.materialtoolbarset.sample.R;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Tamas Kojedzinszky on 06/11/2016.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ItemViewHolder> {

    public enum Type {
        NAME,
        COMPANY,
        ADDRESS
    }

    private final Type mType;
    private final List<Person> mFullList;
    private final List<Person> mFiltered;

    public ListAdapter(Type type, List<Person> fullList) {
        this.mType = type;
        this.mFullList = fullList;
        this.mFiltered = new ArrayList<>(fullList);
    }

    public void setFilter(final String filter) {
        Observable
                .from(mFullList)
                .filter(new Func1<Person, Boolean>() {
                    @Override
                    public Boolean call(Person person) {
                        if (filter.length() == 0) return true;

                        String searchSubject;
                        switch (mType) {
                            case COMPANY:
                                searchSubject = person.company;
                                break;
                            case ADDRESS:
                                searchSubject = person.address;
                                break;
                            case NAME:
                            default:
                                searchSubject = person.firstName + " " + person.lastName;
                                break;
                        }

                        searchSubject = searchSubject.toLowerCase();
                        return searchSubject.contains(filter.toLowerCase());
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Person>>() {
                    @Override
                    public void call(List<Person> persons) {
                        mFiltered.clear();
                        mFiltered.addAll(persons);
                        notifyDataSetChanged();
                    }
                });

    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView view = (TextView) LayoutInflater
                .from(parent.getContext()).inflate(R.layout.simple_text, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        String toDisplay;
        Person dataItem = mFiltered.get(position);
        switch (mType) {
            case COMPANY:
                toDisplay = dataItem.company;
                break;
            case ADDRESS:
                toDisplay = dataItem.address;
                break;
            case NAME:
            default:
                toDisplay = dataItem.firstName + " " + dataItem.lastName;
                break;
        }
        holder.mTextView.setText(toDisplay);
    }

    @Override
    public int getItemCount() {
        return mFiltered.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        final TextView mTextView;

        ItemViewHolder(TextView itemView) {
            super(itemView);
            mTextView = itemView;
        }

    }
}
