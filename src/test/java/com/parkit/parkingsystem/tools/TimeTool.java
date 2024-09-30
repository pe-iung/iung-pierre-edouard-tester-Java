package com.parkit.parkingsystem.tools;

import java.util.Date;

public class TimeTool {

    private final long time;

    private TimeTool(){
        this(System.currentTimeMillis());
    }

    private TimeTool(Date date ){
        this(date.getTime());
    }

    private TimeTool(long timeInMillisec ){
        time = timeInMillisec;
    }


    public static TimeTool now(){
        return new TimeTool();
    }

    public static TimeTool of(Date date){
        return new TimeTool(date);
    }


    public static TimeTool of(long timeInMillisec){
        return new TimeTool(timeInMillisec);
    }

    public TimeTool minusMinute(long minutes){
        long timeInMillisec =  time - (minutes * 60 * 1000);
        return new TimeTool(timeInMillisec);
    }



    public TimeTool minusHour(long hour){
        long timeInMillisec =  time - (hour * 60 * 60 * 1000);
        return new TimeTool(timeInMillisec);
    }

    public TimeTool plusMinute(long minutes){
        long timeInMillisec =  time + (minutes * 60 * 1000);
         return new TimeTool(timeInMillisec);
    }

    public Date toDate(){
        return new Date(time);
    }

    public long toLong(){
        return time;
    }



}
