package com.kuldiegor.remote_control;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return MainFragment.newInstance();
    }
}
