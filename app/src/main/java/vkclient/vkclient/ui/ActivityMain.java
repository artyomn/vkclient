package vkclient.vkclient.ui;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import vkclient.vkclient.BuildConfig;
import vkclient.vkclient.R;
import vkclient.vkclient.ui.dialog.DialogFragmentLocationNeeded;
import vkclient.vkclient.util.Utils;
import vkclient.vkclient.vm.VMActivityMain;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FragmentSignin.Listener, FragmentActions.Listener, DialogFragmentLocationNeeded.Listener,
        FragmentAudio.Listener {

    private static final int CODE_AUTH = 100;
    private static final int PERMISSIONS_REQUEST_LOCATION = 101;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 102;

    private static final int REQUEST_TAKE_PHOTO = 103;

    private static final String UPLOADING_FRAGMENT_TAG = "uploading";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(android.R.id.content)
    View root;

    TextView vkUsername;

    private VMActivityMain viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = ViewModelProviders.of(this).get(VMActivityMain.class);
        ButterKnife.bind(this);
        vkUsername = navigationView.getHeaderView(0).findViewById(R.id.username);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            if (viewModel.isAuthorized()) {
                showActions();
            } else {
                showAuth();
            }
        }

        viewModel.openAudio().observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                showAudioFragment();
            }
        });

        viewModel.requestAudioPermission().observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                requestAudioPermission();
            }
        });

        viewModel.showAudioPermissionSnackbar().observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                Snackbar.make(root, R.string.snackbar_audio_permission_required, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.username().observe(this, username -> {
            vkUsername.setText(username);
            vkUsername.setVisibility(View.VISIBLE);
        });

        viewModel.showProcessing().observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                showProcessing();
            }
        });

        viewModel.showUploading().observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                showUploading();
            }
        });

        viewModel.errorAudioRecording().observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                showActions();
                Snackbar.make(root, R.string.label_error_audio_recording, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.errorUploading().observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                showActions();
                Snackbar.make(root, R.string.label_error_uploading, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.finishUploading().observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                showActions();
                Snackbar.make(root, R.string.label_upload_success, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.uploadingProgreess().observe(this, integer -> {
            if (integer != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment = fragmentManager.findFragmentByTag(UPLOADING_FRAGMENT_TAG);
                if (fragment != null) {
                    ((FragmentUploading) fragment).updateProgress(integer);
                }
            }
        });

        viewModel.startCameraIntent().observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                startCameraIntent();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getVkProfile();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            viewModel.logout();
            showAuth();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_AUTH) {
            if (resultCode == RESULT_OK) {
                String token = data.getStringExtra(ActivityAuth.EXTRA_TOKEN);
                viewModel.tokenReceived(token);
                showActions();
            } else if (resultCode == ActivityAuth.RESULT_ERROR) {
                showAuthError();
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                viewModel.startPhotoUploading();
            } else {
                showCameraError();
            }
        }
    }

    private void showAuth() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Fragment fragment = FragmentSignin.newInstance();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_placeholder, fragment);
        ft.commit();
        setLogout(false);
        vkUsername.setVisibility(View.GONE);
    }

    private void showActions() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Fragment fragment = FragmentActions.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_placeholder, fragment);
        ft.commit();
        setLogout(true);
        if (viewModel.locationRequestAllowed()) {
            DialogFragment dialogFragment = DialogFragmentLocationNeeded.newInstance();
            dialogFragment.show(getSupportFragmentManager(), "dialog_location");
            viewModel.locationRequested();
        }
    }

    private void setLogout(boolean visible) {
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_logout).setVisible(visible);
    }

    private void showAuthError() {
        Snackbar.make(root, R.string.label_auth_error, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onStartSignin() {
        Intent intent = new Intent(this, ActivityAuth.class);
        startActivityForResult(intent, CODE_AUTH);
    }

    @Override
    public void onStartCamera() {
        viewModel.openPhotoClick();
    }

    @Override
    public void onStartMic() {
        viewModel.openAudioClick();
    }

    @Override
    public void onShowLocationRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
    }

    private void showAudioFragment() {
        Fragment fragment = FragmentAudio.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_placeholder, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSIONS_REQUEST_RECORD_AUDIO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.audioPermissionGranted();
                } else {
                    viewModel.audioPermissionRejected();
                }
                return;
            }
        }
    }

    @Override
    public void onAudioRecordingStart() {
        viewModel.startAudioRecording();
    }

    @Override
    public void onAudioRecordingFinish() {
        viewModel.stopAudioRecording();
    }

    private void showProcessing() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = FragmentProcessing.newInstance();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_placeholder, fragment);
        ft.commit();
    }

    private void showUploading() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = FragmentUploading.newInstance();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_placeholder, fragment, UPLOADING_FRAGMENT_TAG);
        ft.commit();
    }

    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = viewModel.getPhotoFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            } else {
                showCameraError();
            }
        } else {
            showCameraError();
        }
    }

    private void showCameraError() {
        Snackbar.make(root, R.string.label_error_camera, Snackbar.LENGTH_SHORT).show();
    }
}
