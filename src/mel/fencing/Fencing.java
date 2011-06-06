package mel.fencing;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class Fencing extends Activity
{
    Deck deck = new Deck();
    TextView header;
    TextView footer;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        header = (TextView) findViewById( R.id.header );
        header.setText(deck.toString());
        footer = (TextView) findViewById( R.id.footer );
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.newGame:
            newGame();
            return true;
        case R.id.help:
            showHelp();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void newGame()
    {
	footer.setText("Hoy, look, a New Game!");
	deck.shuffle();
	header.setText(deck.toString());
    }
    
    public void showHelp()
    {
	footer.setText("If you have to ask, you don't already know.");
    }
}