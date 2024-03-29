package com.java.group8;

import android.accounts.NetworkErrorException;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NewsService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS

    //add key
    public static final String KEY = "getBy";
    public static final String NEWSID = "news_ID";
    public static final String NEWSCATEGORY = "_category";
    public static final String NEWSKEYWORD = "_keyword";
    public static final String MOVETYPE = "_move";
    public static final String ISFAV = "isfav";
    public static final String SERVICEKIND = "_servicekind";

    //add value
    public static final String LIST = "List";
    public static final String DETAILS = "Details";
    public static final String RESULT = "Result";
    public static final String RECOMMEND = "Recommed";
    public static final String REFRESH = "Refresh";
    public static final String LOAD = "Load";
    public static final String FAV = "Fav";
    public static final String CLEARLOCAL = "Clearlocal";
    public static final String CLEARFAV = "Clearfav";
    public static final String HISTORY = "SearchHistory";
    public static final String NEWSFAVORITE = "Newsfavorite";
    public static final String LATEST = "Latest";
    public static final String DELEATE_ONE = "Deleteone";
    public static final String FAV_ONE = "Fav_one";
    public static final String DELETE_ALL = "Deleteall";

    //add action

    public static final String MAINACTION = "android.intent.action.NEWSLIST";
    public static final String DETAIACTION = "android.intent.action.NEWSDETAILS";
    public static final String SEARCHACTION = "android.intent.action.NEWSSEARCH";
    public static final String FAVACTION = "android.intent.action.GETFAV";
    public static final String RECOMMEDACTION = "android.intent.action.RECOMMED";

    //add return key
    public static final String NEWSLIST = "newslist";
    public static final String NEWSDETAILS = "newsdetails";
    public static final String HISTORYLIST = "historylist";
    public static final String FAVLIST = "favlist";

    //add URL
    private static final String LATEST_URL = "http://166.111.68.66:2042/news/action/query/latest";
    private static final String LATEST_CATEGORY_URL = "http://166.111.68.66:2042/news/action/query/latest?category=";
    private static final String DETAIL_URL = "http://166.111.68.66:2042/news/action/query/detail?newsId=";
    private static final String SEARCH_URL = "http://166.111.68.66:2042/news/action/query/search?keyword=";
    private final String IMAGE_URL = "https://api.cognitive.microsoft.com/bing/v5.0/images/search?subscription-key=2a11735afda3496aa0c4fc2c9a6c5641&q=";

    //database
    private static final String SELECT = "select * from ";
    private static final String WHEREID = " where ID =?";
    private static final String WHEREHIS = " where History =?";
    private static final String WHERECATEGORY = " where ClassTag =?";

    public NewsService() {
        super("NewsService");
    }

    NewsDatabase dbmanager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        dbmanager = new NewsDatabase(this);
        Log.d("dbmanager", "create");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String key = intent.getStringExtra(KEY);
            switch (key) {
                case LIST:
                    Bundle b = intent.getExtras();
                    getNews((NewsCategory) b.get(NEWSCATEGORY), (String) b.get(MOVETYPE));
                    Log.d("start", "startservice");
                    break;
                case DETAILS:
                    final String news_ID = intent.getStringExtra(NEWSID);
                    final String service_kind = intent.getStringExtra(SERVICEKIND);
                    getDetails(news_ID, service_kind);
                    Log.d("start", "getdetails");
                    break;
                case RESULT:
                    final String keyword = intent.getStringExtra(NEWSKEYWORD);
                    getResult(keyword);
                    Log.d("start", "getresult");
                    break;
                case FAV:
                    final String _news_ID = intent.getStringExtra(NEWSID);
                    final String _service_kind = intent.getStringExtra(SERVICEKIND);
                    addFavorite(_news_ID, _service_kind);
                    Log.d("start", "addfavorite");
                    break;
                case CLEARLOCAL:
                    clearLocal();
                    break;
                case CLEARFAV:
                    final String service__kind = intent.getStringExtra(SERVICEKIND);
                    clearFav(service__kind);
                    break;
                case HISTORY:
                    final String _kind = intent.getStringExtra(SERVICEKIND);
                    getHistory(_kind);
                    break;
                case NEWSFAVORITE:
                    final String kind_ = intent.getStringExtra(SERVICEKIND);
                    getFavorite(kind_);
                    break;
                case RECOMMEND:
                    final String _keyword = intent.getStringExtra(NEWSKEYWORD);
                    final String _kind_ = intent.getStringExtra(SERVICEKIND);
                    getRecommend(_keyword, _kind_);
                    break;
                case LATEST:
                    boolean flag = false;
                    Bundle _b = intent.getExtras();
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm != null) {
                        NetworkInfo infos = cm.getActiveNetworkInfo();
                        if (infos.isConnected()) {
                            flag = true;
                        }
                    }
                    Log.d("flagg", String.valueOf(flag));
                    if (flag == true)
                        getLatest((NewsCategory) _b.get(NEWSCATEGORY), (String) _b.get(MOVETYPE));
                    else
                        getNews((NewsCategory) _b.get(NEWSCATEGORY), (String) _b.get(MOVETYPE));
                    break;
                default:
                    break;
            }
        }
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.i("myIntentService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }*/

    @Override
    public void onDestroy() {
        Log.i("myIntentService", "onDestroy");
        super.onDestroy();
    }

    private void getLatest(final NewsCategory category, final String move) {
        Log.d("getlatest", "123");
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .get()
                .url(category == null ? LATEST_URL : LATEST_CATEGORY_URL + category.getIndex())
                .build();

        Log.d("URL REQUEST", request.toString());


        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("begin", "233");
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response.code());
                        //Log.d("Response", response.body().string());
                        String body = response.body().string();
                        JSONObject json = JSONObject.fromObject(body);
                        JSONArray list = json.getJSONArray("list");
                        ArrayList<News> newslist = new ArrayList<News>();
                        for (int i = 0; i < list.size(); i++) {
                            JSONObject json_obj = list.getJSONObject(i);
                            //Log.d("res", json_obj.toString());
                            String id = json_obj.getString("news_ID");
                            String tag = json_obj.getString("newsClassTag");
                            String source = json_obj.getString("news_Source");
                            String title = json_obj.getString("news_Title");
                            String time = json_obj.getString("news_Time");
                            String url = json_obj.getString("news_URL");
                            String lang_type = json_obj.getString("lang_Type");
                            String author = json_obj.getString("news_Author");
                            String pic = json_obj.getString("news_Pictures");
                            String video = json_obj.getString("news_Video");
                            String intro = json_obj.getString("news_Intro");
                            News news = new News(tag, id, source, title, time, url, author, lang_type, pic, video, intro);
                            String _str = SELECT + NewsDatabase.ALL_TABLE_NAME + WHEREID;
                            String[] _s = {id};
                            Cursor c = dbmanager.query(_str, _s);
                            if (c.moveToFirst() == false) {
                                ContentValues values = new ContentValues();
                                values.put("ID", id);
                                values.put("ClassTag", tag);
                                values.put("Source", source);
                                values.put("Title", title);
                                values.put("Time", time);
                                values.put("URL", url);
                                values.put("Author", author);
                                values.put("Type", lang_type);
                                values.put("Pictures", pic);
                                values.put("Video", video);
                                values.put("Read", 0);
                                dbmanager.insert(values, NewsDatabase.ALL_TABLE_NAME);
                            }
                            else{
                                news.read = c.getInt(c.getColumnIndex("Read")) == 1? true : false;
                            }
                            newslist.add(news);
                            if (c != null && !c.isClosed())
                                c.close();
                            //Log.d("tag", tag);
                        }
                        Log.d("wait", "a minute");
                        Intent intent = new Intent();
                        intent.putExtra(NEWSLIST, newslist);
                        intent.putExtra(NEWSCATEGORY, category);
                        intent.putExtra(MOVETYPE, move);
                        intent.setAction(MAINACTION);
                        sendBroadcast(intent);

                    }
                });
            }
        }).start();
    }


    private void getNews(final NewsCategory category, final String move) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String str = new String();
                String a = new String();
                Cursor c;
                if (category == null) {
                    Log.d("sad", "null");
                    str = SELECT + NewsDatabase.ALL_TABLE_NAME;
                    c = dbmanager.query(str, null);
                }
                else {
                    Log.d("sad", "have");
                    str = SELECT + NewsDatabase.ALL_TABLE_NAME + WHERECATEGORY;
                    switch (category) {
                        case SCIENCE:
                            a = "科技";
                            break;
                        case EDUCATION:
                            a = "教育";
                            break;
                        case MILITARY:
                            a = "军事";
                            break;
                        case DOMESTIC:
                            a = "国内";
                            break;
                        case SOCIETY:
                            a = "社会";
                            break;
                        case CULTURE:
                            a = "文化";
                            break;
                        case CAR:
                            a = "汽车";
                            break;
                        case INTERNATIONAL:
                            a = "国际";
                            break;
                        case SPORT:
                            a = "体育";
                            break;
                        case ECONOMY:
                            a = "财经";
                            break;
                        case HEALTH:
                            a = "健康";
                            break;
                        case ENTERTAINMENT:
                            a = "娱乐";
                            break;
                    }
                    String[] _s = {a};
                    c = dbmanager.query(str, _s);
                }
                Log.d("cors", String.valueOf(c.moveToFirst()));
                int count = 0;
                ArrayList<News> newslist = new ArrayList<News>();
                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    String id = c.getString(c.getColumnIndex("ID"));
                    String tag = c.getString(c.getColumnIndex("ClassTag"));
                    String source = c.getString(c.getColumnIndex("Source"));
                    String title = c.getString(c.getColumnIndex("Title"));
                    String time = c.getString(c.getColumnIndex("Time"));
                    String url = c.getString(c.getColumnIndex("URL"));
                    String lang_type = c.getString(c.getColumnIndex("Type"));

                    String author = c.getString(c.getColumnIndex("Author"));
                    String pic = c.getString(c.getColumnIndex("Pictures"));
                    String video = c.getString(c.getColumnIndex("Video"));
                    int read = c.getInt(c.getColumnIndex("Read"));
                    News news = new News(tag, id, source, title, time, url, author, lang_type, pic, video, null);
                    news.read = read == 1 ? true : false;
                    newslist.add(news);
                    count++;
                    Log.d("count", String.valueOf(count));
                    if (count == 20)
                        break;
                }
                if (c != null && !c.isClosed())
                    c.close();
                Intent intent = new Intent();
                intent.putExtra(NEWSLIST, newslist);
                intent.putExtra(NEWSCATEGORY, category);
                intent.putExtra(MOVETYPE, move);
                intent.setAction(MAINACTION);
                sendBroadcast(intent);

            }
        }).start();
    }


    private void getDetails(final String news_ID, final String kind) {
        Log.d("asdf", news_ID);
        ContentValues values = new ContentValues();
        values.put("Read", 1);

        String str = SELECT + NewsDatabase.FAV_TABLE_NAME + WHEREID;
        String[] _ss = {news_ID};
        Cursor c = dbmanager.query(str, _ss);
        if (c.moveToFirst() == true) {
            String id = c.getString(c.getColumnIndex("ID"));
            String tag = c.getString(c.getColumnIndex("ClassTag"));
            String source = c.getString(c.getColumnIndex("Source"));
            String title = c.getString(c.getColumnIndex("Title"));
            String time = c.getString(c.getColumnIndex("Time"));
            String url = c.getString(c.getColumnIndex("URL"));
            String lang_type = c.getString(c.getColumnIndex("Type"));
            ;
            String author = c.getString(c.getColumnIndex("Author"));
            String pic = c.getString(c.getColumnIndex("Pictures"));
            String video = c.getString(c.getColumnIndex("Video"));
            News news = new News(tag, id, source, title, time, url, lang_type, author, pic, video, null);
            news.read = true;
            byte data[] = c.getBlob(c.getColumnIndex("Details"));
            values.put("Details", data);
            dbmanager.update(NewsDatabase.ALL_TABLE_NAME, values, "ID=?", new String[]{news_ID});

            ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
            try {
                ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
                news.news_content = (News.NewsDetail) inputStream.readObject();
                inputStream.close();
                arrayInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("wait", "a new");
            if (c != null && !c.isClosed())
                c.close();
            Intent intent = new Intent();
            intent.putExtra(NEWSDETAILS, news);
            intent.putExtra(ISFAV, true);
            intent.putExtra(SERVICEKIND, kind);
            intent.setAction(DETAIACTION);
            sendBroadcast(intent);
        }
        else {
            final OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .get()
                    .url(DETAIL_URL + news_ID)
                    .build();
            Log.d("IDIDID", news_ID);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (!response.isSuccessful())
                                throw new IOException("Unexpected code " + response.code());
                            String body = response.body().string();
                            Log.d("Response", body);
                            JSONObject json_obj = JSONObject.fromObject(body);
                            String id = json_obj.getString("news_ID");
                            String tag = json_obj.getString("newsClassTag");
                            String source = json_obj.getString("news_Source");
                            String title = json_obj.getString("news_Title");
                            String time = json_obj.getString("news_Time");
                            String url = json_obj.getString("news_URL");
                            String lang_type = json_obj.getString("lang_Type");
                            String author = json_obj.getString("news_Author");
                            String pic = json_obj.getString("news_Pictures");
                            String video = json_obj.getString("news_Video");
                            News news = new News(tag, id, source, title, time, url, author, lang_type, pic, video, null);
                            news.read = true;
                            //news.news_content.news_Category = json_obj.getString("news_Category");
                            news.news_content.inborn_KeyWords = json_obj.getString("inborn_KeyWords");
                            news.news_content.news_Content = json_obj.getString("news_Content");
                            news.news_content.crawl_Source = json_obj.getString("crawl_Source");
                            news.news_content.crawl_Time = json_obj.getString("crawl_Time");
                            news.news_content.news_Journal = json_obj.getString("news_Journal");
                            news.news_content.repeat_ID = json_obj.getString("repeat_ID");
                            news.news_content.seggedTitle = json_obj.getString("seggedTitle");
                            news.news_content.wordCountOfTitle = json_obj.getInt("wordCountOfTitle");
                            news.news_content.wordCountOfContent = json_obj.getInt("wordCountOfContent");

                            news.news_content.seggedPListOfContent = new ArrayList<String>();
                            JSONArray listcontent = json_obj.getJSONArray("seggedPListOfContent");
                            for (int i = 0; i < listcontent.size(); i++) {
                                news.news_content.seggedPListOfContent.add(listcontent.getString(i));
                            }
                            news.news_content.persons = new ArrayList<News.NewsDetail.Person>();
                            JSONArray person_json = json_obj.getJSONArray("persons");
                            for (int i = 0; i < person_json.size(); i++) {
                                JSONObject person_json_obj = person_json.getJSONObject(i);
                                String word = person_json_obj.getString("word");
                                int count = person_json_obj.getInt("count");
                                News.NewsDetail.Person person = news.news_content.new Person(word, count);
                                news.news_content.persons.add(person);
                            }

                            news.news_content.locations = new ArrayList<News.NewsDetail.Location>();
                            JSONArray location_json = json_obj.getJSONArray("locations");
                            for (int i = 0; i < location_json.size(); i++) {
                                JSONObject location_json_obj = location_json.getJSONObject(i);
                                String word = location_json_obj.getString("word");
                                int count = location_json_obj.getInt("count");
                                News.NewsDetail.Location location = news.news_content.new Location(word, count);
                                news.news_content.locations.add(location);
                            }
                            news.news_content.organizations = new ArrayList<News.NewsDetail.Location>();
                            JSONArray organ_json = json_obj.getJSONArray("organizations");
                            for (int i = 0; i < organ_json.size(); i++) {
                                JSONObject organ_json_obj = organ_json.getJSONObject(i);
                                String word = organ_json_obj.getString("word");
                                int count = organ_json_obj.getInt("count");
                                News.NewsDetail.Location location = news.news_content.new Location(word, count);
                                news.news_content.organizations.add(location);
                            }
                            news.news_content.Keywords = new ArrayList<News.NewsDetail.Keyword>();
                            JSONArray keyword_json = json_obj.getJSONArray("Keywords");
                            for (int i = 0; i < keyword_json.size(); i++) {
                                JSONObject keyword_json_obj = keyword_json.getJSONObject(i);
                                String word = keyword_json_obj.getString("word");
                                double count = keyword_json_obj.getDouble("score");
                                News.NewsDetail.Keyword keyword = news.news_content.new Keyword(word, count);
                                news.news_content.Keywords.add(keyword);
                            }
                            news.news_content.bagOfWords = new ArrayList<News.NewsDetail.Word>();
                            JSONArray bag_json = json_obj.getJSONArray("bagOfWords");
                            for (int i = 0; i < bag_json.size(); i++) {
                                JSONObject bag_json_obj = bag_json.getJSONObject(i);
                                String word = bag_json_obj.getString("word");
                                double count = bag_json_obj.getDouble("score");
                                News.NewsDetail.Word bagword = news.news_content.new Word(word, count);
                                news.news_content.bagOfWords.add(bagword);
                            }
                            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                            try {
                                ObjectOutputStream objectOutputStream = new ObjectOutputStream(arrayOutputStream);
                                objectOutputStream.writeObject(news.news_content);
                                objectOutputStream.flush();
                                byte data[] = arrayOutputStream.toByteArray();
                                objectOutputStream.close();
                                arrayOutputStream.close();
                                ContentValues _values = new ContentValues();
                                _values.put("Read", 1);
                                _values.put("Details", data);
                                String str = SELECT + NewsDatabase.ALL_TABLE_NAME + WHEREID;
                                Cursor c = dbmanager.query(str, new String[]{news_ID});
                                if(c.moveToFirst() == false){
                                    _values.put("ID", id);
                                    _values.put("ClassTag", tag);
                                    _values.put("Source", source);
                                    _values.put("Title", title);
                                    _values.put("Time", time);
                                    _values.put("URL", url);
                                    _values.put("Author", author);
                                    _values.put("Type", lang_type);
                                    _values.put("Pictures", pic);
                                    _values.put("Video", video);
                                    dbmanager.insert(_values, NewsDatabase.ALL_TABLE_NAME);
                                }
                                else
                                    dbmanager.update(NewsDatabase.ALL_TABLE_NAME, _values, "ID=?", new String[]{news_ID});
                            }catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            Log.d("wait", "a new");
                            Intent intent = new Intent();
                            intent.putExtra(NEWSDETAILS, news);
                            intent.putExtra(ISFAV, false);
                            intent.putExtra(SERVICEKIND, kind);
                            intent.setAction(DETAIACTION);
                            sendBroadcast(intent);
                        }
                    });
                }
            }).start();

        }
    }

    private void getResult(String keyWord) {
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .get()
                .url(SEARCH_URL + keyWord)
                .build();
        String _str = SELECT + NewsDatabase.HIS_TABLE_NAME + WHEREHIS;
        String[] _s = {keyWord};
        Cursor d = dbmanager.query(_str, _s);
        ContentValues values = new ContentValues();
        values.put("History", keyWord);
        if (d.moveToFirst() == false) {
            dbmanager.insert(values, NewsDatabase.HIS_TABLE_NAME);
        } else {
            dbmanager.update(NewsDatabase.HIS_TABLE_NAME, values, "History=?", new String[]{keyWord});
            Log.d("update", "sucess");
        }
        if (d != null && !d.isClosed())
            d.close();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("begin", "SEARCH");
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response.code());
                        //Log.d("Response", response.body().string());
                        String body = response.body().string();
                        JSONObject json = JSONObject.fromObject(body);
                        JSONArray list = json.getJSONArray("list");
                        ArrayList<News> newslist = new ArrayList<News>();
                        for (int i = 0; i < list.size(); i++) {
                            JSONObject json_obj = list.getJSONObject(i);
                            //Log.d("res", json_obj.toString());
                            String id = json_obj.getString("news_ID");
                            String tag = json_obj.getString("newsClassTag");
                            String source = json_obj.getString("news_Source");
                            String title = json_obj.getString("news_Title");
                            String time = json_obj.getString("news_Time");
                            String url = json_obj.getString("news_URL");
                            String lang_type = json_obj.getString("lang_Type");
                            String author = json_obj.getString("news_Author");
                            String pic = json_obj.getString("news_Pictures");
                            String video = json_obj.getString("news_Video");
                            String intro = json_obj.getString("news_Intro");
                            News news = new News(tag, id, source, title, time, url, author, lang_type, pic, video, intro);
                            newslist.add(news);
                            Log.d("tag", tag);
                        }
                        Log.d("wait", "a minute");
                        Intent intent = new Intent();
                        intent.putExtra(NEWSLIST, newslist);
                        intent.setAction(SEARCHACTION);
                        sendBroadcast(intent);
                    }
                });
            }
        }).start();
    }

    private void getRecommend(String keyWord, final String kind) {
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .get()
                .url(SEARCH_URL + keyWord)
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("begin", "SEARCH");
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response.code());
                        //Log.d("Response", response.body().string());
                        String body = response.body().string();
                        JSONObject json = JSONObject.fromObject(body);
                        JSONArray list = json.getJSONArray("list");
                        ArrayList<News> newslist = new ArrayList<News>();
                        for (int i = 0; i < list.size(); i++) {
                            JSONObject json_obj = list.getJSONObject(i);
                            //Log.d("res", json_obj.toString());
                            String id = json_obj.getString("news_ID");
                            String tag = json_obj.getString("newsClassTag");
                            String source = json_obj.getString("news_Source");
                            String title = json_obj.getString("news_Title");
                            String time = json_obj.getString("news_Time");
                            String url = json_obj.getString("news_URL");
                            String lang_type = json_obj.getString("lang_Type");
                            String author = json_obj.getString("news_Author");
                            String pic = json_obj.getString("news_Pictures");
                            String video = json_obj.getString("news_Video");
                            String intro = json_obj.getString("news_Intro");
                            News news = new News(tag, id, source, title, time, url, author, lang_type, pic, video, intro);
                            newslist.add(news);
                            //Log.d("tag", tag);
                        }
                        Log.d("wait", "a minute");
                        Intent intent = new Intent();
                        intent.putExtra(NEWSLIST, newslist);
                        intent.putExtra(SERVICEKIND, kind);
                        intent.setAction(RECOMMEDACTION);
                        sendBroadcast(intent);
                    }
                });
            }
        }).start();
    }

    private void addFavorite(String news_ID, String kind) {
        String _str = SELECT + NewsDatabase.FAV_TABLE_NAME + WHEREID;
        String[] _s = {news_ID};
        Cursor c = dbmanager.query(_str, _s);
        if (c.moveToFirst() == true) {
            dbmanager.delete(news_ID, NewsDatabase.FAV_TABLE_NAME);
        } else {
            _str = SELECT + NewsDatabase.ALL_TABLE_NAME + WHEREID;
            c = dbmanager.query(_str, _s);
            if (c.moveToFirst() == true) {
                String id = c.getString(c.getColumnIndex("ID"));
                String tag = c.getString(c.getColumnIndex("ClassTag"));
                String source = c.getString(c.getColumnIndex("Source"));
                String title = c.getString(c.getColumnIndex("Title"));
                String time = c.getString(c.getColumnIndex("Time"));
                String url = c.getString(c.getColumnIndex("URL"));
                String lang_type = c.getString(c.getColumnIndex("Type"));
                ;
                String author = c.getString(c.getColumnIndex("Author"));
                String pic = c.getString(c.getColumnIndex("Pictures"));
                String video = c.getString(c.getColumnIndex("Video"));

                ContentValues values = new ContentValues();
                values.put("ID", id);
                values.put("ClassTag", tag);
                values.put("Source", source);
                values.put("Title", title);
                values.put("Time", time);
                values.put("URL", url);
                values.put("Type", lang_type);
                values.put("Author", author);
                values.put("Pictures", pic);
                values.put("Video", video);
                values.put("Read", 1);
                byte data[] = c.getBlob(c.getColumnIndex("Details"));
                values.put("Details", data);
                dbmanager.insert(values, NewsDatabase.FAV_TABLE_NAME);
            }
        }
        if (c != null && !c.isClosed())
            c.close();
        Intent intent = new Intent();
        intent.putExtra(SERVICEKIND, kind);
        if(kind == DELEATE_ONE)
            intent.setAction(FAVACTION);
        else
            intent.setAction(DETAIACTION);
        sendBroadcast(intent);
    }

    private void clearLocal() {
        dbmanager.delete_all(NewsDatabase.ALL_TABLE_NAME);
    }
    private void clearFav(String kind){
        dbmanager.delete_all(NewsDatabase.FAV_TABLE_NAME);
        Intent intent = new Intent();
        intent.putExtra(SERVICEKIND, kind);
        intent.setAction(FAVACTION);
        sendBroadcast(intent);
    }
    private void getHistory(final String kind) {
        String str = SELECT + NewsDatabase.HIS_TABLE_NAME;
        Cursor c = dbmanager.query(str, null);
        ArrayList<String> historylist = new ArrayList<String>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            historylist.add(c.getString(c.getColumnIndex("History")));
        }
        if (c != null && !c.isClosed())
            c.close();
        Intent intent = new Intent();
        intent.putExtra(HISTORYLIST, historylist);
        intent.putExtra(SERVICEKIND, kind);
        intent.setAction(RECOMMEDACTION);
        sendBroadcast(intent);
    }

    private void getFavorite(final String kind) {
        String str = SELECT + NewsDatabase.FAV_TABLE_NAME;
        Cursor c = dbmanager.query(str, null);
        ArrayList<News> favlist = new ArrayList<News>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String id = c.getString(c.getColumnIndex("ID"));
            String classtag = c.getString(c.getColumnIndex("ClassTag"));
            String source = c.getString(c.getColumnIndex("Source"));
            String Time = c.getString(c.getColumnIndex("Time"));
            String Title = c.getString(c.getColumnIndex("Title"));
            String URL = c.getString(c.getColumnIndex("URL"));
            String Author = c.getString(c.getColumnIndex("Author"));
            String Type = c.getString(c.getColumnIndex("Type"));
            String Pictures = c.getString(c.getColumnIndex("Pictures"));
            String Video = c.getString(c.getColumnIndex("Video"));
            int Read = c.getInt(c.getColumnIndex("Read"));
            boolean _read = Read == 1 ? true : false;
            byte data[] = c.getBlob(c.getColumnIndex("Details"));
            News news = new News(classtag, id, source, Title, Time, URL, Author, Type, Pictures, Video, null);
            news.read = _read;
            ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
            try {
                ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
                news.news_content = (News.NewsDetail) inputStream.readObject();
                inputStream.close();
                arrayInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            favlist.add(news);
        }
        Intent intent = new Intent();
        intent.putExtra(FAVLIST, favlist);
        intent.putExtra(SERVICEKIND, kind);
        intent.setAction(FAVACTION);
        sendBroadcast(intent);

    }
}
