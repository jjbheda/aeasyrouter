package com.arouter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.aeasyannotation.Route;
import com.routercore.AEasyRouter;

import com.baseprovider.BaseProvider;

@Route(path = "/main/main")
public class MainActivity extends AppCompatActivity {
    private BaseProvider baseProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initProviders();
    }
    private void initProviders() {
        baseProvider = (BaseProvider) AEasyRouter.getInstance().build("/module1/provider").navigation();
    }

    public void add(View view) {
        int num = baseProvider.add(5, 6);
        Toast.makeText(this, "5+6=" + num, Toast.LENGTH_SHORT).show();
    }

    public void startModule1MainActivity(View view) {
//        EasyRouter.getsInstance().build("/module1/module1main").navigation();
        AEasyRouter.getInstance().build("/module1/module1main")
                .withString("msg", "从MainActivity").navigation();
    }

}
