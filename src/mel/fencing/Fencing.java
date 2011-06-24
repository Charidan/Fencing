package mel.fencing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

    public static final int DIALOG_CONNECT = 0;
    public static final int DIALOG_RETRY_LOGIN = 1;
    public static final int DIALOG_NEW_GAME = 2;
    public static final int DIALOG_WAIT = 3;
    
    public static final int MESSAGE_ERROR = 0;
    public static final int MESSAGE_COMMAND = 1;
    
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_CHALLENGE_SENT = 2;
    
    TextView header;
    TextView footer;
    String headerText = "";
    String footerText = "";
    EditText usernameET = null;
    EditText newGameUsernameET = null;
    EditText passwordET;
    EditText retryUserNameET;
    EditText retryPasswordET;
    EditText hostET;
    TextView waitForTV;
    Socket socket;
    AlertDialog connectDialog;
    AlertDialog retryLoginDialog;
    AlertDialog newGameDialog;
    AlertDialog waitDialog;
    FencingHandler handler;
    int state = STATE_DISCONNECTED;
    
    boolean refresh = false;
    
    private BufferedReader in;
    private PrintStream out;
    
    String host = "localhost";
    String username = "<empty>";
    String password = "<empty>";
    private boolean connected = false;
    private boolean loggedIn = false;
    private boolean tryingLogin = false;
    
    private HashMap<Character,Command> opcode2Command = new HashMap<Character,Command>();
    
    public Fencing()
    {
        super();
        registerCommand('E', new ErrorCommand());
    }
    
    private void registerCommand(Character opcode, Command command)
    {
        opcode2Command.put(opcode, command);
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        handler = new FencingHandler();
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
        showDialog(DIALOG_CONNECT);        
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
                        String s = in.readLine();
                        if(s.startsWith("Z")) done = true;
                        Message m = Message.obtain(handler, MESSAGE_COMMAND, s);
                        handler.sendMessage(m);
                    } 
                    catch(Exception ex)
                    {
                        done = true;
                        Message m = Message.obtain(handler, MESSAGE_ERROR, ex);
                        handler.sendMessage(m);
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
    }
    
    private void newGame()
    {
        showDialog(DIALOG_NEW_GAME);
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
            case DIALOG_CONNECT:
                return createConnectDialog();
            case DIALOG_RETRY_LOGIN:
                return createRetryLoginDialog();
            case DIALOG_NEW_GAME:
                return createNewGameDialog();
            case DIALOG_WAIT:
                return createWaitDialog();
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
            showDialog(DIALOG_RETRY_LOGIN);
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
        View view = factory.inflate(R.layout.connect_dialog, null);
        connectDialog = new AlertDialog.Builder(Fencing.this)
             .setTitle("login")
             .setCancelable(false)
             .setView(view)
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
    
    private void initWaitHandles()
    {
        if(waitForTV != null) return;
        waitForTV = (TextView)waitDialog.findViewById(R.id.waitFor);
    }
    
    private Dialog createWaitDialog()
    {
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.wait_dialog, null); 
        Dialog waitDialog = new AlertDialog.Builder(Fencing.this)
            .setTitle("Awaiting Opponet")
            .setView(view)
            .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener()
                    {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // TODO send cancel message 
                            
                        }
                    }
            )
            .create();
         return waitDialog;
    }
    
    private Dialog createRetryLoginDialog()
    {
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.retry_login_dialog, null); 
        retryLoginDialog = new AlertDialog.Builder(Fencing.this)
             .setTitle("Retry Login")
             .setCancelable(false)
             .setView(view)
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
    
    public class FencingHandler extends Handler
    {
        @Override
        public void handleMessage(Message m)
        {
            switch(m.what)
            {
                case MESSAGE_ERROR:
                    header.setText("Exception Received");
                    Exception ex = (Exception) m.obj;
                    footer.setText(ex.getMessage());
                break;
                case MESSAGE_COMMAND:
                    header.setText("Command Received");
                    String s = (String) m.obj;
                    if(s == null || s.length()<1) return; // ignore empty messages
                    Command c = opcode2Command.get(s.charAt(0));
                    if(c == null)
                    {
                        header.setText("Unknown Sever Command");
                        footer.setText(s);
                    }
                    else c.execute(s.substring(1));
                break;
            }
        }
    }
    
    private class ErrorCommand implements Command
    {
        @Override
        public void execute(String in)
        {
            header.setText("Server Error");
            footer.setText("Error: "+in);
        }
    }
}