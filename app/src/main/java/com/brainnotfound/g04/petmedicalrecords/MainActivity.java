package com.brainnotfound.g04.petmedicalrecords;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.brainnotfound.g04.petmedicalrecords.module.SaveFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_view, new IntroFragment()).commit();
        }
    }

    @Override
    public void onBackPressed() {

        if(SaveFragment.getName().equals("RegisterFragment")) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.main_view, new IntroFragment()).commit();
        } else if(SaveFragment.getName().equals("LoginFragment")) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.main_view, new IntroFragment()).commit();
        } else if (SaveFragment.getName().equals("ProfileFragment")){
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.main_view, new MenuFragment()).commit();
        } else if (SaveFragment.getName().equals("ProfileEditFragment")){
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.main_view, new ProfileFragment()).commit();
        } else {
            super.onBackPressed();
        }
    }
}