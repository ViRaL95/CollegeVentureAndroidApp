package com.example.varun.finalproject;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Varun on 4/10/17.
 */
@DynamoDBTable(tableName = "SBUProjects")

public class Project {
private String ProjectName;
private List<String> TeamMates;
private String Administrator;
private String PhoneNumber;
private String ProjectLocation;
private String projectDescription;
private HashMap<String,String> applicants;
    @DynamoDBHashKey(attributeName = "ProjectName")
    public String getProjectName() {
        return ProjectName;
    }

    public void setProjectName(String ProjectName) {
        this.ProjectName = ProjectName;
    }
    @DynamoDBAttribute(attributeName="TeamMates")
    public List<String> getTeammates() {
        return TeamMates;
    }

    public void setTeammates(List <String> TeamMates) {
        this.TeamMates= TeamMates;
    }
    @DynamoDBAttribute(attributeName = "Administrator")
    public String getAdministrator() {
        return Administrator;
    }

    public void setAdministrator(String administrator) {
        this.Administrator = administrator;
    }

    @DynamoDBAttribute(attributeName = "PhoneNumber")
    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }
    @DynamoDBAttribute(attributeName = "ProjectLocation")
    public String getProjectLocation() {
        return ProjectLocation;
    }

    public void setProjectLocation(String projectLocation) {
        ProjectLocation = projectLocation;
    }
    @DynamoDBAttribute(attributeName = "Applicants")
    public HashMap<String,String> getApplicants() {
        return applicants;
    }

    public void setApplicants(HashMap<String,String> applicants) {
        this.applicants = applicants;
    }
    @DynamoDBAttribute(attributeName="Description")
    public String getProjectDescription(){
        return projectDescription;
    }
    public void setProjectDescription(String projectDescription){
        this.projectDescription=projectDescription;
    }
}
