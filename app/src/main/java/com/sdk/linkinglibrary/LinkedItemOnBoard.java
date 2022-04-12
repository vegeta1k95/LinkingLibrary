package com.sdk.linkinglibrary;

import android.os.Bundle;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LinkedItemOnBoard {

    private static final String DEFAULT_LANG = "en";

    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_PACKAGE = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_IMAGE_URL = "image";
    private static final String KEY_FEATURES = "features";
    private static final String KEY_FEATURE_ICON_URL = "icon";
    private static final String KEY_FEATURE_TEXT = "text";
    private static final String KEY_BUTTON_TEXT = "button";

    private Integer mWeight;
    private String mPkg;
    private String mTitle;
    private String mImageUrl;
    private String mButtonText;
    private final List<Pair<String, String>> mFeatures = new ArrayList<>();

    public Integer getWeight() { return mWeight; }
    public String getImageUrl() { return mImageUrl; }
    public List<Pair<String, String>> getFeatures() { return mFeatures; }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_PACKAGE, mPkg);
        bundle.putString(KEY_TITLE, mTitle);
        bundle.putString(KEY_IMAGE_URL, mImageUrl);
        bundle.putString(KEY_BUTTON_TEXT, mButtonText);

        ArrayList<String> featuresUrls = new ArrayList<>();
        ArrayList<String> featuresText = new ArrayList<>();

        for (Pair<String, String> feature : mFeatures) {
            featuresUrls.add(feature.first);
            featuresText.add(feature.second);
        }
        bundle.putStringArrayList(KEY_FEATURES+"_"+KEY_FEATURE_ICON_URL, featuresUrls);
        bundle.putStringArrayList(KEY_FEATURES+"_"+KEY_FEATURE_TEXT, featuresText);
        return bundle;
    }

    public static LinkedItemOnBoard fromJSON(JSONObject object) {
        String langCode = Locale.getDefault().getLanguage();

        LinkedItemOnBoard item = new LinkedItemOnBoard();
        try {
            item.mWeight = object.getInt(KEY_WEIGHT);
            item.mPkg = object.getString(KEY_PACKAGE);
            item.mImageUrl = object.getString(KEY_IMAGE_URL);

            item.mTitle = object.getJSONObject(KEY_TITLE).optString(langCode);
            if (item.mTitle.isEmpty())
                item.mTitle = object.getJSONObject(KEY_TITLE).getString(DEFAULT_LANG);

            item.mButtonText = object.getJSONObject(KEY_BUTTON_TEXT).optString(langCode);
            if (item.mButtonText.isEmpty())
                item.mButtonText = object.getJSONObject(KEY_BUTTON_TEXT).getString(DEFAULT_LANG);

            JSONArray features = object.getJSONArray(KEY_FEATURES);

            for (int i=0; i<features.length(); i++) {

                JSONObject feature = features.getJSONObject(i);

                String featureIconUrl = feature.getString(KEY_FEATURE_ICON_URL);
                String featureText = feature.getJSONObject(KEY_FEATURE_TEXT).optString(langCode);
                if (featureText.isEmpty())
                    featureText = feature.getJSONObject(KEY_FEATURE_TEXT).getString(DEFAULT_LANG);

                item.mFeatures.add(new Pair<>(featureIconUrl, featureText));

            }

            return item;

        } catch (JSONException e) {
            /* ... */
        }

        return null;
    }

}
