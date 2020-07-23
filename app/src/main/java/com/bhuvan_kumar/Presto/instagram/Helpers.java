package com.bhuvan_kumar.Presto.instagram;

public class Helpers {
    public static void validateURL(String url){
        if (url.startsWith("www.") || url.startsWith("instagram.com")){
            throw new IllegalArgumentException("URL must start with https://");
        }

        if (!(url.startsWith("https://") || url.startsWith("http://"))){
            throw new IllegalArgumentException("Invalid instagram URL");
        }

        if (!url.contains("instagram.com/p/") && !url.contains("instagram.com/tv/") && !url.contains("instagram.com/reel/")){
            throw new IllegalArgumentException("Invalid instagram URL");
        }

        if (url.length() <=0){
            throw new IllegalArgumentException("URL parameter cannot be empty");
        }
    }

    public static boolean isInstagramUrlValid(String url){
//        https://www.instagram.com/reel/CCsF0_QBXCX/?igshid=xgdyx96tyfsa
        if (url.length() <=0){
            return false;
        }
        if (url.startsWith("www.") || url.startsWith("instagram.com")){
            return false;
        }
        if (!(url.startsWith("https://") || url.startsWith("http://"))){
           return false;
        }
        return url.contains("instagram.com/p/") || url.contains("instagram.com/tv/") || url.contains("instagram.com/reel/");
    }

    public static String mediaType(String fileName){
        if (fileName.endsWith(".mp4")) {
            return "video";
        }
        else if(fileName.endsWith(".png") || fileName.endsWith(".jpg")) {
            return "image";
        }
        else {
            return "media type not found";
        }
    }
}
