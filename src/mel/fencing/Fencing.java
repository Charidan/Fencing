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
    public static final int RETRY_LOGIN_DIALOG = 1;
    public static final int NEW_GAME_DIALOG = 2;
    
    TextView header;
    TextView footer;
    EditText usernameET = null;
    EditText newGameUsernameET = null;
    EditText passwordET;
    EditText retryUserNameET;
    EditText retryPasswordET;
    EditText hostET;
    Socket socket;
    AlertDialog connectDialog;
    AlertDialog retryLoginDialog;
    AlertDialog newGameDialog;
    
    private BufferedReader in;
    private PrintStream out;
    
    
    
    String host = "localhost";
    String username = "<empty>";
    String password = "<empty>";
    private boolean connected = false;
    private boolean loggedIn = false;
    private boolean tryingLogin = false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        header = (TextView) findViewById( R.id.header );
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
        try
        {            
            socket = new Socket(hostET.getText().toString(), PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream());
            connected = true;
            header.setText("Connection Successful!");
        }
        catch(IOException e)
        {
            header.setText("Connection Failed.");
            footer.setText(e.getMessage());
        }
        if(loggedIn) disconnect();
    }
    
    synchronized private boolean tryLogin()
    {
        out.println(username);
        out.println(password);
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
            disconnect();
            return false;
        }
        if(s.startsWith("L"))
        {
            footer.setText(s.substring(1)+" has logged in successfully.");
            startClientSession();
            return true;
        } else
        {
            footer.setText("Login failed with name \""+s.substring(1)+"\"");
            return false;
        }
    }
    
    private void startClientSession()
    {
        Runnable r = new Runnable()
        {
            public void run() 
            {
                boolean done = false;
                while(!done) 
                {
                    try
                    {
                        done = handleCommand(in.readLine());
                    } 
                    catch(IOException ex)
                    {
                        done = true;
                        footer.setText(ex.getMessage());
                    }
                }
                handleDisconnect();
            }
        };
        new Thread(r).start();
    }

    private void handleDisconnect()
    {
        // TODO - help! my server is gone
        header.setText("Server Connection Lost");
    }
    
    private boolean handleCommand(String command)
    {
        // TODO actually handle a server message
        footer.setText(command);
        return false;
    }
    
    private void newGame()
    {
        showDialog(NEW_GAME_DIALOG);
    }
    
    public void showHelp()
    {
        footer.setText("If you have to ask, you don't already know.");
    }
    
    synchronized private void disconnect()
    {
        connected = false;
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
        footer.setText("");
        header.setText("Disconnected");
    }
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch(id)
        {
            case CONNECT_DIALOG:
                return createConnectDialog();
            case RETRY_LOGIN_DIALOG:
                return createRetryLoginDialog();
            case NEW_GAME_DIALOG:
                return createNewGameDialog();
                //return createConnectDialog();
            default: return null;
            
        }
    }
    
    private Dialog createNewGameDialog()
    {
        LayoutInflater factory = LayoutInflater.from(this);
        View newGameDialogView = factory.inflate(R.layout.new_game_dialog, null);
        newGameDialog = new AlertDialog.Builder(Fencing.this)
             .setTitle("new game")
             .setCancelable(false)
             .setView(newGameDialogView)
             .setPositiveButton("Challenge User", 
                 new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which)
                     {
                         initNewGameDialogHandles();
                         out.println("NT"+newGameUsernameET.getText());
                         out.flush();
                     }
                 }
             )
             .setNeutralButton("Open Challenge",
                 new DialogInterface.OnClickListener()
                 {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        out.println("NO");
                        out.flush();
                    }
                }
            )
             .setNegativeButton("Cancel",
                 new DialogInterface.OnClickListener()
                 {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // do nothing  
                    }
                }
            )
            .create();
        return newGameDialog;
    }

    @Override 
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        
        if(connected && !loggedIn && !tryingLogin) 
        {
            tryingLogin = true;
            showDialog(RETRY_LOGIN_DIALOG);
            initRetryLoginDialogHandles();
            retryUserNameET.setText(username);
            retryPasswordET.setText("");
        }
    }
    
    private void initConnectDialogHandles()
    {
        if(usernameET != null) return;
        usernameET = (EditText) connectDialog.findViewById(R.id.username);
        passwordET = (EditText) connectDialog.findViewById(R.id.password);
        hostET = (EditText) connectDialog.findViewById(R.id.host);
    }
    
    private void initNewGameDialogHandles()
    {
        if(newGameUsernameET != null) return;
        newGameUsernameET = (EditText) newGameDialog.findViewById(R.id.new_game_username);
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
                         initConnectDialogHandles();
                         connect();
                         username = usernameET.getText().toString();
                         password = passwordET.getText().toString();
                         loggedIn = tryLogin();
                     } 
                 }
             )
             .setNegativeButton("Cancel",
                 new DialogInterface.OnClickListener()
                 {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // do nothing  
                    }
                }
            )
            .create();
        return connectDialog;
    }
    
    private void initRetryLoginDialogHandles()
    {
        if(retryUserNameET != null) return;
        retryUserNameET = (EditText) retryLoginDialog.findViewById(R.id.retry_username);
        retryPasswordET = (EditText) retryLoginDialog.findViewById(R.id.retry_password);
    }
    
    private Dialog createRetryLoginDialog()
    {
        LayoutInflater factory = LayoutInflater.from(this);
        View connectDialogView = factory.inflate(R.layout.retry_login_dialog, null); 
        retryLoginDialog = new AlertDialog.Builder(Fencing.this)
             .setTitle("retry login")
             .setCancelable(false)
             .setView(connectDialogView)
             .setPositiveButton("Login", 
                 new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which)
                     {
                         tryingLogin = false;
                         initRetryLoginDialogHandles();
                         username = retryUserNameET.getText().toString();
                         password = retryPasswordET.getText().toString();
                         loggedIn = tryLogin();
                     } 
                 }
             )
             .setNegativeButton("Disconnect",
                 new DialogInterface.OnClickListener()
                 {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {   
                        tryingLogin = false;
                        disconnect();
                    }
                }
            )
            .create();
        return retryLoginDialog;
    }
}