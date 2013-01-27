package pl.uj.edu.football;

import android.content.Context;

public class Singleton {
	static String name1 = "gracz1";
	static String name2 = "gracz2";
	static int score1 = 0;
	static int score2 = 0;
	static Context context;
	
	
	static final Singleton INSTANCE = new Singleton();
	private Singleton(){
	}
	//Setters
	public static void setName1(String arg){
		name1 = arg;
	}
	public static void setName2(String arg){
		name2 = arg;
	}
	public static void setContext(Context con){
		context = con;
	}
	public static void addScore1(){
		score1++;
	}
	public static void addScore2(){
		score2++;
	}
	public static void resetScore(){
		score1 = score2 = 0;
	}
	
	//Getters
	public static String getName1(){
		return name1;
	}
	public static String getName2(){
		return name2;
	}
	public static Context getContext(){
		return context;
	}
	public static int getScore1(){
		return score1;
	}
	public static int getScore2(){
		return score2;
	}

}
