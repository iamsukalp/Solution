package com.appsters.flexx.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.appsters.flexx.R;
import com.appsters.flexx.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import static com.appsters.flexx.Base.Flexx.storageReference;
import static com.appsters.flexx.Base.Flexx.userRef;
import static java.util.Locale.getDefault;

public class ProfileActivity extends AppCompatActivity {

    private static final int MAX_IMAGE_DIMENSION = 120;
    private ActivityProfileBinding binding;
    private String userImage,userName;
    private String userId;
    private String type;
    private String current_user;
    private FirebaseAuth mAuth;
    private static final int CAMERA_PERMISSION_CODE=151;
    private int request_code;
    private File image;
    private Uri imageUri;
    private String email,phone,generatedFilepath,name;
    private StorageReference filepath;
    private ProgressDialog progressDialog;
    private String userEmail,userPhone;
    private Boolean isPicSelected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        binding.profileImageTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="Image";
                openDocTile(type);
            }
        });

        binding.profileDocTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="Docx";
                openDocTile(type);
            }
        });
        binding.profilePdfTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="Pdf";
                openDocTile(type);
            }
        });
        binding.profileUrlTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type="Link";
                openDocTile(type);
            }
        });


        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageInputDialog();
            }
        });

        binding.profileImageEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageInputDialog();
            }
        });

        binding.profileSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                    if (isPicSelected){
                        uploadUserData();
                    }else{
                        uploadDataToUserProfile();
                    }

                }

            }
        });


    }


    private boolean validate() {
        email=binding.profileEmail.getText().toString();
        phone=binding.profilePhone.getText().toString();
        name=binding.profileName.getText().toString();

        if (TextUtils.isEmpty(name) || name.length()<3){
            binding.profileName.setError("Please end a valid name");
            return false;
        }
        if (TextUtils.isEmpty(email)){
            binding.profileEmail.setError("Please enter a valid email");
            return false;
        }
        if (TextUtils.isEmpty(phone) || phone.length()<10){
            binding.profilePhone.setError("Please enter a valid number");
            return false;
        }
        if (phone.length()>10 && phone.length()<13)
        {
            phone="+91"+phone;
        }
        return true;
    }

    private void openImageInputDialog() {
        // option menu for editing image
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_option_menu);
        dialog.setTitle("Pick image from");
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));


        ImageView imageCamera = (ImageView) dialog.findViewById(R.id.imageViewCamera);
        ImageView imageGallery = (ImageView) dialog.findViewById(R.id.imageViewGallery);
        FloatingActionButton closeBtn=dialog.findViewById(R.id.floatingActionButtonClose);

        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission(Manifest.permission.CAMERA,
                        CAMERA_PERMISSION_CODE);
                dialog.dismiss();
            }
        });

        imageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
                dialog.dismiss();
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }
    private void checkPermission(String camera, int cameraPermissionCode) {
        if (ContextCompat.checkSelfPermission(this, camera)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this,
                    new String[] { camera },
                    cameraPermissionCode);
        }
        else {
            openCamera();
        }
    }

    private void openCamera() {
        request_code = 0;

        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.appsters.flexx.provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(pictureIntent,
                        request_code);
            }
        }
    }

    private File createImageFile() {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = null;
        try {
            image = File.createTempFile(
                    imageFileName,   //prefix
                    ".jpg",          //suffix
                    storageDir       //directory
            );
        } catch (IOException e) {
            e.printStackTrace();
        }


        imageUri=Uri.fromFile(image);

        return image;
    }

    private void openGallery() {
        request_code=1;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a picture"), request_code);

    }


    private void openDocTile(String type) {
        Intent intent=new Intent(ProfileActivity.this,DocumentActivity.class);
        intent.putExtra("type",type);
        intent.putExtra("userId",userId);
        startActivity(intent);
    }

    private void init(){


        progressDialog=new ProgressDialog(this);


        // getting data from intent
        userImage=getIntent().getStringExtra("Image");
        userName=getIntent().getStringExtra("Name");
        userId=getIntent().getStringExtra("userId");
        userEmail=getIntent().getStringExtra("Email");
        userPhone=getIntent().getStringExtra("Phone");

        if (!TextUtils.isEmpty(userEmail)){
            binding.profileEmail.setText(userEmail);
        }
        if (!TextUtils.isEmpty(userPhone)){
            binding.profilePhone.setText(userPhone);
        }

        Picasso.get().load(userImage)
                .placeholder(R.drawable.ic_profile_placeholder)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(binding.profileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(userImage)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(binding.profileImage);
                    }
                });

        binding.profileName.setText(userName);


        //firebase auth
        mAuth=FirebaseAuth.getInstance();
        current_user=mAuth.getCurrentUser().getUid();


        if (!TextUtils.equals(userId,current_user)){
            binding.profileImageEditBtn.setVisibility(View.GONE);
            binding.profileImage.setEnabled(false);
            binding.profileImageBg.setVisibility(View.GONE);
            binding.profileName.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this,R.drawable.ic_user),
                    null,
                    null,
                    null);
            binding.profileEmail.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this,R.drawable.ic_email),
                    null,
                    null,
                    null);
            binding.profilePhone.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this,R.drawable.ic_smartphone),
                    null,
                    null,
                    null);
            binding.profileSaveBtn.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        getProfileData();
    }

    private void getProfileData() {
        userRef.child(userId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                binding.profileApplaudCount.setText("Got applauded "+snapshot.child("Applaud").child("All").getChildrenCount()+" times by peers");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){

            isPicSelected=true;
            switch (request_code){
                case 0:     // get image from camera
                    if (image!=null) {
                       Picasso.get().load(imageUri).into(binding.profileImage);
                    }
                    break;
                case 1:
                    imageUri = Objects.requireNonNull(data).getData();
                    if (imageUri != null) {
                        Picasso.get().load(imageUri).into(binding.profileImage);
                    }
                    break;
            }
        }

    }

    public void uploadUserData(){
        progressDialog.setTitle("Uploading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait while the upload completes");
        progressDialog.show();
        if (imageUri!=null){
            filepath = storageReference.child("Profile").child(Calendar.getInstance().getTime() + ".jpeg");
            try {

                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                byte[] data = baos.toByteArray();

                /*  InputStream stream = new FileInputStream(new File(cameraImageFilePath));*/
                UploadTask uploadTask = filepath.putBytes(data);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            getDownloadUrl(filepath);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private void getDownloadUrl(StorageReference filepath) {
        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                generatedFilepath = uri.toString();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                uploadDataToUserProfile();


            }
        });


    }

    private void uploadDataToUserProfile() {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("image",generatedFilepath);
        userMap.put("phone",phone);
        userMap.put("email",email);
        userMap.put("name",name);

        userRef.child(userId).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });

    }



}