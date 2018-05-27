package vkclient.vkclient.vm;

import android.Manifest;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vkclient.vkclient.VkClient;
import vkclient.vkclient.data.VkApi;
import vkclient.vkclient.data.entity.VkSavedFile;
import vkclient.vkclient.data.entity.VkSavedWallPhoto;
import vkclient.vkclient.data.entity.VkUploadURL;
import vkclient.vkclient.data.entity.VkUploadedDocument;
import vkclient.vkclient.data.entity.VkUploadedPhoto;
import vkclient.vkclient.data.entity.VkUser;
import vkclient.vkclient.data.entity.VkWallPost;
import vkclient.vkclient.util.AudioRecorder;
import vkclient.vkclient.util.ProgressRequestBody;
import vkclient.vkclient.util.SingleLiveEvent;
import vkclient.vkclient.util.Utils;
import vkclient.vkclient.util.ZipArchiver;

public class VMActivityMain extends AndroidViewModel implements ProgressRequestBody.UploadCallbacks {

    public VMActivityMain(@NonNull Application application) {
        super(application);
        prefs = PreferenceManager.getDefaultSharedPreferences(application);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication());
    }

    private MutableLiveData<Boolean> openAudio = new SingleLiveEvent<>();
    private MutableLiveData<Boolean> requestAudioPermission = new SingleLiveEvent<>();
    private MutableLiveData<Boolean> showAudioPermissionSnackbar = new SingleLiveEvent<>();
    private MutableLiveData<Boolean> showProcessing = new SingleLiveEvent<>();
    private MutableLiveData<Boolean> showUploading = new SingleLiveEvent<>();
    private MutableLiveData<String> username = new MutableLiveData<>();
    private MutableLiveData<Boolean> errorAudioRecording = new SingleLiveEvent<>();
    private MutableLiveData<Boolean> errorUploading = new SingleLiveEvent<>();
    private MutableLiveData<Boolean> finishUploading = new SingleLiveEvent<>();
    private MutableLiveData<Integer> uploadingProgreess = new MutableLiveData<>();

    private MutableLiveData<Boolean> startCameraIntent = new SingleLiveEvent<>();

    private SharedPreferences prefs;
    private FusedLocationProviderClient fusedLocationClient;

    private double lat;
    private double lng;

    public LiveData<Boolean> openAudio() {
        return openAudio;
    }

    public MutableLiveData<Boolean> requestAudioPermission() {
        return requestAudioPermission;
    }

    public MutableLiveData<Boolean> showAudioPermissionSnackbar() {
        return showAudioPermissionSnackbar;
    }

    public MutableLiveData<Boolean> showProcessing() {
        return showProcessing;
    }

    public MutableLiveData<Boolean> showUploading() {
        return showUploading;
    }

    public LiveData<String> username() {
        return username;
    }

    public MutableLiveData<Boolean> errorAudioRecording() {
        return errorAudioRecording;
    }

    public MutableLiveData<Boolean> errorUploading() {
        return errorUploading;
    }

    public MutableLiveData<Boolean> finishUploading() {
        return finishUploading;
    }

    public MutableLiveData<Integer> uploadingProgreess() {
        return uploadingProgreess;
    }

    public MutableLiveData<Boolean> startCameraIntent() {
        return startCameraIntent;
    }

    public void tokenReceived(String token) {
        prefs.edit().putString("token", token).apply();
    }

    public boolean isAuthorized() {
        return prefs.getString("token", null) != null;
    }

    public void logout() {
        prefs.edit().remove("token").apply();
        Utils.clearCookies(getApplication());
    }

    public boolean locationRequestAllowed() {
        return !prefs.getBoolean("location_requested", false);
    }

    public void locationRequested() {
        prefs.edit().putBoolean("location_requested", true).apply();
    }

    public void getVkProfile() {
        String token = prefs.getString("token", null);
        if (token == null) {
            return;
        }
        VkClient vkClient = getApplication();
        vkClient.getVkApi().getProfileInfo(token, VkApi.VK_API_LANG, VkApi.VK_API_VERSION).enqueue(new Callback<VkUser>() {
            @Override
            public void onResponse(@NonNull Call<VkUser> call, @NonNull Response<VkUser> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    VkUser vkUser = response.body();
                    if (vkUser == null || vkUser.response == null) {
                        username.setValue("");
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    if (vkUser.response.first_name != null) {
                        sb.append(vkUser.response.first_name);
                        sb.append(" ");
                    }
                    if (vkUser.response.last_name != null) {
                        sb.append(vkUser.response.last_name);
                    }
                    username.setValue(sb.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VkUser> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void openAudioClick() {
        loadLocation();
        if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            openAudio.setValue(true);
        } else {
            requestAudioPermission.setValue(true);
        }
    }

    public void openPhotoClick() {
        loadLocation();
        startCameraIntent.setValue(true);
    }

    private void loadLocation() {
        if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                        }
                    });
        }
    }

    public void audioPermissionGranted() {
        openAudio.setValue(true);
    }

    public void audioPermissionRejected() {
        showAudioPermissionSnackbar.setValue(true);
    }

    private AudioRecorder audioRecorder;
    private File audioFile;

    public void startAudioRecording() {
        audioFile = Utils.getTempFile(getApplication(), "audio", ".mp4");
        if (audioFile == null) {
            errorAudioRecording.setValue(true);
            return;
        }
        try {
            audioRecorder = new AudioRecorder(audioFile);
            audioRecorder.startRecording();
        } catch (AudioRecorder.Exception e) {
            errorAudioRecording.setValue(true);
        }
    }

    public void stopAudioRecording() {
        try {
            audioRecorder.stopRecording();
            processAudio(audioFile);
        } catch (AudioRecorder.Exception e) {
            errorAudioRecording.setValue(true);
        }
        audioRecorder = null;
    }

    private void processAudio(File audioFile) {
        showProcessing.setValue(true);
        File zipFile = Utils.getTempFile(getApplication(), "audio", ".zip");
        if (zipFile == null) {
            errorAudioRecording.setValue(true);
            return;
        }
        new Thread(() -> {
            try {
                ZipArchiver.zip(new String[]{audioFile.getAbsolutePath()}, zipFile.getAbsolutePath());
                audioFile.delete();
                uploadAudio(zipFile);
            } catch (IOException e) {
                errorAudioRecording.setValue(true);
            }
        }).start();
    }

    private void uploadAudio(File zipFile) {
        uploadingProgreess.postValue(0);
        showUploading.postValue(true);
        String token = prefs.getString("token", null);
        if (token == null) {
            errorUploading.postValue(true);
            return;
        }
        VkClient app = getApplication();
        app.getVkApi().getUploadUrlDocument(token, VkApi.VK_API_LANG, VkApi.VK_API_VERSION).enqueue(new Callback<VkUploadURL>() {
            @Override
            public void onResponse(@NonNull Call<VkUploadURL> call, @NonNull Response<VkUploadURL> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    VkUploadURL vkUploadURL = response.body();
                    if (vkUploadURL != null && vkUploadURL.response != null) {
                        doUploadDocument(zipFile, vkUploadURL.response.upload_url);
                    } else {
                        errorUploading.setValue(true);
                    }
                } else {
                    errorUploading.setValue(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VkUploadURL> call, @NonNull Throwable t) {
                errorUploading.setValue(true);
                t.printStackTrace();
            }
        });
    }

    private void doUploadDocument(File zipFile, String url) {
        ProgressRequestBody fileBody = new ProgressRequestBody(zipFile, this);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", zipFile.getName(), fileBody);
        VkClient app = getApplication();
        app.getVkApi().uploadDocument(url, filePart).enqueue(new Callback<VkUploadedDocument>() {
            @Override
            public void onResponse(@NonNull Call<VkUploadedDocument> call, @NonNull Response<VkUploadedDocument> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    VkUploadedDocument vkUploadedDocument = response.body();
                    if (vkUploadedDocument != null && vkUploadedDocument.file != null) {
                        saveDocument(vkUploadedDocument.file, zipFile.getName());
                        zipFile.delete();
                    } else {
                        errorUploading.setValue(true);
                    }
                } else {
                    errorUploading.setValue(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VkUploadedDocument> call, @NonNull Throwable t) {
                errorUploading.setValue(true);
                t.printStackTrace();
            }
        });
    }

    private void saveDocument(String file, String title) {
        String token = prefs.getString("token", null);
        if (token == null) {
            errorUploading.postValue(true);
            return;
        }
        VkClient app = getApplication();
        app.getVkApi().saveDocument(token, VkApi.VK_API_LANG, VkApi.VK_API_VERSION, file, title, "VkClientUpload").enqueue(new Callback<VkSavedFile>() {
            @Override
            public void onResponse(@NonNull Call<VkSavedFile> call, @NonNull Response<VkSavedFile> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    VkSavedFile vkSavedFile = response.body();
                    if (vkSavedFile != null && vkSavedFile.response != null && vkSavedFile.response.length > 0) {
                        wallPost("doc", vkSavedFile.response[0].id, vkSavedFile.response[0].owner_id);
                    } else {
                        errorUploading.setValue(true);
                    }
                } else {
                    errorUploading.setValue(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VkSavedFile> call, @NonNull Throwable t) {
                errorUploading.setValue(true);
                t.printStackTrace();
            }
        });
    }

    private void wallPost(String type, int id, int ownerId) {
        String token = prefs.getString("token", null);
        if (token == null) {
            errorUploading.postValue(true);
            return;
        }
        String attachment = type + ownerId + "_" + id;
        Callback<VkWallPost> callback = new Callback<VkWallPost>() {
            @Override
            public void onResponse(@NonNull Call<VkWallPost> call, Response<VkWallPost> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    VkWallPost vkWallPost = response.body();
                    if (vkWallPost != null && vkWallPost.response != null) {
                        uploadingProgreess.setValue(100);
                        finishUploading.setValue(true);
                    } else {
                        errorUploading.setValue(true);
                    }
                } else {
                    errorUploading.setValue(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VkWallPost> call, Throwable t) {
                errorUploading.setValue(true);
                t.printStackTrace();
            }
        };
        VkClient vkClient = getApplication();
        if (lat != 0 || lng != 0) {
            vkClient.getVkApi().wallPostWithLocation(token, VkApi.VK_API_LANG, VkApi.VK_API_VERSION, attachment, lat, lng).enqueue(callback);
        } else {
            vkClient.getVkApi().wallPost(token, VkApi.VK_API_LANG, VkApi.VK_API_VERSION, attachment).enqueue(callback);
        }
    }

    @Override
    public void onProgressUpdate(int percentage) {
        uploadingProgreess.postValue(percentage);
    }

    private File photoFile;

    public File getPhotoFile() {
        photoFile = Utils.getTempFile(getApplication(), "photo", ".jpg");
        return photoFile;
    }

    public void startPhotoUploading() {
        showUploading.setValue(true);
        uploadingProgreess.setValue(0);
        String token = prefs.getString("token", null);
        if (token == null) {
            errorUploading.postValue(true);
            return;
        }
        VkClient app = getApplication();
        app.getVkApi().getUploadUrlPhoto(token, VkApi.VK_API_LANG, VkApi.VK_API_VERSION).enqueue(new Callback<VkUploadURL>() {
            @Override
            public void onResponse(@NonNull Call<VkUploadURL> call, @NonNull Response<VkUploadURL> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    VkUploadURL vkUploadURL = response.body();
                    if (vkUploadURL != null && vkUploadURL.response != null) {
                        doUploadPhoto(photoFile, vkUploadURL.response.upload_url);
                    } else {
                        errorUploading.setValue(true);
                    }
                } else {
                    errorUploading.setValue(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VkUploadURL> call, @NonNull Throwable t) {
                errorUploading.setValue(true);
                t.printStackTrace();
            }
        });
    }

    private void doUploadPhoto(File photoFile, String url) {
        ProgressRequestBody fileBody = new ProgressRequestBody(photoFile, this);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("photo", photoFile.getName(), fileBody);
        VkClient app = getApplication();
        app.getVkApi().uploadPhoto(url, filePart).enqueue(new Callback<VkUploadedPhoto>() {
            @Override
            public void onResponse(@NonNull Call<VkUploadedPhoto> call, @NonNull Response<VkUploadedPhoto> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    VkUploadedPhoto vkUploadedPhoto = response.body();
                    if (vkUploadedPhoto != null) {
                        savePhoto(vkUploadedPhoto.server, vkUploadedPhoto.photo, vkUploadedPhoto.hash);
                        photoFile.delete();
                    } else {
                        errorUploading.setValue(true);
                    }
                } else {
                    errorUploading.setValue(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VkUploadedPhoto> call, @NonNull Throwable t) {
                errorUploading.setValue(true);
                t.printStackTrace();
            }
        });
    }

    private void savePhoto(int server, String photo, String hash) {
        String token = prefs.getString("token", null);
        if (token == null) {
            errorUploading.postValue(true);
            return;
        }
        VkClient app = getApplication();
        app.getVkApi().saveWallPhoto(token, VkApi.VK_API_LANG, VkApi.VK_API_VERSION, server, photo, hash).enqueue(new Callback<VkSavedWallPhoto>() {
            @Override
            public void onResponse(@NonNull Call<VkSavedWallPhoto> call, @NonNull Response<VkSavedWallPhoto> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    VkSavedWallPhoto vkSavedWallPhoto = response.body();
                    if (vkSavedWallPhoto != null && vkSavedWallPhoto.response != null && vkSavedWallPhoto.response.length > 0) {
                        wallPost("photo", vkSavedWallPhoto.response[0].id, vkSavedWallPhoto.response[0].owner_id);
                    } else {
                        errorUploading.setValue(true);
                    }
                } else {
                    errorUploading.setValue(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VkSavedWallPhoto> call, @NonNull Throwable t) {
                errorUploading.setValue(true);
                t.printStackTrace();
            }
        });
    }

}
