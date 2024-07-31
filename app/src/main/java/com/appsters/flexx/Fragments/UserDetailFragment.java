package com.appsters.flexx.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appsters.flexx.Activities.HomeActivity;
import com.appsters.flexx.R;
import com.appsters.flexx.databinding.FragmentUserDetailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.appsters.flexx.Base.Flexx.userRef;


public class UserDetailFragment extends Fragment {

    private FragmentUserDetailBinding binding;
    private FirebaseAuth mAuth;
    private String userId;
    private String name;
    public UserDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentUserDetailBinding.inflate(inflater,container,false);
        init();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.userDetailsSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                    Map<String,Object> map=new HashMap<>();
                    map.put("name",name);

                    userRef.child(userId).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Intent intent=new Intent(requireActivity(), HomeActivity.class);
                                    startActivity(intent);
                                    requireActivity().finish();
                                }
                                else{
                                    Toast.makeText(requireActivity(), "Registration failed", Toast.LENGTH_SHORT).show();
                                }
                        }
                    });
                }
            }
        });
    }

    private boolean validate() {
        name=binding.userDetailsName.getText().toString().trim();
        if (name.length()<3){
            binding.userDetailsName.setError("Please enter a valid name");
            return false;
        }
        return true;
    }

    private void init() {
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
    }
}