package mel.fencing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class StripView extends View implements GameListener
{
    public static final int MARGIN = 5;
    public static final int MARGIN_CARD = 15;
    public static final int FENCER_SMALL = 20;
    public static final int FENCER_BIG = 34;
    public static final int STRIP_THICK = 2;
    
    public static final String SUBMIT = "GO";
    public static final String RESET = "RESET";
    public static final String FENCING = "FENCING";
    
    public StripModel model = null;
    
    private boolean landscape = false;
    Paint textPaint,linePaint,greenPaint,purpPaint,cardPaint;
    
    public StripView(Context context)
    {
        super(context);
        init();
    }
    
    public StripView(Context context, AttributeSet attr)
    {
        super(context, attr);
        init();
    }
    
    void init()
    {
        textPaint = new Paint();
        textPaint.setColor(Color.GRAY);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(20);
        textPaint.setStyle(Style.FILL_AND_STROKE);
        
        linePaint = new Paint();
        linePaint.setColor(Color.GRAY);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(STRIP_THICK);
        linePaint.setStyle(Style.STROKE);
        
        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setAntiAlias(true);
        greenPaint.setStyle(Style.FILL_AND_STROKE);
        
        purpPaint = new Paint();
        purpPaint.setColor(Color.MAGENTA);
        purpPaint.setAntiAlias(true);
        purpPaint.setStyle(Style.FILL_AND_STROKE);
        
        cardPaint = new Paint();
        cardPaint.setColor(Color.GRAY);
        cardPaint.setAntiAlias(true);
        cardPaint.setTextSize(50);
        cardPaint.setStyle(Style.STROKE);
    }
    
    private void resetModel() { model = Fencing.stripModel; model.getGame().addListener(this); }
    
    public void startGame(int color, String oppName)
    {
        setOppName(oppName);
        setMyColor(color);
        model.getGame().reset();
    }
    
    @Override
    public void onMeasure(int x, int y)
    {
        super.onMeasure(x, y);
        landscape = x>y;
        resetModel();
        model.refreshText();
    }
    
    @Override
    synchronized public boolean onTouchEvent(MotionEvent e)
    {
        switch(e.getAction())
        {
            case MotionEvent.ACTION_CANCEL:
                if(model.isDragging()) abortDrag();
            break;
            
            case MotionEvent.ACTION_DOWN:
                if(model.isDragging()) abortDrag();
                tryGrab(e.getX(), e.getY());
                model.setDown(e.getX(), e.getY());
            break;
            
            case MotionEvent.ACTION_MOVE:
                if(model.isDragging()) animateDrag(e.getX(), e.getY());
            break;
            
            case MotionEvent.ACTION_UP:
                if(model.isDragging()) tryDrop(e.getX(), e.getY());
                tryClick(e.getX(), e.getY());
            break;
        }
        // opaque to touch
        return true;
    }
    
    private void tryGrab(float x, float y)
    {
        // if (x,y) is over a card, start dragging it
        if(y<model.getCardTop() || y>model.getCardBottom() || x<model.getCardLeft() ) return;
        x -= model.getCardLeft();
        float offsetX = x % model.getCardStep();
        float offsetY = y - model.getCardTop();
        int slot = (int)(x/model.getCardStep());
        if(offsetX > model.getCardWidth()) return;
        
        Card card = model.getGame().getHand().takeCard(slot);
        if(card == null) return;
        
        model.setDragging(true);
        model.setDragCard(card);
        model.setDragOffset(offsetX, offsetY);
        model.setDragPosition(offsetX, offsetY);
    }
    
    /**
     * must be called from GUI thread
     */
    private void tryClick(float x, float y)
    {
        // TODO RFE if each clickable had an object that implemented a standard interface (like View or even just BoundaryRectangle)
        // we could just loop through the list of clickables instead of writing a different boundary check for each
        // since there are only two buttons in our GUI, we can ignore that for now
        if(model.isGoClick(x, y)) clickGo();
        else if (model.isStopClick(x, y)) clickStop();
    }
    
    /**
     * must be called from GUI thread
     */
    private void clickGo()
    {
        int turn = model.getTurn();
        // if it's not your turn
        switch(turn)
        {
            case Game.TURN_PURPLE_MOVE: 
            case Game.TURN_PURPLE_PARRY:
            case Game.TURN_PURPLE_PARRY_OR_RETREAT:
                if(model.getColor() == Game.COLOR_GREEN) { model.setHeader("Please wait your turn"); return; }
            break;
            
            case Game.TURN_GREEN_MOVE:
            case Game.TURN_GREEN_PARRY:
            case Game.TURN_GREEN_PARRY_OR_RETREAT:
                if(model.getColor() == Game.COLOR_PURPLE) { model.setHeader("Please wait your turn"); return; }
            break;
        }
        
        String retreatValue = model.getRetreatCard() == null ? "*" : model.getRetreatCard().toString();
        String advanceValue = model.getAdvanceCard() == null ? "*" : model.getAdvanceCard().toString();
        String attackValue = model.getAttackList().isEmpty() ? "*" : model.getAttackList().get(0).toString();
        String attackCount = ""+model.getAttackList().size();
        
        // this is a useless comment
        if(turn == Game.TURN_GREEN_MOVE || turn == Game.TURN_PURPLE_MOVE)
        {
            //advance options
            if(!advanceValue.equals("*"))
            {
                if(!attackValue.equals("*"))
                {
                    //patenandu
                    send("p"+advanceValue+attackValue+attackCount);
                    return;
                } else
                {
                    //normal advance
                    send("m"+advanceValue);
                    return;
                }
            } 
            
            if(!attackValue.equals("*"))
            {
                //standing attack
                send("a"+attackValue+attackCount);
                return;
            }
            
            if(!retreatValue.equals("*"))
            {   
                //retreat
                send("r"+retreatValue);
                return;
            }
        }
        
        if(turn == Game.TURN_GREEN_PARRY || turn == Game.TURN_PURPLE_PARRY)
        {
            // parry attack
            if (model.getAttackList().size() == model.getParryCount() && 
                model.getAttackList().get(0).getValue() == model.getParryValue())        
            {
                if(!advanceValue.equals("*"))
                {
                    model.setHeader("Defend before advancing");
                    return;
                }
                model.trashActions();
                send("q");
                return;
            }
            else
            {
                model.setHeader("Wrong cards for parry");
                return;
            }
        }
        
        if(turn == Game.TURN_GREEN_PARRY_OR_RETREAT || turn == Game.TURN_PURPLE_PARRY_OR_RETREAT)
        {
            if(!retreatValue.equals("*"))
            {   
                //retreat
                send("r"+retreatValue);
                return;
            }
            if (model.getAttackList().size() == model.getParryCount() && 
                model.getAttackList().get(0).getValue() == model.getParryValue())        
            {
                if (!advanceValue.equals("*"))
                {
                    model.setHeader("Defend before advancing");
                    return;
                }
                model.trashActions();
                send("q");
                return;
            }
            else
            {
                model.setHeader("Wrong cards for parry");
                return;
            }
        }
        
        model.setHeader("Illegal move submission");
    }
    
    /**
     * must be called from GUI thread
     */
    private void clickStop()
    {
        model.clearActions();
        model.displayLastAction();
        model.displayNextChoice();
        invalidate();
    }

    private void abortDrag()
    {
        model.setDragging(false);
        model.replaceCard(model.getDragCard());
        model.setDragCard(null);
        invalidate();
    }
    
    private void stopDrag()
    {
        model.setDragging(false);
        model.setDragCard(null);
        invalidate();
    }
    
    private void tryDrop(float x, float y)
    {
        int slot = getActionSlot(x, y);
        
        switch(slot)
        {
            //TODO check for legal moves
            case SLOT_RETREAT:
                model.setRetreatCard(model.getDragCard());
                stopDrag();
            return;
            case SLOT_ADVANCE:
                model.setAdvanceCard(model.getDragCard());
                stopDrag();
            return;
            case SLOT_ATTACK:
                model.addAttackCard(model.getDragCard());
                stopDrag();
            return;
            case -1:
                abortDrag();
            break;
        }
    }
    
    /**
     * @return find the slot# of the action slot at (x,y) or -1 if not on a slot
     */
    private int getActionSlot(float x, float y)
    {
        if(y<model.getActionTop() || y>model.getActionBottom() || x<model.getActionLeft() ) return -1;
        x -= model.getActionLeft();
        float offsetX = x % model.getActionStep();
        int slot = (int)(x/model.getActionStep());
        if(offsetX > model.getActionWidth()) return -1;
        if(slot >= model.slots.length) return -1;
        return model.slots[slot];
    }

    private void animateDrag(float x, float y)
    {
        model.setDragPosition(x, y);
        invalidate();
    }

    @Override
    synchronized public void onDraw(Canvas g)
    {
        resetModel();
        
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        float step = landscape ? (width-2*MARGIN)/23*FENCER_SMALL/FENCER_BIG : (width-2*MARGIN)/23;
        float startX = (width+1-step*23)/2;
        Rect bounds = new Rect();
        float textOffsetX, textOffsetY;
        
        //Splash Screen
        if(model.getState() == StripModel.STATE_SPLASH)
        {
            //TODO replace with splash picture
            cardPaint.getTextBounds(FENCING, 0, FENCING.length(), bounds);
            textOffsetX = (width-bounds.width())/2;
            textOffsetY = (height-bounds.height())/2;
            g.drawText(FENCING, textOffsetX, textOffsetY, cardPaint);
            return;
        }
        
        String greenName,purpName;
        if(model.getColor() == Game.COLOR_PURPLE)
        {
            greenName = model.getOppName();
            purpName = model.getMyName();
        } else
        {
            greenName = model.getMyName();
            purpName = model.getOppName();
        }
        
        textPaint.getTextBounds(greenName, 0, greenName.length(), bounds);
        g.drawText(greenName, startX, bounds.height()+MARGIN, textPaint);
        textPaint.getTextBounds(purpName, 0, purpName.length(), bounds);
        g.drawText(purpName, width-startX-bounds.width(), bounds.height()+MARGIN, textPaint);
        
        int fencerSize = landscape ? FENCER_BIG : FENCER_BIG;
        Bitmap fencerGreen, fencerPurple;
        if(fencerSize == FENCER_BIG)
        {
            fencerGreen = BitmapFactory.decodeResource(getResources(), R.drawable.greenfencer34);
            fencerPurple = BitmapFactory.decodeResource(getResources(), R.drawable.purpfencer34);
        }
        else
        {
            fencerGreen = BitmapFactory.decodeResource(getResources(), R.drawable.greenfencer20);
            fencerPurple = BitmapFactory.decodeResource(getResources(), R.drawable.purpfencer20);
        }
        
        //TODO RFE use a selector to remove the chance of accidental change to position
        int stripTop = 2*MARGIN+bounds.height()+fencerSize;
        float greenX = startX+(model.getGame().greenpos-1)*step;
        float purpX = startX+(model.getGame().purppos-1)*step;            
        
        //draw the strip
        g.drawLine(startX, stripTop+fencerSize, startX+23*step, stripTop+fencerSize, linePaint);
        //draw the fencers
        g.drawBitmap(fencerGreen, greenX, stripTop, linePaint);
        g.drawBitmap(fencerPurple, purpX, stripTop, linePaint);
        
        //draw position numbers
        int posTop = stripTop+fencerSize+MARGIN;
        String greenpos = ""+model.getGame().getGreenpos();
        String purppos = ""+model.getGame().getPurppos();
        textPaint.getTextBounds(greenpos, 0, greenpos.length(), bounds);
        g.drawText(greenpos, greenX+(fencerSize-bounds.width()-1)/2, posTop+bounds.height(), textPaint);
        textPaint.getTextBounds(purppos, 0, purppos.length(), bounds);
        g.drawText(purppos, purpX+(fencerSize-bounds.width()-1)/2, posTop+bounds.height(), textPaint);
        
        //draw buttons and cards
        float cardWidth,cardHeight,cardTop,cardBottom,cardLeft,cardStep;
        float goLeft, goTop, goRight, goBottom;
        float stopLeft, stopRight, stopTop, stopBottom;
        float actionLeft, actionTop, actionBottom, actionWidth, actionStep;
        
        if(landscape)
        {
            cardTop = stripTop+fencerSize+2*MARGIN+bounds.height();
            cardHeight = (height-cardTop-2*MARGIN_CARD-2*MARGIN-4*MARGIN_SHADOW)/2;
            cardWidth  = cardHeight*1000/1400;
            cardStep = cardWidth+MARGIN_CARD;
            cardLeft = (width-6*cardStep-cardWidth)/2;
            cardBottom = cardHeight+cardTop;
        } else
        {
            cardWidth  = (width-6*MARGIN_CARD)/5;
            cardHeight = cardWidth*1400/1000;
            cardTop = stripTop+fencerSize+4*MARGIN+bounds.height();
            cardStep = cardWidth+MARGIN_CARD;
            cardLeft = (width-4*cardStep-cardWidth)/2;
            cardBottom = cardHeight+cardTop;
        }       
        
        for(int i = 0; i < Hand.HAND_SIZE; i++)
        {
            Card c = model.getGame().getHand().getCard(i);
            if(c == null) continue;
            float cardX = cardLeft+cardStep*i;
            String value = c.toString();

            renderCard(g, value, 1, cardX, cardTop, cardWidth, cardHeight);
        }
        
        if(landscape)
        {
            goTop = cardTop;
            goBottom = cardBottom;
            goLeft = cardLeft + 5*cardStep + 2*MARGIN_CARD;
            goRight = goLeft + 2*cardStep;
            stopTop = goBottom + MARGIN_CARD;
            stopBottom = stopTop + cardHeight + 2*MARGIN + 4*MARGIN_SHADOW;
            stopLeft = goLeft;
            stopRight = goRight;
        } 
        else 
        {
            goTop = cardBottom+2*MARGIN_CARD+cardHeight+2*MARGIN+4*MARGIN_SHADOW;
            goBottom = goTop+cardHeight+2*MARGIN;
            goRight = width/2 - MARGIN_CARD/2;
            goLeft = MARGIN_CARD;
            stopTop = goTop;
            stopBottom = goBottom;
            stopLeft = width/2 + MARGIN_CARD/2;
            stopRight = width - MARGIN_CARD;
        }
        
        cardPaint.getTextBounds(SUBMIT, 0, SUBMIT.length(), bounds);
        textOffsetX = (goRight-goLeft-bounds.width())/2;
        textOffsetY = (goBottom-goTop-bounds.height())/2;
        g.drawRect(goLeft, goTop, goRight, goBottom, linePaint); 
        g.drawText(SUBMIT, goLeft+textOffsetX, goTop+textOffsetY+bounds.height(), cardPaint);
        
        cardPaint.getTextBounds(RESET, 0, RESET.length(), bounds);
        textOffsetX = (stopRight-stopLeft-bounds.width())/2;
        textOffsetY = (stopBottom-stopTop-bounds.height())/2;
        g.drawRect(stopLeft, stopTop, stopRight, stopBottom, linePaint); 
        g.drawText(RESET, stopLeft+textOffsetX, stopTop+textOffsetY+bounds.height(), cardPaint);
        
        // draw action spaces for holding cards
        if(landscape)
        {
            actionTop = goBottom + MARGIN_CARD;
            actionBottom = stopBottom;
            actionLeft = cardLeft;
            actionWidth = (cardStep*5-3*MARGIN_CARD)/3;
            actionStep = actionWidth+MARGIN_CARD;
        }
        else
        {
            actionTop = cardBottom+MARGIN_CARD;
            actionBottom = actionTop+cardHeight+2*MARGIN+4*MARGIN_SHADOW;
            actionLeft = cardLeft;
            actionWidth = (cardStep*5-3*MARGIN_CARD)/3;
            actionStep = actionWidth+MARGIN_CARD;
        }
        
      //save screen position in case of a touch event
        model.setCardWidth(cardWidth);
        model.setCardHeight(cardHeight);
        model.setCardTop(cardTop);
        model.setCardBottom(cardBottom);
        model.setCardLeft(cardLeft);
        model.setCardStep(cardStep);
        model.setGoLeft(goLeft);
        model.setGoTop(goTop);
        model.setGoRight(goRight);
        model.setGoBottom(goBottom);
        model.setStopLeft(goLeft);
        model.setStopRight(stopRight);
        model.setStopTop(stopTop);
        model.setStopBottom(stopBottom);
        model.setActionLeft(actionLeft);
        model.setActionTop(actionTop);
        model.setActionBottom(actionBottom);
        model.setActionWidth(actionWidth);
        model.setActionStep(actionStep);
        
        for(int i=0; i<3; i++)
        {
            float actionX = actionLeft+actionStep*i;
            renderActionHolder(g, i, actionX, actionTop, actionX+actionWidth, actionBottom); 
        }
        
        if(model.isDragging()) 
        {
            // TODO consider a new paint for the dragging rectangle
            float x = model.getDragPositionX() - model.getDragOffsetX();
            float y = model.getDragPositionY() - model.getDragOffsetY();
            g.drawRect(x, y, x+cardWidth, y+cardHeight, linePaint);
            String val = ""+model.getDragValue();
            cardPaint.getTextBounds(val, 0, val.length(), bounds);
            textOffsetX = (cardWidth-bounds.width())/2;
            textOffsetY = (cardHeight-bounds.height())/2+bounds.height();
            g.drawText(val, x+textOffsetX, y+textOffsetY, cardPaint);
        }
    }

    public static final int SLOT_RETREAT = 0;
    public static final int SLOT_ADVANCE = 1;
    public static final int SLOT_ATTACK = 2;
    // TODO replace these with pictures
    public static final String PIC_LEFT = "<==";
    public static final String PIC_RIGHT = "==>";
    public static final String PIC_ATTACK = "+";
    public static final int leftSlots[] = { SLOT_RETREAT, SLOT_ADVANCE, SLOT_ATTACK };
    public static final String leftPics[] = { PIC_LEFT, PIC_RIGHT, PIC_ATTACK };
    public static final int rightSlots[] = { SLOT_ATTACK, SLOT_ADVANCE, SLOT_RETREAT };
    public static final String rightPics[] = { PIC_ATTACK, PIC_LEFT, PIC_RIGHT };
    
    private void renderActionHolder(Canvas g, int slot, float left, float top, float right, float bottom)
    {
        Rect bounds = new Rect();
        float textOffsetX, textOffsetY;
        
        g.drawRect(left, top, right, bottom, linePaint);
        
        if(model.isSlotEmpty(slot))
        {
            //if the slot is empty, draw the slot picture
            cardPaint.getTextBounds(model.pics[slot], 0, model.pics[slot].length(), bounds);
            textOffsetY = (bottom-top-bounds.height())/2;
            textOffsetX = (right-left-bounds.width())/2;
            g.drawText(model.pics[slot], left+textOffsetX, top+textOffsetY+bounds.height(), cardPaint);
        }
        else
        {
            Card c = null;
            int shadowCount = 0;
            switch(model.slots[slot])
            {
                case StripView.SLOT_RETREAT:
                    c = model.getRetreatCard();
                break;
                case StripView.SLOT_ADVANCE:
                    c = model.getAdvanceCard();
                break;
                case StripView.SLOT_ATTACK:
                    c = model.getAttackList().get(0);
                    shadowCount = model.getAttackList().size();
                break;
            }
            
            float actionWidth = right-left;
            float actionHeight = bottom-top;
            float cardLeft = left+(actionWidth-model.getCardWidth())/2;
            float cardTop = top+(actionHeight-model.getCardHeight())/2;
            
            renderCard(g, c.toString(), shadowCount, cardLeft, cardTop, model.getCardWidth(), model.getCardHeight());
        }
            
    }
    
    private static final int MARGIN_SHADOW = 5;
    private void renderCard(Canvas g, String value, int cardCount, float left, float top, float width, float height)
    {
        top += (cardCount-1)*MARGIN_SHADOW/2;
        left -= (cardCount-1)*MARGIN_SHADOW/2;
        Rect bounds = new Rect();
        cardPaint.getTextBounds(value, 0, value.length(), bounds);
        float textOffsetX = (width-bounds.width())/2;
        float textOffsetY = (height-bounds.height())/2;
        float cardBottom = top+height;
        
        g.drawRect(left, top, left+width, cardBottom, linePaint);
        g.drawText(value, left+textOffsetX, top+textOffsetY+bounds.height(), cardPaint);
        
        for(int i=1; i<cardCount; i++) 
        {
            float offset = i*MARGIN_SHADOW;
            float shadowTop = top-offset;
            float shadowLeft = left+offset;
            float shadowRight = shadowLeft+width;
            float shadowBottom = shadowTop+height;
            g.drawLine(shadowLeft, shadowTop, shadowLeft, shadowTop+MARGIN_SHADOW, linePaint);
            g.drawLine(shadowLeft, shadowTop, shadowRight, shadowTop, linePaint);
            g.drawLine(shadowRight, shadowTop, shadowRight, shadowBottom, linePaint);
            g.drawLine(shadowRight-MARGIN_SHADOW, shadowBottom, shadowRight, shadowBottom, linePaint);
        }
    }

    public final void setMyName(String name)  { model.setMyName(name); }
    public final void setOppName(String name) { model.setOppName(name); }
    public final void setMyColor(int color)   
    { 
        model.setColor(color); 
        if(model.getColor() == Game.COLOR_GREEN) { model.slots = leftSlots; model.pics = leftPics; }
        else { model.slots = rightSlots; model.pics = rightPics; }
    }

    synchronized public void setHand(String in)
    {
        resetModel();
        model.clearActions();
        model.getGame().setHand(in);
    }

    @Override
    public void gameChanged()
    {
        model.displayLastAction();
        model.displayNextChoice();
        postInvalidate();
    }
    
    private void send(String in)
    {
        Fencing.getSingleton().send(in);
    }

    public void setPositions(String in)
    {
        resetModel();
        model.getGame().setPositions(in);
    }
    
    public void setTurn(String in)
    {
        resetModel();
        try
        {
            model.getGame().setTurn(Integer.parseInt(in));
        }
        catch(NumberFormatException e)
        {
            model.setFooter("Garbled turn number from server: "+in);
        }
    }
}
