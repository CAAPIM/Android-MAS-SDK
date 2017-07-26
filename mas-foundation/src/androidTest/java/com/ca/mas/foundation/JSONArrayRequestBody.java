package com.ca.mas.foundation;

import com.ca.mas.core.http.ContentType;

import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;

public class JSONArrayRequestBody extends MASRequestBody {

    private byte[] content;

    public JSONArrayRequestBody(JSONArray jsonArray) {
        content = jsonArray.toString().getBytes(getContentType().getCharset());
    }

    @Override
    public ContentType getContentType() {
        return ContentType.APPLICATION_JSON;
    }

    @Override
    public long getContentLength() {
        return content.length;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(content);
    }
}
