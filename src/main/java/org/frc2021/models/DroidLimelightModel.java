package org.frc2021.models;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import org.xero1425.simulator.engine.SimulationEngine;
import org.xero1425.simulator.engine.SimulationModel;
import org.xero1425.simulator.models.LimeLightModel;
import org.xero1425.simulator.models.TankDriveModel;
import org.xero1425.misc.BadParameterTypeException;
import org.xero1425.misc.MessageLogger;
import org.xero1425.misc.MessageType;
import org.xero1425.misc.SettingsValue;
import org.xero1425.misc.XeroMath;

public class DroidLimelightModel extends SimulationModel {
        
    private TurretModel turret_ ;
    private LimeLightModel limelight_ ;
    private TankDriveModel db_ ;
    private static final Translation2d target_pos_ = new Translation2d(625, 92) ;
    private double target_height_ ;
    private double camera_height_ ;
    private double camera_angle_ ;
    private Rotation2d shooter_rotation_ ;

    private static final String LimelightModelPropertyName = "limelight_model";
    private static final String LimelightInstancePropertyName = "limelight_instance";
    private static final String TankDriveModelPropertyName = "tankdrive_model";
    private static final String TankDriveInstancePropertyName = "tankdrive_instance";
    private static final String TurretModelPropertyName = "turret_model";
    private static final String TurretInstancePropertyName = "turret_instance";
    private static final String TargetHeightParamName = "target_height";
    private static final String CameraHeightParamName = "camera_height";
    private static final String CameraAngleParamName = "camera_angle" ;

    public DroidLimelightModel(SimulationEngine engine, String model, String inst) {
        super(engine, model, inst);

        shooter_rotation_ = Rotation2d.fromDegrees(180.0);
    }

