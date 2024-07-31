package com.appsters.flexx.Adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.appsters.flexx.Activities.ProfileActivity;


import com.appsters.flexx.Model.UsersModel;
import com.appsters.flexx.R;
import com.appsters.flexx.databinding.UserListItemBinding;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import android.widget.Filter;

import static com.appsters.flexx.Fragments.UsersFragment.viewSwitcher;


public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private List<String> userKey;
    private List<UsersModel> userList;
    private List<UsersModel> userAll;
    private Activity activity;

    public UsersAdapter(List<UsersModel> userList, List<String> userKey, List<UsersModel> userAll, FragmentActivity activity) {
        this.activity=activity;
        this.userKey=userKey;
        this.userAll=userAll;
        this.userList=userList;
    }

    @NonNull
    @NotNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new UsersViewHolder(UserListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UsersViewHolder holder, int position) {

        UsersModel model=userList.get(position);
        String name="";
        String image="";

        name=model.getName();
        image=model.getImage();

        holder.binding.usersName.setText(model.getName());

        if (image.isEmpty()){
            holder.binding.usersImage.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.ic_profile_placeholder));
        }else{
            Picasso.get().load(model.getImage())
                    .fit().centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(holder.binding.usersImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(model.getImage())
                                    .fit().centerCrop()
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .into(holder.binding.usersImage);
                        }
                    });


        }




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(activity, ProfileActivity.class);
                intent.putExtra("Image",model.getImage());
                intent.putExtra("Name",model.getName());
                intent.putExtra("userId",userKey.get(position));
                intent.putExtra("Email",model.getEmail());
                intent.putExtra("Phone",model.getPhone());
                Pair[] pairs = new Pair[1];
                pairs[0]=new Pair<View,String>(holder.binding.usersImage,"profile_image");
                ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(activity,pairs);
                activity.startActivity(intent,options.toBundle());
            }
        });


    }

    @Override
    public int getItemCount() {
        return  userList.size();
    }


    public Filter getFilter() {
        return myFilter;
    }

    Filter myFilter=new Filter() {

        // runs on background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<UsersModel> filteredList =new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(userAll);

            }
            else{



                for(UsersModel model:userList){
                    String filterPattern=constraint.toString().toLowerCase();

                    if (model.getName().toLowerCase().contains(filterPattern)){
                        Log.d("Filter", "performFiltering: "+model.getName());
                        filteredList.add(model);

                    }
                }
            }

            FilterResults results=new FilterResults();
            results.values=filteredList;

            return results;
        }

        // runs on ui thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            userList.clear();
            userList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class UsersViewHolder extends RecyclerView.ViewHolder {
        private UserListItemBinding binding;

        public UsersViewHolder(UserListItemBinding b) {
            super(b.getRoot());
            binding=b;
        }
    }
}
