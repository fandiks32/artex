package com.example.artext.TextRecognition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NameList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class FileOperations {
	  	 
	 public static String searchWord(String text1,String text2,Context context,StringBuilder sb) throws IOException{
			
		 String[] tmpResult = sb.toString().split(text1+"\\?"+text2+"\\t");
		 try{
	         if(sb!=null ){
	        	 //s = '?'+s;
	        	 tmpResult = tmpResult[1].split("\\n");
	        	 //tmpResult = tmpResult[0].split("\\t");
	        	 return tmpResult[0];
	         }
	        }catch (Exception e){//Catch exception if any
	        	Log.e("ERROR", "XXXXXXsearchword:---> " + e.toString());
	        	tmpResult = sb.toString().split(text2+"\\?"+text1+"\\t");
	        	tmpResult = tmpResult[1].split("\\n");
	        	return tmpResult[0];
	    }
		return null;
	 }
	 
	 public static boolean checkTargetedWord(String targetWord,StringBuilder sb){
		 
		 try{
	         if(sb!=null){
	        	 if(sb.toString().contains('?'+targetWord+"\t")){
	        		 return true;
	        	 }else {
	        		 return false;
	        	 }
	         }
	        }catch (Exception e){//Catch exception if any
	    	Log.e("ERROR", "XXXXXXsearchword:---> " + e.toString());	
	    	return false;
	    }
		 return false;
	}
	 
}
