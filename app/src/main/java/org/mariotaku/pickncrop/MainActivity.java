/*
 * Copyright (c) 2015 mariotaku
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.pickncrop;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.mariotaku.pickncrop.library.MediaPickerActivity;


public class MainActivity extends Activity {

    private static final int REQUEST_PICK_MEDIA = 101;
    private static final int REQUEST_REQUEST_PERMISSION = 201;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebViewClient client = new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }
        };
        mWebView.setWebViewClient(client);

        WebSettings settings = mWebView.getSettings();
        settings.setLoadsImagesAutomatically(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setAllowFileAccess(true);

        findViewById(R.id.pick_media).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        findViewById(R.id.capture_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureVideo();
            }
        });
        findViewById(R.id.pick_and_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickAndCrop();
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_REQUEST_PERMISSION);
        }
    }

    @Override
    protected void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mWebView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mWebView.destroy();
        super.onDestroy();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mWebView = (WebView) findViewById(R.id.webview);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_MEDIA: {
                if (resultCode == RESULT_OK) {
                    StringBuilder hb = new StringBuilder();
                    hb.append("<!DOCTYPE html>");
                    hb.append("<html>");
                    hb.append("<head>");
                    hb.append("<meta charset=\"UTF-8\">");
                    hb.append("</head>");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clipData = data.getClipData();
                        for (int i = 0, j = clipData.getItemCount(); i < j; i++) {
                            final ClipData.Item item = clipData.getItemAt(i);
                            final Uri uri = item.getUri();
                            final String type = getContentResolver().getType(uri);
                            if (type != null && type.startsWith("video/")) {
                                hb.append("<video src='");
                                hb.append(uri.toString());
                                hb.append("'/>");
                            } else {
                                hb.append("<img src='");
                                hb.append(uri.toString());
                                hb.append("'/>");
                            }
                            hb.append("</br>");
                        }
                    } else {
                        final String type = data.resolveType(this);
                        if (type != null && type.startsWith("video/")) {
                            hb.append("<video src='");
                            hb.append(data.getDataString());
                            hb.append("'/>");
                        } else {
                            hb.append("<img src='");
                            hb.append(data.getDataString());
                            hb.append("'/>");
                        }
                    }

                    hb.append("</html>");
                    mWebView.loadDataWithBaseURL("http://example.com/", hb.toString(), "text/html", "UTF-8", null);
                }
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void takePhoto() {
        final Intent intent = MediaPickerActivity.with(this)
                .takePhoto()
                .build();
        startActivityForResult(intent, REQUEST_PICK_MEDIA);
    }

    private void captureVideo() {
        final Intent intent = MediaPickerActivity.with(this)
                .captureVideo()
                .videoQuality(0)
                .build();
        startActivityForResult(intent, REQUEST_PICK_MEDIA);
    }

    private void pickImage() {
        final Intent intent = MediaPickerActivity.with(this)
                .pickMedia()
                .containsVideo(true)
                .videoOnly(false)
                .allowMultiple(true)
                .build();
        startActivityForResult(intent, REQUEST_PICK_MEDIA);
    }

    private void pickAndCrop() {
        final Intent intent = MediaPickerActivity.with(this)
                .pickMedia()
                .containsVideo(false)
                .allowMultiple(false)
                .aspectRatio(1, 1)
                .maximumSize(512, 512)
                .build();
        startActivityForResult(intent, REQUEST_PICK_MEDIA);
    }

}