    public boolean create() {
        MessageLogger logger = getEngine().getMessageLogger();

        if (!hasProperty(LimelightModelPropertyName)) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" is missing required property").addQuoted(LimelightModelPropertyName);
            logger.endMessage();
            return false;
        }

        if (!hasProperty(LimelightInstancePropertyName)) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" is missing required property").addQuoted(LimelightInstancePropertyName);
            logger.endMessage();
            return false;
        }

        SettingsValue modelprop = getProperty(LimelightModelPropertyName);
        SettingsValue instprop = getProperty(LimelightInstancePropertyName);

        if (!modelprop.isString()) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" property ").addQuoted(LimelightModelPropertyName).add(" is not a string");
            logger.endMessage();
            return false;
        }

        if (!modelprop.isString()) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" property ").addQuoted(LimelightInstancePropertyName).add(" is not a string");
            logger.endMessage();
            return false;
        }

        try {
            limelight_ = (LimeLightModel) getEngine().findModel(modelprop.getString(), instprop.getString());
        } catch (Exception ex) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" referenced limelight model is not a limelight");
            logger.endMessage();
            return false;
        }

        if (!hasProperty(TankDriveModelPropertyName)) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" is missing required property").addQuoted(TankDriveModelPropertyName);
            logger.endMessage();
            return false;
        }

        if (!hasProperty(TankDriveInstancePropertyName)) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" is missing required property").addQuoted(TankDriveInstancePropertyName);
            logger.endMessage();
            return false;
        }

        modelprop = getProperty(TankDriveModelPropertyName);
        instprop = getProperty(TankDriveInstancePropertyName);

        if (!modelprop.isString()) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" property ").addQuoted(LimelightModelPropertyName).add(" is not a string");
            logger.endMessage();
            return false;
        }

        if (!modelprop.isString()) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" property ").addQuoted(LimelightInstancePropertyName).add(" is not a string");
            logger.endMessage();
            return false;
        }

        try {
            db_ = (TankDriveModel) getEngine().findModel(modelprop.getString(), instprop.getString());
        } catch (Exception ex) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" referenced tankdrive model is not a TankDriveModel");
            logger.endMessage();
            return false;
        }

        if (!hasProperty(TurretModelPropertyName)) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" is missing required property").addQuoted(TurretModelPropertyName);
            logger.endMessage();
            return false;
        }

        if (!hasProperty(TurretInstancePropertyName)) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" is missing required property").addQuoted(TurretInstancePropertyName);
            logger.endMessage();
            return false;
        }

        modelprop = getProperty(TurretModelPropertyName);
        instprop = getProperty(TurretInstancePropertyName);

        if (!modelprop.isString()) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" property ").addQuoted(TurretModelPropertyName).add(" is not a string");
            logger.endMessage();
            return false;
        }

        if (!modelprop.isString()) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" property ").addQuoted(TurretInstancePropertyName).add(" is not a string");
            logger.endMessage();
            return false;
        }

        try {
            turret_ = (TurretModel) getEngine().findModel(modelprop.getString(), instprop.getString());
        } catch (Exception ex) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" referenced turret model is not a TurretModel");
            logger.endMessage();
            return false;
        }

        if (!hasProperty(TargetHeightParamName)) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" is missing required property").addQuoted(TargetHeightParamName);
            logger.endMessage();
        }

        SettingsValue v = getProperty(TargetHeightParamName);
        if (!v.isDouble()) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" property ").addQuoted(TargetHeightParamName);
            logger.add(" exists but is not a double");
            logger.endMessage();
        }
        try {
            target_height_ = v.getDouble();
        } catch (BadParameterTypeException e) {
        }

        if (!hasProperty(CameraHeightParamName)) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" is missing required property").addQuoted(CameraHeightParamName);
            logger.endMessage();
        }

        v = getProperty(CameraHeightParamName);
        if (!v.isDouble()) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" property ").addQuoted(CameraHeightParamName);
            logger.add(" exists but is not a double");
            logger.endMessage();
        }
        try {
            camera_height_ = v.getDouble();
        } catch (BadParameterTypeException e) {
        }        

        if (!hasProperty(CameraAngleParamName)) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" is missing required property").addQuoted(CameraAngleParamName);
            logger.endMessage();
        }

        v = getProperty(CameraAngleParamName);
        if (!v.isDouble()) {
            logger.startMessage(MessageType.Error);
            logger.add("event: model ").addQuoted(getModelName());
            logger.add(" instance ").addQuoted(getInstanceName());
            logger.add(" property ").addQuoted(CameraAngleParamName);
            logger.add(" exists but is not a double");
            logger.endMessage();
        }
        try {
            camera_angle_ = v.getDouble();
        } catch (BadParameterTypeException e) {
        }         

        setCreated();
        return true ;
    }

    public boolean processEvent(String name, SettingsValue value) {
        return false ;
    }

    public void run(double dt) {

        //
        // Get the robot pose, pose is position plus heading angle
        //
        Pose2d robot = db_.getPose() ;

        if (Math.abs(turret_.getAngle().getDegrees()) > 0.1)
            System.out.println("Testing") ;

        //
        // Get the shooter angle, which is the robot angle rotated by 180 degrees.  The
        // shooter is mounted on the back of the robot
        //
        Rotation2d shooter = robot.getRotation().rotateBy(shooter_rotation_) ;
        double shooterdeg = shooter.getDegrees() ;
        Rotation2d effective = shooter.rotateBy(turret_.getAngle()) ;
        double effectivedeg = effective.getDegrees() ;

        //
        // Get the angle from the center of the robot to the target
        //
        Translation2d delta = target_pos_.minus(robot.getTranslation()) ;
        Rotation2d robottarget = new Rotation2d(Math.atan2(-delta.getY(), delta.getX())) ;
        double robottargetdeg = robottarget.getDegrees() ;
        Rotation2d result = robottarget.plus(effective) ;
        double resultdeg = result.getDegrees() ;
        double turretdeg = turret_.getAngle().getDegrees() ;

        if (result.getDegrees() > -60.0 && result.getDegrees() < 60.0) {
            double dist = robot.getTranslation().getDistance(target_pos_) ;
            double ty = Math.atan2(target_height_ - camera_height_, dist) ;
            limelight_.setTY(XeroMath.rad2deg(ty) - camera_angle_);
            limelight_.setTX(result.getDegrees()) ;
            limelight_.setTV(1.0) ;
        }
        else {
            limelight_.setTV(0.0) ;
        }
    }

}