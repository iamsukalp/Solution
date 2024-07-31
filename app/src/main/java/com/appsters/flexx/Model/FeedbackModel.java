package com.appsters.flexx.Model;

public class FeedbackModel {

    String user;
    String feedback;

    public FeedbackModel(){}

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public FeedbackModel(String user, String feedback) {
        this.user = user;
        this.feedback = feedback;
    }
}
