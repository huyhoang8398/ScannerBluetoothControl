package com.example.andjm.scanny;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class CentralActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private MenuItem scanButton;
    private LinearLayout content_layout,sysinfo_container;
    private TextView checking_board;
    private TextView noti_view;
    private ImageView imageView;
    private ScrollView scroll_container;
    private final String TAG = "scanny_application";
    private final String EXTRA_DEVICE_ADDRESS = "device_address";
    private final String EXTRA_FILE_NAME = "file_name";
    private final String REQUEST_BROWSE_FILE = "browsefile";
    private static final int REQUEST_CODE = 1;
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private final UUID MY_UUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
    private OutputStream outStream;
    private static InputStream inStream;
    private BluetoothChatService mChatService = null;
    private StringBuffer mOutStringBuffer;
    private String mConnectedDeviceName = null;
    private String dialogMessage = "hello it is me";
    private boolean isAdmin ;
    private boolean isConnected = false;
    private boolean isTransferList = false;
    private String[] piImageList;
    private String bufFileName;
    private TextView info_time, info_img_JPG, info_img_PNG, info_storage, info_dpi, info_crontab;
    private final String imageText = "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                      ";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central);

        String user_type = getIntent().getStringExtra("usertype");
        if(user_type.equals("admin")) isAdmin = true;
        else isAdmin = false;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        noti_view         = findViewById(R.id.noti_view);
        scanButton        = findViewById(R.id.nav_bluetooth);
        mDrawerLayout     = findViewById(R.id.drawer_layout);
        content_layout    = findViewById(R.id.content_layout);
        checking_board    = findViewById(R.id.noti_view);
        //imageView         = findViewById(R.id.imageView);
        scroll_container  = findViewById(R.id.scrollable_container);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        sysinfo_container = findViewById(R.id.sysinfo_container);

        info_crontab      = findViewById(R.id.info_crontab);
        info_dpi          = findViewById(R.id.info_dpi);
        info_img_JPG      = findViewById(R.id.info_img_JPG);
        info_img_PNG      = findViewById(R.id.info_img_PNG);
        info_storage      = findViewById(R.id.info_storage);
        info_time         = findViewById(R.id.info_time);

        sysinfo_container.removeAllViews();
        ImageView imageView = new ImageView(getApplicationContext());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        String path = Environment.getExternalStorageDirectory() + "";

        // ---------- TRY TO READ IMAGE FROM BINARY ---------- //
        /*InputStream inputStream = getResources().openRawResource(R.raw.binaryImage);
        BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream));
        String eachline = null;
        try {
            eachline = bufferedReader.readLine();
            while (eachline != null) {
                // `the words in the file are separated by space`, so to get each words
                String[] words = eachline.split(" ");
                eachline = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        isStoragePermissionGranted();


        if(isAdmin) noti_view.setText("You are ADMIN");
        else noti_view.setText("You are GUESS");

        if(mBluetoothAdapter == null){
            Toast.makeText(CentralActivity.this,"Bluetooth is not supported",
                    Toast.LENGTH_SHORT).show();
        }

        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        switch (menuItem.getItemId()){
                            case(R.id.nav_bluetooth):
                                if (!mBluetoothAdapter.isEnabled()) {
                                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                                    // Otherwise, setup the chat session
                                }
                                Intent mIntent = new Intent(CentralActivity.this, DeviceListActivity.class);
                                CentralActivity.this.startActivityForResult(mIntent, REQUEST_CODE);
                                break;

                            case(R.id.nav_files):
                                if(isConnected){
                                    //sendMessage(REQUEST_BROWSE_FILE); // send message to ask Pi send back the file list
                                    sendMessage("browsefile");
                                    Bundle b = new Bundle();
                                    // piImageList is changed in handleMessage();
                                    b.putStringArray("imagelist", piImageList);
                                    if(isTransferList) {

                                        Intent mmIntent = new Intent(CentralActivity.this, FileListActivity.class);
                                        mmIntent.putExtras(b);
                                        CentralActivity.this.startActivityForResult(mmIntent, REQUEST_CODE);
                                        break;
                                    }
                                }else{
                                    Bundle b = new Bundle();
                                    piImageList = new String[] {
                                            "Files are not ready, try again!"
                                    };
                                    b.putStringArray("imagelist", piImageList);
                                    Intent mmIntent = new Intent(CentralActivity.this, FileListActivity.class);
                                    mmIntent.putExtras(b);
                                    CentralActivity.this.startActivityForResult(mmIntent, REQUEST_CODE);
                                    break;
                                }
                        }

                        return true;
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }

        //sysinfo_container.removeAllViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(mBluetoothAdapter.isEnabled()) {
            if (resultCode == RESULT_OK && data!=null) {
                String address = data.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                if(address!=null){
                    BluetoothDevice newDevice = mBluetoothAdapter.getRemoteDevice(address);
                    mChatService.connect(newDevice, true);
                    Toast.makeText(CentralActivity.this, "Connecting to " + data.getExtras().getString(EXTRA_DEVICE_ADDRESS),
                            Toast.LENGTH_SHORT).show();
                }

                String fileName = data.getExtras().getString(EXTRA_FILE_NAME);
                if(fileName!=null){
                    Toast.makeText(CentralActivity.this, "File name: " + fileName,
                            Toast.LENGTH_SHORT).show();
                    bufFileName = fileName;
                    if(isConnected){
                        sendMessage("transfer "+fileName);
                    }
                }

            }
        }
    }

    //BluetoothDevice Bluedevice = new BluetoothDevice("adasdasd");

    private void setupChat(){
        Log.d(TAG, "setupChat()");
        mChatService = new BluetoothChatService(this, mHandler);
        mOutStringBuffer = new StringBuffer("");
    }

    private void updateStatus(String status){
        checking_board.setText(status);
    }

    protected void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    public void querryButtonPressed(View view){
        sysinfo_container.removeAllViews();
        String command = "checkinfo_123456";
        sendMessage(command);

        //Bitmap bm = BitmapFactory.decodeResource(getResources(), R.raw.white);
        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //bm.compress(Bitmap.CompressFormat.JPEG, 100,baos); //bm is the bitmap object
        //byte[] b = baos.toByteArray();
        //Toast.makeText(getBaseContext(), String.valueOf(b.length), Toast.LENGTH_SHORT).show();
        //mChatService.write(b);

        //sysinfo_container.removeAllViews();
        //byte[] decodedString = Base64.decode(imageText,Base64.DEFAULT);
        //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);


        //ImageView imgByte = new ImageView(CentralActivity.this);
        //sysinfo_container.addView(imgByte);
        //imgByte.setImageBitmap(decodedByte);

    }

    public void crontabButtonPressed(View view){
        String usertype;
        if(isAdmin) usertype = "admin";
        else  usertype = "guess";
        Bundle bundle = new Bundle();
        bundle.putString("usertype",usertype);

        FragmentManager fragmentManager = getSupportFragmentManager();
        CrontabFragment crontabFragment = CrontabFragment.newInstance("infore");
        crontabFragment.setArguments(bundle);
        crontabFragment.show(fragmentManager, null);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }



    private final Handler mHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            updateStatus("CONNECTED SUCCESSFULLY");
                            isConnected = true;

                            //content_layout.removeAllViews();
                        case BluetoothChatService.STATE_CONNECTING:
                            //setStatus(R.string.title_connecting);
                            mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                            updateStatus("CONNECTING TO "+mConnectedDeviceName);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                            updateStatus("LISTENING STATE");
                        case BluetoothChatService.STATE_NONE:
                            updateStatus("FREE MODE");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    TextView outMessage = new TextView(CentralActivity.this);
                    break;
                case Constants.MESSAGE_READ:
                    //sysinfo_container.removeAllViews();
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //readMessage = readMessage.split("'")[1];

                    String[] splitted  = readMessage.split(" ");
                    if (splitted.length > 1) {
                        String[] newList = new String[splitted.length - 1];
                        for (int i = 0; i < newList.length; i++) {
                            newList[i] = splitted[i + 1];
                        }

                        switch (splitted[0]) {
                            case ("filelist"):
                                isTransferList = true;
                                piImageList = newList;
                                break;
                            case ("checkinfo"):
                                List<String> list = Arrays.asList(newList);
                                String joinedMessage = String.join(" ", list);
                                TextView inMessage = new TextView(CentralActivity.this);
                                inMessage.setText(mConnectedDeviceName + ": \n" + joinedMessage + "\n");

                                String lines[] = joinedMessage.split("\\r?\\n");
                                info_time.setText(lines[0]);
                                info_img_JPG.setText(lines[1]);
                                info_img_PNG.setText(lines[2]);
                                info_storage.setText(lines[3]);
                                info_dpi.setText(lines[4]);
                                info_crontab.setText(lines[5]);

                                sysinfo_container.addView(info_time);
                                sysinfo_container.addView(info_img_JPG);
                                sysinfo_container.addView(info_img_PNG);
                                sysinfo_container.addView(info_storage);
                                sysinfo_container.addView(info_dpi);
                                sysinfo_container.addView(info_crontab);

                                //sysinfo_container.addView(inMessage);
                                checking_board.setText("Information:");
                                break;
                        }

                    }else{
                        //bufFileName
                        sysinfo_container.removeAllViews();
                        updateStatus("Size of image transferred: "+Integer.toString(readBuf.length));
                        ImageView imgView = new ImageView(CentralActivity.this);
                        sysinfo_container.addView(imgView);
                        String filePath = "/storage/emulated/0/bluetooth/"+bufFileName;
                        updateStatus("file transferred: "+filePath);
                        File imgFile = new File(filePath);
                        //Bitmap bmp = BitmapFactory.decodeFile(filePath);
                        //imgView.setImageBitmap(bmp);
                        if( isStoragePermissionGranted())
                        {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            imgView.setImageBitmap(myBitmap);
                            updateStatus("file exists");
                        }else updateStatus("file doesnt not exist!!");



                    }
                    break;

                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != CentralActivity.this) {
                        Toast.makeText(CentralActivity.this, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != CentralActivity.this) {
                        Toast.makeText(CentralActivity.this, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        // And stop the BluetoothChatService.
        if (mChatService != null) {
            mChatService.stop();
        }
    }




}
