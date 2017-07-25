package practise.postcourse.circledraw;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import eu.davidea.flipview.FlipView;

public class MainActivity extends AppCompatActivity implements TransactionEntryDialog.OnCompleteListener,
        UndoConfirmDialog.OnCompleteListener, CurrencyListDialog.OnCompleteListener, DefaultCurrencyDialog.OnCompleteListener {

    final static String TAG = "MainActivity";
    final static String TAG_UNDODATA = "HISTORY";
    final static String TAG_CURRLIST = "CURRENCIES";
    final static String TAG_CURRDEFAULT = "CURRENCY_DEFAULT";
    final static int NUM_CURRENCIES = 12;
    private MySQLiteHelper db;
    private RelativeLayout parentLayout;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;
    Toolbar Toolbar;
    private DrawerLayout Drawer;
    Cursor AllUserCursor;
    int w, h;  // height and width of screen
    int GUIDE_CIRCLE_OFFSET_X;
    int GUIDE_CIRCLE_OFFSET_Y;
    int SIDE_MARGINS;
    int numUsers;
    int CHILD_RING_WIDTH_DP;
    int CHILD_RING_WIDTH_PX;
    int CHILD_RADIUS_DP;
    int CHILD_RADIUS_PX;
    // this is the list of historical transactions ids.  the last array is the latest set of transactions
    // e.g. [[11,12,13],[14,15,16,17,18,19,20]....]
    private ArrayList<Integer[]> historyData = new ArrayList<>();
    // list of circles that are currently selected (i.e. have rear view showing)
    private ArrayList<UserCircle> flippedCircles = new ArrayList<>();
    // boolean list of the currently selected currency codes to use (true = selected, false - not selected)
    private boolean[] currencyList;
    private String defaultCurrency; // this is the full name of the default currency e.g. "EUR - Euro"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set layout
        setContentView(R.layout.activity_main);
        // set action bar / toolbar
        Toolbar = (Toolbar) findViewById(R.id.my_toolbar);

        Toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(Toolbar);

        // set drawer
        Drawer = (DrawerLayout) findViewById(R.id.drawer);

        Drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
             @Override
             public void onDrawerSlide(View drawerView, float slideOffset) {

             }

             @Override
             public void onDrawerOpened(View drawerView) {
                 invalidateOptionsMenu();
             }

             @Override
             public void onDrawerClosed(View drawerView) {
             }

             @Override
             public void onDrawerStateChanged(int newState) {

             }
         });

        // set child circle ring width
        CHILD_RING_WIDTH_DP = 3;
        CHILD_RING_WIDTH_PX = convertToPx(CHILD_RING_WIDTH_DP);

        // get screen width and height (in pixels)
        w = getScreenWidth();
        h = getScreenHeight();

        //get database reference
        db = new MySQLiteHelper(this);

        // initialise sharedPreferences
        sharedPrefs = this.getSharedPreferences("CircleDraw", Context.MODE_PRIVATE);
        editor = sharedPrefs.edit();


////        DB DEBUG
//        db.clearUserTable();
////        db.createNewUser("0000-0000", "Jim", "Jim Renwick", "#CC9900");
//        db.clearTransactionTable();
////        SHARED PREFS DEBUG
//        editor.putString(TAG_UNDODATA,null);
//        editor.commit();

        // get all users from SQL table
        AllUserCursor = db.getAllUsers();

        // get number of users and get corresponding coordinates for positioning in the parent circle
        numUsers = AllUserCursor.getCount();

        // set the child radius based upon the number of current users
        switch (numUsers) {
            case 1:
                CHILD_RADIUS_DP = 55;
                break;
            case 2:
                CHILD_RADIUS_DP = 55;
                break;
            case 3:
                CHILD_RADIUS_DP = 55;
                break;
            case 4:
                CHILD_RADIUS_DP = 50;
                break;
            case 5:
                CHILD_RADIUS_DP = 50;
                break;
            case 6:
                CHILD_RADIUS_DP = 50;
                break;
            case 7:
                CHILD_RADIUS_DP = 45;
                break;
            case 8:
                CHILD_RADIUS_DP = 45;
                break;
            case 9:
                CHILD_RADIUS_DP = 40;
                break;
            case 10:
                CHILD_RADIUS_DP = 40;
                break;
            default:
                CHILD_RADIUS_DP = 40;
        }

        // set side margins based on child radius (+ 10 additional pixels for buffer)
        CHILD_RADIUS_PX = convertToPx(CHILD_RADIUS_DP);
        SIDE_MARGINS = CHILD_RADIUS_PX + 10;

        int guideCircleRadius = Math.round((w - SIDE_MARGINS * 2) / 2); // radius of guide circle (-minus the two side margins)
        GUIDE_CIRCLE_OFFSET_X = SIDE_MARGINS; // distance from each side of the window
        GUIDE_CIRCLE_OFFSET_Y = SIDE_MARGINS + (h / 6); // distance from each top / bottom of the window

        // get parent view to load guide circle onto
        parentLayout = (RelativeLayout) findViewById(R.id.background_layout);

        // construct guide circle and place in parent view
        TextView guideCircle = new TextView(this);
        guideCircle.setBackgroundResource(R.drawable.backcircle);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(guideCircleRadius*2, guideCircleRadius*2);
