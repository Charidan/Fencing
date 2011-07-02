package mel.fencing;

//TODO RFE Make a common package for Card and Hand
public class Card
{
    private final int value;
    
    public Card(int value) { this.value = value; }
    public Card(char value) { this.value = value-'0'; }

    public int getValue() { return value; }
    
    public int toChar() { return '0'+value; }
    public String toString() { return ""+value; }
}
