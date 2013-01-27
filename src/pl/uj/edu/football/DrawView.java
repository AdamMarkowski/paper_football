package pl.uj.edu.football;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class DrawView extends View implements OnTouchListener {
    private static final String TAG = "DrawView";

    List<Point> points = new ArrayList<Point>();
    Vector<Boolean> turns = new Vector<Boolean>();
    Paint paint = new Paint();
    Paint paint2 = new Paint();
    Paint paintBall = new Paint();
    Paint playerColor1 = new Paint();
    Paint playerColor2 = new Paint();
    int countW = 6;
	int countH = 8;
    int XYplane[][][] = new int[countW+1][countH+3][9]; // 8 directions of move + visited flag
    float BallX = 0;
    float BallY = 0;
    float dX, dY, StartX, StartY;
    boolean playersTurn = true; //true -> player1, false -> player2
    Toast toast;
	
	
    
    

    public DrawView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        turns.add(true);
        
        paint.setColor(Color.BLUE);
        paint.setAntiAlias(true);
        paint2.setColor(Color.BLACK);
        paint2.setAntiAlias(true);
        paintBall.setColor(Color.GREEN);
        paintBall.setAntiAlias(true);
        playerColor1.setColor(Color.BLUE);
        playerColor2.setColor(Color.RED);
    }

    @Override
    public void onDraw(Canvas canvas) {
    	
    	double fieldW = 0.6;
    	double fieldH = 0.6;
    	this.dX = (float)fieldW/countW*canvas.getWidth();
    	this.dY = (float)fieldH/countH*canvas.getHeight();
    	this.dY = this.dX;
    	StartX = (float)((1.0-fieldW)/2.0)*canvas.getWidth();
    	StartY = (float)((1.0-fieldH)/2.0)*canvas.getHeight();
    	
    	//ball  position
    	if(BallX == 0 && BallY == 0){
    		this.BallX = StartX + countW/2*dX;
    		this.BallY = StartY + countH/2*dY;
    		Point point = new Point();
    		point.x = BallX;
    		point.y = BallY;
    		points.add(point);
    	}
    	
    	if(playersTurn) this.paint = this.playerColor1;
    	else this.paint = this.playerColor2;
    	
//    	XYplane[(int)Math.round((BallX-StartX)/dX)][(int)Math.round((BallY-StartY)/dY)+1] = 1;
    	for(int i = 0; i<countW+1; i++){
    		canvas.drawLine(StartX+i*dX, StartY, StartX+i*dX, StartY + countH*dY, paint2);
    	}
    	for(int i = 0; i<countH+1; i++){
    		canvas.drawLine(StartX, StartY+i*dY, StartX + countW*dX, StartY + i*dY, paint2);
    	}
    	//Rysunek bramek
    	canvas.drawLine(StartX+(countW/2-1)*dX,  StartY, StartX+(countW/2-1)*dX, StartY-(dY),playerColor1);
    	canvas.drawLine(StartX+(countW/2+1)*dX,  StartY, StartX+(countW/2+1)*dX, StartY-(dY),playerColor1);
    	canvas.drawLine(StartX+(countW/2-1)*dX,  StartY-(dY), StartX+(countW/2+1)*dX, StartY-(dY),playerColor1);
    	//Rysunek bramki dolnej
    	canvas.drawLine(StartX+(countW/2-1)*dX,  StartY+countH*dY, StartX+(countW/2-1)*dX, StartY+countH*dY+(dY),playerColor2);
    	canvas.drawLine(StartX+(countW/2+1)*dX,  StartY+countH*dY, StartX+(countW/2+1)*dX, StartY+countH*dY+(dY),playerColor2);
    	canvas.drawLine(StartX+(countW/2-1)*dX,  StartY+countH*dY+(dY), StartX+(countW/2+1)*dX, StartY+countH*dY+(dY),playerColor2);
    	
    	//Draw the ball in the center of the FIELD!!!!!!!!!!!!!!!!!!!!!!
    	canvas.drawCircle(BallX, BallY, 10, paintBall);

//    	canvas.drawCircle(200 * random.nextFloat(), 200 * random.nextFloat(), 10, paintBall);
    	Point before = null;
    	int counter = 0;
    	Log.d(TAG, "points.size(): " + points.size());
   		Log.d(TAG, "turns.size(): " + turns.size());
   		for(Boolean b : turns){
   			Log.d(TAG, "bool in turns: " + b);
   		}
	   	for (Point point : points) {
	   		if(before == null){
	   			before = point;
	   		}
	   		else if(counter < turns.size()){
	       		Log.d(TAG, "if DUPA");
	       		Log.d(TAG, "coutner" + counter);
	    		if(turns.get(counter)){
	        		paint.setColor(Color.BLUE);
	        		Log.d(TAG, "if BLUE");
	        	}
	        	else{
	        		paint.setColor(Color.RED);
	        		Log.d(TAG, "if RED");
	        	}
	            canvas.drawLine(before.x, before.y, point.x, point.y, paint);
	            before = point;
	            counter++;
	            // Log.d(TAG, "Painting: "+point);
	    	}
//	       	Log.d(TAG, "Buf point" + " " + before.x + " " + before.y);
	    }
    	
//        
        	canvas.drawCircle(before.x, before.y, 5, paint2);
//        	 Log.d(TAG, "Prosta: " +points.get(0).x + " "+ points.get(0).y+" "+ points.get(1).x+" "+ points.get(1).y);
        	//Drawing the score and players names
        	canvas.drawText(Singleton.getName1() + "  " + Singleton.getScore1(), (float)100, (float)50, playerColor1);
        	canvas.drawText(Singleton.getName2() + "  " + Singleton.getScore2(), (float)(canvas.getWidth()-100), (float)50, playerColor2);
    }
    
    private boolean isTheFieldEmpty(float xx, float yy, int direction){
    	int x = (int)Math.round((xx-StartX)/dX);
    	int y = (int)Math.round((yy-StartY)/dY)+1;
    	if(y == 0) {scoredGoal(true);return false;}
    	else if(y == countH+2) {scoredGoal(false); return false;}
    	if((x == countW/2 || x == countW/2-1 || x == countW/2+1) && (y == countH+2 || y == 0)) return true;
    	if(x<0 || y<0) return false;
    	if(x>countW+1 || y>countH+1) return false;
    	if(this.XYplane[x][y][direction] == 1) return false;
    	
    	return true;
    }
    private void setVisitedDirection(float xx, float yy, int direction){
    	int x = (int)Math.round((xx-StartX)/dX);
    	int y = (int)Math.round((yy-StartY)/dY)+1;
    	this.XYplane[x][y][direction] = 1;
    }
    private void setVisited(float xx, float yy){
    	int x = (int)Math.round((xx-StartX)/dX);
    	int y = (int)Math.round((yy-StartY)/dY)+1;
    	this.XYplane[x][y][8] = 1;
    }
    private void reset(){
    	points = new ArrayList<Point>();
        turns = new Vector<Boolean>();
        turns.add(true);
        XYplane = new int[countW+1][countH+3][9]; // 8 directions of move + visited flag
        BallX = 0;
        BallY = 0;
        
    }
    private void scoredGoal(Boolean player){
    	if(player){
    		toast=Toast.makeText(Singleton.getContext(), "GOOOL dla " + Singleton.getName1()+"!!!", Toast.LENGTH_SHORT);
    		Singleton.addScore1();
    	}
    	else{
    		toast=Toast.makeText(Singleton.getContext(), "GOOOL dla " + Singleton.getName2()+"!!!", Toast.LENGTH_SHORT);
    		Singleton.addScore2();
    	}
    	toast.show();
    	reset();
    }
    private void checkWhoseTurnIsIt(float xx, float yy){
    	int x = (int)Math.round((xx-StartX)/dX);
    	int y = (int)Math.round((yy-StartY)/dY)+1;
    	if(this.XYplane[x][y][8] == 1){
    		turns.add(playersTurn);
    	}
    	else if(x != countW/2 && (x == 0 || x == countW || y == 0 || y == countH)){
    		turns.add(playersTurn);
    	}
    	else{
    		boolean foobar = playersTurn;
    		playersTurn = !(foobar);
    		turns.add(playersTurn);
    		if(playersTurn){
    			toast = Toast.makeText(Singleton.getContext(), "Teraz rusza się " + Singleton.getName1(), Toast.LENGTH_SHORT);
    			toast.show();
    			try {
					Thread.sleep(toast.getDuration());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    		else{
    			toast = Toast.makeText(Singleton.getContext(), "Teraz rusza się " + Singleton.getName2(), Toast.LENGTH_SHORT);
    			toast.show();
				try {
					Thread.sleep(toast.getDuration());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	
    }

    public boolean onTouch(View view, MotionEvent event) {
        // if(event.getAction() != MotionEvent.ACTION_DOWN)
        // return super.onTouchEvent(event);
    	//setting the current player color:
    	
    	
    	
        Point point = new Point();
        point.x = event.getX();
        point.y = event.getY();
        boolean rightX = point.x < BallX + 1.2*dX && point.x > BallX + 0.8*dX;
        boolean consX = point.x < BallX + 0.2*dX && point.x > BallX -0.2*dX;
        boolean leftX = point.x < BallX - 0.8*dX && point.x > BallX - 1.2*dX;
        boolean upY = point.y < BallY - 0.8*dY && point.y > BallY - 1.2*dY;
        boolean consY = point.y < BallY +0.2*dY && point.y > BallY -0.2*dY;
        boolean downY = point.y < BallY+1.2*dY && point.y > BallY + 0.8*dY;
//        boolean notBorderX = ((point.y > StartY-0.2*dY && point.y < StartY+0.2*dY) || (point.y > StartY+(countH+1)*dY-0.2*dY && point.y < StartY+(countH+1)*dY+0.2*dY)) && consX;
//        boolean notBorderY = ((point.x > StartX-0.2*dX && point.x < StartX+0.2*dX) || (point.x > StartX+(countW+1)*dX-0.2*dX && point.x < StartX+(countW+1)*dX+0.2*dX)) && consY;
        if((rightX || consX || leftX) && (upY || consY || downY) && !(consX && consY)){
        	
//        	this.BallX = (float)Math.round(point.x);
//        	this.BallY = (float)Math.round(point.y);
	        
	        if(rightX && consY){
	        	if(isTheFieldEmpty(this.BallX+dX, this.BallY, 4)){
	        		setVisitedDirection(BallX, BallY, 0);
	        		setVisited(BallX, BallY);
	        		this.BallX = this.BallX+dX;
	        		setVisitedDirection(BallX, BallY, 4);
	        		checkWhoseTurnIsIt(BallX, BallY);
	        		points.add(point);
	        	}
	        }
	        else if(rightX && upY){
	        	if(isTheFieldEmpty(this.BallX+dX, this.BallY - dY, 5)){
	        		setVisitedDirection(BallX, BallY, 1);
	        		setVisited(BallX, BallY);
	        		this.BallX = this.BallX + dX;
	        		this.BallY = this.BallY - dY;
	        		setVisitedDirection(BallX, BallY, 5);
	        		checkWhoseTurnIsIt(BallX, BallY);
	        		points.add(point);
	        	}
	        }
	        else if(consX && upY){
	        	if(isTheFieldEmpty(this.BallX, this.BallY - dY, 6)){
	        		setVisitedDirection(BallX, BallY, 2);
	        		setVisited(BallX, BallY);
	        		this.BallY = this.BallY - dY;
	        		setVisitedDirection(BallX, BallY, 6);
	        		checkWhoseTurnIsIt(BallX, BallY);
	        		points.add(point);
	        	}
	        }
	        else if(leftX && upY){
	        	if(isTheFieldEmpty(this.BallX-dX, this.BallY - dY, 7)){
	        		setVisitedDirection(BallX, BallY, 3);
	        		setVisited(BallX, BallY);
	        		this.BallX = this.BallX - dX;
	        		this.BallY = this.BallY - dY;
	        		setVisitedDirection(BallX, BallY, 7);
	        		checkWhoseTurnIsIt(BallX, BallY);
	        		points.add(point);
	        	}
	        }
	        else if(leftX && consY){
	        	if(isTheFieldEmpty(this.BallX - dX, this.BallY, 0)){
	        		setVisitedDirection(BallX, BallY, 4);
	        		setVisited(BallX, BallY);
	        		this.BallX = this.BallX - dX;
	        		setVisitedDirection(BallX, BallY, 0);
	        		checkWhoseTurnIsIt(BallX, BallY);
	        		points.add(point);
	        	}
	        }
	        else if(leftX && downY){
	        	if(isTheFieldEmpty(this.BallX-dX, this.BallY + dY, 1)){
	        		setVisitedDirection(BallX, BallY, 5);
	        		setVisited(BallX, BallY);
	        		this.BallX = this.BallX - dX;
	        		this.BallY = this.BallY + dY;
	        		setVisitedDirection(BallX, BallY, 1);
	        		checkWhoseTurnIsIt(BallX, BallY);
	        		points.add(point);
	        	}
	        }
	        else if(consX && downY){
	        	if(isTheFieldEmpty(this.BallX, this.BallY + dY, 2)){
	        		setVisitedDirection(BallX, BallY, 6);
	        		setVisited(BallX, BallY);
	        		this.BallY = this.BallY + dY;
	        		setVisitedDirection(BallX, BallY, 2);
	        		checkWhoseTurnIsIt(BallX, BallY);
	        		points.add(point);
	        	}
	        }
	        else if(rightX && downY){
	        	if(isTheFieldEmpty(this.BallX+dX, this.BallY + dY, 3)){
	        		setVisitedDirection(BallX, BallY, 7);
	        		setVisited(BallX, BallY);
	        		this.BallX = this.BallX + dX;
	        		this.BallY = this.BallY + dY;
	        		setVisitedDirection(BallX, BallY, 3);
	        		checkWhoseTurnIsIt(BallX, BallY);
	        		points.add(point);
	        	}
	        }
        }
        invalidate();
        Log.d(TAG, "point: " + point);
        return true;
    }
}

class Point {
    float x, y;

    @Override
    public String toString() {
        return x + ", " + y;
    }
}
