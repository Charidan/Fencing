package mel.fencing;

import java.util.ArrayList;
import java.util.List;
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
        retreatCard = in;
    }
    
    public void setAdvanceCard(Card in)
    {
        if(advanceCard != null) replaceCard(advanceCard);
        advanceCard = in;
    }
    
    public void addAttackCard(Card in)
    {
        if(!attackList.isEmpty() && attackList.get(0).getValue() != in.getValue())
        {
            while(!attackList.isEmpty()) replaceCard(attackList.remove(0));
        }
        attackList.add(in);
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
    
    public void setHeader(String in)
    {
        header.setText(in);
    }
    
    public void setFooter(String in)
    {
        footer.setText(in);
    }
}