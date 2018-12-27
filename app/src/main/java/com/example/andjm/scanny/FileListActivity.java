package com.example.andjm.scanny;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FileListActivity extends AppCompatActivity {

    public static String EXTRA_FILE_NAME = "file_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        String[] fileImageList = b.getStringArray("imagelist");


        ArrayAdapter<String> fileAdapter =
                new ArrayAdapter<String>(this,
                        R.layout.item,
                        R.id.file_name,
                        fileImageList
                );


        final ListView piImageList = new ListView(this);
        piImageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object o = piImageList.getItemAtPosition(i);
                LinearLayout ll = (LinearLayout) view;
                TextView tv = (TextView) ll.findViewById(R.id.file_name);
                String filename = tv.getText().toString();
                Intent intent = new Intent();
                intent.putExtra(EXTRA_FILE_NAME, filename);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        setContentView(piImageList);
        piImageList.setAdapter(fileAdapter);
    }
}
