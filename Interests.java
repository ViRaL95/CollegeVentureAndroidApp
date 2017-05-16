package com.example.varun.finalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;
import java.util.List;

/**
 * The purpose of this class is to find the interests of a specific user and then to store the information in a database
 */
public class Interests extends AppCompatActivity {
    private String username;
    private CheckBox Java;
    private CheckBox Cplus;
    private CheckBox Matlab;
    private CheckBox Python;
    private TextView warning;
    private DynamoDBMapper mapper;
    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private String status;
    private String identitypoolid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Credentials credentials= new Credentials();
        identitypoolid=credentials.getIdentityPoolId();

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                identitypoolid, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        //if the user has registered for the application and their interests have already been filled out
        status="Registered/FoundInterests";
        Intent getIntent=getIntent();
        Bundle getBundle=getIntent.getExtras();
        username=(String) getBundle.get("username");
        ddbClient =  new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
        setContentView(R.layout.activity_interests);
        //retrieve all checkboxes
        Java= (CheckBox) findViewById(R.id.Java);
        Cplus= (CheckBox) findViewById(R.id.Cplus);
        Matlab= (CheckBox) findViewById(R.id.Matlab);
        Python= (CheckBox) findViewById(R.id.Python);

        warning=(TextView) findViewById(R.id.activitywarning);
    }
    //create interests if the create interests button was clicked
    public void createInterests(View view){
        Runnable runnable= new Runnable(){
            public void run (){
                User user= new User();
                user.setUsername(username);
                List <String> interests= new ArrayList<>();
                DynamoDBQueryExpression dynamoDBQueryExpression= new DynamoDBQueryExpression<User>().withHashKeyValues(user);
                List<User> itemList=mapper.query(User.class, dynamoDBQueryExpression);
                boolean checked=false;

                user=itemList.get(0);
                /*
                In order to find out what interests the user is interested in we must see
                which checkboxes are clicked.
                 */
                if(Java.isChecked()){
                    interests.add("Java");
                    checked=true;
                }
                if(Cplus.isChecked()){
                    interests.add("C plus");
                    checked=true;

                }
                if(Matlab.isChecked()){
                    checked=true;
                }
                if(Python.isChecked()){
                    checked=true;
                }
                user.setInterests(interests);
                if(checked==true){
                user.setStatus(status);
                    mapper.save(user);
                    Intent intent= new Intent(Interests.this, StonyBrook.class);
                    intent.putExtra("username",username);
                    startActivity(intent);
                }
                else{
                    warning.setText("You must submit atleast one interest");
                }

                    }
                }
                ;
                Thread mythread= new Thread(runnable);
                mythread.start();

            }


}
