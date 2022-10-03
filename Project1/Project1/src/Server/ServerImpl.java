package Server;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class ServerImpl {
    private ServerSocket m_serverSocket = null;
    private Socket m_clientSocket = null;
    private BufferedReader m_inputStream = null;
    private BufferedWriter m_outputStream = null;

    private DatagramSocket m_UDPSocket = null;

    HashMap<String, String> m_map;

    /**
     * Initializes a server socket to listen for client requests.
     * @param port Port number that the server should be listening on.
     */
    private void initServerSocket(int port){
        try {
            m_serverSocket = new ServerSocket(port);
            System.out.println(System.currentTimeMillis() + " Server started");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Will read requests sent by the client line by line, process them, and then respond to each one.
     * Continues in an infinite loop until forcibly cancelled.
     */
    private void serverLoop() {
        System.out.println("Starting server communication loop.");
        while(true) {
            try {
                String line = m_inputStream.readLine();
                String response = processRequest(line);
                if(response == ""){
                    break;
                }
                System.out.println(System.currentTimeMillis() + " Server responding with: " + response);
                m_outputStream.write(response);
                m_outputStream.newLine();
                m_outputStream.flush();

                System.out.println(System.currentTimeMillis() + " Response sent.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Processes a single line of data sent from the client. Depending on the request type (Supported types
     * are "PUT", "GET", "DELETE"), this function will mutate or access its internal key/value store.
     * Generates a response for each operation performed.
     * @param request Processes a single line of data sent from the client. Must start with "PUT", "GET", or "DELETE"
     * @return A response that should be sent to the client. Empty string if the client wishes to terminate the connection.
     */
    private String processRequest(String request) throws IOException {
        System.out.println(System.currentTimeMillis() + " Processing request [" + request + "]...");
        String[] split = request.trim().split("\\s+");

        if(split.length < 1){
            System.out.println(System.currentTimeMillis() + " Error: Empty request body.");
            return "ERROR";
        }

        if(split[0].equals("PUT")){
            if(split.length != 3){
                System.out.println(System.currentTimeMillis() + " Error: PUT request [" + request + "] must have exactly 3 tokens");
                return "ERROR";
            }
            String key = split[1];
            String value = split[2];
            m_map.put(key,value);
            return "PUT SUCCESS";
        } else if(split[0].equals("GET")){
            String value = m_map.get(split[1]);
            if(value == null){
                return "KEY NOT FOUND";
            } else {
                return value;
            }
        } else if (split[0].equals("DELETE")){
            m_map.remove(split[1]);
            return "DELETE SUCCESS";

        } else if (split[0].equals("TERMINATE")){
            if(m_clientSocket != null){
                m_clientSocket.close();
            }
            return "";

        } else {
            System.out.println(System.currentTimeMillis() + " Error: request [" + request + "] is malformed.");
            return "ERROR";
        }
    }

    /**
     * Waits until a client connects to the server's listening port.
     * Once a client does connect, establishes input and output streams that can be used to communicate with the client.
     */
    private void establishConnection() {
        try {
            System.out.println(System.currentTimeMillis() + " Waiting for a client ...");
            m_clientSocket = m_serverSocket.accept();
            System.out.println(System.currentTimeMillis() + " Client accepted");

            m_inputStream = new BufferedReader(new InputStreamReader(m_clientSocket.getInputStream()));
            m_outputStream = new BufferedWriter(new OutputStreamWriter(m_clientSocket.getOutputStream()));
            System.out.println(System.currentTimeMillis() + " Input and output streams established.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes this object's hashmap and populates it with some filler values.
     */
    private void initMap(){
        m_map = new HashMap<>();
        m_map.put("fillerKey1", "fillerValue1");
        m_map.put("fillerKey2", "fillerValue2");
        m_map.put("fillerKey3", "fillerValue3");
        m_map.put("fillerKey4", "fillerValue4");
        m_map.put("fillerKey5", "fillerValue5");
    }

    /**
    * Constructs a UDP socket that this object will listen on.
     */
    private void initUDPSocket(InetAddress listenAddress, int listenPort) throws SocketException {
        m_UDPSocket = new DatagramSocket(listenPort, listenAddress);
        m_UDPSocket.setSoTimeout(1000);
    }

    /**
     * This function handles all UDP logic.
     * It will wait until it receives a UDP packet.
     * Then, with the help of processRequest(), it will parse the UDP packet, execute its instructions, and send
     * an appropriate response back to the client.
     * @return false if a TERMINATE message has been received, true otherwise.
     */
    private Boolean processDatagram() {
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            m_UDPSocket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println(System.currentTimeMillis() + " UDP packet [" + received +"] received from client.");
            String response = processRequest(received);
            if(response.equals("")){
                System.out.println("TERMINATE received. Closing server.");
                m_UDPSocket.close();
                return false;
            }

            byte[] responseBuffer = response.getBytes();
            System.out.println(System.currentTimeMillis() + " Sending UDP Packet [" + response + "]...");
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length, packet.getAddress(), packet.getPort());
            m_UDPSocket.send(responsePacket);
            return true;
            }
        catch (IOException ex) {
            System.out.println(System.currentTimeMillis() + "No message from client. Listening....");
            return true;
        }
    }

    /**
     * Loops until the server receives a TERMINATE message from a client.
     */
    private void UDPLoop() {
        System.out.println(System.currentTimeMillis() + "Starting Server UDP loop.");
        boolean shouldContinue = true;
        while(shouldContinue) {
            shouldContinue = processDatagram();
        }
    }


    /**
     * Constructs a hashmap that the server will use as its key value store.
     * Creates a server socket and listens for a client to connect.
     * Upon connection, responds to all client requests until TERMINATE is sent, at which point the server loop stops.
     * @param port TCP port number for the client to connect to.
     */
    public ServerImpl(int port) {
        initMap();
        initServerSocket(port);
        establishConnection();
        serverLoop();
    }


    /**
     * @param listenAddress IP address or hostname that the server's UDP socket will be associated with.
     * @param listenPort Port that the server will be listening on. It will respond to whatever port the incoming packet came frome.
     * @throws SocketException
     */
    public ServerImpl(InetAddress listenAddress, int listenPort) throws SocketException {
        initMap();
        initUDPSocket(listenAddress,listenPort);
        UDPLoop();
    }


}
