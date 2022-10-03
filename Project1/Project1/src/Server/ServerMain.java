package Server;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import static java.lang.Integer.parseInt;

/**
 * Driver code for project one. Given a command line argument port number, will create a TCP or UDP server.
 * That server will immediately begin listening for traffic on the provided port.
 */
public class ServerMain {
    public static void main(String[] args) throws UnknownHostException, SocketException {
        int port = parseInt(args[0]);
        InetAddress listenAddress = InetAddress.getByName("127.0.0.1");
        System.out.println("Establishing Server to listen on port " + args[0]);

        ServerImpl tcpServer = new ServerImpl(port);
        //ServerImpl udpServer = new ServerImpl(listenAddress, port);
    }
}