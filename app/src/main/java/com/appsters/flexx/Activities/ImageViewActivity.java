package com.appsters.flexx.Activities;


import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;

import com.appsters.flexx.R;
import com.appsters.flexx.databinding.ActivityImageViewBinding;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ImageViewActivity extends AppCompatActivity {
        private ActivityImageViewBinding binding;
        private String image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityImageViewBinding.inflate(getLayoutInflater());
        getWindow().setNavigationBarColor(getColor(R.color.transparent));
        getWindow().setStatusBarColor(getColor(R.color.transparent));
        setContentView(binding.getRoot());

        init();

    }

    private void init(){
        image=getIntent().getStringExtra("image");
        Log.d("IMAGEVIEW", "init: "+image);

        Picasso.get().load(image)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(binding.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                            binding.imageViewViewSwitcher.setDisplayedChild(1);
                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image)
                                .into(binding.imageView);
                        binding.imageViewViewSwitcher.setDisplayedChild(1);
                    }
                });




    }
}