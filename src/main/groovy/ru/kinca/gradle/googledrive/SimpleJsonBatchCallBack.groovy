package ru.kinca.gradle.googledrive

import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.http.HttpHeaders

/**
 * {@link com.google.api.client.googleapis.batch.BatchCallback} that
 * throws {@link RuntimeException} with specified message on error.
 *
 * @author Valentin Naumov
 */
class SimpleJsonBatchCallBack<T>
extends JsonBatchCallback<T>
{
    String failureMessage

    SimpleJsonBatchCallBack(
        String failureMessage)
    {
        this.failureMessage = failureMessage
    }

    @Override
    void onFailure(
        GoogleJsonError e,
        HttpHeaders responseHeaders)
    throws IOException
    {
        throw new RuntimeException("failureMessage. Response: $e")
    }

    @Override
    void onSuccess(
        T object,
        HttpHeaders responseHeaders)
    throws IOException
    {
        // Do nothing on success, just continue the program flow.
    }
}
