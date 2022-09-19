package Server;
import java.net.*;
import java.io.*;
public class BasicServerImpl {

    private boolean m_listening = false;

    private ServerSocket m_server = null;
    private Socket m_socket = null;
    private BufferedReader m_is = null;
    private DataOutputStream m_os = null;

    public BasicServerImpl(int port){
        try{
            m_server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");
            m_socket = m_server.accept();
            System.out.println("Client acceppted");

            //Create input and output streams for communicating with the client.
            m_is = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
            m_os = new DataOutputStream(m_socket.getOutputStream());

            System.out.println("Streams Created");

            String line = "";
            while(!line.equals("Over")){
                try {
                    line = m_is.readLine();
                    System.out.println("Request received:");
                    System.out.println(line);
                    String response = generateResponse(line);
                    System.out.println("Sending Response and closing");
                    System.out.println(response);
                    m_os.writeUTF(response);
                    m_os.flush();

                } catch(IOException i){
                    System.out.println(i);
                }
            }

        } catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    public void closeSocket() {
        try {
            m_is.close();
            m_os.close();
            m_socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateResponse(String request){
        String returnString = "";
        for (int i = request.length() - 1; i >= 0; i--) {
            char theCharacter = request.charAt(i); //Reference to the current string character, iterating backwards.
            if(Character.isLowerCase(theCharacter)) { //if it's lowercase, make it uppercase
                theCharacter = Character.toUpperCase(theCharacter);
            } else { //if it's uppercase, make it lowercase
                theCharacter = Character.toLowerCase(theCharacter);
            }
            returnString = returnString + theCharacter;
        }
        return returnString + "\n";
    }
}
