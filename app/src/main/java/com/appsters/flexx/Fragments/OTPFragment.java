package com.appsters.flexx.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appsters.flexx.R;
import com.appsters.flexx.databinding.FragmentOTPBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.appsters.flexx.Base.Flexx.userRef;


public class OTPFragment extends Fragment {

    private FragmentOTPBinding binding;
    private String phone,verificationId,code;
    private String userId,token;
    private FirebaseAuth mAuth;
    public OTPFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentOTPBinding.inflate(inflater,container,false);
        init();
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.otpLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                    if (verificationId!=null){
                        PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(
                                verificationId,
                                code
                        );
                        mAuth.signInWithCredential(phoneAuthCredential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                            if (task.isSuccessful()){
                                                userId=task.getResult().getUser().getUid();
                                                mAuth.getAccessToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull @NotNull Task<GetTokenResult> task) {
                                                                 if (task.isSuccessful()){
                                                                     token=task.getResult().getToken();
                                                                     Map<String, Object> map= new HashMap<>();
                                                                     map.put("phone",phone);
                                                                     map.put("token",token);
                                                                     userRef.child(userId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                         @Override
                                                                         public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_OTPFragment_to_userDetailFragment);
                                                                                }
                                                                                else {
                                                                                    Log.d("Login",task.getException().getMessage());
                                                                                    Toast.makeText(requireActivity(), "Login Failed", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                         }
                                                                     });
                                                                 }
                                                                 else {
                                                                     Log.d("Login",task.getException().getMessage());
                                                                     Toast.makeText(requireActivity(), "Login Failed", Toast.LENGTH_SHORT).show();
                                                                 }
                                                    }
                                                });
                                            }
                                            else{
                                                Log.d("Login",task.getException().getMessage());
                                                Toast.makeText(requireActivity(), "Login Failed", Toast.LENGTH_SHORT).show();
                                            }

                                    }
                                });
                    }
                }

            }
        });
    }

    private boolean validate() {
        code=binding.otpPin.getText().toString();
        if (code.length()<6){
            Toast.makeText(requireActivity(), "Wrong OTP", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void init() {
            // getting data from safe arguments
            phone=OTPFragmentArgs.fromBundle(getArguments()).getPhone();
            verificationId=OTPFragmentArgs.fromBundle(getArguments()).getVerificationId();

            // firebase auth
        mAuth=FirebaseAuth.getInstance();



    }
}