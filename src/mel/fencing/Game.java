package mel.fencing;

public class Game
{
    public static final int TURN_BLACK_MOVE = 0;
    public static final int TURN_BLACK_PARRY = 1;
    public static final int TURN_WHITE_MOVE = 2;
    public static final int TURN_WHITE_PARRY = 3;
    public static final int TURN_GAME_OVER = 4;
    public static final int COLOR_NONE = 0;
    public static final int COLOR_WHITE = 1;
    public static final int COLOR_BLACK = 2;
    
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
    }
    
    public int getWhitepos() { return whitepos; }
    public int getBlackpos() { return blackpos; }
    public Hand getHand() { return hand; }
}
