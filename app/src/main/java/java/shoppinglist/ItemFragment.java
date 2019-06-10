package java.shoppinglist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ItemFragment extends Fragment {
    private static final String ARG_ITEM_ID = "item_id";
    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;

    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 51;
    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 52;

    private Item mItem;
    private File mPhotoFile;
    private EditText mItemTitle;
    private CheckBox mBoughtCheckbox;
    private ImageButton mPhotoCameraButton;
    private ImageButton mPhotoGalleryButton;
    private ImageView mImageView;

    private ItemDB mItemDB;

    public static ItemFragment newInstance(String itemId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM_ID, itemId);

        ItemFragment fragment = new ItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        String itemId = (String) getArguments().getSerializable(ARG_ITEM_ID);
        mItemDB = ItemDB.get(getActivity().getApplicationContext());
        mItem = mItemDB.getItem(itemId);
        mPhotoFile = mItemDB.getPhotoFile(mItem);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_item, container, false);

        mItemTitle = v.findViewById(R.id.item_title);
        mItemTitle.setText(mItem.getTitle());

        mBoughtCheckbox = v.findViewById(R.id.item_bought);
        mBoughtCheckbox.setChecked(mItem.isBought());

        mImageView = v.findViewById(R.id.item_photo);
        mPhotoCameraButton = v.findViewById(R.id.item_camera);
        mPhotoGalleryButton = v.findViewById(R.id.item_gallery);

        updatePhotoView();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePhotoView();
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mItemTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mItem.setTitle(s.toString());
                mItemDB.updateItem(mItem);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mBoughtCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mItem.setBought(isChecked);
                mItemDB.updateItem(mItem);
            }
        });

        mPhotoCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
                } else {
                    cameraRequest();
                }
            }
        });

        mPhotoGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
                } else {
                    galleryRequest();
                }
            }
        });
    }

    private void galleryRequest() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    private void cameraRequest() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPhotoFile = mItemDB.getPhotoFile(mItem);
        Uri uri = FileProvider.getUriForFile(getActivity(),
                "java.shoppinglist.fileprovider", mPhotoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            updatePhotoView();
        }

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try (FileOutputStream out = new FileOutputStream(mPhotoFile)) {
                bitmap = MediaStore.Images.Media
                        .getBitmap(getActivity().getApplicationContext().getContentResolver(), selectedImage);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            updatePhotoView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    cameraRequest();
                } else {
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    galleryRequest();
                } else {
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void updatePhotoView() {
        if(mPhotoFile == null || !mPhotoFile.exists()) {
            mImageView.setImageDrawable(null);
            mImageView.setContentDescription(
                    getString(R.string.item_photo_no_image_description));
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mImageView.setImageBitmap(bitmap);
            mImageView.setContentDescription(
                    getString(R.string.item_photo_image_description));
        }
    }
}
