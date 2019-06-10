package java.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;


public class ItemPagerActivity extends AppCompatActivity {
    private static final String EXTRA_ITEM_ID = "shoppinglist.crime_id";

    private ViewPager mViewPager;
    private ItemDB mItemDB;
    private boolean isBought;
    private List<Item> mItemList;

    public static Intent newIntent(Context packageContext, String itemId) {
        Intent intent = new Intent(packageContext, ItemPagerActivity.class);
        intent.putExtra(EXTRA_ITEM_ID, itemId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_pager);

        String itemId = (String) getIntent().getSerializableExtra(EXTRA_ITEM_ID);

        mViewPager = findViewById(R.id.item_view_pager);

        mItemDB = ItemDB.get(this);
        isBought = mItemDB.getItem(itemId).isBought();
        mItemList = mItemDB.getItems(isBought);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int i) {
                Item item = mItemList.get(i);
                return ItemFragment.newInstance(item.getUUID());
            }

            @Override
            public int getCount() {
                return mItemList.size();
            }
        });

        for(int i = 0; i < mItemList.size(); i++) {
            if(mItemList.get(i).getUUID().equals(itemId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
