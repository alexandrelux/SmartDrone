package com.example.circles;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.navdata.Altitude;
import de.yadrone.base.navdata.AltitudeListener;

public class MainActivity extends Activity implements CvCameraViewListener2/*, AltitudeListener*/{
	
	private static String TAG = "Cibles";

	private CameraBridgeViewBase mOpenCvCameraView;
	
	//Debug
	private static boolean DEBUG = false;
	
	//Drone
	IARDrone drone;
	
	boolean isFlying = false;
	
	//Pour la direction
	private int direction;
	
	//Pour le sms receiver
	private SMSRECEIVER receiversms;
	
	// Vitesse du drone
	private int speed = 15;
	private int duree = 7;
	
	// Altitude
	private int atteint = 0;
	private final int MAX_ALTITUDE = 1500;
	
	// Etat
	private int etat = 0;
	
	//Timer
	private long startTime = 0;
	private long timeInMilliseconds = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		if(DEBUG)
			Log.i(TAG, "Appel de onCreate");
		
		super.onCreate(savedInstanceState);

		//Empêcher l'écran de s'éteindre
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//spécifier le "main" 
		setContentView(R.layout.activity_main);
		
		//Afficher à  l'écran le flux video
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.MyCameraView);
		//mOpenCvCameraView.setCameraIndex(1); //Camera Facial
		mOpenCvCameraView.setMaxFrameSize(800, 600); // Resolution
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		// Declarer le smsreceiver
		receiversms = new SMSRECEIVER ();
		registerReceiver(receiversms,new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		
		// Initialisation du drone et des boutons
		initialize();
		initButtons();
		
		// Mettre le Listener pour les sms
		receiversms.setListener(new NewsUpdateListener() {
            public void onComplete() {
                	drone.takeOff();
                	isFlying = true;
                	Toast.makeText(getBaseContext(), "SMS Recu !",Toast.LENGTH_SHORT).show();
                	try {
    					Thread.sleep(2000);
    				} catch (InterruptedException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}	
            	
            	//drone.landing();
		            	
		        /*Thread thread = new Thread() {
				    @Override
				    public void run() {
				        try {
				        	int DG = 0;
				        	int HB = 0;
				            while(true) {
								DG = direction/10; //Pour droite gauche
								HB = direction % 10;	//Pour haut reculer
				            	PositionDrone(DG,HB);
				            }
				        } catch (InterruptedException e) {
				            e.printStackTrace();
				        }
				    }
				};
		
				thread.start(); */
		            	 
		            	 
            }           

            public void Reset() {
            	drone.reset();
            	etat = 0;
            }
            
            public void onError(Throwable error) {
            	Toast.makeText(getBaseContext(), "Erreur",Toast.LENGTH_SHORT).show();
            }
		});
		
		/*
		initialize();
		initButtons();
		Thread thread = new Thread() {
		    @Override
		    public void run() {
		        try {
		        	int DG = 0;
		        	int HB = 0;
		            while(true) {
						DG = direction/10; //Pour droite gauche
						HB = direction % 10;	//Pour haut reculer
		            	PositionDrone(DG,HB);
		            }
		        } catch (InterruptedException e) {
		            e.printStackTrace();
		        }
		    }
		};

		thread.start();*/
		
	}
	
	private void initButtons()
	{
		try
		{
			//Message POP-UP
			Toast.makeText(getBaseContext(), "Initialisation BOUTON",Toast.LENGTH_SHORT).show();
			final Button takeOff = (Button)findViewById(R.id.button1);
			takeOff.setOnClickListener(new OnClickListener() {
				public void onClick(View v)
				{
					if (!isFlying)
					{
						drone.takeOff();
						takeOff.setText("Landing");
						isFlying = true;
					}
					else
					{
						takeOff.setText("Take Off");
						//Landing();
						drone.landing();
						isFlying = false;
					}
				}
			});
			
			final Button emergency = (Button)findViewById(R.id.button2);
			emergency.setOnClickListener(new OnClickListener() {	   
				public void onClick(View v)
				{
					drone.reset();
					etat = 0;
				}
			});
			
			final Button x = (Button)findViewById(R.id.button3);
			x.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						Toast.makeText(getBaseContext(), "X",Toast.LENGTH_SHORT).show();
						drone.move3D(25, 0, 0, 0);
						}
					else if (event.getAction() == MotionEvent.ACTION_UP)
		                drone.hover();

					return true;
				}
			});
			
			final Button y = (Button)findViewById(R.id.button4);
			y.setOnTouchListener(new OnTouchListener() {
							public boolean onTouch(View v, MotionEvent event)
							{
								if(event.getAction() == MotionEvent.ACTION_DOWN){
									Toast.makeText(getBaseContext(), "Y",Toast.LENGTH_SHORT).show();
									drone.move3D(0, 25, 0, 0);
								}
								else if (event.getAction() == MotionEvent.ACTION_UP)
					                drone.hover();

								return true;
							}
						});
			
			final Button z = (Button)findViewById(R.id.button5);
			z.setOnTouchListener(new OnTouchListener() {
							public boolean onTouch(View v, MotionEvent event)
							{
								if(event.getAction() == MotionEvent.ACTION_DOWN){
									Toast.makeText(getBaseContext(), "Z",Toast.LENGTH_SHORT).show();
									drone.move3D(0, 0, 25, 0);
								}
								else if (event.getAction() == MotionEvent.ACTION_UP)
					                drone.hover();

								return true;
							}
						});
			final Button spin = (Button)findViewById(R.id.button6);
			spin.setOnTouchListener(new OnTouchListener() {   
							public boolean onTouch(View v, MotionEvent event)
							{
								if(event.getAction() == MotionEvent.ACTION_DOWN){
									Toast.makeText(getBaseContext(), "SPIN",Toast.LENGTH_SHORT).show();
									drone.move3D(0, 0, 0, 25);
								}
								else if (event.getAction() == MotionEvent.ACTION_UP)
					                drone.hover();

								return true;
							}
						});
	}	
	    catch (Exception exc)
		{
			Toast.makeText(getBaseContext(), " ECHEC Initialisation BOUTON",Toast.LENGTH_SHORT).show();
			exc.printStackTrace();
		}
	}
	
	/**
	 * Initialisation du drone
	 */
	private void initialize()
    {	
		
        //WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		//text.append("\nConnected to " + wifi.getConnectionInfo().getSSID());
		//Message POP-UP
		Toast.makeText(getBaseContext(), "Initialisation",Toast.LENGTH_SHORT).show();
	    try
	    {	
	    	drone =  new ARDrone("192.168.1.1", null); 
	        drone.start();
	        //drone.getNavDataManager().addAltitudeListener(this);
	        drone.getCommandManager().setMaxAltitude(MAX_ALTITUDE);
			Toast.makeText(getBaseContext(), "Initialisation Reussi",Toast.LENGTH_SHORT).show();
	        
	    }
	    catch (Exception exc)
		{
			Toast.makeText(getBaseContext(), "Initialisation FAIL",Toast.LENGTH_SHORT).show();
			exc.printStackTrace();
		}
	}
	
	/**
	 *Methode appeler quand une nouvel frame arrive
	 **/
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
			Mat mRgba = inputFrame.rgba();
			
			//On convertie l'image en noir/blanc
			Mat mGray = inputFrame.gray();

			//Utilisation du code cpp
			float resultat = process(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());	
			System.out.println("Resultat  toto: " + resultat);
			int nbcibles = (int)resultat;
			System.out.println("Resultat  nbcircles: " + nbcibles);
			direction = (int)(resultat*100)-(nbcibles*100);
			
			//Si la cible est détecté
			if(nbcibles == 1 && direction >= 0 && direction < 100)
			{
				System.out.println("Resultat  direction: " + direction);
				final int DG = direction/10; //Pour droite gauche
				final int HB = direction % 10;	//Pour haut reculer
				PositionDrone(DG,HB);
			}
			
			//Si la cible n'est pas detecté
			/*else if (nbcibles != 1)
			{
				ChercheCible();
			}*/
			
			if(DEBUG)
				Log.e(TAG , nbcibles+" cible trouve");
			

			
		//Afficher l'image Ã  l'Ã©cran
		return mRgba;
			//return mGray;
	}

	//Fonction cpp pour le traitement d'image
	private static native float process(long matAddrGr, long matAddrRgba);

	/**
	 *Methode pour positionner le drone suivant la position de la cible
	 **/
	private void PositionDrone(int DG, int HB) 
	{
		try{
		if (DG == 5)
		{
				Button takeOff = (Button)findViewById(R.id.button1);
	        	takeOff.setText("Take Off");
				//Landing();
	        	drone.landing();
		}
		
		else if (HB == 8 && DG == 4)
		{
			for(int i = 0 ; i < duree ; i++)
			{
				drone.move3D(speed, speed, 0, 0);
				Thread.sleep(30);
			}
		}
		
		else if (HB == 8 && DG == 6)
		{
			for(int i = 0 ; i < duree ; i++)
			{
				drone.move3D(speed, -speed, 0, 0);
				Thread.sleep(30);
			}
		}
		
		else if (HB == 2 && DG == 4)
		{
			for(int i = 0 ; i < duree ; i++)
			{
				drone.move3D(-speed, speed, 0, 0);
				Thread.sleep(30);
			}
		}
		
		else if (HB == 2 && DG == 6)
		{
			for(int i = 0 ; i < duree ; i++)
			{
				drone.move3D(-speed, -speed, 0, 0);
				Thread.sleep(30);
			}
		}
		
		else if(HB == 8)
		{
			for(int i = 0 ; i < duree ; i++)
			{
				drone.move3D(speed, 0, 0, 0);
				Thread.sleep(30);
			//	System.out.println("Temps : " + timeInMilliseconds + " Commande : avancer" + i);
			}
			/*startTime = SystemClock.uptimeMillis();
			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			do{
				timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
				drone.move3D(speed, 0, 0, 0);
			}while (timeInMilliseconds<300);
			*/
		}
		else if (HB == 2)
			{
			for(int i = 0 ; i < duree ; i++)
			{
				drone.move3D(-speed, 0, 0, 0);
				Thread.sleep(30);
			//	System.out.println("Temps : " + timeInMilliseconds + " Commande : reculer" + i);
			}
				/*startTime = SystemClock.uptimeMillis();
				timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
				do{
					timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
					drone.move3D(-25, 0, 0, 0);
				}while (timeInMilliseconds<100);*/
			}

			/*startTime = SystemClock.uptimeMillis();
			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			do{
				timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
				drone.hover();
			}while (timeInMilliseconds<100);*/
		
			
			else if(DG == 4)
			{
				for(int i = 0 ; i < duree ; i++)
				{
					drone.move3D(0, speed, 0, 0);
					Thread.sleep(30);
				//	System.out.println("Temps : " + timeInMilliseconds + " Commande : gauche" + i);
				}
				/*startTime = SystemClock.uptimeMillis();
				timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
				do{
					timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
					drone.move3D(0, 25, 0, 0);
				}while (timeInMilliseconds<100);*/
			}
			
			else if (DG == 6)
			{
				for(int i = 0 ; i < duree ; i++)
				{
					drone.move3D(0, -speed, 0, 0);
					Thread.sleep(30);
				//	System.out.println("Temps : " + timeInMilliseconds + " Commande : droite" + i);
				}
				/*startTime = SystemClock.uptimeMillis();
				timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
				do{
					timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
					drone.move3D(0, -25, 0, 0);
				}while (timeInMilliseconds<100);*/
			}
			
		/*startTime = SystemClock.uptimeMillis();
		timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
		do{
			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			drone.hover();
		}while (timeInMilliseconds<500);*/
		
			for(int i = 0 ; i < 2 ; i++)
			{
				drone.getCommandManager().hover();
				Thread.sleep(30);
			}
				
			
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
				/*startTime = SystemClock.uptimeMillis();
				timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
				do{
					timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
					drone.hover();
				}while (timeInMilliseconds<100);*/
			//	System.out.println("Temps : " + timeInMilliseconds + " Commande : hover");
		
	}

	/**
	 *Methode pour chercher une cible
	 **/
	private void ChercheCible()
	{
		if(etat == 0)
		{
			etat = 1;
			for(int i = 0 ; i < 50 ; i++)
			{
				drone.move3D(0, 0, -100, 0);
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//	System.out.println("Temps : " + timeInMilliseconds + " Commande : droite" + i);
			}
		}

		// Premiere etape de recherche de cible
		// Monter en altitude
		/*if(atteint < MAX_ALTITUDE - 100 && etat == 0)
		{
			drone.up();
		}
		
		// Deuxième étape
		// Avancer
		else if (etat == 1)
		{
			// Ne faire cette étape que pendant X ms
			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			if (timeInMilliseconds >= 1500)
			{
				etat = 2;
			}
			
			drone.move3D(speed, 0, 0, 0);
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if (etat == 2)
		{
			startTime = SystemClock.uptimeMillis();
			do{
				timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
				drone.hover();
			}while(timeInMilliseconds < 1000);
			//Landing();
			drone.landing();
		}*/
	}
	
	/**
	 *Methode pour atterir le drone correctement
	 **/
	private void Landing()
	{
		isFlying = false;
		while(atteint >= 400)
		{
			drone.move3D(0, 0, 100, 0);
			//drone.down();
			/*try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		
		drone.landing();
	}
	//OpenCVManager
	@Override
	public void onPause()
	{
		super.onPause();
		//drone.getNavDataManager().removeAltitudeListener(this);
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onDestroy() {
		super.onDestroy();
		Landing();
		unregisterReceiver(receiversms);
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	//Debug
	public void onCameraViewStarted(int width, int height) 
	{
		if(DEBUG)
		Log.i(TAG , "Taille de l'image est "+width+"x"+height);
	}

	public void onCameraViewStopped() 
	{
		if(DEBUG)
		Log.i(TAG , "CameraViewStopped");
	}

	//Initialisation OpenCV
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				if(DEBUG)
				Log.i(TAG , "OpenCV execute");
				System.loadLibrary("jni_part");
				
				if(DEBUG)
				Log.i(TAG , "NDK execute");
				
				mOpenCvCameraView.enableView();
				
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
		//drone.getNavDataManager().addAltitudeListener(this);
	}

	/**
	 *Methode appeler lorsque l'on recoit l'altitude
	 **/
	/*@Override
	public void receivedAltitude(int alti) {
		// TODO Auto-generated method stub
		System.out.println("Altitude : " + alti);
		atteint = alti;
		if (alti > MAX_ALTITUDE+200)
		{
			//drone.move3D(0, 0, speed, 0);
			drone.down();
		}
		
		else if (alti < MAX_ALTITUDE-150)
		{
			//drone.move3D(0, 0, -50, 0);
			drone.up();
		}
		
		else if(etat == 0 && atteint >= MAX_ALTITUDE - 100)
		{
			etat = 1;
			//startTime = SystemClock.uptimeMillis();
		}
		
	}

	@Override
	public void receivedExtendedAltitude(Altitude alti) {
		// TODO Auto-generated method stub
		//System.out.println("Altitude extended: " + alti);
	}*/
	
	
	
}
