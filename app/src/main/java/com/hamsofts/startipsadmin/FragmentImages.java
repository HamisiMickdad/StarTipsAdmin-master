package com.hamsofts.startipsadmin;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentImages extends Fragment implements View.OnClickListener{
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private ImageView imageView;
    private EditText txtImageName;
    private Uri imgUri;
Button btnImg;
    public static final String FB_STORAGE_PATH = "image/";
    public static final String FB_DATABASE_PATH = "image";
    public static final int REQUEST_CODE = 1234;



    public FragmentImages() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View v = inflater.inflate(R.layout.fragment_images_main, container, false);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(FB_DATABASE_PATH);

        imageView = v.findViewById(R.id.imageView);
        txtImageName = v.findViewById(R.id.txtImageName);
        btnImg = v.findViewById(R.id.btnBrowse);
        btnImg = v.findViewById(R.id.btnUpload);
        btnImg = v.findViewById(R.id.btnShow);
        btnImg.setOnClickListener(this);

       return v;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBrowse:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"),REQUEST_CODE);

            case R.id.btnUpload:
                if (imgUri != null) {
                    final ProgressDialog dialog = new ProgressDialog(getActivity().getApplicationContext());
                    dialog.setTitle("Uploading Image");
                    dialog.show();

                    StorageReference ref = mStorageRef.child(FB_STORAGE_PATH+System.currentTimeMillis() +"."+getImageUri(imgUri));

                    ref.putFile(imgUri).addOnSuccessListener(taskSnapshot -> {

                        dialog.dismiss();

                        Toast.makeText(getActivity().getApplicationContext(),"Image Uploaded",Toast.LENGTH_SHORT).show();

                        //ImageUpload imageUpload = new ImageUpload(txtImageName.getText().toString(),taskSnapshot.getDownloadUrl().toString());
                        String uploadId = mDatabaseRef.push().getKey();
                        //mDatabaseRef.child(uploadId).setValue(imageUpload);
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {


                                    dialog.dismiss();

                                    Toast.makeText(getActivity().getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                    dialog.setMessage("Uploaded " + (int)progress+"%");
                                }
                            });
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),"Please Select Image",Toast.LENGTH_SHORT).show();
                }
            case R.id.btnShow:
                Intent i = new Intent(getActivity().getApplicationContext(), ImageListActivity.class);
                startActivity(i);


                break;
        }

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null)
            ;
        imgUri = data.getData();

        try {
            Bitmap bm = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), imgUri);
            imageView.setImageBitmap(bm);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getImageUri(Uri uri){
        ContentResolver contentResolver = getActivity().getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

}
