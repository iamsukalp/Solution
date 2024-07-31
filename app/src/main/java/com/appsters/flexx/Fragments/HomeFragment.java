package com.appsters.flexx.Fragments;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.appsters.flexx.Activities.ImageViewActivity;
import com.appsters.flexx.Activities.PDFViewActivity;
import com.appsters.flexx.Activities.SearchDocActivity;
import com.appsters.flexx.Dialogs.CustomProgressDialog;
import com.appsters.flexx.Model.ArticleModel;
import com.appsters.flexx.R;
import com.appsters.flexx.databinding.ArticleListItemBinding;
import com.appsters.flexx.databinding.FragmentHomeBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.appsters.flexx.Base.Flexx.articleRef;
import static com.appsters.flexx.Base.Flexx.userRef;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseRecyclerOptions<ArticleModel> options;
    private FirebaseRecyclerAdapter<ArticleModel,ArticleViewHolder> adapter;
    private Query query;
    private LinearLayoutManager linearLayoutManager;
    private String userId;
    private FirebaseAuth mAuth;
    ArrayList<String> isMarked=new ArrayList<>();

  //  private CustomProgressDialog progressDialog;
    private ProgressDialog progressDialog;
    
    private Context mCtx;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentHomeBinding.inflate(inflater,container,false);
        init();

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        mCtx=context;
        super.onAttach(context);


    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getArticleList();


        binding.homeSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentActivity(v);
            }
        });

        binding.homeArticleList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.homeSearchButton.show();

                } else {
                    binding.homeSearchButton.hide();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

    }

    private void getArticleList() {
        options=new FirebaseRecyclerOptions.Builder<ArticleModel>().setQuery(query,ArticleModel.class).build();
        adapter=new FirebaseRecyclerAdapter<ArticleModel, ArticleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull ArticleViewHolder holder, int position, @NonNull @NotNull ArticleModel model) {
                String key=getRef(position).getKey();


                userRef.child(userId).child("Starred").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(key)){
                                    holder.binding.articleStarBtn.setImageDrawable(ContextCompat.getDrawable(mCtx,R.drawable.ic_star_pressed));
                                    isMarked.add(key);
                                }
                                else{
                                    holder.binding.articleStarBtn.setImageDrawable(ContextCompat.getDrawable(mCtx,R.drawable.ic_star));
                                    isMarked.remove(key);
                                }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

                switch ((int) model.getFileType()){

                    case 0:{ // image
                        holder.binding.articleImage.setScaleType(ImageView.ScaleType.CENTER);
                        Picasso.get().load(model.getUrl())
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.ic_image_file)
                                .into(holder.binding.articleImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(model.getUrl()).placeholder(R.drawable.ic_image_file).into(holder.binding.articleImage);
                            }
                        });
                        break;
                    }
                    case 1: // pdf
                    {
                        holder.binding.articleImage.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_pdf_file));
                        break;
                    }
                    case 2: //docx
                    {
                        holder.binding.articleImage.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_docs_file));
                        break;
                    }
                    case 3:{ // link
                        holder.binding.articleImage.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_url_file));
                        holder.binding.articleDownloadBtn.setVisibility(View.GONE);
                        break;
                    }
                }

                holder.binding.articleTitle.setText(model.getTitle());
                holder.binding.articleUser.setText(model.getUser());

                holder.binding.articleStarBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String key=adapter.getRef(position).getKey();
                        Log.d("KEY", "onClick: "+key);
                        String uploadedBy=model.getUserId();
                        Map<String,Object> map=new HashMap<>();
                        map.put("user",model.getUser());
                        map.put("url",model.getUrl());
                        map.put("timestamp",model.getTimestamp());
                        map.put("title",model.getTitle());
                        map.put("fileType",model.getFileType());

                        holder.binding.articleStarBtn.setEnabled(false);

                        if (isMarked.contains(key)){

                            userRef.child(userId).child("Starred").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        holder.binding.articleStarBtn.setEnabled(true);
                                    }
                                });
                        }
                        else{
                            userRef.child(userId).child("Starred").child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    holder.binding.articleStarBtn.setEnabled(true);
                                    if (task.isSuccessful()){

                                        userRef.child(uploadedBy).child("Applaud").child(key).child(userId).child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    userRef.child(uploadedBy).child("Applaud").child("All").child(key).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                holder.binding.articleStarBtn.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_star_pressed));
                                                            }
                                                            else{
                                                                Toast.makeText(requireActivity(), "Operation failed", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                                }
                                                else{
                                                    Toast.makeText(requireActivity(), "Operation failed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else{
                                        Toast.makeText(requireActivity(), "Operation failed", Toast.LENGTH_SHORT).show();
                                    }


                                }
                            });
                        }



                    }
                });

                holder.binding.articleDownloadBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getUrl()));
                        startActivity(intent);
                     /*   Toast.makeText(mCtx, "Clicked", Toast.LENGTH_SHORT).show();
                       downloadFile(model.getUrl(),model.getFileType(),model.getTitle());*/
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                       if (model.getFileType()!=0){

                           progressDialog.setMessage("validating data source for the file.");
                           progressDialog.show();

                           if (model.getFileType()==3){
                               progressDialog.dismiss();
                               openLargeFileOrUrlReference(model.getUrl());
                           }
                           else{
                               new Thread(new Runnable() {
                                   @Override
                                   public void run() {
                                       try {
                                           URL myUrl = new URL(model.getUrl());
                                           URLConnection urlConnection = myUrl.openConnection();
                                           urlConnection.connect();
                                           int file_size = urlConnection.getContentLength();
                                           Log.i("FILE SIZE", "file_size = " + file_size);
                                           progressDialog.dismiss();
                                           if (file_size>15000000){
                                               openLargeFileOrUrlReference(model.getUrl());
                                         /*  Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getUrl()));
                                           startActivity(browserIntent);*/
                                           }
                                           else{

                                               Intent intent=new Intent(requireActivity(), PDFViewActivity.class);
                                               intent.putExtra("url",model.getUrl());
                                               intent.putExtra("largeFile",false);
                                               startActivity(intent);
                                           }
                                       } catch (IOException e) {
                                           e.printStackTrace();


                                       }
                                   }
                               }).start();

                           }

                       }
                       else{
                           Intent intent=new Intent(requireActivity(), ImageViewActivity.class);
                           intent.putExtra("image",model.getUrl());
                           startActivity(intent);
                       }
                    }
                });

            }

            @NonNull
            @NotNull
            @Override
            public ArticleViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new ArticleViewHolder(ArticleListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
        };
        adapter.startListening();
        binding.homeArticleList.setAdapter(adapter);
    }

    private void openLargeFileOrUrlReference(String url) {

        Intent intent=new Intent(requireActivity(), PDFViewActivity.class);
        intent.putExtra("url",url);
        intent.putExtra("largeFile",true);
        startActivity(intent);
    }

    public void downloadFile(String url,long type, String docName) {
        String extension;
        DownloadManager.Request request1 = new DownloadManager.Request(Uri.parse(url));
        request1.setDescription("Solutions");   //appears the same in Notification bar while downloading
        request1.setTitle(docName);
        request1.setVisibleInDownloadsUi(true);

        request1.allowScanningByMediaScanner();
        request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        switch ((int) type){
            case 0:
            {
                extension=".jpeg";
                break;
            }
            case 1:
            {
                extension=".pdf";
                break;
            }
            case 2:
            {
                extension=".docx";
                break;
            }

            default:
                throw new IllegalStateException("Unexpected value: " + (int) type);
        }
        request1.setDestinationInExternalFilesDir(mCtx, "/File", docName+extension);

        DownloadManager manager1 = (DownloadManager) mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
        Objects.requireNonNull(manager1).enqueue(request1);
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
        }
    }
    private void init() {
        // firebase auth
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();

            //firebase  query
        query=articleRef.orderByChild("timestamp").limitToLast(20);

        // recycler view setup
        linearLayoutManager=new LinearLayoutManager(requireActivity());
        linearLayoutManager.isAutoMeasureEnabled();
        binding.homeArticleList.setHasFixedSize(true);
        binding.homeArticleList.setLayoutManager(linearLayoutManager);

        // progress dialog
//        progressDialog=new CustomProgressDialog(requireActivity());
        progressDialog=new ProgressDialog(requireActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Please Wait...");

    }

    private static class ArticleViewHolder extends RecyclerView.ViewHolder {

        private ArticleListItemBinding binding;

        public ArticleViewHolder(ArticleListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }

    private void presentActivity(View v) {
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(requireActivity(), v, "transition");
        int revealX = (int) (v.getX() + v.getWidth() / 2);
        int revealY = (int) (v.getY() + v.getHeight() / 2);

        Intent intent = new Intent(requireActivity(), SearchDocActivity.class);
        intent.putExtra(SearchDocActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(SearchDocActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);


        ActivityCompat.startActivity(requireActivity(), intent, options.toBundle());
    }

}