package com.example.william.test442;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
	public static final String WEBVIEW_URL = "EA_WEBVIEW_URL";
	public static final String EXTRA_TITLE = "EXTRA_TITLE";
	private final static int FILECHOOSER_RESULTCODE = 1;
	private static final int INPUT_FILE_REQUEST_CODE = 9999;
	private static final String TAG = "HoroscopeActivity";
	private static final int FILECHOOSER_RESULTCODE_KITKAT = 322;
	private int TTL_INT = 0;
	private WebView webView;
	private ValueCallback<Uri> mUploadMessage;
	private ValueCallback<Uri[]> mFilePathCallback;
	private String mCameraPhotoPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		webView = (WebView) findViewById(R.id.wv);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new HoroscopeJavaInterface(this), "Android");
//		webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		webView.setBackgroundColor(Color.TRANSPARENT);
		webView.setWebViewClient(new WebViewClient() {
			private boolean mbSuccessPage = true;

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (mbSuccessPage) {
				}
				super.onPageFinished(view, url);
				view.clearCache(true);
			}

			//			@Override
//			public void onPageStarted(WebView view, String url, Bitmap favicon) {
//				super.onPageStarted(view, url, favicon);
//				mbSuccessPage = true;
//			}
			@Override
			public void onReceivedError(final WebView view, int errorCode, String description,
			                            final String failingUrl) {
				Log.e(String.valueOf(errorCode), description);
				mbSuccessPage = false;
				if (!mbSuccessPage) {
					String customErrorPageHtml = "<html></html>";
					view.loadData(customErrorPageHtml, "text/html", null);
					view.clearCache(true);
				}
				//control your layout, show something like a retry button, and
				//call view.loadUrl(failingUrl) to reload.
				super.onReceivedError(view, errorCode, description, failingUrl);
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
			}
		});
		webView.setWebChromeClient(new WebChromeClient() {
			// Android < 3.0
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				showAttachmentDialog(uploadMsg);
			}

			// For Android >= 5.0
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
			                                 WebChromeClient.FileChooserParams fileChooserParams) {
				if (mFilePathCallback != null) {
					mFilePathCallback.onReceiveValue(null);
				}
				mFilePathCallback = filePathCallback;
				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
					// Create the File where the photo should go
					File photoFile = null;
					try {
						photoFile = createImageFile();
						takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
					} catch (IOException ex) {
						// Error occurred while creating the File
						Log.e(TAG, "Unable to create Image File", ex);
					}
					// Continue only if the File was successfully created
					if (photoFile != null) {
						mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
						takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(photoFile));
					} else {
						takePictureIntent = null;
					}
				}
				Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
				contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
				contentSelectionIntent.setType("image/*");
				Intent[] intentArray;
				if (takePictureIntent != null) {
					intentArray = new Intent[]{takePictureIntent};
				} else {
					intentArray = new Intent[0];
				}
				Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
				chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
				chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
				startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
				return true;
			}

			// For Android > 3.x
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
				showAttachmentDialog(uploadMsg);
			}

			// For Android > 4.1
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
				showAttachmentDialog(uploadMsg);
			}
		});
		String url = "http://ijt.co.id/content/horoscope/index.php";
		String title = "";
		if (getIntent() != null) {
			url = getIntent().getStringExtra(WEBVIEW_URL);
			title = getIntent().getStringExtra(EXTRA_TITLE);
		}
		// setTitle(title);
		webView.loadUrl(url);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Android >= 5.0
		if (requestCode == INPUT_FILE_REQUEST_CODE && mFilePathCallback != null) {
			Uri[] results = null;
			// Check that the response is a good one
			if (resultCode == Activity.RESULT_OK) {
				if (data == null) {
					// If there is not data, then we may have taken a photo
					if (mCameraPhotoPath != null) {
						results = new Uri[]{Uri.parse(mCameraPhotoPath)};
					}
				} else {
					String dataString = data.getDataString();
					if (dataString != null) {
						results = new Uri[]{Uri.parse(dataString)};
					}
				}
			}
			mFilePathCallback.onReceiveValue(results);
			mFilePathCallback = null;
		}
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if (resultCode == RESULT_OK) {
				Log.d(TAG, "FILE CHOSEN IS " + data.getData());
				mUploadMessage.onReceiveValue(data.getData());
			}
		}
		if (requestCode == FILECHOOSER_RESULTCODE_KITKAT) {
			if (resultCode == RESULT_OK) {
				Log.d(TAG, "KITKAT FILE CHOSEN IS " + data.getData());
				// TODO do something??
			}
		}
	}

	/**
	 * More info this method can be found at
	 * http://developer.android.com/training/camera/photobasics.html
	 *
	 * @return
	 * @throws IOException
	 */
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES);
		File imageFile = File.createTempFile(
				imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir      /* directory */
		);
		return imageFile;
	}

	private void showAttachmentDialog(ValueCallback<Uri> uploadMsg) {
		this.mUploadMessage = uploadMsg;
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("image/*");
		int wew = Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT ? FILECHOOSER_RESULTCODE_KITKAT : FILECHOOSER_RESULTCODE;
		startActivityForResult(Intent.createChooser(i, "Choose type of attachment"), wew);
	}

	@Override
	public void onBackPressed() {
		if (webView.copyBackForwardList().getCurrentIndex() > 0) {
			webView.goBack();
		} else {
			// Your exit alert code, or alternatively line below to finish
			super.onBackPressed(); // finishes activity
		}
	}

	private class HoroscopeJavaInterface {
		private final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");

		public HoroscopeJavaInterface(Context context) {
		}

		@JavascriptInterface
		public void chooseImage() {
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("image/*");
			startActivityForResult(Intent.createChooser(i, "Choose type of attachment"), FILECHOOSER_RESULTCODE_KITKAT);
		}

		@JavascriptInterface
		public boolean isKitkat() {
			return Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;
		}

		/**
		 * upload jpg file using okhttp multipart post request.
		 * @param imagePath image path in phone.
		 * @param urlToUpload where to upload that image.
		 * @return response string.
		 */
		@JavascriptInterface
		public String uploadPic(String imagePath, String urlToUpload) {
			String ret = "";
			OkHttpClient okHttpClient = new OkHttpClient();
			RequestBody requestBody = new MultipartBuilder()
					.type(MultipartBuilder.FORM)
					.addPart(Headers.of("Content-Disposition", "form-data; name=\"image\""),
							RequestBody.create(MEDIA_TYPE_JPG, new File("imagePath")))
					.build();
			Request request = new Request.Builder()
					.url(urlToUpload)
					.post(requestBody)
					.build();
			try {
				Response response = okHttpClient.newCall(request).execute();
				if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
				ret = response.body().string();
			} catch (IOException e) {
				ret = e.getMessage();
			}
			return ret;
		}
	}
}
