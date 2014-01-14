/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.exception;

/**
 * Service层的任何异常都包装成这个Runtime异常抛出
 * 
 * @author boyan
 * @date 2010-5-5
 */
public class ConfigServiceException extends RuntimeException {
    static final long serialVersionUID = -1L;


    public ConfigServiceException() {
        super();

    }


    public ConfigServiceException(String message, Throwable cause) {
        super(message, cause);

    }


    public ConfigServiceException(String message) {
        super(message);

    }


    public ConfigServiceException(Throwable cause) {
        super(cause);

    }

}
