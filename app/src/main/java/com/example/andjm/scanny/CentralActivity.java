package com.example.andjm.scanny;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        imageView         = findViewById(R.id.imageView);
        scroll_container  = findViewById(R.id.scrollable_container);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        sysinfo_container = findViewById(R.id.sysinfo_container);

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
                                    sendMessage(REQUEST_BROWSE_FILE); // send message to ask Pi send back the file list

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

    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
        File file = new File(mcoContext.getFilesDir(),"magicscan");
        if(!file.exists()){
            file.mkdir();
        }

        try{
            File gpxfile = new File(file, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
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
                                sysinfo_container.addView(inMessage);
                                checking_board.setText("image is being displayed!");
                                break;
                        }
                    /*if(!splitted[0].equals("filelist")){
                        TextView inMessage = new TextView(CentralActivity.this);
                        inMessage.setText(mConnectedDeviceName +": \n" + readMessage+"\n");
                        scroll_container.removeAllViews();
                        scroll_container.addView(inMessage);

                    }else{
                        piImageList = splitted;


                    }*/
                    }else{


                        /*if(!bufFileName.equals("")) writeFileOnInternalStorage(CentralActivity.this,bufFileName,readMessage);

                        Context ctx = CentralActivity.this ;
                        try {
                            checking_board.setText("lineData: bo m kho chiu lam roi");
                            FileInputStream fileInputStream = ctx.openFileInput(bufFileName);
                            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            String lineData = bufferedReader.readLine();
                            byte[] bt = lineData.getBytes();
                            Bitmap img = BitmapFactory.decodeByteArray(bt, 0, bt.length);

                            sysinfo_container.addView(imageView);
                            sysinfo_container.addView(noti_view);
                            noti_view.setText(lineData);
                            imageView.setImageBitmap(img);
                            checking_board.setText("lineData: "+lineData);
                            Toast.makeText(CentralActivity.this,"stored: "+lineData, Toast.LENGTH_SHORT).show();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        sysinfo_container.removeAllViews();
                        checking_board.setText(readBuf.toString());
                        sysinfo_container.addView(imageView);
                        Bitmap img = BitmapFactory.decodeByteArray(readBuf, 0, readBuf.length);

                        imageView.setImageBitmap(img);
                        TextView imgByte = new TextView(CentralActivity.this);
                        sysinfo_container.addView(imgByte);
                        imgByte.append(readMessage);
                        Toast.makeText(CentralActivity.this,"stored: ", Toast.LENGTH_SHORT).show();

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
