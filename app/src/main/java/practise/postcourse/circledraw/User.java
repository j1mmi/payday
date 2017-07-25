package practise.postcourse.circledraw;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import eu.davidea.flipview.FlipView;

/**
 * Created by Jim on 05/01/2016.
 */
public class User implements Parcelable{

    // Contains basic user information defined when a new user is added - the id of the user is the same as the user's profile pic.
    // Sits inside the UserCircle class - also used as standalone for settlement procedure.

    private final String TAG = "User";
    private String fullName, shortName;
    private int ringColor;
    private String id;
    private String email;


    public User(String id, int ringColor, String shortName, String fullName, String email) {
        this.id = id;
        this.shortName = shortName;
        this.fullName = fullName;
        this.ringColor = ringColor;
        this.email = email;
    }

    public User(Parcel in) {
        this.id = in.readString();
        this.ringColor = in.readInt();
        this.shortName = in.readString();
        this.fullName = in.readString();
        this.email = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(ringColor);
        dest.writeString(shortName);
        dest.writeString(fullName);
        dest.writeString(email);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {

        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // ACCESSOR METHODS

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
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

    public int getRingColor() {
        return this.ringColor;
    }
    public void setRingColor(int color) {
        this.ringColor = color;
    }

    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return this.getFullName();
    }

    @Override
    public boolean equals(Object o) {
        try {
            User u = (User) o;
            if (u.getId() == this.id
                    && u.getRingColor() == this.ringColor
                    && u.getShortName() == this.shortName
                    && u.getFullName() == this.fullName
                    && u.getEmail() == this.email) {
                return true;
            }
        } catch (ClassCastException e) {
            System.out.println("object is not an instance of User");
            e.printStackTrace();
        }
        return false;
    }

}
