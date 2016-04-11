#include <ComputerVision_jni.h>
#include <iostream>
#include <string>
#include <sstream>
#include <vector>
#include <cstdlib>
#include <ctime>

JNIEXPORT float JNICALL Java_com_example_circles_MainActivity_process
(JNIEnv *jenv, jobject obj, jlong addrGray, jlong addrRgba)
{
	if(DEBUG)
		LOGD("Java_com_example_circles_MainActivity_process");

	Mat& mRgb = *(Mat*)addrRgba;
	vector<Vec3f> circles;
	//vector<Point> approx;

	//Timer
	clock_t begin, end;
	double time_spent;
	begin = clock();

	//Traitement de l'image
	find_circles(addrGray,addrRgba,&circles);
	//find_circles(addrGray,addrRgba,&approx);
	end = clock();

	//Centrer le cercle vert à l'écran
	float direction = computeOrders(addrRgba,&circles);
	//float direction = computeOrders(addrRgba,&approx);

	//Dessiner le cercle
	drawCircles(addrRgba,&circles);


	time_spent = (double)(end - begin) / 1000.0;
	ostringstream os;
	os << time_spent;
	string text = os.str();
	Point center(50,50);
	//putText(mRgb, text, center,FONT_HERSHEY_PLAIN,3,Scalar(255,0,0),4,8,false);

	float resultat = 0;
	resultat = circles.size() + direction;
	return resultat;
}
/*
float computeOrders(jlong addImage, vector<Point>* approx)
{
	float direction = 0;
	//Paramètre
	int delta = 40;		//tolerance avec le point central (pixels)
	int delta2 = 20;	//tolerance avec la distance entre le cercle et le device (pixels)
	int dist = 100; 	//Rayon cible (pixels)

	Mat& mRgb = *(Mat*)addImage;
	vector<Point> approx2 = *approx;
	//Si l'on détecte une cible
	if(approx->size() != 0)
	{
		//Centre du quadrilatere
		Point p1 = approx2[0];
		Point p2 = approx2[1];
		Point p3 = approx2[2];
		Point p4 = approx2[3];
		int xx = p1.x + p2.x + p3.x + p4.x;
		xx = xx / 4;
		int yy = p1.y + p2.y + p3.y + p4.y;
		yy = yy /4;

		int rr = 10;

		//Image postion central
		int cxx = (mRgb.cols)/2;
		int cyy = (mRgb.rows)/2;

		//Print cercle (vert)
		Point cent(cxx,cyy);
		circle( mRgb, cent, 16, Scalar(0,255,0), 3, 8, 0 );

		//Texte a afficher lorsque l'on ai bien centré
		string text = "Aller";

		//Traitement...
		if(xx-cxx > delta)
		{
			text.append(" droite");
			direction += 0.6;
		}
		else if(xx-cxx < -delta)
		{
			text.append(" gauche");
			direction += 0.4;
		}
		if(yy-cyy > delta)
		{
			text.append(" reculer");
			direction += 0.02;
		}
		else if(yy-cyy < -delta)
		{
			text.append(" en avant");
			direction += 0.08;
		}
		if(rr-dist > delta2)
		{
			text.append(" loin");
		}
		else if(rr-dist < -delta2)
		{
			text.append(" proche");
			//direction += 0.05;
		}

		//Print les ordres à l'écran (rouge)
		Point center(50,50);
		int fontFace = FONT_HERSHEY_PLAIN;
		double fontScale = 3;
		Scalar col = Scalar(255,0,0);
		putText(mRgb, text, center,fontFace, fontScale,col,4,8,false);
		if (direction == 0)
			direction = 0.5;
	}
	return direction;
}

*/
float computeOrders(jlong addImage, vector<Vec3f>* circles)
{
	float direction = 0;
	//Paramètre
	int delta = 40;		//tolerance avec le point central (pixels)
	int delta2 = 20;	//tolerance avec la distance entre le cercle et le device (pixels)
	int dist = 100; 	//Rayon cible (pixels)

	Mat& mRgb = *(Mat*)addImage;

	//Si l'on détecte un cercle
	if(circles->size() != 0)
	{
		size_t i = 0;

		//Information sur les cercles (0 = le premier cercle detecté)
		//Rayon, centre x and centre y
		int rr= ((int)(*circles)[i][2]);
		int xx= ((int)(*circles)[i][0]);
		int yy= ((int)(*circles)[i][1]);

		//Image postion central
		int cxx = (mRgb.cols)/2;
		int cyy = (mRgb.rows)/2;

		//Print cercle (vert)
		Point cent(cxx,cyy);
		circle( mRgb, cent, dist, Scalar(0,255,0), 3, 8, 0 );

		//Texte a afficher lorsque l'on ai bien centré
		string text = "Aller";

		//Traitement...
		if(xx-cxx > delta)
		{
			text.append(" droite");
			direction += 0.6;
		}
		else if(xx-cxx < -delta)
		{
			text.append(" gauche");
			direction += 0.4;
		}
		if(yy-cyy > delta)
		{
			text.append(" reculer");
			direction += 0.02;
		}
		else if(yy-cyy < -delta)
		{
			text.append(" en avant");
			direction += 0.08;
		}
		if(rr-dist > delta2)
		{
			text.append(" loin");
		}
		else if(rr-dist < -delta2)
		{
			text.append(" proche");
			//direction += 0.05;
		}

		//Print les ordres à l'écran (rouge)
		Point center(50,50);
		int fontFace = FONT_HERSHEY_PLAIN;
		double fontScale = 3;
		Scalar col = Scalar(255,0,0);
		putText(mRgb, text, center,fontFace, fontScale,col,4,8,false);
		if (direction == 0)
			direction = 0.5;
	}
	return direction;
}



