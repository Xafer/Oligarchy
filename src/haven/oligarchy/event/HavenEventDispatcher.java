package haven.oligarchy.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HavenEventDispatcher {
	private Map<HavenEventType,List<HavenEventListener>> listenerMap;
	public HavenEventDispatcher()
	{
		for(HavenEventType t : HavenEventType.values())
		listenerMap.put(t, new ArrayList<HavenEventListener>());
	}
	
	public void dispatchEvent(HavenEvent e)
	{
		List<HavenEventListener> listenerList = listenerMap.get(e.getEventType());
		for(HavenEventListener l : listenerList)
		{
			l.act(e);
		}
	}
	
	public void addListener(HavenEventListener l)
	{
		listenerMap.get(l.getType()).add(l);
	}
	
	public void removeListener(HavenEventListener l)
	{
		listenerMap.get(l.getType()).remove(l);
	}
}
