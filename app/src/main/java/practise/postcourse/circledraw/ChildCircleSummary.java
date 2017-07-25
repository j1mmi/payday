package practise.postcourse.circledraw;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.timroes.android.listview.EnhancedListView;

/**
 * Created by Jim on 07/01/2016.
 */
public class ChildCircleSummary extends AppCompatActivity {

    private TextView user;
    private String userId;
    private int width;
    private int height;
    private int orig_radius;
    private String shortName;
    private String fullName;
    private int ring_color;
    private int x_loc;
    private int y_loc;
    private TextView title;
    private TextView sub_title;
    private LinearLayout currencyLayout;
    private TextView currency;
    private TextView total;
    private EnhancedListView list;
    private Cursor SummaryCursor;
    private SimpleCursorAdapter adapter;
    private MySQLiteHelper db;
    UserCircle circle; // the intent information from the calling main activity
    User userInfo;
    String[] userCurrencyCodes; // stores the currencies that the user owes/is owed money in, to be shown when the currency is switched
    String[] userCurrencyNames;
    int viewPosition; // tracks which currency is being displayed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.child_summary_main);
        unpackIntent(intent);
        db = new MySQLiteHelper(this);

        //set action bar / toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar(toolbar);

        // set initial position as first currency item
        viewPosition = 0;

        // get list of user currencies
        userCurrencyCodes = db.getUserCurrencies(userId);
        userCurrencyNames = new String[userCurrencyCodes.length];

        // create list of currency names that correspond to the user currencies
        String[] fullCurrencyList = getResources().getStringArray(R.array.currency_list);
        for (int i = 0; i < userCurrencyCodes.length; i++) {
            for (int j = 0; j < fullCurrencyList.length; j++) {
                if (fullCurrencyList[j].split(" ")[0].contentEquals(userCurrencyCodes[i])) {
                    userCurrencyNames[i] = fullCurrencyList[j];
                    break;
                }
            }
        }

        // get main title resource, and set text, text color and visibility (will be shown at end of Usercircle animation)
        title = (TextView) findViewById(R.id.title);
        title.setVisibility(View.GONE);
        title.setText(shortName);
        title.setTextColor(ring_color);

        // get subtitle resource, and set text and visibility (will be shown at end of Usercircle animation)
        sub_title = (TextView) findViewById(R.id.sub_title);
        sub_title.setVisibility(View.GONE);
        sub_title.setText(fullName.toUpperCase());

        // hide currency information until Usercircle animation ends
        currencyLayout = (LinearLayout) findViewById(R.id.currencyAmountHeader);
        currencyLayout.setVisibility(View.GONE);

        // set currency handle
        currency = (TextView) findViewById(R.id.currency);
        currency.setText(userCurrencyNames[viewPosition]);

        // set total value
        total = (TextView) findViewById(R.id.total);
        float amount = db.getUserCurrencyTotal(userId, userCurrencyCodes[viewPosition]);
        total.setText(String.format("%.2f", Math.abs(amount)));

        // set text total to green if positive, red if negative balance
        if (amount >= 0) {
            total.setTextColor(getResources().getColor(R.color.tagGreen));
        } else {
            total.setTextColor(getResources().getColor(R.color.tagRed));
        }

        // set all listView items
        list = (EnhancedListView) findViewById(R.id.summary_list);
        // hide listview until animation is complete
        list.setVisibility(View.GONE);
        // get FromUser cursor
        SummaryCursor = db.getSummaryCursor(userId, userCurrencyCodes[viewPosition]);
        adapter = new SimpleCursorAdapter(this, // Context.
                R.layout.child_list_user, // Specify the row template to use
                SummaryCursor, // Pass in the cursor to bind to.
                // Array of cursor columns to bind to.
                new String[] { "from_user_name", "to_user_name", "amount", "description"}, //from fields
                // Parallel array of which template objects to bind to those
                // columns.
                new int[] { R.id.from_user,R.id.to_user, R.id.amount, R.id.desc},0);

