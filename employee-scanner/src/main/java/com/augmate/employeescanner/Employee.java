package com.augmate.employeescanner;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by premnirmal on 8/18/14.
 */
public final class Employee implements Parcelable {

    public static final EmployeeCreator CREATOR = new EmployeeCreator();

    private String id;
    private String name;

    public Employee(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Employee(Parcel in) {
        String data[] = new String[2];
        in.readStringArray(data);
        this.id = data[0];
        this.name = data[1];
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.id, this.name
        });
    }

    public static class EmployeeCreator implements Parcelable.Creator<Employee> {
        public Employee createFromParcel(Parcel source) {
            return new Employee(source);
        }
        public Employee[] newArray(int size) {
            return new Employee[size];
        }
    }
}
