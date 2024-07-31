package com.appsters.flexx.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.appsters.flexx.R;
import com.appsters.flexx.databinding.ActivityAdminConsoleBinding;

public class AdminConsoleActivity extends AppCompatActivity {
    private ActivityAdminConsoleBinding binding;
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAdminConsoleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init(){
            // bottom bar customization
            binding.adminBottomBar.setItemIconTintList(null);

            navController= Navigation.findNavController(this, R.id.admin_host_fragment);

            NavigationUI.setupWithNavController(binding.adminBottomBar,navController);



    }

}