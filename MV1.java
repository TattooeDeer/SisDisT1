
import java.net.InetAddress;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class MV1 {
    
    

    public static void main(String[] args) throws UnknownHostException {
        // Al levantar el servidor hay que pedir la informacion de ip y puerto
        System.setProperty("java.net.preferIPv4Stack", "true");
        SuperSocket.IniciarDistrito();

        
        //InetAddress address = InetAddress.getByName(P_ADDR);
        //SocketDistritos sd = new SocketDistritos(M_ADDR, M_PORT);
        SocketCliente sc = new SocketCliente();
        SocketDistritos sd = new SocketDistritos();

        Thread t_cliente = new Thread(sc);
        Thread t_distrito = new Thread(sd);

        
        t_cliente.start();
        t_distrito.start();
    }
}


    ////////////////////// SOCKET CLIENTE ///////////////////

abstract class SuperSocket implements Runnable{
    protected static String M_ADDR; //"224.0.0.3"
    protected static int M_PORT; // 8888
    protected static String P_ADDR; // ?
    protected static int P_PORT; // 8887
    protected static final int TIMEOUT = 10000; // cantidad de milisegundos que va a estar el puerto abierto, para evitar que quede abierto
                                    // al caerse o cerrarce el programa

    public static List<Titan> lista_titanes;  // Lleva un registro de los titantes creados
    // members to support the two server sockets
    protected static DatagramSocket socket_personal;
    //private Runnable accepter_cliente;
    protected static MulticastSocket socket_distritos;
    protected static String nombre_distrito;
    

    //Las siglas son de:
    //P_ADDR: Personal Address, por ser la direccion para consultas personales
    //M_ADDR: Multicast Address, por ser la direccion para recibir consultas multicast

   
   


