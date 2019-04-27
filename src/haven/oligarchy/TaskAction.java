package haven.oligarchy;

import haven.Coord2d;
import haven.FlowerMenu;
import haven.FlowerMenu.Petal;
import haven.GameUI;
import haven.Gob;
import haven.Moving;
import haven.Resource;
import haven.oligarchy.CommandUtil.Controller;

public class TaskAction {
	private ActionType type;
	private String data;
	private boolean finished;
	private String target;
	
	public TaskAction(String action)
	{
		char typeChar = action.charAt(0);
		
		type = ActionType.HEARTH;
		
		for(ActionType t : ActionType.values())
			if(typeChar == t.getTypeChar())
			{
				type = t;
				break;
			}
		
		if(action.length() > 1)
			data = action.substring(1, action.length());
		
		finished = false;
	}
	
	public void run()
	{
		GameUI ui = OligarchyOverlay.currentUI;
		
		switch(type)
		{
		case FCHOOSE:
			finished = Controller.chooseFlowerMenuPetal(data.substring(1,data.length()));
			break;
			
		case HEARTH:
			ui.menu.wdgmsg("act", "travel","hearth");
			break;
			
		case MOVE:
			Coord2d pos = null;
			switch(data.charAt(0))
			{
			default:
			case 's'://Strings (sString)
				String objectName = data.substring(1,data.length());
				Gob cobj = CommandUtil.Environment.getClosestObject(CommandUtil.Environment.getObjects(objectName));
				if(cobj != null)
					pos = cobj.rc;
				break;
			case 'v'://Vector (vX,Y)
				float[] v = CommandUtil.Parser.parseVector(data);
				pos = CommandUtil.Localizer.toGlobal(new Coord2d(v[0],v[1]));
				break;
			}
			
			if(pos != null)
				CommandUtil.Mover.move(pos);
			break;
			
		case REPEAT:
			String[] arg = data.split(":");
			int n = (int) Math.floor(CommandUtil.Parser.parseFloat(arg[0]));
			if(arg.length > 1 && arg[1].charAt(0) == 'c' && CommandUtil.Controller.processCondition(arg[1]))
				CommandUtil.Controller.moveTaskIndex(-n);
			break;
			
		case RIGHTCLICK:
			switch(data.charAt(0))
			{
			default:
			case 's':
				String objName = data.substring(1,data.length());
				Gob g = CommandUtil.Environment.getClosestObject(objName);
				if(g != null) CommandUtil.Mover.click(g, false, false);
				else finished = true;
				break;
			case 'v':
				break;
			}
			break;
		case WAIT:
			target = new StringBuilder().append("f").append(System.currentTimeMillis() + CommandUtil.Parser.parseFloat(data)*1000).toString();
			break;
		}
	}

	public boolean isFinished() {
		
		if(finished)
			return finished;
		
		GameUI ui = OligarchyOverlay.currentUI;
		
		try
		{
			switch(type)
			{
			case FCHOOSE:
				run();
				break;
			case HEARTH:
				Gob g = CommandUtil.Environment.getClosestObject("pow");
				if(finished = CommandUtil.Player.getPosition().dist(g.rc) < 5.0)
					CommandUtil.Localizer.fetchOffset();
				break;
				
			case MOVE:
				Coord2d v;
				float factor = 5.0f;
				switch(data.charAt(0))
				{
				default:
				case 's':
					String objectName = data.substring(1,data.length());
					v = CommandUtil.Environment.getClosestObject(CommandUtil.Environment.getObjects(objectName)).rc;
					factor = 20f;
					break;
				case 'v':
					float[] va = CommandUtil.Parser.parseVector(data);
					v = CommandUtil.Localizer.toGlobal(new Coord2d(va[0],va[1]));
					break;
				}
				Coord2d c = CommandUtil.Player.getPosition();
				v.sub(c);
				//if closer than half of a tile to target destination
				if(v.dist(c) < factor)
					finished = true;
				else if(!CommandUtil.Player.isMoving())
					run();
				break;
			case REPEAT:
				finished = true;
				break;
			case RIGHTCLICK:
				finished = !CommandUtil.Player.isMoving();
				break;
			case WAIT:
				finished = System.currentTimeMillis() > CommandUtil.Parser.parseFloat(target);
				break;
			}
		} catch(NullPointerException npe)
		{
			//Preventing crashes in case some resources are not loaded
		}
		
		return finished;
	}
	
	public String getData() { return this.data; }
	public ActionType getType() { return this.type; }
}
