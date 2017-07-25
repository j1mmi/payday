package practise.postcourse.circledraw;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jim on 08/01/2016.
 */
public class Transaction implements Parcelable {

    private String from_userID;
    private String to_userID;
    private String from_userName;
    private String to_userName;
    private int amount;
    private String currency;
    private String desc;

    //CONSTRUCTORS

    public Transaction(String from_userID, String from_userName, String to_userID, String to_userName, int amount, String currency) {
        this.from_userID = from_userID;
        this.from_userName = from_userName;
        this.to_userID = to_userID;
        this.to_userName = to_userName;
        this.amount = amount;
        this.currency = currency;
    }

    public Transaction(String from_userID, String from_userName, String to_userID, String to_userName, int amount, String currency, String desc) {
        this.from_userID = from_userID;
        this.from_userName = from_userName;
        this.to_userID = to_userID;
        this.to_userName = to_userName;
        this.amount = amount;
        this.currency = currency;
        this.desc = desc;
    }

    public Transaction(Parcel in) {
        this.from_userID = in.readString();
        this.from_userName = in.readString();
        this.to_userID = in.readString();
        this.to_userName = in.readString();
        this.amount = in.readInt();
        this.currency = in.readString();
        this.desc = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(from_userID);
        dest.writeString(from_userName);
        dest.writeString(to_userID);
        dest.writeString(to_userName);
        dest.writeInt(amount);
        dest.writeString(currency);
        dest.writeString(desc);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {

        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    //ACCESSOR METHOD

    public String getFromUserID() {
        return this.from_userID;
    }

    public void setFromUserID(String id) {
        this.from_userID = id;
    }

    public String getFromUserName() {
        return this.from_userName;
    }

    public void setFromUserName(String user) {
        this.from_userName = user;
    }

    public String getToUserID() {
        return this.to_userID;
    }

    public void setToUserID(String id) {
        this.to_userID = id;
    }

    public String getToUserName() {
        return this.to_userName;
    }

    public void setToUserName(String user) {
        this.to_userName = user;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency){
        this.currency = currency;
    }

    public String getDescription() {
        return this.desc;
    }

    public void setDescription(String description) {
        this.desc = description;
    }

    @Override
    public String toString() {
        return "from:" + from_userName + "; to: " + to_userName + "; amount: " + amount + "; curr: " + currency + "; desc: " + desc;
    }

}