package com.example.varun.finalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.HashMap;
import java.util.List;

/**
 * The acceptance letter allows the administrator to create an acceptance letter and send it to a user
 if they have been accepted to a project.
 */
public class AcceptanceLetter extends AppCompatActivity {
private String letter;
private EditText retrieveletter;
private DynamoDBMapper mapper;
private AmazonDynamoDBClient ddbClient;
private CognitoCachingCredentialsProvider credentialsProvider;
private String applicant;
private TextView acceptanceletterwarning;
private String projectname;
private String identitypoolid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptance_letter);
        Credentials credentials= new Credentials();
        identitypoolid=credentials.getIdentityPoolId();
        Bundle bundle=getIntent().getExtras();
        applicant=(String) bundle.get("username");
        projectname=(String) bundle.get("projectname");
        retrieveletter= (EditText) findViewById(R.id.acceptanceletter);
        acceptanceletterwarning=(TextView) findViewById(R.id.acceptanceletterwarning);
        //credentials provider used to connect to Amazon Web Service
        credentialsProvider = new CognitoCachingCredentialsProvider(getApplicationContext(),
                identitypoolid, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        ddbClient =  new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
    }
    public void sendLetter(View view){
        letter=retrieveletter.getText().toString();
        if(!letter.matches("")){
            acceptanceletterwarning.setText("");
            Runnable runnable = new Runnable(){
                public void run(){
                    User user= new User();
                    user.setUsername(applicant);
                    //in order to query the database we create a DynamoDBQueryExpression along with a user with its username which is passed
                    //in as a Hash Key Value
                    DynamoDBQueryExpression <User> dynamoDBQueryExpression= new DynamoDBQueryExpression<User>().withHashKeyValues(user);
                    //A list of users is returned after the database is queried
                    List<User> userlist= mapper.query(User.class,dynamoDBQueryExpression);
                    user=userlist.get(0);
                    HashMap<String, String> applications;
                    applications=user.getApplications();
                    String currentstatus=applications.get(projectname);

                    currentstatus=currentstatus+"/"+letter;
                    applications.put(projectname,currentstatus);
                    user.setApplications(applications);
                    //database is updated
                    mapper.save(user);

                    Intent intent= new Intent(AcceptanceLetter.this, EditPersonalProject.class);
                    intent.putExtra("projectname",projectname);
                    startActivity(intent);
                }
            };
            Thread mythread= new Thread(runnable);
            mythread.start();


        }
        else{
            acceptanceletterwarning.setText("Your acceptance letter must contain words");
        }
    }

}
