package com.hypercron;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Vector;

public class ProjectIndex {
    public static final String FILENAME = "index.json";
    public static final String HYPERCRON = "Hypercron";

    private File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    private File file = null;
    private JSONObject json = null;
    private Vector<Project> projects = new Vector<>();

    public ProjectIndex() {
        directory = new File(directory, HYPERCRON);
        if (!directory.exists() && !directory.mkdirs()) {

            Log.e("write_file", "Could not write to external storage");
            return;
        }
        file = new File(directory, FILENAME);
        if (file.exists()) loadFile();
        else createFile();
    }

    public void addImage(String project_name, String image_name) {

        try {
            JSONObject project = json.getJSONObject(project_name);
            project.getJSONArray("images").put(image_name);
            Log.i("debug", json.toString());
            Log.i("debug", "addImage");
            File imageFile = new File(new File(directory, project_name), image_name);
            if (!imageFile.exists()) return;
            for (int i = 0; i < projects.size(); i++) {
                Project current = projects.elementAt(i);
                if (current.getName().equals(project_name)) {
                    current.setLatest(imageFile.getPath());
                    break;
                }
            }
        } catch (JSONException e) {
            Log.e("create_json", "Could not add image to project: " + e.getMessage());
        }
        checkForNew();
    }

    public void addProject(String project_name) {
        Log.d("debug", "adding project " + project_name);

        try {
            if (json.has(project_name)) {
                int offset = 0;
                while(json.has(project_name + " (" + String.valueOf(offset) + ")")) offset++;
                project_name = project_name + " (" + String.valueOf(offset) + ")";
            }
            JSONObject project = new JSONObject();
            project.put("images", new JSONArray());
            json.put(project_name, project);
            Log.i("debug", "addProject");
            saveFile();
        } catch (JSONException e) {
            Log.e("create_json", "Could not add project to index: " + e.getMessage());
        }
        projects.add(new Project(project_name));
    }

    private void checkForNew() {

        //  If there is no "New Project" add one
        if (!json.has("New Project") || projects.lastElement().getLatest() != null)
            addProject("New Project");
        Log.d("debug", "checking for need for new project");
        saveFile();
    }

    private void createFile() {
        json = new JSONObject();
        Log.i("debug", "createFile");
        checkForNew();
    }

    private void findProjects() {
        Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {

            String project_name = iterator.next();
            Log.d("debug", project_name);
            Project current = new Project(project_name);
            try {
                JSONObject project = json.getJSONObject(project_name);
                JSONArray images = project.getJSONArray("images");
                current.setImageQuantity(images.length());
                Log.d("debug", images.toString());
                String latest = images.getString(images.length() - 1);
                File imageFile = new File(directory, project_name);
                if (imageFile.exists()) {

                    imageFile = new File(imageFile, latest);
                    current.setLatest(imageFile.getPath());
                }
                Log.d("debug", "new size: " + String.valueOf(projects.size()));
            } catch (JSONException e) {
                Log.e("parse_json", "Could not parse index: " + e.getMessage());
            }
            projects.add(current);
        }
    }

    public Vector<Project> getProjects() {

        return projects;
    }

    private void loadFile() {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int buffer_size = inputStream.available();
            byte[] buffer = new byte[buffer_size];
            inputStream.read(buffer);
            inputStream.close();
            json = new JSONObject(new String(buffer, "UTF-8"));
            Log.d("debug", "loaded " + json.toString());
            findProjects();
        } catch (IOException e) {
            Log.e("read_file", "Could not read index: " + e.getMessage());
        } catch (JSONException e) {
            Log.e("parse_json", "Could not parse index: " + e.getMessage());
        }
        checkForNew();
    }

    public void rename(String old_name, String new_name) {
        try {
            if (json.has(new_name)) {
                int offset = 0;
                while(json.has(new_name + " (" + String.valueOf(offset) + ")")) offset++;
                new_name = new_name + " (" + String.valueOf(offset) + ")";
            }
            JSONObject project = json.getJSONObject(old_name);
            json.remove(old_name);
            json.put(new_name, project);
            for (int i = 0; i < projects.size(); i++) {
                Project current = projects.elementAt(i);
                if (current.getName().equals(old_name)) {

                    current.setName(new_name);
                }
            }
        } catch (JSONException e) {
            Log.e("create_json", "Could not rename project: " + e.getMessage());
        }
        checkForNew();
    }

    private void saveFile() {
        try {
            Log.d("debug", file.getAbsolutePath());
            FileOutputStream outputStream = new FileOutputStream(file);
            Log.d("debug", "step 1");
            Log.d("debug", json.toString());
            outputStream.write(json.toString().getBytes());
            Log.d("debug", "step 2");
            outputStream.close();
            Log.d("debug", "step 3");
        } catch (IOException e) {
            Log.e("write_file", "Could not write to index");
        }
    }
}
