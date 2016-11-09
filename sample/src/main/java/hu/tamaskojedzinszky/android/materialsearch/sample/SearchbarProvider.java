package hu.tamaskojedzinszky.android.materialsearch.sample;

import android.support.v7.widget.Toolbar;

import hu.tamaskojedzinszky.android.materialsearch.CircularRevealView;

/**
 * Created by Tamas Kojedzinszky on 08/11/2016.
 */

public interface SearchbarProvider {

    CircularRevealView provideRevealView();

    Toolbar provideToolbar();

}
