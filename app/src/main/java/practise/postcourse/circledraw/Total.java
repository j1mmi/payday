package practise.postcourse.circledraw;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jim on 20/01/2016.
 */
public class Total implements Parcelable{

    private String from_user;
    private String from_id;
    private String to_user;
    private String to_id;
    private int amount;
    private String currency;
    private String transactionIDs;

    public Total(String from_id, String from_user, String to_id, String to_user, int amount, String currency, String transactionIDs) {
        this.from_id = from_id;
        this.from_user = from_user;
        this.to_id = to_id;
        this.to_user = to_user;
        this.amount = amount;
        this.currency = currency;
        this.transactionIDs = transactionIDs;
    }

    // Parceable methods

    public Total(Parcel in) {
        this.from_id = in.readString();
        this.from_user = in.readString();
        this.to_id = in.readString();
        this.to_user = in.readString();
        this.amount = in.readInt();
        this.currency = in.readString();
        this.transactionIDs = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(from_id);
        dest.writeString(from_user);
        dest.writeString(to_id);
        dest.writeString(to_user);
        dest.writeInt(amount);
        dest.writeString(currency);
        dest.writeString(transactionIDs);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Total> CREATOR = new Parcelable.Creator<Total>() {

        @Override
        public Total createFromParcel(Parcel in) {
            return new Total(in);
        }

        @Override
        public Total[] newArray(int size) {
            return new Total[size];
        }
    };


    @Override
    public String toString() {
        return "from_id: " + from_id + " from: " +
                from_user + " to_id: " + to_id + " to: " +
                to_user + " amount: " + amount + " currency: " + currency +
                " transactionIDs: " + transactionIDs;
    }


    public String getFromID() {
        return from_id;
    }

    public void setFromID(String from_id) {
        this.from_id = from_id;
    }

    public String getFromUser() {
        return this.from_user;
    }

    public void setFromUser(String user) {
        this.from_user = user;
    }

    public String getToID() {
        return to_id;
    }

    public void setToID(String to_id) {
        this.to_id = to_id;
    }

    public String getToUser() {
        return this.to_user;
    }

    public void setToUser(String user) {
        this.to_user = user;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransactionIDs() {
        return transactionIDs;
    }

    public void setTransactionIDs(String transactionIDs) {
        this.transactionIDs = transactionIDs;
    }


}
