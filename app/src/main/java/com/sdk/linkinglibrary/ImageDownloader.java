package com.sdk.linkinglibrary;

import android.app.Activity;
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

    public static void drawableFromUrl(Activity context, ImageView image, String url,
                                       @Nullable IOnImageLoaded listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (context == null || context.isDestroyed()
                || url == null || url.isEmpty() || !url.startsWith("gs://")) {
            if (listener != null)
                listener.onFailed();
            return;
        }

        StorageReference reference;

        try {
            reference = storage.getReferenceFromUrl(url);
        } catch (IllegalArgumentException e) {
            if (listener != null)
                listener.onFailed();
            return;
        }

        Glide.with(context)
                .load(reference)
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

    public static void preloadDrawablesFromUrls(Activity context, List<String> urls,
                                                @Nullable IOnImageLoaded listener) {
        if (context == null || context.isDestroyed() || urls.size() == 0) {
            if (listener != null)
                listener.onFailed();
            return;
        }

        final int[] states = {urls.size(), 0};

        FirebaseStorage storage = FirebaseStorage.getInstance();

        for (int i = 0; i< states[0]; i++)  {

            String url = urls.get(i);

            if (url == null || url.isEmpty() || !url.startsWith("gs://")) {
                if (listener != null) {
                    states[0] -= 1;
                    states[1] = 1;
                    if (states[0] == 0)
                        listener.onFailed();
                }
                continue;
            }

            StorageReference reference;

            try {
                reference = storage.getReferenceFromUrl(url);
            } catch (IllegalArgumentException e) {
                if (listener != null) {
                    states[0] -= 1;
                    states[1] = 1;
                    if (states[0] == 0)
                        listener.onFailed();
                }
                continue;
            }

            Glide.with(context)
                    .load(reference)
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

    public static void loadDrawablesFromCache(Activity activity, List<ImageView> images,
                                              List<String> urls) {
        if (activity == null || activity.isDestroyed() ||
                images.size() != urls.size() || images.size() == 0) {
            return;
        }

        final int[] states = {images.size(), 0};

        FirebaseStorage instance = FirebaseStorage.getInstance();

        for (int i = 0; i< states[0]; i++)  {

            String url = urls.get(i);

            if (url == null || url.isEmpty() || !url.startsWith("gs://"))
                continue;

            try {
                StorageReference reference = instance.getReferenceFromUrl(url);
                Glide.with(activity)
                        .load(reference)
                        .onlyRetrieveFromCache(true)
                        .into(images.get(i));
            } catch (IllegalArgumentException ignored) { /* ... */ }
        }

    }

}
