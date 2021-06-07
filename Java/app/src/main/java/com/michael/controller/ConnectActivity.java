package com.michael.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectActivity extends AppCompatActivity {
    private EditText inputIP;
    private volatile boolean isComplete;
    private volatile Socket socket;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        inputIP = findViewById(R.id.inputIP);

        inputIP.setText("192.168.0.100");
    }

    public void handleConnectClick(View view) {
        final String _ip = inputIP.getText().toString();
        ip = _ip;
        if (_ip.length() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket();
                        Socket client = new Socket();
                        client.connect(new InetSocketAddress(_ip, 38368), 2000);
//                        TODO client get status and then different errors
                        isComplete = true;
                    } catch (IOException e) {
                        isComplete = false;
                    }
                    handleSocketConnect();
                }
            }).start();
        }
    }

    private void displayError(String err) {
        final String _err = err;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                CharSequence text = _err;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

    }

    private void handleSocketConnect() {
        if (!isComplete) displayError("Error connecting to door, make sure your IP is correct");
        else {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent();
            intent.putExtra("socket", ip);
            setResult(0, intent);
            finish();
        }
    }
}
