package realise.SweetBread.mainproject;


import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Controller extends AppCompatActivity {
    String IP;
    SeekBar volume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput("data.txt")));
            IP = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            IP = "0.0.0.0";
        }

        new API().execute("http://" + IP + ":5000/get_vol");

        TabHost tabhost = findViewById(R.id.tabhost);
        tabhost.setup();
        TabHost.TabSpec tabSpec;

        tabSpec = tabhost.newTabSpec("tag1");
        tabSpec.setIndicator("Яркость / громкость");
        tabSpec.setContent(R.id.brivol);
        tabhost.addTab(tabSpec);

        tabSpec = tabhost.newTabSpec("tag2");
        tabSpec.setIndicator("Курсор мыши");
        tabSpec.setContent(R.id.mouse);
        tabhost.addTab(tabSpec);

        final SeekBar brightness = findViewById(R.id.set_brightness);
        volume = findViewById(R.id.set_volume);
        final TextView cur_vol = findViewById(R.id.cur_vol);
        final TextView cur_bri = findViewById(R.id.cur_bri);
        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cur_bri.setText(String.valueOf(brightness.getProgress()) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new API().execute("http://" + IP + ":5000/brightness/" + seekBar.getProgress());
            }
        });
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cur_vol.setText(String.valueOf(volume.getProgress()) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new API().execute("http://" + IP + ":5000/set_vol/" + seekBar.getProgress());
            }
        });
    }

    public void monitor(View v) {
        switch (v.getId()) {
            case R.id.mon_on:
                new API().execute("http://" + IP + ":5000/mon/on");
                break;
            case R.id.mon_off:
                new API().execute("http://" + IP + ":5000/mon/off");
                break;
        }
    }

    public void change_volume(View v) {
        String url = "http://" + IP + ":5000/ch_vol/";

        switch (v.getId()) {
            case R.id.m1:
                new API().execute(url + "-1");
                break;
            case R.id.m2:
                new API().execute(url + "-2");
                break;
            case R.id.m5:
                new API().execute(url + "-5");
                break;
            case R.id.p1:
                new API().execute(url + "1");
                break;
            case R.id.p2:
                new API().execute(url + "2");
                break;
            case R.id.p5:
                new API().execute(url + "5");
                break;
        }
    }

    public void mute(View v) {
        new API().execute("http://" + IP + ":5000/mute");
    }

    public void media_key(View v) {
        String url = "http://" + IP + ":5000/media_key/";
        switch (v.getId()) {
            case R.id.media_play:
                new API().execute(url + "0xB3");
                break;
            case R.id.media_prev:
                new API().execute(url + "0xB1");
                break;
            case R.id.media_next:
                new API().execute(url + "0xB0");
                break;
            case R.id.media_pp:
                new API().execute(url + "0x25");
                break;
            case R.id.media_ff:
                new API().execute(url + "0x27");
                break;
        }
    }

    class API extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(5000);
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                return buffer.readLine();
            } catch (IOException | ClassCastException e) {
                e.printStackTrace();
                Toast.makeText(Controller.this, e.toString(), Toast.LENGTH_LONG).show();
                return e.toString();
            }
        }

        protected void onPostExecute(String data) {
            if (!data.equals("OK")) {
                volume.setProgress(Integer.parseInt(data));
            } else {
                Toast.makeText(Controller.this, "OK", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
