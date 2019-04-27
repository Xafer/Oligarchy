package haven.oligarchy;

import haven.AuthClient;
import haven.BuddyWnd.Buddy;
import haven.CharWnd;
import haven.Charlist;
import haven.ChatUI;
import haven.ChatUI.Channel;
import haven.ChatUI.EntryChannel;
import haven.Coord;
import haven.Coord2d;
import haven.FlowerMenu;
import haven.FlowerMenu.Petal;
import haven.GameUI;
import haven.Glob;
import haven.Gob;
import haven.LoginScreen;
import haven.MCache;
import haven.Moving;
import haven.MCache.Grid;
import haven.OCache;
import haven.Polity;
import haven.Resource;
import haven.Resource.Loading;
import haven.Resource.Spec;
import haven.Widget;
import haven.oligarchy.event.HavenEventDispatcher;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class CommandUtil {
	
	//public static HavenEventDispatcher dispatcher = new HavenEventDispatcher();
	
	public static String testLoad()
	{
		String err = "";
		if(	CommandUtil.Controller.currentFlowerMenu == null)err += 0 + ",";
		if( CommandUtil.Login.loginScreen == null)err += 1 + ",";
		if( CommandUtil.Login.charScreen == null)err += 2 + ",";
		if( OligarchyThread.currentThread == null)err += 3 + ",";
		if( OligarchyOverlay.currentUI == null)err += 4 + ",";
		return err;
	}
	
	public static void log(String msg)
	{
		while(true)
		{
			try
			{
				if(OligarchyOverlay.currentUI != null)
					OligarchyOverlay.currentUI.msg("LOG: " + msg, Color.CYAN);
				break;
			} catch(NullPointerException n)//Prevent from crashing and wait for ui before continuing
			{
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static enum PolityType {
		VILLAGE ("Village"),
		REALM ("Realm");
		
		private final String n;
		private PolityType(String n){this.n = n;}
		public String getName() { return this.n; }
	}
	
	public static enum ConditionType {
		VISIBLE ('v');
		
		private final char letter;
		private ConditionType(char letter) { this.letter = letter; }
		public char getLetter() { return this.letter; }
	}
	public static class Environment
	{
		//get objects from resource name
		public static List<Gob> getObjects(String objectRes)
		{
			
			List<Gob> gobs = new ArrayList<Gob>();
			
			try
			{
				OCache c = OligarchyOverlay.currentUI.ui.sess.glob.oc;
				Set<Long> keys = c.getKeys();
				
				for(long l : keys)
				{
					Gob g = c.getgob(l);
					Resource res = g.getres();
					if(	res != null &&
						res.basename().equals(objectRes))
						gobs.add(g);
				}
			} catch(Exception e)
			{
				return new ArrayList<Gob>();
			}
			
			return gobs;
		}
		
		public static Gob getClosestObject(List<Gob> gobs)
		{
			Gob closest = null;
			Coord2d pos = Player.getPosition();
			double lastDist = Double.MAX_VALUE;

			double currDist;
			for(Gob gob : gobs)
				if((currDist = gob.rc.dist(pos)) < lastDist)
				{
					closest = gob;
					lastDist = currDist;
				}
			
			return closest;
		}
		
		public static Gob getClosestObject(String resName)
		{
			return getClosestObject(getObjects(resName));
		}
		
		public static String getTileAt(double x, double y)
		{
			int resInt = OligarchyOverlay.currentUI.map.glob.map.gettile(new Coord2d(x,y).floor(MCache.tilesz));
			Resource res = OligarchyOverlay.currentUI.map.glob.map.tilesetr(resInt);
			
			return res.basename();
		}
		
		public static Resource getTileResAt(double x, double y)
		{
			int resInt = OligarchyOverlay.currentUI.map.glob.map.gettile(new Coord2d(x,y).floor(MCache.tilesz));
			Resource res = OligarchyOverlay.currentUI.map.glob.map.tilesetr(resInt);
			
			return res;
		}
		
		public static int getHeightAt(double x, double y)
		{
			return OligarchyOverlay.currentUI.map.glob.map.getz(new Coord2d(x,y).floor(MCache.tilesz));
		}
	}
	
	public static class Controller
	{
		public static List<TaskAction> currentActions;
		private static TaskAction currentAction;
		private static int taskIndex = 0;
		private static long lastFetch = System.currentTimeMillis();
		private static FlowerMenu currentFlowerMenu;
		
		public static void setCurrentActions(List<TaskAction> currentActions)
		{
			Controller.currentActions = currentActions;
			taskIndex = 0;
			nextAction();
		}
		
		public static void update()
		{
			long tick = System.currentTimeMillis();
			
			if(	currentActions != null &&
				currentAction.isFinished())
				nextAction();
			else if(currentActions == null &&
					tick - lastFetch > 20*1000 &&
					isEnvironmentLoaded())
			{
					CommandUtil.Networking.connection.sendUTF("f");
					lastFetch = tick;
			}
		}
		
		/**
		 * 
		 * @return !if there are actions
		 */
		public static boolean nextAction()
		{
			if(currentActions == null)
				return false;
			
			currentAction = currentActions.get(taskIndex);
			currentAction.run();
			
			taskIndex++;
			
			log("running task: " + currentAction.getType().toString());
			
			if(taskIndex >= currentActions.size())
			{
				taskIndex = 0;
				currentActions = null;
			}
			
			return true;
		}
		
		public static void moveTaskIndex(int i) { taskIndex += i; }
		
		
		public static boolean processCondition(String c)
		{
			String[] condition = c.split(",");
			
			ConditionType ct = null;
			
			for(ConditionType t : ConditionType.values())
				if(t.getLetter() == condition[1].charAt(0))
				{
					ct = t;
					break;
				}
			
			boolean r = false;
			
			switch(ct)
			{
			case VISIBLE:
				boolean v = Environment.getClosestObject(condition[1].substring(1, condition[1].length())) != null;
				r = (v == (condition[2].equals("bt")));
				break;
			}
			
			return r;
		}
		
		public static FlowerMenu getFlowerMenu(){ return currentFlowerMenu; }
		
		public static void setCurrenFlowerMenu(FlowerMenu flowerMenu) { currentFlowerMenu = flowerMenu; }
		
		public static Petal getFlowerMenuPetal(String petalName)
		{
			if(currentFlowerMenu == null)
				return null;
			
			String n = petalName.toLowerCase();
			for(Petal opt : currentFlowerMenu.opts)
				if(opt.name.toLowerCase().equals(n))
					return opt;
			
			return null;
		}
		
		public static boolean chooseFlowerMenuPetal(String petalName)
		{
			Petal p = getFlowerMenuPetal(petalName);
			
			if(p == null)
				return false;
			
			currentFlowerMenu.choose(p);
			
			return true;
		}

		public static void waitForEnvironment() {
			while(!isEnvironmentLoaded())
			{
				waitFor(250);
			}
		}
		
		public static boolean isEnvironmentLoaded()
		{
			try
			{
			if(	OligarchyOverlay.currentUI != null &&
				OligarchyOverlay.currentUI.map != null &&
				OligarchyOverlay.currentUI.map.rls != null)
			{
				System.out.println(""+OligarchyOverlay.currentUI.map.rls.ignload);
				return OligarchyOverlay.currentUI.map.rls.ignload;
			}
			}
			catch(NullPointerException e) {
				e.printStackTrace();
				return false;
			}
			return false;
			/*try
			{
				Coord2d p = CommandUtil.Player.getPosition();
				CommandUtil.Environment.getTileAt(p.x,p.y);
				return true;
			}
			catch(Exception e)
			{
				return false;
			}*/
		}
		
		public static void waitFor(long time)
		{
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Classes for making a coordinate local or global (with random offset)
	 * @author June
	 *
	 */
	public static class Localizer
	{
		private static Coord2d offset = new Coord2d();
		private static Coord2d hashPos = new Coord2d();
		
		public static Coord2d toLocal(Coord2d c){ return c.sub(Localizer.offset); }
		
		public static Coord2d toGlobal(Coord2d c) { return c.add(Localizer.offset); }

		public static String hashedTileRes = "limestone";
		public static String cornerTileRes = "catgold"; 
		
		public static Map<Integer,Coord2d> hashedLocations = new HashMap<Integer,Coord2d>();
		
		public static int[] currentMapTilePos = new int[]{0,0};
		
		public static final int MAPTILESIZE = 25;
		
		public static void update()
		{
			Coord2d playerpos = Player.getPosition();
			Coord2d localpos = Localizer.toLocal(playerpos);
			Coord2d tileSize = MCache.tilesz;
			int[] intpos = new int[]{ new Double(localpos.x/tileSize.x).intValue(),new Double(localpos.y/tileSize.y).intValue()};
			int[] current = new int[]{intpos[0]/MAPTILESIZE,intpos[1]/MAPTILESIZE};
			
			
			if(	current[0] != currentMapTilePos[0] ||
				current[1] != currentMapTilePos[1])
			{
				CommandUtil.log("Switch! " + Environment.getHeightAt(playerpos.x,playerpos.y));
				
				int[] dir = new int[] {currentMapTilePos[0]-current[0],currentMapTilePos[1]-current[1]};
				
				for(int i = -1; i <= 1; i++)
					for(int j = -1; j <= 1; j++)
					{
						int[] newpos = new int[] {current[0]+j,current[1]+i};
						String mapData = "";
						if(	Math.abs(currentMapTilePos[0]-newpos[0]) > 1 ||
							Math.abs(currentMapTilePos[1]-newpos[1]) > 1)
						{
							mapData = readMapTileData(newpos[0],newpos[1]);
							CommandUtil.Networking.connection.sendUTF("m"+mapData);
						}

						
					}
				
				currentMapTilePos = current;
			}
		}
		
		public static String readMapTileData(int x, int y)
		{
			String mapTileData = "";
			
			StringBuilder sb = new StringBuilder();
			List<String> tiletypes = new ArrayList<String>();
			
			Map<String,char[]> tileColors = new HashMap<String,char[]>();
			double[] offset = new double[]{x*MAPTILESIZE*MCache.tilesz.x,y*MAPTILESIZE*MCache.tilesz.y};
			
			for(int yn = 0; yn < MAPTILESIZE; yn++)
				for(int xn = 0; xn < MAPTILESIZE; xn++)
				{
					Coord2d global = Localizer.toGlobal(new Coord2d(offset[0]+xn*MCache.tilesz.x,offset[1]+yn*MCache.tilesz.y));
					String tile = Environment.getTileAt(global.x,global.y);
					int height = Math.max(-20,Environment.getHeightAt(global.x,global.y)) + 20;
							
					boolean alreadyHasTile = false;
					
					char index = 0;
					
					for(String tileName: tiletypes)
					{
						if(tileName.equals(tile))
						{
							alreadyHasTile = true;
							break;
						}
						index++;
					}
					
					if(!alreadyHasTile)
					{
						tiletypes.add(tile);
						tileColors.put(tile,new char[]{tile.charAt(0),tile.charAt(1),tile.charAt(2)});
					}
						
					char tileIndex = 0;//(char)((List<String>)tileColors.keySet()).indexOf(tile);
					char heightB = (char)((height%(Character.MAX_VALUE-'A')) + 'A');
					char heightA = (char)(((height-heightB)/(Character.MAX_VALUE-'A')) + 'A');
					
					boolean appended = false;
					
					sb.append((char)(index+'A'));
					sb.append(heightA);
					sb.append(heightB);
				}
			
			StringBuilder sb2 = new StringBuilder();
			
			sb2.append(x);
			sb2.append(",");
			sb2.append(y);
			sb2.append(":");
			
			int tilei = 0;
			for(String tileType : tiletypes)
			{
				if(tilei > 0)sb2.append(",");
				sb2.append(tileType);
				sb2.append("!");
				
				char[] col = tileColors.get(tileType);

				sb2.append(col[0]);
				sb2.append(col[1]);
				sb2.append(col[2]);
				tilei++;
			}
				
			sb2.append(":");
			
			sb2.append(sb.toString());
			
			mapTileData = sb2.toString();
			
			return mapTileData;
		}
		
		public static void fetchOffset()
		{
			int r = 35;
			Coord2d pos = Player.getPosition();
			int hash = -1;
			
			for(int i = -r+1; i < r; i++)
			{
				for(int j = -r+1; j < r; j++)
				{
					double x = i*MCache.tilesz.x + pos.x;
					double y = j*MCache.tilesz.y + pos.y;
					String tile = Environment.getTileAt(x, y);
					if(tile.equals(hashedTileRes))
					{
						hash = parseTileHashAt(x,y);
						break;
					}
				}
				if(hash >= 0)break;
			}
			
			CommandUtil.Networking.connection.sendUTF("o" + hash);
		}
		
		public static int parseTileHashAt(double x, double y)
		{
			Coord2d topLeft = new Coord2d(x,y);
			Coord2d testCoord = new Coord2d(x,y);
			Coord2d tileSize = MCache.tilesz;
			
			int hash = 0;
			
			log("parsing tile hash at " + x + "," + y);
			
			//Find top corner
			for(int i = 0; i < 7; i++)
			{
				for(int j = 0; j < 7; j++)
				{
					Coord2d c = new Coord2d();
					c.x = -i*tileSize.x + testCoord.x;
					c.y = -j*tileSize.y + testCoord.y;
					
					if(Environment.getTileAt(c.x, c.y).equals(hashedTileRes))
					{
						topLeft.x = Math.min(topLeft.x, c.x);
						topLeft.y = Math.min(topLeft.y, c.y);
					}
				}
			}
			
			//topLeft.add(MCache.tilesz);
			
			hashPos = new Coord2d(topLeft.x,topLeft.y);
			
			//Get corner stone to check for fake parse
			if(!Environment.getTileAt(hashPos.x + 5*tileSize.x, hashPos.y + 5*tileSize.y).equals(cornerTileRes)){ return -1; }
			
			int e = 1;
			
			for(int i = 1; i <= 4; i++)
				for(int j = 1; j <= 4; j++)
				{
					Coord2d c = new Coord2d();
					c.x = j*tileSize.x + topLeft.x;
					c.y = i*tileSize.y + topLeft.y;
					hash += e*(Environment.getTileAt(c.x, c.y).equals(hashedTileRes)?1:0);
					e *= 2;
				}
			
			log("Located hash '" + hash + '\'');
			
			return hash;
		}
		
		public static void loadHashedLocation(String msg)
		{
				Coord2d tileSize = MCache.tilesz;
				
				String[] hashData = msg.substring(1,msg.length()).split(":");
				String data = hashData[1];
				String[] loc = data.split(",");
				offset = new Coord2d(hashPos.x,hashPos.y).sub(Double.parseDouble(loc[0])*tileSize.x, Double.parseDouble(loc[1])*tileSize.x);
				
				Coord2d p = Localizer.toLocal(Player.getPosition());
				
				log("Player local pos: " + p.x + ", " + p.y);
		}
		
		public static void setOffset(float x, float y)
		{
			offset.x = x;
			offset.y = y;
		}
		
		public static Coord2d getOffset()
		{
			if(offset.x + offset.y == 0)
				fetchOffset();
			return new Coord2d(offset.x,offset.y);
		}
	}
	
	public static class Player
	{
		public static Coord2d lastPos = new Coord2d();
		
		public static void update()
		{
			Coord2d pos = getPosition();
			Coord2d locPos = CommandUtil.Localizer.toLocal(pos);
			
			OligarchyThread.currentThread.sendUTF("p" + locPos.x + ":" + locPos.y);
			lastPos = new Coord2d(pos.x,pos.y);
		}
		
		public static Gob getPlayerGob() { return OligarchyOverlay.currentUI.map.player(); }
		
		public static String getCharacterName(){ return OligarchyOverlay.currentUI.buddies.getCharName(); }
		
		public static Coord2d getPosition() {
			try
				{return OligarchyOverlay.currentUI.map.player().rc;}
			catch(NullPointerException npe)
				{return lastPos;}
		}

		public static String getAttributes(boolean comp) {
			
			if(OligarchyOverlay.currentUI == null || OligarchyOverlay.currentUI.chrwdg == null)
				return null;
			
			StringBuilder attributes = new StringBuilder();
			
			int i = 0;
			int l = OligarchyOverlay.currentUI.chrwdg.base.size();
			
			for(CharWnd.Attr a : OligarchyOverlay.currentUI.chrwdg.base)
			{
				attributes.append(a.attr.getAttributeValues()[comp?0:1]);
				
				if(i < l-1)
					attributes.append(',');
					
				i++;
			}
			
			return attributes.toString();
		}
		
		public static String getAbilities(boolean comp) {
			
			if(OligarchyOverlay.currentUI == null || OligarchyOverlay.currentUI.chrwdg == null)
				return null;
			
			StringBuilder attributes = new StringBuilder();
			
			int i = 0;
			int l = OligarchyOverlay.currentUI.chrwdg.base.size();
			
			for(CharWnd.SAttr a : OligarchyOverlay.currentUI.chrwdg.skill)
			{
				attributes.append(a.attr.getAttributeValues()[comp?0:1]);
				
				if(i < l-1)
					attributes.append(',');
					
				i++;
			}
			
			
			return attributes.toString();
		}
		
		public static boolean isMoving() { return getPlayerGob().getattr(Moving.class) == null; }

		public static void fetchBuddies() {
			Iterator<Buddy> buddies = OligarchyOverlay.currentUI.buddies.iterator();
			Buddy b;
			StringBuilder buddyMsg = new StringBuilder();
			
			buddyMsg.append('b');
			
			while(buddies.hasNext())
			{
				b = buddies.next();
				
				buddyMsg.append(b.name).append(',').append(b.seen?'t':'f');
				
				if(buddies.hasNext())
					buddyMsg.append(':');
			}
			
			CommandUtil.Networking.connection.sendUTF(buddyMsg.toString());
		}
	}
	
	public static class Chatter {
		
		public static Polity getPolity(PolityType t)
		{
			for(Polity pol : OligarchyOverlay.currentUI.polities)
			{
				CommandUtil.log(t.getName() + " vs " + pol.cap);
				if(pol.cap.equals(t.getName()))
					return pol;
			}
			
			return null;
		}
		
		private static void chat(EntryChannel channel, String msg)
		{
			if(channel != null)
				channel.send(msg);
		}
		
		private static Channel getChannelByName(String channelName)
		{
			for(Widget w = OligarchyOverlay.currentUI.chat.lchild; w != null; w = w.prev)
			{
				if(w instanceof Channel && ((Channel)w).name().equals(channelName))
					return (Channel) w;
			}
			
			return null;
		}
		
		public static void chatBuddy(String buddyName, String msg)
		{
			EntryChannel c = (EntryChannel) Chatter.getChannelByName(buddyName);
			
		
			if(c == null && OligarchyOverlay.currentUI.buddies != null)
			{
				Iterator<Buddy> it =  OligarchyOverlay.currentUI.buddies.iterator();
				while(it.hasNext())
				{
					Buddy b = it.next();
					b.chat();
				}
				
				c = (EntryChannel) Chatter.getChannelByName(buddyName);
			}
			
			chat(c,msg);
		}
		
		public static void chatLocal(String msg)
		{
			chat((EntryChannel) getChannelByName("Area Chat"), msg);
		}
		
		public static void chatRealm(String msg)
		{
			String realmName = Chatter.getPolity(PolityType.REALM).name;
			
			chat((EntryChannel) getChannelByName(realmName), msg);
		}
		
		public static void chatVillage(String msg)
		{
			Polity polity = Chatter.getPolity(PolityType.VILLAGE);
			CommandUtil.log(msg);
			String villageName = (polity == null)?"Area Chat":polity.name;
			
			chat((EntryChannel) getChannelByName(villageName), msg);
		}
		
		public static void parseAndChat(String channelData, String msg)
		{
			CommandUtil.log("Supposed to send this: "+msg);
			switch(channelData.charAt(0))
			{
			case 'v':
				chatVillage(msg);
				break;
			case 'r':
				chatRealm(msg);
			case 'l':
				chatLocal(msg);
				break;
			case 'b':
				String[] d = channelData.split(",");
				chatBuddy(d[1],msg);
			}
		}
		
		public static void recieveChat(String chatMsg)
		{
			OligarchyThread.currentThread.sendUTF("c" + chatMsg);
		}
	}
	
	public static class Networking
	{
		public static final int PORT = 2123;
		public static final String HOST = "localhost";
		public static Connection connection;
	}
	
	public static class Rendering
	{
		public static boolean RENDER = true;

		public static Color colorizePetal(String name) {
            
            Color c = Color.YELLOW;
            
            if(name.startsWith("Travel "))
            	c = Color.GREEN;
            if(name.startsWith("Extend"))
            	c = Color.ORANGE;
            else switch(name.toLowerCase())
            {
            case "kin":
            case "butcher":
            case "spar":
            	c = Color.RED;
            	break;
            case "chop":
            	c = new Color(0x664411);
            }
			return c;
		}
	}

	public static class Mover {
		public static void moveRelative(float x, float y)
		{
			Coord2d dest = new Coord2d(-x,-y).add(Player.getPosition());
			move((float)dest.x,(float)dest.y);
		}
		
		public static void move(float x, float y)
		{
			if(	OligarchyOverlay.currentUI == null ||
					OligarchyOverlay.currentUI.map == null)
					return;
				
			OligarchyOverlay.currentUI.map.pfLeftClick(new Coord2d(x,y).floor(), null);
		}

		public static void move(Coord2d c) { move((float)c.x,(float)c.y); }
		public static void moveRelative(Coord2d c) { moveRelative((float)c.x,(float)c.y); }
		
		public static void click(Gob gob, boolean shift, boolean left)
		{
			int btn = left?1:3;
			int mod = shift?0:1;
			OligarchyOverlay.currentUI.map.pfRightClick(gob, -1, btn, mod, null);
		}
	}
	
	public static class Login {
		
		public static LoginScreen loginScreen;
		public static Charlist charScreen;
		
		private static void lsmsg(Widget w, String msg, Object... obj) throws NullPointerException
		{
			w.wdgmsg(msg,obj);
		}
		
		public static boolean login(String username, String pass)
		{
			try
			{
				lsmsg(loginScreen, "login", new Object[]{new AuthClient.NativeCred(username, pass), false});
				return true;
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		
		public static boolean chooseCharacter(String characterName)
		{
			if(Login.charScreen == null)return false;
			try
			{
				lsmsg(charScreen, "play", new Object[]{characterName});
				return true;
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}
	
	public static class Parser
	{
		public static float[] parseVector(String vector){
			String[] values = vector.substring(1,vector.length()).split(",");
			
			int l = values.length;
			
			float[] parsed = new float[l];
			
			for(int i = 0; i < l; i++)
				parsed[i] = Float.parseFloat(values[i]);
			
			return parsed;
		}
		
		public static float parseFloat(String f)
		{
			if(f.length() < 2)
				return -1;
			
			return Float.parseFloat(f.substring(1, f.length()));
		}
	}
	
	public static class State
	{
		public static boolean inGame = false;
	}
}
