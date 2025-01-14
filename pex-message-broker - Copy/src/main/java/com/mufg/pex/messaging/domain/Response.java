package com.mufg.pex.messaging.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Response {

    private final Date date = new Date();
    private Object body;
    private int code;
    private boolean noConvert;
    private final Map<String, String> parameters = new HashMap<>();

    @Getter
    private final Map<String, String> headers = new HashMap<>();

    public Response addParam(String key, String value){

        parameters.put(key, value);

        return this;
    }

    public Response addHeader(String key, String value){

        headers.put(key, value);

        return this;
    }

    public Response addAllHeaders(Map<String, String> data){

        headers.putAll(data);

        return this;
    }

    public Response addAllParams(Map<String, String> allParams){

        parameters.putAll(allParams);

        return this;
    }

    public Response code(int code){

        this.code = code;

        return this;
    }

    public Response body(Object body){

        this.body = body;

        return this;
    }

    public static Response builder(){

        return new Response();
    }

    public Response build(){

        return this;
    }

    public int getCode() {
        return code;
    }

    public Date getDate() {
        return date;
    }

    public Object getBody() {
        return body;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public boolean isNoConvert() {
        return noConvert;
    }

    public Response noConvert() {

        noConvert = true;
        return this;
    }
}
