package mel.fencing;

public class StripModel
{
    private String myName = "Not Logged In";
    private String oppName = "";
    private int color = Game.COLOR_NONE;
    private Game game = new Game(); 
    
    public final Game getGame() { return game; }
    public final int getColor() {return color; }
    public final String getMyName() { return myName; }
    public final String getOppName() { return oppName; }
    
    public final void setMyName(String in) { myName = in; }
    public final void setOppName(String in) { oppName = in; }
    public final void setColor(int in) { color = in; }
}
