package com.overkill.live.pony.engine;

import android.os.SystemClock;

public class RenderThread extends Thread {

    private RenderEngine engine;
    private boolean run;
    private boolean wait;
    
    public RenderThread(RenderEngine engine) {
        this.engine = engine;
    }

    public void startRender(){
        this.run = true;
        this.start();
    }

    public void pauseRender(){
        this.wait = true;
        synchronized(this){
            this.notify();
        }
    }
    
    public void resumeRender(){
    	 this.wait = false;
         synchronized(this) {
             this.notify();
         }
    }

    public void stopRender(){
        this.run = false;
        synchronized(this){
            this.notify();
        }
    }
    
    @Override
    public void run() {
        while(this.run){
        	long renderStartTime = SystemClock.elapsedRealtime();   
            this.engine.render(renderStartTime);
            long cycleTime = SystemClock.elapsedRealtime() - renderStartTime;
            //Log.i("renderThread", "cycleTime: " + cycleTime);
            long sleep = Math.max(0, RenderEngine.CONFIG_FRAME_DELAY - cycleTime);
            try {
				sleep(sleep);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
            synchronized (this) {
                if (wait) {
                    try {
                        wait();
                    } catch (Exception e) {}
                }
            }
        }
    }
}
