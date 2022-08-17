package com.example.springbootflowabledemo.result;

import java.io.Serializable;

public class BaseResponse<T> implements Serializable {

    private boolean success = false;
    private T result;
    private Error error = null;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
    public static class Error {
        private String code = "";
        private String message = "";

        public Error(String code, String msg){
            this.code = code;
            this.message = msg;
        }


        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "Error [code=" + code + ", message=" + message + "]";
        }

    }



    public BaseResponse(boolean isSuccess, T result, Error error){
        this.success = isSuccess;
        this.result = result;
        this.error = error;
    }

    public static BaseResponse success(Object result){
        return new BaseResponse(true, result, null);
    }

    public static BaseResponse fail(String code, String msg){
        return new BaseResponse(false, null, new Error(code, msg));
    }



    @Override
    public String toString() {
        return "BaseResponse [success=" + success + ", result=" + result + ", error=" + error + "]";
    }

}
