package com.example.varun.finalproject;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;
import java.util.List;

/**
 * The purpose of this application is to view the interests a certain user has.
 */

public class ViewInterests extends AppCompatActivity {
private String username;
private AmazonDynamoDBClient ddbClient;
private CognitoCachingCredentialsProvider credentialsProvider;
private DynamoDBMapper mapper;
private List <String> interests;
private String projectname;
private TableLayout intereststable;
private String identitypoolid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_interests);
        Credentials credentials= new Credentials();
        identitypoolid=credentials.getIdentityPoolId();
       Bundle bundle= getIntent().getExtras();
        username=  bundle.getString("username");
        projectname= bundle.getString("projectname");
         intereststable= (TableLayout) findViewById(R.id.intereststable);
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                identitypoolid, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        ddbClient =  new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        Runnable runnable= new Runnable(){
            public void run(){
                User user= new User();
                user.setUsername(username);
                DynamoDBQueryExpression<User> dynamoDBQueryExpression= new DynamoDBQueryExpression<User>().withHashKeyValues(user);
                List<User> userlist= mapper.query(User.class,dynamoDBQueryExpression);
                user=userlist.get(0);
                interests=user.getInterests();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                int interestnumber=1;
                TableRow tableRowheader= new TableRow(getApplicationContext());
                tableRowheader.setBackgroundColor(Color.parseColor("#000000"));

                TextView textView1= new TextView(getApplicationContext());
                textView1.setTextColor(Color.parseColor("#FFFFFF"));
                textView1.setText(username+"'s interests");

                tableRowheader.addView(textView1);
                //for each loop for every interest in interests one can will dynamically generate the table
                for (String interest: interests){
                    TableRow tableRow= new TableRow(getApplicationContext());
                    tableRow.setBackgroundColor(Color.parseColor("#000000"));
                    TextView textView2= new TextView(getApplicationContext());
                    textView2.setTextColor(Color.parseColor("#FFFFFF"));
                    textView2.setText("Interest Number "+interestnumber);

                    tableRow.addView(textView2);

                    TextView textView3= new TextView(getApplicationContext());
                    textView3.setTextColor(Color.parseColor("#FFFFFF"));
                    textView3.setText(interest);
                    tableRow.addView(textView3);
                    intereststable.addView(tableRow);
                    interestnumber++;

                }
                TableRow tableRowButton= new TableRow(getApplicationContext());
        // the following code allows ou go to back to the previous screen
                Button button= new Button(getApplicationContext());
                button.setText("View Applicants");
                tableRowButton.addView(button);
                intereststable.addView(tableRowButton);
                button.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view){
                        Intent intent= new Intent(ViewInterests.this,ViewApplicants.class);
                        intent.putExtra("project",projectname);
                        startActivity(intent);
                    }
                });

                    }
                });


            }
        };
        Thread thread= new Thread(runnable);
        thread.start();


    }

}
