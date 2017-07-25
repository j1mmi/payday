package practise.postcourse.circledraw;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;

import eu.davidea.flipview.FlipView;

/**
 * Created by Jim on 05/01/2016.
 */
public class UserCircle implements Parcelable{

    private String fullName, shortName;
    private int leftOffsetPx, topOffsetPx;
    private int radiusPx;
    private int ringColor;
    private final String TAG = "UserCircle";
    private TextView t;
    private FlipView view;
    private String id;
    private int rearColor = Color.parseColor("#eb3e3e");
    private User userInfo;

    public UserCircle(FlipView v, int leftOffsetPx, int topOffsetPx, int radiusPx, User userInfo) {
        this.view = v;
        this.leftOffsetPx = leftOffsetPx;
        this.topOffsetPx = topOffsetPx;
        this.radiusPx = radiusPx;
        this.userInfo = userInfo;
    }

    public UserCircle(Parcel in) {
        this.view = null;
        this.leftOffsetPx = in.readInt();
        this.topOffsetPx = in.readInt();
        this.radiusPx = in.readInt();
        this.userInfo = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(leftOffsetPx);
        dest.writeInt(topOffsetPx);
        dest.writeInt(radiusPx);
        dest.writeParcelable(userInfo, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<UserCircle> CREATOR = new Parcelable.Creator<UserCircle>() {

        @Override
        public UserCircle createFromParcel(Parcel in) {
            return new UserCircle(in);
        }

        @Override
        public UserCircle[] newArray(int size) {
            return new UserCircle[size];
        }
    };

    // ACCESSOR METHODS

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public int getleftOffset() {
        return this.leftOffsetPx;
    }
    public void setleftOffset(int leftOffsetPx) {
        this.leftOffsetPx = leftOffsetPx;
    }

    public int getTopOffset() {
        return this.topOffsetPx;
    }
    public void setTopOffset(int topOffsetPx) {
        this.topOffsetPx = topOffsetPx;
    }

    public int getRadius() {
        return this.radiusPx;
    }
    public void setRadius(int radiusPx) {
        this.radiusPx = radiusPx;
    }

    public String getFullName() {
        return this.fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return this.shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public FlipView getView() {
        return this.view;
    }
    public void setView(FlipView v) {
        this.view = v;
    }

    public int getRingColor() {
        return this.ringColor;
    }
    public void setRingColor(int color) {
        this.ringColor = color;
    }

    public User getUserInfo() {
        return this.userInfo;
    }
    public void setUserInfo(User newInfo) {
        this.userInfo = newInfo;
    }

    public void showTransactionPopup(Context c, RelativeLayout parentLayout, String text, int color) {
        // here you set the textView
        if (t!=null) {
            t.setText(text);
            t.setTextColor(color);
            t.setVisibility(View.VISIBLE);
        } else {
            t = new TextView(c);
            t.setText(text);
            t.setTextColor(color);
            t.setTypeface(null, Typeface.BOLD);
            t.setGravity(Gravity.CENTER);
            // define the width and height of the circle to be added
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(radiusPx * 2,radiusPx * 2);
            // define the absolute offsets from the top and left of the screen
            params.leftMargin = leftOffsetPx;
            params.topMargin = topOffsetPx; // - (radiusPx * 2);
//            Log.d(TAG, "leftMargin: " + leftOffsetPx);
//            Log.d(TAG, "topMargin: " + (topOffsetPx - (radiusPx * 2)));
            // create view as defined above
            parentLayout.addView(t, params);
        }

        Animation anim = AnimationUtils.loadAnimation(c, R.anim.translate);
        t.startAnimation(anim);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // disable user input while animation is in progress
                view.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation.cancel();
                // hide textview
                t.setVisibility(View.GONE);
                // flip usercircle to front
                view.flipSilently(false);
                // reset back colour of flipview to red
                view.setChildBackgroundColor(1, rearColor);
                // enable user input
                view.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    @Override
    public String toString() {
        return userInfo.getFullName();
    }

}
