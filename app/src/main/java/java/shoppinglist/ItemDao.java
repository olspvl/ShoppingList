package java.shoppinglist;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ItemDao {
    @Query("SELECT * FROM items")
    List<Item> getAll();

    @Query("SELECT * FROM items WHERE item_bought = :isBought")
    List<Item> getBought(boolean isBought);

    @Query("SELECT * FROM items WHERE mUUID = :uuid")
    Item getItem(String uuid);

    @Update
    public void update(Item... items);

    @Insert
    void insert(Item... items);

    @Delete
    void delete(Item item);
}
