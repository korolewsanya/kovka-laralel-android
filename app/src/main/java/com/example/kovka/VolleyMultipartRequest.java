package com.example.kovka;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mHeaders = new HashMap<>();
    private final Map<String, String> mStringParams = new HashMap<>();
    private final Map<String, DataPart> mByteParams = new HashMap<>();
    private final String boundary = "---------------------------" + System.currentTimeMillis();

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>(mHeaders);
        headers.put("Accept", "application/json");
        return headers;
    }

    public void addHeader(String key, String value) {
        mHeaders.put(key, value);
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Строковые параметры
            for (Map.Entry<String, String> entry : mStringParams.entrySet()) {
                buildTextPart(bos, entry.getKey(), entry.getValue());
            }

            // Файловые параметры
            for (Map.Entry<String, DataPart> entry : mByteParams.entrySet()) {
                buildFilePart(bos, entry.getKey(), entry.getValue());
            }

            // Закрывающий boundary
            bos.write(("--" + boundary + "--\r\n").getBytes());

        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
            return null;
        }
        return bos.toByteArray();
    }

    private void buildTextPart(ByteArrayOutputStream bos, String key, String value) throws IOException {
        bos.write(("--" + boundary + "\r\n").getBytes());
        bos.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n").getBytes());
        bos.write("Content-Type: text/plain; charset=UTF-8\r\n".getBytes());
        bos.write("\r\n".getBytes());
        bos.write(value.getBytes("UTF-8"));
        bos.write("\r\n".getBytes());
    }

    private void buildFilePart(ByteArrayOutputStream bos, String key, DataPart dataPart) throws IOException {
        bos.write(("--" + boundary + "\r\n").getBytes());
        bos.write(("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + dataPart.getFileName() + "\"\r\n").getBytes());
        bos.write(("Content-Type: " + dataPart.getMimeType() + "\r\n").getBytes());
        bos.write("\r\n".getBytes());
        bos.write(dataPart.getData());
        bos.write("\r\n".getBytes());
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, null);
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        if (mErrorListener != null) {
            mErrorListener.onErrorResponse(error);
        }
    }

    public void addStringParam(String key, String value) {
        mStringParams.put(key, value);
    }

    public void addByteParam(String key, DataPart dataPart) {
        mByteParams.put(key, dataPart);
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] data;
        private final String mimeType;

        public DataPart(String fileName, byte[] data) {
            this(fileName, data, "image/jpeg");
        }

        public DataPart(String fileName, byte[] data, String mimeType) {
            this.fileName = fileName;
            this.data = data;
            this.mimeType = mimeType;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getData() {
            return data;
        }

        public String getMimeType() {
            return mimeType;
        }
    }
}