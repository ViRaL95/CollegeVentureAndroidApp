package com.example.varun.finalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.HashMap;
import java.util.List;

/**
 * This class is used in order to view the acceptance letter taht the administrator of the project has created and
 * sent to the user.
 */
public class ViewAcceptanceLetter extends AppCompatActivity {
private TextView acceptanceLetter;
    private DynamoDBMapper mapper;
    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private String projectname;
    private String username;
    private String letter;
    private String identitypoolid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_acceptance_letter);
        Credentials credentials= new Credentials();
        identitypoolid=credentials.getIdentityPoolId();
        credentialsProvider = new CognitoCachingCredentialsProvider(getApplicationContext(),
                identitypoolid, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        ddbClient =  new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
         username=(String)bundle.get("username");
         projectname=(String)bundle.get("projectname");
        acceptanceLetter=(TextView) findViewById(R.id.viewacceptanceletter);
        Runnable runnable= new Runnable(){
           public void run(){


               User user= new User();
            user.setUsername(username);
             DynamoDBQueryExpression<User> queryexpression= new DynamoDBQueryExpression<User>().withHashKeyValues(user);
            List<User> userList= mapper.query(User.class,queryexpression);

            User getuser;
            getuser=userList.get(0);
            HashMap<String, String> hashMap;
            hashMap=getuser.getApplications();
            //In order to retrieve the letter we must split the status by its backslash and retrieve its last element
            String status=hashMap.get(projectname);
            String letterarray[]=status.split("/");
            letter=letterarray[2];
               runOnUiThread(new Runnable() {
                   public void run() {
                         acceptanceLetter.setText(letter);

                   }
               });
           }
        };
        Thread thread= new Thread(runnable);
        thread.start();

    }
    public void goBack(View view){
        Intent intent= new Intent(ViewAcceptanceLetter.this, StonyBrook.class );
        startActivity(intent);
    }

}
