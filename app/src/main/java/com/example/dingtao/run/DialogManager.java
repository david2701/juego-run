package com.example.dingtao.run;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Dingtao on 6/17/2015.
 */
public class DialogManager {

    public static void QuitNotice(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.quit_notice_title).setMessage(R.string.quit_notice_message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.show();
    }

    public static void NameRun(final Context context, final Run run){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.dialog_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        builder.setTitle(R.string.name_run_title).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().length() > 0) {
                    run.name = input.getText().toString();
                } else {
                    run.name = String.valueOf(System.currentTimeMillis());
                }
                Model.Get().runs.add(run);
                Model.Get().WriteToFile();
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static void ClearLists(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.clear_runs_title).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Model.Get().ClearRuns();
            }
        }).setNegativeButton(R.string.cancel,null);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void NoInput(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.no_input_title).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
