
import java.net.InetAddress;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.MulticastSocket;
import java.net.Socket;





class SocketDistritos extends MainServer implements Runnable{
    private String m_addr;
    private int m_port;

    public SocketDistritos(String m_addr, int m_port){
        this.m_addr = m_addr;
        this.m_port = m_port;
    }

    public void run(){
        try{
        System.setProperty("java.net.preferIPv4Stack", "true");
        
        
        
        // Create a buffer of bytes, which will be used to store
        // the incoming bytes containing the information from the server.
        // Since the message is small here, 256 bytes should be enough.
        byte[] buf = new byte[1024];
        
        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        
        try{
            MulticastSocket mSocket = new MulticastSocket(m_port);
            InetAddress group = InetAddress.getByName(m_addr);
            //Join the Multicast group.
            mSocket.joinGroup(group);
            
            //checkeamos a los distritos que esten disponibles
            String msg = "CheckDistritos";
            DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                    msg.getBytes().length, group, m_port);
             mSocket.send(msgPacket);

            //En los distritos hacer un pequeño sleep para poder darle tiempo a este thread de comenzar a leer
            while (true) {
                System.out.println("Escuchando en Multicast en la direccion IP:PORT: " + m_addr+":"+m_port);
                // Receive the information and print it.
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                clientSocket.setSoTimeout(10000);
                clientSocket.receive(msgPacket);

                //Por diferencias de tamaño de buffer hay que reasignar la info recibida
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                String msg_response = new String(data);

                if(!msg_response.contains("/")){
                    System.out.println("Se recibio un mensaje con formato incorrecto");
                    continue;        
                }

                String sep = "[/]"; //caracter separador
                String[] sockets_distrito = msg_response.split(sep);// El resultado es: [IP, PORT]
                //Se esperan respuestas del tipo: Nombre/IPMulti;PortMulti;IPPersonal;PortPersonal    
                // Cuando se anuncia la creacion de un nuevo distrito, hay que agregarlo
                if(socket_distritos[0].equals("New")){
                    if(!map_distritos.containsKey(socket_distritos[0])){ //Si no se encuentra el distrito ya en el diccionario...
                    map_distritos.put(socket_distritos[1],socket_distritos[2]);
                    switch_distritos.put(socket_distritos[1],index_distritos);
                    index_distritos+=1;
                    }    
                }
                //Se creo un nuevo titan en un distrito
                if(socket_distritos[0].equals("NewTitan")){
                    if(!map_distritos.containsKey(socket_distritos[0])){ //Si no se encuentra el distrito ya en el diccionario...
                        System.out.println("Se creo un titan en un distrito desconocido!");
                        continue;
                    }    
                    switch_distritos.put()
                }

                //////// DEMAS FUNCIONES DE SERVIDOR A PEDIDO DE DISTRITOS
                
                

                //Esperamos
                System.out.println("Socket Multicast recieved msg: " + msg);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            }
        } catch (UnknownHostException un){System.out.println(un);}  
    }
   
}

class SocketCliente extends MainServer implements Runnable{
    private int c_port;

    public SocketCliente(int c_port){
        this.c_port = c_port;
        //this.map_distritos = map_distritos;

    }
    public void run(){
        byte[] buf = new byte[1024];
        System.setProperty("java.net.preferIPv4Stack", "true");
        
        try {   
             DatagramSocket s = new DatagramSocket(c_port);
            //s.joinGroup(address);
            while(true) {
                
                System.out.println("Escuchando en Unicast en PORT: "+c_port);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                s.setSoTimeout(10000);
                s.receive(packet);                    
                
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                String msg = new String(data);
                // lectura de lo que nos mandan
                
                switch (switch_distritos.get(msg)) {
                //logica de respuesta
                    case 1:
                        System.out.println("Se eligio a Trost");
                        Boolean auth;
                        auth = VerifyAuth(packet.getAddress(), "trost");
                        // Si se da autorizacion
                        if(auth){
                            s.send(InfoDistrito(packet, "trost"));
                        }
                        else{
                            System.out.println("Permiso de conexion a Trost denegado");
                        }
                        break;
                    default:
                        System.out.println("Default");
                        break;


                }

                System.out.println("Socket Unicast recieved: "+ msg);
            }
        }
        catch (IOException e) { e.printStackTrace();}
    }

}

    



public class MainServer{
    // output thread count
    private final static int OUTPUT_THREADS = 10;

    private final static String MULTICAST_ADDR = "224.0.0.3";
    private final static String CLIENT_ADDR = "192.168.0.17";
    private final static int MULTICAST_PORT = 8888;
    private final static int CLIENT_PORT = 8887;

    protected int index_distritos = 1;
    protected static Map<String,String> map_distritos = new HashMap<String,String>();
    protected static Map<String,Integer> switch_distritos = new HashMap<String,Integer>();
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
        map_distritos.put("trost","192.168.0.17;8007;224.0.0.4;8888");
        switch_distritos.put("trost",1);
        SocketDistritos sd = new SocketDistritos(MULTICAST_ADDR, 8888);
        SocketCliente sc = new SocketCliente(8887);

        Thread t_cliente = new Thread(sc);
        Thread t_distrito = new Thread(sd);

        
        t_cliente.start();
        t_distrito.start();

    }

    //Retorna un packete datagrama listo para ser enviado con la informacion del distrito
    protected static DatagramPacket InfoDistrito(DatagramPacket packet, String selected_distrito) throws UnknownHostException{
        System.out.println("Respuesta a "+packet.getAddress().toString()+" por "+selected_distrito);
        
        //Responder a socket con informacion de adonde conectarse
        String msg = map_distritos.get(selected_distrito).toString(); //recuperamos el par IP/PORT del diccionario
        String sep = "[/]"; //caracter separador
        String[] socket = msg.split(sep);// El resultado es: [IP, PORT]
        int puerto = Integer.parseInt(socket[1]);
        InetAddress ip = InetAddress.getByName(socket[0]);
        DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, ip, puerto);
        return(msgPacket);
    }


    //Pregunta si se le quiere dar acceso a cierto usuario sobre las credenciales de un distrito
    protected static Boolean VerifyAuth(InetAddress addr, String nombre_distrito){
        

        System.out.println("Dar autorizacion a "+addr.toString()+" por distrito "+nombre_distrito+"?");
        System.out.println("1.- SI \n2.- NO");
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input = br.readLine();

            switch(input){
                case "1":
                    return(true);
                case "2":
                    return(false);
                default:
                    System.out.println("Opcion no valida.\n");
                    return(false);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return(false);
    }

}
