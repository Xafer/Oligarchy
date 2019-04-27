package haven.oligarchy.event;

public class HavenEventData<T> {
	private T[] data;
	
	public HavenEventData(T[] data)
	{
		this.data = data;
	}
	
	public T[] getData()
	{
		return this.data;
	}
}