//        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.leftMargin = GUIDE_CIRCLE_OFFSET_X;
        params.topMargin = GUIDE_CIRCLE_OFFSET_Y;
        parentLayout.addView(guideCircle, params);

        if (numUsers > 0) {
            // get corresponding coordinates for positioning in the parent circle
            int[][] array = getCoordinatesFromCentre(guideCircleRadius, numUsers);

            // iterate through cursor and create user circles
            int i = 0;
            if (AllUserCursor.moveToFirst()) {
                String id;
                int color;
                String shortText;
                String longText;
                String email;
                do {
                    id = AllUserCursor.getString(AllUserCursor.getColumnIndex("id"));
                    color = AllUserCursor.getInt(AllUserCursor.getColumnIndex("color"));
                    shortText = AllUserCursor.getString(AllUserCursor.getColumnIndex("short_name"));
                    longText = AllUserCursor.getString(AllUserCursor.getColumnIndex("full_name"));
                    email = AllUserCursor.getString(AllUserCursor.getColumnIndex("email"));
                    createUserCircle(parentLayout, guideCircleRadius, array[i][1], array[i][0], CHILD_RADIUS_PX,
                            new User(id, color, shortText, longText, email));
                    i++;
                } while (AllUserCursor.moveToNext());
            }
        }
    }

    public void createUserCircle(RelativeLayout parentLayout, int parentRadiusPx, int xOffset, int yOffset, int childRadiusPx, User userInfo) {

        // inflate the main flipView from the layout xml
        LayoutInflater inflater = LayoutInflater.from(this);
        FlipView flip = (FlipView) inflater.inflate(R.layout.child_flip, null);

        // try to locate and display image associated with this user - otherwise display generic pic
        File f = this.getFileStreamPath(userInfo.getId() + ".jpg");
        RoundedBitmapDrawable bmp;
        if (f.exists()) {
            bmp = RoundedBitmapDrawableFactory.create
                    (this.getResources(), BitmapFactory.decodeFile(f.toString()));
        } else {
            bmp = RoundedBitmapDrawableFactory.create
                    (this.getResources(), BitmapFactory.decodeResource(this.getResources(), R.drawable.default_profile));
        }
        bmp.setCircular(true);

        // overlay outer ring and set to user colour
        GradientDrawable d3 = (GradientDrawable) getResources().getDrawable(R.drawable.circle_big);
        d3.setStroke(convertToPx(CHILD_RING_WIDTH_DP), userInfo.getRingColor());
//
        LayerDrawable layer = new LayerDrawable(new Drawable[]{bmp,d3});

        // set this layer list as the background
        flip.setChildBackgroundDrawable(0, layer);

        // set on flip listener - on flip the flipped circle gets added to the flippedCircles array
        flip.setOnFlippingListener(new FlipView.OnFlippingListener() {
            @Override
            public void onFlipped(FlipView flipView, boolean checked) {
                if (checked) {
                    flippedCircles.add((UserCircle) flipView.getTag());
//                    System.out.println("IN");
                } else {
                    flippedCircles.remove(flipView.getTag());
//                    System.out.println("OUT");
                }
            }
        });
        // set on long click listener - lauches dialog fragment for long clicked circle
        flip.setOnLongClickListener(new OnLongClickTransactionListener(currencyList) {
            @Override
            public boolean onLongClick(View view) {
                // Long touch to declare who is owed the money, and/or to open their summary breakdown
                // 1st option when other users are flipped, and 2nd option when not
                if (!((FlipView) view).isFlipped()) {
                    // if circle is not already flipped, and there are other circles flipped, show dialog
                    if (!flippedCircles.isEmpty()) {
                        TransactionEntryDialog dialog = TransactionEntryDialog.create
                                (flippedCircles, (UserCircle) view.getTag(), currencyList, defaultCurrency);
                        dialog.show(getFragmentManager(), "Currency Entry");
                        return true;
                    } else // otherwise if circle is not already flipped, and there are not other circles flipped,
                    // then start summary screen for circle
                    { Intent i = new Intent(getApplication(),ChildCircleSummary.class);
                        UserCircle c = (UserCircle) view.getTag();
                        i.putExtra("UserCircle_parcel", c);
                        startActivity(i);
                        return true;
                    }
                }
                return false;
            }
        });


        // define the width and height of the circle to be added
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(childRadiusPx * 2,childRadiusPx * 2);
        // define the absolute offsets from the top and left of the screen
        int leftMargin = GUIDE_CIRCLE_OFFSET_X  + parentRadiusPx + xOffset - childRadiusPx;
        int topMargin = GUIDE_CIRCLE_OFFSET_Y + parentRadiusPx - yOffset - childRadiusPx;
        // set offsets
        params.leftMargin = leftMargin;
        params.topMargin = topMargin;
        // tag userCircle object to view - this will be pulled on later interactions id, color, shortText, longText
        flip.setTag(new UserCircle(flip, leftMargin, topMargin, childRadiusPx, userInfo));
        // create view as defined above
        parentLayout.addView(flip, params);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get historyData from SharedPrefs
        String jsonUndoList = sharedPrefs.getString(TAG_UNDODATA, null);
        Gson gsonUndo = new Gson();
        Integer[][] rawUndoList = gsonUndo.fromJson(jsonUndoList, Integer[][].class);
        if (rawUndoList != null) {
            historyData.clear();
            for (Integer[] array : rawUndoList) {
                historyData.add(array);
            }
        }

        // get currencyList from SharedPrefs - if none found, set currencyList as new array of false values
        String jsonCurrencyList = sharedPrefs.getString(TAG_CURRLIST, null);
        Gson gsonCurr = new Gson();
        currencyList = gsonCurr.fromJson(jsonCurrencyList, boolean[].class);
        if (currencyList == null) {
            currencyList = new boolean[NUM_CURRENCIES];
        }

        // get defaultCurrency for SharedPrefs - if none found, set as "DEFAULT"
        defaultCurrency = sharedPrefs.getString(TAG_CURRDEFAULT,"DEFAULT");

        // get all users from SQL table
        AllUserCursor = db.getAllUsers();

        if (numUsers != AllUserCursor.getCount()) {
            for (int i = 0; i < parentLayout.getChildCount(); i++){
                if (parentLayout.getChildAt(i) instanceof FlipView || parentLayout.getChildAt(i) instanceof TextView){
                    parentLayout.removeView(parentLayout.getChildAt(i));
                    i--;
                }
            }
            numUsers = AllUserCursor.getCount();
            // set the child radius based upon the number of current users
            switch (numUsers) {
                case 1:
                    CHILD_RADIUS_DP = 55;
                    break;
                case 2:
                    CHILD_RADIUS_DP = 55;
                    break;
                case 3:
                    CHILD_RADIUS_DP = 55;
                    break;
                case 4:
                    CHILD_RADIUS_DP = 50;
                    break;
                case 5:
                    CHILD_RADIUS_DP = 50;
                    break;
                case 6:
                    CHILD_RADIUS_DP = 50;
                    break;
                case 7:
                    CHILD_RADIUS_DP = 45;
                    break;
                case 8:
                    CHILD_RADIUS_DP = 45;
                    break;
                case 9:
                    CHILD_RADIUS_DP = 40;
                    break;
                case 10:
                    CHILD_RADIUS_DP = 40;
                    break;
                default:
                    CHILD_RADIUS_DP = 40;
            }

            // set side margins based on child radius (+ 10 additional pixels for buffer)
            CHILD_RADIUS_PX = convertToPx(CHILD_RADIUS_DP);
            SIDE_MARGINS = CHILD_RADIUS_PX + 10;

            int guideCircleRadius = Math.round((w - SIDE_MARGINS * 2) / 2); // radius of guide circle (-minus the two side margins)
            GUIDE_CIRCLE_OFFSET_X = SIDE_MARGINS; // distance from each side of the window
            GUIDE_CIRCLE_OFFSET_Y = SIDE_MARGINS + (h / 6); // distance from each top / bottom of the window

            // get parent view to load guide circle onto
            parentLayout = (RelativeLayout) findViewById(R.id.background_layout);

            // construct guide circle and place in parent view
            TextView guideCircle = new TextView(this);
            guideCircle.setBackgroundResource(R.drawable.backcircle);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(guideCircleRadius*2, guideCircleRadius*2);
            params.leftMargin = GUIDE_CIRCLE_OFFSET_X;
            params.topMargin = GUIDE_CIRCLE_OFFSET_Y;
            parentLayout.addView(guideCircle, params);

            if (numUsers > 0) {
                // get corresponding coordinates for positioning in the parent circle
                int[][] array = getCoordinatesFromCentre(guideCircleRadius, numUsers);

                // iterate through cursor and create user circles
                int i = 0;
                if (AllUserCursor.moveToFirst()) {
                    String id;
                    int color;
                    String shortText;
                    String longText;
                    String email;
                    do {

                        id = AllUserCursor.getString(AllUserCursor.getColumnIndex("id"));
                        color = AllUserCursor.getInt(AllUserCursor.getColumnIndex("color"));
                        shortText = AllUserCursor.getString(AllUserCursor.getColumnIndex("short_name"));
                        longText = AllUserCursor.getString(AllUserCursor.getColumnIndex("full_name"));
                        email = AllUserCursor.getString(AllUserCursor.getColumnIndex("email"));
                        createUserCircle(parentLayout, guideCircleRadius, array[i][1], array[i][0], CHILD_RADIUS_PX,
                                new User(id, color, shortText, longText, email));
                        i++;
                    } while (AllUserCursor.moveToNext());
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_undoTransaction:
                ArrayList<Transaction> last_undo_data = getTransactionsForUndo();
                if (!last_undo_data.isEmpty()) {
                    // get transactionDB row ids of last transaction
                    Integer[] rawLastUndoData = historyData.get(historyData.size() - 1);
                    // start confirmation dialog
                    UndoConfirmDialog dialog = UndoConfirmDialog.create(rawLastUndoData, last_undo_data);
                    dialog.show(getFragmentManager(), "Undo Confirm");
                } else {
                    Toast.makeText(this,"Nothing to undo.",Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_settleUsers:
                intent = new Intent(this,SettleUsersActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_toc:
                Drawer.openDrawer(Gravity.RIGHT);
                return true;
            default:
                super.onOptionsItemSelected(item);
        }


        return super.onOptionsItemSelected(item);
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int[][] getCoordinatesFromCentre(int radius, int circleNums) {
        if (circleNums == 0) {
            throw new IllegalStateException("Cannot have zero UserCircles!");
        }
        double segmentAngle = 360 / circleNums;
        Log.d(TAG,"angle = " + segmentAngle);
        double angle = segmentAngle; //copy segmentAngle for use in loop
        int[][] array = new int[circleNums][2];
        array[0][0] = radius; // y co-ordinate
        array[0][1] = 0; // x co-ordinate
        for (int i = 1; i < circleNums; i++) {
            array[i][1] =  Math.round((float) Math.sin(Math.toRadians(angle)) * radius); // x co-ordinate
            array[i][0] = Math.round((float) Math.sin(Math.toRadians(90-angle)) * radius); // y co-ordinate
            angle += segmentAngle; // move on to next segment
//            Log.d(TAG, "i= " + i + "; y: " + array[i][0] + "; x: " + array[i][1]);
        }
        return array;
    }

    public int convertToPx(int dp) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }

    // called when OK on the TransactionEntryDialog is pressed
    public void onCompleteCurrencyEntry(UserCircle target, String target_value,
                           ArrayList<UserCircle> owers, String ower_standard, String ower_plus, String currency, String description) {

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        // use boolean to track if the plus value has been added
        boolean differenceAdded = false;
        // get target rear colour to green
        target.getView().setChildBackgroundColor(1,getResources().getColor(R.color.colorPrimary));
        // show rear of user circle
        target.getView().flipSilently(true);
        // show animation for target
        target.showTransactionPopup(this, parentLayout, target_value + "\n" + currency, Color.parseColor("#FFFFFF"));
        // initialise transaction object to pass to SQL table
        Transaction t;
        // initialise array object to hold generated transaction _ids for undo later
        ArrayList<Integer> transactionList = new ArrayList<>();
        for (UserCircle circle : owers) {
            // for first ower (who is paying extra):
            if (!differenceAdded) {
                // show animation for ower
                circle.showTransactionPopup(this, parentLayout, ower_plus + "\n" + currency, Color.parseColor("#FFFFFF"));
                // create transaction object (from,to,amount)
                t = new Transaction(circle.getUserInfo().getId(),circle.getUserInfo().getFullName(),
                        target.getUserInfo().getId(),target.getUserInfo().getFullName(),
                        Math.round(Float.parseFloat(ower_plus)*100), currency, description);
                // show that difference has now been added
                differenceAdded = true;
            }else {
                // for all others owers...
                // show animation for ower
                circle.showTransactionPopup(this, parentLayout, ower_standard + "\n" + currency, Color.parseColor("#FFFFFF"));
                // create transaction object (from,to,amount)
                t = new Transaction(circle.getUserInfo().getId(), circle.getUserInfo().getFullName(),
                        target.getUserInfo().getId(), target.getUserInfo().getFullName(),
                        Math.round(Float.parseFloat(ower_standard)*100), currency, description);
            }
            // commit transaction to DB and get _id
            int transactionID = db.addTransactionToTable(t);

            // add _id to array for undo later
            transactionList.add(transactionID);

//            // reset circles
//            circle.getView().flipSilently(false);
        }
        flippedCircles.clear();
        // convert transactionList (ArrayList<Integer>) to Integer[] and add to main undoList
        Integer[] temp = new Integer[transactionList.size()];
        historyData.add(transactionList.toArray(temp));

        // store new history data in SharedPrefs for retrieval later
        storeData(historyData, TAG_UNDODATA);

    }

    private void storeData(ArrayList<Integer[]> dataToStore, String tag) {
        // convert undoList to storable string and store in SharedPrefs, for retrieval later
        Gson gson = new Gson();
        String jsonUndoList = gson.toJson(dataToStore);
        editor.putString(tag, jsonUndoList);
        editor.commit();
    }

    private void storeData(boolean[] dataToStore, String tag) {
        // convert currencyList to storable string and store in SharedPrefs, for retrieval later
        Gson gson = new Gson();
        String jsonUndoList = gson.toJson(dataToStore);
        editor.putString(tag, jsonUndoList);
        editor.commit();
    }

    private ArrayList<Transaction> getTransactionsForUndo() {
        // create blank Transaction ArrayList to store the values
        ArrayList<Transaction> array = new ArrayList<>();
        // if there is a history of recorded transactions
        if (!historyData.isEmpty()) {
            do {
                // get transactionDB row ids of last transaction
                Integer[] rawLastUndoData = historyData.get(historyData.size() - 1);
                // iterate over entries and pull out the relevant transactions from the db
                for (int i = 0; i < rawLastUndoData.length; i++) {
                    // try and get transaction from DB
                    Transaction t = db.getTransactionFromTable(rawLastUndoData[i]);
                    // the transaction returned is not null (i.e. has not been deleted), add it to the array
                    if (t!=null) {
                        array.add(db.getTransactionFromTable(rawLastUndoData[i]));
                    }
                }
                // if all entries in the undo set have been deleted, remove the set from history list
                if (array.isEmpty()) {
                    historyData.remove(historyData.size() - 1);
                }
            } while (array.isEmpty()&& !historyData.isEmpty());
        }
        // return resulting transaction array (will be empty if all of undo history has been deleted)
        return array;
    }

    // called when OK on the UndoConfirmDialog is pressed
    public void onCompleteUndoConfirmation(int[] IDsToDelete) {
        // delete each id in the supplied array from the DB
        for (int i : IDsToDelete) {
            db.deleteTransactionFromTable(i);
        }
        // delete entries from history list
        historyData.remove(historyData.size() - 1);
        // store new undoList in SharedPrefs
        storeData(historyData, TAG_UNDODATA);
    }

    public void setCurrencies(View v) {
        CurrencyListDialog currencyDialog = CurrencyListDialog.create(currencyList);
        currencyDialog.show(getFragmentManager(), "Set Currency");
    }

    // called when OK on the CurrencyListDialog is pressed
    public void onCompleteCurrencyListSelection(boolean[] newPrefs){
        currencyList = newPrefs;
        storeData(newPrefs, TAG_CURRLIST);
    }

    public void setDefaultCurrency(View v) {
        DefaultCurrencyDialog defaultCurrDialog = DefaultCurrencyDialog.create(defaultCurrency, currencyList);
        defaultCurrDialog.show(getFragmentManager(), "Default Currency");
    }

    public void onCompleteDefaultCurrencySelection(String newDefaultCurr) {
        defaultCurrency = newDefaultCurr;
        editor.putString(TAG_CURRDEFAULT, newDefaultCurr);
        editor.commit();
    }

    public void addUser(View v) {
        // starts AddUser activity on option clicked in the navigation drawer
        if (numUsers!=10) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent;
                    intent = new Intent(getApplication(), AddUserActivity.class);
                    startActivity(intent);
                }
            }, 250);
            Drawer.closeDrawer(Gravity.RIGHT);
        } else {
            Toast.makeText(this,"Cannot add any more users!",Toast.LENGTH_SHORT).show();
        }
    }

}
