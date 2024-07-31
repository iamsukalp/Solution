package com.appsters.flexx.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appsters.flexx.Activities.HomeActivity;
import com.appsters.flexx.R;
import com.appsters.flexx.databinding.FragmentFlashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import static com.appsters.flexx.Base.Flexx.userRef;

public class FlashFragment extends Fragment {

    private FragmentFlashBinding binding;
    private static SpannableString flashText;
    private FirebaseAuth mAuth;

    public FlashFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentFlashBinding.inflate(inflater,container,false);
        init();
        return binding.getRoot();
    }

    private void init() {

        flashText= SpannableString.valueOf(requireActivity().getResources().getString(R.string.heart));
        flashText.setSpan(new ForegroundColorSpan(Color.RED),1,flashText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.flashText.setText("Made with "+flashText+" at NMIMS");

        // firebase auth
        mAuth=FirebaseAuth.getInstance();

    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    if (mAuth.getCurrentUser()!=null){ // check if user is logged in

                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    // user is logged in and registered
                                    Intent intent=new Intent(requireActivity(), HomeActivity.class);
                                    startActivity(intent);
                                    requireActivity().finish();
                                }
                                else{
                                    // user is logged in but not registered
                                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_flashFragment_to_userDetailFragment);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                    }
                    else{  // user is not logged in
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_flashFragment_to_loginFragment);
                    }
            }
        },2100);



    }
}