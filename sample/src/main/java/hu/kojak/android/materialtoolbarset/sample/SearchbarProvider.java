package hu.kojak.android.materialtoolbarset.sample;

import android.support.v7.widget.Toolbar;

import hu.kojak.android.materialtoolbarset.CircularRevealView;

/**
 * Created by Tamas Kojedzinszky on 08/11/2016.
 */

public interface SearchbarProvider {

    CircularRevealView provideRevealView();

    Toolbar provideToolbar();

}
