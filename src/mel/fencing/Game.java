package mel.fencing;

import java.util.ArrayList;
import java.util.List;

public class Game
{
    public static final int COLOR_NONE = -10;
    public static final int COLOR_WHITE = 0;
    public static final int COLOR_BLACK = 10;
    public static final int TURN_MOVE = 0;
    public static final int TURN_PARRY = 1;
    public static final int TURN_PARRY_OR_RETREAT = 2;
    public static final int TURN_BLACK_MOVE =               COLOR_BLACK+TURN_MOVE;
    public static final int TURN_BLACK_PARRY =              COLOR_BLACK+TURN_PARRY;
    public static final int TURN_BLACK_PARRY_OR_RETREAT =   COLOR_BLACK+TURN_PARRY_OR_RETREAT; 
    public static final int TURN_WHITE_MOVE =               COLOR_WHITE+TURN_MOVE;
    public static final int TURN_WHITE_PARRY =              COLOR_WHITE+TURN_PARRY;
    public static final int TURN_WHITE_PARRY_OR_RETREAT =   COLOR_WHITE+TURN_PARRY_OR_RETREAT; 
    public static final int TURN_GAME_OVER = -1;
    
    private List<GameListener> listeners = new ArrayList<GameListener>();
    
    Hand hand = new Hand();
    int blackHP = 5;
    int whiteHP = 5;
    int blackpos = 23;
    int whitepos = 1;
    int turn = TURN_WHITE_MOVE;
    
    public void reset()
    {
        blackpos = 23;
        whitepos = 1;
        turn = TURN_WHITE_MOVE;
        fireGameChanged();
    }
    
    public int getWhitepos() { return whitepos; }
    public int getBlackpos() { return blackpos; }
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
}
