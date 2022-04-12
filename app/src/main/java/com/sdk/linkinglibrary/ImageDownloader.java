package com.sdk.linkinglibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ImageDownloader {

    public interface IOnImageLoaded {
        default void onLoaded() {}
        default void onFailed() {}
    }

    public static void drawableFromUrl(Context context, ImageView image, String url,
                                       @Nullable IOnImageLoaded listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        Glide.with(context)
                .load(url.startsWith("gs://") ? storage.getReferenceFromUrl(url) : url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (listener != null)
                            listener.onFailed();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (listener != null)
                            listener.onLoaded();
                        return false;
                    }
                })
                .into(image);
    }

    public static void preloadDrawablesFromUrls(Context context, List<String> urls,
                                                @Nullable IOnImageLoaded listener) {
        if (urls.size() == 0) {
            if (listener != null)
                listener.onFailed();
            return;
        }

        final int[] states = {urls.size(), 0};

        FirebaseStorage storage = FirebaseStorage.getInstance();

        for (int i = 0; i< states[0]; i++)  {
            String url = urls.get(i);
            Glide.with(context)
                    .load(url.startsWith("gs://") ? storage.getReferenceFromUrl(url) : url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                            if (listener == null)
                                return false;

                            states[0] -= 1;
                            states[1] = 1;
                            if (states[0] == 0)
                                listener.onFailed();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (listener == null)
                                return false;

                            states[0] -= 1;

                            if (states[0] == 0) {
                                if (states[1] == 0)
                                    listener.onLoaded();
                                else
                                    listener.onFailed();
                            }

                            return false;
                        }
                    })
                    .preload();

        }

    }

    public static void loadDrawablesFromCache(Context context, List<ImageView> images,
                                              List<String> urls) {
        if (images.size() != urls.size() || images.size() == 0) {
            return;
        }

        final int[] states = {images.size(), 0};

        FirebaseStorage instance = FirebaseStorage.getInstance();

        for (int i = 0; i< states[0]; i++)  {
            StorageReference reference = instance.getReferenceFromUrl(urls.get(i));
            Glide.with(context)
                    .load(reference)
                    .onlyRetrieveFromCache(true)
                    .into(images.get(i));

        }

    }

}
