package practise.postcourse.circledraw;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jim on 17/01/2016.
 */
public class SettleUsersActivity extends AppCompatActivity {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private int NUM_USERS;
    private int lastPageIndex;
    private MySQLiteHelper db;
    private ArrayList<User> userArray;
    private ArrayList<Total> summaryData;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private ScreenSlidePagerAdapter mPagerAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settleallusers, menu);
        return true;
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
            case R.id.action_back:
                mPager.setCurrentItem(Math.max(mPager.getCurrentItem()-1, 0));
                return true;
            case R.id.action_forward:
                mPager.setCurrentItem(Math.min(mPager.getCurrentItem()+1, NUM_USERS-1));
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settle_users_main);

        //set action bar / toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar(toolbar);

        db = new MySQLiteHelper(this);
        userArray = new ArrayList<>();

        // get all users from SQL table
        Cursor c = db.getAllUsers();

        // populate userArray with UserCircle information
        if (c.moveToFirst()) {
            do {
                userArray.add(new User(c.getString(c.getColumnIndex("id")),
                        c.getInt(c.getColumnIndex("color")),
                        c.getString(c.getColumnIndex("short_name")),
                        c.getString(c.getColumnIndex("full_name")),
                        c.getString(c.getColumnIndex("email"))));
            } while (c.moveToNext());
        }

        NUM_USERS = userArray.size();

        lastPageIndex = 0;

        // get all users from SQL table
        final ArrayList<Total> rawData = db.getAllSummaryTotals();

        summaryData = reconcileSummaryData(rawData);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state==ViewPager.SCROLL_STATE_IDLE) {
                    int currentIndex = mPager.getCurrentItem();
                    if (currentIndex!=lastPageIndex) {
                        ScreenSlidePageFragment frag = (ScreenSlidePageFragment) mPagerAdapter.getRegisteredFragment(lastPageIndex);
                        if (frag != null && !frag.mUserDataToDelete.isEmpty()) {
                            for (Total t: frag.mUserDataToDelete) {
                                summaryData.remove(t);
                                db.deleteTransactionsFromTable(t.getTransactionIDs());
                            }
                            frag.update();
                        }
                    }
                    lastPageIndex = currentIndex;
                }
            }


        });
    }

    /**
     * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment} objects, in
     * sequence.
     */
    class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public Fragment getItem(int position) {
            return ScreenSlidePageFragment.create(position, userArray, summaryData);
        }

        @Override
        public int getCount() {
            return NUM_USERS;
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }
    }

    private ArrayList<Total> reconcileSummaryData(ArrayList<Total> total_list) {
        // initialise new array to store results
        ArrayList<Total> new_total_list = new ArrayList<>();

        for (int i = 0; i <= 10; i++) {
            // while there are still entries in the total array...
            while(!total_list.isEmpty()) {
                // get first total in arraylist
                Total t = total_list.remove(0);
                // find next matching total in chain
                Total next_t = getNextInChain(t, total_list);
                // if no next matching total in chain was found, store the value and move on
                if (next_t == null) {
                    new_total_list.add(t);
                    continue;
                }
                total_list.remove(next_t);
                // get summarised values
                Total[] reconciled_totals = reconcile(t,next_t);
                // add old t to the new array
                if (reconciled_totals != null) {
                    if (reconciled_totals[0].getAmount() > 0) {
                        new_total_list.add(reconciled_totals[0]);
                    }
                    if (reconciled_totals.length == 2 && reconciled_totals[1].getAmount() > 0) {
                        new_total_list.add(reconciled_totals[1]);
                    }
                }
            }
            Collections.reverse(new_total_list);
            total_list = new_total_list;
            new_total_list = new ArrayList<>();
        }
        return summarise(total_list);
    }

    static ArrayList<Total> summarise(ArrayList<Total> array) {
        ArrayList<Total> summaryArray = new ArrayList<>();
        boolean found;
        for (Total t1: array) {
            found = false;
            for (Total t2: summaryArray) {
                if (t1.getFromID().matches(t2.getFromID()) &&
                   t1.getToID().matches(t2.getToID()) &&
                   t1.getCurrency().matches(t2.getCurrency())) {
                    t2.setAmount(t1.getAmount() + t2.getAmount());
                    found = true;
                    break;
                }
            }
            if (!found) {
                summaryArray.add(t1);
            }
        }
        return summaryArray;
    }

    static Total getNextInChain(Total t, ArrayList<Total> array) {
        String nextUser = t.getToID();
        String chainCurrency = t.getCurrency();
        for (Total item: array) {
            if (item.getFromID().matches(nextUser)&& item.getCurrency().matches(chainCurrency)) return item;
        }
        return null;
    }

    static Total[] reconcile(Total t1, Total t2) {
        int t1_amount = t1.getAmount();
        int t2_amount = t2.getAmount();
        // if matched total are A-B and B-A:
        if (t1.getFromID().matches(t2.getToID())) {
            if (t1_amount < t2_amount ) {
                //SCENARIO 4
                t2.setAmount(t2_amount-t1_amount);
                return new Total[]{t2};
            } else if (t1_amount > t2_amount) {
                //SCENARIO 5
                t1.setAmount(t1_amount - t2_amount);
                return new Total[]{t1};
            } else {
                //SCENARIO 6
                return null;
            }
            // if matched total are different e.g. A-B and B-C:
        } else {
            if (t1_amount < t2_amount ) {
                //SCENARIO 1
                t1.setToID(t2.getToID());
                t1.setToUser(t2.getToUser());
                t2.setAmount(t2_amount-t1_amount);
                return new Total[]{t1,t2};
            } else if (t1_amount > t2_amount) {
                //SCENARIO 2
                t1.setAmount(t1_amount - t2_amount);
                t2.setFromID(t1.getFromID());
                t2.setFromUser(t1.getFromUser());
                return new Total[]{t1,t2};
            } else {
                //SCENARIO 3
                t2.setFromID(t1.getFromID());
                t2.setFromUser(t1.getFromUser());
                return new Total[]{t2};
            }
        }
    }

}
