package Client;

import java.io.*;
import java.net.Socket;

public class BasicClientImpl {
    private Socket m_socket;
    private BufferedReader m_is;
    private DataOutputStream m_os;

    public BasicClientImpl(String address, int port){

        try {
            m_socket = new Socket(address, port);
            System.out.println("Client connected");
            m_is = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
            m_os = new DataOutputStream(m_socket.getOutputStream());
            System.out.println("Sending Message to Server");
            m_os.writeUTF("Hello There!\n");
            m_os.flush();

            String line = "";
            while(!line.equals("Over")){
                try {
                    System.out.println("Listening for response from server");
                    line = m_is.readLine();
                    System.out.println("Response from Server Received:");
                    System.out.println(line);

                } catch(IOException i){
                    System.out.println(i);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void closeSocket(){
        try {
            m_is.close();
            m_os.close();
            m_socket.close();
        } catch(Exception e) {
            System.out.println(e.toString());
        }

    }

    public void sendMessageAndPrintResponse(String message){
        try{
            m_os.writeUTF(message);
            String line = "";
            while (line.isEmpty()){
                line = m_is.readLine();
                System.out.println(line);
            }

        } catch(Exception e) {
            System.out.println(e.toString());
        }

    }

}
