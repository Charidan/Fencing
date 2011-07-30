package mel.fencing;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;
import android.widget.TextView;

public class StripModel
{
    public static final int STATE_SPLASH = 0;
    public static final int STATE_GAME = 1;
    public static final int STATE_ENDGAME = 2;
    
    private String myName = "Not Logged In";
    private String oppName = "";
    private int color = Game.COLOR_NONE;
    private Game game = new Game();
    private int state = STATE_SPLASH;
    private float cardWidth,cardHeight,cardTop,cardBottom,cardLeft,cardStep;
    private float goLeft, goTop, goRight, goBottom;
    private float stopLeft, stopRight, stopTop, stopBottom;
    private float actionLeft, actionTop, actionBottom, actionWidth, actionStep;
    
    public final Game getGame()             { return game; }
    public final int getColor()             { return color; }
    public final int getTurn()              { return game.getTurn(); }
    public final String getMyName()         { return myName; }
    public final String getOppName()        { return oppName; }
    public final int getState()             { return state; }
    public final float getCardWidth()       { return cardWidth; }
    public final float getCardHeight()      { return cardHeight; }
    public final float getCardTop()         { return cardTop; }
    public final float getCardBottom()      { return cardBottom; }
    public final float getCardLeft()        { return cardLeft; }
    public final float getCardStep()        { return cardStep; }
    public final float getGoLeft()          { return goLeft; }
    public final float getGoTop()           { return goTop; }
    public final float getGoRight()         { return goRight; }
    public final float getGoBottom()        { return goBottom; }
    public final float getStopLeft()        { return stopLeft; }
    public final float getStopRight()       { return stopRight; }
    public final float getStopTop()         { return stopTop; }
    public final float getStopBottom()      { return stopBottom; }
    public final float getActionLeft()      { return actionLeft; }
    public final float getActionTop()       { return actionTop; }
    public final float getActionBottom()    { return actionBottom; }
    public final float getActionWidth()     { return actionWidth; }
    public final float getActionStep()      { return actionStep; }
    
    public final void setMyName(String in)      { myName = in; }
    public final void setOppName(String in)     { oppName = in; }
    public final void setColor(int in)          { color = in; }
    public final void setState(int in)          { state = in; }
    public final void setCardWidth(float in)    { cardWidth = in; }
    public final void setCardHeight(float in)   { cardHeight = in; }
    public final void setCardTop(float in)      { cardTop = in; }
    public final void setCardBottom(float in)   { cardBottom = in; }
    public final void setCardLeft(float in)     { cardLeft = in; }
    public final void setCardStep(float in)     { cardStep = in; }
    public final void setGoLeft(float in)       { goLeft = in; }
    public final void setGoTop(float in)        { goTop = in; }
    public final void setGoRight(float in)      { goRight = in; }
    public final void setGoBottom(float in)     { goBottom = in; }
    public final void setStopLeft(float in)     { goLeft = in; }
    public final void setStopRight(float in)    { stopRight = in; }
    public final void setStopTop(float in)      { stopTop = in; }
    public final void setStopBottom(float in)   { stopBottom = in; }
    public final void setActionLeft(float in)   { actionLeft = in; }
    public final void setActionTop(float in)    { actionTop = in; }
    public final void setActionBottom(float in) { actionBottom = in; }
    public final void setActionWidth(float in)  { actionWidth = in; }
    public final void setActionStep(float in)   { actionStep = in; }
    
    private boolean dragging;               // true when dragging a card
    private Card dragCard;                  // the card being dragged
    private float dragOffsetX, dragOffsetY; // position of the touch relative to topLeft of the card
    private float dragPosX, dragPosY;       // position finger last detected at
    
    public final boolean isDragging()     { return dragging; }
    public final Card getDragCard()       { return dragCard; }
    public final int getDragValue()       { return (dragCard == null) ? -1 : dragCard.getValue(); }
    public final float getDragOffsetX()   { return dragOffsetX; }
    public final float getDragOffsetY()   { return dragOffsetY; }
    public final float getDragPositionX() { return dragPosX; }
    public final float getDragPositionY() { return dragPosY; }
    
    public final void setDragging(boolean in)           { dragging = in; }
    public final void setDragCard(Card in)              { dragCard = in; }
    public final void setDragOffset(float x, float y)   { dragOffsetX = x; dragOffsetY = y; }
    public final void setDragPosition(float x, float y) { dragPosX = x; dragPosY = y; }
    
    private Card retreatCard = null;
    private Card advanceCard = null;
    private List<Card> attackList = new ArrayList<Card>();
    int slots[];
    String pics[];
    
    public Card getRetreatCard()        { return retreatCard; }
    public Card getAdvanceCard()          { return advanceCard; }
    public List<Card> getAttackList()   { return attackList; }
    
