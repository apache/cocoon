package org.apache.cocoon.processor.newsql;

public class SQLProcessorException extends Exception {

	protected Exception inner_exception;

	public SQLProcessorException(String message) {
		super(message);
	}

	public SQLProcessorException(String message, Exception e) {
		super(message);
		inner_exception = e;
	}

}

