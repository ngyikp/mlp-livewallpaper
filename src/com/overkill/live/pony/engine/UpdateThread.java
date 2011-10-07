package com.overkill.live.pony.engine;

import android.os.SystemClock;

public class UpdateThread extends Thread {

    private RenderEngine engine;
    private boolean run;
    private boolean wait;
    public boolean ready = false;
    
    public UpdateThread(RenderEngine engine) {
        this.engine = engine;
    }

    public void startUpdate(){
        this.run = true;
        this.start();
    }

    public void pauseUpdate(){
        this.wait = true;
        synchronized(this){
            this.notify();
        }
    }
    
    public void resumeUpdate(){
    	 this.wait = false;
         synchronized(this) {
             this.notify();
         }
    }

    public void stopUpdate(){
        this.run = false;
        synchronized(this){
            this.notify();
        }
    }
    
    @Override
    public void run() {
        while(this.run){
        	long cycleStartTime = SystemClock.elapsedRealtime();   
            this.engine.update(cycleStartTime);
            this.ready = true;
            long cycleTime = SystemClock.elapsedRealtime() - cycleStartTime;
            //Log.i("updateThread", "cycleTime: " + cycleTime);
            long sleep = Math.max(0, 30 - cycleTime);
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
