import java.net.InetAddress;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.MulticastSocket;
import java.net.Socket;

public class ServidorDistrito{
	private String nombre_servidor;
	private String IP_multicast;
	private String puerto_multicast;
	private String IP_peticiones;
	private String puerto_peticiones;
	final static String INET_ADDR = "localhost";
	final static int PORT = 8887;


	public ServidorDistrito(String nombre_server, String ip_m, String puerto_m, String ip_p, String puerto_p){
		this.nombre_servidor = nombre_server;
		this.IP_multicast = ip_m;
		this.puerto_peticiones = puerto_p;
		this.IP_peticiones = ip_p;
		this.puerto_peticiones = puerto_p;
	}

	public static void main(String[] args) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		//INICIALIZAR  SERVIDOR
		System.out.println("[Distrito] Nombre Distrito: ");
		String nombre_distrito = br.readline();
		System.out.println("[Distrito  "+nombre_distrito+"] IP Multicast: ");
		String ip_m =  br.readline();
		System.out.println("[Distrito"+nombre_distrito+"] Puerto Multicast: ");
		String puerto_m = br.readline();
		System.out.println("[Distrito"+nombre_distrito+"] IP Peticiones: ");
		String ip_p = br.readline();
		System.out.println("[Distrito"+nombre_distrito+"] Puerto Peticiones: ");
		String puerto_p = br.readline();
	}

	private void publicar_titan(String nombre_distrito){
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println(["[Distrito  "+nombre_distrito+"] Introducir Nombre : ");
		String nombre_titan =  br.readline();
		System.out.println(["[Distrito"+nombre_distrito+"] Introducir Tipo: ");
		System.out.println("1.- Normal\n 2.-Excentrico\n 3.-Cambiante");
		String opcion_tipo_titan = br.readline();
		if (opcion_tipo_titan.equals("1")){
			String tipo_titan = "Normal";
		}
		else if (opcion_tipo_titan.equals("2"){
			String tipo_titan = "Excentrico";
		}
		else if (opcion_tipo_titan.equals("3")){
			String tipo_titan = "Cambiante";
		}


		//conexion al servidor central Unicast
		InetAddress addr = InetAddress.getByName(INET_ADDR);
		byte[] buf = new byte[1024];

     
        try (DatagramSocket serverSocket = new DatagramSocket()) {
        	//envia preguntado por id del titan
        	String msg = "id";
        	DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(),
                        msg.getBytes().length, addr, PORT);
            serverSocket.send(sendPacket);

            //recibe el id del titan 
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            serverSocket.setSoTimeout(10000);
        	serverSocket.receive(receivePacket);
            String id_titan = new String(packet.getData());

            //muestra la informacion del titan
            System.out.println("[Distrito" + nombre_distrito + "] Se ha publicado el Titan: " + nombre_titan);
            System.out.println("*********************\n
            					ID: "+ id_titan + "\n" +
            					"Nombre: " + nombre_titan + "\n"+
            					"Tipo: "+ tipo_titan+"\n"+
            					"*********************");

            //enviar alerta al cliente

            //conexion al Servidor por multicast
             

        } catch (IOException ex) {
            ex.printStackTrace();
        }

		String ip_p = br.readline();
		System.out.println("[Distrito"+ nombre_distrito +"] Puerto Peticiones: ");
		String puerto_p = br.readline();
	}
}
