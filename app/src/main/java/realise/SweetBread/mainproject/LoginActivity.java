package realise.SweetBread.mainproject;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class LoginActivity extends AppCompatActivity {

    EditText IP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        String ip;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput("data.txt")));
            ip = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            ip = "";
        }


        IP = findViewById(R.id.ip);
        IP.setText(ip);
        final Button confirm = findViewById(R.id.login);
        if (ip.length() >= 7) confirm.setEnabled(true);

        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       android.text.Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) + source
                            .subSequence(start, end) + destTxt.substring(dend);
                    if (!resultingTxt.matches
                            ("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")
                    ) {
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i=0; i<splits.length; i++) {
                            if (Integer.parseInt(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
                return null;
            }

        };
        IP.setFilters(filters);

        TextWatcher listener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (IP.getText().length() >= 7) {
                    confirm.setEnabled(true);
                } else {
                    confirm.setEnabled(false);
                }
            }
        };

        IP.addTextChangedListener(listener);
    }

    public void on_confirm(View v) {
        Log.i("MEOW", "http://" + IP.getText() + ":5000");
        new API().execute("http://" + IP.getText() + ":5000");
    }

    class API extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try{
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(500);
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                return buffer.readLine();
            } catch (IOException | ClassCastException e) {
                e.printStackTrace();
                return e.toString();
            }
        }

        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            if (data.equals("Success")) {
                Intent intent = new Intent(LoginActivity.this, Controller.class);
                startActivity(intent);

                File internalStorageDir = getFilesDir();
                File file = new File(internalStorageDir, "data.txt");
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(IP.getText().toString().getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else {
                Log.i("MEOW", data);
                if (!data.contains("ailed to connect")) {
                    Toast.makeText(LoginActivity.this, data, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Не удалось подключиться к серверу", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
