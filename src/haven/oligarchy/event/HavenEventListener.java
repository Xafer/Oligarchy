package haven.oligarchy.event;

public interface HavenEventListener {
	public void act(HavenEvent e);
	public HavenEventType getType();
}
