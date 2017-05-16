package com.example.varun.finalproject;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ViewApplicants extends AppCompatActivity {
private String projectname;
private TableLayout table;
private DynamoDBMapper mapper;
private Project project;
private HashMap<String, String> applicants;
    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private String identitypoolid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_applicants);
        Credentials credentials= new Credentials();
        identitypoolid=credentials.getIdentityPoolId();

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                identitypoolid, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        ddbClient =  new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);


        table=(TableLayout) findViewById(R.id.viewapplicants);
        Intent getIntent= getIntent();
        Bundle bundle=getIntent.getExtras();
            projectname = (String) bundle.get("project");


        Runnable runnable= new Runnable(){
            public void run(){
                 project= new Project();
                project.setProjectName(projectname);
                DynamoDBQueryExpression<Project> dynamoDBQueryExpression= new DynamoDBQueryExpression<Project>().withHashKeyValues(project);
                final List<Project> projectlist= mapper.query(Project.class, dynamoDBQueryExpression);
                project=projectlist.get(0);
                applicants=project.getApplicants();
                runOnUiThread(new Runnable(){
                    public void run(){
                        TableRow tablerow= new TableRow(ViewApplicants.this);

                        TextView textView= new TextView(ViewApplicants.this);
                        textView.setText("Applicant");
                        textView.setWidth(200);
                        textView.setTextColor(Color.parseColor("#FFFFFF"));
                        tablerow.addView(textView);


                        TextView textview1= new TextView(ViewApplicants.this);
                        textview1.setWidth(200);
                        textview1.setTextColor(Color.parseColor("#FFFFFF"));
                        textview1.setText("Status");

                        tablerow.addView(textview1);

                        TextView textview2= new TextView(ViewApplicants.this);
                        textview2.setWidth(200);
                        textview2.setText("Interests");
                        textview2.setTextColor(Color.parseColor("#FFFFFF"));
                        tablerow.addView(textview2);

                        TextView textview= new TextView(ViewApplicants.this);
                        textview.setWidth(200);
                        textview.setText("Accept");
                        textview.setTextColor(Color.parseColor("#FFFFFF"));

                        tablerow.addView(textview);

                        TextView textViewa= new TextView(ViewApplicants.this);
                        textViewa.setWidth(200);
                        textViewa.setText("Decline");
                        textViewa.setTextColor(Color.parseColor("#FFFFFF"));

                        tablerow.addView(textViewa);

                        tablerow.setBackgroundColor(Color.parseColor("#000000"));
                        table.addView(tablerow);
                        for(final String applicant:applicants.keySet()) {
                            TableRow tableRow = new TableRow(ViewApplicants.this);
                            tableRow.setBackgroundColor(Color.parseColor("#000000"));

                            TextView textView2 = new TextView(ViewApplicants.this);
                            textView2.setWidth(200);
                            textView2.setTextColor(Color.parseColor("#FFFFFF"));
                            textView2.setText(applicant);
                            tableRow.addView(textView2);

                            TextView spaceheader2= new TextView(ViewApplicants.this);
                            spaceheader2.setWidth(50);
                            tablerow.addView(spaceheader2);


                            TextView textView3= new TextView(ViewApplicants.this);
                            textView3.setText(applicants.get(applicant));
                            textView3.setWidth(200);
                            textView3.setTextColor(Color.parseColor("#FFFFFF"));
                            tableRow.addView(textView3);

                            Button button4= new Button(ViewApplicants.this);
                            button4.setText("Interests");
                            tableRow.addView(button4);

                            Button button3= new Button(ViewApplicants.this);
                            button3.setText("Accept");
                            if(applicants.get(applicant).equals("accepted")){
                                button3.setEnabled(false);
                            }
                            tableRow.addView(button3);

                            Button button2= new Button(ViewApplicants.this);
                            button2.setText("Decline");
                            if(applicants.get(applicant).equals("rejected")){
                                button2.setEnabled(false);
                            }
                            tableRow.addView(button2);
                            table.addView(tableRow);


                            button4.setOnClickListener(new View.OnClickListener(){

                                public void onClick(View v){
                                    Intent intent= new Intent(ViewApplicants.this, ViewInterests.class);
                                    intent.putExtra("username",applicant);
                                    intent.putExtra("projectname",projectname);
                                    startActivity(intent);

                                }
                            });
                            button3.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Runnable runnable2= new Runnable(){
                                        public void run(){
                                            HashMap<String, String> temp= new HashMap<String, String>();

                                            temp=project.getApplicants();
                                            temp.put(applicant,"accepted");
                                            project.setApplicants(temp);

                                            User user= new User();
                                            user.setUsername(applicant);
                                            DynamoDBQueryExpression<User> queryExpression =new DynamoDBQueryExpression<User>().withHashKeyValues(user);
                                            List <User> queryList= mapper.query(User.class, queryExpression);

                                            user=queryList.get(0);
                                            HashMap<String, String> usertemp= new HashMap<String, String>();
                                            usertemp=user.getApplications();

                                            usertemp.put(projectname,"accepted/unread");
                                            user.setApplications(usertemp);
                                            mapper.save(project);
                                            mapper.save(user);

                                            Intent intent = new Intent(ViewApplicants.this, AcceptanceLetter.class);
                                            intent.putExtra("projectname",projectname);
                                            intent.putExtra("username",applicant);
                                            startActivity(intent);
                                        }

                                    };
                                    Thread mythread= new Thread(runnable2);
                                    mythread.start();
                                }
                            });
                            button2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v){
                                    HashMap<String, String> temp= new HashMap<String, String>();
                                    temp=project.getApplicants();
                                    temp.put(applicant,"rejected");
                                    project.setApplicants(temp);
                                    Runnable runnable2= new Runnable(){
                                        public void run(){
                                            User user= new User();
                                            user.setUsername(applicant);
                                            DynamoDBQueryExpression<User> queryExpression =new DynamoDBQueryExpression<User>().withHashKeyValues(user);

                                            List <User> queryList= mapper.query(User.class, queryExpression);
                                            user=queryList.get(0);
                                            HashMap<String, String> usertemp= new HashMap<String, String>();
                                            usertemp=user.getApplications();
                                            usertemp.put(projectname,"rejected/unread");

                                            user.setApplications(usertemp);
                                            mapper.save(project);
                                            mapper.save(user);

                                            Intent intent= new Intent(ViewApplicants.this,EditPersonalProject.class);
                                            intent.putExtra("projectname",projectname);
                                            startActivity(intent);

                                        }
                                    };
                                    Thread mythread2= new Thread(runnable2);
                                    mythread2.start();

                                }
                            });

                        }
                        TableRow tableRow3= new TableRow(getApplicationContext());
                        tableRow3.setBackgroundColor(Color.parseColor("#000000"));
                        Button button= new Button(getApplicationContext());

                        button.setText("Go Back");
                        tableRow3.addView(button);
                        table.addView(tableRow3);

                        button.setOnClickListener(new View.OnClickListener(){public void onClick(View view){
                            Intent intent= new Intent(ViewApplicants.this,EditPersonalProject.class);
                            intent.putExtra("projectname",projectname);
                            startActivity(intent);

                        }});
                    }
                });


            }
        };
    Thread mythread= new Thread(runnable);
        mythread.start();
    }
}
