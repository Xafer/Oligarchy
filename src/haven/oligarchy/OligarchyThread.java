package haven.oligarchy;

import java.awt.Color;

import haven.Coord2d;
import haven.GameUI;
import haven.HavenPanel;

public class OligarchyThread extends Thread{
	
	private UTFInterpret interpret;
	private Connection connection;
	private long start;
	private boolean checked = false;
	
	public static OligarchyThread currentThread;
	
	public OligarchyThread()
	{
		interpret = new UTFInterpret();
	}
	
	private void connect()
	{
		connection = new Connection(CommandUtil.Networking.HOST,
									CommandUtil.Networking.PORT,
									interpret);
		
		connection.start();
	}
	
	@Override
	public void run()
	{
		connect();
		currentThread = this;
		try {
			Thread.sleep(2000);
			start = System.currentTimeMillis();
			while(true)
			{
					tick();
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void tick()
	{
		if(checked)CommandUtil.Localizer.update();
		CommandUtil.Controller.update();
		CommandUtil.Player.update();
		if(System.currentTimeMillis()-start > 20000 && !checked)// && CommandUtil.Controller.isEnvironmentLoaded())
		{
			checked = true;
			CommandUtil.Localizer.fetchOffset();
		}
		
	}
	
	public void sendUTF(String msg) { connection.sendUTF(msg); }
}
