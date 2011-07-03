package mel.fencing;

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
    
    public final Game getGame() { return game; }
    public final int getColor() {return color; }
    public final String getMyName() { return myName; }
    public final String getOppName() { return oppName; }
    public final int getState() { return state; }
    
    public final void setMyName(String in) { myName = in; }
    public final void setOppName(String in) { oppName = in; }
    public final void setColor(int in) { color = in; }
    public final void setState(int state) { this.state = state; }
}
