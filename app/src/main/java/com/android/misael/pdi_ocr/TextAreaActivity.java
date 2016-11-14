package com.android.misael.pdi_ocr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TextAreaActivity extends AppCompatActivity {
    private String text;
    private static final int DIR_PICK = 123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_area);
        Intent intent = getIntent();
        text = intent.getStringExtra("text");
        EditText resultText = (EditText)findViewById(R.id.resultText);
        resultText.setText(text);
    }
    public void searchText(View view) throws UnsupportedEncodingException {
        String query = URLEncoder.encode(text,"utf-8");
        String url = "http://www.google.com/search?q="+query;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
    public void SaveText(View view){
        setDirPickIntent();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DIR_PICK && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String route = uri.getPath();
            storeText(route);
        }
    }

    private void setDirPickIntent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
        intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
        intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        startActivityForResult(intent, DIR_PICK);
    }

    private void storeText(String route){
        File directory = new File(route);
        File file = new File(directory,"resultOcr.txt");

        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(text.getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
