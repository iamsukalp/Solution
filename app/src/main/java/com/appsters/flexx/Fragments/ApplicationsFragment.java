package com.appsters.flexx.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appsters.flexx.Model.ApplicationModel;
import com.appsters.flexx.R;
import com.appsters.flexx.databinding.ApplicationsListItemBinding;
import com.appsters.flexx.databinding.FragmentApplicationsBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;

import org.jetbrains.annotations.NotNull;

import static com.appsters.flexx.Base.Flexx.applicationRef;


public class ApplicationsFragment extends Fragment {

    private FragmentApplicationsBinding binding;
    private FirebaseRecyclerOptions<ApplicationModel> options;
    private FirebaseRecyclerAdapter<ApplicationModel,ApplicationsViewHolder> adapter;
    private LinearLayoutManager linearLayoutManager;
    private Query query;

    public ApplicationsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentApplicationsBinding.inflate(inflater,container,false);
        init();
        return binding.getRoot();
    }

    private void init() {
        linearLayoutManager=new LinearLayoutManager(requireActivity());
        linearLayoutManager.isAutoMeasureEnabled();
        binding.applicationsList.setHasFixedSize(true);
        binding.applicationsList.setLayoutManager(linearLayoutManager);

        query=applicationRef.orderByKey();
    }

    @Override
    public void onStart() {
        super.onStart();
        getApplicationsList();
    }

    private void getApplicationsList() {
        options=new FirebaseRecyclerOptions.Builder<ApplicationModel>().setQuery(query,ApplicationModel.class).build();
        adapter=new FirebaseRecyclerAdapter<ApplicationModel, ApplicationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull ApplicationsViewHolder holder, int position, @NonNull @NotNull ApplicationModel model) {

                String email,phone;
                email= model.getEmail();
                phone=model.getPhone();

                if (TextUtils.isEmpty(email))
                {
                    email="Email unavailable";
                }
                if(TextUtils.isEmpty(phone)){
                    phone="Phone unavailable";
                }
                holder.binding.applicationEmail.setText(email);
                holder.binding.applicationName.setText(model.getName());
                holder.binding.applicationPhone.setText(phone);

            }

            @NonNull
            @NotNull
            @Override
            public ApplicationsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new ApplicationsViewHolder(ApplicationsListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
            }
        };
        adapter.startListening();
        binding.applicationsList.setAdapter(adapter);
    }

    private class ApplicationsViewHolder extends RecyclerView.ViewHolder {
        private ApplicationsListItemBinding binding;
        public ApplicationsViewHolder(ApplicationsListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }
}