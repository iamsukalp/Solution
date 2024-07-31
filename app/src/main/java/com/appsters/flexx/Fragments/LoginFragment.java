package com.appsters.flexx.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appsters.flexx.Activities.HomeActivity;
import com.appsters.flexx.R;
import com.appsters.flexx.databinding.FragmentLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.appsters.flexx.Base.Flexx.userRef;


public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private String phone;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private String token;
    private String email;

    public LoginFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentLoginBinding.inflate(inflater,container,false);

        init();
        return binding.getRoot();
    }

    private void init() {

        // google OAuth definition
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        mAuth=FirebaseAuth.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
      binding.loginSendOtpBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              if (validate()){
                   PhoneAuthProvider.getInstance().verifyPhoneNumber(
                           phone,
                           20,
                           TimeUnit.SECONDS,
                           requireActivity(),
                           new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

                               @Override
                               public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {

                               }

                               @Override
                               public void onVerificationFailed(@NonNull @NotNull FirebaseException e) {
                                   Toast.makeText(requireActivity(), "Verification Failed", Toast.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onCodeSent(@NonNull @NotNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                   super.onCodeSent(s, forceResendingToken);
                                   LoginFragmentDirections.ActionLoginFragmentToOTPFragment action=LoginFragmentDirections.actionLoginFragmentToOTPFragment();
                                   action.setVerificationId(s);
                                   action.setPhone(phone);
                                   Navigation.findNavController(binding.getRoot()).navigate(action);
                               }
                           }
                   );
                  }
              }

      });

      binding.loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
                googleSignIn();
          }
      });
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("Login", "Google sign in failed", e);
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                        Intent intent=new Intent(requireActivity(), HomeActivity.class);
                                        startActivity(intent);
                                        requireActivity().finish();
                                    }
                                    else{
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        email=user.getEmail();
                                        assert user != null;
                                        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                                if (task.isSuccessful()){
                                                    token= Objects.requireNonNull(task.getResult()).getToken();
                                                    updateUI(mAuth.getCurrentUser().getUid());
                                                }
                                                else{
                                                    Toast.makeText(requireActivity(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                }


                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });



                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Login", "signInWithCredential:failure", task.getException());

                        }
                    }
                });
    }

    private void updateUI(String uid) {
        Map<String,Object> map=new HashMap<>();
        map.put("token",token);
        map.put("email",email);
        userRef.child(uid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_loginFragment_to_userDetailFragment);
                }
                else{
                    Log.d("Login", "onComplete: "+task.getException().getMessage());
                    Toast.makeText(requireActivity(), "Login Failed", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private boolean validate() {
        phone="+91"+binding.loginPhone.getText().toString().trim();
        if (phone.length()!=13 ){
            binding.loginPhone.setError("Enter a valid number");
            return false;

        }
        return true;
    }
}