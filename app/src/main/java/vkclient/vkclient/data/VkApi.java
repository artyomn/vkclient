package vkclient.vkclient.data;


import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;
import vkclient.vkclient.data.entity.VkSavedFile;
import vkclient.vkclient.data.entity.VkSavedWallPhoto;
import vkclient.vkclient.data.entity.VkUploadURL;
import vkclient.vkclient.data.entity.VkUploadedDocument;
import vkclient.vkclient.data.entity.VkUploadedPhoto;
import vkclient.vkclient.data.entity.VkUser;
import vkclient.vkclient.data.entity.VkWallPost;

public interface VkApi {

    String VK_API_VERSION = "5.78";
    String VK_API_LANG = "ru";
    String VK_API_PATH = "https://api.vk.com/method/";

    @GET("account.getProfileInfo")
    Call<VkUser> getProfileInfo(@Query("access_token") String accessToken,
                                @Query("lang") String vkApiLang,
                                @Query("v") String vkApiVersion);

    @GET("docs.getUploadServer")
    Call<VkUploadURL> getUploadUrlDocument(@Query("access_token") String accessToken,
                                           @Query("lang") String vkApiLang,
                                           @Query("v") String vkApiVersion);

    @Multipart
    @POST
    Call<VkUploadedDocument> uploadDocument(@Url String url, @Part MultipartBody.Part file);

    @Multipart
    @POST
    Call<VkUploadedPhoto> uploadPhoto(@Url String url, @Part MultipartBody.Part file);

    @GET("docs.save")
    Call<VkSavedFile> saveDocument(@Query("access_token") String accessToken,
                                   @Query("lang") String vkApiLang,
                                   @Query("v") String vkApiVersion,
                                   @Query("file") String file,
                                   @Query("title") String title,
                                   @Query("tags") String tags);

    @POST("wall.post")
    Call<VkWallPost> wallPost(@Query("access_token") String accessToken,
                              @Query("lang") String vkApiLang,
                              @Query("v") String vkApiVersion,
                              @Query("attachments") String attachments);

    @POST("wall.post")
    Call<VkWallPost> wallPostWithLocation(@Query("access_token") String accessToken,
                                          @Query("lang") String vkApiLang,
                                          @Query("v") String vkApiVersion,
                                          @Query("attachments") String attachments,
                                          @Query("lat") Double lat,
                                          @Query("long") Double lng);

    @GET("photos.getWallUploadServer")
    Call<VkUploadURL> getUploadUrlPhoto(@Query("access_token") String accessToken,
                                        @Query("lang") String vkApiLang,
                                        @Query("v") String vkApiVersion);

    @GET("photos.saveWallPhoto")
    Call<VkSavedWallPhoto> saveWallPhoto(@Query("access_token") String accessToken,
                                         @Query("lang") String vkApiLang,
                                         @Query("v") String vkApiVersion,
                                         @Query("server") int server,
                                         @Query("photo") String photo,
                                         @Query("hash") String hash);

}
