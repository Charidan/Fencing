package mel.fencing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Fencing extends Activity
{
    public static final int PORT = 9738;

    public static final int CONNECT_DIALOG = 0;
    
    Deck deck = new Deck();
    TextView header;
    TextView footer;
    EditText usernameTV = null;
    EditText passwordTV;
    EditText hostTV;
    Socket socket;
    private BufferedReader in;
    private PrintStream out;
    private boolean connected = false;
    AlertDialog connectDialog;
    int port = PORT;
    String host = "localhost";
    String username = "<empty>";
    String password = "<empty>";
    boolean abort = false;
    
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
                tryConnect();
                return true;
            case R.id.help:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void tryConnect()
    {
        if(connected) disconnect();
        showDialog(CONNECT_DIALOG);
        
    }
    
    private void connect()
    {
        if(usernameTV == null)
        {
            usernameTV = (EditText) connectDialog.findViewById(R.id.username);
            passwordTV = (EditText) connectDialog.findViewById(R.id.password);
            hostTV = (EditText) connectDialog.findViewById(R.id.host);
        }
        try
        {
            socket = new Socket(hostTV.getText().toString(), PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream());
            header.setText("Connection Successful!");
            while(!abort && loginFailed()) retryPassword();
        }
        catch(IOException e)
        {
            header.setText("Connection Failed.");
            footer.setText(e.getMessage());
        }
        if(abort) disconnect();
    }

    
    synchronized private boolean loginFailed()
    {
        out.println(usernameTV.getText());
        out.println(passwordTV.getText());
        out.flush();
        String s;
        try
        {
            // TODO check if login good or bad
            s = in.readLine();
        }
        catch (IOException e)
        {
            footer.setText(e.getMessage());
            return true;
        }
        if(s.startsWith("L"))
        {
            footer.setText(s.substring(1)+" has logged in successfully.");
            return false;
        } else
        {
            footer.setText("Login failed with name \""+s.substring(1)+"\"");
            return true;
        }
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
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch(id)
        {
            case CONNECT_DIALOG:
                return createConnectDialog();
            default: return null;
        }
    }
    
    private Dialog createConnectDialog()
    {
        LayoutInflater factory = LayoutInflater.from(this);
        View connectDialogView = factory.inflate(R.layout.connect_dialog, null);
        connectDialog = new AlertDialog.Builder(Fencing.this)
             .setTitle("login")
             .setCancelable(false)
             .setView(connectDialogView)
             .setPositiveButton("Connect", 
                 new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which)
                     {
                         //TODO load connect info
                         connect();
                     } 
                 }
             )
             .setNegativeButton("Cancel",
                 new DialogInterface.OnClickListener()
                 {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        abort = true;    
                    }
                }
            )
            .create();
        return connectDialog;
    }
}