package com.appsters.flexx.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.appsters.flexx.Model.ArticleModel;
import com.appsters.flexx.R;
import com.appsters.flexx.databinding.ActivitySearchDocBinding;
import com.appsters.flexx.databinding.GeneralArticleListItemBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import static com.appsters.flexx.Base.Flexx.articleRef;

public class SearchDocActivity extends AppCompatActivity {

    private ActivitySearchDocBinding binding;
    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    View rootLayout;

    private int revealX;
    private int revealY;

    private FirebaseRecyclerOptions<ArticleModel> options;
    private FirebaseRecyclerAdapter<ArticleModel,SearchViewHolder> adapter;
    private LinearLayoutManager linearLayoutManager;
    private String title;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySearchDocBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        revealAnimation(savedInstanceState);
        init();
        binding.searchDocSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((validate())){
                    makeSearch(title);
                }
            }
        });
    }



    private void makeSearch(String title) {
        query=articleRef.orderByChild("title").startAt(title.toUpperCase()
        ).endAt(title.toLowerCase()+"\uf8ff");

        options=new FirebaseRecyclerOptions.Builder<ArticleModel>().setQuery(query,ArticleModel.class).build();
        adapter=new FirebaseRecyclerAdapter<ArticleModel, SearchViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull SearchViewHolder holder, int position, @NonNull @NotNull ArticleModel model) {

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
                        holder.binding.articleImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_pdf_file,null));
                        break;
                    }
                    case 2: //docx
                    {
                        holder.binding.articleImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_docs_file,null));
                        break;
                    }
                    case 3:{ // link
                        holder.binding.articleImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_url_file,null));
                        break;
                    }
                }

                holder.binding.articleTitle.setText(model.getTitle());
                holder.binding.articleUser.setText(model.getUser());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (model.getFileType()!=0){
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getUrl()));
                            startActivity(browserIntent);
                        }
                        else{
                            Intent intent=new Intent(SearchDocActivity.this, ImageViewActivity.class);
                            intent.putExtra("image",model.getUrl());
                            startActivity(intent);
                        }
                    }
                });

            }

            @NonNull
            @NotNull
            @Override
            public SearchViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new SearchViewHolder(GeneralArticleListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
            }

        };
        adapter.startListening();
        binding.searchDocViewSwitcher.setDisplayedChild(1);
        binding.searchDocResult.setAdapter(adapter);
    }

    private boolean validate() {
        title=binding.searchDocSearchBar.getText().toString().trim();
        if (TextUtils.isEmpty(title)){
            binding.searchDocSearchBar.setError("Invalid input");
            return false;
        }
        return true;
    }


    private void init(){

        // auto focus editText
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.searchDocSearchBar.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        },500);

        // recyclerview
        linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.isAutoMeasureEnabled();
        binding.searchDocResult.setHasFixedSize(true);
        binding.searchDocResult.setLayoutManager(linearLayoutManager);



    }

    private void revealAnimation(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(getColor(R.color.background_grey));
        getWindow().setNavigationBarColor(getColor(R.color.background_grey));

        final Intent intent = getIntent();

        rootLayout=binding.searchDocLayout;
        if (savedInstanceState == null && intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) && intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);


            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }
    }

    private void revealActivity(int revealX, int revealY) {
        float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, revealX, revealY, 0, finalRadius);
        circularReveal.setDuration(400);
        circularReveal.setInterpolator(new AccelerateInterpolator());

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();

        circularReveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                /*activitySearchBinding.searchLayout.setBackgroundColor(getColor(R.color.black));*/
            }
        });

    }

    @Override
    public void onBackPressed() {


        getWindow().setStatusBarColor(getColor(R.color.black));
        getWindow().setNavigationBarColor(getColor(R.color.black));

        int cx = rootLayout.getWidth();
        int cy = 0;
        float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, revealX, revealY, finalRadius, 0);

        circularReveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                rootLayout.setVisibility(View.INVISIBLE);
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        circularReveal.setDuration(400);
        circularReveal.start();
    }

    private class SearchViewHolder extends RecyclerView.ViewHolder {
        private GeneralArticleListItemBinding binding;

        public SearchViewHolder(GeneralArticleListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }
}