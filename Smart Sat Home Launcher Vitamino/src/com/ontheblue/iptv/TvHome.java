package com.ontheblue.iptv;

import java.io.IOException;
import java.net.MalformedURLException;
import org.json.JSONException;
import org.json.JSONObject;

import com.ontheblue.iptv.IPTV;


import android.graphics.Point;
/*import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.*;*/
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;
import io.vov.vitamio.VitamioInstaller.VitamioNotCompatibleException;
import io.vov.vitamio.VitamioInstaller.VitamioNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;




public class TvHome extends Activity implements OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener, OnInfoListener,SurfaceHolder.Callback, OnErrorListener, KeyEvent.Callback{
	
	final String TAG = "IPTV";
	public IPTV IPTV = new IPTV(this);
	//private WifiManager mainWifi;
	
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
     * MENU
     */
    public View menu;
    public ImageView menu_channel_icon;
    public Toast toast;
    public LayoutInflater inflater;
    public ProgressBar progressBar;
    /*
     * Buttons
     */
    public Button btnChannels;
    public Button btnApps;    
    public Button btnSettings;
    public Button btnExit;
    public ImageButton btnApplications;
    public ImageButton btnPrograms;
    /*
     * Channels
     */
    public String toChannel="";
    private boolean chn = false;
    
    /*
     * APPS
     */
    private GridView apps;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_home);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //setVolumeControlStream(AudioManager.STREAM_MUSIC);        
		init();		
    }
    public void init(){    	
    	/* INIT MENU */
    	this.menu = (View) findViewById(R.id.menu);
        this.toast = new Toast(getApplicationContext());
        this.btnChannels = (Button) findViewById(R.id.btn_channels);
        this.btnApps = (Button) findViewById(R.id.btn_apps);
        this.btnSettings = (Button) findViewById(R.id.btn_settings);
        this.btnExit = (Button) findViewById(R.id.btn_exit);
        this.btnApplications = (ImageButton) findViewById(R.id.btn_appdrawer);
        this.btnPrograms = (ImageButton) findViewById(R.id.btn_programs);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.progressBar.setMax(100);
        this.progressBar.setProgress(0);
        initApps();        
        this.programLayout = (GridView) findViewById(R.id.program_layout);
        initProgramList();
        btnPrograms.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showProgram();
			}
		});
        btnApplications.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showApplications();
			}			
		});
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
			@Override
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
        programLayout.setAdapter(new channelAdapter(this,IPTV));
        programLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				/*Toast toast = Toast.makeText(getBaseContext(), "channel item is "+arg0.getId(), Toast.LENGTH_LONG);
				toast.show();*/
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
				showProgram();
				//playChannel();
			}        	
		});
        Log.d(TAG,"FINISH INITIALIZING PROGRM LIST");
        playChannel();
    }
    public void initApps(){
    	Log.d(TAG,"INITIALIZING APPS");
    	//get a list of installed apps.
		apps = (GridView)findViewById(R.id.gvMain);		
        apps.setAdapter(new appInfoAdapter(this));
        apps.setBackgroundColor(0x80808000);
        apps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
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
    /* URL Constructors */   
    
    private boolean playChannel(){
    	//doCleanUp();
    	try {
			String stream = IPTV.getStream();
			Log.i(TAG, "Stream to Play: "+stream);			
			if(mMediaPlayer==null){
				mMediaPlayer = new MediaPlayer(this);
				mMediaPlayer.setDisplay(holder);
	            mMediaPlayer.setOnBufferingUpdateListener(this);
	            mMediaPlayer.setOnCompletionListener(this);
	            mMediaPlayer.setOnPreparedListener(this);
	            mMediaPlayer.setOnVideoSizeChangedListener(this);
			}else{
				Log.i(TAG,"Mediaplayer has been reset!");
				mMediaPlayer.reset();
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
        } catch (VitamioNotCompatibleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VitamioNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			mMediaPlayer.stop();
			mMediaPlayer.reset();
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
			mMediaPlayer.stop();
			mMediaPlayer.reset();
			playChannel();
		} catch (JSONException e1) {
			Log.e(TAG,e1.toString());
			e1.printStackTrace();
		}
	}    

    private void showChannelInfo(){    	
    	Log.i(TAG,"showing channel info");
    	try {
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
		}
    	
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
		mIsVideoSizeKnown = true;
		mVideoWidth = this.width;
		mVideoHeight = this.height;
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
		showChannelInfo();		
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
		mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
		Log.d(TAG,"doCleanUp Called");
	}
	
	private void showProgram(){
		Log.d(TAG,"showProgram Called");
		if(programLayout.getVisibility()==GridView.VISIBLE){
			programLayout.setVisibility(GridView.INVISIBLE);
		} else {
			programLayout.setVisibility(GridView.VISIBLE);					
		}
	}
	private void showApplications(){
		if(apps.getVisibility()==GridView.VISIBLE){
			apps.setVisibility(GridView.INVISIBLE);
		}else{
			apps.setVisibility(GridView.VISIBLE);
		}
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

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		Log.e(TAG,"ERROR Media Player "+arg1+" "+arg2);
		return false;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
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
					nextChannel();
					break;
				case 93://prgDown
					previousChannel();
					break;
				case 173:
				case 82://Menu				
					if(menu.getVisibility()==View.VISIBLE){
						menu.setVisibility(View.INVISIBLE);
						apps.setVisibility(GridView.INVISIBLE);
					} else {
						menu.setVisibility(View.VISIBLE);
					}
					break;
				case 23:
					showProgram();
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
	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.i(TAG,"on info "+what+" "+extra);
		return false;
	}
}
