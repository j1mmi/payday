package practise.postcourse.circledraw;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jim on 14/02/2016.
 */
public class CurrencyListDialog extends DialogFragment {

    // dialog for picking the currencies to show in the TransactionEntryDialog spinner

    final static String DIALOG_LISTPREFS = "selected_currencies";
    private boolean[] prefs;
//    private HashMap<Integer,Boolean> tracker = new HashMap<>();
    private OnCompleteListener mListener;

    // define listener to pass back information to main activity
    public interface OnCompleteListener {
        void onCompleteCurrencyListSelection(boolean[] newSelection);
    }

    public static CurrencyListDialog create (boolean[] prefs) {
        CurrencyListDialog dialog = new CurrencyListDialog();
        Bundle bundle = new Bundle();
        bundle.putBooleanArray(DIALOG_LISTPREFS, prefs);
        dialog.setArguments(bundle);
        return dialog;
    }

    public CurrencyListDialog() {}

    // override onAttach to ensure that activity calling the fragment has implemented the listener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CurrencyListDialog.OnCompleteListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        prefs = getArguments().getBooleanArray(DIALOG_LISTPREFS);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("CURRENCY LIST")
                .setMultiChoiceItems(R.array.currency_list, prefs, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                        tracker.put(which, isChecked);
                        prefs[which] = isChecked;
                    }
                })
                .setPositiveButton("OK", null);
        final AlertDialog d = builder.create();

        // add onShowListener with onClick - this will enable the onClick function to be stopped early (i.e. no exiting) if no
        // currencies have been ticked
        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        boolean currencySelected = false;
                        for (boolean b: prefs) {
                            if (b) {
                                currencySelected = true;
                            }
                        }
                        if (!currencySelected) {
                            Toast.makeText(getActivity(),"You must select at least one currency.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // pass the information back to the main activity
                        CurrencyListDialog.this.mListener.onCompleteCurrencyListSelection(prefs);
                        dismiss();
                    }
                });
            }
        });
        return d;
    }

}
