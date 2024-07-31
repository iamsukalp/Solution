package com.appsters.flexx.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.appsters.flexx.databinding.ActivityPdfviewBinding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PDFViewActivity extends AppCompatActivity {

    private ActivityPdfviewBinding binding;
    private boolean largeFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPdfviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            init();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() throws UnsupportedEncodingException {
        binding.pdfWebView.getSettings().setJavaScriptEnabled(true);
        binding.pdfWebView.getSettings().setBuiltInZoomControls(true);
        binding.pdfWebView.setWebChromeClient(new WebChromeClient());


        Intent intent = getIntent();
        final String url = intent.getStringExtra("url");
        largeFile=intent.getBooleanExtra("largeFile",false);

        Log.d("Large File", "init: "+largeFile);
        Log.d("URL", "init: "+url);

        binding.pdfWebView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
               if (largeFile){
                   binding.pdfViewSwitcher.setDisplayedChild(1);
               }
               else{
                   binding.pdfWebView.loadUrl("javascript:(function() { " +
                           "document.querySelector('[role=\"toolbar\"]').remove();})()");
                   binding.pdfViewSwitcher.setDisplayedChild(1);
               }
            }
        });
        if (largeFile){
            binding.pdfWebView.loadUrl(url);
        }else{
            binding.pdfWebView.loadUrl("https://docs.google.com/gview?embedded=true&url="+ URLEncoder.encode(url, "ISO-8859-1"));
        }

    }
}