    public static void IniciarDistrito(){
        String input;
        try{
            System.out.println("Nombre Servidor: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            input = br.readLine();
            nombre_distrito = input;
            
            System.out.println("IP Multicast: ");
            input = br.readLine();
            M_ADDR = input;


            System.out.println("Puerto Multicast: ");
            input = br.readLine();
            M_PORT = Integer.parseInt(input);

            System.out.println("IP Peticiones: ");
            input = br.readLine();
            P_ADDR = input;

            System.out.println("Puerto Peticiones: ");
            input = br.readLine();
            P_PORT = Integer.parseInt(input);

            lista_titanes = new ArrayList<Titan>();  // Lleva un registro de los titantes creados            

            //Ligamos el socket personal a un socket local especifico
            socket_personal = new DatagramSocket(P_PORT,InetAddress.getByName(P_ADDR));

            //Enviamos un mensaje avisando de nuestra creacion
            socket_distritos = new MulticastSocket(M_PORT);
            socket_distritos.joinGroup(InetAddress.getByName(M_ADDR));
            String msg = "NewDistrito/"+nombre_distrito+"/"+M_ADDR+";"+M_PORT+";"+P_ADDR+";"+P_PORT;
            DatagramPacket notice_msg = new DatagramPacket(msg.getBytes(),msg.length(),InetAddress.getByName(M_ADDR),M_PORT);
            socket_distritos.send(notice_msg);

        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
class SocketCliente extends SuperSocket implements Runnable{
    
    public void run(){
        byte[] buf = new byte[1024];
        System.setProperty("java.net.preferIPv4Stack", "true");
        DatagramPacket resp_packet;
        
        
        try {
            while(true){
                
                System.out.println("Escuchando peticiones unicast en el puerto: "+P_PORT);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket_personal.setSoTimeout(TIMEOUT);
                socket_personal.receive(packet);                    
                
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                String msg = new String(data);

                // lectura de lo que nos mandan
                System.out.println(msg);
                
                String sep = "[/]"; //caracter separador
                String[] comando_cliente = msg.split(sep);// El resultado es: [IP, PORT]

                if(comando_cliente[0].equals("NewClient")){
                    msg = "Consola\n(1) Listar Titanes\n(2) Cambiar Distrito\n(3) Capturar Titan\n(4) Asesinar Titan\n(5) Listar Titantes Capturados\n(6) Listar Titantes Asesinados\n";
                    resp_packet = new DatagramPacket(msg.getBytes(), msg.length(), packet.getAddress(), packet.getPort());
                    socket_personal.send(resp_packet);
                }

                if(comando_cliente[0].equals("1")){ //Listar Titanes
                    String codif;
                    String resp_msg = "ListTitanes";
                    ListIterator<Titan> iter = lista_titanes.listIterator();
                    Titan titan;
                    while(iter.hasNext()){
                        // Formato: ListTitanes/idTitan;Nombre;Tipo;Estado;Distrito/...
                        titan = iter.next();
                        codif = "/"+titan.GetId()+";"+titan.GetNombre()+";"+titan.GetTipo()+";"+titan.GetEstado()+";"+titan.GetDistrito();
                        resp_msg = resp_msg.concat(codif);
                    }
                    resp_packet = new DatagramPacket(resp_msg.getBytes(), resp_msg.length(),packet.getAddress(), packet.getPort());
                    socket_personal.send(resp_packet);
                }

                if(comando_cliente[0].equals("3")){// Capturar titan, formato esperado: 3/IDTitanCapturado
                    //Primero chequeamos que sea uno de los titanes del distrito y que este como vivo
                    ListIterator<Titan> iter = lista_titanes.listIterator();
                    Titan titan;
                    while(iter.hasNext()){
                        titan = iter.next();
                        if(titan.GetId() == comando_cliente[1]){
                            if(titan.GetEstado().equals("vivo")){//Si esta vivo

                                //ahora avisamos que este fue capturado
                                String notice_msg = "CaptTitan/"+titan.GetId().toString();//formato de aviso: Comando/IdTitanCapturado
                                DatagramPacket notice_packet = new DatagramPacket(notice_msg.getBytes(),notice_msg.length(),InetAddress.getByName(M_ADDR),M_PORT);
                                socket_distritos.send(notice_packet);

                                Titan cap_titan;
                                cap_titan = titan;
                                cap_titan.SetEstado("capturado");
                                iter.set(cap_titan);
                                break;
                            }
                            String resp_msg = "El titan especificado no puede ser capturado porque no esta vivo";
                            resp_packet = new DatagramPacket(resp_msg.getBytes(),resp_msg.length(),packet.getAddress(),packet.getPort());
                            socket_personal.send(resp_packet);
                            break;
                        }
                        String resp_msg = "El titan especificado no se encuentra en el distrito seleccionado";
                        resp_packet = new DatagramPacket(resp_msg.getBytes(),resp_msg.length(),packet.getAddress(),packet.getPort());
                        socket_personal.send(resp_packet);
                    }
                }

                if(comando_cliente[0].equals("4")){// Asesinar titan, formato esperado: 4/IDTitanAsesinado
                    //Primero chequeamos que sea uno de los titanes del distrito y que este como vivo
                    ListIterator<Titan> iter = lista_titanes.listIterator();
                    Titan titan;
                    while(iter.hasNext()){
                        titan = iter.next();
                        if(titan.GetId() == comando_cliente[1]){
                            if(titan.GetEstado().equals("vivo")){//Si esta vivo
                                
                                //ahora avisamos que este fue asesinado
                                String notice_msg = "KillTitan/"+titan.GetId();//formato de aviso: Comando/IdTitanasesinado
                                DatagramPacket notice_packet = new DatagramPacket(notice_msg.getBytes(),notice_msg.length(),InetAddress.getByName(M_ADDR),M_PORT);
                                socket_distritos.send(notice_packet);

                                Titan killd_titan = titan;
                                killd_titan.SetEstado("asesinado");
                                iter.set(killd_titan);
                                break;
                            }
                            String resp_msg = "El titan especificado no puede ser asesinado porque no esta vivo";
                            resp_packet = new DatagramPacket(resp_msg.getBytes(),resp_msg.length(),packet.getAddress(),packet.getPort());
                            socket_personal.send(resp_packet);
                            break;
                        }
                        String resp_msg = "El titan especificado no se encuentra en el distrito seleccionado";
                        resp_packet = new DatagramPacket(resp_msg.getBytes(),resp_msg.length(),packet.getAddress(),packet.getPort());
                        socket_personal.send(resp_packet);
                    }
                }

                if(comando_cliente[0].equals("5")){
                    String codif;
                    String resp_msg = "ListTitanesCapt";
                    ListIterator<Titan> iter = lista_titanes.listIterator();
                    Titan titan;
                    while(iter.hasNext()){
                        titan = iter.next();
                        if(titan.GetEstado().equals("capturado")){
                           // Formato: ListTitanesCapt/idTitan;Nombre;Tipo;Estado;Distrito/...
                            codif = "/"+titan.GetId()+";"+titan.GetNombre()+";"+titan.GetTipo()+";"+titan.GetEstado()+";"+titan.GetDistrito();
                            resp_msg = resp_msg.concat(codif);

                        }
                    }
                    resp_packet = new DatagramPacket(resp_msg.getBytes(),resp_msg.length(), packet.getAddress(), packet.getPort());
                    socket_personal.send(resp_packet);
                }
                
                if(comando_cliente[0].equals("6")){
                    String codif;
                    String resp_msg = "ListTitanesKill";
                    ListIterator<Titan> iter = lista_titanes.listIterator();
                    Titan titan;
                    while(iter.hasNext()){
                        titan = iter.next();
                        if(titan.GetEstado().equals("asesinado")){
                           // Formato: ListTitanesCapt/idTitan;Nombre;Tipo;Estado;Distrito/...
                            codif = "/"+titan.GetId()+";"+titan.GetNombre()+";"+titan.GetTipo()+";"+titan.GetEstado()+";"+titan.GetDistrito();
                            resp_msg = resp_msg.concat(codif);
                        }
                    }
                    resp_packet = new DatagramPacket(resp_msg.getBytes(),resp_msg.length(), packet.getAddress(), packet.getPort());
                    socket_personal.send(resp_packet);
                }
                System.out.println("Socket Unicast recieved: "+ msg);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}


///////////////////////// SOCKET DISTRITOS /////////////////
class SocketDistritos extends SuperSocket implements Runnable{


    public void run(){
        System.setProperty("java.net.preferIPv4Stack", "true");
        // Create a buffer of bytes, which will be used to store
        // the incoming bytes containing the information from the server.
        // Since the message is small here, 256 bytes should be enough.
        byte[] buf = new byte[1024];
        String sep; //caracter separador
        String[] comando_cliente;// El resultado es: [IP, PORT]
        String msg;
        byte[] data;
        DatagramPacket packet;
        
        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        
        try{
           
           
            while (true) {
                System.out.println("Escuchando en Multicast en la direccion IP:PORT: " + M_ADDR+":"+M_PORT);
                // Receive the information and print it.
                packet = new DatagramPacket(buf, buf.length);
                socket_distritos.setSoTimeout(TIMEOUT);
                socket_distritos.receive(packet);

                //Por diferencias de tamaño de buffer hay que reasignar la info recibida
                data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                msg = new String(data);
                /*
                if((msg.contains("/") == false) && (msg.equals(msg) == false)){
                    System.out.println("Se recibio un mensaje con formato incorrecto");
                    continue;        }
*/
                sep = "[/]"; //caracter separador
                comando_cliente = msg.split(sep);// El resultado es: [IP, PORT]

                //Se crea un nuevo titan desde el servidor principal
                if(comando_cliente[0].equals("NewTitan")){//formato: NewTitan/Id;Nombre;Tipo;Distrito
                    String sep2 = "[;]"; //caracter separador
                    String[] info_titan = msg.split(sep2);// El resultado es: [IP, PORT]
                    if(info_titan[3].equals(nombre_distrito)){
                        Titan nuevo_titan = new Titan(info_titan[0],info_titan[1],info_titan[2],info_titan[3]);
                        lista_titanes.add(nuevo_titan);    
                    }

                }
                //Cuando el servidor principal pide que todos los distritos se identifiquen
                if(comando_cliente[0].equals("CheckDistritos")){
                    msg = "NewDistrito/"+nombre_distrito+"/"+M_ADDR+";"+M_PORT+";"+P_ADDR+";"+P_PORT;
                    DatagramPacket notice_msg = new DatagramPacket(msg.getBytes(),msg.length(),InetAddress.getByName(M_ADDR),M_PORT);
                    socket_distritos.send(notice_msg);
                }

                //

                

                
                System.out.println("Socket Multicast recieved msg: " + msg);}}
                catch (IOException ex) {
                    ex.printStackTrace();
                    }

    }
        
        
   
}




//////////////// CLASE TITAN ///////////////////////////////
class Titan{
    private String id;
    private String nombre;
    private String tipo;
    private String distrito;
    private String estado; //capturado, asesinado, vivo
    private static AtomicInteger ID_GENERATOR = new AtomicInteger(); //para autogenerar los id

    // Constructor
    public Titan(String titan_id,String titan_nombre, String titan_tipo, String titan_distrito){
        this.SetId(titan_id);
        this.SetNombre(titan_nombre);
        this.SetTipo(titan_tipo);
        this.SetDistrito(titan_distrito);
        this.SetEstado("vivo");

    }

    /// GETTERS /////
    public String GetId(){
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
    public void SetId(String id){
        this.id = id;
    }
    public void SetNombre(String titan_nombre){
        this.nombre = titan_nombre;
    }
    public void SetDistrito(String titan_distrito){
        this.distrito = titan_distrito;
    }
    public void SetTipo(String titan_tipo){
        this.tipo = titan_tipo;
    }
    public void SetEstado(String titan_estado){
        this.estado = titan_estado;
    }
}
