package java.shoppinglist;

import android.arch.persistence.room.Room;
import android.content.Context;

import java.io.File;
import java.util.List;

public class ItemDB {
    private static ItemDB sItemDB;
    private Context mContext;
    private ItemDao mItemDao;

    public static ItemDB get(Context context) {
        if (sItemDB == null) {
            sItemDB = new ItemDB(context);
        }
        return sItemDB;
    }

    private ItemDB(Context context) {
        mContext = context.getApplicationContext();
        AppDatabase database = Room.databaseBuilder(mContext, AppDatabase.class,
                "database-name").allowMainThreadQueries().build();
        mItemDao = database.itemDao();
    }

    public void addItem(Item i) {
        mItemDao.insert(i);
    }

    public void updateItem(Item i) {
        mItemDao.update(i);
    }

    public void removeItem(Item i) {
        mItemDao.delete(i);
    }

    public List<Item> getItems() {
        return mItemDao.getAll();
    }

    public List<Item> getItems(boolean isBought) {
        return mItemDao.getBought(isBought);
    }

    public Item getItem(String id) {
        return mItemDao.getItem(id);
    }


    public File getPhotoFile(Item item) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, item.getPhotoFilename());
    }
}
