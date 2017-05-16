package com.example.varun.finalproject;

import android.app.DownloadManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;

import java.util.HashMap;
import java.util.List;

/**
 * The Sign Up class allows a user to register for the application. The password the user has entered is retrieved
 * and form validation is performed on the password to ensure that all criterias are met. (Atleast 6 characters, Uppercase letter, one number)
 *
 */

public class SignUp extends AppCompatActivity {

    private EditText signup_name;
    private EditText signup_password;
    private TextView signup_warning;
    private String username;
    private String password;
    private DynamoDBMapper mapper;
    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private String identitypoolid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Credentials credentials= new Credentials();
        identitypoolid=credentials.getIdentityPoolId();
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                identitypoolid, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        signup_name = (EditText) findViewById(R.id.signup_name);
        signup_password = (EditText) findViewById(R.id.signup_password);
        signup_warning= (TextView) findViewById(R.id.signup_warning);
    }

    public void mainScreen(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }
//This method is simply called when the button is clicked
    public void createUser(View view) {

        username = signup_name.getText().toString().toLowerCase();
        password = signup_password.getText().toString();
        User user = new User();
        user.setUsername(username);
        if(!username.matches("") && !password.matches("")) {
            Runnable runnable = new Runnable() {
                public void run() {
                    User user = new User();
                    user.setUsername(username);
                    DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>().withHashKeyValues(user);
                    List<User> itemList = mapper.query(User.class, queryExpression);
                    if (itemList.size() == 0) {
                        //if password doesnt contain an upper case letter or there are no numbers in password then a warning is generated
                        if (password.equals(password.toLowerCase())) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    signup_warning.setText("Password must contain an upper case letter");
                                }
                            });
                        } else if (!password.matches(".*\\d.*")) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    signup_warning.setText("Password must contain a digit ");
                                }
                            });
                        } else if ((password.length() < 6)) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    signup_warning.setText("Password must contain atleast 6 characters");
                                }
                            });
                        } else {
                            HashMap <String, String> temp= new HashMap<String,String>();
                            user.setPassword(password);
                            user.setStatus("Registered/Interested");
                            user.setApplications(temp);
                            mapper.save(user);
                            Intent intent = new Intent(SignUp.this, Interests.class);
                            intent.putExtra("username", username);
                            finish();

                            startActivity(intent);
                        }
                    } else if (itemList.size() > 0) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                signup_warning.setText("Username already exists");
                            }

                        });
                    }
                }


            };
            Thread mythread = new Thread(runnable);
            mythread.start();
        }
        //if the username or password was empty
        else{
            signup_warning.setText("Username and Password empty");
        }
    }
}

