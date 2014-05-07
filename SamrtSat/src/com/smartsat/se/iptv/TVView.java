package com.smartsat.se.iptv;

import java.io.IOException;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.MediaController.MediaPlayerControl;

public class TVView extends SurfaceView implements MediaPlayerControl, SurfaceHolder.Callback, OnPreparedListener, OnVideoSizeChangedListener, OnCompletionListener, OnBufferingUpdateListener, OnErrorListener{
	
	final String TAG = "IPTV TVView";
	
	private Context mContext;
	/* Video dimensions */
	private int mVideoHeight;
	private int mVideoWidth;
	/* Surface Dimensions */
	private int mSurfaceHeight;
	private SurfaceHolder mSurfaceHolder = null;
	private int mSurfaceWidth;
	/* Others */
	public MediaPlayer mMediaPlayer = null;
	private boolean mIsPrepared;
	private int mCurrentBufferPercentage;
	private int mDuration;
	private Uri mUri;
	private String ratioValue = null;
	private String cvrsValue = null;
	
	public TVView(Context context) {
		super(context);
		this.mContext = context;
	    initVideoView();
	}
	public TVView(Context context, AttributeSet paramAttributeSet){
	    this(context, paramAttributeSet, 0);
	    this.mContext = context;
	    initVideoView();
	}
	public TVView(Context context, AttributeSet paramAttributeSet, int paramInt){
	    super(context, paramAttributeSet, paramInt);
	    this.mContext = context;
	    initVideoView();
	}
	private void initVideoView(){
		//this.display_man = new DisplayManager();
	    this.mVideoWidth = 0;
	    this.mVideoHeight = 0;
	    getHolder().addCallback(this);
	    //getHolder().setType(0);
	    getHolder().setFormat(1);
	    setFocusable(true);
	    setFocusableInTouchMode(true);
	    requestFocus();
	}
	
	private void openVideo(){
		intVideoValue();
	    if ((this.mUri == null) || (this.mSurfaceHolder == null));
	      while(true){
	      //Intent localIntent1 = new Intent("com.android.music.musicservicecommand");
	      //localIntent1.putExtra("command", "pause");
	      //this.mContext.sendBroadcast(localIntent1);
	      //Intent localIntent2 = new Intent("com.android.music.videoOpened");
	      //localIntent2.putExtra("flag", "true");
	      //this.mContext.sendBroadcast(localIntent2);
	      if (this.mMediaPlayer != null){
	        this.mMediaPlayer.reset();
	        this.mMediaPlayer.release();
	        this.mMediaPlayer = null;
	      }
	      try{
	        this.mMediaPlayer = new MediaPlayer();
	        this.mIsPrepared = false;
	        Log.v(this.TAG, "reset duration to -1 in openVideo");
	        this.mDuration = -1;
	        
	        //this.mMediaPlayer.setOnFastBackwordCompleteListener(this.mFastBackwordCompleteListener);	        
	        this.mCurrentBufferPercentage = 0;
	        this.mMediaPlayer.setDataSource(this.mContext, this.mUri);
	        this.mSurfaceHolder.setFixedSize(getVideoWidth(), getVideoHeight());
	        this.mMediaPlayer.setDisplay(this.mSurfaceHolder);
	        this.mMediaPlayer.setAudioStreamType(3);
	        this.mMediaPlayer.setScreenOnWhilePlaying(true);
	        this.mMediaPlayer.prepareAsync();
	      }catch (IOException localIOException){
	        Log.w(this.TAG, "Unable to open content: " + this.mUri, localIOException);
	      }catch (IllegalArgumentException localIllegalArgumentException){
	        Log.w(this.TAG, "Unable to open content: " + this.mUri, localIllegalArgumentException);
	      }
	    }
	  }
	
	public void destroyPlayer(){
		Log.i(this.TAG, "-----------in the destroyPlayer-------------mSurfaceHolder:" + this.mSurfaceHolder + " mMediaPlayer:" + this.mMediaPlayer);
	    if (this.mSurfaceHolder != null){
	    	this.mSurfaceHolder = null;
	    	//Intent localIntent = new Intent("com.android.music.videoOpened");
	    	//localIntent.putExtra("flag", "false");
	    	//this.mContext.sendBroadcast(localIntent);
	    }
	    if (this.mMediaPlayer != null){
	    	this.mMediaPlayer.reset();
	    	this.mMediaPlayer.release();
	    	this.mMediaPlayer = null;
	    }
	}
	public int getMediaHeight(){
		return this.mMediaPlayer.getVideoHeight();
	}
	public int getMediaWidth(){
		return this.mMediaPlayer.getVideoWidth();
	}
	public int getVideoHeight(){
		return this.mVideoHeight;
	}
	public int getVideoWidth(){
		return this.mVideoWidth;
	}
	
	public void intVideoValue(){
		String[] arrayOfString1 = new String[2];
	    arrayOfString1[0] = "value";
	    arrayOfString1[1] = "flag";
	    String[] arrayOfString2 = new String[2];
	    arrayOfString2[0] = "14";
	    arrayOfString2[1] = "15";
	    ContentResolver localContentResolver = this.mContext.getContentResolver();
	    Uri localUri = Uri.parse("content://com.android.mysetting/item");
	    Cursor localCursor = null;
	    while (true){
	    	try{
	    		localCursor = localContentResolver.query(localUri, arrayOfString1, " id in (?,?) ", arrayOfString2, null);
	    		if ((localCursor != null) && (localCursor.getCount() > 0))
	    			if (localCursor.moveToNext())
	    				if (localCursor.getString(localCursor.getColumnIndex("flag")).equals("1")){
	    					this.ratioValue = localCursor.getString(localCursor.getColumnIndex("value"));
	    					Log.i("caoshanshan", "ratioValue ==" + this.ratioValue + ",cvrsValue=" + this.cvrsValue);
	    					continue;
	    				}
	    	}catch (Exception localException){
	    		localException.printStackTrace();
	    		return;
	    		//this.cvrsValue = localCursor.getString(localCursor.getColumnIndex("value"));
	    		//continue;
	    	}finally{
	    		if (localCursor != null)
	    			localCursor.close();
	    	}
	    	if (localCursor != null)
	    		localCursor.close();
	    	}
	  }
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean canPause() {
		return false;
	}
	@Override
	public boolean canSeekBackward() {
		return false;
	}
	@Override
	public boolean canSeekForward() {
		return false;
	}
	@Override
	public int getBufferPercentage() {
		if (this.mMediaPlayer != null);
		for (int i = this.mCurrentBufferPercentage; ; i = 0)
	      return i;
	}
	@Override
	public int getCurrentPosition() {
		if ((this.mMediaPlayer != null) && (this.mIsPrepared));
	    for (int i = this.mMediaPlayer.getCurrentPosition(); ; i = 0)
	      return i;
	}
	@Override
	public int getDuration() {
		int i;
	    if ((this.mMediaPlayer != null) && (this.mIsPrepared))
	    	if (this.mDuration > 0)
	    		i = this.mDuration;
	    while (true){	      
	      this.mDuration = this.mMediaPlayer.getDuration();
	      return i = this.mDuration;
	    }
	}
	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void seekTo(int pos) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPrepared(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		
	}

}
