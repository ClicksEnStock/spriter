package com.brashmonkey.spriter;

import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Loader;
import com.brashmonkey.spriter.Timeline.Key.Object;
//CF2.5
import Banks.CImage;
import OpenGL.GLRenderer;
import Extensions.CRunExtension;

public class CF25Drawer extends Drawer<Short>{
	
	private int color;
	CRunExtension ext;
	
	public CF25Drawer(Loader<Short> loader, CRunExtension extPtr){
		super(loader);
		ext = extPtr;
	}
	
	@Override
	public void setColor(float r, float g, float b, float a) {
		//color = Color.argb(r,g,b,a);
	}
	
	@Override
	public void rectangle(float x, float y, float width, float height) {
		/* ES 2.0 function renderRect exists but implementation seems empty */
		GLRenderer.inst.renderRect((int)x, (int)y, (int)width, (int)height, color, 2);//2 for thickness
	}
	
	@Override
	public void line(float x1, float y1, float x2, float y2) {
		GLRenderer.inst.renderLine((int)x1, (int)y1, (int)x2, (int)y2, color, 2);//2 for thickness
	}

	@Override
	public void circle(float x, float y, float radius) {
		 /* No support in ES2Renderer */
		 /*gl.glBegin(GL.GL_LINE_LOOP);
		 for(int i =0; i <= 300; i++){
		 double angle = 2 * Math.PI * i / 300;
		 double x = Math.cos(angle);
		 double y = Math.sin(angle);
		 gl.glVertex2d(x,y);
		 }
		 gl.glEnd();*/ 
	}

	@Override
	public void draw(Object object) {
		short imgId = loader.get(object.ref);
		if(imgId==-1)
		{
			return;
		}
		CImage image = ext.ho.getImageBank().getImageFromHandle(imgId);
		if(image==null)
		{
			return;
		}
		float newPivotX = (image.getWidth() * object.pivot.x);
		float newX = object.position.x;// - newPivotX;
		float newPivotY = (image.getHeight() * object.pivot.y);
		float newY = object.position.y;// - newPivotY;
		
    	if(image != null)
		{
    		if(object.alpha == 0)
			{
				//public abstract void renderScaledRotatedImage(ITexture image, float angle, float sX, float sY, int hX, int hY, int x, int y, int w, int h, int inkEffect, int inkEffectParam);
				GLRenderer.inst.renderScaledRotatedImage
                (image, object.angle, object.scale.x,
                                 object.scale.y,
                                 (int)newPivotX,
                                 (int)newPivotY,
                                 (int)newX,
                                 (int)newY,
                                 image.getWidth(),
                                 image.getHeight(),
                                 ext.ho.ros.rsEffect,
                                 ext.ho.ros.rsEffectParam);
			}
			else
			{
				GLRenderer.inst.renderScaledRotatedImage
					(image, object.angle, object.scale.x,
									 object.scale.y,
									 (int)newPivotX,
									 (int)newPivotY,
									 (int)newX,
									 (int)newY,
									 image.getWidth(),
									 image.getHeight(),
									 GLRenderer.BOP_MASK,
									 (int)(1.28f*object.alpha));
			}
		}
	}
}
