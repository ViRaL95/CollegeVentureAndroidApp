package com.example.varun.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import org.w3c.dom.Text;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewProject extends AppCompatActivity {
    private TextView viewProjectName;
    private TextView viewAdministrator;
    private TextView viewPhoneNumber;
    private TextView viewProjectlLocation;
    private TextView viewTeamMate;
    private TextView projectdescriptiontext;

    private Button applicationButton;
    private AmazonS3 s3;
    private DynamoDBMapper mapper;
    private AmazonDynamoDBClient ddbClient;
    private TableLayout tableLayout;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private String projectname;
    private int rownumber;
    private int teammatenumber;
    private Button viewProjectPicture;
    boolean disabled=false;
    private String username;
    private HashMap<String, String> applicants;
    private HashMap<String, String> applications;
    private String identitypoolid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_project);

        viewProjectName=(TextView) findViewById(R.id.viewProjectName);
        projectdescriptiontext=(TextView) findViewById(R.id.project_description);
        viewAdministrator=(TextView) findViewById(R.id.viewAdministrator);
        viewPhoneNumber=(TextView) findViewById(R.id.viewPhonenumber);
        viewProjectlLocation=(TextView) findViewById(R.id.viewProjectLocation);
        viewTeamMate=(TextView) findViewById(R.id.viewTeammate);
        tableLayout=(TableLayout) findViewById(R.id.view_personal_project_table);
        viewProjectPicture=(Button) findViewById(R.id.viewPersonalProjectPicture);
        applicationButton=(Button) findViewById(R.id.applyToProject);

        Credentials credentials= new Credentials();
        identitypoolid=credentials.getIdentityPoolId();

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                identitypoolid, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        s3=new AmazonS3Client(credentialsProvider);

        ddbClient =  new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);


        Intent wherefromintent=getIntent();
        Bundle bundle=wherefromintent.getExtras();
        if(bundle!=null) {
             projectname=(String)bundle.get("projectname");
            username=(String) bundle.get("username");
        }
        Runnable runnable= new Runnable(){
            public void run (){
                if(!s3.doesObjectExist("personalprojectsimages",projectname)){
                    disabled=true;
                }

                Project project= new Project();
                project.setProjectName(projectname);
                DynamoDBQueryExpression<Project> queryExpression=new DynamoDBQueryExpression<Project>().withHashKeyValues(project);

                final List<Project> projectList= mapper.query(Project.class, queryExpression);
                runOnUiThread(new Runnable(){
                    public void run(){
                        if(disabled){
                            viewProjectPicture.setEnabled(false);
                        }

                        if(projectList.size()>0){
                            HashMap<String, String> applicants;
                            applicants=projectList.get(0).getApplicants();

                            if(applicants.size()>0){
                                //already an applicant for this project
                                if(applicants.containsKey(username)){
                                    applicationButton.setEnabled(false);
                                }
                            }
                            String projectname=projectList.get(0).getProjectName();
                            String administrator= projectList.get(0).getAdministrator();
                            String projectlocation=projectList.get(0).getProjectLocation();
                            String projectphonenumber=projectList.get(0).getPhoneNumber();
                            String projectdescription=projectList.get(0).getProjectDescription();
                            List <String> projectteammates=projectList.get(0).getTeammates();


                            viewProjectName.setText(projectname);
                            viewAdministrator.setText(administrator);
                            viewPhoneNumber.setText(projectphonenumber);
                            viewProjectlLocation.setText(projectlocation);
                            viewTeamMate.setText(projectteammates.get(0));
                            rownumber=5;
                            teammatenumber=2;
                            for (int i=1; i<= projectteammates.size()-1; i++){
                                String teammate=projectteammates.get(i);
                                TableRow tablerow= new TableRow(ViewProject.this);
                                tablerow.setBackgroundColor(Color.parseColor("#000000"));
                                TextView textView= new TextView(ViewProject.this);
                                textView.setWidth(150);
                                textView.setTextColor(Color.parseColor("#FFFFFF"));

                                textView.setText("TeamMate "+teammatenumber);
                                tablerow.addView(textView);

                                TextView texttextteam= new TextView(ViewProject.this);
                                texttextteam.setWidth(150);
                                texttextteam.setTextColor(Color.parseColor("#FFFFFF"));
                                texttextteam.setText(teammate);

                                tablerow.addView(texttextteam);

                                tableLayout.addView(tablerow,rownumber);
                                rownumber++;
                                teammatenumber++;
                            }

                            projectdescriptiontext.setText(projectdescription);

                        }

                    }

                });
            };

        };
        Thread thread= new Thread(runnable);
        thread.start();


    }

public void viewProjectLocation(View view){

    TextView textview= (TextView) findViewById(R.id.viewProjectLocation);
    String location=textview.getText().toString();

    Intent intent= new Intent(ViewProject.this, MapsMarkerActivity.class);
    intent.putExtra("address",location);

    startActivity(intent);

}
    public void viewProjectPicture(View view){
    Runnable runnable= new Runnable(){
        public void run(){

        Bitmap bitmap;
        S3Object s3Object= s3.getObject(new GetObjectRequest("personalprojectsimages",projectname));
        InputStream objectdata=s3Object.getObjectContent();
        bitmap= BitmapFactory.decodeStream(objectdata);
        Intent intent= new Intent(ViewProject.this, DisplayProject.class);
        intent.putExtra("imagename", bitmap);

        startActivity(intent);

        }
    };
    Thread thread= new Thread(runnable);
    thread.start();


    }

    public void applyToProject(View view){
        //when you apply to the project you should get the username and you should return a hashmap with strings as the key
        //and string as the value. you should then use the company name as the index into the second hash map which returns another string
        //this string should return whether the user was rejected, accepted etc.
    Runnable runnable= new Runnable() {
        @Override
        public void run() {
            Project project= new Project();
            project.setProjectName(projectname);
            DynamoDBQueryExpression<Project> queryExpression=new DynamoDBQueryExpression<Project>().withHashKeyValues(project);
            List<Project> projectList= mapper.query(Project.class, queryExpression);
            Project project2=projectList.get(0);
            applicants= new HashMap<>();
            applicants = project2.getApplicants();

            applicants.put(username,"pending");
            project2.setApplicants(applicants);

            User user= new User();
            user.setUsername(username);
            DynamoDBQueryExpression<User> queryExpression2=new DynamoDBQueryExpression<User>().withHashKeyValues(user);
            List <User> userList= mapper.query(User.class,queryExpression2);
            applications= new HashMap<>();
            user=userList.get(0);
            applications=user.getApplications();
            applications.put(projectname,"pending");
            user.setApplications(applications);
            mapper.save(project2);
            mapper.save(user);
            Intent intent= new Intent(ViewProject.this, StonyBrook.class);
            startActivity(intent);
        }
    };
    Thread mythread= new Thread(runnable);
        mythread.start();

    }
    public void goBack(View view){
        Intent intent= new Intent(ViewProject.this, StonyBrook.class);
        startActivity(intent);
    }
}
