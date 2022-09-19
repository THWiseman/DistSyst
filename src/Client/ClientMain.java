package Client;
import java.io.IOException;

public class ClientMain {

    public static void main(String args[]) throws IOException {

        BasicClientImpl client = new BasicClientImpl("127.0.0.1",10101);
    }
}