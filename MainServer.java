
import java.net.InetAddress;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;



public class MainServer{
    // output thread count
    private final static int OUTPUT_THREADS = 10;

    
    public static void main(String[] args) throws UnknownHostException {
        // Get the address that we are going to connect to.
        SuperSocket.IniciarServidor();
        //map_distritos.put("trost","192.168.0.17;8007;224.0.0.4;8888");
        //switch_distritos.put("trost",index_distritos);
        //index_distritos+=1;
        SocketDistritos sd = new SocketDistritos();
        SocketCliente sc = new SocketCliente();

        Thread t_cliente = new Thread(sc);
        Thread t_distrito = new Thread(sd);

        
        t_cliente.start();
        t_distrito.start();

    }

}


///////////////////////// CLASES DE SOCKETS /////////////////////////////////

abstract class SuperSocket implements Runnable{
    protected  static String MULTICAST_ADDR;
    protected  static String CLIENT_ADDR;
    protected  static int MULTICAST_PORT;
    protected  static int CLIENT_PORT;

    protected static int index_distritos = 1;
    protected static Map<String,String> map_distritos = new HashMap<String,String>();
    protected static Map<String,Integer> switch_distritos = new HashMap<String,Integer>();
    protected static List<Titan> lista_titanes = new ArrayList<Titan>(); // Lleva un registro de los titantes creados
    // members to support the two server sockets
    protected static DatagramSocket socket_cliente;
    //private Runnable accepter_cliente;
    protected static MulticastSocket socket_distritos;
    //private Runnable accepter_distritos;

    // members to hold the two groups of clients
    //private Set<Socket> group_clientes;
    //private Set<Socket> group_distritos;

    // members to support the output thread pool
    //private ExecutorService outputService;


