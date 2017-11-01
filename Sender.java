import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Sender {
    
    final static String INET_ADDR = "localhost";
    final static String M_ADDR = "224.0.0.3";
    final static int PORT = 8887;
    final static int M_PORT = 8888;


    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        /////////////// UDP /////////////////////////////
        // Get the address that we are going to connect to.
        InetAddress addr = InetAddress.getByName(INET_ADDR);
     
        // Open a new DatagramSocket, which will be used to send the data.
        try (DatagramSocket serverSocket = new DatagramSocket()) {
            for (int i = 0; i < 5; i++) {
                String msg = "Sent message no " + i;

                // Create a packet that will contain the data
                // (in the form of bytes) and send it.
                DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                        msg.getBytes().length, addr, PORT);
                serverSocket.send(msgPacket);
     
                System.out.println("Server sent packet with msg (Unicast 8887): " + msg);
                
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        ///////////////////// MULTICAST //////////////////////
        InetAddress addr2 = InetAddress.getByName(M_ADDR);
        // Open a new DatagramSocket, which will be used to send the data.
        try (DatagramSocket serverSocket = new DatagramSocket()) {
            for (int i = 0; i < 5; i++) {
                String msg = "Sent message no " + i;

                // Create a packet that will contain the data
                // (in the form of bytes) and send it.
                DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                        msg.getBytes().length, addr2, M_PORT);
                serverSocket.send(msgPacket);
     
                System.out.println("Server sent packet with msg (Multicast 8888): " + msg);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        

}
}
