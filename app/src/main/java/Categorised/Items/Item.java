package Categorised.Items;


import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by Al Jaki on 2018/05/13.
 */

public class Item implements Parcelable {

    private String Title;               //Title of the todo item
    private String Description;         //description of the todo item
    private int Category;               //Category of the todo item
    private Long date;              //Date if the todo item is scheduled
    private boolean Completed;          //Has the user completed the todo item
    private boolean Logged;             //Has the user logged the completed todo item


    /**
     *

     * @param title of the todo item
     * @param description of the todo item
     */
    public Item(String title, String description, int category, Long d, boolean completed, boolean logged) {
        Title = title;
        Description = description;
        Category = category;
        date = d;
        Completed = completed;
        Logged = logged;
    }

    /**
     *
     * @param source of the pasrced item
     */
    public Item(Parcel source) {
        //create the item passed from one activity to another
        Title = source.readString();
        Description = source.readString();
        Category = source.readInt();
        if(Category == 3){
            date = source.readLong();
        }else
        {
            date = null;
        }
        Completed = source.readByte() != 0;
        Logged = source.readByte() != 0;
    }

    /**
     *
     * @return whether the item is completed
     */
    public boolean isCompleted() {
        return Completed;
    }

    /**
     *
     * @return whether the item has been completed or not
     */
    public void changeCompleted(){
        if(Completed == true){
            Completed = false;
        }else
        {
            Completed = true;
        }
    }

    /**
     *
     * @return if the item has been logged or not
     */
    public boolean isLogged() {
        return Logged;
    }

    /**
     *
     * @param logged set  the logged of the item
     */
    public void setLogged(boolean logged) {
        this.Logged = logged;
    }

    /**
     *
     * @return Category of the todo item
     */
    public int getCategory() {
        return Category;
    }

    /**
     *
     * @return date if the item is scheduled
     */
    public Long getDate() {
        return date;
    }

    /**
     *
     * @param date if the item is scheduled
     */
    public void setDate(Long date) {
        this.date = date;
    }

    /**
     *
     * @param category of the todo item
     */
    public void setCategory(int category) {
        Category = category;
    }

    /**
     *
     * @return Title of the todo item
     */
    public String getTitle() {
        return Title;
    }

    /**
     *
     * @param title of the todo item
     */
    public void setTitle(String title) {
        Title = title;
    }

    /**
     *
     * @return the description of the todo item
     */
    public String getDescription() {
        return Description;
    }

    /**
     *
     * @param description of the todo item
     */
    public void setDescription(String description) {
        Description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     *
     * @param destination activity item is passed from
     * @param flags parcel flags
     */
    @Override
    public void writeToParcel(Parcel destination, int flags) {
        //variales that will be parced with intent
        destination.writeString(Title);
        destination.writeString(Description);
        destination.writeInt(Category);
        if(date != null){
            destination.writeLong(date);
        }
        destination.writeByte((byte) (Completed ? 1 : 0));
        destination.writeByte((byte) (Completed ? 1 : 0));

    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }

        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }
    };
}