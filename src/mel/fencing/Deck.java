package mel.fencing;

import java.util.ArrayList;

public class Deck
{
    private ArrayList<Card> cards = new ArrayList<Card>();
    
    public Deck()
    {
        for(int i = 0; i < 5; i++)
        {
            cards.add(new Card(1));
            cards.add(new Card(2));
            cards.add(new Card(3));
            cards.add(new Card(4));
            cards.add(new Card(5));
        }
        shuffle();
    }

    /**
     * Shuffles the cards.
     */
    public void shuffle()
    {
        int size = cards.size();
        for(int i = 0; i < size; i++)
        {
            int temp = (int) (Math.random()*(size-i))+i;
            Card c = cards.get(temp);
            cards.set(temp, cards.get(i));
            cards.set(i, c);
        }
    }
    
    public Card getCard(int index) { return cards.get(index); }
    
    public String toString()
    {
	String out = "";
	for(Card c: cards)
	{
	    out += c.toString();
	    out += ",";
	}
	return out.substring(0, out.length()-2);
    }
}
