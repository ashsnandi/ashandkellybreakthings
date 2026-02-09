package uk.co.compendiumdev.sparkstart;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Port {

    public static boolean inUse(String host, int port) {
        Socket socket = null;
        try {
            SocketAddress address = new InetSocketAddress(host, port);
            socket = new Socket();
            socket.connect(address, 5000);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    return false;
                }
            }
        }
    }
}
