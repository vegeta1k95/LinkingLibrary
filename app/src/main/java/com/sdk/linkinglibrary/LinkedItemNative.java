package com.sdk.linkinglibrary;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class LinkedItemNative {

    private static final String DEFAULT_LANG = "en";

    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_PACKAGE = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_ICON_URL = "icon";

    private Integer mWeight;

    private String mPkg;
    private String mTitle;
    private String mDescription;
    private String mIconUrl;

    public Integer getWeight() { return mWeight; }
    public String getAppPackage() { return mPkg; }
    public String getTitle() { return mTitle; }
    public String getDescription() { return mDescription; }
    public String getIconUrl() { return mIconUrl; }

    public static LinkedItemNative fromJSON(JSONObject object) {
        String langCode = Locale.getDefault().getLanguage();

        LinkedItemNative item = new LinkedItemNative();
        try {
            item.mWeight = object.getInt(KEY_WEIGHT);
            item.mPkg = object.getString(KEY_PACKAGE);
            item.mIconUrl = object.getString(KEY_ICON_URL);

            item.mTitle = object.getJSONObject(KEY_TITLE).optString(langCode);
            if (item.mTitle.isEmpty())
                item.mTitle = object.getJSONObject(KEY_TITLE).getString(DEFAULT_LANG);

            item.mDescription = object.getJSONObject(KEY_DESCRIPTION).optString(langCode);
            if (item.mDescription.isEmpty())
                item.mDescription = object.getJSONObject(KEY_DESCRIPTION).getString(DEFAULT_LANG);

            return item;

        } catch (JSONException e) {
            /* ... */
        }

        return null;
    }
}
