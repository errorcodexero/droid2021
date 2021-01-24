package org.xero1425.base.swervedrive;

import org.xero1425.base.actions.Action;

public abstract class SwerveDriveAction extends Action {
    private SwerveDriveSubsystem sub_ ;

    public SwerveDriveAction(SwerveDriveSubsystem drive) {
        super(drive.getRobot().getMessageLogger()) ;

        sub_ = drive ;
    }

    protected SwerveDriveSubsystem getSubsystem() {
        return sub_ ;
    }
}
