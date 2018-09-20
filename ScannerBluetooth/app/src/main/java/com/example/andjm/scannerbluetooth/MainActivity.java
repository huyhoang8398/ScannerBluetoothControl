package com.example.andjm.scannerbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    public static final String EXTRA_MESSAGE = "infore.ndm.usth";
    private ArrayAdapter<String> mNewDevicesArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_main);

    }

    public void sendMessage(View view){
        Intent intent       = new Intent(this, DisplayMessageActivity.class);
        TextView background = (TextView) findViewById(R.id.turnon);
        String message      = "Say Hiiii";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);



    }

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public void getBTadapter(View view){

        int REQUEST_ENABLE_BT = 1;

        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            TextView paired_board = findViewById(R.id.board_devices);
            paired_board.setText("Bluetooth is ON");
        }
    }

    public void pairedDevices(View view){
        TextView paired_board = findViewById(R.id.board_devices);
        Set<BluetoothDevice> paired_devices = mBluetoothAdapter.getBondedDevices();
        paired_board.setText("there are "+paired_devices.size()+" paired devices.");
        if (paired_devices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : paired_devices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                paired_board.append("\n");
                paired_board.append(deviceName);
                paired_board.append(deviceHardwareAddress);
                paired_board.append("\n");
            }
        }
    }


    public void discoverDevices(View view){

        TextView paired_board = findViewById(R.id.board_devices);
        paired_board.setText("---");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        Toast.makeText(MainActivity.this,"Starting discover...",Toast.LENGTH_SHORT).show();
        mBluetoothAdapter.startDiscovery();

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.print("onReceive going......");
            TextView found_list = findViewById(R.id.board_devices);
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                System.out.print("Broadcasting.................");
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Toast.makeText(MainActivity.this,deviceName+" "+deviceHardwareAddress,Toast.LENGTH_LONG).show();
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Toast.makeText(MainActivity.this,"Found nothing...",Toast.LENGTH_SHORT).show();
                System.out.print("Found nothing here.....");
            }
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
        // Don't forget to unregister the ACTION_FOUND receiver.
        //unregisterReceiver(mReceiver);
        this.unregisterReceiver(mReceiver);
    }













}