    public void setRetreatCard(Card in)
    {
        if(retreatCard != null) replaceCard(retreatCard);
        if(advanceCard != null) replaceCard(advanceCard);
        while(!attackList.isEmpty()) replaceCard(attackList.remove(0));
        retreatCard = in;
        advanceCard = null;
    }
    
    public void setAdvanceCard(Card in)
    {
        if(retreatCard != null) replaceCard(retreatCard);
        if(advanceCard != null) replaceCard(advanceCard);
        advanceCard = in;
        retreatCard = null;
    }
    
    public void addAttackCard(Card in)
    {
        if(retreatCard != null) replaceCard(retreatCard);
        retreatCard = null;
        if(!attackList.isEmpty() && attackList.get(0).getValue() != in.getValue())
        {
            while(!attackList.isEmpty()) replaceCard(attackList.remove(0));
        }
        attackList.add(in);
    }
    
    public void clearActions()
    {
        if(retreatCard != null) replaceCard(retreatCard);
        if(advanceCard != null) replaceCard(advanceCard);
        retreatCard = null;
        advanceCard = null;
        while(!attackList.isEmpty()) replaceCard(attackList.remove(0));
    }
    
    public void trashActions()
    {
        retreatCard = null;
        advanceCard = null;
        attackList.clear();
    }
    
    public boolean isSlotEmpty(int in)
    {
        switch(slots[in])
        {
            case StripView.SLOT_RETREAT:
                return retreatCard == null;
            case StripView.SLOT_ADVANCE:
                return advanceCard == null;
            case StripView.SLOT_ATTACK:
                return attackList.isEmpty();
            default: return false;
        }
    }
    
    public void replaceCard(Card in)
    {
        game.getHand().replaceCard(in);
    }
    
    TextView header;
    TextView footer;
    private String headerString = "";
    private String footerString = "";
    private int footerColor = Game.COLOR_NONE;
    
    public void setHeader(String in)
    {
        headerString = in;
        header.setText(in);
    }
    
    public void setFooter(String in)
    {
        setFooter(in, Game.COLOR_NONE);
    }
    
    public void setFooter(String in, int textColor)
    {
        footerString = in;
        footer.setText(in);
        footerColor = textColor;
        footer.setTextColor(colorOf(textColor));
    }
    
    public int colorOf(int color)
    {
        switch(color)
        {
            case Game.COLOR_NONE:
                return Color.WHITE;
            case Game.COLOR_GREEN:
                return Color.GREEN;
            case Game.COLOR_PURPLE:
                return Color.MAGENTA;
            default: return Color.WHITE;
        }
    }
    
    public void refreshText()
    {
        footer.setText(footerString);
        header.setText(headerString);
    }
    
    private float downX = -1;
    private float downY = -1;
    public void setDown(float x, float y)
    {
        downX = x;
        downY = y;
    }
    public float getDownX() { return downX; }
    public float getDownY() { return downY; }
    
    public boolean isGoClick(float x, float y)
    {
        return
            x > goLeft && x < goRight && 
            y > goTop && y < goBottom &&
            downX > goLeft && downX < goRight && 
            downY > goTop && downY < goBottom;
    }
    
    public boolean isStopClick(float x, float y)
    {
        return
        x > stopLeft && x < stopRight && 
        y > stopTop && y < stopBottom &&
        downX > stopLeft && downX < stopRight && 
        downY > stopTop && downY < stopBottom;
    }
    
    public static final int ACTION_NONE = -1;
    public static final int ACTION_MOVE = 0;
    public static final int ACTION_ATTACK = 1;
    public static final int ACTION_PAT = 2;
    public static final int ACTION_PARRY = 3;
    public static final int ACTION_RETREAT = 4;
    public static final int ACTION_DISCONNECT = 5;
    
    private int parryValue = -1;
    private int parryCount = -1;
    private boolean mayRetreat = false;
    private int distance = -1;
    private int lastAction = ACTION_NONE;
    
    public void setDisconnect()
    {
        lastAction = ACTION_DISCONNECT;
    }
    
    public void setActionNewGame()
    {
        lastAction = ACTION_NONE;
    }
    
    public void setParryDone()
    {
        lastAction = ACTION_PARRY;
    }
    
    public void setParryNeeded(int value, int count, int distance)
    {
        parryValue = value;
        parryCount = count;
        this.distance = distance;
        mayRetreat = (distance != 0);
        if(mayRetreat) lastAction = ACTION_PAT;
        else lastAction = ACTION_ATTACK;
    }
    
    public void setMove(int distance)
    {
        this.distance = distance;
        lastAction = ACTION_MOVE;
    }
    
    public void setRetreat(int distance)
    {
        this.distance = distance;
        lastAction = ACTION_RETREAT;
    }
    
