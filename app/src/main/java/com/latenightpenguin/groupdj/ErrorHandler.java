package com.latenightpenguin.groupdj;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHandler {
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

    static void handleWithToast(Exception ex, String messege)
    {
        handle(ex);
        Toast.makeText(_context, messege, Toast.LENGTH_SHORT).show();
    }

    static void handleWithToast(Exception ex, int id)
    {
        handle(ex);
        Toast.makeText(_context, _context.getResources().getString(id), Toast.LENGTH_SHORT).show();
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
}
