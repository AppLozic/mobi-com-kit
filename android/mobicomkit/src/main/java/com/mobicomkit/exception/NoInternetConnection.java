package com.mobicomkit.exception;

/**
 * Created by user on 5/30/2015.
 */
public class NoInternetConnection  extends  Exception{
    private  String message;

    public NoInternetConnection(String message)
    {
        super(message);
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
