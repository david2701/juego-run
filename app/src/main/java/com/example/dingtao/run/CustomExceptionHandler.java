package com.example.dingtao.run;

import android.content.Context;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by Dingtao on 6/24/2015.
 */
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler{
    Thread.UncaughtExceptionHandler ueh;
    Context main;
    public CustomExceptionHandler(Context context){
        main = context;
        ueh = Thread.getDefaultUncaughtExceptionHandler();
    }
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        String stackTrace = result.toString();
        printWriter.close();
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"dingtaoyin@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "Bug Report");
        email.putExtra(Intent.EXTRA_TEXT, stackTrace);
        email.setType("message/rfc822");
        main.startActivity(Intent.createChooser(email, "Choose an Email client to send the crash report to:"));


        ueh.uncaughtException(thread,throwable);
    }
}
