package com.latenightpenguin.groupdj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHandler implements java.lang.Thread.UncaughtExceptionHandler{
    static String _tag;
    static Context _context;
    static View _view;

    static void setContext(Context context){
        _context = context;
        _tag = context.getClass().getSimpleName() + " ERROR";
    }

    static void setView(View view){
        _view = view;
    }

    static void handleExeption(Exception ex)
    {
        Log.v(_tag, formatErrorMsg(ex, true));
    }

    static void handleMessege(String message)
    {
        Log.v(_tag, " " + message);
    }

    static void handleExeptionWithToast(Exception ex)
    {
        handleExeption(ex);
        Toast.makeText(_context, formatErrorMsg(ex, false), Toast.LENGTH_SHORT).show();
    }

    static void handleMessegeWithToast(String message) {
        handleMessege(message);
        Toast.makeText(_context, message, Toast.LENGTH_SHORT).show();
    }

    static void handleExeptionWithToast(Exception ex, String message)
    {
        handleExeption(ex);
        Toast.makeText(_context, message, Toast.LENGTH_SHORT).show();
    }

    static void handleExeptionWithToast(Exception ex, int id)
    {
        handleExeption(ex);
        Toast.makeText(_context, _context.getResources().getString(id), Toast.LENGTH_SHORT).show();
    }

    static void handleExeptionWithSnackbar(Exception ex)
    {
        handleExeption(ex);
        try{
        Snackbar.make(_view, formatErrorMsg(ex, false), Snackbar.LENGTH_LONG).show();
        }catch (Exception e){
            handleExeption(e);
        }
    }

    static void handleExeptionWithSnackbar(Exception ex, String message)
    {
        handleExeption(ex);
        try{
            Snackbar.make(_view, message, Snackbar.LENGTH_LONG).show();
        }catch (Exception e)
        {
        handleExeption(e);
        }

    }

    static void handleExeptionWithSnackbar(Exception ex, int id)
    {
        handleExeption(ex);
        try{
            Snackbar.make(_view, _context.getResources().getString(id), Snackbar.LENGTH_LONG).show();
        }catch (Exception e)
        {
            handleExeption(e);
        }
    }

    static void handleMessegeWithSnackbar(String message)
    {
        handleMessege(message);
        try{
            Snackbar.make(_view, message, Snackbar.LENGTH_LONG).show();
        }catch (Exception e)
        {
            handleExeption(e);
        }

    }

    static String formatErrorMsg(Exception ex, boolean printStackTrace)
    {
        StringBuilder msg = new StringBuilder();

        if(printStackTrace) {
            msg.append("\n");
            StringWriter stringWriter = new StringWriter();
            ex.printStackTrace(new PrintWriter(stringWriter));
            String trace = stringWriter.toString();
            msg.append(trace);
            msg.append("\n");
        }
        if(ex.getMessage().isEmpty())
        {
            msg.append(ex.toString());
        }
        else {
            msg.append(ex.getMessage());
        }


        return msg.toString();
    }

    @Override
    public void uncaughtException(Thread t, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("CAUSE OF ERROR\n\n");
        errorReport.append(stackTrace.toString());


        Intent intent = new Intent(_context, CrashActivity.class);
        intent.putExtra("error", errorReport.toString());
        _context.startActivity(intent);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
