package org.xero1425.base.controllers;

import org.xero1425.base.actions.SequenceAction;

import edu.wpi.first.wpilibj.geometry.Pose2d;

public class AutoMode extends SequenceAction {
    public AutoMode(AutoController ctrl, String name, Pose2d start) {
        super(ctrl.getRobot().getMessageLogger()); 

        ctrl_ = ctrl ;
        name_ = name ;
        start_ = start ;

        ctrl.addAutoMode(this) ;
    }

    public String getName() {
        return name_ ;
    }

    public void update(String gdata) throws Exception {
    }

    public Pose2d startPosition() {
        return start_ ;
    }

    protected AutoController getAutoController() {
        return ctrl_ ;
    }

    private String name_ ;
    private AutoController ctrl_ ;
    private Pose2d start_ ;
}