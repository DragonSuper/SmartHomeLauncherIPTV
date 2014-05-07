package atn.ontheblue.iptv.launcher;


import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;

public class HomeActivity extends Activity implements Callback, OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener {	
	
	//Constants
	public static final String PREFS_NAME = "ATNIPTV";
	final private String TAG = "ATN IPTV HL -- Main";
	Resources res;
	public HomeActivity context;
	public IPTV iptv;
	public boolean boot = true;
	
	//Connectivity
	private ConnectivityListener connListener = null;	
    private IntentFilter connIntentFilter = null;
    private Boolean connIntentFilterIsRegistered = false;
    //Applications
    private ApplicationsListener appsListener = null;
    private IntentFilter appsIntentFilter = null;
    private Boolean appsIntentFilterIsRegistered = false;
    
    //Menu
    public View cnt_menu;
    String[] menus;
    
    //Applications
    public GridView apps;
    
    //SETTINGS LAYOUT
    private View settings;
    private Button wifiSettings;
    private Button btnLogin;
    private ToggleButton btnChannelIcons;
    public boolean channelsAsIcons;
    
    //PROGRAM LAYOUT
    public GridView programLayout;    
    public channelAdapter channelAdapter;
    
    //FAVORITES LAYOUT
    public GridView favoritesLayout;    
    public favoritesAdapter favoritesAdapter;
    
    //Video Player
    private MediaPlayer mMediaPlayer;
    public SurfaceView mPreview;
    private SurfaceHolder holder;
    private boolean mIsVideoSizeKnown = false;
    private boolean mIsVideoReadyToBePlayed = false;
    public boolean mIsAvailableTostream=false;
    private int mVideoWidth;
    private int mVideoHeight;
    private int width=0;
    private int height=0;
    
    //Channels
    public String toChannel="";
    private boolean chn = false;    
    //Channel Info
    private View channelInfo;
    private TextView currentChannel;
    private TextView channelName;
    //Channel Toast
    private Toast channelNr;
    private TextView chnNr;
    
