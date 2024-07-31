package com.appsters.flexx.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appsters.flexx.Model.FeedbackModel;
import com.appsters.flexx.R;
import com.appsters.flexx.databinding.FeedbackListItemBinding;
import com.appsters.flexx.databinding.FragmentFeedbackBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;

import org.jetbrains.annotations.NotNull;

import static com.appsters.flexx.Base.Flexx.feedBackRef;


public class FeedbackFragment extends Fragment {

    private FragmentFeedbackBinding binding;
    private FirebaseRecyclerOptions<FeedbackModel> options;
    private FirebaseRecyclerAdapter<FeedbackModel,FeedbackViewHolder> adapter;
    private LinearLayoutManager linearLayoutManager;
    private Query query;


    public FeedbackFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentFeedbackBinding.inflate(inflater,container,false);
        init();
        return binding.getRoot();
    }

    private void init() {
        linearLayoutManager=new LinearLayoutManager(requireActivity());
        linearLayoutManager.isAutoMeasureEnabled();
        binding.feedbackList.setHasFixedSize(true);
        binding.feedbackList.setLayoutManager(linearLayoutManager);

        query=feedBackRef.orderByKey();
    }

    @Override
    public void onStart() {
        super.onStart();
        getFeedBacks();
    }

    private void getFeedBacks() {
        options=new FirebaseRecyclerOptions.Builder<FeedbackModel>().setQuery(query,FeedbackModel.class).build();
        adapter=new FirebaseRecyclerAdapter<FeedbackModel, FeedbackViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull FeedbackViewHolder holder, int position, @NonNull @NotNull FeedbackModel model) {

                holder.binding.feedback.setText(model.getFeedback());
                holder.binding.user.setText(model.getUser());

            }

            @NonNull
            @NotNull
            @Override
            public FeedbackViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new FeedbackViewHolder(FeedbackListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
            }
        };
        adapter.startListening();
        binding.feedbackList.setAdapter(adapter);
    }

    private class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private FeedbackListItemBinding binding;
        public FeedbackViewHolder(FeedbackListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }
}