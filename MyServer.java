import java.net.*;
import java.io.*;
import java.lang.Thread;
import java.util.ArrayList;

public class MyServer{


	protected static ArrayList<Servicio> servicios = new ArrayList<Servicio>();
	protected static ArrayList<Socket> clientes = new ArrayList<Socket>();
	protected static ArrayList<String> historial = new ArrayList<String>();//Declaracion del arreglo historial 

    public static void main(String[] args){
		
		ServerSocket server = null;
		
		try{
			server = new ServerSocket(5001);
		}catch(Exception e){
            e.printStackTrace();
        }
		
		while(true){
			try{
				Socket cliente = server.accept();
				clientes.add(cliente);
				Servicio s = new Servicio(cliente, historial);
				
				servicios.add(s);
				Thread t = new Thread(s);
				t.start();

				for(Socket client : clientes)
					System.out.println("actual: "+client);


				for(Servicio ser : servicios)
				{
					ser.actualizar(clientes);
									
				}
				historial = s.getHistorial();//obtiene el historial de mensajes guardados(actualiza)
				//System.out.println(historial);


			}catch(Exception e){
				e.printStackTrace();
			}
		}
    }

}


class Servicio implements Runnable{
	protected Socket cliente;
	protected PrintWriter out; 
	protected PrintWriter outHis;                        
    protected BufferedReader in;
    protected ArrayList<Socket> clientes = new ArrayList<Socket>();
	protected String msg;

	protected static ArrayList<String> historial = new ArrayList<String>();//Declaracion de historial en clase Servicio

	public Servicio(Socket cliente, ArrayList<String> historial){
		this.cliente = cliente;
		this.historial = historial;//obtiene el historial del server
	}

	public void actualizar(ArrayList<Socket> clientes){
		this.clientes = clientes;
	}
	
	@Override
	public void run(){
		try{                                                                                                             
			in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));   
		    outHis = new PrintWriter(cliente.getOutputStream(),true);//Se crea el objeto para enviar el historial
		    outHis.println(String.join("a~", historial));//Se envia el historial convertido a String agregando un "-" entre elementos para bandera 

			while((msg = in.readLine())!=null && !msg.equals("DSCNCTR")){
				for(Socket cliente : this.clientes)
				{
					out = new PrintWriter(cliente.getOutputStream(),true);
					
					out.println(msg);
					historial.add(msg);//agrega mensajes al historial

					System.out.println("Enviando a: "+cliente);
					//System.out.println(historial);
				}
				
			}
			clientes.remove(cliente);

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public ArrayList<String> getHistorial()
	{
		return this.historial;//Retorna el historial
	}
}