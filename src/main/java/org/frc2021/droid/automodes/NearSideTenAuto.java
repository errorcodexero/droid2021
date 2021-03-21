package org.frc2021.droid.automodes;

import edu.wpi.first.wpilibj.geometry.Pose2d;

//
// Start along the side of our trench at the start line against the edge of the field.
// Drive to collect the two balls on the edge of the rendezvous point.  Drive back to the
// edge of the trench and score the five.  Then drive the trench collecting the five balls
// there.  Drive back to the front of the trench and score these five.
//
// This mode will not work with the 2021 field as the balls in the rendezvous area have been
// moved.  This mode is only for 2020.
//
// Status: never working completely, needed a collector that could quickly collect two balls
//         in parallel to work.
//
public class NearSideTenAuto extends DroidAutoMode {
    public NearSideTenAuto(DroidAutoController ctrl) throws Exception {
        super(ctrl, "NearSideTen", new Pose2d()) ;

        //
        // Initial ball count, 3 balls loaded into the robot at start
        //
        setInitialBallCount(3);

        // Collect two from the middle zone
        driveAndCollect("ten_ball_auto_collect1") ;

        // Fire the initial five
        driveAndFire("ten_ball_auto_fire1", true, 20.0) ;

        // Collect five more down the trench
        driveAndCollect("ten_ball_auto_collect2") ;     
        
        // Fire the next five
        driveAndFire("ten_ball_auto_fire2", true, 20.0) ;        
    }
}