    //UPDATER
    private autoUpdate autoUpdate;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	this.context=this;
    	Log.i(TAG,"Application Started...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        this.res = getResources();
        this.menus = res.getStringArray(R.array.menu_items);
        //Connectivity
        connIntentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        connIntentFilter.addAction("android.net.conn.EXTRA_NO_CONNECTIVITY ");
        connIntentFilter.addAction("android.net.conn.EXTRA_NETWORK_INFO ");
        connIntentFilter.addAction("android.net.conn.EXTRA_IS_FAILOVER ");
        connIntentFilter.addAction("android.net.conn.EXTRA_EXTRA_INFO ");
        connIntentFilter.addAction("android.net.conn.EXTRA_OTHER_NETWORK_INFO ");
        connIntentFilter.addAction("android.net.conn.EXTRA_REASON ");
        
        connListener = new ConnectivityListener(this);
        registerReceiver(connListener, connIntentFilter);
        //Applications
        appsIntentFilter = new IntentFilter();
        appsListener = new ApplicationsListener(this);
        appsIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appsIntentFilter.addDataScheme("package");
        registerReceiver(appsListener, appsIntentFilter);        
        initMenu();
        initApps();
        initSettings();
        initPreferences();
        initProgramList();
        initFavoriteList();
        initChannelInfo();
        initVideo();
    }
    @Override
	protected void onPause() {
    	Log.d(TAG,"onPause have been called");
		/*if (!connIntentFilterIsRegistered) {
			Log.d(TAG,"onPause registerreciver conn");
            registerReceiver(connListener, connIntentFilter);
            connIntentFilterIsRegistered = true;
        }
		if (!appsIntentFilterIsRegistered) {
            registerReceiver(appsListener, appsIntentFilter);
            appsIntentFilterIsRegistered = true;
        }*/
		//releaseMediaPlayer();
		//doCleanUp();
    	if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
    		//mMediaPlayer.pause();
    		releaseMediaPlayer();
    		doCleanUp();
    	}    	
		super.onPause();
	}
	@Override
	protected void onResume() {
		Log.d(TAG,"onResume have been called");
		if (connIntentFilterIsRegistered) {
            unregisterReceiver(connListener);
            connIntentFilterIsRegistered = false;
        }
		if (appsIntentFilterIsRegistered) {
            unregisterReceiver(appsListener);
            appsIntentFilterIsRegistered = false;
        }
		//if(mMediaPlayer!=null){
			playChannel();
		//}
		super.onResume();
	}
	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG,"onNewIntent have been called");		
		super.onNewIntent(intent);
	}
	@Override
	protected void onDestroy() {		
		if (connIntentFilterIsRegistered) {
            unregisterReceiver(connListener);
            connIntentFilterIsRegistered = false;
        }
		if (appsIntentFilterIsRegistered) {
            unregisterReceiver(appsListener);
            appsIntentFilterIsRegistered = false;
        }
		releaseMediaPlayer();
		doCleanUp();
		super.onDestroy();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//prevent Back Button closing application
		if (event.getKeyCode()==KeyEvent.KEYCODE_BACK) {
            return true;
        }
		Log.i(TAG,"keycode : "+keyCode+" keyEnvent : "+event.getAction()+" and "+event.getNumber()+" "+event.getCharacters());		
		if(IPTV.isNumeric(event.getNumber()+"")){
			Log.i(TAG,"A number has been pressed "+event.getNumber());
			this.toChannel+=""+event.getNumber();
			Log.i(TAG,"channel pressed is : "+this.toChannel);			
			this.chnNr.setText(this.toChannel);
			this.channelNr.show();
			Runnable r = new Runnable() {
		        public void run() {
		        	changeChannel();
		        }
			};
			if(chn==false){
				chn = new Handler().postDelayed(r, 2000);
			}			
		}else{	
		    switch (keyCode) {
			    case 92://prgUp
			    case 19:
			    	if(cnt_menu.getVisibility()==View.INVISIBLE){
			    		nextChannel();
			    	}
					break;
				case 93://prgDown
				case 20:
					if(cnt_menu.getVisibility()==View.INVISIBLE){
						previousChannel();
					}
					break;
				case 173:
				case 82://Menu
					if(cnt_menu.getVisibility()==View.VISIBLE){
						closeMenu();
					} else {
						cnt_menu.setVisibility(View.VISIBLE);
					}
					break;
				case 23:
					if(cnt_menu.getVisibility()==View.INVISIBLE){
						cnt_menu.setVisibility(View.VISIBLE);
					}
					switchMenu(0);
					break;
				/*case 142:
					audio.setStreamVolume(currentVolume+1, maxVolume, AudioManager.STREAM_MUSIC);
					break;
				case 141:					
					audio.setStreamVolume(currentVolume-1, maxVolume, AudioManager.STREAM_MUSIC);
					break;*/
		    }
		}
	    return super.onKeyDown(keyCode, event);
	}
	public void changeChannel(){
    	Log.i(TAG,"ToChannel Current channel place : "+this.toChannel);
    	try {    		
			IPTV.currentChannel=IPTV.MyChannels.getJSONObject(Integer.parseInt(this.toChannel)-1);
			IPTV.currentChannelPlace=Integer.parseInt(this.toChannel)-1;
			mMediaPlayer.stop();
			mMediaPlayer.reset();
			playChannel();
    	} catch(NumberFormatException nFE) { 
		} catch (JSONException e1) {
			Log.e(TAG,e1.toString());
			e1.printStackTrace();
		}    	
    }
    public void nextChannel(){    	
    	if(IPTV.currentChannelPlace>IPTV.MyChannels.length()){    		
    		IPTV.currentChannelPlace=0;
    		Log.i(TAG,"Current channel place lowererd to 0 : "+IPTV.currentChannelPlace);
    	}else{    		
    		IPTV.currentChannelPlace=IPTV.currentChannelPlace+1;
    		Log.i(TAG,"Current channel place incremented : "+IPTV.currentChannelPlace);
    	}    	
    	Log.i(TAG,"Current channel place : "+IPTV.currentChannelPlace);
    	try {
			IPTV.currentChannel=IPTV.MyChannels.getJSONObject(IPTV.currentChannelPlace);
			if(mMediaPlayer!=null){
				mMediaPlayer.reset();
			}			
			playChannel();
		} catch (JSONException e1) {
			Log.e(TAG,e1.toString());
			e1.printStackTrace();
		}
	}
    public void previousChannel(){    	
    	if(IPTV.currentChannelPlace==0){    		
    		IPTV.currentChannelPlace=0;
    		Log.i(TAG,"Current channel place lowererd to 0 : "+IPTV.currentChannelPlace);
    	}else{    		
    		IPTV.currentChannelPlace=IPTV.currentChannelPlace-1;
    		Log.i(TAG,"Current channel place incremented : "+IPTV.currentChannelPlace);
    	}    	
    	Log.i(TAG,"Current channel place : "+IPTV.currentChannelPlace);
    	try {
			IPTV.currentChannel=IPTV.MyChannels.getJSONObject(IPTV.currentChannelPlace);
			if(mMediaPlayer!=null){
				//mMediaPlayer.stop();
				mMediaPlayer.reset();
			}			
			playChannel();
		} catch (JSONException e1) {
			Log.e(TAG,e1.toString());
			e1.printStackTrace();
		}
	}
    public void closeMenu(){
    	programLayout.setVisibility(View.INVISIBLE);
    	apps.setVisibility(View.INVISIBLE);
    	cnt_menu.setVisibility(View.INVISIBLE);
    	favoritesLayout.setVisibility(View.INVISIBLE);
    	settings.setVisibility(View.INVISIBLE);
    }
	/////// PREFERENCES
    private void initPreferences(){
    	SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    	String pEmail = preferences.getString("email", "");
    	String pPass = preferences.getString("pass", "");
    	channelsAsIcons = preferences.getBoolean("channelAsIco", false);
    	btnChannelIcons.setChecked(channelsAsIcons);
    	btnChannelIcons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {				
				if(isChecked){
					channelsAsIcons=true;
				}else{
					channelsAsIcons = false;
				}
				SharedPreferences preferences = context.getPreferences(Activity.MODE_PRIVATE);
    			SharedPreferences.Editor editor = preferences.edit();
    			editor.putBoolean("channelAsIco", channelsAsIcons);
    			editor.commit();
				updateProgramList();
				updateFavoriteList();
			}        	
        });
    	//
    	Log.d(TAG,"Preferences "+pEmail+" "+pPass);
    	if(pEmail!="" && pPass!=""){
    		TextView email =(TextView) findViewById(R.id.email);
    		email.setText(pEmail);
    		email.setEnabled(false);
    		TextView pass= (TextView)findViewById(R.id.pass);
    		pass.setText(pPass);
    		pass.setEnabled(false);
    		this.btnLogin.setEnabled(false);
    	}
    	
    	//String pName = preferences.getString("Name", "");
    	//String pStatus = preferences.getString("Status", "");
    	//Integer pPack = preferences.getInt("Pack", 0);    	
    	//String pMyChannels = preferences.getString("MyChannels", "");
    	//String pMyFavorites = preferences.getString("MyFavorites", "");
    	
    }
    public void initAutoUpdate(){
    	this.autoUpdate = new autoUpdate(this);
    }
	/////// MENU
	private void initMenu(){	
		this.cnt_menu = (View) findViewById(R.id.cnt_menu);
		ListView menu = (ListView) findViewById(R.id.menu);
		menuAdapter menuAdapter = new menuAdapter(this,menus);
		menu.setAdapter(menuAdapter);		
		menu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				switchMenu(arg2);
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {				
				switchMenu(arg2);
			}
		});		
	}
	private void switchMenu(int val){
		Log.d(TAG,val+ " ");
		switch(val){
			case 0://Channels
				apps.setVisibility(GridView.INVISIBLE);
				settings.setVisibility(View.INVISIBLE);
				programLayout.setVisibility(GridView.VISIBLE);
				favoritesLayout.setVisibility(GridView.INVISIBLE);
			break;
			case 1://Favorites
				apps.setVisibility(GridView.INVISIBLE);
				settings.setVisibility(View.INVISIBLE);
				programLayout.setVisibility(GridView.INVISIBLE);
				favoritesLayout.setVisibility(GridView.VISIBLE);
			break;
			case 2://Applications				
				apps.setVisibility(GridView.VISIBLE);
				settings.setVisibility(View.INVISIBLE);
				programLayout.setVisibility(GridView.INVISIBLE);
				favoritesLayout.setVisibility(GridView.INVISIBLE);
			break;
			case 3://Settings
				apps.setVisibility(GridView.INVISIBLE);
				settings.setVisibility(View.VISIBLE);
				programLayout.setVisibility(GridView.INVISIBLE);
				favoritesLayout.setVisibility(GridView.INVISIBLE);
			break;
		}
	}
	///////Applications
	public void initApps(){
		apps = (GridView)findViewById(R.id.apps_layout);
        apps.setAdapter(new appInfoAdapter(this));
        apps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				try{
					ApplicationInfo app = (ApplicationInfo) arg0.getItemAtPosition(arg2);
					startActivity(app.intent);
				}catch(Error e){
					Log.e(TAG,e.toString());
				}				
			}        	
        });        
    }
	//////Settings
	public void initSettings(){
		settings = (View) findViewById(R.id.settings_layout);
		btnLogin = (Button) findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				iptv.login();
			}			
		});
        wifiSettings = (Button) findViewById(R.id.btn_wifi_settings);
        wifiSettings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));	
			}        	
        });
        btnChannelIcons = (ToggleButton) findViewById(R.id.btn_channel_icons);
	}
	//////ProgramList
	public void initProgramList(){
		programLayout = (GridView) findViewById(R.id.program_layout);
	}
	public void updateProgramList(){		
		channelAdapter=new channelAdapter(this);
		programLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				JSONObject chan = (JSONObject) arg0.getItemAtPosition(arg2);
				IPTV.currentChannelPlace=arg2;
				IPTV.currentChannel=chan;
				if(mMediaPlayer!=null){
					mMediaPlayer.reset();
				}			
				playChannel();
				closeMenu();
			}        	
		});
        programLayout.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				final JSONObject chan = (JSONObject) arg0.getItemAtPosition(arg2);			
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						context);
					alertDialogBuilder.setTitle("Favorites");
					alertDialogBuilder
						.setMessage("Add as Favorite ?")
						.setCancelable(false)
						.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								IPTV.favoritesChannels.put(chan);
								IPTV.setFavorites(favoritesAdapter);
							}
						  })
						.setNegativeButton("No",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								dialog.cancel();
							}
						});
						AlertDialog alertDialog = alertDialogBuilder.create();
						alertDialog.show();
				return true;
			}
		});
	}
	//////FavoriteList
	public void initFavoriteList(){
		favoritesLayout = (GridView) findViewById(R.id.favorites_layout);
	}
	public void updateFavoriteList(){
		favoritesAdapter=new favoritesAdapter(this);
		favoritesLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {				
				JSONObject chan = (JSONObject) arg0.getItemAtPosition(arg2);
				IPTV.currentChannelPlace=arg2;
				IPTV.currentChannel=chan;
				if(mMediaPlayer!=null){
					mMediaPlayer.reset();
				}			
				playChannel();
				closeMenu();
			}
		});
        favoritesLayout.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				final JSONObject chan = (JSONObject) arg0.getItemAtPosition(arg2);			
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
				alertDialogBuilder.setTitle("Favorites");
				alertDialogBuilder
					.setMessage("Remove from Favorite ?")
					.setCancelable(false)
					.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							JSONArray new_arr = new JSONArray();
							for(int i=0; i<IPTV.favoritesChannels.length();i++){
								try {
									if(IPTV.favoritesChannels.get(i)!=chan){
										new_arr.put(IPTV.favoritesChannels.get(i));
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}									
							}
							IPTV.favoritesChannels=new_arr;
							IPTV.setFavorites(favoritesAdapter);
						}
					  })
					.setNegativeButton("No",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
						}
					});
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
				return true;
			}
        });
	}
	//////Channel Info
	private void initChannelInfo(){
		this.channelInfo = (View) findViewById(R.id.channel_info);
	    this.currentChannel = (TextView) findViewById(R.id.current_channel);
	    this.channelName = (TextView) findViewById(R.id.channel_name);
	    LayoutInflater inflater = getLayoutInflater();
	    View view = inflater.inflate(R.layout.channel_nr,
	                                   (ViewGroup) findViewById(R.id.cnt_channel_nr));
	    view.setRight(20);
	    this.chnNr = (TextView) view.findViewById(R.id.channel_nr);
	    this.channelNr = new Toast(this);
	    this.channelNr.setGravity(Gravity.TOP|Gravity.RIGHT, 0, 0);
	    this.channelNr.setView(view);
	}	
	//Video
	public void initVideo(){
    	Log.d(TAG,"INITIALIZING VIDEO...");
    	mPreview = (SurfaceView) findViewById(R.id.surface);    	
    	/* GETTING DEVICE DIMENSIONS */
    	Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size); 
		this.width = size.x;
		this.height = size.y;
		Log.i(TAG,"Getting device Dimensions w:"+size.x+" h:"+size.y);
		/* INITIALIZING VIDEO */
		this.holder = mPreview.getHolder();
		this.holder.addCallback(this);
		this.holder.setFixedSize(width, height);
		Log.i(TAG,"Getting device Dimensions w:"+width+" h:"+height);
		/* Only on Touch device to remove */
		mPreview.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent event) {
				event.setAction(MotionEvent.ACTION_CANCEL);
				if(width/2>event.getX()){					
					Log.i(TAG,"Touched "+" CLICKED LEFT "+event.getX()+" "+event.getY());
					//previousChannel();
				}else{
					//nextChannel();
					Log.i(TAG,"Touched "+" CLICKED RIGHT "+event.getX()+" "+event.getY());
				}
				return false;
				
			}			
		});
		Log.d(TAG,"FINISHED INITIALIZING VIDEO...");
    }
	public boolean playChannel(){
		if(this.mIsAvailableTostream){
			String channel_id = null;
			try {
				channel_id = IPTV.currentChannel.getString("ID");
				SharedPreferences preferences = context.getPreferences(Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("last", channel_id);
				editor.commit();
			} catch (JSONException e1) {
				Log.e(TAG,"Error parsing channel Id");
				e1.printStackTrace();
			}
	    	AQuery aq = new AQuery(this.context);
			Log.d(TAG,"channel_id"+channel_id);
	    	String URL = iptv.getURLplay(Integer.parseInt(channel_id));
	    	Log.d(TAG,"channel_url "+URL);
	    	aq.ajax(URL, JSONObject.class, new AjaxCallback<JSONObject>() {
	            @Override
	            public void callback(String url, JSONObject json, AjaxStatus status) {
	                if(json != null){
	                	try {
							String j = json.getString("Message");
							if(context.mMediaPlayer==null){
								context.mMediaPlayer = new MediaPlayer();
								context.mMediaPlayer.setDisplay(context.holder);							
								context.mMediaPlayer.setOnBufferingUpdateListener(context);
					            context.mMediaPlayer.setOnCompletionListener(context);
					            context.mMediaPlayer.setOnPreparedListener(context);
					            context.mMediaPlayer.setOnVideoSizeChangedListener(context);
							}else{
								Log.i(TAG,"Mediaplayer has been reset!");
								context.mMediaPlayer.setDisplay(context.holder);
								context.mMediaPlayer.setOnBufferingUpdateListener(context);
								context.mMediaPlayer.setOnCompletionListener(context);
								context.mMediaPlayer.setOnPreparedListener(context);
								context.mMediaPlayer.setOnVideoSizeChangedListener(context);
							}
							context.mMediaPlayer.setDataSource(j);
							context.mMediaPlayer.prepareAsync();
				            context.toChannel="";
				            context.setChn(false);
						} catch (JSONException e) {							
							e.printStackTrace();
						} catch (IOException e) {
				            Log.e(TAG, e.getMessage(), e);
				        }catch(IllegalArgumentException e){
				        	Log.e(TAG, e.getMessage(), e);
				        }
	                }
	            }
	    	});    	
	    	showChannelInfo();
		}
    	return false;
    }
	Runnable mChannelInfoRunnable=new Runnable() {
        public void run() {
            channelInfo.setVisibility(View.INVISIBLE);   
        }
    };;
    Handler mChannelInfoHandler=new Handler();
	private void showChannelInfo(){    	
    	Log.i(TAG,"showing channel info");
    	try{
    		AQuery aq = new AQuery(this.context);
    		aq.id(R.id.channel_ico).image(IPTV.currentChannel.getString("Logo"));
    		this.currentChannel.setText((IPTV.currentChannelPlace+1)+"");
    		this.channelName.setText(IPTV.currentChannel.getString("Name"));//+" \nID: ["+IPTV.currentChannel.getString("ID")+"]\n"    		
    		this.channelInfo.setVisibility(View.VISIBLE);
    		mChannelInfoHandler.removeCallbacks(mChannelInfoRunnable);
    		mChannelInfoHandler.postDelayed(mChannelInfoRunnable, 5000);
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    }
	private void releaseMediaPlayer() {
		Log.d(TAG,"Release MediaPlayer");
		if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
	}
	private void doCleanUp() {
		setmVideoWidth(0);
        setmVideoHeight(0);
        mIsVideoReadyToBePlayed = false;
        setmIsVideoSizeKnown(false);
		Log.d(TAG,"doCleanUp Called");
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged called" + format + "  " + width + "   " + width);
	}
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated called");
	}
	public void surfaceDestroyed(SurfaceHolder holder) {		
		Log.d(TAG, "surfaceDestroyed called");
		mIsVideoReadyToBePlayed = false;
	}
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.d(TAG, "onBufferingUpdate percent:" + percent);		
	}
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "onCompletion called and mediaplayer is playing : "+mp.isPlaying());		
	}
	public void onPrepared(MediaPlayer mp) {
		long startTime = System.currentTimeMillis();
		Log.d(TAG, "onPrepared called");
		mIsVideoReadyToBePlayed = true;
		Log.d(TAG,"Video Size onPrepared = w:"+this.width+" h:"+this.height);
        if (mIsVideoReadyToBePlayed) {//mIsVideoSizeKnown
        	try{
				holder.setFixedSize(this.width, this.height);
				mMediaPlayer.setDisplay(holder);
				mMediaPlayer.start();
			}catch(Error e){
				Log.e(TAG,e.toString());
			}
        }
        long endTime = System.currentTimeMillis();
		Log.d(TAG,"play stream exectution = "+(endTime-startTime)+"ms");
	}
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.v(TAG, "onVideoSizeChanged called with w:"+width+" h:"+height);		
		if (width == 0 || height == 0) {
			Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
			return;
		}
		Log.i(TAG, "video width(" + width + ") or height(" + height + ")");
		setmIsVideoSizeKnown(true);
		setmVideoWidth(this.width);
		setmVideoHeight(this.height);			
	}
	public boolean ismIsVideoSizeKnown() {
		return mIsVideoSizeKnown;
	}
	public void setmIsVideoSizeKnown(boolean mIsVideoSizeKnown) {
		this.mIsVideoSizeKnown = mIsVideoSizeKnown;
	}
	public int getmVideoWidth() {
		return mVideoWidth;
	}
	public void setmVideoWidth(int mVideoWidth) {
		this.mVideoWidth = mVideoWidth;
	}
	public int getmVideoHeight() {
		return mVideoHeight;
	}
	public void setmVideoHeight(int mVideoHeight) {
		this.mVideoHeight = mVideoHeight;
	}
	public boolean isChn() {
		return chn;
	}
	public void setChn(boolean chn) {
		this.chn = chn;
	}
	/*android.graphics.drawable.Drawable drawable_from_url(String url, String src_name) throws java.net.MalformedURLException, java.io.IOException {
	    return android.graphics.drawable.Drawable.createFromStream(((java.io.InputStream)new java.net.URL(url).getContent()), src_name);
	}*/
}
