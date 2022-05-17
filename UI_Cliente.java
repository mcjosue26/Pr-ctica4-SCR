
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.util.*;

import java.net.*;
import java.io.*;
import java.lang.Thread;


public class UI_Cliente extends JFrame implements ActionListener{
    
    private JPanel panel;
	private JTextArea textArea;
	private JScrollPane jScrollPane;
	private JLabel lblUsuario;
	private JLabel lblIP;
	private JLabel lblPuerto;
	private JLabel lblNotificacion;
	private JTextField txtUsuario;
	private JTextField txtIP;
	private JTextField txtPuerto;
	private JButton btnConectar;
	private JTextField txtMensaje;
	private JButton btnEnviar;
	
	private Socket servidor;                   
    private PrintWriter out;
    private String ip;
    private int puerto;
    private String usuario;
    private String mensaje;

    private boolean conectado = false;
    private Monitor monitor;
    private Thread t;
    


    public UI_Cliente(){
        this.setTitle("MiCliente");
        panel = new JPanel();
        panel.setLayout(null);
		
		lblUsuario = new JLabel("Usuario:");
		txtUsuario = new JTextField();
		lblIP = new JLabel("IP destino:");
		txtIP = new JTextField();
		lblPuerto = new JLabel("Puerto:");
		txtPuerto = new JTextField();
		lblNotificacion = new JLabel("Desconectado");
		btnConectar = new JButton("Conectar");
		btnConectar.setBackground(Color.decode("#FF7171")); 
		textArea = new JTextArea("Mensajes:");
		textArea.setEditable(false);
		jScrollPane = new JScrollPane(textArea);
		txtMensaje = new JTextField();
		txtMensaje.setEnabled(false);
		btnEnviar =  new JButton("Enviar");
		btnEnviar.setEnabled(false);

		

		panel.add(lblUsuario);
		panel.add(txtUsuario);
		panel.add(lblIP);
		panel.add(txtIP);
		panel.add(lblPuerto);
		panel.add(txtPuerto);
		panel.add(lblNotificacion);
		panel.add(btnConectar);
		panel.add(jScrollPane);
		panel.add(txtMensaje);
		panel.add(btnEnviar);

		lblUsuario.setBounds(50, 25, 100, 20);
		txtUsuario.setBounds(130, 25, 200,20);
		lblIP.setBounds(50, 50, 100, 20);
		txtIP.setBounds(130, 50, 200, 20);
		lblPuerto.setBounds(50, 75, 100, 20);
		txtPuerto.setBounds(130, 75, 50, 20);
		lblNotificacion.setBounds(200, 75, 200, 20);
		btnConectar.setBounds(375, 25, 150, 50);
		jScrollPane.setBounds(50, 110, 525, 300);
		txtMensaje.setBounds(50, 415, 405, 75);
		btnEnviar.setBounds(455, 415, 120, 75);

		

        
        this.add(panel);
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        this.setSize(600,500);
        this.setVisible(true);
		
		btnConectar.addActionListener(this);
		btnEnviar.addActionListener(this);
		
    }

    
    public void actionPerformed(ActionEvent event){
        if(event.getSource() == btnConectar){
        	if(!conectado){
        		conectar();
        	}

    		else{
    			desconectar();
    		}
        }
        	
        else if(event.getSource() == btnEnviar){
        	enviar();
        }
    }
    
    public void conectar(){

		System.out.println("Conectando...");

		try
		{
			ip = txtIP.getText();
			puerto = Integer.parseInt(txtPuerto.getText());

			servidor = new Socket(ip, puerto); 
            monitor = new Monitor(servidor, textArea);
            t = new Thread(monitor);
            t.start();
            out = new PrintWriter(servidor.getOutputStream(),true);
            usuario = txtUsuario.getText();

            txtUsuario.setEnabled(false);
            txtIP.setEnabled(false);
            txtPuerto.setEnabled(false);
            btnConectar.setText("Desconectar");
            btnConectar.setBackground(Color.decode("#64FF69"));
            txtMensaje.setEnabled(true);
			btnEnviar.setEnabled(true);
			txtMensaje.requestFocusInWindow();

			lblNotificacion.setText("Conectado");
			conectado = true;

		}
		catch(Exception err){
            err.printStackTrace();
            lblNotificacion.setText("Error");
        }
    }

