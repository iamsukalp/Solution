package com.appsters.flexx.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.appsters.flexx.Model.ArticleModel;
import com.appsters.flexx.R;
import com.appsters.flexx.databinding.ActivityDocumentBinding;
import com.appsters.flexx.databinding.GeneralArticleListItemBinding;
import com.appsters.flexx.databinding.StarredListItemBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import static com.appsters.flexx.Base.Flexx.articleRef;
import static com.appsters.flexx.Base.Flexx.userRef;

public class DocumentActivity extends AppCompatActivity {

    private ActivityDocumentBinding binding;
    private String type;
    private Query query;
    private String userId;
    private FirebaseRecyclerOptions<ArticleModel> options;
    private FirebaseRecyclerAdapter<ArticleModel,DocViewHolder> adapter;
    private LinearLayoutManager linearLayoutManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getVisibilityStatus();
    }

    private void getVisibilityStatus() {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()){
                    binding.docViewSwitcher.setDisplayedChild(1);
                    getDocList();
                }else{
                    binding.docViewSwitcher.setDisplayedChild(0);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getDocList() {
        options=new FirebaseRecyclerOptions.Builder<ArticleModel>().setQuery(query,ArticleModel.class).build();
        adapter=new FirebaseRecyclerAdapter<ArticleModel, DocViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull DocViewHolder holder, int position, @NonNull @NotNull ArticleModel model) {

                String key=adapter.getRef(position).getKey();
                switch ((int) model.getFileType()){



                    case 0:{ // image
                        holder.binding.articleImage.setScaleType(ImageView.ScaleType.CENTER);
                        Picasso.get().load(model.getUrl()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.binding.articleImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(model.getUrl()).into(holder.binding.articleImage);
                            }
                        });
                        break;
                    }
                    case 1: // pdf
                    {
                        holder.binding.articleImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_pdf_file));
                        break;
                    }
                    case 2: //docx
                    {
                        holder.binding.articleImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_docs_file));
                        break;
                    }
                    case 3:{ // link
                        holder.binding.articleImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_url_file));
                        break;
                    }
                }

                holder.binding.articleTitle.setText(model.getTitle());
                holder.binding.articleUser.setText(model.getUser());


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (model.getFileType()!=0){

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        URL myUrl = new URL(model.getUrl());
                                        URLConnection urlConnection = myUrl.openConnection();
                                        urlConnection.connect();
                                        int file_size = urlConnection.getContentLength();
                                        Log.i("FILE SIZE", "file_size = " + file_size);
                                        if (file_size>15000000){
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getUrl()));
                                            startActivity(browserIntent);
                                        }
                                        else{
                                            Intent intent=new Intent(DocumentActivity.this, PDFViewActivity.class);
                                            intent.putExtra("url",model.getUrl());
                                            startActivity(intent);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();


                                    }
                                }
                            }).start();

                        }
                        else{
                            Intent intent=new Intent(DocumentActivity.this, ImageViewActivity.class);
                            intent.putExtra("image",model.getUrl());
                            startActivity(intent);
                        }
                    }
                });

                holder.binding.articleRemoveMarkBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        holder.binding.articleRemoveMarkBtn.setEnabled(false);
                        articleRef.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    userRef.child(userId).child(type).child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                holder.binding.articleRemoveMarkBtn.setEnabled(true);

                                            }
                                            else{
                                                Toast.makeText(DocumentActivity.this, "Couldn't delete the document, please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                else{
                                    Toast.makeText(DocumentActivity.this, "Couldn't delete the document, please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

            }

            @NonNull
            @NotNull
            @Override
            public DocViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new DocViewHolder(StarredListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }


        };
        adapter.startListening();
        binding.docList.setAdapter(adapter);

    }

    private  void init(){



        // getting data
        type=getIntent().getStringExtra("type");
        userId=getIntent().getStringExtra("userId");

        // setup recyclerview
            linearLayoutManager=new LinearLayoutManager(this);
            linearLayoutManager.isAutoMeasureEnabled();

            binding.docList.setHasFixedSize(true);
            binding.docList.setLayoutManager(linearLayoutManager);


        // query
        query=userRef.child(userId).child(type);
    }

    private static class DocViewHolder extends RecyclerView.ViewHolder {
        private final StarredListItemBinding binding;
        public DocViewHolder(StarredListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }
}