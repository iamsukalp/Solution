package com.appsters.flexx.Model;

import java.util.List;

public class UsersModel {
    String image;
    String name;
    String userId;
    String phone;
    String email;

    public UsersModel(String image, String name, String userId, String phone, String email) {
        this.image = image;
        this.name = name;
        this.userId = userId;
        this.phone = phone;
        this.email = email;
    }

    public UsersModel(){}

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
