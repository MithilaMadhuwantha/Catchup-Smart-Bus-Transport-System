package com.MMW.Catchup.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class User implements Serializable{


    @Exclude private String userId;
    @Exclude private String email;
    @Exclude private String coordinates;     // 'Latitude , Longitude'
    @Exclude private String updateTime;
    @Exclude private String userType;
    @Exclude private String userName;


    public User() {
        // Constructor with no Parameters
    }

    public User(String userId,String userType) {
        this.userId=userId;
        this.userType = userType;

    }

    public User(String userId, String coordinates, String updateTime,String userName) {
        this.userId = userId;
        this.coordinates = coordinates;
        this.updateTime = updateTime;
        this.userName = userName;
    }

    public User(String userId, String coordinates, String updateTime) {
        this.userId = userId;
        this.coordinates = coordinates;
        this.updateTime = updateTime;
    }

    public User(String userId, String coordinates, String updateTime,String userType,String email) {
        this.userId = userId;
        this.coordinates = coordinates;
        this.updateTime = updateTime;
        this.userType=userType;
        this.email=email;
    }

    public String getUserId() {
        return userId;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getUserType() {
        return userType;
    }

    public String getEmail() {
        return email;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


}