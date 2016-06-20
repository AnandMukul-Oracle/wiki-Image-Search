package com.example.kh1970.imagesearch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    EditText searchText;
    GridView mgridView;
    GridViewAdapter mGridAdapter;
    public ArrayList<String> urlList;
    private String Wiki_URL = "https://en.wikipedia.org/w/api.php?" +
            "action=query&prop=pageimages&format=json&piprop=thumbnail&pithumbsize=50&" +
            "pilimit=50&generator=prefixsearch&";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchText = (EditText) findViewById(R.id.textBox);
        urlList = new ArrayList<>();
        mgridView = (GridView) findViewById(R.id.grid_view);
        mGridAdapter = new GridViewAdapter(this, R.layout.image_grid, urlList );
        mgridView.setAdapter(mGridAdapter);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Toast.makeText(getApplicationContext(), "STRING MESSAGE-before", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Toast.makeText(getApplicationContext(), "STRING MESSAGE-on", Toast.LENGTH_LONG).show();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Toast.makeText(getApplicationContext(), "STRING MESSAGE-after", Toast.LENGTH_LONG).show();
                String pattern = searchText.getText().toString();

                urlList.clear();
                attachAndParseURL(pattern);
            }
        });

    }
    public void attachAndParseURL(String pattern) {
        final String QUERY_PARAM = "gpssearch";
        Uri builtUri = Uri.parse(Wiki_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, pattern)
                .build();
        //Toast.makeText(getApplicationContext(), builtUri.toString(), Toast.LENGTH_LONG).show();
        new AsyncHttpTask().execute(builtUri.toString());
        mGridAdapter.notifyDataSetChanged();

    }


    class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            try {
                // Create Apache HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(params[0]));
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                Log.d("statusCode :", "..................." + statusCode);
                // 200 represents HTTP OK
                if (statusCode == 200) {
                    String response = streamToString(httpResponse.getEntity().getContent());
                    parseResult(response);
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed
                }
            } catch (Exception e) {
                Log.d("", e.getLocalizedMessage());
            }
            return result;
        }



        String streamToString(InputStream stream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            String line;
            String result = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }

            // Close stream
            if (null != stream) {
                stream.close();
            }
            Log.d("streamToString", "..................." + result);
            return result;
        }

        public void parseResult(String result) {
            try {
                JSONObject response = new JSONObject(result);
                Log.d("response :", "..................." + response);
                JSONObject json = response.getJSONObject("query").getJSONObject("pages");
                int length = json.length();
                Log.d("json1","......................." + length);
                Iterator<String> keys = json.keys();
                while(keys.hasNext()){
                    String key = keys.next();
                    Log.d("aaa : ",key);
                    JSONObject json1 = json.getJSONObject(key);
                    JSONObject json2;
                    if (json1.has("thumbnail")) {
                        json2 = json1.getJSONObject("thumbnail");
                        if (json2.has("source")) {
                            String sourceJson = json2.getString("source");
                            Log.d("json1 : ", sourceJson);
                            urlList.add(sourceJson);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class GridViewAdapter extends ArrayAdapter<String> {

        Context mContext;
        int layoutResourceId;
        ArrayList<String> mGridData = new ArrayList<String>();
        public GridViewAdapter(Context mContext, int layoutResourceId, ArrayList<String> mGridData) {
            super(mContext, layoutResourceId, mGridData);
            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.mGridData = mGridData;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            final ViewHolder holder;
            ImageView imageView=null;

            if (row == null) {
                imageView = new ImageView(mContext);
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) row.findViewById(R.id.image_grid);
                row.setTag(holder);
                convertView = imageView;
            } else {
                holder = (ViewHolder) row.getTag();
            }

            Log.d("Inside getView : ", "................................" + position);

            Picasso.with(mContext).load(urlList.get(position)).into(holder.imageView);

            return row;
        }


        class ViewHolder {
            ImageView imageView;
        }
    }

}