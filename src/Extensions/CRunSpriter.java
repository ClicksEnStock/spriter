package Extensions;

import Actions.CActExtension;
import Application.CRunApp;
import Application.CRunApp.MenuEntry;
import Banks.CImage;
import Conditions.CCndExtension;
import Expressions.CValue;
import OpenGL.GLRenderer;
import RunLoop.CCreateObjectInfo;
import Runtime.MMFRuntime;
import Services.CBinaryFile;
import Services.CFile;
import Objects.CObject;
import Animations.CAnimDir;
import com.brashmonkey.spriter.SCMLReader;
import com.brashmonkey.spriter.Player;
import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Loader;
import com.brashmonkey.spriter.Data;
import com.brashmonkey.spriter.Animation;
import com.brashmonkey.spriter.CF25Drawer;
import com.brashmonkey.spriter.CF25Loader;
import com.brashmonkey.spriter.Folder;
import com.brashmonkey.spriter.File;
import com.brashmonkey.spriter.Entity.CharacterMap;
import android.graphics.Bitmap;

public class CRunSpriter extends CRunExtension
{
	public boolean isValid;
	public boolean visible;
	public boolean isFlipped;
	public boolean animPlaying;
	public long deltaTime; 
 	public long currentSystemTime; 
	
	String scmlFileName = null; 
	String scmlFileString = null;
	Player player = null;
	CF25Loader loader = null;
	Drawer<Short> drawer = null;
	Data data = null;
	String lastError = ""
	String errorTemp = ""
	float speedRatio = 1.0
	String extSourcePath = "";
	
	public HashMap <String,short> SoundBank; // HashMap<sound path in spriter file, sound id in CF25 sound bank>
	public HashMap <String,CObject> BoxLink;
	public HashMap <String,short> SoundEvent;
	public HashMap <String,short> TriggerEvent;
	
	/* missing parameters compared to C++ version
	public HashMap <String,short> SpriteSource;
 	public CRect displayRect; 
*/
	
	@Override 
	public int getNumberOfConditions()
	{
		return 8;
	}
	
    @Override
    public boolean createRunObject(CBinaryFile file, CCreateObjectInfo cob, int version)
    {
	
		/*_snwprintf_s(lastError, _countof(lastError), ERRORSIZE, ErrorS[noError]); 
		_snwprintf_s(errorTemp, _countof(errorTemp), ERRORSIZE, ErrorS[noError]); 
		deltaTime = 0; 
		currentSystemTime = GetTickCount(); 
		animPlaying = false; 
		flipX = false; 
		speedRatio = 1.0f; 
		displayRect = { 0, 0, 0, 0 }; 
		scmlObj = scmlModel->getNewEntityInstance(0);//assume first entity at start 
		extSourcePath = _T(""); 
		*/
		visible = true;
		isFlipped = false;
		animPlaying = false;
		isValid = false;
		deltaTime = 0; 
		currentSystemTime = System.currentTimeMillis();
		
		scmlFileName = file.readString(256);
		file.bUnicode=false;
    	file.skipBytes(8);
		scmlFileString = file.readString();
		SCMLReader reader = new SCMLReader(scmlFileString);
		data = reader.getData();
		player = new Player(data.getEntity(0)); //assume first entity on start
		player.speed = 0;//animation will only be updated through time parameter
		player.characterMaps = new CharacterMap[1];
    	loader = new CF25Loader(data,this);
		loader.load(scmlFileName);
		drawer = new CF25Drawer(loader, this);
		isValid = true;
        return false;
    }

    @Override
    public void displayRunObject()
    {
    	if (isValid)
    	{
			long time = System.currentTimeMillis();
			deltaTime = time - currentSystemTime;
			currentSystemTime = time;
			if (!animPlaying)
			{
				deltaTime = 0;
			}
			//deltaTime=20;
			player.setTime(player.getTime()+(int)(deltaTime*speedRatio));
			if(isFlipped)
			{
				player.flipX();
			}
			player.setPosition(ho.hoX,ho.hoY);
			player.setAngle(ho.roc.rcAngle);
			player.setScale(ho.roc.rcScaleX);
			player.update();
			if (visible)
			{
				drawer.draw(player);
			}
    	}
    }
    
    @Override
    public void getZoneInfos()
    {
    	/*if (isValid)
    	{
			CImage image = ho.getImageBank ().getImageFromHandle (imageList [0]);

            if (image != null)
            {
			    ho.hoImgWidth = image.getWidth();
			    ho.hoImgHeight = image.getHeight();
            }
    	}
    	else
    	{
    		ho.hoImgWidth = 1;
    		ho.hoImgHeight = 1;
    	}*/
    }
    
