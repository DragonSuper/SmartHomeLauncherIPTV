package com.ontheblue.iptv;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ontheblue.iptv.IPTV;
import com.ontheblue.iptv.utils;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TvHome extends Activity implements OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener, OnInfoListener,SurfaceHolder.Callback, OnErrorListener, KeyEvent.Callback{
	
	final String TAG = "IPTV";
	final Context context = this;
	public IPTV IPTV = new IPTV(this);
	//private WifiManager mainWifi;
	private utils utils =new utils(this);
	private long startTime;
	/*
	 * Video Player
	 */
    private MediaPlayer mMediaPlayer;
    private SurfaceView mPreview;
    private SurfaceHolder holder;
    private boolean mIsVideoSizeKnown = false;
    private boolean mIsVideoReadyToBePlayed = false;
    private int mVideoWidth;
    private int mVideoHeight;
    private int width=0;
    private int height=0;
    
    /*
     * PROGRAM LAYOUT
     */
    public GridView programLayout;    
    public channelAdapter channelAdapter;
    /*
     * FAVORITES LAYOUT
     */
    public GridView favoritesLayout;    
    public favoritesAdapter favoritesAdapter;
    /*
     * MENU
     */
    public ListView menu;
    public View cnt_menu;
    public menuAdapter menuAdapter;
    public ImageView menu_channel_icon;
    public Toast toast;
    public LayoutInflater inflater;
    /*
     * Channels
     */
    public String toChannel="";
    private boolean chn = false;    
    /*
     * APPS
     */
    private GridView apps;
    /*
     * Settings
     */
    private View settings;
    private Button wifiSettings;
    /*
     * Channel Info
     */
    private View channelInfo;
    private TextView currentChannel;
    private TextView channelName;
    private ImageView channelIco;
    /*
     * AUTO UPDATE 
     */
    //private autoUpdate aupd;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_home);
        Intent intent = new Intent();
        intent.setAction("android.net.conn.CONNECTIVITY_CHANGE");
        sendBroadcast(intent);
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.ThreadPolicy policy2 = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build();
        StrictMode.setThreadPolicy(policy2);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);                
        //this.aupd= new autoUpdate(this);
        this.settings = (View) findViewById(R.id.settings_layout);
        this.wifiSettings = (Button) findViewById(R.id.btn_wifi_settings);
        this.wifiSettings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));	
			}        	
        });
        while(utils.isNetworkAvailable()==false){
        	try {
        		Log.d(TAG,"trying to connect... wait for 1000 ms");
				Thread.sleep(1000);				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        };
		init();
    }
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            getWindow().closeAllPanels();
        }
    }
	@Override
    protected void onResume() {
        super.onResume();
        while(utils.isNetworkAvailable()==false){
        	try {
        		Log.d(TAG,"trying to connect... wait for 1000 ms");
				Thread.sleep(1000);				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        };
        playChannel();
    }
	public void init(){    	
    	/* INIT MENU */
    	this.menu = (ListView) findViewById(R.id.menu);
    	this.cnt_menu = (View) findViewById(R.id.cnt_menu);
    	final String[] menus = new String[] { "Channels", "Apps", "Favorites",
    			  "Settings" };
    	this.menuAdapter = new menuAdapter(this,menus);
    	menu.setAdapter(this.menuAdapter);
    	menu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				Log.d(TAG, "on click menu item ... : "+menus[arg2]+" "+arg2);
				//String clicked_menu =(String) arg0.getItemAtPosition(arg2); 
				switch(arg2){
					case 0://Channels
						if(cnt_menu.getVisibility()==View.VISIBLE){
							programLayout.setVisibility(GridView.VISIBLE);
						}
						apps.setVisibility(GridView.INVISIBLE);
						settings.setVisibility(View.INVISIBLE);
						favoritesLayout.setVisibility(View.INVISIBLE);
					break;
					case 1://Apps
						programLayout.setVisibility(GridView.INVISIBLE);
						apps.setVisibility(GridView.VISIBLE);
						settings.setVisibility(View.INVISIBLE);
						favoritesLayout.setVisibility(View.INVISIBLE);
					break;
					case 2:
						programLayout.setVisibility(GridView.INVISIBLE);
						apps.setVisibility(GridView.INVISIBLE);
						settings.setVisibility(View.INVISIBLE);
						favoritesLayout.setVisibility(View.VISIBLE);
					break;
					case 3:
						programLayout.setVisibility(GridView.INVISIBLE);
						apps.setVisibility(GridView.INVISIBLE);
						settings.setVisibility(View.VISIBLE);
						favoritesLayout.setVisibility(View.INVISIBLE);
					break;
					default:
						
					break;
				}				
			}			
			public void onNothingSelected(AdapterView<?> arg0) {				
			}
		});
    	/*init channel info*/
    	this.channelInfo = (View) findViewById(R.id.channelInfo);
        this.currentChannel = (TextView) findViewById(R.id.current_channel);
        this.channelName = (TextView) findViewById(R.id.channel_name);
        this.channelIco= (ImageView) findViewById(R.id.channel_ico);
        this.toast = new Toast(getApplicationContext());        
        initApps();
        this.programLayout = (GridView) findViewById(R.id.program_layout);        
        initProgramList();
        this.favoritesLayout = (GridView) findViewById(R.id.favorites_layout);        
        initFavoritesList();
        /* INIT VIDEO */
    	initVideo();
        
    }    
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
					previousChannel();
				}else{
					nextChannel();
					Log.i(TAG,"Touched "+" CLICKED RIGHT "+event.getX()+" "+event.getY());
				}
				return false;
				
			}			
		});
		Log.d(TAG,"FINISHED INITIALIZING VIDEO...");
    }
    public void initProgramList(){
    	Log.d(TAG,"INITIALIZING PROGRM LIST");
    	this.channelAdapter=new channelAdapter(this,IPTV);
        programLayout.setAdapter(this.channelAdapter);
        programLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {				
				JSONObject chan = (JSONObject) arg0.getItemAtPosition(arg2);				
				Log.d(TAG,chan.toString()+" "+arg2+" "+arg3);
				try {
					IPTV.currentChannel = chan;
			    	IPTV.currentChannelPlace=arg2;					
					Log.i(TAG,"Channel ID : "+chan.getString("ID"));
					Log.i(TAG, "Play GET started...");
					playChannel();
				} catch (JSONException e) {
					Log.e(TAG,e.toString());
					e.printStackTrace();
				}
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
								if(IPTV.setFavorites()){									
									favoritesAdapter = new favoritesAdapter(context, IPTV);
									favoritesLayout.setAdapter(favoritesAdapter);
								}
							}
						  })
						.setNegativeButton("No",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								dialog.cancel();
							}
						});
		 
						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();
		 
						// show it
						alertDialog.show();
				
				return false;
			}
		});
        Log.d(TAG,"FINISH INITIALIZING PROGRM LIST (GIRDView)");
        playChannel();
    }
    public void initFavoritesList(){
    	Log.d(TAG,"INITIALIZING Favorites LIST");
    	this.favoritesAdapter=new favoritesAdapter(this,IPTV);
        favoritesLayout.setAdapter(this.favoritesAdapter);
        favoritesLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {				
				JSONObject chan = (JSONObject) arg0.getItemAtPosition(arg2);				
				Log.d(TAG,chan.toString()+" "+arg2+" "+arg3);
				try {
					IPTV.currentChannel = chan;
			    	IPTV.currentChannelPlace=arg2;					
					Log.i(TAG,"Channel ID : "+chan.getString("ID"));
					Log.i(TAG, "Play GET started...");
					playChannel();
				} catch (JSONException e) {
					Log.e(TAG,e.toString());
					e.printStackTrace();
				}
				closeMenu();
			}        	
		});
        favoritesLayout.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				final JSONObject chan = (JSONObject) arg0.getItemAtPosition(arg2);			
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						context);
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
								if(IPTV.setFavorites()){									
									favoritesAdapter = new favoritesAdapter(context, IPTV);
									favoritesLayout.setAdapter(favoritesAdapter);
								}
							}
						  })
						.setNegativeButton("No",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								dialog.cancel();
							}
						});
		 
						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();
		 
						// show it
						alertDialog.show();
				
				return false;
			}
		});
    }
    public void initApps(){
    	Log.d(TAG,"INITIALIZING APPS");
    	//get a list of installed apps.
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
        Log.d(TAG,"FINISHED INITIALIZING APPS");
    }
    /*
     * Functions for API
     */    
    private boolean playChannel(){
    	showChannelInfo();
    	//doCleanUp();
    	try {
    		startTime = System.currentTimeMillis();
			String stream = IPTV.getStream();
			long endTime = System.currentTimeMillis();
			Log.d(TAG,"IPTV.getStream time exectution = "+(endTime-startTime)+"ms");
			Log.i(TAG, "Stream to Play: "+stream);
			startTime = System.currentTimeMillis();
			if(mMediaPlayer==null){
				mMediaPlayer = new MediaPlayer();
				mMediaPlayer.setDisplay(holder);
	            mMediaPlayer.setOnBufferingUpdateListener(this);
	            mMediaPlayer.setOnCompletionListener(this);
	            mMediaPlayer.setOnPreparedListener(this);
	            mMediaPlayer.setOnVideoSizeChangedListener(this);
			}else{
				Log.i(TAG,"Mediaplayer has been reset!");
				//Log.d(TAG,holder.toString());
				//holder = mPreview.getHolder();				
				//mMediaPlayer.reset();
				mMediaPlayer.setDisplay(holder);
	            mMediaPlayer.setOnBufferingUpdateListener(this);
	            mMediaPlayer.setOnCompletionListener(this);
	            mMediaPlayer.setOnPreparedListener(this);
	            mMediaPlayer.setOnVideoSizeChangedListener(this);
			}			
			mMediaPlayer.setDataSource(stream);           
            mMediaPlayer.prepareAsync();
			this.toChannel="";
			this.chn=false;
			/*Runnable r = new Runnable() {
		        public void run() {                
		                try {
							mMediaPlayer.prepare();
							Log.v(TAG, "Duration:  ===>" + mMediaPlayer.getDuration());
			                mMediaPlayer.start();
						} catch (IllegalStateException e) {
							if(mMediaPlayer!=null){
								mMediaPlayer.stop();
								mMediaPlayer.release();
							}
							Log.e(TAG,e.getMessage(),e);
							e.printStackTrace();
						} catch (IOException e) {
							if(mMediaPlayer!=null){
								mMediaPlayer.stop();
								mMediaPlayer.release();
							}
							Log.e(TAG,e.getMessage(),e);
							e.printStackTrace();
						}                
		            }
		        };
		        new Thread(r).start();
			return true;*/
		} catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }catch(IllegalArgumentException e){
        	Log.e(TAG, e.getMessage(), e);
        }
    	return false;
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
				//mMediaPlayer.stop();
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
    
    Runnable mChannelInfoRunnable=new Runnable() {
        public void run() {
            channelInfo.setVisibility(View.INVISIBLE);   
        }
    };;
    Handler mChannelInfoHandler=new Handler();
    
    private void showChannelInfo(){    	
    	Log.i(TAG,"showing channel info");
    	try{
    		this.channelIco.setImageDrawable(drawable_from_url(IPTV.currentChannel.getString("Logo"),"test"));
    		this.currentChannel.setText((IPTV.currentChannelPlace+1)+"");
    		this.channelName.setText(IPTV.currentChannel.getString("Name"));//+" \nID: ["+IPTV.currentChannel.getString("ID")+"]\n"
    		
    		this.channelInfo.setVisibility(View.VISIBLE);
    		mChannelInfoHandler.removeCallbacks(mChannelInfoRunnable);
    		mChannelInfoHandler.postDelayed(mChannelInfoRunnable, 5000);
    	} catch (JSONException e) {
    		e.printStackTrace();
    	} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	/*try {
    		inflater = getLayoutInflater();    		
			View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));
			ImageView image = (ImageView) layout.findViewById(R.id.image);			
			try {
				image.setImageDrawable(drawable_from_url(IPTV.currentChannel.getString("Logo"),"test"));				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			TextView text = (TextView) layout.findViewById(R.id.text);
			TextView currentChannelText = (TextView) layout.findViewById(R.id.current_channel);
			text.setText(IPTV.currentChannel.getString("Name")+" \nID: ["+IPTV.currentChannel.getString("ID")+"]\n");
			currentChannelText.setText((IPTV.currentChannelPlace+1)+"");			
			toast.setGravity(Gravity.TOP|Gravity.LEFT, 10, 10);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(layout);			
			toast.show();
		} catch (JSONException e) {
			e.printStackTrace();
		}*/
    }
    private void closeMenu(){
    	programLayout.setVisibility(GridView.INVISIBLE);
    	apps.setVisibility(GridView.INVISIBLE);
    	cnt_menu.setVisibility(View.INVISIBLE);
    	favoritesLayout.setVisibility(View.INVISIBLE);
    }
    public void onBufferingUpdate(MediaPlayer arg0, int percent) {
		//Log.d(TAG, "onBufferingUpdate percent:" + percent);    	
	}

	public void onCompletion(MediaPlayer arg0) {
		Log.d(TAG, "onCompletion called and mediaplayer is playing : "+mMediaPlayer.isPlaying());
		/*if(mMediaPlayer.isPlaying()){
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}*/		
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
		/*
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			try{
				holder.setFixedSize(this.width, this.height);
				//mMediaPlayer.setDisplay(holder);
				mMediaPlayer.start();
			}catch(Error e){
				Log.e(TAG,e.toString());
			}
		}*/		
	}

	public void onPrepared(MediaPlayer mediaplayer) {	
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

	public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
		Log.d(TAG, "surfaceChanged called" + i + "  " + j + "   " + k);
		/*if (mMediaPlayer != null && mMediaPlayer.isPlaying()==false) {
			Log.i(TAG,"should start playing");
			playChannel();
		}*/
	}

	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
		Log.d(TAG, "surfaceDestroyed called");		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated called");
		/*if (mMediaPlayer != null && mMediaPlayer.isPlaying()==false) {
			Log.i(TAG,"should start playing");
			playChannel();
		}*/
	}
	@Override
	protected void onPause() {
		Log.d(TAG,"onPause Called");
		super.onPause();
		releaseMediaPlayer();
		doCleanUp();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestroy Called");
		super.onDestroy();
		releaseMediaPlayer();
		doCleanUp();
	}

	private void releaseMediaPlayer() {
		Log.d(TAG,"onDestroy Called");
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
	
	/*
	 * Events
	 */
	
	/*
	 * GETURE
	 */	
	
	/*
	 * IMAGE Fetch
	 */
	android.graphics.drawable.Drawable drawable_from_url(String url, String src_name) throws java.net.MalformedURLException, java.io.IOException {
	    return android.graphics.drawable.Drawable.createFromStream(((java.io.InputStream)new java.net.URL(url).getContent()), src_name);
	}

	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		Log.e(TAG,"ERROR Media Player "+arg1+" "+arg2);
		return false;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//prevent Back Button closing application
		if (event.getKeyCode()==KeyEvent.KEYCODE_BACK) {
            return true;
        }		
		/*AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);*/
		Log.i(TAG,"keycode : "+keyCode+" keyEnvent : "+event.getAction()+" and "+event.getNumber()+" "+event.getCharacters());		
		if(IPTV.isNumeric(event.getNumber()+"")){
			Log.i(TAG,"A number has been pressed "+event.getNumber());
			this.toChannel+=""+event.getNumber();
			Log.i(TAG,"channel pressed is : "+this.toChannel);
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
	/*
	 * 
			case KeyEvent.KEYCODE_MENU:
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));
				ViewGroup lay = (ViewGroup) findViewById(R.layout.activity_tv_home);
				lay.addView(layout);
				break;
	 */
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.i(TAG,"on info "+what+" "+extra);
		return false;
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
}
