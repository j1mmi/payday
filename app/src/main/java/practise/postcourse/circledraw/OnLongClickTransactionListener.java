package practise.postcourse.circledraw;

import android.view.View;

/**
 * Created by Jim on 16/02/2016.
 */

public class OnLongClickTransactionListener implements View.OnLongClickListener {

    boolean[] currencyPreferences;
    public OnLongClickTransactionListener(boolean[] currencyPreferences) {
        this.currencyPreferences = currencyPreferences;
    }

    @Override
    public boolean onLongClick(View v)
    {
        return false;//read your lovely variable
    }

};