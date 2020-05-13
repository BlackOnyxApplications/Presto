package com.bhuvan_kumar.Presto.instagram;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.model.StoryModel;
import com.bhuvan_kumar.Presto.util.Constants;
import com.bhuvan_kumar.Presto.util.FileUtils;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;

import static android.provider.SettingsSlicesContract.AUTHORITY;


public class InstaStoryAdapter extends RecyclerView.Adapter<InstaStoryAdapter.ViewHolder> {

    private static final int MENU_ITEM_VIEW_TYPE = 0;
    private Context context;
    private ArrayList<Object> filesList;

    public InstaStoryAdapter(Context context, ArrayList<Object> filesList) {
        this.context = context;
        this.filesList = filesList;
    }

    @Override
    public InstaStoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.insta_single_item,null,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InstaStoryAdapter.ViewHolder holder, final int position) {
        final StoryModel files = (StoryModel) filesList.get(position);

        loadImage(files.getUri(), holder.imageView);
        holder.textView.setText(files.getFilename());
        holder.sizeView.setText(formatFileSize(files.getFileSize()));
        holder.fileTypeView.setText(getFileType(files.getFilename()));
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareFile(files.getFilename(), files.getPath());
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInGallery(files.getPath());
            }
        });
    }

    private String formatFileSize(double fileSize){
        //1048576 = 1024 * 1024
        double sizeInMB = (double) Math.round((fileSize/1048576) * 100) / 100;
        if (sizeInMB > 1){
            return sizeInMB + " MB";
        }
        return ((double) Math.round((fileSize/1024) * 100) / 100) + " KB";
    }

    private String getFileType(String fileName){
        return FilenameUtils.getExtension(fileName).toUpperCase();
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

    private void showInGallery(String file_path){
//        FileUtils.openUri(context, Uri.parse(file_path));
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return MENU_ITEM_VIEW_TYPE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, shareButton;
        TextView textView, sizeView, fileTypeView;
        CardView cardView;
        public ViewHolder(View itemView) {
            super(itemView);
            this.setIsRecyclable(false);
            cardView = itemView.findViewById(R.id.itemCardView);
            imageView = itemView.findViewById(R.id.imageView);
            shareButton = itemView.findViewById(R.id.shareButton);
            textView = itemView.findViewById(R.id.textView);
            sizeView = itemView.findViewById(R.id.sizeView);
            fileTypeView = itemView.findViewById(R.id.fileTypeView);
        }
    }
}

