package com.hypercron;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ProjectActivity extends AppCompatActivity {
    private int imageQuantity;
    private String lastImage;
    private String projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        projectName = getIntent().getStringExtra("name");
        imageQuantity = getIntent().getIntExtra("image_quantity", 0);
        lastImage = getIntent().getStringExtra("last_image");

        EditText editText_projectName = (EditText) findViewById(R.id.editText_project_name);
        if (editText_projectName != null) editText_projectName.setText(projectName);
        TextView textView_imageQuantity = (TextView) findViewById(R.id.textView_image_quantity);
        if (textView_imageQuantity != null) textView_imageQuantity.setText(String.valueOf(imageQuantity) + " images");
        ImageView imageView_lastImage = (ImageView) findViewById(R.id.imageView_last_image);
        if (imageView_lastImage != null) imageView_lastImage.setImageBitmap(BitmapFactory.decodeFile(lastImage));
    }

    public void exitActivity(View view) {
        Intent result = new Intent();
        result.putExtra("new_name", "");
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    public void saveName(View view) {
        Intent result = new Intent();
        String new_name = projectName;
        EditText editText_projectName = (EditText) findViewById(R.id.editText_project_name);
        if (editText_projectName != null) new_name = editText_projectName.getText().toString();
        if (new_name.equals(projectName)) {
           new_name = "";
        }
        result.putExtra("new_name", new_name);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    public void onBackPressed() {
        exitActivity(new View(this));
    }
}
