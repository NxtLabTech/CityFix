package com.cityfix.exception;

public class DuplicateUpvoteException extends RuntimeException {

    public DuplicateUpvoteException(String voterEmail) {
        super(voterEmail + " has already upvoted this report");
    }
}
