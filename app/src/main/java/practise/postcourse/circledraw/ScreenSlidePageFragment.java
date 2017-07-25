/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package practise.postcourse.circledraw;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A fragment representing a single step in a wizard. The fragment shows a dummy title indicating
 * the page number, along with some dummy text.
 *
 */


public class ScreenSlidePageFragment extends Fragment {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_USERS = "user_position";
    public static final String ARG_MAIN_USER = "user_in_focus";
    public static final String ARG_MAIN_USER_DATA = "user_data";

    private User mUser_obj;
    private ArrayList<Total> mUserData;
    public ArrayList<Total> mUserDataToDelete;
    RVAdapter adapter;
    RecyclerView rv;
    TextView default_text;

    public void update() {
        for (Total t: mUserDataToDelete) {
            mUserData.remove(t);
        }
        mUserDataToDelete.clear();
        if (mUserData.isEmpty()) {
            //show default empty text view
            default_text.setVisibility(View.VISIBLE);
        }
        adapter = new RVAdapter(mUserData, this);
        rv.setAdapter(adapter);
    }

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ScreenSlidePageFragment create(int userNumber, ArrayList<User> users, ArrayList<Total> data) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        // get user information and ID, to be used to filter stored data
        User u = users.get(userNumber);
        String id = u.getId();
        // init new array to stored the filtered user data
        ArrayList<Total> temp_data = new ArrayList<>();
        // populate array with data relevant to user
        for (Total item: data) {
            if (item.getFromID().matches(id)) {
                temp_data.add(item);
            }
        }
        // create and populate Bundle
        Bundle args = new Bundle();
        args.putParcelable(ARG_MAIN_USER, users.get(userNumber));
        args.putParcelableArrayList(ARG_MAIN_USER_DATA, temp_data);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser_obj = getArguments().getParcelable(ARG_MAIN_USER);
        mUserData = getArguments().getParcelableArrayList(ARG_MAIN_USER_DATA);
        //initialise arraylist to store deletable total objects (i.e. flipped objects)
        mUserDataToDelete = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        MySQLiteHelper db = new MySQLiteHelper(getActivity());

        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.alluser_summary_main, container, false);

        // get circle handle
        TextView circle = (TextView) rootView.findViewById(R.id.circle_drawable);
        // set background circle and color
        GradientDrawable drawable = (GradientDrawable) getResources().getDrawable(R.drawable.circle_bottom);
        drawable.setColor(mUser_obj.getRingColor());
        circle.setBackground(drawable);

        // Set title text and color
        TextView title = (TextView) rootView.findViewById(R.id.title);
        title.setText(mUser_obj.getShortName());
        title.setTextColor(mUser_obj.getRingColor());

        // set subtitle text
        TextView sub_title = (TextView) rootView.findViewById(R.id.sub_title);
        sub_title.setText(mUser_obj.getFullName().toUpperCase());

        default_text = (TextView) rootView.findViewById(R.id.empty_default);
        if (!mUserData.isEmpty()) {
            //hide default empty text view
            default_text.setVisibility(View.GONE);
            // get recycler view handle
            rv = (RecyclerView) rootView.findViewById(R.id.card_layout);
            // declare the view to have an unchanging size
            rv.setHasFixedSize(true);
            // define layout of items in recyclerView (linear in this case)
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            rv.setLayoutManager(llm);

            adapter = new RVAdapter(mUserData, this);
            rv.setAdapter(adapter);
        }

        return rootView;
    }

}


    class RVAdapter extends RecyclerView.Adapter<RVAdapter.DataViewHolder>{

        ArrayList<Total> data;
        ScreenSlidePageFragment fragment;


        RVAdapter(ArrayList<Total> array, ScreenSlidePageFragment fragment){
            this.data = array;
            this.fragment = fragment;
        }

        public class DataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            CardView cardFront;
            CardView cardBack;
            TextView user, amount, currency;
            TextView amount_back, currency_back;
            boolean backVisible;
            AnimatorSet setRightOut, setLeftIn;

            DataViewHolder(View itemView, Context c) {
                super(itemView);
                cardFront = (CardView)itemView.findViewById(R.id.card_front);
                user = (TextView) itemView.findViewById(R.id.user);
                amount = (TextView)itemView.findViewById(R.id.amount);
                currency = (TextView)itemView.findViewById(R.id.currency_code);
                cardBack = (CardView)itemView.findViewById(R.id.card_back);
                amount_back = (TextView) itemView.findViewById(R.id.amount_back);
                currency_back = (TextView) itemView.findViewById(R.id.currency_back);

                itemView.setOnClickListener(this);
                // initialise the animations to be used to flip the cards
                setRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(c, R.animator.flip_right_out);
                setLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(c, R.animator.flip_left_in);
            }

            @Override
            public void onClick(View v) {
                if (!setRightOut.isRunning() && !setLeftIn.isRunning()) {
                    Total totalItem = data.get(this.getAdapterPosition());
                    if (!backVisible) {
                        setRightOut.setTarget(cardFront);
                        setLeftIn.setTarget(cardBack);
                        setRightOut.start();
                        setLeftIn.start();

                        fragment.mUserDataToDelete.add(totalItem);
                    } else {
                        setRightOut.setTarget(cardBack);
                        setLeftIn.setTarget(cardFront);
                        setRightOut.start();
                        setLeftIn.start();

                        fragment.mUserDataToDelete.remove(totalItem);
                    }
                    backVisible = !backVisible;
                }
            }

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.alluser_summary_card, viewGroup, false);
            DataViewHolder dvh = new DataViewHolder(v, viewGroup.getContext());
            return dvh;
        }

        @Override
        public void onBindViewHolder(DataViewHolder dvh, int i) {
            dvh.user.setText(String.valueOf(data.get(i).getToUser()));
            dvh.amount.setText(String.format("%.2f", (float) data.get(i).getAmount() / 100));
            dvh.currency.setText(data.get(i).getCurrency());
            dvh.amount_back.setText(String.format("%.2f", (float) data.get(i).getAmount() / 100));
            dvh.currency_back.setText(data.get(i).getCurrency());
        }

    }
