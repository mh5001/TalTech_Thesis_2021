package com.michael.controller;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private WebView graphView;
    private EditText inputForce, inputTime;
    private  Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Spinner availableDoors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graphView = findViewById(R.id.graphView);
        inputForce = findViewById(R.id.inputForce);
        inputTime = findViewById(R.id.inputTime);
        availableDoors = findViewById(R.id.availableDoors);
        availableDoors.setOnItemSelectedListener(this);

        WebSettings graphViewSettings = graphView.getSettings();
        graphViewSettings.setLoadWithOverviewMode(true);
        graphViewSettings.setUseWideViewPort(true);

        setConnectActivity();
    }

    private void updateAvailableDoors(String[] ips) {
        final String[] _ips = ips;
        final Context _this = this;
        availableDoors.post(new Runnable() {
            @Override
            public void run() {
                String[] output = new String[_ips.length + 1];
                output[0] = "None";
                System.arraycopy(_ips, 0, output, 1, _ips.length);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(_this,   android.R.layout.simple_spinner_item, output);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                availableDoors.setAdapter(spinnerArrayAdapter);
            }
        });
    }

    private void updateGraph(String data) {
        String labelString = "\"0:00\", \"1:00\", \"2:00\", \"3:00\", \"4:00\", \"5:00\", \"6:00\", \"7:00\", \"8:00\", \"9:00\", \"10:00\", \"11:00\", \"12:00\", \"13:00\", \"14:00\", \"15:00\", \"16:00\", \"17:00\", \"18:00\", \"19:00\", \"20:00\", \"21:00\", \"22:00\", \"23:00\"";
        final String url = "https://quickchart.io/chart?bkg=black&c={options: {legend: false},type:'line',data:{labels:[" + labelString +"],datasets:[{backgroundColor: 'White',label:'Activity',data:[" + data +"]}]}}";
        graphView.post(new Runnable() {
            @Override
            public void run() {
                graphView.loadUrl(url);
            }
        });
    }

    private void setConnectActivity() {
        Intent intent = new Intent(this, ConnectActivity.class);
        startActivityForResult(intent, 0);
    }

    public void handleUpdateClick (View current) {
        inputForce.post(new Runnable() {
            @Override
            public void run() {
                int force = Integer.parseInt(inputForce.getText().toString());
                int delay = Integer.parseInt(inputTime.getText().toString());
                if (force > 99) {
                    force = 99;
                } else if (force < 1) {
                    force = 1;
                }
                if (delay > 99) {
                    delay = 99;
                } else if (delay < 1) {
                    delay = 1;
                }

                String finalForce = String.format("%d", force);
                String finalDelay = String.format("%d", delay);
                inputForce.setText(finalForce);
                inputTime.setText(finalDelay);
                final String out = String.format("v%d|%d", force, delay);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            outputStream.write(out.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

    }

    public void handleDisconnectClick(View current) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setConnectActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        socketHandshake(data.getExtras().getString("socket"));
    }

    private String readTCPData() {
        byte[] buffer = new byte[4];
        String dataString = "";
        try {
            inputStream.read(buffer, 0, 4);
            String lenString = new String(buffer, StandardCharsets.UTF_8);
            int len = Integer.parseInt(lenString);

            byte[] payload = new byte[len];
            inputStream.read(payload, 0, len);
            dataString = new String(payload, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  dataString;
    }

    private void socketHandshake(String ip) {
        final String _ip = ip;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                socket = null;
                try {
                    socket = new Socket(_ip, 38368);
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();

                    outputStream.write("amController".getBytes());

                    while (true) {
                        String data = readTCPData();
                        if (data.length() > 0) parsedCommand(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parsedCommand(String input) {
        char command = input.charAt(0);
        String argv = input.substring(1);

        if (command == 'i') handleAvailableUpdate(argv);
        if (command == 'u') updateGraph(argv);
        if (command == 'A') updateParameters(argv);
    }

    private void updateParameters(String argv) {
        final String force = argv.substring(0, 2);
        final String delay = argv.substring(2, 4);
        inputForce.post(new Runnable() {
            @Override
            public void run() {
                inputForce.setText(force);
                inputTime.setText(delay);
            }
        });
    }

    private void handleAvailableUpdate(String rawIps) {
        String[] ips = rawIps.split(",");
        updateAvailableDoors(ips);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final String ip = (String)parent.getItemAtPosition(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String out;
                    if (ip.equals("None")) {
                        out = "b ";
                        graphView.post(new Runnable() {
                            @Override
                            public void run() {
                                graphView.loadUrl("about:blank");
                                inputForce.setText("");
                                inputTime.setText("");
                            }
                        });
                    } else {
                        out = String.format("b%s", ip);
                    }
                    outputStream.write(out.getBytes());
                    outputStream.write(("a").getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
