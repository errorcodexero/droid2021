package org.xero1425.base.actions ;

/// \file

/// \brief this class is an exception that is thrown when an invalid request is made of an action.
public class InvalidActionRequest extends Exception
{
    private Action act_ ;
    private Reason reason_ ;

    static final long serialVersionUID = 42 ;

    public enum Reason
    {
        ModifyingRunningAction
    } ;
    
    /// \brief create the InvalidActionRequest object.
    /// \param act the action that caused the exception
    /// \param msg a string describing the error that occurred
    public InvalidActionRequest(Action act, Reason r, String msg) {
        super(act.toString() + "- " + msg) ;
        act_ = act ;
    }

    public Action getAction() {
        return act_ ;
    }

    public Reason getReason() {
        return reason_ ;
    }
}