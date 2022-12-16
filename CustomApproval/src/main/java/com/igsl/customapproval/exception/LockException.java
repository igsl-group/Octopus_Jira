package com.igsl.customapproval.exception;

/**
 * Thrown when unable to lock issue for approval process.
 * This means another user is doing it, retry later.
 */
public class LockException extends Exception {

}
