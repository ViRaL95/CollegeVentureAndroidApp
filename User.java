package com.example.varun.finalproject;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Varun on 4/9/17.
 */
@DynamoDBTable(tableName = "Users")
public class User {
private String Username;
private String password;
private String status;
private List <String> Interests;
private HashMap<String, String> applications;
    @DynamoDBHashKey(attributeName = "Username")
    public String getUsername() {
        return Username;
    }
    public void setUsername(String username) {
        this.Username = username;
    }
    @DynamoDBAttribute
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @DynamoDBAttribute(attributeName = "Status")
    public String getStatus(){return status;}

    public void setStatus(String status){
        this.status=status;
    }

    public void setInterests(List<String> Interests){
        this.Interests=Interests;
    }
    @DynamoDBAttribute(attributeName = "Interests")
    public List<String> getInterests(){
        return Interests;
    }

    @DynamoDBAttribute(attributeName = "Applications")
    public HashMap<String, String> getApplications() {
        return applications;
    }

    public void setApplications(HashMap<String, String> applications) {
        this.applications = applications;
    }
}

