package com.example.varun.finalproject;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.id;
import static android.R.attr.isDefault;
import static android.R.attr.onClick;
import static android.R.attr.start;
import static android.R.attr.text;


/**
 * Created by Varun on 4/12/17.
 */

/**
 * The personal projects fragment contains the information for all the projects that have been created thus far
 * at stony brook. Administrators will ahve the rights to edit a project and regular users will ahve the privileges
 * in order to view a project. This class also includes a notification center which only allows applications to appear
 * when the project has not been viewed yet. This information is stored in the status of a user within the applications
 * hashmap
 */

public class PersonalProjectsFragment extends Fragment{
    private DynamoDBMapper mapper;
    private AmazonDynamoDBClient ddbClient;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private TableLayout tableLayout;
    private TableLayout notifications;
    private HashMap<String, String> applications;
    private String identitypoolid;
    public PersonalProjectsFragment(){


    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //initialize the credentials provider
        Credentials credentials= new Credentials();
        identitypoolid=credentials.getIdentityPoolId();
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getActivity().getApplicationContext(),
                identitypoolid, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        //create an amazondynamodb client
        ddbClient =  new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
        View view = inflater.inflate(R.layout.personalprojectsfragment,container,false);
        tableLayout=(TableLayout) view.findViewById(R.id.personal_projects_table);
        notifications=(TableLayout) view.findViewById(R.id.notifications);
        Button button3 =(Button) view.findViewById(R.id.create_project_button);
        //send the user to the Add Personal Projects class
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddPersonalProject.class);
                intent.putExtra("username",((StonyBrook)getActivity()).getUsername());
                startActivity(intent);
            }
        });


        Runnable runnable = new Runnable() {
            public void run() {
                DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                final List<Project> scanResult=mapper.scan(Project.class,scanExpression);
                final String username= ((StonyBrook) getActivity()).getUsername();
                User user= new User();
                user.setUsername(username);
                DynamoDBQueryExpression queryExpression= new DynamoDBQueryExpression().withHashKeyValues(user);
                final List<User> queryResult2= mapper.query(User.class, queryExpression);
                //dynamically generate the table depending on te number of projects that have been viewed
                //by users
                getActivity().runOnUiThread(new Runnable(){
                    public void run(){
                        final User user=queryResult2.get(0);
                        applications=user.getApplications();

                        TableRow tablerow= new TableRow(getActivity());
                        TextView textView= new TextView(getActivity());
                        textView.setText("Project");
                        textView.setWidth(300);
                        textView.setTextColor(Color.parseColor("#FFFFFF"));
                        tablerow.addView(textView);

                        TextView spaceheader= new TextView(getActivity());
                        spaceheader.setWidth(50);
                        tablerow.addView(spaceheader);

                        TextView textview1= new TextView(getActivity());
                        textview1.setWidth(300);
                        textview1.setTextColor(Color.parseColor("#FFFFFF"));
                        textview1.setText("Status");

                        tablerow.addView(textview1);

                        tablerow.setBackgroundColor(Color.parseColor("#000000"));
                        notifications.addView(tablerow);
                        //In order to find out whether a user has been accepted, rejected or their application is still
                        //pending we must iterate through the entire applications hashmap
                        for (final String projects: applications.keySet()){
                            final String [] status=applications.get(projects).split("/");
                            if(status[0].equals("pending") ||(status[1].equals("unread") && (status[0].equals("accepted")||status[0].equals("rejected")))){
                                TableRow tableRow= new TableRow(getActivity());
                                TextView textView2= new TextView(getActivity());
                                textView2.setWidth(300);
                                textView2.setText(projects);
                                textView2.setTextColor(Color.parseColor("#FFFFFF"));
                                tableRow.addView(textView2);


                                TextView spaceheader2= new TextView(getActivity());
                                spaceheader2.setWidth(50);
                                tableRow.addView(spaceheader2);

                                TextView textView3= new TextView(getActivity());
                                textView3.setText(status[0]);
                                textView3.setWidth(300);
                                textView3.setTextColor(Color.parseColor("#FFFFFF"));
                                tableRow.addView(textView3);
                                if(status[0].equals("rejected")) {
                                    final Button dismiss = new Button(getActivity());
                                    dismiss.setText("Disimss");
                                    dismiss.setWidth(50);
                                    tableRow.addView(dismiss);

                                    dismiss.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View view) {
                                            status[1]="read";
                                            String finalstatus="";
                                            for (int i=0; i<=status.length-1; i++){
                                                finalstatus+="/"+status[i];
                                            }
                                            applications.put(projects,finalstatus);
                                            user.setApplications(applications);

                                            TableRow parent=(TableRow) dismiss.getParent();
                                            notifications.removeView(parent);


                                            Runnable runnable=new Runnable(){
                                                public void run(){
                                                    mapper.save(user);

                                                }

                                            };
                                            Thread mythread= new Thread(runnable);
                                            mythread.start();

                                        }
                                    });
                                }
                                else if(status[0].equals("accepted")){
                                 Button viewAcceptance= new Button(getActivity());
                                    viewAcceptance.setText("Acceptance");
                                    viewAcceptance.setWidth(50);
                                    tableRow.addView(viewAcceptance);
                                    viewAcceptance.setOnClickListener(new View.OnClickListener(){
                                        public void onClick(View view){
                                            Runnable runnable= new Runnable(){
                                                public void run(){
                                                    status[1]="read";
                                                    String finalstatus="";
                                                    for (int i=0; i<=status.length-1; i++){
                                                        finalstatus+=status[i]+"/";
                                                    }
                                                    applications.put(projects,finalstatus);

                                                    user.setApplications(applications);
                                                    mapper.save(user);

                                                    Intent intent= new Intent(getActivity(), ViewAcceptanceLetter.class);
                                                    intent.putExtra("username",username);
                                                    intent.putExtra("projectname",projects);
                                                    startActivity(intent);
                                                }
                                            };
                                            Thread mythread= new Thread(runnable);
                                            mythread.start();

                                        }
                                    });
                                }
                                else if (status[0].equals("pending")){
                                    TextView pending= new TextView(getActivity());
                                    pending.setWidth(50);
                                    pending.setText("Pending");
                                    tableRow.addView(pending);
                                }
                                tableRow.setBackgroundColor(Color.parseColor("#000000"));
                                notifications.addView(tableRow);
                            }
                        }

                        final String username=((StonyBrook)getActivity()).getUsername();
                        TableRow header= new TableRow(getActivity());
                        final TextView projectname= new TextView(getActivity());
                        projectname.setText("Project Name");
                        projectname.setWidth(200);

                        TextView spaceheader1= new TextView(getActivity());
                        spaceheader1.setWidth(50);

                        TextView usernameheader= new TextView(getActivity());
                        usernameheader.setText("Administrator");
                        usernameheader.setWidth(100);

                        TextView spaceheader2= new TextView(getActivity());
                        spaceheader2.setWidth(50);

                        TextView phonenumber= new TextView(getActivity());
                        phonenumber.setText("Phone Number");



                        header.addView(projectname);
                        header.addView(spaceheader1);
                        header.addView(usernameheader);
                        header.addView(spaceheader2);
                        header.addView(phonenumber);
                        tableLayout.addView(header);

                        for (final Project project: scanResult){
                                TableRow tableRow= new TableRow(getActivity());
                                tableRow.setBackgroundColor(Color.parseColor("#000000"));
                                TextView textViewn= new TextView(getActivity());
                                textViewn.setWidth(150);
                                textViewn.setTextColor(Color.parseColor("#FFFFFF"));

                                textViewn.setText(project.getProjectName());
                                textViewn.setWidth(150);
                                textViewn.setTextColor(Color.parseColor("#FFFFFF"));
                                tableRow.addView(textViewn);

                                TextView space= new TextView(getActivity());
                                space.setWidth(50);
                                tableRow.addView(space);

                                String administrator_str=project.getAdministrator();
                                TextView administrator_text= new TextView(getActivity());
                                administrator_text.setTextColor(Color.parseColor("#FFFFFF"));
                                administrator_text.setText(administrator_str);
                                administrator_text.setWidth(150);
                                tableRow.addView(administrator_text);

                                 TextView space2= new TextView(getActivity());
                                space2.setWidth(50);
                                tableRow.addView(space2);

                            String phonenumber_str=project.getPhoneNumber();
                                TextView phonenumber_text= new TextView(getActivity());
                                phonenumber_text.setText(phonenumber_str);
                                phonenumber_text.setTextColor(Color.parseColor("#FFFFFF"));
                                phonenumber_text.setWidth(100);
                                tableRow.addView(phonenumber_text);
                                if(administrator_str.equals(username)) {
                                    Button button2 = new Button(getActivity());
                                    button2.setWidth(30);
                                    button2.setHeight(40);
                                    button2.setText("Edit project");
                                    button2.setTextColor(Color.parseColor("#FFFFFF"));

                                    button2.setOnClickListener(new View.OnClickListener(){
                                        public void onClick(View v){
                                            Intent intent= new Intent(getActivity(), EditPersonalProject.class);
                                            intent.putExtra("projectname",project.getProjectName());
                                            getActivity().finish();
                                            startActivity(intent);
                                        }

                                    });

                                    tableRow.addView(button2);
                                }
                                else {
                                    Button button = new Button(getActivity());
                                    button.setText("View Project");
                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent= new Intent(getActivity(),ViewProject.class);
                                            Bundle extras= new Bundle();
                                            extras.putString("username",username);
                                            extras.putString("projectname",project.getProjectName());
                                            intent.putExtras(extras);
                                            startActivity(intent);
                                        }
                                    });
                                    tableRow.addView(button);
                                }
                                 tableLayout.addView(tableRow, new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        }

                    }
                });
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();



        return view;
    }
}
