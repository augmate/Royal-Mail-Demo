package com.augmate.employeescanner;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by premnirmal on 8/19/14.
 */
public final class Bin implements Parcelable {

    public static final BinCreator CREATOR = new BinCreator();

    private String id;
    private int numberOfItems;

    public Bin(String id, int numberOfItems) {
        this.id = id;
        this.numberOfItems = numberOfItems;
    }

    public Bin(Parcel in) {
        this.id = in.readString();
        this.numberOfItems = in.readInt();
    }

    public String getId() {
        return id;
    }

    public void setNumberOfItems(int numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    @Override
    public int describeContents() {
        return numberOfItems;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(numberOfItems);
    }

    public static class BinCreator implements Parcelable.Creator<Bin> {
        public Bin createFromParcel(Parcel source) {
            return new Bin(source);
        }

        public Bin[] newArray(int size) {
            return new Bin[size];
        }
    }
}
