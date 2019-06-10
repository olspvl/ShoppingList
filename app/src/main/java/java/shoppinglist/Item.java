package java.shoppinglist;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.UUID;

@Entity(tableName = "items")
public class Item {
    @PrimaryKey
    @NonNull
    private String mUUID;
    @ColumnInfo(name = "item_title")
    private String mTitle;
    @ColumnInfo(name = "item_bought")
    private boolean mBought;
    @ColumnInfo(name = "item_selected")
    private boolean mSelected;

    public Item() {
        this(UUID.randomUUID());
    }

    public Item(UUID uuid) {
        mUUID = uuid.toString();
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String UUID) {
        mUUID = UUID;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isBought() {
        return mBought;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setBought(boolean bought) {
        mBought = bought;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public String getPhotoFilename() {
        return "IMG_" + getUUID() + ".jpg";
    }
}
