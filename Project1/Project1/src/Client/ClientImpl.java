package Client;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class ClientImpl {
    private Socket m_socket = null;

    private InetAddress m_serverAddress = null;
    private int m_serverPort = 0;
    private DatagramSocket m_UDPSocket = null;
    private BufferedReader m_inputStream = null;
    private BufferedWriter m_outputStream = null;

    private LinkedList<String> m_messageQueue;

    /**
     * Tries to establish a socket connection with a server at the specified address and port.
     * On success, this will open input and output streams to communicate with the server through the socket.
     * @param address IP address or hostname to connect to.
     * @param port Port number to connect to
     * @throws IOException
     */
    private void establishConnection(InetAddress address, int port) throws IOException {
        System.out.println(System.currentTimeMillis() + " Attempting to connect to server at " + address + " on port " + port + "...");
        m_socket = new Socket(address, port);
        m_socket.setSoTimeout(1000);
        System.out.println(System.currentTimeMillis() + " Socket connection established. Creating data streams...");
        m_inputStream = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
        m_outputStream = new BufferedWriter(new OutputStreamWriter(m_socket.getOutputStream()));
        System.out.println(System.currentTimeMillis() + " Data streams established.");
    }

    /**
     * Sends the provided string to the server through the output stream. Assumes that the socket and output stream
     * have already been established.
     * @param message String to send to the server
     * @throws IOException
     */
    private void sendMessage(String message) throws IOException {
        System.out.println(System.currentTimeMillis() + " Sending to server: " + message);
        m_outputStream.write(message);
        m_outputStream.newLine();
        m_outputStream.flush();
    }

    private void initMessageQueue(){
        m_messageQueue = new LinkedList<>();
        m_messageQueue.addLast("PUT key1 value1");
        m_messageQueue.addLast("PUT key2 value2");
        m_messageQueue.addLast("PUT key3 value3");
        m_messageQueue.addLast("PUT key4 value4");
        m_messageQueue.addLast("PUT key5 value5");

        m_messageQueue.addLast("GET key1");
        m_messageQueue.addLast("DELETE key1");
        m_messageQueue.addLast("GET key1");

        m_messageQueue.addLast("GET fillerKey1");
        m_messageQueue.addLast("DELETE fillerKey1");
        m_messageQueue.addLast("GET fillerKey1");

        m_messageQueue.addLast("DELETE fillerKey2");
        m_messageQueue.addLast("DELETE fillerKey3");
        m_messageQueue.addLast("DELETE fillerKey4");
        m_messageQueue.addLast("DELETE fillerKey5");

        m_messageQueue.addLast("GET fillerKey1");
        m_messageQueue.addLast("GET fillerKey2");
        m_messageQueue.addLast("GET fillerKey3");
        m_messageQueue.addLast("GET key4");
        m_messageQueue.addLast("GET key5");

        m_messageQueue.addLast("TERMINATE");
    }

    /**
     * While this object's message queue is not empty, the client will send the first message in the queue to the server.
     * It will wait up to one second for a response before giving up and sending the next message.
     * When a response is received, it will be printed to the console.
     * @throws IOException
     */
    private void clientLoop() throws IOException {
        System.out.println(System.currentTimeMillis() + "Starting client communication loop.");
        while(!m_messageQueue.isEmpty()) {
                String message = m_messageQueue.poll();
                sendMessage(message);

                try {
                    String serverResponse = m_inputStream.readLine();
                    System.out.println(System.currentTimeMillis() + " Response from Server: " + serverResponse);
                } catch (IOException e){
                    System.out.println(System.currentTimeMillis() + " Error: No response from server, timeout reached. Continuing to send messages.");
                }
        }
    }

    /**
     * While this object's message queue is not empty, the client will send the first message in the queue to the server.
     * It will wait up to one second for a response before giving up and sending the next message.
     * When a response is received, it will be printed to the console.
     * @throws IOException
     */
    private void clientUDPLoop() throws IOException {
        System.out.println(System.currentTimeMillis() + "Starting client UDP loop.");
        while(!m_messageQueue.isEmpty()) {
            String message = m_messageQueue.poll();
            sendDatagram(message);

            String serverResponse = receiveDatagram();
            System.out.println(System.currentTimeMillis() + " Response from Server: " + serverResponse);
        }
    }

    /**
     * This function waits for traffic to be sent to a UDP socket and then returns a String representation of that packet.
     * @return a String representation of whatever data has been sent to the UDP socket.
     */
    private String receiveDatagram() {
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try{
            m_UDPSocket.receive(packet);
            if(packet.getPort() == m_serverPort && packet.getAddress().equals(m_serverAddress)){
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println(System.currentTimeMillis() + " UDP packet [" + received +"] received.");
                return received;
            } else {
                return "";
            }
        } catch(IOException e){
            System.out.println(System.currentTimeMillis() + " Error: No response from server, timeout reached. Continuing to send messages.");
            return "";
        }
    }

    private void sendDatagram(String message) throws IOException {
        byte[] buf = message.getBytes();
        System.out.println(System.currentTimeMillis() + " Sending UDP Packet [" + message + "]...");
        DatagramPacket packet = new DatagramPacket(buf, buf.length, m_serverAddress, m_serverPort);
        m_UDPSocket.send(packet);
    }

    private void initUDPSocket(InetAddress address, int port) throws SocketException, UnknownHostException {
        m_UDPSocket = new DatagramSocket(port, address);
        m_UDPSocket.setSoTimeout(1000);
    }

    /**
     * Populates a message queue with some canned PUT/GET/DELETE requests.
     * Establishes a connection with the server.
     * Upon connection, each message in the queue will be sent and each response from the server will be printed to console.
     * TERMINATE is the last message sent, and it causes the server to shut down.
     * @param address Internet address of the server that the client should connect to.
     * @param port Port number that the server will be listening on.
     * @throws IOException
     */
    public ClientImpl(InetAddress address, int port) throws IOException {
        initMessageQueue();
        establishConnection(address,port);
        clientLoop();
    }

    public ClientImpl(InetAddress address, int port, boolean udp) throws IOException {
        m_serverAddress = address;
        m_serverPort = port;
        initMessageQueue();
        initUDPSocket(address, port+1);
        clientUDPLoop();
    }
}
