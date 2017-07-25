package practise.postcourse.circledraw;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jim on 04/01/2016.
 */
public class UndoConfirmDialog extends DialogFragment {

    public static final String ARG_IDS = "transaction_ids"; // these will be the ids e.g. [3,4,5,6]
    public static final String ARG_DATA = "transaction_data"; // this will be the transactions themselves
    private ArrayList<Transaction> mUndoData; // this are the last transactions made
    private int[] mUndoIDs; // these are the DB ids of the last transactions made - to be sent back to main activity on callback
    private OnCompleteListener mListener; // pass back listener

    // define listener to pass back information to main activity
    public interface OnCompleteListener {
        void onCompleteUndoConfirmation(int[] IDsToDelete);
    }

    // default constructor
    public UndoConfirmDialog() {}


    public static UndoConfirmDialog create(Integer[] transactionIDs, ArrayList<Transaction> transactionData) {
        UndoConfirmDialog dialog = new UndoConfirmDialog();
        //convert Integer array to int array for storage
        int[] convertedIDs = new int[transactionIDs.length];
        for (int i = 0; i < transactionIDs.length; i++) {
            convertedIDs[i] = transactionIDs[i];
        }
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_DATA, transactionData);
        bundle.putIntArray(ARG_IDS, convertedIDs);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUndoData = getArguments().getParcelableArrayList(ARG_DATA);
        mUndoIDs = getArguments().getIntArray(ARG_IDS);
    }

    // override createDialog function - inflating custom layout goes here
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog - Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.undo_confirm_dialog, null);

        TextView dialogText = (TextView) v.findViewById(R.id.undo_text);

        for (Transaction t : mUndoData) {
            if (t!=null) {
                dialogText.setText("Reverse last payment to " + t.getToUserName().toUpperCase() + " ?");
                break;
            }
        }

        ListView transactionList = (ListView) v.findViewById(R.id.undo_list);

        // IMPLEMENT ARRAY ADAPTER HERE

        UndoListArrayAdapter adapter = new UndoListArrayAdapter(
                getActivity(), mUndoData);
        transactionList.setAdapter(adapter);


        builder.setView(v) // your layout here
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // close the dialog
                        UndoConfirmDialog.this.mListener.onCompleteUndoConfirmation(mUndoIDs);
                        UndoConfirmDialog.this.getDialog().cancel();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // close the dialog
                        UndoConfirmDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    // override onAttach to ensure that activity calling the fragment has implemented the listener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UndoConfirm.OnCompleteListener");
        }
    }

    private class UndoListArrayAdapter extends ArrayAdapter<Transaction> {
        private final Context context;
        private final ArrayList<Transaction> values;

        public UndoListArrayAdapter(Context context, ArrayList<Transaction> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // get inflater
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // get layout for each row
            View rowView;
//            if (values.get(position)!=null) {
            rowView = inflater.inflate(R.layout.undo_confirm_child_list, parent, false);
            // get and set from user
            TextView from = (TextView) rowView.findViewById(R.id.from_user);
            from.setText(values.get(position).getFromUserName());
            // get and set amount
            TextView amount = (TextView) rowView.findViewById(R.id.amount);
            amount.setText(String.format("%.2f", (float) values.get(position).getAmount()/100));
            // get and set currency
            TextView currency = (TextView) rowView.findViewById(R.id.currency);
            currency.setText(values.get(position).getCurrency());
            // return view
            return rowView;
        }
    }

}
