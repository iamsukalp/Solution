package com.appsters.flexx.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.appsters.flexx.R;
import com.appsters.flexx.databinding.ActivityAboutBinding;
import com.appsters.flexx.databinding.AdminDialogBinding;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;

public class AboutActivity extends AppCompatActivity {
    private ActivityAboutBinding binding;
    public static final int RC_BARCODE_CAPTURE = 9001;
    public static final int REQUEST_CAMERA_PERMISSION=101;
    private ArrayList<String> result= new ArrayList<>();
    private String pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        result.add("696969");
        result.add("138055");
        result.add("622673");
        result.add("545044");
        result.add("266247");
        result.add("112151");

        binding.aboutMeet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AdminDialogBinding binding;
                Dialog dialog=new Dialog(AboutActivity.this);
                binding=AdminDialogBinding.inflate(getLayoutInflater());
                dialog.setContentView(binding.getRoot());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

                binding.adminPinLoginBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (validate(binding)){
                            doSomethingWithTheScanResult(pin);
                        }
                    }
                });

                binding.adminQrBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(AboutActivity.this, new
                                String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);

                    }
                });


                binding.floatingActionButtonClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


                dialog.show();





                return false;
            }
        });
    }

    private boolean validate(AdminDialogBinding binding) {
        pin=binding.adminPin.getText().toString().trim();
        if (pin.length()<6){
            Toast.makeText(this, "Pin can't be smaller than 6 digits", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initiateScan();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }



    public void initiateScan() {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == CommonStatusCodes.SUCCESS && requestCode == RC_BARCODE_CAPTURE) {
            if (data == null) return;
            Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
            final String scanResult = barcode.displayValue;
            doSomethingWithTheScanResult(scanResult);
        }
    }

    private void doSomethingWithTheScanResult(String scanResult) {

        if (result.contains(scanResult)){
            Intent intent=new Intent(this,AdminConsoleActivity.class);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "You shall not pass!", Toast.LENGTH_SHORT).show();
        }


    }

}