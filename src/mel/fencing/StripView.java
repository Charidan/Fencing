package mel.fencing;

import android.content.Context;
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
    Paint textPaint,linePaint,whitePaint,blackPaint,cardPaint;
    
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
        //TODO set up paints
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
        
        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setAntiAlias(true);
        whitePaint.setStyle(Style.FILL_AND_STROKE);
        
        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setAntiAlias(true);
        blackPaint.setStyle(Style.FILL_AND_STROKE);
        
        cardPaint = new Paint();
        cardPaint.setColor(Color.GRAY);
        cardPaint.setAntiAlias(true);
        cardPaint.setTextSize(50);
        cardPaint.setStyle(Style.STROKE);
    }
    
    private void resetModel() { model = Fencing.stripModel; }
    
    public void startGame(int color, String oppName)
    {
        setOppName(oppName);
        setMyColor(color);
    }
    
    @Override
    public void onMeasure(int x, int y)
    {
        super.onMeasure(x, y);
        landscape = x>y;
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
            break;
            
            case MotionEvent.ACTION_MOVE:
                if(model.isDragging()) animateDrag(e.getX(), e.getY());
            break;
            
            case MotionEvent.ACTION_UP:
                if(model.isDragging()) tryDrop(e.getX(), e.getY());
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
        
        // TODO remove card from hand -- takeCard() instead of getCard()
        Card card = model.getGame().getHand().getCard(slot);
        if(card == null) return;
        
        model.setDragging(true);
        model.setDragValue(card.getValue());
        model.setDragOffset(offsetX, offsetY);
        model.setDragPosition(offsetX, offsetY);
    }

    private void abortDrag()
    {
        // TODO return card to hand
        model.setDragging(false);   
    }

    private void tryDrop(float x, float y)
    {
        // TODO implement drop
        
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
        int step = (width-2*MARGIN)/23;
        int startX = (width+1-step*23)/2;
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
        
        String whiteName,blackName;
        if(model.getColor() == Game.COLOR_BLACK)
        {
            whiteName = model.getOppName();
            blackName = model.getMyName();
        } else
        {
            whiteName = model.getMyName();
            blackName = model.getOppName();
        }
        
        textPaint.getTextBounds(whiteName, 0, whiteName.length(), bounds);
        g.drawText(whiteName, startX, bounds.height()+MARGIN, textPaint);
        textPaint.getTextBounds(blackName, 0, blackName.length(), bounds);
        g.drawText(blackName, width-startX-bounds.width(), bounds.height()+MARGIN, textPaint);
        
        int fencer = landscape ? FENCER_BIG : FENCER_SMALL;
        
        //TODO RFE use a selector to remove the chance of accidental change to position
        int stripTop = 2*MARGIN+bounds.height()+fencer;
        int whiteX = startX+(model.getGame().whitepos-1)*step;
        int blackX = startX+(model.getGame().blackpos-1)*step;            
        
        //draw the strip
        g.drawLine(startX, stripTop+fencer, startX+23*step, stripTop+fencer, linePaint);
        //draw the fencers
        //TODO replace with bitmaps
        g.drawRect(whiteX, stripTop, whiteX+fencer, stripTop+fencer, whitePaint);
        g.drawRect(blackX, stripTop, blackX+fencer, stripTop+fencer, blackPaint);
        
        //draw position numbers
        int posTop = stripTop+fencer+MARGIN;
        String whitepos = ""+model.getGame().getWhitepos();
        String blackpos = ""+model.getGame().getBlackpos();
        textPaint.getTextBounds(whitepos, 0, whitepos.length(), bounds);
        g.drawText(whitepos, whiteX+(fencer-bounds.width()-1)/2, posTop+bounds.height(), textPaint);
        textPaint.getTextBounds(blackpos, 0, blackpos.length(), bounds);
        g.drawText(blackpos, blackX+(fencer-bounds.width()-1)/2, posTop+bounds.height(), textPaint);
        
        //draw buttons and cards
        float cardWidth,cardHeight,cardTop,cardBottom,cardLeft,cardStep;
        float goLeft, goTop, goRight, goBottom;
        float stopLeft, stopRight, stopTop, stopBottom;
        float actionLeft, actionTop, actionBottom, actionWidth, actionStep;
        
        if(landscape)
        {
            cardTop = stripTop+fencer+2*MARGIN+bounds.height();
            cardHeight = (height-cardTop-2*MARGIN_CARD-2*MARGIN)/2;
            cardWidth  = cardHeight*1000/1400;
            cardStep = cardWidth+MARGIN_CARD;
            cardLeft = (width-6*cardStep-cardWidth)/2;
            cardBottom = cardHeight+cardTop;
        } else
        {
            cardWidth  = (width-6*MARGIN_CARD)/5;
            cardHeight = cardWidth*1400/1000;
            cardTop = stripTop+fencer+4*MARGIN+bounds.height();
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
            cardPaint.getTextBounds(value, 0, value.length(), bounds);
            textOffsetX = (cardWidth-bounds.width())/2;
            textOffsetY = (cardHeight-bounds.height())/2;
            
            g.drawRect(cardX, cardTop, cardX+cardWidth, cardBottom, linePaint);
            g.drawText(value, cardX+textOffsetX, cardTop+textOffsetY+bounds.height(), cardPaint);
        }
        
        if(landscape)
        {
            goTop = cardTop;
            goBottom = cardBottom;
            goLeft = cardLeft + 5*cardStep + 2*MARGIN_CARD;
            goRight = goLeft + 2*cardWidth;
            stopTop = goBottom + MARGIN_CARD;
            stopBottom = stopTop + cardHeight + 2*MARGIN;
            stopLeft = goLeft;
            stopRight = goRight;
        } 
        else 
        {
            goTop = cardBottom+2*MARGIN_CARD+cardHeight+2*MARGIN;
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
            actionBottom = actionTop+cardHeight+2*MARGIN;
            actionLeft = cardLeft;
            actionWidth = (cardStep*5-3*MARGIN_CARD)/3;
            actionStep = actionWidth+MARGIN_CARD;
        }
        else
        {
            actionTop = cardBottom+MARGIN_CARD;
            actionBottom = actionTop+cardHeight+2*MARGIN;
            actionLeft = cardLeft;
            actionWidth = (cardStep*5-3*MARGIN_CARD)/3;
            actionStep = actionWidth+MARGIN_CARD;
        }
        
        for(int i=0; i<3; i++)
        {
            float actionX = actionLeft+actionStep*i;
            g.drawRect(actionX, actionTop, actionX+actionWidth, actionBottom, linePaint);
            //renderActionHolder(g, actionTop, actionX+actionWidth, actionBottom); 
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
    }

    public final void setMyName(String name)  { model.setMyName(name); }
    public final void setOppName(String name) { model.setOppName(name); }
    public final void setMyColor(int color)   { model.setColor(color); }

    synchronized public void setHand(String in)
    {
        resetModel();
        model.getGame().setHand(in);
    }

    @Override
    public void gameChanged()
    {
        postInvalidate();
    }
}
