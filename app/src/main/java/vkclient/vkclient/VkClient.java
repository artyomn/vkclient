package vkclient.vkclient;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vkclient.vkclient.data.VkApi;

public class VkClient extends Application{
    private VkApi vkApi;

    public VkApi getVkApi() {
        return vkApi;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(VkApi.VK_API_PATH)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        vkApi = retrofit.create(VkApi.class);
    }

}
