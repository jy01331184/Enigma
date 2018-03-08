package com.enigma.test;

import android.app.Activity;
import android.os.Bundle;

import com.enigma.message.MCenter;
import com.enigma.message.annotations.Subscribe;
import com.enigma.message.slink.StaticMessenger;
import com.enigma.object.annotations.AutoWrap;

import javax.inject.Inject;

@AutoWrap
public class MainActivity extends Activity {

    @Inject
    public Object object;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MCenter.getInstance().regist(this);
        MCenter.getInstance().post("event", "hahaha", 11111);
        StaticMessenger.getInstance().dump();

        System.out.println("hahazzz:"+object);
    }


    @Subscribe("event")
    public void testMCenter(String str, int i) {
        System.out.println("event:" + str + "-" + i);
    }


}
