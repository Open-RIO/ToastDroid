package jaci.openrio.module.android.net;

import jaci.openrio.module.android.ToastDroid;
import jaci.openrio.toast.core.thread.ToastThreadPool;

import java.io.IOException;
import java.net.*;

/**
 * The Multicast Receiver. This thread is responsible for receiving multicasts from the Android Device. This operates
 * on UDP Port 5810
 *
 * @author Jaci
 */
public class MulticastThread extends Thread {

    boolean run;

    @Override
    public void run() {
        try {
            run = true;
            if (ToastDroid.getPreferIPv4())
                System.setProperty("java.net.preferIPv4Stack", "true");

            MulticastSocket socket = new MulticastSocket(5810);
            InetAddress group = InetAddress.getByName("228.5.6.7");
            String hostname = ToastDroid.getInterfaceHostname();
            if (hostname != null && !hostname.equals(""))
                socket.setInterface(InetAddress.getByName(hostname));
            socket.joinGroup(group);

            while (run) {
                try {
                    final DatagramPacket packet;
                    byte[] buf = new byte[19];
                    packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    String received = new String(packet.getData());
                    if (received.trim().equals("TOAST_DROID_REQUEST")) {
                        ToastThreadPool.INSTANCE.addWorker(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    respond(packet);
                                } catch (Exception e) {
                                    ToastDroid.log.error("Error whilst responding to request packet: " + e);
                                    ToastDroid.log.exception(e);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    ToastDroid.log.error("Error whilst reading multicast packet: " + e);
                    ToastDroid.log.exception(e);
                }
            }

            socket.leaveGroup(group);
            socket.close();
        } catch (Exception e) {
            ToastDroid.log.error("Error whilst initializing multicast socket. Aborting ToastDroid: " + e);
            ToastDroid.log.exception(e);
        }
    }

    public void respond(DatagramPacket p) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        byte[] id = PacketManager.encodeRobotID(ToastDroid.currentID);
        DatagramPacket reply = new DatagramPacket(id, id.length, p.getAddress(), 5810);
        socket.send(reply);
    }

}
