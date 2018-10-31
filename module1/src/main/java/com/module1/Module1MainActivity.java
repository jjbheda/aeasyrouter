package com.module1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.aeasyannotation.Extra;
import com.aeasyannotation.Route;
import com.routercore.AEasyRouter;

@Route(path = "/module1/module1main")
public class Module1MainActivity extends AppCompatActivity {

    @Extra
    String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module1_main);
        AEasyRouter.getInstance().inject(this);
        Toast.makeText(this, "msg=" + msg , Toast.LENGTH_SHORT).show();
    }
}
