package practise.postcourse.circledraw;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jim on 04/01/2016.
 */
public class TransactionEntryDialog extends DialogFragment {

    public static final String ARG_OWERS = "ower_data"; // these will be the Usercircle objects for the owers in the transaction
    public static final String ARG_TARGET = "target_data"; // this will be the Usercircle object for the target of the transaction
    public static final String ARG_CURRPREFS = "currency_prefs"; // this is the boolean array of currency code prefs to show in the spinner
    public static final String ARG_CURRENCYDEFAULT = "currency_default"; // this is the boolean array of currency code prefs to show in the spinner
    private ArrayList<UserCircle> owers; // these are the person paying the money
    private UserCircle target; // this is the person being paid
    private OnCompleteListener mListener; // new onComplete callback listener to pass info back to the activity
    private EditText amount_field;
    private Spinner spinner;
    private EditText desc_field;
    private CheckBox split_check;
    private boolean[] currencyPrefs;
    private String currencyDefault;

    // default constructor
    public TransactionEntryDialog() {}

    public static TransactionEntryDialog create(ArrayList<UserCircle> owers, UserCircle target, boolean[] currencyPrefs, String currency_default) {
        TransactionEntryDialog dialog = new TransactionEntryDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_OWERS, owers);
        bundle.putParcelable(ARG_TARGET, target);
        bundle.putBooleanArray(ARG_CURRPREFS, currencyPrefs);
        // take first part of currency name and store text before first space - e.g. "EUR - Euro" to "EUR"
        bundle.putString(ARG_CURRENCYDEFAULT, currency_default.split(" ")[0]);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        owers = getArguments().getParcelableArrayList(ARG_OWERS);
        target = getArguments().getParcelable(ARG_TARGET);
        currencyPrefs = getArguments().getBooleanArray(ARG_CURRPREFS);
        currencyDefault = getArguments().getString(ARG_CURRENCYDEFAULT);
    }

    // define listener to pass back information to main activity
    public static interface OnCompleteListener {
        public abstract void onCompleteCurrencyEntry(UserCircle target, String target_value,
                                        ArrayList<UserCircle> owers, String ower_standard, String ower_plus,
                                        String currency, String description);
    }

    // override onAttach to ensure that activity calling the fragment has implemented the listener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CurrencyEntry.OnCompleteListener");
        }
    }

    // override createDialog function - inflating custom layout goes here
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog - Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.currency_entry_dialog, null);

        // set Dialog title text
        TextView title = (TextView) v.findViewById(R.id.title);
        title.setText("PAY " + target.getUserInfo().getFullName().toUpperCase());

        // set amount field text watcher to limit what is inputted
        amount_field = (EditText) v.findViewById(R.id.amount_input_field);

        // add listener to check that EditText amount field is to 2 decimal places only
        amount_field.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable arg0) {
                String str = amount_field.getText().toString();
                if (str.isEmpty()) return;
                String str2 = PerfectDecimal(str, 3, 2);

                if (!str2.equals(str)) {
                    amount_field.setText(str2);
                    int pos = amount_field.getText().length();
                    amount_field.setSelection(pos);
                }
            }
        });

        // get spinner handle
        spinner = (Spinner) v.findViewById(R.id.currency_spinner);

        // get description field handle
        desc_field = (EditText) v.findViewById(R.id.desc_input_field);

        // get check box handle
        split_check = (CheckBox) v.findViewById(R.id.split_checkbox);

        builder.setView(v) // your layout here
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if the amount field has a value in it process the request
                        if (!amount_field.getText().toString().isEmpty()) {
                            String currency = spinner.getSelectedItem().toString();
                            String description = desc_field.getText().toString();
                            // get string value of amount field - e.g. "0.9"
                            String raw_strValue = amount_field.getText().toString();
                            // convert this to a float value e.g. 0.9 (this is required so that we can parse it to a certain number of
                            // decimal places later)
                            Float raw_floatValue = Float.parseFloat(raw_strValue);
                            // if the value in the amount_field is not 0:
                            if (raw_floatValue != 0) {
                                // if the 'Split Amount' checkbox is checked on the dialog screen:
                                if (split_check.isChecked()) {
                                    // get string value of the raw input to two decimal places - e.g. 0.9  -> "0.90".  This will
                                    // be used as the value for the target (i.e. the person owed the money)
                                    String target_value = String.format("%.2f", raw_floatValue);
                                    // get the number of owers in the transaction to split the transaction across
                                    int divider = owers.size();
                                    // multiply value by 100 to make it easier to round the split amount to the nearest penny
                                    float rounding_floatValue = raw_floatValue * 100;
                                    // get the base rounded value, ignoring the remainders - e.g. 123.02 / 3 = 41
                                    float base_floatValue = (float) Math.floor(rounding_floatValue / divider) / 100;
                                    // calculate the remaining amount to add to one person e.g. 2
                                    double raw_difference = (rounding_floatValue) - (Math.floor(rounding_floatValue / divider) * divider);
                                    // change this to pennies e.g. 0.02
                                    float difference = (float) raw_difference / 100;
                                    // get the value to be assigned to all of the owers except one
                                    String ower_standard = String.format("%.2f", base_floatValue);
                                    // get the value to be assigned to the one remaining ower (base value + additional difference value)
                                    String ower_plus = String.format("%.2f", base_floatValue + difference);
                                    // pass the information back to the main activity
                                    TransactionEntryDialog.this.mListener.
                                            onCompleteCurrencyEntry(target, target_value, owers, ower_standard, ower_plus, currency, description);
                                    // if the 'Split Amount' checkbox is not checked on the dialog screen:
                                } else {
                                    // get string value of (raw input * the number of owers) to two decimal places - e.g. 0.9 * 3 -> "2.70".
                                    // This will be used as the value for the target (i.e. the person owed the money)
                                    String target_value = String.format("%.2f", Float.parseFloat(raw_strValue) * owers.size());
                                    // get the value to be assigned to all of the owers
                                    String ower_values = String.format("%.2f", raw_floatValue);
                                    // pass the information back to the main activity
                                    TransactionEntryDialog.this.mListener.
                                            onCompleteCurrencyEntry(target, target_value, owers, ower_values, ower_values, currency, description);
                                }
                            }
                        }
                        // close the dialog
                        TransactionEntryDialog.this.getDialog().cancel();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // close the dialog
                        TransactionEntryDialog.this.getDialog().cancel();
                    }
                });
        Dialog d = builder.create();
        // set dialog to show the soft keyboard on startup
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return d;
    }

    @Override
    public void onResume() {
        super.onResume();

        // get full list of currency codes
        String[] rawCurrencyList = getActivity().getResources().getStringArray(R.array.currency_codes);
        // populate ArrayList with selected currencies based on currencyPrefs boolean array
        ArrayList<String> arrayCurrencyList = new ArrayList<>();
        for (int i =0; i < rawCurrencyList.length; i++) {
            if (currencyPrefs[i]) {
                arrayCurrencyList.add(rawCurrencyList[i]);
            }
        }

        // get position in spinner of default currency
        int currencyDefaultPos = arrayCurrencyList.indexOf(currencyDefault);

        // set spinner position to first item if the default currency is not found in the list
        // (i.e. if user has changed currency list items and not selected a default currency)
        if (currencyDefaultPos == -1) {
            currencyDefaultPos = 0;
            System.out.println("Transaction Entry Dialog: RESET POS");
        }

        // convert ArrayList to string for use in the spinner
        String [] strCurrencyList = arrayCurrencyList.toArray(new String[arrayCurrencyList.size()]);

        // create and set spinner adapter with selected currencies
        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, strCurrencyList); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        // set spinner position to default currency
        spinner.setSelection(currencyDefaultPos);
    }

    public String PerfectDecimal(String str, int MAX_BEFORE_POINT, int MAX_DECIMAL){
        int max = str.length();
        if(str.charAt(0) == '.') return "0"+str;
        if(str.charAt(max-1) == '.' && str.indexOf('.') < max-1) return str.substring(0, max-1);

        String rFinal = "";
        boolean after = false;
        int i = 0, up = 0, decimal = 0; char t;
        while(i < max){
            t = str.charAt(i);
            if(t != '.' && after == false){
                up++;
                if(up > MAX_BEFORE_POINT) return rFinal;
            }else if(t == '.'){
                after = true;
            }else{
                decimal++;
                if(decimal > MAX_DECIMAL)
                    return rFinal;
            }
            rFinal = rFinal + t;
            i++;
        }

        return rFinal;
    }

}
