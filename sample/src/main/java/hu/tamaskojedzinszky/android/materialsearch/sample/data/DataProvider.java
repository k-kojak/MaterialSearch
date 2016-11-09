package hu.tamaskojedzinszky.android.materialsearch.sample.data;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import hu.tamaskojedzinszky.android.materialsearch.sample.R;

public class DataProvider {

    private static final String TAG = DataProvider.class.getSimpleName();
    private static List<Person> people = null;

    public static List<Person> getData(final Context context) {

        if (people == null) {
            people = new ArrayList<>(2000);
            Resources res = context.getResources();
            try {
                InputStream ins = res.openRawResource(R.raw.sample2000);
                BufferedReader br = new BufferedReader(new InputStreamReader(ins));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] items = line.split("\t");
                    people.add(new Person(items));
                }
                br.close();
                ins.close();
            } catch (IOException e) {
                Log.e(TAG, "error while reading in resources", e);
            }
        }

        return people;
    }

}
