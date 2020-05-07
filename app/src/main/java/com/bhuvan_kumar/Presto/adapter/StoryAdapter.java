package com.bhuvan_kumar.Presto.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.model.StoryModel;
import com.bhuvan_kumar.Presto.util.Constants;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;


public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private static final int MENU_ITEM_VIEW_TYPE = 0;
    private Context context;
    private ArrayList<Object> filesList;

    public StoryAdapter(Context context, ArrayList<Object> filesList) {
        this.context = context;
        this.filesList = filesList;
    }

    @Override
    public StoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.status_card_item,null,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StoryAdapter.ViewHolder holder, final int position) {
        final StoryModel files = (StoryModel) filesList.get(position);

        if(files.getUri().toString().endsWith(".mp4") || files.getUri().toString().endsWith(".3gp"))
        {
            holder.videoView.setVideoURI(files.getUri());
            holder.playButtonImage.setVisibility(View.VISIBLE);
            holder.playButtonImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.savedImage.setVisibility(View.GONE);
                    holder.playButtonImage.setVisibility(View.GONE);
                    holder.videoLayout.setVisibility(View.VISIBLE);
                    holder.videoView.start();
                }
            });
        }else{
            holder.playButtonImage.setVisibility(View.GONE);
        }

        loadImage(files.getUri(), holder.savedImage);

        holder.videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                holder.videoView.pause();
                holder.videoLayout.setVisibility(View.GONE);
                holder.savedImage.setVisibility(View.VISIBLE);
                holder.playButtonImage.setVisibility(View.VISIBLE);
                return false;
            }
        });

        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            public void onCompletion(MediaPlayer mp)
            {
                holder.videoLayout.setVisibility(View.GONE);
                holder.savedImage.setVisibility(View.VISIBLE);
                holder.playButtonImage.setVisibility(View.VISIBLE);
            }
        });

        holder.shareID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareFile(files.getFilename(), files.getPath());
            }
        });

        holder.downloadID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFolder();
                final String path = ((StoryModel) filesList.get(position)).getPath();
                final File file = new File(path);
                String destPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.SAVE_FOLDER_NAME;
                File destFile = new File(destPath);
                try {
                    FileUtils.copyFileToDirectory(file,destFile);
                    Snackbar snackbar = Snackbar.make(v, Html.fromHtml("<font color=\"#000000\">Successfully saved!!</font>"), Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.save_green));
                    snackbar.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar snackbar = Snackbar.make(v, Html.fromHtml("<font color=\"#000000\">Cannot be saved!</font>"), Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.colorSecondary));
                    snackbar.show();
                }
                MediaScannerConnection.scanFile(
                        context,
                        new String[]{ destPath + files.getFilename()},
                        new String[]{ "*/*"},
                        new MediaScannerConnection.MediaScannerConnectionClient()
                        {
                            public void onMediaScannerConnected()
                            {
                            }
                            public void onScanCompleted(String path, Uri uri)
                            {
                                Log.d("path: ",path);
                            }
                        });
            }
        });
    }

    private void loadImage(Uri uri, ImageView imageView){
        imageView.setVisibility(View.VISIBLE);
        imageView.layout(0,0,0,0);
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions()
                        .fitCenter())
                .transition(GenericTransitionOptions.with(R.anim.zoom_in))
                .into(imageView);
    }

    private void shareFile(String file_name, String file_path) {

        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

        intentShareFile.setType(URLConnection.guessContentTypeFromName(file_name));
        intentShareFile.putExtra(Intent.EXTRA_STREAM,
                Uri.parse(file_path));

        //if you need
        //intentShareFile.putExtra(Intent.EXTRA_SUBJECT,"Sharing File Subject);
        //intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File Description");

        context.startActivity(Intent.createChooser(intentShareFile, "Share File"));

    }

    public void checkFolder() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.SAVE_FOLDER_NAME ;
        File dir = new File(path);

        boolean isDirectoryCreated = dir.exists();
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir();
        }
        if (isDirectoryCreated) {
            Log.d("Folder", "Already Created");
        }
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return MENU_ITEM_VIEW_TYPE;
    }

//    @Override
//    public void onViewDetachedFromWindow(@NonNull ViewHolder viewHolder){
//        if(isPlaying[0]){
//            isPlaying[0] = false;
//            viewHolder.videoView.pause();
//            viewHolder.videoView.setVisibility(View.GONE);
//            viewHolder.savedImage.setVisibility(View.VISIBLE);
//            viewHolder.playButtonImage.setVisibility(View.VISIBLE);
//        }
//    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView savedImage, playButtonImage;
        LinearLayout downloadID, shareID;
        RelativeLayout videoLayout;
        VideoView videoView;
        public ViewHolder(View itemView) {
            super(itemView);
            this.setIsRecyclable(false);
            savedImage = itemView.findViewById(R.id.mainImageView);
            downloadID = itemView.findViewById(R.id.downloadID);
            shareID = itemView.findViewById(R.id.shareID);
            videoView = itemView.findViewById(R.id.mainVideoView);
            videoLayout = itemView.findViewById(R.id.video_layout);
            playButtonImage = itemView.findViewById(R.id.playButtonImage);
        }
    }
}
