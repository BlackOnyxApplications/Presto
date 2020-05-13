package com.bhuvan_kumar.Presto.instagram;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class InstagramDownloader {
    private Context mContext;
    String TAG = getClass().getName();

    InstagramDownloader(Context context){
        this.mContext = context;
    }

    private Document page;
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36";

    public boolean downloadMedia(String url, String targetDirectory){
        //    https://www.instagram.com/p/Bt2ECtVg4jh/
        Helpers.validateURL(url);
        try{
            page = Jsoup.connect(url).userAgent(USER_AGENT).get();
            String mediaType = page.select("meta[name=medium]").first()
                    .attr("content");

            switch (mediaType) {
                case "video":
                    return downloadVideo(url, targetDirectory);
                case "image":
                    return downloadImage(url, targetDirectory);
                default:
                    System.out.println("Unable to download media file.");
                    return false;

            }

        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean downloadVideo(String url, String targetDirectory){
        String videoUrl = "";

        Helpers.validateURL(url);
        try {
            page = Jsoup.connect(url).userAgent(USER_AGENT).get();
            videoUrl = page.select("meta[property=og:video]").first()
                    .attr("content");

        } catch (IOException e){
            e.printStackTrace();
            return false;
        }

        return download(videoUrl, targetDirectory);
    }

    public boolean downloadImage(String url, String targetDirectory){
        String imageUrl = "";

        Helpers.validateURL(url);
        try {
            page = Jsoup.connect(url).userAgent(USER_AGENT).get();
            imageUrl = page.select("meta[property=og:image]").first()
                    .attr("content");

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return download(imageUrl, targetDirectory);
    }

    public String getDownloadUrl(String url){
        String downloadUrl = "";

        Helpers.validateURL(url);
        try {
            page = Jsoup.connect(url).userAgent(USER_AGENT).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String mediaType = page.select("meta[name=medium]").first()
                .attr("content");

        switch (mediaType) {
            case "video":
                downloadUrl = page.select("meta[property=og:video]").first()
                        .attr("content");
                break;
            case "image":
                downloadUrl = page.select("meta[property=og:image]").first()
                        .attr("content");
                break;
            default:
                downloadUrl = "No media file found.";
                break;
        }
        return downloadUrl;
    }

    private boolean download(String url, String targetDirectory){
        try {
            String[] tempName = url.split("/");
            String filename = tempName[tempName.length - 1].split("[?]")[0];

            DownloadManager downloadmanager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(filename);
            request.setDescription("Downloading");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setVisibleInDownloadsUi(false);
            String destPath = targetDirectory + File.separator + filename;
            request.setDestinationUri(Uri.fromFile(new File(destPath)));
            downloadmanager.enqueue(request);
            Log.e(TAG, "Download completed");
            return true;
        }catch (Exception e){
            return false;
        }
    }
}