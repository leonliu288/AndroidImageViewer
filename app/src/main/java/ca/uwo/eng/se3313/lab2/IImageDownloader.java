package ca.uwo.eng.se3313.lab2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Downloader class that downloads data from the internet
 * Converts the stream into a Bitmap image which is then returned to the UI thread
 */
class AsynchronousDownloader extends AsyncTask<String, Void, Bitmap> {
    /**
     * String field within the class that corresponds to the
     */
    private String imageUrl;


    /**
     * Constructor for the AsynchronousDownloader class
     * @param imageUrl , String parameter that has the value of the URL from which the image needs to be downloaded
     */
    AsynchronousDownloader(String imageUrl){
        this.imageUrl = imageUrl;
    }

    /**
     * Main execution function in AsyncTask
     *
     * @param params , Any number of parameters
     * @return , Returns the Bitmap image associated with the URL passed in.
     */
    @Override
    protected Bitmap doInBackground(String... params) {
        return downloadImage(imageUrl);
    }

    /**
     * Downloads image by making an HTTP connection and decodes the bit stream into forming a Bitmap image
     *
     * @param imageUrl, String parameter that has the value of the URL from which the image needs to be downloaded
     *
     * @return Bitmap image object associated with the url from which the download takes place, Returns null if connection fails
     */
    private Bitmap downloadImage(String imageUrl) {
        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(imageUrl);
            urlConnection = (HttpURLConnection) uri.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            assert urlConnection != null;
            urlConnection.disconnect();
            Log.w("ImageDownloader", "Error downloading image from " + imageUrl);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

}