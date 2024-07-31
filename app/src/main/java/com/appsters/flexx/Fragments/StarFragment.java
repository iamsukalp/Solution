package com.appsters.flexx.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.appsters.flexx.Activities.ImageViewActivity;
import com.appsters.flexx.Activities.PDFViewActivity;
import com.appsters.flexx.Model.ArticleModel;
import com.appsters.flexx.R;
import com.appsters.flexx.databinding.FragmentStarBinding;
import com.appsters.flexx.databinding.GeneralArticleListItemBinding;
import com.appsters.flexx.databinding.StarredListItemBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import static com.appsters.flexx.Base.Flexx.userRef;


public class    StarFragment extends Fragment {

    private FragmentStarBinding binding;
    private FirebaseAuth mAuth;
    private String userId;
    private FirebaseRecyclerOptions<ArticleModel> options;
    private FirebaseRecyclerAdapter<ArticleModel,StarViewHolder> adapter;
    private Query query;
    private LinearLayoutManager linearLayoutManager;
    private Context mCtx;

    public StarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentStarBinding.inflate(inflater,container,false);
        init();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        getStarredArticles();
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        mCtx=context;
    }

    private void getStarredArticles() {
            options=new FirebaseRecyclerOptions.Builder<ArticleModel>().setQuery(query,ArticleModel.class).build();
            adapter=new FirebaseRecyclerAdapter<ArticleModel, StarViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull @NotNull StarViewHolder holder, int position, @NonNull @NotNull ArticleModel model) {
                 String key=adapter.getRef(position).getKey();

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
                            holder.binding.articleImage.setImageDrawable(ContextCompat.getDrawable(mCtx,R.drawable.ic_pdf_file));
                            break;
                        }
                        case 2: //docx
                        {
                            holder.binding.articleImage.setImageDrawable(ContextCompat.getDrawable(mCtx,R.drawable.ic_docs_file));
                            break;
                        }
                        case 3:{ // link
                            holder.binding.articleImage.setImageDrawable(ContextCompat.getDrawable(mCtx,R.drawable.ic_url_file));
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
                                                Intent intent=new Intent(requireActivity(), PDFViewActivity.class);
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
                                Intent intent=new Intent(requireActivity(), ImageViewActivity.class);
                                intent.putExtra("image",model.getUrl());
                                startActivity(intent);
                            }
                        }
                    });

                    holder.binding.articleRemoveMarkBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            userRef.child(userId).child("Starred").child(key).removeValue();
                        }
                    });
                }

                @NonNull
                @NotNull
                @Override
                public StarViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                    return new StarViewHolder(StarredListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
                }
            };
            adapter.startListening();
            binding.starList.setAdapter(adapter);
    }

    private void init() {
        // firebase auth
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();

        // query
        query=userRef.child(userId).child("Starred");

        //setup recycler view
        linearLayoutManager=new LinearLayoutManager(requireActivity());
        linearLayoutManager.isAutoMeasureEnabled();

        binding.starList.setHasFixedSize(true);
        binding.starList.setLayoutManager(linearLayoutManager);

    }

    private class StarViewHolder extends RecyclerView.ViewHolder {
        private StarredListItemBinding binding;
        public StarViewHolder(StarredListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }
}