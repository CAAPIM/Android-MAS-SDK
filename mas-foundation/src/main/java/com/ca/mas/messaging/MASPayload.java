/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.messaging;

import android.content.Context;

/**
 * <p><b>MASPayload</b> describes the message format that is passed between MAS clients. The payload is a simple
 * JSON document consisting of 8 key/value pairs;</p>
 * <ul>
 * <li>Version - currently 1.0.</li>
 * <li>SenderID - a unique identifier typically registered with the corporate system.</li>
 * <li>SenderType - any defined sender such as 'User', 'Device', 'Application', etc. Whatever has meaning in the application's context.</li>
 * <li>DisplayName - a free-form value that contains the user friendly name for display.</li>
 * <li>SentTime - the number of milliseconds since the epoch.</li>
 * <li>ContentType - the <a href="http://www.iana.org/assignments/media-types/media-types.xhtml">IANA MIME</a> media type.</li>
 * <li>ContentEncoding - the payload encoding such as base64 or UTF-8.</li>
 * <li>Payload - the opaque payload that is described by the content type and content encoding. This could be any set of bytes
 * such as a text message, an image, etc. and is Base64 encoded</li>
 * </ul>
 * <p>This payload is opaque to the message broker and the format is MAS SDK specific. There is no expectation within the message broker of
 * format or content. It is simply a byte stream.</p>
 * <br>
 * <p>From the design document the payload should be of the form:
 *  <pre>
 *      {
 *          "Version":"1.0",
 *          "SenderId":"user id",
 *          "SenderType":"USER",
 *          "DisplayName":"User Name",
 *          "SentTime":1438386369,
 *          "ProcessedTime":1446600366,
 *          "ForwardedTime":1446600373,
 *          "ContentType":"text/plain",
 *          "ContentEncoding":"base64",
 *          "Payload":"QWxsIHlvdXIgYXBpJ3MgYXJlIGJlbG9uZ3MgdG8gdXMh"
 *      }
 *  </pre></p>
 */
public interface MASPayload {
    /**
     * <b>Description:</b> The version will always be 1.0.
     *
     * @param version the value '1.0'
     */
    /* public */ void setVersion(String version);

    /**
     * <b>Description:</b> The version will always be 1.0.
     *
     * @return String the value '1.0'
     */
    /* public */ String getVersion();

    /**
     * <b>Description:</b> The string value set by the application developer.
     *
     * @return String the free-form display name. Can be null.
     */
    String getDisplayName();

    /**
     * <b>Description:</b> The Id of the sender from the MASUser instance.
     *
     * @param senderId the identifier so that the recipient will know who it is from.
     */
    /* public */ void setSenderId(String senderId);

    /**
     * <b>Description:</b> The Id of the sender from the MASUser instance.
     *
     * @return String the identifier so that the recipient will know who it is from.
     */
    /* public */ String getSenderId();

    /**
     * <b>Description:</b> Set the sender type that has context within the implementing application.
     *
     * @param senderType one of USER, DEVICE, APPLICATION, etc.
     */
    /* public */ void setSenderType(String senderType);

    /**
     * <b>Description:</b> Get the sender type that has context within the implementing application.
     *
     * @return String one of USER, DEVICE, APPLICATION, etc. It could be null.
     */
    /* public */ String getSenderType();

    /**
     * <b>Description:</b> The number of milliseconds since the Epoch returned by an OS call to getTime, for example.
     *
     * @param sentTime the time the message was sent by the client in milliseconds
     */
    /* public */ void setSentTime(long sentTime);

    /**
     * <b>Description:</b> The number of milliseconds since the Epoch returned by an OS call to getTime, for example.
     *
     * @return long  the time that the message was sent by the MAS SDK call to {@link com.ca.mas.connecta.client.MASConnectaClient#publish}
     */
    /* public */ long getSentTime();

    /**
     * <b>Description:</b> Set the {<a href="http://www.iana.org/assignments/media-types/media-types.xhtml">IANA MIME</a>} media type.
     *
     * @param contentType the string representing the MIME type, such as 'text/plain'.
     */
    /* public */ void setContentType(String contentType);

    /**
     * <b>Description:</b> Get the {<a href="http://www.iana.org/assignments/media-types/media-types.xhtml">IANA MIME</a>} media type.
     *
     * @return String the string representing the MIME type. Could be null.
     */
    /* public */ String getContentType();

    /**
     * <b>Description:</b> The payload's content encoding. This is set for the receiving client and has no meaning to the message broker.
     *
     * @param encoding a string representation of a the content encoding such as 'base64'. The default is utf-8.
     */
    /* public */ void setContentEncoding(String encoding);

    /**
     * <b>Description:</b> The payload's content encoding. The client will use this value to decode the payload byte stream.
     *
     * @return String a representation of a the content encoding such as 'base64'. The default is utf-8.
     */
    /* public */ String getContentEncoding();

    /**
     * <b>Description:</b> This payload is a byte stream that is meaningful to the receiving client. The message broker passes
     * this information through along with the rest of the MASMessage. The maximum size of the payload is system dependent and is
     * not limited by the MAS SDK. If there are enough resource to handle this message then it considered to be a valid size.
     *
     * @param payload - the data part of the MASMessage
     */
    /* public */ void setPayload(byte[] payload);

    /**
     * <b>Description:</b> This payload is a byte stream that is meaningful to the receiving client. The message broker passes
     * this information through along with the rest of the MASMessage. The client will use the content type and encoding type
     * to interpret the received byte stream.
     *
     * @return byte[] the arbitrary bytes. Should not be null or there is no payload to transmit.
     */
    /* public */ byte[] getPayload();

    /**
     * <b>Description:</b> This method takes the entire MASPayload attributes and create a Base64 encoded
     * version of the JSON document representing the payload. For example, this interface results in the JSON
     * document;
     * <pre>
     *     {
     *      "Version":"1.0",
     *      "SenderId":"me",
     *      "SenderType":"USER",
     *      "SentTime":1457383162000,
     *      "ContentType":"text/plain",
     *      "ContentEncoding":"utf8",
     *      "Payload":"U29tZSBwYXlsb2FkIG1lc3NhZ2U="
     *      }
     * </pre>
     * <br>
     * Note that the inner payload is base64 encoded.
     *
     * @return String representing the base64 encoded JSON document.
     */
    /* public */ String createJSONStringFromMASMessage(Context context) throws MASMessageException;

    /**
     * <b>Description:</b> This method takes a JSON object that has been turned into a String and attempts to turn it into a
     * MASPayload object. This is a re-serialization of the transmitted MASPayload.
     *
     * @param jsonStr the JSON object, as a String, representing the MASPayload.
     * @throws MASMessageException if the String cannot be converted or is not valid JSON.
     */
    /* public */ void createMASMessageFromJSONString(String jsonStr) throws MASMessageException;
}
