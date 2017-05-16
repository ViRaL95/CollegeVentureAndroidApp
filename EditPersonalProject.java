package com.example.varun.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.graphics.Bitmap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EditPersonalProject extends AppCompatActivity {
    private String projectname;
    private DynamoDBMapper mapper;
    private AmazonDynamoDBClient ddbClient;
    private TableLayout tableLayout;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private EditText editprojectname;
    private TextView editadministratorname;
    private EditText editphonenumber;
    private EditText editlocation;
    private EditText editteammate;
    private EditText editprojectdescription;
    private ImageView plus;
    private ImageView minus;
    private int rownumber;
    private int teammatenumber;
    private TextView warning;
    private Bitmap imageBitMap;
    private static boolean phototaken=false;
    public static final int REQUEST_IMAGE_CAPTURE=1;
    private AmazonS3 s3;
    private String identitypoolid;

    /**
     * The purpose of the EditPersonalProject class is to retrieve the information for a specific project
     * that already exists within DynamoDB and allow the user to edit it with several EditTexts. It also allows
     * the user to retake the project photo.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_project);
        Credentials credentials= new Credentials();
        identitypoolid=credentials.getIdentityPoolId();
        //credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                identitypoolid, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        ddbClient =  new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

         s3= new AmazonS3Client(credentialsProvider);

        tableLayout=(TableLayout) findViewById(R.id.edit_personal_project_table);
        Intent wherefromintent=getIntent();
        Bundle wherefrombundle=wherefromintent.getExtras();
        if (wherefrombundle!=null){
            projectname=(String) wherefrombundle.get("projectname");
        }
        plus=(ImageView) findViewById(R.id.plus);
        minus=(ImageView) findViewById(R.id.minus);
        warning= (TextView) findViewById(R.id.edit_project_warning);
        /*
            If the plus sign is clicked a new Table Row will be created and the user
            will be allowed to enter a teammates name inside of it. The number of teammates
            that were created must be kept up with, thus the variable teammatenumber. This variable
            will also be used when the minus button is clicked
         */
        plus.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                TableRow tableRow= new TableRow(EditPersonalProject.this);
                tableRow.setBackgroundColor(Color.parseColor("#000000"));
                TextView textview= new TextView(EditPersonalProject.this);
                textview.setWidth(150);
                textview.setTextColor(Color.parseColor("#FFFFFF"));
                textview.setText("TeamMate Number "+teammatenumber);
                tableRow.addView(textview);

                EditText editText= new EditText(EditPersonalProject.this);
                editText.setWidth(150);
                editText.setTextColor(Color.parseColor("#FFFFFF"));
                editText.setHint("Enter TeamMate Number "+teammatenumber);

                tableRow.addView(editText);

                tableLayout.addView(tableRow,rownumber);
                rownumber++;
                teammatenumber++;

            }
        });
        /*
        If the minus sign is clicked a TableRow at the end of the table will be deleted. In order to
        ensure that rows from the table before the teammate row isnt deleted we have the edge case to only
        allow the deletion of a row if the row number is greater than and equal to 6.
         */
        minus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(rownumber>=6) {
                    rownumber--;
                    teammatenumber--;
                    View view2 = tableLayout.getChildAt(rownumber);
                    tableLayout.removeView(view2);
                }
            }
        });

        Runnable runnable= new Runnable(){
            public void run (){
                Project project= new Project();
                project.setProjectName(projectname);
                DynamoDBQueryExpression<Project> queryExpression=new DynamoDBQueryExpression<Project>().withHashKeyValues(project);
                final List<Project> projectList= mapper.query(Project.class, queryExpression);
                runOnUiThread(new Runnable(){
                    public void run(){


                         editprojectname= (EditText) findViewById(R.id.editProjectName);
                        editadministratorname=(TextView) findViewById(R.id.editAdministrator);
                        editprojectdescription=(EditText) findViewById(R.id.project_description);
                        editphonenumber=(EditText) findViewById(R.id.editPhonenumber);
                        editlocation= (EditText) findViewById(R.id.editProjectLocation);
                        editteammate=(EditText) findViewById(R.id.editTeammate);
                        if(projectList.size()>0){

                            String administrator= projectList.get(0).getAdministrator();
                            String projectlocation=projectList.get(0).getProjectLocation();
                            String projectphonenumber=projectList.get(0).getPhoneNumber();
                            String projectdescription=projectList.get(0).getProjectDescription();
                            List <String> projectteammates=projectList.get(0).getTeammates();


                            editprojectname.setText(projectname);
                            editadministratorname.setText(administrator);
                            editphonenumber.setText(projectphonenumber);
                            editlocation.setText(projectlocation);
                            editprojectdescription.setText(projectdescription);
                            editteammate.setText(projectteammates.get(0));
                             rownumber=5;
                             teammatenumber=2;
                            for (int i=1; i<= projectteammates.size()-1; i++){
                                String teammate=projectteammates.get(i);
                                TableRow tablerow= new TableRow(EditPersonalProject.this);
                                tablerow.setBackgroundColor(Color.parseColor("#000000"));
                                TextView textView= new TextView(EditPersonalProject.this);
                                textView.setWidth(150);
                                textView.setTextColor(Color.parseColor("#FFFFFF"));

                                textView.setText("Enter TeamMate "+teammatenumber);
                                tablerow.addView(textView);

                                EditText edittextteam= new EditText(EditPersonalProject.this);
                                edittextteam.setWidth(150);
                                edittextteam.setTextColor(Color.parseColor("#FFFFFF"));
                                edittextteam.setText(teammate);

                                tablerow.addView(edittextteam);

                                tableLayout.addView(tablerow,rownumber);
                                rownumber++;
                                teammatenumber++;
                            }
                        }

                    }

                });
            };

        };
        Thread thread= new Thread(runnable);
        thread.start();

    }

    public void editProject(View view){
       final String newprojectname= editprojectname.getText().toString();
        final String administratorname= editadministratorname.getText().toString();
        final String phonenumber= editphonenumber.getText().toString();
        final String location= editlocation.getText().toString();
        final String projectdescription=editprojectdescription.getText().toString();

        final List <String> listofteammates=new ArrayList<>();
        boolean empty=false;
        for (int i=4; i<=rownumber-1; i++) {
                View view2=tableLayout.getChildAt(i);
                if(view2 instanceof TableRow){
                    TableRow tableRow= (TableRow) view2;
                    TextView textview=(TextView) tableRow.getChildAt(1);
                    String teammate2=textview.getText().toString();

                    if(teammate2.matches("")){
                        empty=true;
                    }
                    else {
                        listofteammates.add(teammate2);
                    }
                }

        }

        if(!administratorname.matches("") && !projectdescription.matches("")&&!phonenumber.matches("") && !location.matches("") && !empty){
            warning.setText("");
            Runnable runnable= new Runnable(){
                public void run(){
                    Project project= mapper.load(Project.class, projectname);
                    project.setProjectName(newprojectname);
                    project.setTeammates(listofteammates);
                    project.setProjectLocation(location);
                    project.setPhoneNumber(phonenumber);
                    project.setAdministrator(administratorname);
                    project.setProjectDescription(projectdescription);
                    mapper.save(project);
                    boolean changed=false;
                    //if the project name was changed (which is the hash key) we need to delete this old project name
                    if(!projectname.toLowerCase().equals(newprojectname)){
                        Project project2=new Project();
                        project2.setProjectName(projectname);
                        mapper.delete(project2);
                        changed=true;
                    }
                    if(s3.doesObjectExist("personalprojectsimages",projectname)){
                        //takes old photo gets the image and puts it into database with new name
                        //condition checked
                        if(phototaken==false && changed==true) {
                            Log.v("condition1","condition1");
                            S3Object s3Object = s3.getObject(new GetObjectRequest("personalprojectsimages", projectname));
                            s3.deleteObject(new DeleteObjectRequest("personalprojectsimages", projectname));
                            InputStream inputStream=s3Object.getObjectContent();
                            PutObjectRequest putObjectRequest= new PutObjectRequest("personalprojectsimages",newprojectname,inputStream, new ObjectMetadata());
                            s3.putObject(putObjectRequest);
                        }
                        //takes the old photo deletes it, creates a new photo with a new name and puts it into database
                        //condition 2 CHECKED
                        else if(phototaken==true && changed==true){
                            s3.deleteObject(new DeleteObjectRequest("personalprojectsimages",projectname));
                            Log.v("condition2","condition2");
                            ByteArrayOutputStream bos=new ByteArrayOutputStream();
                            imageBitMap.compress(Bitmap.CompressFormat.PNG,0,bos);
                            byte[] bitmapdata=bos.toByteArray();

                            ByteArrayInputStream bosin=new ByteArrayInputStream(bitmapdata);
                            PutObjectRequest putObjectRequest= new PutObjectRequest("personalprojectsimages",newprojectname,bosin,new ObjectMetadata());
                            //putObejct takes a putObjectRequest
                            s3.putObject(putObjectRequest);


                        }
                        //deletes the old photo under the same name, creates new photo with same name
                        //condition 3 checked

                        else if(phototaken==true && changed==false){
                            s3.deleteObject(new DeleteObjectRequest("personalprojectsimages",projectname));
                            Log.v("condition3","condition3");
                            ByteArrayOutputStream bos= new ByteArrayOutputStream();
                            imageBitMap.compress(Bitmap.CompressFormat.PNG, 0,bos);
                            byte [] bitmapdata=bos.toByteArray();

                            ByteArrayInputStream bosin= new ByteArrayInputStream(bitmapdata);
                            PutObjectRequest putObjectRequest= new PutObjectRequest("personalprojectsimages",projectname,bosin, new ObjectMetadata());
                            s3.putObject(putObjectRequest);
                        }

                    }
                    //if there is no photo in the database already then one should create a new photo with the new name, or without the new name
                    else {

                    //condition checked
                        if (phototaken == true) {
                            Log.v("condition4","condition4");
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            imageBitMap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                            byte[] bitmapdata = bos.toByteArray();
                            ByteArrayInputStream bosin = new ByteArrayInputStream(bitmapdata);
                            if(changed){
                                PutObjectRequest putObjectRequest = new PutObjectRequest("personalprojectsimages", projectname, bosin, new ObjectMetadata());
                                s3.putObject(putObjectRequest);
                            }
                            else{
                                PutObjectRequest putObjectRequest = new PutObjectRequest("personalprojectsimages", newprojectname, bosin, new ObjectMetadata());
                                s3.putObject(putObjectRequest);
                            }

                        }

                    }
                }
            };
            Thread thread= new Thread(runnable);
            thread.start();


            Intent intent= new Intent(EditPersonalProject.this, StonyBrook.class);
            finish();
            startActivity(intent);
        }
        else{
            warning.setText("One of the fields is empty");
        }
    }

    public void viewProjectLocation(View view){
        String location=editlocation.getText().toString();
        if(!location.equals(" ")) {
            Intent intent = new Intent(EditPersonalProject.this, MapsMarkerActivity.class);
            intent.putExtra("address", location);
            startActivity(intent);
        }
    }
    public void retakePhoto(View view){
        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            phototaken=true;
            startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
        }
        else {
            phototaken = false;
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK){
            Log.v("resultcodeok","yahook");
            Bundle extras = data.getExtras();
            imageBitMap = (Bitmap) extras.get("data");
            //save imagebitmap to amazon s3
        }
    }
    public void viewApplicants(View view){
        Intent intent= new Intent(EditPersonalProject.this, ViewApplicants.class);
        intent.putExtra("project",projectname);

        startActivity(intent);
    }
    public void goBack(View view){
        Intent intent= new Intent(EditPersonalProject.this, StonyBrook.class);
        startActivity(intent);
    }
}
