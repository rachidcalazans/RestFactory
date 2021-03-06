package com.rogalabs.restfactory;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rogalabs.restfactory.defaults.LogInterceptor;
import com.rogalabs.restfactory.parser.DateSerializer;
import com.rogalabs.restfactory.parser.MixedDateDeserializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;

/**
 * Created by roga on 19/01/16.
 */
public class RestFactory extends MakeRest {

    private static String baseUrl;
    private static List<String> dateFormats;
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public RestFactory() {
        super();
    }

    public RestFactory(String baseUrl) {
        super();
        this.baseUrl = baseUrl;
    }

    public RestFactory(String baseUrl , List<String> dateFormats) {
        super();
        this.baseUrl = baseUrl;
        this.dateFormats = dateFormats;
    }

    private static <T> T create(Class<T> endpoint, String baseUrl,List<String> dateFormats) {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new LogInterceptor()).build();
        Gson gson = buildGson(dateFormats);
        Retrofit retrofit = buildRetroFit(client, gson, baseUrl);
        return retrofit.create(endpoint);
    }

    private static Gson buildGson(List<String> dateFormats) {
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateSerializer(DEFAULT_DATE_FORMAT))
                .registerTypeAdapter(Date.class , new MixedDateDeserializer(dateFormats))
                .create();
    }

    private static Retrofit buildRetroFit(OkHttpClient client, Gson gson, String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    public static final class Builder {
        private String baseUrl;
        private List<String> dateFormats = new ArrayList<>();

        public Builder setBaseUrl(String url) {
            this.baseUrl = url;
            return this;
        }

        public Builder addDateFormat(String format) {
            this.dateFormats.add(format);
            return this;
        }

        public Builder addDateFormat(String[] formats) {
            this.dateFormats.addAll(Arrays.asList(formats));
            return this;
        }

        public RestFactory build() {
            if (dateFormats.isEmpty())
                return new RestFactory(baseUrl);
            return new RestFactory(baseUrl,dateFormats);
        }
    }


    public static void make(final Object context) {
        load(context, new Listener() {
            @Override
            public void onLoad(Field field) {
                try {
                    field.set(context, create(field.getType(), baseUrl , dateFormats));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