void find_circles(jlong addrGray, jlong addrRgba, /*vector<Point> approx*/vector<Vec3f>* circles)
{
	Mat& mGr  = *(Mat*)addrGray;
	Mat& mRgb = *(Mat*)addrRgba;
	/*Mat* noir = new Mat();
	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;
    CvMemStorage *mem = cvCreateMemStorage(0);
    CvSeq* result;
    vector<Point> approx2 = *approx;*/

	//Flouter l'image pour détecter le meilleur cercle
	medianBlur(mGr,mGr,5);

	//Transformée de Hough
		HoughCircles(mGr, 			//Noir/Blanc de l'image
				*circles, 			//Vecteur sortie
				CV_HOUGH_GRADIENT,	//Méthode de detection a utiliser
				4, 					//Inverser le ratio
				mGr.rows/8, 		//Distance minimum entre le centre des cercles détecté
				220, 				//Filtre de Canny
				200, 				//Seuil accumulateur 100
				20, 				//Rayon minimm
				mGr.cols/4			//Rayon maximum
		);
	//On convertis l'image en noir/blanc pour une meilleur detection des contours
	/*inRange(mGr,Scalar(0,0,0),Scalar(110,100,100),*noir);

	mGr = *noir;

	//Dilatation
	//dilate(mGr,mGr,Mat(),Point(-1,-1));

	//Trouver les contours
	Canny(mGr,mGr,100,100,3);
	findContours(mGr, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );

	//Afficher les contours
	for( int i = 0; i< contours.size(); i++ )
	     {
	       Scalar color = Scalar( 255, 0, 0 );
	       drawContours( mGr, contours, i, color, 2, 8, hierarchy, 0, Point() );
	       approxPolyDP(Mat(contours[i]), approx2, arcLength(Mat(contours[i]), true)*0.02, true);
	       if (contourArea(Mat(contours[i])) < 500 || !cv::isContourConvex(approx2))
	               continue;
	       //Afficher les contours de notre quadrilateral
	       if(approx2.size() == 4)
	       {
	    	   circle( mRgb,(Point)approx2[0], 10, Scalar(255,0,255), -1, 8, 0 );	// pourpre
	    	   circle( mRgb,(Point)approx2[1], 10, Scalar(255,0,255), -1, 8, 0 );	// pourpre
	    	   circle( mRgb,(Point)approx2[2], 10, Scalar(255,0,255), -1, 8, 0 );	// pourpre
	    	   circle( mRgb,(Point)approx2[3], 10, Scalar(255,0,255), -1, 8, 0 );	// pourpre
	    	   *approx = approx2;
	       }
	     }*/
}

void drawCircles(jlong addrImage, vector<Vec3f>* circles)
{
	Mat& mRgb = *(Mat*)addrImage;

	//Dessiner chaque cercle détecté
	for( size_t i = 0; i < circles->size(); i++ )
	{
		Point center(cvRound(((double)(*circles)[i][0])), cvRound(((double)(*circles)[i][1])));
		int radius = cvRound(((double)(*circles)[i][2]));
		// Dessiner le centre du cercle
		circle( mRgb, center, 3, Scalar(255,0,255), -1, 8, 0 );	// pourpre
		// Dessiner le contour du cercle
		circle( mRgb, center, radius, Scalar(255,0,0), 3, 8, 0 );	//rouge
	}
}
