package jaci.openrio.module.android.net;

import jaci.openrio.delegate.BoundDelegate;
import jaci.openrio.module.android.tile.Tile;
import jaci.openrio.module.android.tile.TileRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

/**
 * The main delegate to connect to the Android Device. This operates on TCP to keep the connection alive
 *
 * DelegateID: TOAST_DroidMain
 *
 * @author Jaci
 */
public class MainDelegate implements BoundDelegate.ConnectionCallback {

    static Vector<Client> clients = new Vector<>();

    @Override
    public void onClientConnect(Socket socket, BoundDelegate boundDelegate) {
        try {
            Client client = new Client();
            client.client = socket;
            client.output = new DataOutputStream(socket.getOutputStream());
            client.input = new DataInputStream(socket.getInputStream());
            clients.add(client);

            for (Tile tile : TileRegistry.getAllTiles()) {
                client.output.write(PacketManager.encodeTile(tile));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(byte[] packet) {
        Iterator<Client> it = clients.iterator();
        while (it.hasNext()) {
            Client client = it.next();
            try {
                if (!client.client.isClosed()) {
                    client.output.write(packet);
                    continue;
                }
            } catch (Exception e) {}
            it.remove();
        }
    }

    public static int clients() {
        return clients.size();
    }

    public static void listenTick() {
        Iterator<Client> it = clients.iterator();
        while (it.hasNext()) {
            Client client = it.next();
            try {
                if (!client.client.isClosed()) {
                    DataInputStream in = client.input;
                    if (in.available() >= 1) {
                        switch (in.readByte()) {
                            case 0x10:
                                String id = PacketManager.decodeTouch(in);
                                TileRegistry.process_touch(id);
                                break;
                            case 0x11:
                                broadcast(PacketManager.encodeProfiler());
                                break;
                        }
                    }
                    continue;
                }
            } catch (Exception e){}
            it.remove();
        }
    }

    public static class Client {

        public DataOutputStream output;
        public DataInputStream input;
        public Socket client;

    }

}