    @Override
    public boolean condition (int num, CCndExtension cnd)
    {
    	switch (num)
    	{
    	case 0:// IsAnimationPlayingByName
			return (player.getAnimation().name == cnd.getParamExpString(rh, 0));
		case 1:// HasCurrentAnimationFinished
			return player.IsCurrentAnimationFinished();
		case 2:// IsTagActive
			return false;//not supported on Android
		case 3:// IsObjectTagActive
			return false;//not supported on Android
		case 4:// OnSoundEvent
			return false; //not supported on Android
		case 5:// OnTriggerEvent
			return false; //not supported on Android
		case 6:// IsAnimationFlipped
			return isFlipped;
		case 7:// CompareCurrentKeyFrameToValue
			return (player.getAnimation().getCurrentKey().id == cnd.getParamExpression(rh, 0));	
    	};
    	
    	return false;
    }

	private boolean loadFromActiveObject(CObject pHo, int nAnim, int nDir, int nFrame, int folderId, int fileId)
	{
		CImage newImage = null;
		if (nAnim >= pHo.hoCommon.ocAnimations.ahAnimMax || nDir>32 || nDir<0)
		{
			return false;
		}
		CAnimDir adPtr = pHo.hoCommon.ocAnimations.ahAnims[nAnim].anDirs[nDir];
		if(adPtr==null)
		{
			return false;
		}
		if (nFrame < adPtr.adNumberOfFrame)
		{
			short frame = adPtr.adFrames[nFrame];
			newImage = ho.getImageBank().getImageFromHandle(frame);
		}
		//unload former image if needed and add new image
		if(newImage == null)
		{
			return false;
		}
		int[] mImage = newImage.getRawPixels();
		if(mImage == null)
		{
			return false;
		}
		Bitmap bImage = Bitmap.createBitmap(newImage.getWidth(), newImage.getHeight(), newImage.getFormat());
		bImage.setPixels(mImage, 0, newImage.getWidth(), 0, 0, newImage.getWidth(), newImage.getHeight());
		short newImg = rh.rhApp.imageBank.addImage(bImage, (short) 0, (short) newImage.getHeight(), (short) 0, (short) 0);
		// Replace old image by new one
		adPtr.adFrames[nFrame] = newImg;
		if(newImg!=-1)
		{
			loader.loadImage(newImg,folderId,fileId);
		}
		return true;
	}

