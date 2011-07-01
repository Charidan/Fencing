package mel.fencing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class StripView extends View
{
    public static final int MARGIN = 5;
    public static final int FENCER_SMALL = 20;
    public static final int FENCER_BIG = 34;
    public static final int STRIP_THICK = 2;
    
    private String myName = "Not Logged In";
    private String oppName = "Evil Bad Guy";
    private int color = Game.COLOR_NONE;
    private Game game = new Game(); 
    
    private boolean landscape = false;
    Paint textPaint;
    Paint linePaint;
    Paint whitePaint;
    Paint blackPaint;
    
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
        
        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setAntiAlias(true);
        whitePaint.setStyle(Style.FILL_AND_STROKE);
        
        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setAntiAlias(true);
        blackPaint.setStyle(Style.FILL_AND_STROKE);
    }
    
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
    public void onDraw(Canvas g)
    {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int step = (width-2*MARGIN)/23;
        int startX = (width+1-step*23)/2;
        Rect bounds = new Rect();
        
        String whiteName,blackName;
        if(color == Game.COLOR_BLACK)
        {
            whiteName = oppName;
            blackName = myName;
        } else
        {
            whiteName = myName;
            blackName = oppName;
        }
        
        textPaint.getTextBounds(whiteName, 0, whiteName.length(), bounds);
        g.drawText(whiteName, startX, bounds.height()+MARGIN, textPaint);
        textPaint.getTextBounds(blackName, 0, blackName.length(), bounds);
        g.drawText(blackName, width-startX-bounds.width(), bounds.height()+MARGIN, textPaint);
        
        int fencer = landscape ? FENCER_BIG : FENCER_SMALL;
        
        int stripTop = 2*MARGIN+bounds.height()+fencer;
        int whiteX = startX+(game.whitepos-1)*step;
        int blackX = startX+(game.blackpos-1)*step;            
        
        //draw the strip
        g.drawLine(startX, stripTop+fencer, startX+23*step, stripTop+fencer, linePaint);
        //draw the fencers
        //TODO replace with bitmaps
        g.drawRect(whiteX, stripTop, whiteX+fencer, stripTop+fencer, whitePaint);
        g.drawRect(blackX, stripTop, blackX+fencer, stripTop+fencer, blackPaint);
        
        if(landscape)
        {
            //TODO Draw buttons and cards
        } else
        {
            //TODO make real portait code
        }
    }
    
    public final void setMyName(String name)  { myName = name; }
    public final void setOppName(String name) { oppName = name; }
    public final void setMyColor(int color)   { this.color = color; }
}
