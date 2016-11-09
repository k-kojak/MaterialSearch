package hu.kojak.android.materialtoolbarset;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

/**
 * Created by Tamas Kojedzinszky on 06/11/2016.
 */

public class SimpleSearch extends RelativeLayout implements RevealListener {

    private static final String TAG = SimpleSearch.class.getSimpleName();

    private SearchListener mSearchListener;

    private EditText mSearchInput;
    private View mBack;
    private View mClear;

    @Nullable
    private CircularRevealView mCircularReveal;

    public SimpleSearch(Context context) {
        super(context);
        init();
    }

    public SimpleSearch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleSearch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        initListeners();
    }

    public void setSearchListener(SearchListener searchListener) {
        this.mSearchListener = searchListener;
    }

    public EditText getSearchInput() {
        return mSearchInput;
    }

    private void init() {
        inflate(getContext(), R.layout.simple_search, this);
        mSearchInput = (EditText) findViewById(R.id.search_input);
        mBack = findViewById(R.id.search_back);
        mClear = findViewById(R.id.search_clear);
    }

    private void initListeners() {
        // already initialized
        if (mCircularReveal != null) return;

        ViewParent parent = getParent();
        if (parent instanceof CircularRevealView) {
            mCircularReveal = (CircularRevealView) parent;
        } else {
            throw new RuntimeException(TAG + " needs to be placed inside " + CircularRevealView.class.getSimpleName());
        }

        // registering this view as a reveal listener
        mCircularReveal.setListener(this);

        // registering back press listener
        mBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mCircularReveal.hide();
                if (mSearchListener != null) {
                    mSearchListener.onBackPressed();
                }
            }
        });

        mClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchInput.setText("");
                if (mSearchListener != null) {
                    mSearchListener.onClearPressed();
                }
            }
        });

        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    mClear.setVisibility(View.VISIBLE);
                } else {
                    mClear.setVisibility(View.INVISIBLE);
                }
            }
        });

        if (mSearchInput.getText().length() != 0) {
            mClear.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onReveal() {
        mSearchInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mSearchInput, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onHide() {
        mSearchInput.clearFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchInput.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        mSearchInput.setText("");
    }

    public interface SearchListener {

        public void onBackPressed();

        public void onClearPressed();

    }

}
