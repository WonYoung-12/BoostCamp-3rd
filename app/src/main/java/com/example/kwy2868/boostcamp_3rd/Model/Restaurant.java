package com.example.kwy2868.boostcamp_3rd.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kwy2868 on 2017-07-17.
 */

public class Restaurant implements Parcelable{
    private String name;
    private String address;
    private String number;
    private String reply;

    public static Parcelable.Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel parcel) {
            return new Restaurant(parcel);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    public Restaurant(String name, String address, String number, String reply){
        this.name = name;
        this.address = address;
        this.number = number;
        this.reply = reply;
    }

    public Restaurant(Parcel parcel){
        name = parcel.readString();
        address = parcel.readString();
        number = parcel.readString();
        reply = parcel.readString();
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", number='" + number + '\'' +
                ", reply='" + reply + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeString(number);
        parcel.writeString(reply);
    }
}
