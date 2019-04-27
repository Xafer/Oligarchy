package haven.oligarchy;

import haven.Coord2d;
import haven.Loading;
import haven.Widget;

import java.util.ArrayList;
import java.util.List;

public class UTFInterpret {

	private void parseLoginMsg(String msg)
	{
		String logindata = msg.substring(1,msg.length());
		String[] info = logindata.split(":");
		while(!CommandUtil.Login.login(info[0], info[1]))
			CommandUtil.Controller.waitFor(250);
		
		if(info.length > 2)
			while(!CommandUtil.Login.chooseCharacter(info[2]))
				CommandUtil.Controller.waitFor(250);
		
		//CommandUtil.Controller.waitForEnvironment();
	}
	
	private void parseTasksMsg(String msg)
	{
		List<TaskAction> actions = new ArrayList<TaskAction>();
		
		String[] actionData = msg.substring(1, msg.length()).split("%");
		
		for(String a : actionData)
			actions.add(new TaskAction(a));
		
		if(actions != null)
			CommandUtil.Controller.setCurrentActions(actions);
	}
	
	public void parseChatMsg(String msg)
	{
		String[] chatdata = msg.substring(1,msg.length()).split(":");
		String channelData = chatdata[0];
		String chatmsg = chatdata[1];
		
		CommandUtil.Chatter.parseAndChat(channelData, chatmsg);
	}
	
	public synchronized void interpret(String msg) {
		//connect
		switch(msg.charAt(0))
		{
		case '?': parseLoginMsg(msg); break;
			
		case 'o': CommandUtil.Localizer.loadHashedLocation(msg); break;
			
		case 't': parseTasksMsg(msg); break;
			
		case 'b': CommandUtil.Player.fetchBuddies(); break;
		
		default:
		case 'c': parseChatMsg(msg); break;
		}
	}
}