    @Override
    public void action (int num, CActExtension act)
    {
    	switch (num)
    	{
    		case 0://StopAnimation
    		{
				animPlaying = false;
				return;
			}
    		
    		case 1://StartAnimation    			
    		{
				animPlaying = true;
				currentSystemTime = System.currentTimeMillis();
				return;
    		}

    		case 2://Change animation by name    			
    		{
				String name = act.getParamExpString(rh, 0);
				player.setAnimation(name);
				return;
    		}
    		
    		case 3://Change animation time
    		{
				int time = act.getParamExpression(rh, 0);
    			player.setTime(time);
    			return;
    		}

    		case 4://SetFlipX
    		{
				boolean flipX = (act.getParamExpression(rh, 0)!=0);
    			isFlipped = flipX;
    			return;
    		}
			
			case 5://SetAnimationSpeed
    		{
				float speed = act.getParamExpFloat(rh, 0);
				speedRatio = speed;
    			return;
    		}
			
			case 6://ApplyCharacterMap
    		{
				String map = act.getParamExpString(rh, 0);
				player.characterMaps[0] = player.getEntity().getCharacterMap(map);
    			//TODO: use a list for characterMaps instead of array to remove and add easily
    			return;
    		}
			
			case 7://SetScale
    		{
				ho.roc.rcScaleX = act.getParamExpFloat(rh, 0);
				ho.roc.rcScaleY = ho.roc.rcScaleX;
    			return;
    		}
			
			case 8://SetAngle
    		{
				ho.roc.rcAngle = act.getParamExpFloat(rh, 0);
    			return;
    		}
			
			case 9://LoadOneSpriteFromActive
    		{
				String spriteName = act.getParamExpString(rh, 0);
				CObject pHo = act.getParamObject(rh, 1);
				int nAnim = act.getParamExpression(rh, 2);
				int dir = act.getParamNewDirection(rh,3);
				int nFrame = act.getParamExpression(rh, 4);
				int folderId =0;
				int fileId = 0;
				String[] parts = spriteName.split("/");
				Folder folder = data.getFolder(parts[0]);
				if(folder == null)
				{
					return;
				}
				folderId = folder.id;
				File file = folder.getFile(spriteName);
				if(file == null)
				{
					return;
				}
				fileId = file.id;
				int nDir = 0;
				for(int i=0;i<32;i++)
				{
					if((dir & (int)Math.pow(2,i))!=0)
					{
						nDir=i;
						break;
					}
				}
				//load image from active object
				boolean res = loadFromActiveObject(pHo,nAnim,nDir,nFrame,folderId,fileId);
				return;
    		}
			case 10://LoadOrderedSpritesPerAnimation
    		{
				CObject pHo = act.getParamObject(rh, 0);
				int nAnim = act.getParamExpression(rh, 1);
				for(int i=0;i<data.getFolderCount();i++){
					for(int j=0;j<data.getFileCount(i);j++){
						if(data.getFile(i,j)!= null) {
							boolean res = loadFromActiveObject(pHo, nAnim, i, j, i, j);
						}
					}
				}
				return;
    		}
			
			case 11://ChangeEntityByNumber
    		{
				int numEntity = act.getParamExpression(rh, 0);
				player.setEntity(data.getEntity(numEntity));
    			return;
    		}
			
			case 12://ChangeAnimationByNameWithBlending TCHAR* name, int blendingTime
    		{
				String name = act.getParamExpString(rh, 0);
				int blendingTime = act.getParamExpression(rh, 1);
				player.setAnimation(name);
    			return;
    		}
			
			case 13://RemoveCharacterMap
    		{
				String map = act.getParamExpString(rh, 0);
				//player.getEntity.getCharacterMap(map)
				player.characterMaps[0] = null;
    			return;
    		}
			
			case 14://RemoveAllCharacterMaps
    		{
				player.characterMaps[0] = null;
    			return;
			}
			
			case 15://BoundBoxToObject
    		{
				String name = act.getParamExpString(rh, 0);
				CObject pHo = act.getParamObject(rh, 1);
				return;
			}
			
			case 16://UnboundBoxFromObject
    		{
				return;
			}
			
			case 17://SetDebug
    		{
				return;
			}
			
			case 18://LoadOrderedSpritesPerDirection
    		{
				CObject pHo = act.getParamObject(rh, 0);
				int nAnim = act.getParamExpression(rh, 1);
				int dir = act.getParamNewDirection(rh,2);
				int nDir = 0;
				for(int i=0;i<32;i++)
				{
					if((dir & (int)Math.pow(2,i))!=0)
					{
						nDir=i;
						break;
					}
				}
				int nFiles = 0;
				for(int i=0;i<data.getFolderCount();i++){
					for(int j=0;j<data.getFileCount(i);j++){
						if(data.getFile(i,j)!= null) {
							boolean res = loadFromActiveObject(pHo, nAnim, nDir, nFiles, i, j);
						}
						nFiles++;
					}
				}
				return;
			}
			
			case 19://LoadScmlFileWithoutExternalFiles
    		{
				String filename = act.getParamExpString(rh, 0);
				scmlFileString = file.readString();
				SCMLReader reader = new SCMLReader(scmlFileString);
				return;
			}
			
			case 20://ChangeEntityByName
    		{
				String name = act.getParamExpString(rh, 0);
				player.setEntity(data.getEntity(name));
				return;
			}
			
			case 21://ChangeKeyFrame
    		{
				int keynum = act.getParamExpression(rh, 0);
				player.setTime(player.getAnimation().getKey(keynum).time);
				return;
			}
			
			case 22://JumpToNextKeyFrame
    		{
				int keynum = player.getAnimation().getcurrentKey().id + 1;
				player.setTime(player.getAnimation().getKey(keynum).time);
				return;
			}
			
			case 23://JumpToPreviousKeyFrame
    		{
				int keynum = player.getAnimation().getcurrentKey().id - 1;
				player.setTime(player.getAnimation().getKey(keynum).time);
				return;
			}
			
			case 24://ClearLastError
    		{
				lastError = "";
				return;
			}
			
			case 25://LoadSpriteFromExternal
    		{
				// does it make sense to load files externally ?
				return;
			}
			
			case 26://SetSpriteRelativePath
    		{
				return;
			}
    	};    	
    }
    
    @Override
    public CValue expression (int num)
    {
    	switch (num)
    	{
	    	case 0: //LastError
	    		return new CValue (lastError);
	    	case 1: //GetAngle
	    		return new CValue (ho.roc.rcAngle);
	    	case 2: //GetScale
    			return new CValue (ho.roc.rcScaleX);
			case 3: //CurrenTime
    			return new CValue (player.getTime());
			case 4: //CurrentSpeedRatio
    			return new CValue (speedRatio);
			case 5: //CurrentAnimationName
    			return new CValue (player.animation.name);
			case 6: //GetRealVariable
				String name = ho.getExpParam().getString();
    			return new CValue (ho.hoY);
			case 7: //GetObjectRealVariable
    			return new CValue (ho.hoY);
			case 8: //GetIntVariable
    			return new CValue (ho.hoY);
			case 9: //GetObjectIntVariable
    			return new CValue (ho.hoY);
			case 10: //GetStringVariable
    			return new CValue (ho.hoY);
			case 11: //GetObjectStringVariable
    			return new CValue (ho.hoY);
			case 12: //GetPointPosX
    			return new CValue (ho.hoY);
			case 13: //GetPointPosY
    			return new CValue (ho.hoY);
			case 14: //GetPointAngle
    			return new CValue (ho.hoY);
			case 15: //CurrentEntityName
    			return new CValue (ho.hoY);
			case 16: //DeltaTimeMs
    			return new CValue (ho.hoY);
			case 17: //CurrentKeyFrame
    			return new CValue (ho.hoY);
			
    	};
    	
    	return new CValue (0);
    }
   
}
