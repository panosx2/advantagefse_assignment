package com.basement.panosx2.moviedatabase.Helpers;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestQueueSingleton {
public static final String TAG = "RequestQueueSingleton";
private static RequestQueueSingleton mInstance;
private RequestQueue mRequestQueue;
private static Context mContext;

private RequestQueueSingleton(Context context) {
    mContext = context;
    mRequestQueue = getRequestQueue();
}

public static synchronized RequestQueueSingleton getInstance(Context context) {
    if (mInstance == null) {
        mInstance = new RequestQueueSingleton(context);
    }
    return mInstance;
}

public RequestQueue getRequestQueue() {
    if (mRequestQueue == null) {
        mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
    }
    return mRequestQueue;
}

public <T> void addToRequestQueue(Request<T> req) {
    getRequestQueue().add(req);
}

/**
 * Cancels all pending requests by the specified TAG, it is important
 * to specify a TAG so that the pending/ongoing requests can be cancelled.
 */
public void cancelPendingRequests(Object tag) {
    if (mRequestQueue != null) {
        mRequestQueue.cancelAll(tag);

        Log.d(TAG, "Pending Requests From " +
                (tag.toString().contains("@")?
                        (tag.toString().substring(0, tag.toString().indexOf("@"))):
                        (tag.toString().substring(0, tag.toString().indexOf("{")))) +
                " Cancelled");
    }
}
}
