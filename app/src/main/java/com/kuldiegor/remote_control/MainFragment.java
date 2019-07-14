package com.kuldiegor.remote_control;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Context.SENSOR_SERVICE;

public class MainFragment extends Fragment implements SensorEventListener {
    //Блок констант
    private static final String REMOTE_HOST = "192.168.0.12";
    private static final int REMOTE_PORT = 50000;


    //Блок View
    /*private TextView mAccelXTextView;
    private TextView mAccelYTextView;
    private TextView mAccelZTextView;
    private TextView mOrientationXTextView;
    private TextView mOrientationYTextView;
    private TextView mOrientationZTextView;*/
    private EditText mIpAddressEditText;
    private Button mReconnectButton;


    //Блок переменных
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Connection mConnection;
    private String mRemoteHost;


    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle bundle = new Bundle();

        //bundle.putSerializable(ARG_KEY,object);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //mConnection = new Connection();
        //Object object = getArguments().getSerializable(ARG_KEY);
        mRemoteHost = REMOTE_HOST;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        /*mAccelXTextView = view.findViewById(R.id.accel_x_text_view);
        mAccelYTextView = view.findViewById(R.id.accel_y_text_view);
        mAccelZTextView = view.findViewById(R.id.accel_z_text_view);
        mOrientationXTextView = view.findViewById(R.id.orientation_x_text_view);
        mOrientationYTextView = view.findViewById(R.id.orientation_y_text_view);
        mOrientationZTextView = view.findViewById(R.id.orientation_z_text_view);*/
        mIpAddressEditText = view.findViewById(R.id.ip_address_edit_text);
        mReconnectButton = view.findViewById(R.id.reconnect_button);
        mReconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRemoteHost = mIpAddressEditText.getText().toString();
                stopConnection();
                startConnection();
            }
        });
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        return view;
    }

    private void startConnection(){
        mConnection = new Connection();
        mConnection.start();
    }

    private void stopConnection(){
        if (mConnection!=null){
            mConnection.interrupt();
            mConnection = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and accelerometer sensors
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,mGyroscope,SensorManager.SENSOR_DELAY_NORMAL);
        startConnection();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        stopConnection();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            mAccelXTextView.setText(String.valueOf(event.values[0]));
            mAccelYTextView.setText(String.valueOf(event.values[1]));
            mAccelZTextView.setText(String.valueOf(event.values[2]));
        } else */if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE) {
            /*mOrientationXTextView.setText(String.valueOf(event.values[0]));
            mOrientationYTextView.setText(String.valueOf(event.values[1]));
            mOrientationZTextView.setText(String.valueOf(event.values[2]));*/
            if (mConnection!=null) {
                mConnection.mAtomicGyroscopeX.set(Float.floatToIntBits(event.values[0]));
                mConnection.mAtomicGyroscopeZ.set(Float.floatToIntBits(event.values[2]));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class Connection extends Thread {
        public AtomicInteger mAtomicGyroscopeZ;
        public AtomicInteger mAtomicGyroscopeX;

        public Connection(){
            mAtomicGyroscopeX = new AtomicInteger(0);
            mAtomicGyroscopeZ = new AtomicInteger(0);
        }

        @Override
        public void run() {
            System.out.println("olol");
            try (Socket socket = new Socket(mRemoteHost,REMOTE_PORT)) {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                while (socket.isConnected()){
                    dataOutputStream.writeInt(mAtomicGyroscopeX.get());
                    dataOutputStream.writeInt(mAtomicGyroscopeZ.get());
                    dataOutputStream.flush();
                    Thread.sleep(20);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }
}