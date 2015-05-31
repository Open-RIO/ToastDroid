package jaci.openrio.module.android.net;

import android.widget.Toast;
import jaci.openrio.module.android.MainActivity;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastDiscover {

    static boolean running;
    static Thread listenThread;

    public static void broadcast() {
        if (running) {
            Toast toast = Toast.makeText(MainActivity.appContext, "Already Searching!", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        running = true;
                        PacketManager.robotIDs.clear();
                        MainActivity.lock.acquire();
                        DatagramSocket socket = new DatagramSocket();
                        socket.setBroadcast(true);
                        byte[] buffer = "TOAST_DROID_REQUEST".getBytes();
                        InetAddress group = InetAddress.getByName("228.5.6.7");
                        socket.send(new DatagramPacket(buffer, buffer.length, group, 5810));
                        socket.close();
                        MainActivity.lock.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        running = false;
                    }
                }
            }).start();
        }
    }

    public static void listen() {
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(5810);
                    while (true) {
                        byte[] data = new byte[256];
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        socket.receive(packet);
                        processPacket(packet.getData(), packet);
                    }
                } catch (Exception e) {}
            }
        });

        listenThread.start();
    }

    public static void processPacket(byte[] data, DatagramPacket packet) {
        switch (data[0]) {
            case 0x01:
                PacketManager.processRobotID(packet);
                break;
        }
    }

}
