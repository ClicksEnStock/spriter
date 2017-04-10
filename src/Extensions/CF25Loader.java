package com.brashmonkey.spriter;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import com.brashmonkey.spriter.Data;
import com.brashmonkey.spriter.FileReference;
import com.brashmonkey.spriter.Loader;
//CF25
import Extensions.CRunExtension;
import Banks.CImage;
import Services.CFile;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CF25Loader extends Loader<Short> {
	
	CRunExtension ext;
	
	public CF25Loader(Data data,CRunExtension extPtr){
		super(data);
		ext = extPtr;
	}

	public void loadImage(short img, int folderId, int fileId)
	{
		FileReference ref = new FileReference(folderId,fileId);
		if(img!=-1)
		{
			super.resources.put(ref, img);
		}
	}
	
	@Override
	protected Short loadResource(FileReference ref) {
		String pathPrefix;
		if(super.root == null || super.root.equals("")) {
			pathPrefix = "";
		} else {
			pathPrefix = super.root + File.separator;
		}
		String path = pathPrefix + data.getFile(ref).name;
		
		//CF2.5
		Bitmap img;

        try
        {   
			img = BitmapFactory.decodeFile (path);
        }
        catch (OutOfMemoryError e)
        {   return -1;
        }
		if(img==null)
		{
			return -1;
		}
		int dwWidth = img.getWidth();
        int dwHeight = img.getHeight();
        if (dwWidth <= 0 || dwHeight <= 0)
        {
            return -1;
        }
		// Create image
		int xHS = 0;
		int yHS = 0; //dwHeight;
		int xAP = 0;
		int yAP = 0;
        short newImg = ext.rh.rhApp.imageBank.addImage(img, (short) xHS, (short) yHS, (short) xAP, (short) yAP);

		if(newImg!=-1)
		{
			super.resources.put(ref, newImg);
		}
		return newImg;
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
	protected void finishLoading() {

	}
}
