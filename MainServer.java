
import java.net.InetAddress;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.MulticastSocket;
import java.net.Socket;




class SocketDistritos implements Runnable{
    private String m_addr;
    private int m_port;

    public SocketDistritos(String m_addr, int m_port){
        this.m_addr = m_addr;
        this.m_port = m_port;
    }

    public void run(){
        try{
        System.setProperty("java.net.preferIPv4Stack", "true");
        InetAddress address = InetAddress.getByName(m_addr);
        
        
        // Create a buffer of bytes, which will be used to store
        // the incoming bytes containing the information from the server.
        // Since the message is small here, 256 bytes should be enough.
        byte[] buf = new byte[1024];
        
        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        
        try{
            MulticastSocket clientSocket = new MulticastSocket(m_port);
            //Joint the Multicast group.
            clientSocket.joinGroup(address);
     
            while (true) {
                System.out.println("Escuchando en Multicast en la direccion IP:PORT: " + m_addr+":"+m_port);
                // Receive the information and print it.
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                clientSocket.setSoTimeout(10000);
                clientSocket.receive(msgPacket);

                String msg = new String(buf, 0, buf.length);
                System.out.println("Socket Multicast received msg: " + msg);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            }
        } catch (UnknownHostException un){System.out.println(un);}
        
    }

    
}

class SocketCliente implements Runnable{
    private int c_port;
    private String c_addr;

    public SocketCliente(int c_port){
        this.c_port = c_port;
    }
        public void run(){
            byte[] buf = new byte[1024];
            System.setProperty("java.net.preferIPv4Stack", "true");
        
        try {   
                DatagramSocket s = new DatagramSocket(c_port);
                //s.joinGroup(address);
                while(true) {
                    
                    System.out.println("Escuchando en Unicast en IP:PORT: "+c_addr+":"+c_port);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    s.setSoTimeout(10000);
                    s.receive(packet);
                    
                    

                    String recieved = new String(packet.getData());
                    System.out.println("Socket Unicast recieved: "+ recieved);
                    
                    /*
                    line = reader.readLine();
                    if (line == null) break; // end of input
                    line = line + "\n";
                    for(it = outputgroup.iterator(); it.hasNext(); ) {
                        opx = new OutputAction_cliente(line.getBytes(),it.next());
                        outputService.submit(opx);
                        */
                    
                }
                

            }

            catch (IOException e) { System.out.println(e);}

            

        }
        
    }

    



public class MainServer{
    // output thread count
    private final static int OUTPUT_THREADS = 10;

    private final static String MULTICAST_ADDR = "224.0.0.3";
    private final static String CLIENT_ADDR = "192.168.0.17";
    private final static int MULTICAST_PORT = 8888;
    private final static int CLIENT_PORT = 8887;
    // members to support the two server sockets
    private DatagramSocket socket_cliente;
    //private Runnable accepter_cliente;
    private MulticastSocket socket_distritos;
    //private Runnable accepter_distritos;

    // members to hold the two groups of clients
    //private Set<Socket> group_clientes;
    //private Set<Socket> group_distritos;

    // members to support the output thread pool
    //private ExecutorService outputService;

    
    public static void main(String[] args) throws UnknownHostException {
        // Get the address that we are going to connect to.

        SocketDistritos sd = new SocketDistritos(MULTICAST_ADDR, 8888);
        SocketCliente sc = new SocketCliente(8887);

        Thread t_cliente = new Thread(sc);
        Thread t_distrito = new Thread(sd);

        
        t_cliente.start();
        t_distrito.start();

    }
}
