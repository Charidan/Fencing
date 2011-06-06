package mel.fencing;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Fencing extends Activity
{
    Deck deck = new Deck();
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView tv = (TextView) findViewById( R.id.header );
        tv.setText(deck.toString());
    }
}