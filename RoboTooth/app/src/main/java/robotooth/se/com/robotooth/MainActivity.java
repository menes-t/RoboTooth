package robotooth.se.com.robotooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_BLUETOOTH = 100;
    private static final int PERMISSION_REQUEST_BLUETOOTH_ADMIN = 101;
    private View mLayout;
    private ListView mListView;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isBluetoothPermissionGranted;
    private boolean isBluetoothAdminPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mListView = findViewById(R.id.listView);
        mListView.setClickable(true);

        mListView.setOnItemClickListener(new onClickListener());

        askForGeneralBluetoothPermission();

        listDevices();

        //connect
        //cameraActivity intent
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
    private void connect(){

    }
}
