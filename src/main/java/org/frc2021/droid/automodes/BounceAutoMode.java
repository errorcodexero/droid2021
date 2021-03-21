package org.frc2021.droid.automodes;

import org.xero1425.base.actions.InvalidActionRequest;
import org.xero1425.base.tankdrive.TankDriveFollowPathAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.MissingPathException;

import edu.wpi.first.wpilibj.geometry.Pose2d;

public class BounceAutoMode extends DroidAutoMode {
    public BounceAutoMode(DroidAutoController ctrl) throws InvalidActionRequest, MissingPathException, BadParameterTypeException, MissingParameterException {
        super(ctrl, "BounceAutoMode", new Pose2d()) ;

        TankDriveSubsystem db = getDroidSubsystem().getTankDrive() ; 

        addSubActionPair(db, new TankDriveFollowPathAction(db, "Bounce_P1", false), true);
        addSubActionPair(db, new TankDriveFollowPathAction(db, "Bounce_P2", true), true);
        addSubActionPair(db, new TankDriveFollowPathAction(db, "Bounce_P3", false), true);
        addSubActionPair(db, new TankDriveFollowPathAction(db, "Bounce_P4", true), true);
    }
}