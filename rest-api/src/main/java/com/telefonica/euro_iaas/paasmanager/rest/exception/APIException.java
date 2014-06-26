/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U <br>
 * This file is part of FI-WARE project.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 * </p>
 * <p>
 * You may obtain a copy of the License at:<br>
 * <br>
 * http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * <p>
 * See the License for the specific language governing permissions and limitations under the License.
 * </p>
 * <p>
 * For those usages not covered by the Apache version 2.0 License please contact with opensource@tid.es
 * </p>
 */

package com.telefonica.euro_iaas.paasmanager.rest.exception;

import javax.servlet.ServletException;

public class APIException extends ServletException {

    private String message;
    private String publicMessage;
    private Integer code;
    private Integer httpCode;
    private Throwable cause;

    public APIException(Throwable cause) {

        super(cause);
        this.cause = cause;

    }

    public APIException(Throwable cause, int error) {

        this.cause = cause;
        this.httpCode = error;

    }

    public Integer getCode() {
        return this.code;
    }

    public String getMessage() {
        if (message == null) {
            parseCause();
        }

        return this.message;
    }

    public void parseCause() {

        ErrorCode errorCode = ErrorCode.find(cause.toString());
        this.code = errorCode.getCode();
        this.publicMessage = errorCode.getPublicMessage();
        this.message = errorCode.toString() + "#" + cause.getMessage();
        this.httpCode = errorCode.getHttpCode();

    }

    public String getPublicMessage() {
        return publicMessage;
    }

    public Integer getHttpCode() {
        return httpCode;
    }
}
