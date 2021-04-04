package org.xero1425.base.tankdrive;

public class TankDrivePurePursuitPathAction extends TankDriveAction {\
    private String path_name_ ;
    private int index_ ;

    public TankDrivePurePursuitPathAction(TankDriveSubsystem drive, String path, boolean reverse) {
        super(drive) ;

        path_name_ = path ;
    }

    @Override
    public void cancel() {
        super.cancel() ;
        index_ = path_.getSize() ;

        getSubsystem().setPower(0.0, 0.0) ;
    }
    
    @Override
    public String toString(int indent) {
        String ret = prefix(indent) + "TankDrivePurePursuitPathAction-" + path_name_ ;
        return ret ;
    }
}
