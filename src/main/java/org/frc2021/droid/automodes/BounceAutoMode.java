package org.frc2021.droid.automodes;

import org.xero1425.base.actions.InvalidActionRequest;
import org.xero1425.base.tankdrive.TankDrivePathFollowerAction;
import org.xero1425.base.tankdrive.TankDriveSubsystem;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MissingParameterException;
import org.xero1425.misc.MissingPathException;

public class BounceAutoMode extends DroidAutoMode {
    public BounceAutoMode(DroidAutoController ctrl) throws InvalidActionRequest, MissingPathException, BadParameterTypeException, MissingParameterException {
        super(ctrl, "BounceAutoMode") ;

        TankDriveSubsystem db = getDroidSubsystem().getTankDrive() ; 

        addSubActionPair(db, new TankDrivePathFollowerAction(db, "Bounce_P1", false), true);
        addSubActionPair(db, new TankDrivePathFollowerAction(db, "Bounce_P2", true), true);
        addSubActionPair(db, new TankDrivePathFollowerAction(db, "Bounce_P3", false), true);
        addSubActionPair(db, new TankDrivePathFollowerAction(db, "Bounce_P4", true), true);
    }
}