package com.mobicomkit.exception;

/**
 * Created by user on 6/2/2015.
 */
public class Error404 extends Exception{
    private  String message;
    public Error404(String message)
    {
        super(message);
        this.message = message;
    }
    @Override
    public String toString() {
        return message;
    }
}