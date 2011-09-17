package com.overkill.live.pony;

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
            this.engine.render();
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
