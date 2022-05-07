package com.sdk.linkinglibrary;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Linking {

    private static final String KEY_CONFIG_NATIVE = "linking_native";
    private static final String KEY_CONFIG_ONBOARD = "linking_onboard";

    public interface IListener {
        void onSuccess(LinkedItemOnBoard item);
    }

    public static void inflateOnBoardItem(Activity context, int number, IListener listener) {

        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        config.fetchAndActivate().addOnCompleteListener(task -> {
            String configString = config.getString(KEY_CONFIG_ONBOARD);
            try {

                RandomCollection<LinkedItemOnBoard> mItems = new RandomCollection<>();

                JSONArray configJson = new JSONArray(configString);
                for (int i = 0; i < configJson.length(); i++) {
                    LinkedItemOnBoard item = LinkedItemOnBoard.fromJSON(configJson.getJSONObject(i));
                    if (item != null)
                        mItems.add(item.getWeight(), item);
                }

                if (mItems.size() == 0 || number <= 0)
                    return;

                int numOf = number;

                if (numOf > mItems.size())
                    numOf = mItems.size();

                List<LinkedItemOnBoard> choices = new ArrayList<>();

                while (numOf > 0) {
                    LinkedItemOnBoard item = mItems.next();
                    if (choices.contains(item))
                        continue;
                    choices.add(item);
                    numOf -= 1;
                }

                for (LinkedItemOnBoard item : choices) {
                    List<String> urls = new ArrayList<>();
                    for (Pair<String, String> feature : item.getFeatures()){
                        urls.add(feature.first);
                    }
                    urls.add(item.getImageUrl());
                    ImageDownloader.preloadDrawablesFromUrls(context, urls, new ImageDownloader.IOnImageLoaded() {
                        @Override
                        public void onLoaded() {
                            listener.onSuccess(item);
                        }
                    });
                }

            } catch (JSONException e) {
                /* ... */
            }
        });

    }

    public static void inflateNativeItem(Activity activity, int number, int layoutId, ViewGroup root, boolean clearRoot) {
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        config.fetchAndActivate().addOnCompleteListener(task -> {
            String configString = config.getString(KEY_CONFIG_NATIVE);
            try {

                RandomCollection<LinkedItemNative> mItems = new RandomCollection<>();

                JSONArray configJson = new JSONArray(configString);
                for (int i = 0; i < configJson.length(); i++) {
                    LinkedItemNative item = LinkedItemNative.fromJSON(configJson.getJSONObject(i));
                    if (item != null)
                        mItems.add(item.getWeight(), item);
                }

                if (mItems.size() == 0 || number <= 0)
                    return;

                int numOf = number;

                if (numOf > mItems.size())
                    numOf = mItems.size();

                List<LinkedItemNative> choices = new ArrayList<>();

                while (numOf > 0) {
                    LinkedItemNative item = mItems.next();
                    if (choices.contains(item))
                        continue;
                    choices.add(item);
                    inflate(activity, item, layoutId, root, clearRoot);
                    numOf -= 1;
                }

            } catch (JSONException e) {
                /* ... */
            }
        });
    }

    private static void inflate(Activity activity, LinkedItemNative item, int layoutId, ViewGroup root, boolean clearRoot) {

        if (item == null) {
            return;
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        Resources resources = activity.getResources();

        View view = inflater.inflate(layoutId, root, false);

        TextView title = view.findViewById(resources.getIdentifier("txt_link_title", "id", activity.getPackageName()));
        TextView descr = view.findViewById(resources.getIdentifier("txt_link_descr", "id", activity.getPackageName()));
        ImageView icon = view.findViewById(resources.getIdentifier("img_link_icon", "id", activity.getPackageName()));

        if (title != null)
            title.setText(item.getTitle());

        if (descr != null)
            descr.setText(item.getDescription());

        if (icon != null)
            ImageDownloader.drawableFromUrl(activity, icon, item.getIconUrl(), new ImageDownloader.IOnImageLoaded() {
                @Override
                public void onLoaded() {

                    if (clearRoot)
                        root.removeAllViews();

                    root.addView(view);
                    view.setOnClickListener(v -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id="+item.getAppPackage()));
                        intent.setPackage("com.android.vending");
                        if (intent.resolveActivity(activity.getPackageManager()) != null)
                            activity.startActivity(intent);
                    });
                }
            });
    }
}