        // set viewbinder for adapter so that amount values show correctly
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int column) {
                TextView tv = (TextView) view;
                String from_user = cursor.getString(cursor.getColumnIndex("from_user_name"));
                switch (view.getId()) {
                    case R.id.from_user:
                        if (from_user.contentEquals(fullName)) {
                            tv.setText("");
                            return true;
                        } else {
                            tv.setText(from_user);
                            tv.setTextColor(getResources().getColor(R.color.tagGreen));
                            return true;
                        }
                    case R.id.to_user:
                        String to_user = cursor.getString(cursor.getColumnIndex("to_user_name"));
                        if (to_user.contentEquals(fullName)) {
                            tv.setText("");
                            return true;
                        } else {
                            tv.setText(to_user);
                            tv.setTextColor(getResources().getColor(R.color.tagRed));
                            return true;
                        }
                    case R.id.amount:
                        String dateStr = cursor.getString(cursor.getColumnIndex("amount"));
                        // set number to decimal
                        tv.setText(String.format("%.2f", (float) Integer.parseInt(dateStr) / 100));
                        if (from_user.contentEquals(fullName)) {
                            tv.setTextColor(getResources().getColor(R.color.tagRed));
                        } else {
                            tv.setTextColor(getResources().getColor(R.color.tagGreen));
                        }
                        return true;
                    case R.id.desc:
                        try {
                            String desc = cursor.getString(cursor.getColumnIndex("description")).toUpperCase();
                            if (desc.isEmpty()) {
                                tv.setText(getResources().getText(R.string.default_description));
                            } else {
                                tv.setText(desc);
                            }
                        } catch (NullPointerException e) {
                            tv.setText(getResources().getText(R.string.default_description));
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Bind to new adapter.
        list.setAdapter(adapter);

        // set up the swipe-to-discuss "undo" capability
        list.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView enhancedListView, int i) {
//                If you don't want to use undo on the list view, just return null from the onDismiss method.
//                In that case the user won't have the possibility to undo the deletion.
//
//                If you want to give the user the possibility of undo, just return an Undoable from the onDismiss method.
//                You must at least overwrite its undo method. This method will be called when the user presses undo,
//                and you have to reinsert the deleted item into the adapter again.

                // get id of the transaction being removed
                final int trans_id = (int) adapter.getItemId(i);
                // get details of transaction
                Transaction t = db.getTransactionFromTable(trans_id);
                // get amount associated with the transaction
                final int amount = t.getAmount();
                // get currency associated with the transaction
                final String currency = t.getCurrency();
                // set the transaction amount for the transaction to 0 - this will not be pulled through by the summaryCursor
                db.setTransactionAmountToZero(trans_id);
                // get the new cursor excluding the transactions with 0
                Cursor newCursor = db.getSummaryCursor(userId, currency);
                // set adapter to the new cursor
                adapter.changeCursor(newCursor);

                refreshTotal();


                // return an Undoable
                return new EnhancedListView.Undoable() {
                    // Reinsert the item to the adapter
                    @Override
                    public void undo() {
                        // restore transaction amount to the original value - this will now be pulled through by the summaryCursor
                        db.setTransactionAmount(trans_id, amount);
                        // refresh currently shown listView cursor
                        refreshCursor();
                        refreshTotal();
                    }

                    // Delete item completely from your persistent storage
                    @Override
                    public void discard() {
                        // permanently delete the row from the table
                        db.deleteTransactionFromTable(trans_id);
                    }
                };
            }
        });

        // enable swiping to dismiss an item
        list.enableSwipeToDismiss();
        // set the undostyle to allow multiple undos
        list.setUndoStyle(EnhancedListView.UndoStyle.MULTILEVEL_POPUP);

        TypedValue tv = new TypedValue();
        int actionBarHeight = -1;

        // get displayed height of toolbar so that animation can be positioned below it
        if (this.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        TranslateAnimation anim = new TranslateAnimation(0,-x_loc + convertToPx(10),0,-y_loc + actionBarHeight + convertToPx(10));
        anim.setDuration(1000);
        anim.setFillAfter(true);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        user.startAnimation(anim);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                title.setVisibility(View.VISIBLE);
                sub_title.setVisibility(View.VISIBLE);
                list.setVisibility(View.VISIBLE);
                currencyLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_deleteUser:
                if (db.hasTransactions(userId)) {
                    showConfirmDeleteUserDialog(getResources().getString(R.string.confirm_deleteUser_withTransactions));
                } else {
                    showConfirmDeleteUserDialog(getResources().getString(R.string.confirm_deleteUser_noTransactions));
                }
                return true;
            case R.id.action_editAccount:
                startUserDetailsActivity();
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadPreviousList (View v) {
        viewPosition = Math.max(0, viewPosition-1);

        currency.setText(userCurrencyNames[viewPosition]);

        refreshTotal();

        refreshCursor();

    }

    public void loadNextList (View v) {
        viewPosition = Math.min(viewPosition + 1, userCurrencyNames.length - 1);

        currency.setText(userCurrencyNames[viewPosition]);

        refreshTotal();

        refreshCursor();
    }

    public void refreshCursor() {

        Cursor c = db.getSummaryCursor(userId, userCurrencyCodes[viewPosition]);
        adapter.changeCursor(c);
        adapter.notifyDataSetChanged();
    }

    public void refreshTotal() {

        float amount = db.getUserCurrencyTotal(userId, userCurrencyCodes[viewPosition]);
        total.setText(String.format("%.2f", Math.abs(amount)));

        // set text total to green if positive, red if negative balance
        if (amount >= 0) {
            total.setTextColor(getResources().getColor(R.color.tagGreen));
        } else {
            total.setTextColor(getResources().getColor(R.color.tagRed));
        }
    }

    public void deleteUserInformation() {
        db.deleteUserTransactions(userId);
        db.deleteUser(userId);
        finish();
    }

    public void showConfirmDeleteUserDialog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(message);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteUserInformation();
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_childcirclesummary, menu);
        return true;
    }

    public void unpackIntent(Intent i) {

        // get the extras
        circle = i.getParcelableExtra("UserCircle_parcel");
        userInfo = circle.getUserInfo();
        userId = userInfo.getId();
        shortName = userInfo.getShortName();
        fullName = userInfo.getFullName();
        ring_color = userInfo.getRingColor();
        width = convertToPx(40) * 2;
        height = convertToPx(40) * 2;
        orig_radius = circle.getRadius();
        x_loc = circle.getleftOffset() + (orig_radius-(width/2));
        y_loc = circle.getTopOffset() + (orig_radius-(height/2));

        // inflate the pseudo UserCircle from the layout xml
        LayoutInflater inflater = LayoutInflater.from(this);
        TextView t = (TextView) inflater.inflate(R.layout.child_pseudo_flip, null);
        // set pseudo UserCircle padding
        t.setPadding(convertToPx(4), convertToPx(4), convertToPx(4), convertToPx(4));
        // set pseudo UserCircle text and gravity
//        t.setText(shortName);
        t.setGravity(Gravity.CENTER);


        // CREATE LAYER LIST BACKGROUND DRAWABLE:

        // get white back circle
        GradientDrawable d1 = (GradientDrawable) getResources().getDrawable(R.drawable.circle_bottom);
        d1.setColor(ring_color);
        t.setBackground(d1);
//
//        // get outer ring and set colour
//        GradientDrawable d2 = (GradientDrawable) getResources().getDrawable(R.drawable.circle_big);
//        d2.setStroke(convertToPx(3), ring_color);
//
//        // assemble drawable and set as background
//        LayerDrawable layer = new LayerDrawable(new Drawable[]{d1,d2});
//        t.setBackground(layer);

        // set view to global variable for use in animation
        user = t;

        // get main layout of content view
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.background_layout);
        // define the width and height of the circle to be added
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        // set offsets
        params.leftMargin = x_loc;
        params.topMargin = y_loc;
        // add view
        relativeLayout.addView(t,params);
    }

    public void startUserDetailsActivity() {
//        User u = new User(userId,ring_color,shortName,fullName);
        Intent i = new Intent(this,AddUserActivity.class);
        i.putExtra("user_info",userInfo);
        startActivity(i);
    }

    public int convertToPx(int dp) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    // to be used for description popups?
    public void PopUpTest(View v) {
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.desc_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
//        popupWindow.showAtLocation(findViewById(R.id.background_layout), Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(v);

    }

}
