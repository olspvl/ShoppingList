package java.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

public class ItemListFragment extends Fragment {
    private static final String LIST_TYPE = "list_type";
    private RecyclerView mItemRecyclerView;
    private ItemAdapter mAdapter;
    private FloatingActionButton mFloatingActionButton;

    private ItemDB mItemDB;

    private int mListType;

    public static ItemListFragment newInstance(int listType) {
        Bundle args = new Bundle();
        args.putInt(LIST_TYPE, listType);
        ItemListFragment fragment = new ItemListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        assert getArguments() != null;
        switch (getArguments().getInt(LIST_TYPE)) {
            case 1:
                mListType = 1;
                break;
            case 2:
                mListType = 2;
                break;
            default:
                mListType = 1;
        }
        mItemDB = ItemDB.get(getActivity().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_item_list, container, false);

        mItemRecyclerView = v.findViewById(R.id.item_recycler_view);
        mItemRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mFloatingActionButton = v.findViewById(R.id.fab);

        for(Item i : getCurrentItems()) {
            if(i.isSelected()) {
                i.setSelected(false);
                mItemDB.updateItem(i);
            }
        }

        updateUI();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = new Item();
                mItemDB.addItem(item);
                Intent intent = ItemPagerActivity
                        .newIntent(getActivity(), item.getUUID());
                startActivity(intent);
            }
        });

        mItemRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private boolean hide = false;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (hide) {
                        mFloatingActionButton.hide();
                    } else {
                        mFloatingActionButton.show();
                    }
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && mFloatingActionButton.isShown()) {
                    mFloatingActionButton.hide();
                }
                hide = dy > 0;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_item_list, menu);

        MenuItem moveMenuItem = menu.findItem(R.id.move_item);
        if (mListType == 2) {
            moveMenuItem.setTitle(R.string.move_item_to_list);
            moveMenuItem.setIcon(R.drawable.ic_menu_move_to_shoppinglist);
        }
        updateUI();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        List<Item> items = getCurrentItems();
        switch (item.getItemId()) {
            case R.id.move_item:
                for (Item i : items) {
                    if (i.isSelected() && mListType == 1) {
                        i.setBought(true);
                    } else if (i.isSelected() && mListType == 2) {
                        i.setBought(false);
                    }
                    i.setSelected(false);
                    mItemDB.updateItem(i);
                }
                updateUI();
                return true;
            case R.id.select_all:
                boolean isAllSelected = true;
                for (Item i : items) {
                    if (!i.isSelected()) {
                        isAllSelected = false;
                        break;
                    }
                }
                for (Item i : items) {
                    i.setSelected(!isAllSelected);
                    mItemDB.updateItem(i);
                }
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private List<Item> getCurrentItems() {
        return mItemDB.getItems(mListType == 2);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }


    private void updateUI() {
        mAdapter = new ItemAdapter(getCurrentItems());
        mItemRecyclerView.setAdapter(mAdapter);
    }


    private class ItemHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private Item mItem;
        private TextView mTitleTextView;
        private CheckBox mCheckBox;

        public ItemHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_single_item, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = itemView.findViewById(R.id.item_title);
            mCheckBox = itemView.findViewById(R.id.list_check_box);
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mItem.setSelected(isChecked);
                    mItemDB.updateItem(mItem);
                }
            });
        }

        public void bind(Item item) {
            mItem = item;
            mTitleTextView.setText(mItem.getTitle());
            mCheckBox.setChecked(mItem.isSelected());
        }


        @Override
        public void onClick(View v) {
            Intent intent = ItemPagerActivity.newIntent(getActivity(), mItem.getUUID());
            startActivity(intent);
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {
        private List<Item> mItems;

        ItemAdapter(List<Item> items) {
            mItems = items;
        }

        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ItemHolder(layoutInflater, viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder itemHolder, int i) {
            Item item = mItems.get(i);
            itemHolder.bind(item);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

}
