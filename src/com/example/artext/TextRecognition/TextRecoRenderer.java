/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.example.artext.TextRecognition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.example.artext.SampleApplication.SampleApplicationSession;
import com.example.artext.utils.LineShaders;
import com.example.artext.utils.LoadingDialogHandler;
import com.example.artext.utils.SampleApplicationGLView;
import com.example.artext.utils.SampleUtils;
import com.example.artext.ui.SampleAppMenu.SampleAppMenu;
import com.example.artext.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Obb2D;
import com.qualcomm.vuforia.Rectangle;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.Word;
import com.qualcomm.vuforia.WordResult;
import com.text.riGraphicTools;


// The renderer class for the ImageTargets sample. 
public class TextRecoRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "TextRecoRenderer";
    
    private SampleApplicationSession vuforiaAppSession;
    
    private static final int MAX_NB_WORDS = 132;
    private static final float TEXTBOX_PADDING = 0.0f;
    
    private static final float ROIVertices[] = { 
    	-0.5f, -0.5f, 0.0f, 
    	0.5f,-0.5f, 0.0f, 
    	0.5f, 0.5f, 0.0f, 
    	-0.5f, 0.5f, 0.0f };
    
    private static final int NUM_QUAD_OBJECT_INDICES = 8;
    private static final short ROIIndices[] = { 0, 1, 1, 2, 2, 3, 3, 0 };
    
    private static final float quadVertices[] = { 
    	-0.5f, -0.5f, 0.0f,
    	0.5f,-0.5f, 0.0f,
    	0.5f, 0.5f, 0.0f,
    	-0.5f, 0.5f, 0.0f, };
    
    private static final float textVertices[] = { 
    	-0.5f, 0.5f, 0.0f,
    	-0.5f,-0.5f, 0.0f,
    	0.5f, -0.5f, 0.0f,
    	0.5f, 0.5f, 0.0f, };
    
    private static final short quadIndices[] = { 0, 1, 1, 2, 2, 3, 3, 0 };
    
    private ByteBuffer mROIVerts = null;
    private ByteBuffer mROIIndices = null;
    
    public boolean mIsActive = false;
    
    // Reference to main activity *
    public TextReco mActivity;
    
    
    private int shaderProgramID;
    
    private int vertexHandle;
    
    private int mvpMatrixHandle;
    
    private Renderer mRenderer;
    
    private int lineOpacityHandle;
    
    private int lineColorHandle;
    
    private List<WordDesc> mWords = new ArrayList<WordDesc>();
    public float ROICenterX;
    public float ROICenterY;
    public float ROIWidth;
    public float ROIHeight;
    private int viewportPosition_x;
    private int viewportPosition_y;
    private int viewportSize_x;
    private int viewportSize_y;
    private ByteBuffer mQuadVerts;
    private ByteBuffer mQuadIndices;
    //textrenderer
    private int width = 100;                           // Updated to the Current Width + Height in onSurfaceChanged()
	private int height = 100;
	private float[] mProjMatrix = new float[16];
	private float[] mVMatrix = new float[16];
	private float[] mVPMatrix = new float[16];
	private int mTextureDataHandle;
    //Set Texture Handles and bind Texture
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    // Our matrices
 	
 	int count=0;
 	
 	//new
 	// Our matrices
 	private final float[] mtrxProjection = new float[16];
 	private final float[] mtrxView = new float[16];
 	private final float[] mtrxProjectionAndView = new float[16];
 	
 	// Geometric variables
 	public static float vertices[];
 	public static short indices[];
 	public static float uvs[];
 	public FloatBuffer vertexBuffer;
 	public ShortBuffer drawListBuffer;
 	public FloatBuffer uvBuffer;
 	private int[] texturenames ;
 	
 	// Our screenresolution
 	float	mScreenWidth = 1280;
 	float	mScreenHeight = 768;

 	// Misc
 	private Bitmap bitmap ;
 	private Bitmap tmpBitmap;
 	int mProgram;
 	private Word tmpword;
 	
 	private String tmpString="irfan";
 	
    boolean mIsDroidDevice = false;
    
 	
    
    public TextRecoRenderer(TextReco activity, SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
        
    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        // Call function to initialize rendering:
        initRendering();
        //init rendertext
       
        //bitmap = convertToText("irfandi");
		// Create the triangles
		SetupTriangle();
		// Create the image information
		SetupImage();
		initTextRender();
		
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
        {
            mWords.clear();
            mActivity.updateWordListUI(mWords);
            return;
        }
        
        // Call our function to render content
        renderFrame();
        //renderText();
        
        
        
        List<WordDesc> words;
        synchronized (mWords)
        {
            words = new ArrayList<WordDesc>(mWords);
        }
        
        Collections.sort(words);
        
        // update UI - we copy the list to avoid concurrent modifications
        mActivity.updateWordListUI(new ArrayList<WordDesc>(words));
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        mActivity.configureVideoBackgroundROI();

        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
        
        // We need to know the current width and height.
     	mScreenWidth = width;
     	mScreenHeight = height;
     		
     	// Redo the Viewport, making it fullscreen.
     	GLES20.glViewport(0, 0, (int)mScreenWidth, (int)mScreenHeight);
     	
     	// Clear our matrices
     	for(int i=0;i<16;i++)
     	{
     	   	mtrxProjection[i] = 0.0f;
     	   	mtrxView[i] = 0.0f;
     	   	mtrxProjectionAndView[i] = 0.0f;
     	}
     	
     	// Setup our screen width and height for normal sprite translation.
     	Matrix.orthoM(mtrxProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight, 0, 50);
     	    
     	// Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);
    }
    
  
    
    
    // Function for initializing the renderer.
    private void initRendering()
    {
        // init the vert/inde buffers
        mROIVerts = ByteBuffer.allocateDirect(4 * ROIVertices.length);
        mROIVerts.order(ByteOrder.LITTLE_ENDIAN);
        updateROIVertByteBuffer();
        
        mROIIndices = ByteBuffer.allocateDirect(2 * ROIIndices.length);
        mROIIndices.order(ByteOrder.LITTLE_ENDIAN);
        for (short s : ROIIndices)
            mROIIndices.putShort(s);
        mROIIndices.rewind();
        
        mQuadVerts = ByteBuffer.allocateDirect(4 * quadVertices.length);
        mQuadVerts.order(ByteOrder.LITTLE_ENDIAN);
        for (float f : quadVertices)
            mQuadVerts.putFloat(f);
        mQuadVerts.rewind();
        
        mQuadIndices = ByteBuffer.allocateDirect(2 * quadIndices.length);
        mQuadIndices.order(ByteOrder.LITTLE_ENDIAN);
        for (short s : quadIndices)
            mQuadIndices.putShort(s);
        mQuadIndices.rewind();
        
        
        
        mRenderer = Renderer.getInstance();
        
      //  GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
        //    : 1.0f);
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            LineShaders.LINE_VERTEX_SHADER, LineShaders.LINE_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        
        lineOpacityHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "opacity");
        lineColorHandle = GLES20.glGetUniformLocation(shaderProgramID, "color");
        
        //Set Texture Handles and bind Texture
        mTextureUniformHandle = GLES20.glGetAttribLocation(shaderProgramID, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgramID, "a_TexCoordinate");

        
    }
    
    
    private void updateROIVertByteBuffer()
    {
        mROIVerts.rewind();
        for (float f : ROIVertices)
            mROIVerts.putFloat(f);
        mROIVerts.rewind();
    }
    
    
    // The render function.
    public void renderFrame()
    {
    	
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
        {
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        } else
        {
        	 //GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera
        }
        //GLES20.glColorMask(true, false, true, true);;
        
        // enable blending to support transparency
        GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,
          //  GLES20.GL_ONE_MINUS_CONSTANT_ALPHA);
        
        // clear words list
        mWords.clear();
        
        
        // did we find any trackables this frame?
        
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            // get the trackable
            TrackableResult result = state.getTrackableResult(tIdx);
            
            Vec2F wordBoxSize = null;
            
            if (result.isOfType(WordResult.getClassType()))
            {
                WordResult wordResult = (WordResult) result;
                Word word = (Word) wordResult.getTrackable();
                Obb2D obb = wordResult.getObb();
                wordBoxSize = word.getSize();
                String wordU = word.getStringU();
                
                if (wordU != null)
                {
                    // in portrait, the obb coordinate is based on
                    // a 0,0 position being in the upper right corner
                    // with :
                    // X growing from top to bottom and
                    // Y growing from right to left
                    //
                    // we convert those coordinates to be more natural
                    // with our application:
                    // - 0,0 is the upper left corner
                    // - X grows from left to right
                    // - Y grows from top to bottom
                    float wordx = -obb.getCenter().getData()[1];
                    float wordy = obb.getCenter().getData()[0];
                    
                    if (mWords.size() < MAX_NB_WORDS)
                    {
                        mWords.add(new WordDesc(wordU,
                            (int) (wordx - wordBoxSize.getData()[0] / 2),
                            (int) (wordy - wordBoxSize.getData()[1] / 2),
                            (int) (wordx + wordBoxSize.getData()[0] / 2),
                            (int) (wordy + wordBoxSize.getData()[1] / 2)));
                    }
                    
                }
            } else
            {
                Log.d(LOGTAG, "Unexpected Detection : " + result.getType());
                continue;
            }
            //Set the active texture unit to texture unit 0.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            //Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

            //result.getTrackable().getName();
            Matrix44F mvMat44f = Tool.convertPose2GLMatrix(result.getPose());
            
            float[] mvMat = mvMat44f.getData();
            float[] mvpMat = new float[16];
            Matrix.translateM(mvMat, 0, 0, 0, 0);
            Matrix.scaleM(mvMat, 0, wordBoxSize.getData()[0] - TEXTBOX_PADDING,
                wordBoxSize.getData()[1] - TEXTBOX_PADDING, 1.0f);
          
            Matrix.multiplyMM(mvpMat, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, mvMat, 0);
            
            renderText(mvpMat,tmpString);
            /*
            GLES20.glUseProgram(shaderProgramID);
            GLES20.glLineWidth(3.0f);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                false, 0, mQuadVerts);
            
            
            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glUniform1f(lineOpacityHandle, 1.0f);
            //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
            GLES20.glUniform1i(mTextureUniformHandle, 0); 
            GLES20.glUniform3f(lineColorHandle,0f, 1f, 0f);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMat, 0);
            //LES20.glDrawElements(GLES20.GL_LINES, NUM_QUAD_OBJECT_INDICES,
              //  GLES20.GL_UNSIGNED_SHORT, mQuadIndices);
            //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glLineWidth(1.0f);
            GLES20.glUseProgram(0);
            */
            
            //draw texture
            
        }
        
        // Draw the region of interest
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        
        drawRegionOfInterest(ROICenterX, ROICenterY, ROIWidth, ROIHeight);
        
        GLES20.glUseProgram(0);
        //GLES20.glDisable(GLES20.GL_BLEND);
        
        mRenderer.end();
        
        
    }
    
    
    public void setROI(float center_x, float center_y, float width, float height)
    {
        ROICenterX = center_x;
        ROICenterY = center_y;
        ROIWidth = width;
        ROIHeight = height;
    }
    
    
    static String fromShortArray(short[] str)
    {
        StringBuilder result = new StringBuilder();
        for (short c : str)
            result.appendCodePoint(c);
        return result.toString();
    }
    
    
    public void setViewport(int vpX, int vpY, int vpSizeX, int vpSizeY)
    {
        viewportPosition_x = vpX;
        viewportPosition_y = vpY;
        viewportSize_x = vpSizeX;
        viewportSize_y = vpSizeY;
    }
    
    
    private void drawRegionOfInterest(float center_x, float center_y,
        float width, float height)
    {
    	
        // assumption is that center_x, center_y, width and height are given
        // here in screen coordinates (screen pixels)
        float[] orthProj = new float[16];
        setOrthoMatrix(0.0f, (float) viewportSize_x, (float) viewportSize_y,
            0.0f, -1.0f, 1.0f, orthProj);
        
        // compute coordinates
        float minX = center_x - width / 2;
        float maxX = center_x + width / 2;
        float minY = center_y - height / 2;
        float maxY = center_y + height / 2;
        
        // Update vertex coordinates of ROI rectangle
        ROIVertices[0] = minX - viewportPosition_x;
        ROIVertices[1] = minY - viewportPosition_y;
        ROIVertices[2] = 0;
        
        ROIVertices[3] = maxX - viewportPosition_x;
        ROIVertices[4] = minY - viewportPosition_y;
        ROIVertices[5] = 0;
        
        ROIVertices[6] = maxX - viewportPosition_x;
        ROIVertices[7] = maxY - viewportPosition_y;
        ROIVertices[8] = 0;
        
        ROIVertices[9] = minX - viewportPosition_x;
        ROIVertices[10] = maxY - viewportPosition_y;
        ROIVertices[11] = 0;
        
        updateROIVertByteBuffer();
        
        GLES20.glUseProgram(shaderProgramID);
        GLES20.glLineWidth(3.0f);
        
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false,
            0, mROIVerts);
        GLES20.glEnableVertexAttribArray(vertexHandle);
        
        GLES20.glUniform1f(lineOpacityHandle, 1.0f); // 0.35f);
        GLES20.glUniform3f(lineColorHandle, 0.0f, 1.0f, 0.0f);// R,G,B
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, orthProj, 0);
        
        // Then, we issue the render call
        GLES20.glDrawElements(GLES20.GL_LINES, NUM_QUAD_OBJECT_INDICES,
            GLES20.GL_UNSIGNED_SHORT, mROIIndices);
        
        // Disable the vertex array handle
        GLES20.glDisableVertexAttribArray(vertexHandle);
        
        // Restore default line width
        GLES20.glLineWidth(1.0f);
        
        // Unbind shader program
        GLES20.glUseProgram(0);
        
    }
    
    public Bitmap convertToText(String string) {
		/*
		final String text = string;
		Bitmap mTexture = BitmapFactory.decodeResource(mActivity.getApplicationContext().getResources(),
                R.drawable.black);

        Bitmap result = Bitmap.createBitmap(mTexture.getWidth(),
                mTexture.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(100);
        paint.setARGB(255, 0, 0, 0);
        canvas.drawColor(Color.WHITE);
        canvas.drawText(text,0, 200, paint);
        
        paint.setXfermode(new PorterDuffXfermode( PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(mTexture, 0, 0, paint);
        paint.setXfermode(null);
		
		return result;*/
    	
    	Bitmap result = Bitmap.createBitmap(500,
                 200, Bitmap.Config.ARGB_8888);
    	Canvas canvas = new Canvas(result);
		canvas.drawColor(Color.WHITE);

		Bitmap b = Bitmap.createBitmap(500, 200, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Paint paint = new Paint();
    	
		c.drawRect(0, 0, 500, 200, paint);
		
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		paint.setTextSize(100);
		paint.setTextScaleX(1.f);
        paint.setARGB(255, 255, 255, 255);
		paint.setAntiAlias(true);
		c.drawText(string, 0, 150, paint);
		//paint.setColor(Color.RED);

		canvas.drawBitmap(b, 0,50, paint);
    	return b;
	}
    
    private Bitmap combineImages(Bitmap bmp1, Bitmap bmp2) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom 
    		
    	Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight()-100, bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp2, 0, 0, null);
        canvas.drawBitmap(bmp1, new android.graphics.Matrix(), null);
        return bmOverlay;
		
    }
    
    
    private void initTextRender(){
    	
    	// Set the clear color to black
    	//GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);	
    	
    	GLES20.glEnable(GLES20.GL_BLEND);
    	GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    	
    	
    	// Create the shaders, solid color
    	int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_SolidColor);
    	int fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_SolidColor);

    	riGraphicTools.sp_SolidColor = GLES20.glCreateProgram();             // create empty OpenGL ES Program
    	GLES20.glAttachShader(riGraphicTools.sp_SolidColor, vertexShader);   // add the vertex shader to program
    	GLES20.glAttachShader(riGraphicTools.sp_SolidColor, fragmentShader); // add the fragment shader to program
    	GLES20.glLinkProgram(riGraphicTools.sp_SolidColor);                  // creates OpenGL ES program executables
    		    
    	// Create the shaders, images
    	vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_Image);
    	fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_Image);
    	
    	riGraphicTools.sp_Image = GLES20.glCreateProgram();             // create empty OpenGL ES Program
    	GLES20.glAttachShader(riGraphicTools.sp_Image, vertexShader);   // add the vertex shader to program
    	GLES20.glAttachShader(riGraphicTools.sp_Image, fragmentShader); // add the fragment shader to program
    	GLES20.glLinkProgram(riGraphicTools.sp_Image);                  // creates OpenGL ES program executables
    		    
    }
    
    public void SetupTriangle()
	{
		// We have to create the vertices of our triangle.
		vertices = new float[]
		           {210.0f, 200f, 0.0f,
					210.0f, 100f, 0.0f,
					300f, 100f, 0.0f,
					300f, 200f, 0.0f,
		           };
		
		indices = new short[] {0, 1, 2, 0, 2, 3}; // The order of vertexrendering.

		// The vertex buffer.
		/*
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		*/
		ByteBuffer bb = ByteBuffer.allocateDirect(textVertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(textVertices);
		vertexBuffer.position(0);
		
		// initialize byte buffer for the draw list
		ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(indices);
		drawListBuffer.position(0);
	}
    
    
    public void SetupImage()
	{
    	
    	// Create our UV coordinates.
    	uvs = new float[] {
    			0.0f, 0.0f,
    			0.0f, 1.0f,
    			1.0f, 1.0f,			
    			1.0f, 0.0f			
    	};
    			
    			
    	// The texture buffer
    	ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
    	bb.order(ByteOrder.nativeOrder());
    	uvBuffer = bb.asFloatBuffer();
    	uvBuffer.put(uvs);
    	uvBuffer.position(0);
    			
    	// Generate Textures, if more needed, alter these numbers.
    	
    	 texturenames = new int[1];
     	GLES20.glGenTextures(1, texturenames, 0);
     	
    	
	}
    private void updateImage(String text){
    	
    	try {
			
    		if(!mActivity.translatedNow.isEmpty()){
    				tmpString = mActivity.translatedNow;
    			
    		}
			
		} catch (Exception e) {
			// TODO: handle exception
			tmpString = "Loading ..";
			
		}
    	/*
    	bitmap = combineImages(
    			convertToText(tmpString), 
    			BitmapFactory.decodeResource(mActivity.getApplicationContext().getResources(),R.drawable.white)
    		8			);
    	*/
    	bitmap = convertToText(tmpString);
		
    	//bitmap = convertToText(tmpString);w
    	// Bind texture to texturenameh
    	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
    	
    	// Set filtering
    	GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    	GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    	        
    	// Load the bitmap into the bound texture.
    	GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
    	        
    	// We are done using the bitmap so we should recycle it.
    	tmpBitmap= bitmap;
    	bitmap.recycle();
    	bitmap = null;
    }
    
    private void renderText(float[] mvMat,String text){
    	
    	updatePos(mvMat);
    	updateImage(text);
    	render(mvMat);
    	
    }
    
    private void updatePos(float[] mvMat){
    	try {
    		// The vertex buffer.
    		ByteBuffer bb = ByteBuffer.allocateDirect(textVertices.length * 4);
    		bb.order(ByteOrder.nativeOrder());
    		vertexBuffer = bb.asFloatBuffer();
    		vertexBuffer.put(textVertices);
    		vertexBuffer.position(0);
    		//Log.e("righttopX:"+rec.getLeftTopX(), null);
		} catch (Exception e) {
			// TODO: handle exception
			//Log.e("updatepos:("+mvMat[0]+","+mvMat[1]+","+mvMat[2]+","+mvMat[3]+")", e.toString());
		}
    	
    }
    
    
    private void render(float[] matrik) {
		
    	//mWords.get(0).
    	// Set our shader programm
    	GLES20.glUseProgram(riGraphicTools.sp_Image);
    	
        // get handle to vertex shader's vPosition member
	    int mPositionHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");
	    
	    // Enable generic vertex attribute array
	    GLES20.glEnableVertexAttribArray(mPositionHandle);

	    // Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, 3,
	                                 GLES20.GL_FLOAT, false,
	                                 0, vertexBuffer);
	    //GLES20.glVertexAttribPointer(mPositionHandle, 3,
	      //                           GLES20.GL_FLOAT, false,
	        //                         0, mQuadVerts);
	    
	    // Get handle to texture coordinates location
	    int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_texCoord" );
	    
	    // Enable generic vertex attribute array
	    GLES20.glEnableVertexAttribArray ( mTexCoordLoc );
	    
	    // Prepare the texturecoordinates
	    GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false, 
                0, uvBuffer);
	    
	    // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, matrik, 0);
        
        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image, "s_texture" );
        
        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i ( mSamplerLoc, 0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);

	    
    	// Set our shader programm
    	GLES20.glUseProgram(0);
        	
	}
    
    
    
    class WordDesc implements Comparable<WordDesc>
    {
        public WordDesc(String text, int aX, int aY, int bX, int bY)
        {
            this.text = text;
            this.Ax = aX;
            this.Ay = aY;
            this.Bx = bX;
            this.By = bY;
        }
        
        String text;
        int Ax, Ay, Bx, By;
        
        
        @Override
        public int compareTo(WordDesc w2)
        {
            WordDesc w1 = this;
            int ret = 0;
            
            // we check first if both words are on the same line
            // both words are said to be on the same line if the
            // mid point (on Y axis) of the first point
            // is between the values of the second point
            int mid1Y = (w1.Ay + w1.By) / 2;
            
            if ((mid1Y < w2.By) && (mid1Y > w2.Ay))
            {
                // words are on the same line
                ret = w1.Ax - w2.Ax;
            } else
            {
                // words on different line
                ret = w1.Ay - w2.Ay;
            }
            Log.e(LOGTAG, "Compare result> " + ret);
            return ret;
        }
    }
    
    
    private void setOrthoMatrix(float nLeft, float nRight, float nBottom,
        float nTop, float nNear, float nFar, float[] _ROIOrthoProjMatrix)
    {
        for (int i = 0; i < 16; i++)
            _ROIOrthoProjMatrix[i] = 0.0f;
        
        _ROIOrthoProjMatrix[0] = 2.0f / (nRight - nLeft);
        _ROIOrthoProjMatrix[5] = 2.0f / (nTop - nBottom);
        _ROIOrthoProjMatrix[10] = 2.0f / (nNear - nFar);
        _ROIOrthoProjMatrix[12] = -(nRight + nLeft) / (nRight - nLeft);
        _ROIOrthoProjMatrix[13] = -(nTop + nBottom) / (nTop - nBottom);
        _ROIOrthoProjMatrix[14] = (nFar + nNear) / (nFar - nNear);
        _ROIOrthoProjMatrix[15] = 1.0f;
        
    }
    
}
