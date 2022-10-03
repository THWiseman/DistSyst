package Client;

import java.io.IOException;
import java.net.InetAddress;

import static java.lang.Integer.parseInt;

/**
 * Driver code for project one. Given a port and internet address of a server, create a client object that will
 * immediately begin sending messages to the server. Can change the commented line to switch between UDP or TCP client.
 */
public class ClientMain {
    public static void main(String[] args) throws IOException {
        String host = args[0];
        InetAddress address = InetAddress.getByName(host);
        int port = parseInt(args[1]);
        System.out.println("Client trying to connect to host " + host + " on port " + port + "...");

        //ClientImpl udpClient = new ClientImpl(address,port, true);
        ClientImpl tcpClient = new ClientImpl(address, port);
    }
}