    public SuperSocket(){
       // IniciarServidor();
    }
    public static void IniciarServidor(){
        System.setProperty("java.net.preferIPv4Stack", "true");
        String input;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try{
            
            System.out.println("IP Multicast: ");
            input = br.readLine();
            MULTICAST_ADDR = input;


            System.out.println("Puerto Multicast: ");
            input = br.readLine();
            MULTICAST_PORT = Integer.parseInt(input);

            System.out.println("IP Peticiones: ");
            input = br.readLine();
            CLIENT_ADDR = input;

            System.out.println("Puerto Peticiones: ");
            input = br.readLine();
            CLIENT_PORT = Integer.parseInt(input);

            

            //Ligamos el socket personal a un socket local especifico
            //InetAddress client_ip = InetAddress.getByName(CLIENT_ADDR);
            socket_cliente = new DatagramSocket(CLIENT_PORT);

            //Enviamos un mensaje avisando de nuestra creacion
            socket_distritos = new MulticastSocket(MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDR);
            socket_distritos.joinGroup(group);
            
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    //Retorna un packete datagrama listo para ser enviado con la informacion del distrito
    protected static String InfoDistrito(DatagramPacket packet, String selected_distrito) throws UnknownHostException{
        System.out.println("Respuesta a "+packet.getAddress().toString()+" por "+selected_distrito);
        
        //Responder a socket con informacion de adonde conectarse
        String msg = map_distritos.get(selected_distrito).toString(); //recuperamos el par IP/PORT del diccionario
        /*String sep = "[/]"; //caracter separador
        String[] socket = msg.split(sep);// El resultado es: [IP, PORT]
        System.out.println(socket[0]);
        System.out.println(msg.split(sep));
        int puerto = Integer.parseInt(socket[1]);
        InetAddress ip = InetAddress.getByName(socket[0]);*/
        //DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length);
        return(msg);
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




class SocketDistritos extends SuperSocket implements Runnable{

    public void run(){
        System.setProperty("java.net.preferIPv4Stack", "true");
        // Create a buffer of bytes, which will be used to store
        // the incoming bytes containing the information from the server.
        // Since the message is small here, 256 bytes should be enough.
        byte[] buf = new byte[1024];
        
        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        
        try{
            //MulticastSocket mSocket = new MulticastSocket(m_port);
            //InetAddress group = InetAddress.getByName(m_addr);
            //Join the Multicast group.
            //socket_distritos.joinGroup(group);
            
            //checkeamos a los distritos que esten disponibles
            String msg = "CheckDistritos";
            DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                    msg.getBytes().length, InetAddress.getByName(MULTICAST_ADDR), MULTICAST_PORT);
             socket_distritos.send(msgPacket);

            //En los distritos hacer un pequeño sleep para poder darle tiempo a este thread de comenzar a leer
            while (true) {
                System.out.println("Escuchando en Multicast en la direccion IP:PORT: " + MULTICAST_ADDR+":"+MULTICAST_PORT);
                // Receive the information and print it.
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket_distritos.setSoTimeout(10500);
                socket_distritos.receive(packet);

                //Por diferencias de tamaño de buffer hay que reasignar la info recibida
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                String msg_response = new String(data);
                System.out.println(msg_response);
                if((msg_response.contains("/") == false) && (msg_response.equals(msg) == false)){
                    System.out.println("Se recibio un mensaje con formato incorrecto");
                    continue;        }

                String sep = "[/]"; //caracter separador
                String[] sockets_distritos = msg_response.split(sep);// El resultado es: [IP, PORT]
                //Se esperan respuestas del tipo: Comando/Nombre/IPMulti;PortMulti;IPPersonal;PortPersonal    
                // Cuando se anuncia la creacion de un nuevo distrito, hay que agregarlo
                if(sockets_distritos[0].equals("NewDistrito")){
                    if(map_distritos.containsKey(sockets_distritos[1]) == false){ //Si no se encuentra el distrito ya en el diccionario...
                    map_distritos.put(sockets_distritos[1],sockets_distritos[2]);
                    switch_distritos.put(sockets_distritos[1],index_distritos);
                    index_distritos+=1;
                    System.out.println("Se creo un nuevo distrito: "+sockets_distritos[1]);
                    }
                }
                

                //Se crea un nuevo titan en un distrito
                
                //if(sockets_distritos[0].equals("NewTitan")){
                    /*if(!map_distritos.containsKey(sockets_distritos[0])){ //Si no se encuentra el distrito ya en el diccionario...
                        System.out.println("Se creo un titan en un distrito desconocido!");
                        continue;
                    }*/    
                    //crear un nuevo titan con los parametros dados, por defecto: NewTitan/nombre;tipo;distrito
                    // si se creo, entonces devolver 
                  //  String sep2 = "[;]"; //caracter separador
                  //  String[] params_titan = sockets_distritos[1].split(sep2);// El resultado es: [Nombre,tipo,distrito]
                   // Titan nuevo_titan = new Titan(params_titan[0],params_titan[1],params_titan[2]);
                   // lista_titanes.add(nuevo_titan);

                   // System.out.println("NUEVO TITAN: \n**********\nNombre: "+lista_titanes.get(0).GetNombre()+"\nTipo: "+lista_titanes.get(0).GetTipo()+"\nEn el distrito: "+lista_titanes.get(0).GetDistrito()+"\n**********");
                //}

                //////// DEMAS FUNCIONES DE SERVIDOR A PEDIDO DE DISTRITOS
                
                

                
                System.out.println("Socket Multicast recieved msg: " + msg_response);}}
                catch (IOException ex) {
                    ex.printStackTrace();
                    }

    }
        
        
   
}



class SocketCliente extends SuperSocket implements Runnable{
    
    public void run(){
        byte[] buf = new byte[1024];
        System.setProperty("java.net.preferIPv4Stack", "true");
        String resp_msg;
        String msg;
        
        try {   

            //s.joinGroup(address);
            while(true) {
                
                System.out.println("Escuchando en Unicast en PORT: "+CLIENT_PORT);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                DatagramPacket notice_packet = new DatagramPacket(buf,buf.length);
                DatagramPacket resp_packet = new DatagramPacket(buf,buf.length);
                socket_cliente.setSoTimeout(10500);
                socket_cliente.receive(packet);                    
                
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                msg = new String(data);
                // lectura de lo que nos mandan
                System.out.println(msg);
                System.out.println(switch_distritos.get(msg));
                System.out.println(switch_distritos.entrySet());

                String sep = "[/]"; //caracter separador
                String[] comando_cliente = msg.split(sep);// El resultado es: [IP, PORT]

                //Nuevo cliente, hay que enviarle los comandos
                
                if(comando_cliente[0].equals("NewClient")){
                    resp_msg = "Consola\n(1) Listar Distritos\n(2) Conectarse a un Distrito\n(3) Nuevo Titan";
                    resp_packet = new DatagramPacket(resp_msg.getBytes(), resp_msg.length(), packet.getAddress(), packet.getPort());
                    socket_cliente.send(resp_packet);
                
                }
                if(comando_cliente[0].equals("1")){//Formato: ComandoCliente
                    resp_msg = map_distritos.keySet().toString(); //recuperamos todos los distritos que tengamos guardados
                    resp_packet = new DatagramPacket(resp_msg.getBytes(),resp_msg.length(),packet.getAddress(),packet.getPort());
                    socket_cliente.send(resp_packet);
                }
                if(comando_cliente[0].equals("2")){//formato: ComandoCliente/NombreDistrito
                    if(map_distritos.get(comando_cliente[1]) != null){
                    System.out.println("Se eligio a "+comando_cliente[1]);
                    Boolean auth = VerifyAuth(packet.getAddress(), comando_cliente[1]);
                    
                    // Si se da autorizacion
                    if(auth){
                        String resp = InfoDistrito(packet, comando_cliente[1]);
                        System.out.println(resp);
                        DatagramPacket respPacket = new DatagramPacket(resp.getBytes(),
                            resp.length(), packet.getAddress(), packet.getPort());
                        socket_cliente.send(respPacket);
                        
                    } else{
                        System.out.println("Permiso de conexion a" +comando_cliente[1]+" denegado");
                    }    
                    }
                }
                if(comando_cliente[0].equals("3")){//formato: Comando/Nombre;Tipo;Distrito
                    String new_sep = "[;]";
                    String[] info_titan = comando_cliente[1].split(sep);
                    String nombre_titan = info_titan[0];
                    String tipo_titan = info_titan[1];
                    String distrito_titan = info_titan[2];
                    Titan nuevo_titan = new Titan(nombre_titan,tipo_titan,distrito_titan);
                    lista_titanes.add(nuevo_titan);
                    
                    //Avisamos a los distritos la creacion del titan para que el distrito correspondiente lo agregue a su lista
                    resp_msg = "NewTitan/"+nuevo_titan.GetId()+";"+nuevo_titan.GetNombre()+";"+nuevo_titan.GetTipo()+";"+nuevo_titan.GetDistrito();
                    notice_packet = new DatagramPacket(resp_msg.getBytes(),resp_msg.length(),InetAddress.getByName(MULTICAST_ADDR),MULTICAST_PORT);
                    socket_distritos.send(notice_packet);
                    System.out.println("NUEVO TITAN: \n**********\nNombre: "+nombre_titan+"\nTipo: "+tipo_titan+"\nEn el distrito: "+distrito_titan+"\n**********");


                }








                //int choice = switch_distritos.get(msg);
                
                
                
                System.out.println("Socket Unicast recieved: "+ msg);
            }

            
        }catch (IOException e) { e.printStackTrace();}
    }
        
}



    




/////////////////////////////// CLASE DE TITAN Y DISTRITO ///////////////////////

class Titan{
    private int id;
    private String nombre;
    private String tipo;
    private String distrito;
    private String estado; //capturado, asesinado, vivo
    private static AtomicInteger ID_GENERATOR = new AtomicInteger(); //para autogenerar los id

    // Constructor
    public Titan(String titan_nombre, String titan_tipo, String titan_distrito){
        this.SetId(ID_GENERATOR.getAndIncrement());
        this.SetNombre(titan_nombre);
        this.SetTipo(titan_tipo);
        this.SetDistrito(titan_distrito);
        this.SetEstado("vivo");

    }

    /// GETTERS /////
    public int GetId(){
        return(this.id);
    }
    public String GetNombre(){
        return(this.nombre);
    }
    public String GetTipo(){
        return(this.tipo);
    }
    public String GetDistrito(){
        return(this.distrito);
    }
    public String GetEstado(){
        return(this.estado);
    }

    /// SETTERS ///
    private void SetId(int id){
        this.id = id;
    }
    private void SetNombre(String titan_nombre){
        this.nombre = titan_nombre;
    }
    private void SetDistrito(String titan_distrito){
        this.distrito = titan_distrito;
    }
    private void SetTipo(String titan_tipo){
        this.tipo = titan_tipo;
    }
    private void SetEstado(String titan_estado){
        this.estado = titan_estado;
    }
}







