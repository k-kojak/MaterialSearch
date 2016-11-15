package hu.tamaskojedzinszky.android.materialsearch.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SampleMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.only_toolbar)
    void onToolbarSampleClick() {
        startActivity(new Intent(this, ToolbarSample.class));
    }

    @OnClick(R.id.toolbar_viewpager)
    void onToolbarWithViewpagerClick() {
        startActivity(new Intent(this, ToolbarWithViewpagerSample.class));
    }

}
