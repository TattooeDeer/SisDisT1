import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Cliente {
	private static String ip_cliente="127.0.0.1";
	private static int puerto_cliente=8887;
	
	public Cliente(String ip_c, int puerto_c){
		this.ip_cliente = ip_c;
		this.puerto_cliente = puerto_c;
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("[Cliente] Ingresar IP Servidor Central: ");
		String ip_sc = br.readLine();
		System.out.println("[Cliente] Ingrear Puerto Servidor Central: ");
		String puerto_sc = br.readLine();
		//hacer conexion con servidor 
		System.out.println("Introducir Nombre del Distrito a Investigar: ");
		String distrito = br.readLine();
		
		InetAddress address = InetAddress.getByName("localhost");
		try (DatagramSocket serverSocket = new DatagramSocket()) {
			String msg = "solicitud/"+distrito;
	        DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
	                        msg.getBytes().length, address, Integer.parseInt(puerto_sc.trim()));
	        serverSocket.send(msgPacket);                
	        System.out.println("Mensaje enviado por el cliente: " + msg);

	        serverSocket.close();
	        //recibir respuesta del servidor
	        byte[] buf = new byte[1024];
            System.setProperty("java.net.preferIPv4Stack", "true");

	        
	        try{
                DatagramSocket s = new DatagramSocket(puerto_cliente);
                System.out.println("Escuchando a Cliente en IP:PORT: "+ip_cliente+":"+ puerto_cliente);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                s.setSoTimeout(20000);
                s.receive(packet);
                
                String logged = new String(packet.getData());
    	        System.out.println("Mensaje enviado desde el servidor: " + logged);
    	        if (logged.equals("true")){
    	        	mostrar_consola();
    	        }
	        } catch(IOException e) { System.out.println(e);}
	        

	        


	    } catch (IOException ex) {
	            ex.printStackTrace();
	        }
		
		
	}

	public static void mostrar_consola(){
		System.out.println("[Cliente] Consola");
		System.out.println("[Cliente] (1) Listar Titanes");
		System.out.println("[Cliente] (2) Cambiar Distrito");
		System.out.println("[Cliente] (3) Capturar Titán");
		System.out.println("[Cliente] (4) Asesinar Titán");
		System.out.println("[Cliente] (5) Listar Titanes Capturados");
		System.out.println("[Cliente] (6) Listar Titanes Asesinados");
	}
	
	public void listar_titanes(){}
	public void cambiar_distrito(){}
	public void capturar_titan(){}
	public void asesinar_titan(){}
	public void listar_titanes_capturados(){}
	public void listar_titanes_asesinados(){}
}
