package com.example.varun.finalproject;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.RelativeLayout;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.model.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * The Add Personal Project is the class which allows the creation of a project. It also includes form
 * validation to ensure that none of the TextViews are empty and meet a set of criterias.
 * This class includes the method OnActivityResult which is called after the camera takes a photo
 * with the respective resultcode and requestcode. Depending on the value of the resultcode and requestcode
 * we are ensured that the photo was taken properly.
 */
public class AddPersonalProject extends AppCompatActivity {
    private DynamoDBMapper mapper;
    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonS3 s3;
    private EditText projectname;
    private TextView administrator;

    private Bitmap imageBitMap;
    private ImageView plus;
    private TableLayout createprojecttable;
    private EditText phonenumber;
    private EditText projectdescription;
    private EditText projectLocation;
    private TextView projectWarning;
    private int teammaterow;
    private int teammatenumber;
    private String username;
    private ImageView minus;
    private static final int REQUEST_IMAGE_CAPTURE=1;
    private String identitypoolid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_personal_project);

        Credentials credentials= new Credentials();
        identitypoolid=credentials.getIdentityPoolId();
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                identitypoolid, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        ddbClient =  new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        s3=new AmazonS3Client(credentialsProvider);

        Bundle bundle=getIntent().getExtras();
         username=(String) bundle.get("username");
         projectname= (EditText) findViewById(R.id.projectName);
         administrator= (TextView) findViewById(R.id.administrator);
        createprojecttable=(TableLayout) findViewById(R.id.create_project_table);
        phonenumber= (EditText) findViewById(R.id.phonenumber);
        projectLocation=(EditText) findViewById(R.id.projectLocation);
        plus=(ImageView) findViewById(R.id.plus);
        minus= (ImageView) findViewById(R.id.minus);
        projectdescription=(EditText) findViewById(R.id.project_description);

        projectWarning=(TextView) findViewById(R.id.project_warning);

        administrator.setText(username);
        teammaterow=5;
        teammatenumber=2;
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRow tableRow= new TableRow(AddPersonalProject.this);
                tableRow.setBackgroundColor(Color.parseColor("#000000"));
                TextView textview= new TextView(AddPersonalProject.this);
                textview.setWidth(150);
                textview.setTextColor(Color.parseColor("#FFFFFF"));
                textview.setText("TeamMate Number "+teammatenumber);

                tableRow.addView(textview);

                EditText editText= new EditText(AddPersonalProject.this);
                editText.setWidth(150);
                editText.setTextColor(Color.parseColor("#FFFFFF"));
                editText.setHint("Enter TeamMate Number "+teammatenumber);

                tableRow.addView(editText);

                createprojecttable.addView(tableRow,teammaterow);
                teammaterow++;
                teammatenumber++;

            }
        });
        minus.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(teammaterow>=6){
                    teammaterow--;
                    teammatenumber--;
                    View deleterow= createprojecttable.getChildAt(teammaterow);
                    createprojecttable.removeView(deleterow);
                }
            }
        });

    }
    /*
        This method handles all form validation for the projectnames, the phonenumber, the project location and the project description.
     */
    public void createProject(View view) {
        final String projectnamestr = projectname.getText().toString();
        final String phonenumberstr = phonenumber.getText().toString();
        final String projectlocationstr = projectLocation.getText().toString();
        final String projectdescriptionstr=projectdescription.getText().toString();
    /*the following is the functionality to find out the names of the team mates for a given application
         the number of team mates depends on the number of new team mates that were created
         */
        boolean teammateempty = false;
        final List<String> teammates = new ArrayList<>();
        for (int i = 4; i <= teammaterow - 1; i++) {
            View view2 = createprojecttable.getChildAt(i);
            //if the child that was retrieved from the table is an instance of TableRow then continue
            if (view2 instanceof TableRow) {
                TableRow tablerow = (TableRow) view2;
                TextView textView = (TextView) tablerow.getChildAt(1);
                if (textView.getText().toString().matches("")) {
                    teammateempty = true;
                } else {
                    String teammate = textView.getText().toString();
                    teammates.add(teammate);
                }
            }
        }
        //form validation
        if (!projectnamestr.matches("") && !phonenumberstr.matches("") && !projectlocationstr.matches("") &&!projectdescriptionstr.matches("") && !teammateempty) {
            projectWarning.setText("");

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    HashMap<String, String> temp= new HashMap<>();
                    Project project2= new Project();
                    project2.setProjectName(projectnamestr);
                    DynamoDBQueryExpression<Project> dynamoDBQueryExpression= new DynamoDBQueryExpression<Project>().withHashKeyValues(project2);
                /*retrive the number of projects that already exist with this name. If the number of projects that exist
                  with this name is greater than 0 then we dont create the project and instead display a warning
                  */
                    List<Project> projectList= mapper.query(Project.class,dynamoDBQueryExpression);
                    if(projectList.size()==0) {
                        Project project = new Project();
                        project.setProjectName(projectnamestr);
                        project.setAdministrator(username);
                        project.setPhoneNumber(phonenumberstr);
                        project.setProjectLocation(projectlocationstr);
                        project.setTeammates(teammates);
                        project.setApplicants(temp);
                        project.setProjectDescription(projectdescriptionstr);
                        mapper.save(project);
                    }
                    else{
                        projectWarning.setText("A project with this name already exists");
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
            /*In order to store the imageBitMap on Amazon S3 the image must be compressed into a bytearrayoutput stream.
            The bytearray output stream is then converted to an array of bytes and is then stored in a ByteArrayInputStream.
            The inputstream is then stored in S3 using a putObjectRequest
            */
            if(imageBitMap!=null) {


                Runnable runnable2 = new Runnable(){
                    public void run(){
                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                imageBitMap.compress(Bitmap.CompressFormat.PNG,0,bos);
                byte[] bitmapdata=bos.toByteArray();

                    ByteArrayInputStream bosin=new ByteArrayInputStream(bitmapdata);
                        PutObjectRequest putObjectRequest= new PutObjectRequest("personalprojectsimages",projectnamestr,bosin,new ObjectMetadata());
                        s3.putObject(putObjectRequest);
                    }
                };
                Thread thread1= new Thread(runnable2);
                thread1.start();


            }
            Intent intent = new Intent(AddPersonalProject.this, StonyBrook.class);
            finish();
            startActivity(intent);
        } else {
            projectWarning.setText("One of the fields is empty");
        }
    }
    /*
    The takePicture method creates an Intent and opens up an activity where the camera is in use. After the photo
    is taken the onActivityResult method is then called.
     */
    public void takePicture(View view){
        Intent takePictureIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
        }

    }
    /*

    Photo was retrieved and stored in a bitMap if the requestCode was equal to REQUEST_IMAGE_CAPTURE and
    if the resultCode was equal to RESULT_OK.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK){

                 imageBitMap = (Bitmap) data.getExtras().get("data");
                if(imageBitMap==null){
                }
        }
    }
    public void goBack(View view){
        Intent intent= new Intent(AddPersonalProject.this, StonyBrook.class);
        startActivity(intent);
    }
    }



