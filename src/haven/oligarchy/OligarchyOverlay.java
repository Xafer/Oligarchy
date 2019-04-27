package haven.oligarchy;

import haven.*;
import java.awt.*;

public class OligarchyOverlay
{
	private OligarchyThread th;
	public static GameUI currentUI = null;
	public static long uiLastOpened = -1;
	
	public OligarchyOverlay()
	{
		th = new OligarchyThread();
		/*if(HavenPanel.lui.root.getchild(GameUI.class) != null)
			HavenPanel.lui.root.getchild(GameUI.class).msg("Hello mumbo", Color.CYAN);*/
		//System.out.println(GameUI.Game.gameui().ui.root.findchild(GameUI.class).msg(String.format("TEST"), Color.WHITE));
	}
	
	public void start() { th.start(); }
	
	public static void setCurrentUI(GameUI ui)
	{
		uiLastOpened = System.currentTimeMillis();
		currentUI = ui;
	}
}