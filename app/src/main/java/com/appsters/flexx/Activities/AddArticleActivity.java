package com.appsters.flexx.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.appsters.flexx.R;
import com.appsters.flexx.databinding.ActivityAddArticleBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.appsters.flexx.Base.Flexx.articleRef;
import static com.appsters.flexx.Base.Flexx.storageReference;
import static com.appsters.flexx.Base.Flexx.userRef;

public class AddArticleActivity extends AppCompatActivity {

    private ActivityAddArticleBinding binding;
    private String userId;
    private FirebaseAuth mAuth;
    private int request_code;
    private Uri fileUri;
    private String desc,title,subject;
    private String link=null;
    private StorageReference filepath;
    private ProgressDialog progressDialog;
    private String mediaDownloadUrl;
    private int fileType=0;
    private String type;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAddArticleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();


        binding.addArticleSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (validate()){

                        if (request_code==4){
                            progressDialog.show();
                            uploadArticleData();
                        }else{
                            progressDialog.show();
                            uploadData();
                        }

                    }
            }
        });
    }

    private void uploadData() {
        filepath = storageReference.child("Articles").child(Calendar.getInstance().getTime() + ".jpeg");
        filepath.putFile(fileUri).continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filepath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    // After uploading is done it progress
                    // dialog box will be dismissed

                    Uri uri = task.getResult();

                    mediaDownloadUrl = uri.toString();

                    uploadArticleData();


                } else {
                    progressDialog.dismiss();
                    Toast.makeText(AddArticleActivity.this, "UploadedFailed", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(AddArticleActivity.this,HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void uploadArticleData() {
        Map<String,Object> map=new HashMap<>();
        map.put("title",title);
        map.put("fileType",fileType);
        map.put("subject",subject);
        map.put("description",desc);
        map.put("userId",userId);
        map.put("user",userName);
        map.put("timestamp", ServerValue.TIMESTAMP);
        map.put("url",mediaDownloadUrl);

        String key=articleRef.push().getKey();

        articleRef.child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    userRef.child(userId).child(type).child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@androidx.annotation.NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressDialog.dismiss();
                                Toast.makeText(AddArticleActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(AddArticleActivity.this,HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                progressDialog.dismiss();
                                Toast.makeText(AddArticleActivity.this, "UploadedFailed", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(AddArticleActivity.this,HomeActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        }
                    });
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(AddArticleActivity.this, "UploadedFailed", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(AddArticleActivity.this,HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private boolean validate() {
        desc=binding.addArticleDesc.getText().toString().trim();
        title=binding.addArticleName.getText().toString().trim();
        subject=binding.addArticleSubject.getText().toString().trim();
        link=binding.addArticleUrl.getText().toString().trim();
        if (desc.isEmpty()){
            binding.addArticleDesc.setError("Invalid Description");
            return false;
        }
        if (title.isEmpty()){
            binding.addArticleName.setError("Invalid Title");
            return false;
        }
        if (subject.isEmpty()){
            binding.addArticleSubject.setError("Invalid Subject");
            return false;
        }
        if (request_code==4){
        if (link.isEmpty()){
                binding.addArticleUrl.setError("Invalid Url");
                return false;
            }
            else{
                mediaDownloadUrl=link;
                return true;

            }

        }
        return true;
    }

    private void init(){
        // firebase auth
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();

        // getting data
        request_code=getIntent().getIntExtra("request_code",0);
        fileUri= Uri.parse(getIntent().getStringExtra("file_uri"));
        userName=getIntent().getStringExtra("userName");

        showFileType(request_code);

        // progress dialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Uploading your note, it might take a minute...");
    }

    private void showFileType(int request_code) {
        Drawable drawable;
        switch (request_code){
            case 0:
            case 1: {
                binding.addArticleImage.setScaleType(ImageView.ScaleType.CENTER);
                Picasso.get().load(fileUri).into(binding.addArticleImage);
                fileType=0;
                type="Image";
                break;
            }
            case 2:
            {
                drawable=getDrawable(R.drawable.ic_pdf_file);
                binding.addArticleImage.setImageDrawable(drawable);
                fileType=1;
                type="Pdf";
                break;
            }
            case 3:
            {
                drawable=getDrawable(R.drawable.ic_docs_file);
                binding.addArticleImage.setImageDrawable(drawable);
                fileType=2;
                type="Docx";
                break;
            }
            case 4:
            {
                drawable=getDrawable(R.drawable.ic_url_file);
                binding.addArticleUrl.setVisibility(View.VISIBLE);
                binding.addArticleImage.setImageDrawable(drawable);
                fileType=3;
                type="Link";
                break;
            }


        }

    }

}