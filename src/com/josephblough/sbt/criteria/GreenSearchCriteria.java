package com.josephblough.sbt.criteria;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class GreenSearchCriteria implements Parcelable {

    private final static String TAG = "GreenSearchCriteria";
    
    private final static String SEARCHES_JSON_ARRAY = "searches";
    private final static String NAME_JSON_ELEMENT = "name";
    private final static String DOWLOAD_ALL_JSON_ELEMENT = "download_all";
    private final static String SEARCH_TERM_JSON_ELEMENT = "search_term";
    private final static String AGENCY_JSON_ELEMENT = "agency";
    private final static String TYPE_JSON_ELEMENT = "type";
    
    public boolean downloadAll;
    public String searchTerm;
    public String agency;
    public String type;
    
    
    public GreenSearchCriteria(boolean downloadAll, String searchTerm, String agency, String type) {
	this.downloadAll = downloadAll;
	this.searchTerm = (searchTerm == null) ? null : searchTerm.trim();
	this.agency = (agency == null) ? null : agency.trim();
	this.type = (type == null) ? null : type.trim();
    }
    
    public int describeContents() {
	return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
	dest.writeInt(downloadAll ? 1 : 0);
	dest.writeString(searchTerm == null ? "" : searchTerm);
	dest.writeString(agency == null ? "" : agency);
	dest.writeString(type == null ? "" : type);
    }

    public static final Parcelable.Creator<GreenSearchCriteria> CREATOR = new Parcelable.Creator<GreenSearchCriteria>() {
	public GreenSearchCriteria createFromParcel(Parcel in) {
	    return new GreenSearchCriteria(in);
	}
	public GreenSearchCriteria[] newArray(int size) {
            return new GreenSearchCriteria[size];
        }
    };
    
    private GreenSearchCriteria(Parcel in) {
	downloadAll = in.readInt() == 1;
	searchTerm = in.readString();
	agency = in.readString();
	type = in.readString();
    }
    
    public static Map<String, GreenSearchCriteria> convertFromJson(final String jsonString) {
	Map<String, GreenSearchCriteria> searches = new HashMap<String, GreenSearchCriteria>();
	
	if (jsonString != null && !"".equals(jsonString)) {
	    try {
		JSONObject json = new JSONObject(jsonString);
		JSONArray jsonSearches = json.optJSONArray(SEARCHES_JSON_ARRAY);
		if (jsonSearches != null) {
		    int length = jsonSearches.length();
		    for (int i=0; i<length; i++) {
			JSONObject jsonSearch = jsonSearches.getJSONObject(i);
			String name = jsonSearch.getString(NAME_JSON_ELEMENT);
			GreenSearchCriteria search = new GreenSearchCriteria(jsonSearch);
			searches.put(name, search);
		    }
		}
	    }
	    catch (JSONException e) {
		Log.e(TAG, e.getMessage(), e);
	    }
	}

	return searches;
    }
    
    public static String convertToJson(final Map<String, GreenSearchCriteria> criteria) {
	JSONObject json = new JSONObject();
	try {
	    JSONArray jsonSearches = new JSONArray();
	    for (Entry<String, GreenSearchCriteria> entry : criteria.entrySet()) {
		GreenSearchCriteria search = entry.getValue();
		JSONObject jsonSearch = search.toJson();
		jsonSearch.put(NAME_JSON_ELEMENT, entry.getKey());

		jsonSearches.put(jsonSearch);
	    }
	    json.put(SEARCHES_JSON_ARRAY, jsonSearches);
	}
	catch (JSONException e) {
	    Log.e(TAG, e.getMessage(), e);
	}
	return json.toString();
    }
    
    public GreenSearchCriteria(final String jsonString) throws JSONException {
	this(new JSONObject(jsonString));
    }
    
    public GreenSearchCriteria(final JSONObject json) {
	try {
	    downloadAll = json.getBoolean(DOWLOAD_ALL_JSON_ELEMENT);
	    searchTerm = json.getString(SEARCH_TERM_JSON_ELEMENT);
	    agency = json.getString(AGENCY_JSON_ELEMENT);
	    type = json.getString(TYPE_JSON_ELEMENT);
	}
	catch (JSONException e) {
	    Log.e(TAG, e.getMessage(), e);
	}
    }
    
    public JSONObject toJson() {
	JSONObject json = new JSONObject();
	try {
	    json.put(DOWLOAD_ALL_JSON_ELEMENT, downloadAll);
	    json.put(SEARCH_TERM_JSON_ELEMENT, searchTerm);
	    json.put(AGENCY_JSON_ELEMENT, agency);
	    json.put(TYPE_JSON_ELEMENT, type);
	}
	catch (JSONException e) {
	    Log.e(TAG, e.getMessage(), e);
	}
	return json;
    }
}
