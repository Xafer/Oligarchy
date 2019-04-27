package haven.oligarchy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class Connection extends Thread
{
	private Socket socket;
	private final String host;
	private final int port;

	private DataInputStream dis;
	private DataOutputStream dos;
	
	private boolean shouldClose;
	
	private UTFInterpret interpret;
	
	private boolean connected;
	
	public Connection(String host, int port, UTFInterpret interpret)
	{
		this.host = host;
		this.port = port;
		
		this.interpret = interpret;
		
		this.connected = false;
		
		CommandUtil.Networking.connection = this;
	}
	
	public void run()
	{
		shouldClose = false;
		
		try {
			socket = new Socket(host, port);

			setConnectionStreams(socket);

			sendUTF("?");
			
			System.out.println("READY TO RECIEVE " + shouldClose);
			
			while(!shouldClose)
			{
				String msg = dis.readUTF();
				System.out.println(msg);
				interpret.interpret(msg);
				
				Thread.sleep(50);
			}
			
		} catch (UnknownHostException e) {
			this.connected = false;
		} catch (IOException e) {
			//e.printStackTrace();
		} catch (InterruptedException e) {
			this.connected = false;
		}
	}
	
	private void setConnectionStreams(Socket socket) throws IOException
	{
		if(!socket.isBound())
			return;
		this.dis = new DataInputStream(socket.getInputStream());
		this.dos = new DataOutputStream(socket.getOutputStream());
		this.connected = true;
	}
	
	synchronized public void sendUTF(String msg)
	{
		if(this.connected == false)
			return;
		
		try {
			dos.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
