package com.example.varun.finalproject;

import android.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;


import java.util.List;

/**
 * The following Fragment is simply the fragment which is displayed in order to display the About Us Page
 * for the College Venture application. The purpose of this is to give the users an idea of what this application
 * is better about and the current status and future of the application
 */

public class AboutUsFragment extends Fragment {
    private TextView aboutus;
    private String about;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.aboutusfragment,container,false);
        about="College Venture is an app which provides users who go to Stony Brook University with a broad variety of options for different projects that students are creating currently" +
                "This app will be extended to be applied to all major schools in the country, and has potential for future business growth";
        aboutus=(TextView) view.findViewById(R.id.aboutus);
        aboutus.setText(about);
        return view;
    }


}
