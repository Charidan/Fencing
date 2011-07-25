package mel.fencing;

import java.util.Collection;
import java.util.HashSet;

public class Game
{
    public static final int COLOR_NONE = -10;
    public static final int COLOR_GREEN = 0;
    public static final int COLOR_PURPLE = 10;
    public static final int TURN_MOVE = 0;
    public static final int TURN_PARRY = 1;
    public static final int TURN_PARRY_OR_RETREAT = 2;
    public static final int TURN_PURPLE_MOVE =               COLOR_PURPLE+TURN_MOVE;
    public static final int TURN_PURPLE_PARRY =              COLOR_PURPLE+TURN_PARRY;
    public static final int TURN_PURPLE_PARRY_OR_RETREAT =   COLOR_PURPLE+TURN_PARRY_OR_RETREAT; 
    public static final int TURN_GREEN_MOVE =               COLOR_GREEN+TURN_MOVE;
    public static final int TURN_GREEN_PARRY =              COLOR_GREEN+TURN_PARRY;
    public static final int TURN_GREEN_PARRY_OR_RETREAT =   COLOR_GREEN+TURN_PARRY_OR_RETREAT; 
    public static final int TURN_GAME_OVER = -1;
    
    private Collection<GameListener> listeners = new HashSet<GameListener>();
    
    Hand hand = new Hand();
    int blackHP = 5;
    int whiteHP = 5;
    int purppos = 23;
    int greenpos = 1;
    int turn = TURN_GREEN_MOVE;
    
    public void reset()
    {
        purppos = 23;
        greenpos = 1;
        turn = TURN_GREEN_MOVE;
        fireGameChanged();
    }
    
    public int getGreenpos() { return greenpos; }
    public int getPurppos() { return purppos; }
    public Hand getHand() { return hand; }
    public int getTurn() { return turn; }

    public void setHand(String in)
    {
        hand.setHand(in);
        fireGameChanged();
    }
    
    public void addListener(GameListener listener)
    {
        listeners.add(listener);
    }
    
    public void fireGameChanged()
    {
        for(GameListener gl : listeners) gl.gameChanged();
    }

    public void setPositions(String in)
    {
        greenpos = parsePos(in.charAt(0));
        purppos = parsePos(in.charAt(1));
        fireGameChanged();
    }
    
    private static final int parsePos(char in)
    {
        return in-'a'+1;
    }

    public void setTurn(int turn)
    {
        this.turn = turn;
        fireGameChanged();
    }
}
