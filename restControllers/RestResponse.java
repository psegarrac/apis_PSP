package com.paracasa.spring.app.restControllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class RestResponse {
    private String messsage;
    private HttpStatus status;
    private ResponseStatus errorMessage;

    public RestResponse(){}
    public RestResponse(ResponseStatus errorMessage) {
        this.errorMessage = errorMessage;
    }
    public RestResponse(String message, HttpStatus status) {
        this.messsage = message;
        this.status = status;
    }

    public String getMesssage() {
        return messsage;
    }

    public void setMesssage(String messsage) {
        this.messsage = messsage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public ResponseStatus getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(ResponseStatus errorMessage) {
        this.errorMessage = errorMessage;
    }

    RestResponse isOk(String message, HttpStatus status){
        return new RestResponse(message, status);
    }

    RestResponse isError(ResponseStatus errorMessage){
        return new RestResponse(errorMessage);
    }
}
