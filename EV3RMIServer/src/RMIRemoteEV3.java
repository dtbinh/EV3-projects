import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import lejos.hardware.lcd.Font;
import lejos.remote.ev3.RMIAnalogPort;
import lejos.remote.ev3.RMIAudio;
import lejos.remote.ev3.RMIBattery;
import lejos.remote.ev3.RMIBluetooth;
import lejos.remote.ev3.RMIEV3;
import lejos.remote.ev3.RMIGraphicsLCD;
import lejos.remote.ev3.RMII2CPort;
import lejos.remote.ev3.RMILCD;
import lejos.remote.ev3.RMIMotorPort;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RMIRemoteTextLCD;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RMITextLCD;
import lejos.remote.ev3.RMIUARTPort;
import lejos.remote.ev3.RMIWifi;


public class RMIRemoteEV3 extends UnicastRemoteObject implements RMIEV3 {

	private static final long serialVersionUID = -6637513883001761328L;

	protected RMIRemoteEV3() throws RemoteException {
		super(0);
	}

	@Override
	public RMIBattery getBattery() throws RemoteException {
		return new RMIRemoteBattery();
	}

	@Override
	public RMIAnalogPort openAnalogPort(String portName) throws RemoteException {
		return new RMIRemoteAnalogPort(portName);
	}

	@Override
	public RMII2CPort openI2CPort(String portName) throws RemoteException {
		return new RMIRemoteI2CPort(portName);
	}

	@Override
	public RMIUARTPort openUARTPort(String portName) throws RemoteException {
		return new RMIRemoteUARTPort(portName);
	}

	@Override
	public RMIMotorPort openMotorPort(String portName) throws RemoteException {
		return new RMIRemoteMotorPort(portName);
	}

	@Override
	public RMISampleProvider createSampleProvider(String portName,
			String sensorName, String modeName) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RMIRegulatedMotor createRegulatedMotor(String portName)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RMIWifi getWifi() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RMIBluetooth getBluetooth() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RMIAudio getAudio() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RMITextLCD getTextLCD() throws RemoteException {
		return null;
	}

	@Override
	public RMIGraphicsLCD getGraphicsLCD() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RMITextLCD getTextLCD(Font f) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
