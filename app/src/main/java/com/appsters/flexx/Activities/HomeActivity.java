package com.appsters.flexx.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appsters.flexx.R;
import com.appsters.flexx.databinding.ActivityHomeBinding;
import com.appsters.flexx.databinding.FeedBackDialogBinding;
import com.appsters.flexx.databinding.JoinDialogBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.appsters.flexx.Base.Flexx.applicationRef;
import static com.appsters.flexx.Base.Flexx.feedBackRef;
import static com.appsters.flexx.Base.Flexx.userRef;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private String userImage=null;
    private String userName=null;
    private FirebaseAuth mAuth;
    private String userId;
    private TextView cameraBtn,urlBtn,pdfBtn,galleryBtn,documentBtn;
    private int request_code;
    private final int CAMERA_PERMISSION_CODE=101;
    private Uri fileUri;
    private NavController navController;
    private boolean isHomeVisible=true;
    private boolean isUsersVisible=false;
    private boolean isStarsVisible=false;
    private Button menu_logout;
    private ImageView menu_image;
    private TextView menu_name,menu_about,menu_join,menu_feedback;
    private boolean isApplied=false;
    private String userPhone,userEmail;
    private String feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityHomeBinding.inflate(getLayoutInflater());

        getWindow().setNavigationBarColor(getColor(R.color.background_grey));

        setContentView(binding.getRoot());

        init();

        getUserDetails();



        binding.homeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeActivity.this,ProfileActivity.class);
                intent.putExtra("Image",userImage);
                intent.putExtra("Name",userName);
                intent.putExtra("userId",userId);
                intent.putExtra("Email",userEmail);
                intent.putExtra("Phone",userPhone);
                Pair[] pairs = new Pair[1];
                pairs[0]=new Pair<View,String>(binding.homeProfileImage,"profile_image");
                ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(HomeActivity.this,pairs);
                startActivity(intent,options.toBundle());
            }
        });

        binding.homeAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
                bottomSheetDialog.setContentView(R.layout.bottomsheet_layout);

                bottomSheetDialog.setCanceledOnTouchOutside(false);
                bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

                cameraBtn=bottomSheetDialog.findViewById(R.id.bottomsheet_camera);
                galleryBtn=bottomSheetDialog.findViewById(R.id.bottomsheet_gallery);
                pdfBtn=bottomSheetDialog.findViewById(R.id.bottomsheet_pdf);
                documentBtn=bottomSheetDialog.findViewById(R.id.bottomsheet_doc);
                urlBtn=bottomSheetDialog.findViewById(R.id.bottomsheet_url);



                cameraBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkPermission(Manifest.permission.CAMERA,
                                CAMERA_PERMISSION_CODE);

                    }
                });

                galleryBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openGallery();
                    }
                });

                pdfBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPdfChooser();
                    }
                });
                documentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        openDocumentChooser();
                    }
                });
                urlBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        request_code=4;
                        Intent intent=new Intent(HomeActivity.this,AddArticleActivity.class);
                        intent.putExtra("request_code",request_code);
                        intent.putExtra("file_uri","null");
                        intent.putExtra("userName",userName);
                        startActivity(intent);
                    }
                });



                bottomSheetDialog.show();
            }
        });

        navController=Navigation.findNavController(this,R.id.home_host_fragment);
        binding.homeBottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {


                switch (item.getItemId()){
                    case R.id.homeFragment:
                    {

                        if (isStarsVisible){
                            navController.navigate(R.id.action_starFragment_to_homeFragment);
                            isHomeVisible=true;
                            isUsersVisible=false;
                            isStarsVisible=false;
                            binding.homeBottomAppBar.getMenu().findItem(R.id.usersFragment).setIcon(R.drawable.ic_users_unselected);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.starFragment).setIcon(R.drawable.ic_star);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.homeFragment).setIcon(R.drawable.ic_home_selected);
                        }
                        if (isUsersVisible){
                            navController.navigate(R.id.action_usersFragment_to_homeFragment);
                            isHomeVisible=true;
                            isUsersVisible=false;
                            isStarsVisible=false;
                            binding.homeBottomAppBar.getMenu().findItem(R.id.usersFragment).setIcon(R.drawable.ic_users_unselected);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.starFragment).setIcon(R.drawable.ic_star);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.homeFragment).setIcon(R.drawable.ic_home_selected);
                        }
                        break;
                    }
                    case R.id.starFragment:
                    {
                        if (isHomeVisible){
                            navController.navigate(R.id.action_homeFragment_to_starFragment);
                            isHomeVisible=false;
                            isStarsVisible=true;
                            isUsersVisible=false;
                            binding.homeBottomAppBar.getMenu().findItem(R.id.usersFragment).setIcon(R.drawable.ic_users_unselected);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.starFragment).setIcon(R.drawable.ic_star_pressed);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.homeFragment).setIcon(R.drawable.ic_home_unselected);
                        }
                        if (isUsersVisible){
                            navController.navigate(R.id.action_usersFragment_to_starFragment);
                            isHomeVisible=false;
                            isStarsVisible=true;
                            isUsersVisible=false;
                            binding.homeBottomAppBar.getMenu().findItem(R.id.usersFragment).setIcon(R.drawable.ic_users_unselected);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.starFragment).setIcon(R.drawable.ic_star_pressed);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.homeFragment).setIcon(R.drawable.ic_home_unselected);
                        }

                        break;
                    }
                    case R.id.usersFragment:
                    {

                       /* Intent intent=new Intent(HomeActivity.this,UsersActivity.class);
                        startActivity(intent);*/
                        if (isStarsVisible){
                            navController.navigate(R.id.action_starFragment_to_usersFragment);
                            isHomeVisible=false;
                            isUsersVisible=true;
                            isStarsVisible=false;
                            binding.homeBottomAppBar.getMenu().findItem(R.id.usersFragment).setIcon(R.drawable.ic_users_selected);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.starFragment).setIcon(R.drawable.ic_star);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.homeFragment).setIcon(R.drawable.ic_home_unselected);
                        }
                        if(isHomeVisible){
                            navController.navigate(R.id.action_homeFragment_to_usersFragment);
                            isHomeVisible=false;
                            isUsersVisible=true;
                            isStarsVisible=false;
                            binding.homeBottomAppBar.getMenu().findItem(R.id.usersFragment).setIcon(R.drawable.ic_users_selected);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.starFragment).setIcon(R.drawable.ic_star);
                            binding.homeBottomAppBar.getMenu().findItem(R.id.homeFragment).setIcon(R.drawable.ic_home_unselected);
                        }

                        break;

                    }
                }



                return false;
            }
        });

        binding.homeBottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
                bottomSheetDialog.setContentView(R.layout.menu_layout);

                bottomSheetDialog.setCanceledOnTouchOutside(false);
                bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

                menu_about=bottomSheetDialog.findViewById(R.id.menu_about);
                menu_name=bottomSheetDialog.findViewById(R.id.menu_name);
                menu_image=bottomSheetDialog.findViewById(R.id.menu_image);
                menu_logout=bottomSheetDialog.findViewById(R.id.menu_logout);
                menu_join=bottomSheetDialog.findViewById(R.id.menu_join_us);
                menu_feedback=bottomSheetDialog.findViewById(R.id.menu_feedback);



                Picasso.get().load(userImage)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(menu_image, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(userImage)
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                        .into(menu_image);
                            }
                        });

                menu_name.setText(userName);
                menu_about.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                        Intent intent=new Intent(HomeActivity.this,AboutActivity.class);
                        startActivity(intent);
                    }
                });

                menu_logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAuth.signOut();
                        Intent intent =new Intent(HomeActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

                menu_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                        Intent intent=new Intent(HomeActivity.this,ProfileActivity.class);
                        intent.putExtra("Image",userImage);
                        intent.putExtra("Name",userName);
                        intent.putExtra("userId",userId);
                        intent.putExtra("Email",userEmail);
                        intent.putExtra("Phone",userPhone);
                        Pair[] pairs = new Pair[1];
                        pairs[0]=new Pair<View,String>(binding.homeProfileImage,"profile_image");
                        ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(HomeActivity.this,pairs);
                        startActivity(intent,options.toBundle());
                    }
                });

                menu_join.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                                bottomSheetDialog.dismiss();
                                showConfirmationDialog();

                    }
                });

                menu_feedback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                        showFeedbackDialog();
                    }
                });






                bottomSheetDialog.show();
            }
        });
    }

    private void showConfirmationDialog() {
        JoinDialogBinding binding;
        Dialog dialog=new Dialog(this);
        dialog.setCanceledOnTouchOutside(true);
        binding=JoinDialogBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        if (isApplied){
            binding.joinViewSwitcher.setDisplayedChild(1);
        }
        else{
            binding.joinViewSwitcher.setDisplayedChild(0);
        }

        binding.joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,String> map=new HashMap<>();
                map.put("name",userName);
                map.put("email",userEmail);
                map.put("phone",userPhone);

                applicationRef.child(userId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()){
                                userRef.child(userId).child("applied").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            isApplied=true;
                                            binding.joinViewSwitcher.setDisplayedChild(1);
                                        }
                                        else{
                                            Toast.makeText(HomeActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }   else{
                                Toast.makeText(HomeActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    }
                });


            }
        });


        binding.floatingActionButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();




    }

    private void showFeedbackDialog() {
        Dialog dialog=new Dialog(this);
        FeedBackDialogBinding binding;
        binding=FeedBackDialogBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());
        dialog.setCanceledOnTouchOutside(false);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        binding.feedbackSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate(binding)){

                    Map<String,String> map=new HashMap<>();
                    map.put("user",userName);
                    map.put("feedback",feedback);
                    feedBackRef.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(HomeActivity.this, "Feedback sent!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                                else{

                                    Toast.makeText(HomeActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        }
                    });
                }
            }
        });

        binding.floatingActionButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();




    }

    private boolean validate(FeedBackDialogBinding binding) {
        feedback=binding.feedbackComment.getText().toString().trim();
        if (TextUtils.isEmpty(feedback)){
            binding.feedbackComment.setError("Cant be left empty");
            return false;
        }
        return true;
    }


    private void openDocumentChooser() {
        request_code=3;
        Intent intentPDF = new Intent(Intent.ACTION_GET_CONTENT);
        intentPDF.setType("application/docx");
        intentPDF.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intentPDF, request_code);
    }

    private void openPdfChooser() {
        request_code=2;
        Intent intentPDF = new Intent(Intent.ACTION_GET_CONTENT);
        intentPDF.setType("application/pdf");
        intentPDF.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intentPDF, request_code);
    }

    public void checkPermission(String permission, int requestCode) // checking for camera permission
    {
        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this,
                    new String[] { permission },
                    requestCode);
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
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.appsters.flexx.provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(pictureIntent,
                        request_code);
            }
        }

    }
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileUri=Uri.fromFile(image);

        return image;
    }

    private void openGallery() {
        request_code=1;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a picture"), request_code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK) {
            {
                if (requestCode==0){
                    openAddArticle(0,fileUri);
                }
                else {
                    fileUri=data.getData();
                    openAddArticle(request_code,fileUri);
                }



            }
        }
    }


    private void openAddArticle(int request_code, Uri fileUri) {
        Intent intent=new Intent(HomeActivity.this,AddArticleActivity.class);
        intent.putExtra("request_code",request_code);
        intent.putExtra("file_uri",fileUri.toString());
        intent.putExtra("userName",userName);
        startActivity(intent);
    }

    private void getUserDetails() {
        userRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.hasChild("image")){
                        userImage=snapshot.child("image").getValue().toString();
                    }
                    if (snapshot.hasChild("name")){
                        userName=snapshot.child("name").getValue().toString();
                    }
                    if (snapshot.hasChild("applied")){
                        isApplied= (boolean) snapshot.child("applied").getValue();
                    }
                    if (snapshot.hasChild("email")){
                        userEmail=snapshot.child("email").getValue().toString();
                    }else{
                        userEmail=null;
                    }
                    if (snapshot.hasChild("phone")){
                        userPhone=snapshot.child("phone").getValue().toString();
                    }else{
                        userPhone=null;
                    }


                    Picasso.get().load(userImage)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(binding.homeProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(userImage)
                                            .placeholder(R.drawable.ic_profile_placeholder)
                                            .into(binding.homeProfileImage);
                                }
                            });


                    getCurrentTime();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void init()  {
        // firebase auth
        mAuth=FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null){
            userId=mAuth.getCurrentUser().getUid();
        }else{
            Intent intent=new Intent(HomeActivity.this,MainActivity.class);
            startActivity(intent);
        }


        // bottom app bar setup
        binding.homeBottomAppBar.replaceMenu(R.menu.bottom_app_bar_menu);
        binding.homeBottomAppBar.getMenu().findItem(R.id.homeFragment).setIcon(R.drawable.ic_home_selected);



    }

    @SuppressLint("SetTextI18n")
    private void getCurrentTime()  {
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String getCurrentTime = sdf.format(c.getTime());
        String salutation;

        if (getCurrentTime .compareTo("12:00") < 0)
        {
            Log.d("Return","getTestTime less than getCurrentTime "+"|"+"GoodMorning");
            salutation= "Good Morning,";
        }
        else if(getCurrentTime.compareTo("15:00")<0)
        {
            Log.d("Return","getTestTime older than getCurrentTime "+"|"+"GoodAfternoon");
            salutation="Good Afternoon,";

        }
        else if (getCurrentTime .compareTo("04:00") < 0){
            salutation="Good Night,";
        }
        else{
            Log.d("Return","getTestTime older than getCurrentTime "+"|"+"GoodEvening");
            salutation= "Good Evening,";
        }

        binding.homeSalutation.setText(salutation+" "+userName+" !");


    }


}