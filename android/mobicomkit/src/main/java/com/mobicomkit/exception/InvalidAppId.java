package com.mobicomkit.exception;

/**
 * Created by user on 5/30/2015.
 */
public class InvalidAppId extends Exception{
    private  String message;
    public InvalidAppId(String message)
    {
        super(message);
        this.message = message;
    }
    @Override
    public String toString() {
        return message;
    }
}