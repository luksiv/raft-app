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

    static void setContext(Context context){
        _context = context;
        _tag = context.getClass().getSimpleName() + " ERROR";
    }

    static void handle(Exception ex)
    {
        Log.v(_tag, formatErrorMsg(ex, true));
    }

    static void handleWithToast(Exception ex)
    {
        handle(ex);
        Toast.makeText(_context, formatErrorMsg(ex, false), Toast.LENGTH_SHORT).show();
    }

    static void handleWithToast(Exception ex, String message)
    {
        handle(ex);
        Toast.makeText(_context, message, Toast.LENGTH_SHORT).show();
    }

    static void handleWithToast(Exception ex, int id)
    {
        handle(ex);
        Toast.makeText(_context, _context.getResources().getString(id), Toast.LENGTH_SHORT).show();
    }

    static void handleWithSnackbar(Exception ex, View view)
    {
        handle(ex);
        Snackbar.make(view, formatErrorMsg(ex, false), Snackbar.LENGTH_LONG).show();
    }

    static void handleWithSnackbar(Exception ex, View view, String message)
    {
        handle(ex);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    static void handleWithSnackbar(Exception ex, View view, int id)
    {
        handle(ex);
        Snackbar.make(view, _context.getResources().getString(id), Snackbar.LENGTH_LONG).show();
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
