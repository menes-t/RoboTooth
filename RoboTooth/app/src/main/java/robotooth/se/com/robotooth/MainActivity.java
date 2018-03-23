package robotooth.se.com.robotooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_BLUETOOTH = 100;
    private static final int PERMISSION_REQUEST_BLUETOOTH_ADMIN = 101;
    private static final int PERMISSION_REQUEST_CAMERA = 102;
    private static final String SPARKI = "ArcBotics";

    private View mLayout;
    private ListView mListView;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private static OutputStream os;
    //private InputStream is;

    private boolean isBluetoothPermissionGranted;
    private boolean isBluetoothAdminPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //mListView = findViewById(R.id.listView);
        //mListView.setClickable(true);

        //mListView.setOnItemClickListener(new onClickListener());

        askForGeneralBluetoothPermission();

        //listDevices();

        while(!connect()){
            Snackbar.make(mLayout,
                    R.string.bluetooth_couldnot_connect,
                    Snackbar.LENGTH_SHORT).show();
        }

        askForCameraPermission();
    }

    private void askForCameraPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            Snackbar.make(mLayout,
                    R.string.camera_permission_available,
                    Snackbar.LENGTH_SHORT).show();
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.CAMERA)) {
            Snackbar.make(mLayout, R.string.camera_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }
            }).show();
            startCamera();
        } else {
            Snackbar.make(mLayout, R.string.camera_unavailable, Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
    }

    private void startCamera(){
        Intent openCameraActivity = new Intent(this, CameraActivity.class);
        openCameraActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(openCameraActivity);
    }

    private void askForGeneralBluetoothPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mLayout,
                    R.string.bluetooth_permission_available,
                    Snackbar.LENGTH_SHORT).show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                    == PackageManager.PERMISSION_GRANTED) {

                Snackbar.make(mLayout,
                        R.string.bluetooth_permission_available,
                        Snackbar.LENGTH_SHORT).show();
                onBluetooth();
            } else {
                requestBluetoothAdminPermission();
            }
        } else {
            requestBluetoothPermission();
        }
    }

    private void requestBluetoothPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.BLUETOOTH)) {
            Snackbar.make(mLayout, R.string.bluetooth_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH},
                            PERMISSION_REQUEST_BLUETOOTH);
                }
            }).show();
            onBluetooth();
        } else {
            Snackbar.make(mLayout, R.string.bluetooth_unavailable, Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_REQUEST_BLUETOOTH);
        }
    }

    private void requestBluetoothAdminPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.BLUETOOTH_ADMIN)) {
            Snackbar.make(mLayout, R.string.bluetooth_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                            PERMISSION_REQUEST_BLUETOOTH_ADMIN);
                }
            }).show();
            onBluetooth();
        } else {
            Snackbar.make(mLayout, R.string.bluetooth_unavailable, Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_ADMIN}, PERMISSION_REQUEST_BLUETOOTH_ADMIN);
        }
    }

    private List<BluetoothDevice> getPairedDevices(){
        List<BluetoothDevice> bluetoothDevices = new ArrayList<>();

        bluetoothDevices.addAll(bluetoothAdapter.getBondedDevices());

        //Snackbar.make(mLayout, R.string.bluetooth_device_list, Snackbar.LENGTH_SHORT).show();

        //final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, bluetoothDevices);

        //mListView.setAdapter(adapter);

        return bluetoothDevices;
    }

    private void listDevices(){
        List<BluetoothDevice> bluetoothDevices = new ArrayList<>();

        bluetoothDevices.addAll(bluetoothAdapter.getBondedDevices());

        Snackbar.make(mLayout, R.string.bluetooth_device_list, Snackbar.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, bluetoothDevices);

        mListView.setAdapter(adapter);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(mLayout, R.string.bluetooth_permission_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                isBluetoothPermissionGranted = true;
            } else {
                Snackbar.make(mLayout, R.string.bluetooth_permission_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }else if (requestCode == PERMISSION_REQUEST_BLUETOOTH_ADMIN) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(mLayout, R.string.bluetooth_permission_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                isBluetoothAdminPermissionGranted = true;
            } else {
                Snackbar.make(mLayout, R.string.bluetooth_permission_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(mLayout, R.string.camera_permission_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                startCamera();
            } else {
                Snackbar.make(mLayout, R.string.camera_permission_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }

        if(isBluetoothAdminPermissionGranted && isBluetoothPermissionGranted){
            onBluetooth();
        }
    }

    private void onBluetooth(){
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Snackbar.make(mLayout, R.string.on,
                    Snackbar.LENGTH_SHORT)
                    .show();
        } else {
            Snackbar.make(mLayout, R.string.already_on,
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private class onClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            connect();//TODO fix
        }
    }
    /*This method is taken from https://github.com/cristivulpe/sparki-remote-control and changed by me  */

    private boolean connect() {
        boolean result = false;
        if (socket == null) {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                String myDeviceAddress = bluetoothAdapter.getAddress();
                String myDeviceName = bluetoothAdapter.getName();
                int state = bluetoothAdapter.getState();
                String myStatus = "'" + myDeviceName + "': " + myDeviceAddress + "; state: " + state;
                System.out.println("Status: " + myStatus);
                bluetoothAdapter.cancelDiscovery();

                for (BluetoothDevice device : getPairedDevices()) {
                    System.out.println(device.getName() + ":" + device.getAddress());
                    if (SPARKI.equals(device.getName())) {
                        try {
                            // accordingly to this reported issue, a fallback procedure should be in place: http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3
                            try {
                                // connect to the serial channel (details here: http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html)
                                socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                                socket.connect();
                            } catch (Exception e) {
                                System.err.println("Exception connecting to bluetooth. Attempting fallback procedure.");
                                e.printStackTrace();
                                socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                                socket.connect();
                            }

                            System.out.println("Connected");
                            this.os = socket.getOutputStream();

                            //this.is = socket.getInputStream();

                            //LineReader lr = new LineReader();
                            //Thread t = new Thread(lr);
                            //t.start();

                            result = true;
                        } catch (Exception e) {
                            System.err.println("Exception caught!");
                            e.printStackTrace();
                        }

                        break;
                    }
                }
            }
        }
        return result;
    }
    public static void writeCommand(String command) {
        if (os != null) {
            try {
                String commandToWrite = command + "\n";
                os.write(commandToWrite.getBytes());
                os.flush();
                System.out.println("TBT: '" + command + "'");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