    public void displayLastAction()
    {
        String actor = (getLastActor() == Game.COLOR_PURPLE) ? "Purple" : "Green";
        switch(lastAction)
        {
            case ACTION_MOVE:
                setHeader(actor+" moved "+distance);
            break;
            case ACTION_RETREAT:
                setHeader(actor+" retreated "+distance);
            break;
            case ACTION_PARRY:
                setHeader(actor+" parried "+parryCount+" "+parryValue+"s");
            break;
            case ACTION_ATTACK:
                setHeader(actor+" attacked with "+attackStr());
            break;
            case ACTION_PAT:
                setHeader(actor+" jumped "+distance+" and attacked with "+attackStr());
            break;
            case ACTION_DISCONNECT:
                setHeader("The game has been cancelled");
            break;
        }
    }
    
    public void displayNextChoice()
    {
        switch(game.getTurn())
        {
            case Game.TURN_PURPLE_MOVE:
                setFooter("Purple's turn to move", Game.COLOR_PURPLE);
            break;
            case Game.TURN_PURPLE_PARRY:
                setFooter("Purple must parry", Game.COLOR_PURPLE);
            break;
            case Game.TURN_PURPLE_PARRY_OR_RETREAT:
                setFooter("Purple must parry or retreat", Game.COLOR_PURPLE);
            break;
            case Game.TURN_GREEN_MOVE:
                setFooter("Green's turn to move", Game.COLOR_GREEN);
            break;
            case Game.TURN_GREEN_PARRY:
                setFooter("Green must parry", Game.COLOR_GREEN);
            break;
            case Game.TURN_GREEN_PARRY_OR_RETREAT:
                setFooter("Green must parry or retreat", Game.COLOR_GREEN);
            break;
            case Game.TURN_GAME_OVER:
                setFooter(getGameOverClause(), Game.COLOR_NONE);
            break;
        }
    }
    
    private String attackStr()
    {
        switch(parryCount)
        {
            case 1:
                return "a "+parryValue;
            case 2:
                return "a pair of "+parryValue+"s";
            case 3:
                return "triple "+parryValue+"s";
            case 4:
                return "four "+parryValue+"s";
            case 5:
                return "five "+parryValue+"s";
            default: return "nothing";
        }
    }
    
    private int victor;
    private char endCause;
    
    public void setVictor(int color)
    {
        victor = color;
    }
    
    public int getVictor() { return victor; }
    
    public void setEndCause(char opcode)
    {
        endCause = opcode;
    }
    
    public char getEndCause() { return endCause; }
    
    private String getGameOverClause()
    {
        if(victor == Game.COLOR_NONE)
        {
            return "The game ended in a tie";
        }
        
        String win = (victor == Game.COLOR_PURPLE) ? "Purple" : "Green";
        String lose = (victor == Game.COLOR_PURPLE) ? "green" : "purple";
        
        String reason = "";
        switch(endCause)
        {
            case '0':
                reason = win+" wins because "+lose+" has backed off the strip";
            break;
            case '1':
                reason = win+" wins because "+lose+" could not parry";
            break;
            case '2':
                reason = win+" wins with more "+(game.purppos-game.greenpos)+"s";
            break;
            case '3':
                reason = win+" wins with the better final position";
            break;
            case 'L':
                reason = "Your opponent has lost connection";
            break;
        }
        
        return reason;
    }
    
    public boolean mayRetreat() { return mayRetreat; }
    public int getParryValue() { return parryValue; }
    public int getParryCount() { return parryCount; }
    public int getDistance() { return distance; }
    public int getLastAction() { return lastAction; }
    
    private int endGameLastActor = Game.COLOR_NONE;
    public void setEndGameLastActor(int lastActor) { endGameLastActor = lastActor; }
    
    public int getLastActor()
    {
        switch(game.getTurn())
        {
            case Game.TURN_PURPLE_PARRY:
            case Game.TURN_PURPLE_PARRY_OR_RETREAT:
                return Game.COLOR_GREEN;
            case Game.TURN_PURPLE_MOVE:
                return (lastAction == ACTION_PARRY) ? Game.COLOR_PURPLE : Game.COLOR_GREEN;
            case Game.TURN_GREEN_PARRY:
            case Game.TURN_GREEN_PARRY_OR_RETREAT:
                return Game.COLOR_PURPLE;
            case Game.TURN_GREEN_MOVE:
                return (lastAction == ACTION_PARRY) ? Game.COLOR_GREEN : Game.COLOR_PURPLE;
            case Game.TURN_GAME_OVER:
                return endGameLastActor;
        }
        return Game.COLOR_NONE;
    }
    
    private boolean finalParry = false;
    
    public boolean getFinalParry() { return finalParry; }
    
    public void setFinalParry(boolean b)
    {
        finalParry = b;
    }
}