package mel.fencing;

public class Card
{
    private final int value;
    
    public Card(int value) { this.value = value; }

    public int getValue() { return value; }
    
    public String toString()
    {
	return ""+value;
    }
}
