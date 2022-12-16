package com.igsl.customapproval.exception;

/**
 * Thrown when approval transitions are invalid.
 * This means CustomApproval is configured incorrectly for the issue, review the post function config.
 */
public class InvalidWorkflowException extends Exception {
	public InvalidWorkflowException(String msg) {
		super(msg);
	}
}
