package mel.fencing;

//TODO RFE make a common package for Card and Hand
public class Hand
{
    public static final int HAND_SIZE = 5;
    Card card[] = new Card[HAND_SIZE];
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(1+HAND_SIZE);
        sb.append("h");
        for(Card c: card) sb.append(c.toChar());
        return sb.toString();
    }
    
    public Card getCard(int i) { return card[i]; }

    public void setHand(String in)
    {
        for(int i = 0; i < HAND_SIZE; i++)
        {
            if(in.charAt(i) == '0') card[i] = null;
            else card[i] = new Card(in.charAt(i));
        }
    } 
}
