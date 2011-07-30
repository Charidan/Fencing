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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Fencing extends Activity
{
    public static final String VERSION = "0.1.2";
    
    public static final int PORT = 9738;

    public static final int DIALOG_CONNECT = 0;
    public static final int DIALOG_RETRY_LOGIN = 1;
    public static final int DIALOG_NEW_GAME = 2;
    public static final int DIALOG_WAIT = 3;
    public static final int DIALOG_CHALLENGED = 4;
    
    public static final int MESSAGE_ERROR = 0;
    public static final int MESSAGE_COMMAND = 1;
    
    EditText usernameET = null;
    EditText newGameUsernameET = null;
    EditText passwordET;
    EditText retryUserNameET;
    EditText retryPasswordET;
    EditText hostET;
    TextView waitForTV;
    TextView challengedTV;
    Socket socket;
    AlertDialog connectDialog;
    AlertDialog retryLoginDialog;
    AlertDialog newGameDialog;
    AlertDialog waitDialog;
    AlertDialog challengedDialog;
    FencingHandler handler;
    StripView stripView;
    
    boolean refresh = false;
    
    private static BufferedReader in;
    private static PrintStream out;
    
    private static String username = "<empty>";
    private static String password = "<empty>";
    private static boolean connected = false;
    private static boolean loggedIn = false;
    private static boolean tryingLogin = false;
    private static boolean killed = false;
    private static boolean debug = false;
    
    static StripModel stripModel = new StripModel();
    
    private HashMap<Character,Command> opcode2Command = new HashMap<Character,Command>();
    private static Fencing singleton;
    
    
    public Fencing()
    {
        super();
        registerCommand('E', new ErrorCommand());
        registerCommand('W', new WaitCommand());
        registerCommand('T', new RecieveChallengeCommand());
        registerCommand('c', new RejectedCommand());
        registerCommand('C', new WithdrawnCommand());
        registerCommand('K', new KillCommand());
        registerCommand('b', new NewGameCommand(Game.COLOR_PURPLE));
        registerCommand('w', new NewGameCommand(Game.COLOR_GREEN));
        registerCommand('h', new SetHandCommand());
        registerCommand('x', new PositionCommand());
        registerCommand('t', new TurnCommand());
        registerCommand('q', new ParryNotice());
        registerCommand('a', new AttackNotice());
        registerCommand('m', new MoveNotice());
        registerCommand('r', new RetreatNotice());
        registerCommand('f', new FinalParryNotice());
        registerCommand('A', new EndGameNotice(Game.COLOR_GREEN));
        registerCommand('B', new EndGameNotice(Game.COLOR_PURPLE));
        registerCommand('X', new EndGameNotice(Game.COLOR_NONE));
        registerCommand('L', new LostConnectionNotice());
        singleton = this;
    }
    
    public static Fencing getSingleton() { return singleton; }
    
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
        stripModel.header = (TextView) findViewById( R.id.header );
        stripModel.footer = (TextView) findViewById( R.id.footer );
        stripView = (StripView) findViewById( R.id.Strip);
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
                if(debug) fakeGame();
                else newGame();
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
    
    private void fakeGame()
    {
        stripModel.setHeader("");
        stripModel.setFooter("");
        stripView.startGame(Game.COLOR_GREEN, "FakeOpponent");
        stripView.setHand("35313");
        stripModel.setState(StripModel.STATE_GAME);
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
            stripModel.setHeader("Connection Successful!");
        }
        catch(IOException e)
        {
            stripModel.setHeader("Connection Failed");
            stripModel.setFooter(e.getMessage());
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
            s = in.readLine();
        }
        catch (IOException e)
        {
            stripModel.setFooter(e.getMessage());
            disconnect();
            return false;
        }
        if(s.startsWith("L"))
        {
            stripModel.setFooter(s.substring(1)+" has logged in successfully");
            startClientSession();
            stripView.setMyName(username);
            return true;
        } else
        {
            stripModel.setFooter("Login failed with name \""+s.substring(1)+"\"");
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
                        if(s == null || s.startsWith("Z")) done = true;
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
                disconnect();
            }
        };
        new Thread(r).start();
    }
    
    private void newGame()
    {
        if(!connected)
        {
            stripModel.setFooter("Connect to server first");
            return;
        }
        showDialog(DIALOG_NEW_GAME);
    }
    
    public void showHelp()
    {
        stripModel.setHeader(VERSION+" by Mel and Richard Nicholson");
        stripModel.setFooter("Illustrated by Moira Nicholson");
    }
    
    synchronized private void disconnect()
    {
        if(killed)
        {
            stripModel.setFooter("You were logged out due to duplicate log in");
            killed = false;
        }
        connected = false;
        loggedIn = false;
        tryingLogin = false;
        stripModel.setState(StripModel.STATE_SPLASH);
        try
        {
            if(socket != null) socket.close();
        }
        catch (Exception e)
        {
            // ignore
        }
        socket = null;
        in = null;
        out = null;
        stripModel.setFooter("");
        stripModel.setHeader("Disconnected");
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
            case DIALOG_CHALLENGED:
                return createChallengedDialog();
            default: return null;
        }
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog)
    {
        super.onPrepareDialog(id, dialog);
        switch(id)
        {
            case DIALOG_CONNECT:
                initConnectDialogHandles();
            break;
            case DIALOG_RETRY_LOGIN:
                initRetryLoginDialogHandles();
            break;
            case DIALOG_NEW_GAME:
                initNewGameDialogHandles();
            break;
            case DIALOG_WAIT:
                initWaitHandles();
            break;
            case DIALOG_CHALLENGED:
                initChallengedHandles();
            break;
        }
    }
    
    private Dialog createNewGameDialog()
    {
        LayoutInflater factory = LayoutInflater.from(this);
        View newGameDialogView = factory.inflate(R.layout.new_game_dialog, null);
        newGameDialog = new AlertDialog.Builder(Fencing.this)
             .setTitle("New Game")
             .setCancelable(false)
             .setView(newGameDialogView)
             .setPositiveButton("Challenge User", 
                 new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which)
                     {
                         send("NT"+newGameUsernameET.getText());
                     }
                 }
             )
             .setNeutralButton("Open Challenge",
                 new DialogInterface.OnClickListener()
                 {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        send("NO");
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

    private Dialog createChallengedDialog()
    {
        LayoutInflater factory = LayoutInflater.from(this);
        View challengedDialogView = factory.inflate(R.layout.challenged_dialog, null);
        challengedDialog = new AlertDialog.Builder(Fencing.this)
             .setTitle("Challenge Recieved")
             .setCancelable(false)
             .setView(challengedDialogView)
             .setPositiveButton("Accept", 
                 new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which)
                     {
                         send("A");
                     }
                 }
             )
             .setNegativeButton("Reject",
                 new DialogInterface.OnClickListener()
                 {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        send("R"); 
                    }
                }
            )
            .create();
        return challengedDialog;
    }
    
    @Override 
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        
        if(connected && !loggedIn && !tryingLogin) 
        {
            tryingLogin = true;
            showDialog(DIALOG_RETRY_LOGIN);
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
        hostET.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event)
            {
                connectDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                return true;
            }
        });
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
             .setTitle("Login")
             .setCancelable(false)
             .setView(view)
             .setPositiveButton("Connect", 
                 new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int which)
                     {
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
        waitForTV = (TextView)waitDialog.findViewById(R.id.wait_for);
    }
    
    private void initChallengedHandles()
    {
        if(challengedTV != null) return;
        challengedTV = (TextView) challengedDialog.findViewById(R.id.challenged_by);
    }
    
    private Dialog createWaitDialog()
    {
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.wait_dialog, null); 
        waitDialog = new AlertDialog.Builder(Fencing.this)
            .setTitle("Awaiting Opponent")
            .setView(view)
            .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener()
                    {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            send("C");
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
    
    public void send(String what)
    {
        out.println(what);
        out.flush();
    }
    
    public class FencingHandler extends Handler
    {
        @Override
        public void handleMessage(Message m)
        {
            switch(m.what)
            {
                case MESSAGE_ERROR:
                    stripModel.setHeader("Exception Received");
                    Exception ex = (Exception) m.obj;
                    stripModel.setFooter(ex.getMessage());
                break;
                case MESSAGE_COMMAND:
                    String s = (String) m.obj;
                    if(s == null || s.length()<1) return; // ignore empty messages
                    Command c = opcode2Command.get(s.charAt(0));
                    if(c == null)
                    {
                        stripModel.setHeader("Unknown Server Command");
                        stripModel.setFooter(s);
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
            stripModel.setHeader("Server Error");
            stripModel.setFooter("Error: "+in);
        }
    }
    
    private class WaitCommand implements Command
    {
        @Override
        public void execute(String in)
        {
            showDialog(DIALOG_WAIT);
            waitForTV.setText("Waiting for "+in);
        }
    }
    
    private class RecieveChallengeCommand implements Command
    {
        @Override
        public void execute(String in)
        {
            showDialog(DIALOG_CHALLENGED);
            challengedTV.setText("Challenged by "+in);
        }
    }
    
    private class RejectedCommand implements Command
    {
        @Override
        public void execute(String in)
        {
            waitDialog.dismiss();
            stripModel.setFooter("Challenge Rejected");
        }
    }
    
    private class WithdrawnCommand implements Command
    {
        @Override
        public void execute(String in)
        {
            challengedDialog.dismiss();
            stripModel.setFooter("Challenge Withdrawn");
        }
    }
    
    private class KillCommand implements Command
    {
        @Override
        public void execute(String in)
        {
            killed = true;
        }
    }
    
    private class NewGameCommand implements Command
    {
        private int color;
        
        NewGameCommand(int color) { this.color = color; }
        
        @Override
        public void execute(String in)
        {
            stripModel.setHeader("");
            stripModel.setFooter("");
            stripModel.setActionNewGame();
            stripView.startGame(color, in);
            if(waitDialog != null) waitDialog.dismiss();
            stripModel.setState(StripModel.STATE_GAME);
        }
    }
    
    private class SetHandCommand implements Command
    {
        @Override
        public void execute(String in)
        {
            stripView.setHand(in);
        }
    }
    
    private class PositionCommand implements Command
    {
        @Override
        public void execute(String in)
        {
            stripView.setPositions(in);
        }
    }
    
    private class TurnCommand implements Command
    {
        @Override
        public void execute(String in)
        {
            //TODO show the attack cards in the footer inside this methos (they will be in the model)
            stripView.setTurn(in);
        }
    }
    
    private class ParryNotice implements Command
    {
        @Override
        public void execute(String in)
        {
            stripModel.setParryDone();
        }
    }
    
    private class AttackNotice implements Command
    {
        @Override
        public void execute(String in)
        {
            if(in.length() != 3) stripModel.setFooter("syntax error");
            stripModel.setParryNeeded(parseDigit(in.charAt(0)),parseDigit(in.charAt(1)),parseDigit(in.charAt(2)));
        }
    }
    
    private class MoveNotice implements Command
    {
        @Override
        public void execute(String in)
        {
            if(in.length() != 1) stripModel.setFooter("syntax error");
            stripModel.setMove(parseDigit(in.charAt(0)));
        }
    }
    
    private class RetreatNotice implements Command
    {
        @Override
        public void execute(String in)
        {
            if(in.length() != 1) stripModel.setFooter("syntax error");
            stripModel.setRetreat(parseDigit(in.charAt(0)));
        }
    }
    
    private class FinalParryNotice implements Command
    {
        @Override
        public void execute(String in)
        {
            stripModel.setFinalParry(true);
        }
    }
    
    private class EndGameNotice implements Command
    {
        private int victor;
        
        EndGameNotice(int victorColor) { victor = victorColor; }
        
        @Override
        public void execute(String in)
        {
            stripModel.setVictor(victor);
            stripModel.setEndCause(in.charAt(0));
            stripModel.setEndGameLastActor(stripModel.getLastActor());
            stripModel.getGame().setTurn(Game.TURN_GAME_OVER);
        }
    }
    
    private class LostConnectionNotice implements Command
    {        
        @Override
        public void execute(String in)
        {
            if(stripModel.getGame().getTurn() == Game.TURN_GAME_OVER) return;
            stripModel.setDisconnect();
            stripModel.setEndCause('L');
            stripModel.getGame().setTurn(Game.TURN_GAME_OVER);
        }
    }
    
    static private final int parseDigit(char in)
    {
        if(in<'0' || in > '9') return -1;
        return in-'0';
    }
}
