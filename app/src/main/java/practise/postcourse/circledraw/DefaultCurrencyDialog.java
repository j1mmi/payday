package practise.postcourse.circledraw;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by Jim on 14/02/2016.
 */
public class DefaultCurrencyDialog extends DialogFragment {

    // dialog for picking the currencies to show in the TransactionEntryDialog spinner

    final static String DIALOG_CURRENCYPREFS = "selected_currencies";
    final static String DIALOG_CURRENCYDEFAULT = "selected_default";
    private boolean[] selectedCurrencies;
    private String defaultCurrency;
    private OnCompleteListener mListener;
    private ArrayList<String> arrayCurrencyList;

    // define listener to pass back information to main activity
    public interface OnCompleteListener {
        void onCompleteDefaultCurrencySelection(String defaultCurrency);
    }

    public static DefaultCurrencyDialog create (String defaultCurrency, boolean[] selectedCurrencies) {
        DefaultCurrencyDialog dialog = new DefaultCurrencyDialog();
        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_CURRENCYDEFAULT, defaultCurrency);
        bundle.putBooleanArray(DIALOG_CURRENCYPREFS, selectedCurrencies);
        dialog.setArguments(bundle);
        return dialog;
    }

    public DefaultCurrencyDialog() {}

    // override onAttach to ensure that activity calling the fragment has implemented the listener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DefaultCurrencyDialog.OnCompleteListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get full name of default currency
        defaultCurrency = getArguments().getString(DIALOG_CURRENCYDEFAULT);

        // get boolean array of selected currencies
        selectedCurrencies = getArguments().getBooleanArray(DIALOG_CURRENCYPREFS);

        // get full currency list
        String[] rawCurrencyList = getActivity().getResources().getStringArray(R.array.currency_list);
        // create ArrayList of currencies using the boolean array as a filter
        arrayCurrencyList = new ArrayList<>();
        for (int i = 0; i < rawCurrencyList.length; i++) {
            if (selectedCurrencies[i]) {
                arrayCurrencyList.add(rawCurrencyList[i]);
            }
        }

        // convert ArrayList to string array for use in dialog
        String [] strCurrencyList = arrayCurrencyList.toArray(new String[arrayCurrencyList.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // get position of default currency in the currency list. Set no position (-1) if the default currency is not found
        int defaultCurrPosition = arrayCurrencyList.indexOf(defaultCurrency);

        builder.setTitle("DEFAULT CURRENCY")
                .setSingleChoiceItems(strCurrencyList, defaultCurrPosition, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        defaultCurrency = arrayCurrencyList.get(i);
                        DefaultCurrencyDialog.this.mListener.onCompleteDefaultCurrencySelection(defaultCurrency);
                        dismiss();
                    }
                });

        return builder.create();
    }

}
