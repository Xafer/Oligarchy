package haven.oligarchy.event;

public class HavenEvent {
	private HavenEventType het;
	private HavenEventData data;
	public HavenEvent(HavenEventType het, HavenEventData data)
	{
		this.het = het;
		this.data = data;
	}
	
	public HavenEventType getEventType() { return this.het; }
	public HavenEventData getData() { return this.data; }
}
