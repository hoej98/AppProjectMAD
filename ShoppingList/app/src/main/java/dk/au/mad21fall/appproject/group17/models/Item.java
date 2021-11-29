package dk.au.mad21fall.appproject.group17.models;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item implements Parcelable {
    private String uid;
    private String name;
    private String pictureUrl;
    private String amount;
    private Boolean isChecked;

    public Item() {
        uid = java.util.UUID.randomUUID().toString();
        isChecked = false;
    }

    public Item(String name, String pictureUrl, String amount) {
        uid = java.util.UUID.randomUUID().toString();
        isChecked = false;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(name);
        parcel.writeString(pictureUrl);
        parcel.writeString(amount);
        parcel.writeBoolean(isChecked);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private Item(Parcel in){
        uid = in.readString();
        name = in.readString();
        pictureUrl = in.readString();
        amount = in.readString();
        isChecked = in.readBoolean();
    }

    public static final Parcelable.Creator<Item> CREATOR
            = new Parcelable.Creator<Item>() {

        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}
