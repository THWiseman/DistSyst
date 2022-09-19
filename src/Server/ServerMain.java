package Server;

import java.io.IOException;

public class ServerMain {
    public static void main(String args[]) throws IOException {

        BasicServerImpl server = new BasicServerImpl(10101);
    }
}
