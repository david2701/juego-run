package com.example.dingtao.run;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.List;


public class RunsActivity extends ActionBarActivity implements UpdateableView {
    Model model;
    RunAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runs);
        model = Model.Get();
        ListView listView = (ListView) findViewById(R.id.list);
        adapter = new RunAdapter(this,R.layout.list_item_run,model.runs);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(view.getContext(),RunActivity.class).putExtra("rid",i);
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_runsq, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private static class RunAdapter extends ArrayAdapter<Run>{

        public RunAdapter(Context context, int resource, List<Run> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            Run run = getItem(position);
            LayoutInflater inflator = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflator.inflate(R.layout.list_item_run,parent,false);
            TextView name = (TextView) rowView.findViewById(R.id.name);
            TextView time = (TextView) rowView.findViewById(R.id.time);

            name.setText(run.name);
            time.setText(run.StartedToTime());
            return rowView;
        }
    }

    @Override
    public void Update(String msg) {
        if (msg == Model.RUNS_UPDATED){
            adapter.notifyDataSetChanged();
        }
    }
}