    public void desconectar(){
    	try
		{
			out.println("DSCNCTR");
			t.interrupt();
			servidor.close();
			txtUsuario.setEnabled(true);
            txtIP.setEnabled(true);
            txtPuerto.setEnabled(true);
            btnConectar.setText("Conectar");
            btnConectar.setBackground(Color.decode("#FF7171"));
            txtMensaje.setEnabled(false);
			btnEnviar.setEnabled(false);

			lblNotificacion.setText("Desconectado");
			conectado = false;
		}
		catch(Exception err){
            err.printStackTrace();
            lblNotificacion.setText("Error");
        }
    	
    }

    public void enviar(){
    	try
		{
			mensaje = txtMensaje.getText();
			String cadena = usuario+": "+mensaje;

			char[] cadArray = cadena.toCharArray();

			for(int i=0; i<cadArray.length;i++)
			{
				int numerosAscii = 0;
				numerosAscii = cadArray[i]+1;
				cadArray[i] = (char) numerosAscii;

			}
			cadena = String.valueOf(cadArray);
			System.out.println(cadena);
			out.println(cadena);

			txtMensaje.setText("");
			txtMensaje.requestFocusInWindow();
		}
		catch(Exception err){
            err.printStackTrace();
        }
    }
    
    
    public static void main(String[] args){
        UI_Cliente miCliente = new UI_Cliente();
    }



}


class Monitor implements Runnable{

    BufferedReader in;
    Socket servidor;
    JTextArea textArea;
    String msg;
    String historial;

    public Monitor(Socket servidor, JTextArea textArea){
        this.servidor = servidor;
        this.textArea = textArea;
    }

    @Override
    public void run(){
        try{  

            in = new BufferedReader(new InputStreamReader(servidor.getInputStream()));   

            historial = in.readLine();//Obtiene la cadena del historial desde el Servidor
            
            char[] hisArray = historial.toCharArray();//Se convierte la cadena historial a un arreglo de tipo char
            desencriptar(hisArray);//desencripta el historial
			String mensajeHis = String.valueOf(hisArray);//Se convierte el arreglo mensajeHis a tipo String
			String[] split = mensajeHis.split("`}");//Se convierte el string mensajeHis a un arreglo de String con cada elemento separado por ","
			eliminarRepetidos(split);//elimina los mensajes repetidos del arreglo split

			for (int i=0; i<split.length; i++)
			{
				if(split[i] != "")//si es diferente de un espacio vacío.
			   {
			      textArea.setText(textArea.getText()+"\n"+split[i]);//escribe el historial en el area de texto si la condicion se cumple
			   } 
			}

            while((msg=in.readLine())!=null){
               // System.out.println(msg);

				char[] msgArray = msg.toCharArray();
				desencriptar(msgArray);
				String mensaje = String.valueOf(msgArray);

                textArea.setText(textArea.getText()+"\n"+mensaje);  
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

//Metodo para desencriptar mensajes
    public static void desencriptar(char[] arreglo)
    {
    	for(int i=0; i<arreglo.length;i++)
				{
					int numAscii1 = 0;
					numAscii1 = arreglo[i]-1;
					arreglo[i] = (char) numAscii1;
				}
    }
//Metodo para eliminar los mensajes repetidos en el arreglo del historial;
	public static void eliminarRepetidos(String[] arreglo) 
	{
  	 for(int i = 0; i < arreglo.length; i++)
  	 {
 	     for(int j = i + 1; j < arreglo.length; j++)
  	    {
  	       if(i != j)
  	       {
  	          if(arreglo[i].equals(arreglo[j]))
  	          {
   	            arreglo[j] = "";
  	          }
   	      }
  	    }
 	  }
	}
}