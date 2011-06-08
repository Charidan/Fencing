package mel.fencing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class Fencing extends Activity
{
    public static final int PORT = 9738;
    
    Deck deck = new Deck();
    TextView header;
    TextView footer;
    Socket socket;
    private BufferedReader in;
    private PrintStream out;
    private boolean connected = false;
    
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
            case R.id.newGame:
                newGame();
                return true;
            case R.id.conServer:
                connect();
                return true;
            case R.id.help:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void connect()
    {
        if(connected) disconnect();
        //TODO show dialog to get host, port, username, password
        try
        {
            socket = new Socket("localhost", PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream());
            footer.setText("Connection Successful!");
            while(loginFailed()) retryPassword();
        }
        catch(IOException e)
        {
            header.setText("Connection Failed.");
            footer.setText(e.getMessage());
        }
    }

    
    synchronized private boolean loginFailed()
    {
        // TODO get a line from server and check if login good or bad
        return true;
    }
    
    private void retryPassword()
    {
        // TODO show a dialog to get new user/password
        // TODO seed with old username
    }
    
    private void newGame()
    {
	footer.setText("Hoy, look, a New Game!");
	deck.shuffle();
	header.setText(deck.toString());
    }
    
    public void showHelp()
    {
	footer.setText("If you have to ask, you don't already know.");
    }
    
    synchronized private void disconnect()
    {
        try
        {
            out.close();
            in.close();
            socket.close();
        }
        catch (IOException e)
        {
            // ignore
        }
        socket = null;
        in = null;
        out = null;
    }
}