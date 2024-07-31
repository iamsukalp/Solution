package com.appsters.flexx.Base;

import android.app.Application;
import android.provider.ContactsContract;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class Flexx extends Application {

    public static DatabaseReference userRef,articleRef,applicationRef,feedBackRef;
    public static StorageReference storageReference;

    @Override
    public void onCreate() {
        super.onCreate();
        // Firebase offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


        //Picasso offline Capability
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        articleRef=FirebaseDatabase.getInstance().getReference().child("Article");
        feedBackRef=FirebaseDatabase.getInstance().getReference().child("Feedback");
        applicationRef=FirebaseDatabase.getInstance().getReference().child("Applications");
        storageReference= FirebaseStorage.getInstance().getReference();

    }
}
