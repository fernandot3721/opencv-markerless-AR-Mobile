package meiroo.cvgl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import javax.microedition.khronos.opengles.GL10;

public class GlBufferView extends GLSurfaceView {
    private static String TAG = "GLAndroid";

    public GlBufferView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        /* We need to choose an EGLConfig that matches the format of
         * our surface exactly. This is going to be done in our
         * custom config chooser. See ConfigChooser class definition
         * below.
         */


        //setZOrderOnTop(true);
        setRenderer(new MyRenderer());
        /*
		requestFocus();
		setFocusableInTouchMode(true);
		*/
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        queueEvent(new Runnable() {
            public void run() {
                CVGLActivity.native_touch_event(event.getX(), event.getY(), event.getAction());
            }
        });

        return true;
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        queueEvent(new Runnable() {
            public void run() {
                CVGLActivity.native_key_event(keyCode, event.getAction());
            }
        });
        return false;
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        queueEvent(new Runnable() {
            public void run() {
                CVGLActivity.native_key_event(keyCode, event.getAction());
            }
        });

        return false;
    }

    class MyRenderer implements GLSurfaceView.Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig arg1) {
			/* do nothing */
            CVGLActivity.native_start();
        }


        public void onSurfaceChanged(GL10 gl, int w, int h) {
            CVGLActivity.native_gl_resize(w, h);
        }

        public void onDrawFrame(GL10 gl) {
            time = SystemClock.uptimeMillis();

            if (time >= (frameTime + 1000.0f)) {
                frameTime = time;
                avgFPS += framerate;
                framerate = 0;
            }

            if (time >= (fpsTime + 3000.0f)) {
                fpsTime = time;
                avgFPS /= 3.0f;
                Log.d("GLAndroid", "FPS: " + Float.toString(avgFPS));
                avgFPS = 0;
            }
            framerate++;

            if (CVGLActivity.result > 0) {
                CVGLActivity.native_gl_render();
            } else {
                gl.glClearColor(0, 0, 0, 0);
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            }
        }

        public long time = 0;
        public short framerate = 0;
        public long fpsTime = 0;
        public long frameTime = 0;
        public float avgFPS = 0;
    }
}
