package com.sdk.linkinglibrary;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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

    public static void inflatePopup(Activity mContext) {
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

                if (mItems.size() == 0)
                    return;

                LinkedItemOnBoard item = mItems.next();

                List<String> urls = new ArrayList<>();
                for (Pair<String, String> feature : item.getFeatures()){
                    urls.add(feature.first);
                }
                urls.add(item.getImageUrl());
                ImageDownloader.preloadDrawablesFromUrls(mContext, urls, new ImageDownloader.IOnImageLoaded() {
                        @Override
                        public void onLoaded() {

                            Bundle args = item.toBundle();

                            if (args == null)
                                return;

                            String pkg = args.getString("id");
                            String title = args.getString("title");
                            String imgUrl = args.getString("image");
                            ArrayList<String> featuresIconUrls = args.getStringArrayList("features_icon");
                            ArrayList<String> featuresText = args.getStringArrayList("features_text");

                            if (pkg  == null
                                    || title == null
                                    || imgUrl == null
                                    || featuresIconUrls == null
                                    || featuresText == null)
                                return;

                            Dialog dialog = new Dialog(mContext);
                            dialog.setContentView(R.layout.dialog_layout_linking);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                            TextView tvTitle = dialog.findViewById(R.id.txt_title);
                            tvTitle.setText(title);

                            View btnDownload = dialog.findViewById(R.id.btn_link);
                            btnDownload.setOnClickListener(v -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + pkg));
                                intent.setPackage("com.android.vending");
                                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                                    mContext.startActivity(intent);
                                }
                            });

                            List<ImageView> images = new ArrayList<>();

                            ViewGroup featuresContainer = dialog.findViewById(R.id.features_container);

                            for (String featureText : featuresText) {
                                View row = mContext.getLayoutInflater().inflate(R.layout.dialog_onboard_feature, featuresContainer, false);
                                TextView tvName = row.findViewById(R.id.txt_feature_name);
                                ImageView ivIcon = row.findViewById(R.id.img_feature_icon);

                                tvName.setText(featureText);
                                images.add(ivIcon);
                                featuresContainer.addView(row);
                            }
                            images.add(dialog.findViewById(R.id.img_big));

                            featuresIconUrls.add(imgUrl);
                            ImageDownloader.loadDrawablesFromCache(mContext, images, featuresIconUrls);

                            dialog.show();

                        }
                });


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
