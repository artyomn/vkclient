package vkclient.vkclient.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import vkclient.vkclient.BuildConfig;
import vkclient.vkclient.R;
import vkclient.vkclient.data.VkApi;
import vkclient.vkclient.util.Utils;

public class ActivityAuth extends AppCompatActivity {

    public static final int RESULT_ERROR = 33;
    public static final String EXTRA_TOKEN = "token";

    private static final String AUTH_HOST = "oauth.vk.com";
    private static final String AUTH_PATH = "authorize";
    private static final String AUTH_REDIRECT_PATH = "blank.html";
    private static final String AUTH_CLIENT_ID = BuildConfig.OAUTH_ID;
    private static final String AUTH_SCOPE = "139268";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.auth_webview)
    WebView webView;
    @BindView(R.id.auth_progress)
    ProgressBar progressBar;

    private String state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        state = Utils.generateOauthState();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(AUTH_HOST)
                .appendPath(AUTH_PATH)
                .appendQueryParameter("client_id", AUTH_CLIENT_ID)
                .appendQueryParameter("redirect_uri", "https://oauth.vk.com/blank.html")
                .appendQueryParameter("display", "mobile")
                .appendQueryParameter("scope", AUTH_SCOPE)
                .appendQueryParameter("response_type", "token")
                .appendQueryParameter("v", VkApi.VK_API_VERSION)
                .appendQueryParameter("state", state);

        String url = builder.build().toString();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                String fragment = uri.getFragment();
                if (AUTH_HOST.equals(uri.getAuthority()) &&
                        AUTH_REDIRECT_PATH.equals(uri.getLastPathSegment()) &&
                        fragment != null) {
                    Map<String, String> data = new HashMap<>();
                    String[] params = fragment.split("&");
                    for (String param : params) {
                        String[] paramData = param.split("=");
                        data.put(paramData[0], URLDecoder.decode(paramData[1]));
                    }
                    if (data.containsKey("access_token")) {
                        if (state.equals(data.get("state"))) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_TOKEN, data.get("access_token"));
                            setResult(RESULT_OK, intent);
                        } else {
                            setResult(RESULT_ERROR);
                        }
                    } else {
                        setResult(RESULT_ERROR);
                    }
                    finish();
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                webView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

        if (savedInstanceState == null) {
            webView.loadUrl(url);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

}
