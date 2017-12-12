package com.dsm.bluetoothsim;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;


public class SocketThread extends Thread {

    private final String TAG = SocketThread.class.getSimpleName();
    private BTDeviceApi btDeviceApi = BTDeviceApi.getInstance();

    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_MTU_CHANGED = 4;

    private static BluetoothSocket bluetoothSocket;
    private static int connectionState = STATE_CONNECTING;

    public SocketThread(BluetoothSocket btSocket) {
        bluetoothSocket = btSocket;
    }

    public static BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public static int getConnectionState() {
        return connectionState;
    }

    public static void close() {
        if (bluetoothSocket != null)
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        connectionState = STATE_DISCONNECTED;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[0x800];
        int n;

        try (BluetoothSocket bluetoothSocket = SocketThread.bluetoothSocket) {
            //mBluetoothAdapter.cancelDiscovery();
            connectionState = STATE_CONNECTING;
            if (bluetoothSocket != null) {
                bluetoothSocket.connect();
                connectionState = STATE_CONNECTED;
                Log.d(TAG, "calling bluetooth_connection_change_notify()");
                btDeviceApi.bluetooth_connection_change_notify();
                while ((n = bluetoothSocket.getInputStream().read(buffer)) > 0) {
                    byte[] bytes = new byte[n];
                    System.arraycopy(buffer, 0, bytes, 0, n);
                    BTDeviceApi.printHexString("spp recv:", bytes);
                    BTDeviceApi.getInstance().ble_notify_data((byte) 64, bytes);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        connectionState = STATE_DISCONNECTED;
        Log.d(TAG, "calling bluetooth_connection_change_notify()");
        btDeviceApi.bluetooth_connection_change_notify();
    }

}