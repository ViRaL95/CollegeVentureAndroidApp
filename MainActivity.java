/**
 * The Main Activity class allows a user to loginto their username and password.
 *
 */

package com.example.varun.finalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;

import java.util.List;

public class MainActivity extends AppCompatActivity {

private EditText login_name;
private EditText login_password;
private TextView login_warning;
private DynamoDBMapper mapper;
private AmazonDynamoDBClient ddbClient;
private CognitoCachingCredentialsProvider credentialsProvider;
private String login_name_text;
private String password_text;
private String [] status;
private String identitypoolid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Credentials credentials= new Credentials();
        identitypoolid=credentials.getIdentityPoolId();

        login_password=(EditText) findViewById(R.id.login_password);
        login_name=(EditText) findViewById(R.id.login_name);
        login_warning=(TextView) findViewById(R.id.login_warning);

        // Initialize the Amazon Cognito credentials provider

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                identitypoolid, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        ddbClient =  new AmazonDynamoDBClient(credentialsProvider);
         mapper = new DynamoDBMapper(ddbClient);

        status= new String[3];
    }
    public void login(View view){
        login_name_text=login_name.getText().toString().toLowerCase();
         password_text=login_password.getText().toString();
        if(!login_name_text.matches("") && !password_text.matches("")){
            Runnable runnable = new Runnable() {
                public void run() {

                    User user = new User();
                    user.setUsername(login_name_text);
                    DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>().withHashKeyValues(user);
                    List<User> itemList = mapper.query(User.class, queryExpression);
                    if(itemList.size()>0){
                        String password=itemList.get(0).getPassword();
                         status=itemList.get(0).getStatus().split("/");
                        if(password.equals(password_text)){
                            runOnUiThread(new Runnable(){
                                public void run(){
                                    Intent intent;
                                    //if the status of the user is FindInterests then fill out interests form
                                    if(status[1].equals("Interested")){
                                         intent= new Intent(MainActivity.this, Interests.class);
                                         intent.putExtra("username",login_name_text);
                                    }
                                    //if the status of the user is Interested then just go to the projects page
                                    else{
                                        intent= new Intent(MainActivity.this, StonyBrook.class);
                                        intent.putExtra("username", login_name_text);
                                    }
                                    finish();
                                    startActivity(intent);
                                }
                            });
                        }
                        else{
                            runOnUiThread(new Runnable(){
                                public void run(){
                                    login_warning.setText("Incorrect Password/correct username");
                                }
                            });
                        }

                    }
                    else if (itemList.size()==0){
                        Log.d("entered0","listitem=0");
                        runOnUiThread(new Runnable(){
                            public void run(){
                                login_warning.setText("No such user/password combination");
                            }
                        });
                    }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();

        }
        else{
            login_warning.setText("Username or Passsword empty");

        }

    }
    public void signUp(View view){
        Intent intent= new Intent(this,SignUp.class);
        finish();
        startActivity(intent);

    }

}
