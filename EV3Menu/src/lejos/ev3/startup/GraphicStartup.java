package lejos.ev3.startup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import lejos.ev3.startup.GraphicListMenu;
import lejos.ev3.startup.Utils;
import lejos.utility.Delay;
import lejos.ev3.startup.Config;
import lejos.hardware.Bluetooth;
import lejos.hardware.Button;
import lejos.hardware.LocalBTDevice;
import lejos.hardware.LocalWifiDevice;
import lejos.hardware.RemoteBTDevice;
import lejos.hardware.Sound;
import lejos.hardware.Wifi;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCDOutputStream;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.port.Port;
import lejos.hardware.port.TachoMotorPort;
import lejos.internal.io.Settings;
import lejos.internal.io.SystemSettings;
import lejos.remote.ev3.Menu;
import lejos.remote.ev3.MenuReply;
import lejos.remote.ev3.MenuRequest;
import lejos.remote.ev3.RMIRemoteEV3;

public class GraphicStartup implements Menu {
	
	static final int REMOTE_MENU_PORT = 8002;
	
	static final String JAVA_RUN_JAR = "jrun -jar ";
	static final String JAVA_DEBUG_JAR = "jrun -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=y -jar ";
	
	static final String defaultProgramProperty = "lejos.default_program";
    static final String defaultProgramAutoRunProperty = "lejos.default_autoRun";
    static final String sleepTimeProperty = "lejos.sleep_time";
    static final String pinProperty = "lejos.bluetooth_pin";
    static final String ntpProperty = "lejos.ntp_host";
	
    static final String ICMProgram = "\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0003\u00c0\u0003\u0000\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u000c\u00c0\u0003\u00c0\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u0003\u00c0\u0003\u0030\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00ff\u00cf\u00c3\u0003\u00ff\u00cf\u00c3\u0003\u0000\u0000\u00c3\u0003\u0000\u0000\u00c3\u0003\u00fc\u00f3\u00c0\u0003\u00fc\u00f3\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u00c3\u00ff\u003f\u00c0\u00c3\u00ff\u003f\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";
    static final String ICMSound = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00c0\u0000\u0003\u0000\u00c0\u0000\u0003\u0000\u00f0\u0030\u000c\u0000\u00f0\u0030\u000c\u0000\u00cc\u00c0\u0030\u0000\u00cc\u00c0\u0030\u0000\u00c3\u000c\u0033\u0000\u00c3\u000c\u0033\u00fc\u00c3\u0030\u0033\u00fc\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u003c\u00c3\u0030\u0033\u003c\u00c3\u0030\u0033\u00cc\u00cf\u0030\u0033\u00cc\u00cf\u0030\u0033\u00fc\u00f3\u0030\u0033\u00fc\u00f3\u0030\u0033\u0000\u00cf\u000c\u0033\u0000\u00cf\u000c\u0033\u0000\u00fc\u00c0\u0030\u0000\u00fc\u00c0\u0030\u0000\u00f0\u0030\u000c\u0000\u00f0\u0030\u000c\u0000\u00c0\u0000\u0003\u0000\u00c0\u0000\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    static final String ICMFile = "\u0000\u00c0\u0000\u0000\u0000\u00c0\u0000\u0000\u0000\u0030\u00ff\u000f\u0000\u0030\u00ff\u000f\u0000\u000c\u000c\u0030\u0000\u000c\u000c\u0030\u00fc\u0003\u0030\u00cc\u00fc\u0003\u0030\u00cc\u00c3\u0000\u00c0\u00f0\u00c3\u0000\u00c0\u00f0\u003f\u0000\u0000\u00c3\u003f\u0000\u0000\u00c3\u00ff\u003f\u0000\u00fc\u00ff\u003f\u0000\u00fc\u0003\u00c0\u0000\u00f0\u0003\u00c0\u0000\u00f0\u0003\u0000\u00ff\u00ff\u0003\u0000\u00ff\u00ff\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00f3\u0003\u0000\u00c0\u00cc\u0003\u0000\u00c0\u00cc\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00cc\u0003\u0000\u0000\u00cc\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00f3\u0003\u0000\u00c0\u00cc\u0003\u0000\u00c0\u00cc\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";

    static final String ICDefault = "\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0003\u00c0\u0003\u0000\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u000c\u00c0\u0003\u00c0\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u0003\u00c0\u0003\u0030\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u00f3\u00cf\u00cf\u00c3\u00f3\u00cf\u00cf\u00c3\u00c3\u000f\u0000\u00c3\u00c3\u000f\u0000\u00c3\u00f3\u00cf\u00f3\u00c0\u00f3\u00cf\u00f3\u00c0\u00f3\u000c\u0000\u00c0\u00f3\u000c\u0000\u00c0\u00f3\u00c3\u003f\u00c0\u00f3\u00c3\u003f\u00c0\u00c3\u0003\u0000\u00c0\u00c3\u0003\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";
    static final String ICProgram = "\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0003\u00c0\u0003\u0000\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u000c\u00c0\u0003\u00c0\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u0003\u00c0\u0003\u0030\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00ff\u00cf\u00c3\u0003\u00ff\u00cf\u00c3\u0003\u0000\u0000\u00c3\u0003\u0000\u0000\u00c3\u0003\u00fc\u00f3\u00c0\u0003\u00fc\u00f3\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u00c3\u00ff\u003f\u00c0\u00c3\u00ff\u003f\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";
    static final String ICFiles = "\u0000\u00c0\u0000\u0000\u0000\u00c0\u0000\u0000\u0000\u0030\u00ff\u000f\u0000\u0030\u00ff\u000f\u0000\u000c\u000c\u0030\u0000\u000c\u000c\u0030\u00fc\u0003\u0030\u00cc\u00fc\u0003\u0030\u00cc\u00c3\u0000\u00c0\u00f0\u00c3\u0000\u00c0\u00f0\u003f\u0000\u0000\u00c3\u003f\u0000\u0000\u00c3\u00ff\u003f\u0000\u00fc\u00ff\u003f\u0000\u00fc\u0003\u00c0\u0000\u00f0\u0003\u00c0\u0000\u00f0\u0003\u0000\u00ff\u00ff\u0003\u0000\u00ff\u00ff\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00f3\u0003\u0000\u00c0\u00cc\u0003\u0000\u00c0\u00cc\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00cc\u0003\u0000\u0000\u00cc\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00f3\u0003\u0000\u00c0\u00cc\u0003\u0000\u00c0\u00cc\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";
    static final String ICBlue = "\u0000\u00f0\u000f\u0000\u0000\u00f0\u000f\u0000\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00ff\u0000\u00c0\u003f\u00ff\u0003\u00c0\u003f\u00ff\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00f0\u003c\u00f0\u000f\u00f0\u003c\u00f0\u000f\u00f0\u0030\u00c3\u000f\u00f0\u0030\u00c3\u000f\u00f0\u0003\u00c3\u000f\u00f0\u0003\u00c3\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u0003\u00c3\u000f\u00f0\u0003\u00c3\u000f\u00f0\u0030\u00c3\u000f\u00f0\u0030\u00c3\u000f\u00f0\u003c\u00f0\u000f\u00f0\u003c\u00f0\u000f\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00ff\u0003\u00c0\u003f\u00ff\u0003\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00ff\u0000\u0000\u00f0\u000f\u0000\u0000\u00f0\u000f\u0000";

    static final String ICSound = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00c0\u0000\u0003\u0000\u00c0\u0000\u0003\u0000\u00f0\u0030\u000c\u0000\u00f0\u0030\u000c\u0000\u00cc\u00c0\u0030\u0000\u00cc\u00c0\u0030\u0000\u00c3\u000c\u0033\u0000\u00c3\u000c\u0033\u00fc\u00c3\u0030\u0033\u00fc\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u003c\u00c3\u0030\u0033\u003c\u00c3\u0030\u0033\u00cc\u00cf\u0030\u0033\u00cc\u00cf\u0030\u0033\u00fc\u00f3\u0030\u0033\u00fc\u00f3\u0030\u0033\u0000\u00cf\u000c\u0033\u0000\u00cf\u000c\u0033\u0000\u00fc\u00c0\u0030\u0000\u00fc\u00c0\u0030\u0000\u00f0\u0030\u000c\u0000\u00f0\u0030\u000c\u0000\u00c0\u0000\u0003\u0000\u00c0\u0000\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";

    static final String ICNXT = "\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u0030\u0000\u0000\u000c\u0030\u0000\u0000\u000c\u0030\u00ff\u00ff\u000c\u0030\u00ff\u00ff\u000c\u0030\u0003\u00c0\u000c\u0030\u0003\u00c0\u000c\u0030\u000f\u00c0\u000c\u0030\u000f\u00c0\u000c\u0030\u0033\u00c0\u000c\u0030\u0033\u00c0\u000c\u0030\u00cf\u00cc\u000c\u0030\u00cf\u00cc\u000c\u0030\u00ff\u00ff\u000c\u0030\u00ff\u00ff\u000c\u0030\u0000\u0000\u000c\u0030\u0000\u0000\u000c\u0030\u00cf\u00f3\u000c\u0030\u00cf\u00f3\u000c\u0030\u00cc\u0033\u000c\u0030\u00cc\u0033\u000c\u00f0\u00c0\u0003\u000c\u00f0\u00c0\u0003\u000c\u0030\u0033\u0000\u000c\u0030\u0033\u0000\u000c\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003";

    static final String ICLeJOS = "\u0000\u0000\u00fc\u000f\u0000\u0000\u00fc\u000f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00c0\u003f\u0000\u0000\u00c0\u003f\u0000\u0000\u00c0\u003f\u0000\u0000\u00c0\u003f\u0000\u00c0\u00cc\u003f\u0000\u00c0\u00cc\u003f\u0000\u0030\u00c3\u003f\u0000\u0030\u00c3\u003f\u0000\u00c0\u00cc\u003f\u0000\u00c0\u00cc\u003f\u00fc\u0033\u00c3\u003f\u00fc\u0033\u00c3\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00ff\u0000";

    static final String ICPower = "\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u00c0\u00cf\u00f3\u0003\u00c0\u00cf\u00f3\u0003\u00f0\u00cf\u00f3\u000f\u00f0\u00cf\u00f3\u000f\u00fc\u00c3\u00c3\u003f\u00fc\u00c3\u00c3\u003f\u00fc\u00c0\u0003\u003f\u00fc\u00c0\u0003\u003f\u00ff\u00c0\u0003\u00ff\u00ff\u00c0\u0003\u00ff\u003f\u00c0\u0003\u00fc\u003f\u00c0\u0003\u00fc\u003f\u00c0\u0003\u00fc\u003f\u00c0\u0003\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u00ff\u0000\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00fc\u0000\u0000\u003f\u00fc\u0000\u0000\u003f\u00fc\u000f\u00f0\u003f\u00fc\u000f\u00f0\u003f\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000";
    static final String ICVisibility = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u00f0\u0003\u00c0\u000f\u00f0\u0003\u00c0\u000f\u00fc\u00c0\u0003\u003f\u00fc\u00c0\u0003\u003f\u003f\u00f0\u000f\u00fc\u003f\u00f0\u000f\u00fc\u003f\u00f0\u000f\u00fc\u003f\u00f0\u000f\u00fc\u00fc\u00c0\u0003\u003f\u00fc\u00c0\u0003\u003f\u00f0\u0003\u00c0\u000f\u00f0\u0003\u00c0\u000f\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    static final String ICSearch = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00fc\u0003\u0000\u0000\u00fc\u0003\u0000\u00c0\u0003\u003c\u0000\u00c0\u0003\u003c\u0000\u00c0\u003c\u0030\u0000\u00c0\u003c\u0030\u0000\u0030\u000f\u00c0\u0000\u0030\u000f\u00c0\u0000\u0030\u0003\u00c0\u0000\u0030\u0003\u00c0\u0000\u0030\u0003\u00c0\u0000\u0030\u0003\u00c0\u0000\u0030\u0000\u00c0\u0000\u0030\u0000\u00c0\u0000\u00c0\u0000\u0030\u0000\u00c0\u0000\u0030\u0000\u00c0\u0003\u00fc\u0000\u00c0\u0003\u00fc\u0000\u0000\u00fc\u0033\u0003\u0000\u00fc\u0033\u0003\u0000\u0000\u00c0\u000c\u0000\u0000\u00c0\u000c\u0000\u0000\u0000\u0033\u0000\u0000\u0000\u0033\u0000\u0000\u0000\u003c\u0000\u0000\u0000\u003c\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    static final String ICPIN = "\u0000\u0000\u00ff\u0003\u0000\u0000\u00ff\u0003\u0000\u00c0\u0000\u000c\u0000\u00c0\u0000\u000c\u0000\u0030\u0000\u0030\u0000\u0030\u0000\u0030\u0000\u000c\u00f0\u00c3\u0000\u000c\u00f0\u00c3\u0000\u000c\u0030\u00c3\u0000\u000c\u0030\u00c3\u0000\u000c\u00f0\u00c3\u0000\u000c\u00f0\u00c3\u0000\u000c\u0000\u00f0\u0000\u000c\u0000\u00f0\u0000\u000c\u0000\u00cc\u0000\u000c\u0000\u00cc\u0000\u0033\u0030\u0033\u0000\u0033\u0030\u0033\u00c0\u000c\u00cc\u000c\u00c0\u000c\u00cc\u000c\u0030\u00c3\u00ff\u0003\u0030\u00c3\u00ff\u0003\u00cc\u00c0\u0000\u0000\u00cc\u00c0\u0000\u0000\u0033\u00fc\u0000\u0000\u0033\u00fc\u0000\u0000\u000f\u000c\u0000\u0000\u000f\u000c\u0000\u0000\u0003\u000f\u0000\u0000\u0003\u000f\u0000\u0000\u00ff\u0000\u0000\u0000\u00ff\u0000\u0000\u0000";
    
    static final String ICDelete = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00c0\u000f\u0000\u0000\u00c0\u000f\u0000\u00c0\u00ff\u00ff\u000f\u00c0\u00ff\u00ff\u000f\u0030\u0000\u0000\u0030\u0030\u0000\u0000\u0030\u00c0\u00ff\u00ff\u000f\u00c0\u00ff\u00ff\u000f\u00c0\u0000\u0000\u000c\u00c0\u0000\u0000\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u000c\u000f\u00c0\u00cc\u000c\u000f\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u000c\u0030\u000f\u00c0\u000c\u0030\u000f\u00c0\u00c0\u00cc\u000c\u00c0\u00c0\u00cc\u000c\u0000\u00ff\u00ff\u0003\u0000\u00ff\u00ff\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    static final String ICFormat = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u0003\u00c0\u0000\u0000\u0003\u00c0\u0000\u0000\u00f3\u00cf\u0000\u0000\u00f3\u00cf\u0000\u00c0\u000c\u0030\u0003\u00c0\u000c\u0030\u0003\u00c0\u0000\u0000\u0003\u00c0\u0000\u0000\u0003\u0030\u0003\u00c0\u000c\u0030\u0003\u00c0\u000c\u0030\u00fc\u003f\u000c\u0030\u00fc\u003f\u000c\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u000c\u0000\u00cc\u0030\u000c\u0000\u00cc\u0030\u00cc\u00ff\u0000\u0030\u00cc\u00ff\u0000\u0030\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    static final String ICSleep = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00fc\u0003\u0000\u0000\u00fc\u0003\u0000\u0000\u00c0\u00f0\u000f\u0000\u00c0\u00f0\u000f\u0000\u0030\u0000\u0033\u0000\u0030\u0000\u0033\u0000\u00fc\u00cf\u00c0\u0003\u00fc\u00cf\u00c0\u0003\u0000\u00f3\u000f\u000c\u0000\u00f3\u000f\u000c\u0000\u0003\u0000\u000c\u0000\u0003\u0000\u000c\u00c0\u000c\u000f\u0033\u00c0\u000c\u000f\u0033\u00c0\u00f0\u00f0\u0030\u00c0\u00f0\u00f0\u0030\u00c0\u0000\u0000\u0030\u00c0\u0000\u0000\u0030\u00c0\u0000\u0000\u0030\u00c0\u0000\u0000\u0030\u0000\u0003\u000f\u000c\u0000\u0003\u000f\u000c\u0000\u0003\u000f\u000c\u0000\u0003\u000f\u000c\u0000\u003c\u00c0\u0003\u0000\u003c\u00c0\u0003\u0000\u00c0\u003f\u0000\u0000\u00c0\u003f\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    static final String ICAutoRun = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00ff\u00f0\u00ff\u00ff\u00ff\u00f0\u00ff\u00ff\u0000\u000c\u0000\u00c0\u0000\u000c\u0000\u00c0\u003f\u000c\u00c0\u00cc\u003f\u000c\u00c0\u00cc\u0000\u00ff\u00ff\u003f\u0000\u00ff\u00ff\u003f\u000f\u0003\u0000\u0030\u000f\u0003\u0000\u0030\u00c0\u0000\u0000\u000c\u00c0\u0000\u0000\u000c\u00c3\u0000\u0000\u000f\u00c3\u0000\u0000\u000f\u0030\u0000\u00cc\u0003\u0030\u0000\u00cc\u0003\u0033\u0030\u0033\u0003\u0033\u0030\u0033\u0003\u00f0\u00ff\u00ff\u0000\u00f0\u00ff\u00ff\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
 
    static final String ICYes = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u000f\u0000\u0000\u0000\u000f\u0000\u0000\u00c0\u003f\u0000\u0000\u00c0\u003f\u0000\u0000\u00f0\u00ff\u0000\u0000\u00f0\u00ff\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0030\u0000\u00ff\u000f\u0030\u0000\u00ff\u000f\u00fc\u00c0\u00ff\u0003\u00fc\u00c0\u00ff\u0003\u00ff\u00f3\u00ff\u0000\u00ff\u00f3\u00ff\u0000\u00ff\u00ff\u003f\u0000\u00ff\u00ff\u003f\u0000\u00fc\u00ff\u000f\u0000\u00fc\u00ff\u000f\u0000\u00f0\u00ff\u0003\u0000\u00f0\u00ff\u0003\u0000\u00c0\u00ff\u0000\u0000\u00c0\u00ff\u0000\u0000\u0000\u003f\u0000\u0000\u0000\u003f\u0000\u0000\u0000\u000c\u0000\u0000\u0000\u000c\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    static final String ICNo = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00f0\u0000\u0000\u000f\u00f0\u0000\u0000\u000f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u000f\u00f0\u003f\u00fc\u000f\u00f0\u003f\u00f0\u003f\u00fc\u000f\u00f0\u003f\u00fc\u000f\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00ff\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00ff\u0000\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u00f0\u003f\u00fc\u000f\u00f0\u003f\u00fc\u000f\u00fc\u000f\u00f0\u003f\u00fc\u000f\u00f0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00f0\u0000\u0000\u000f\u00f0\u0000\u0000\u000f\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";

    static final int defaultSleepTime = 2;
    static final int maxSleepTime = 10;

    // Threads
    private IndicatorThread ind = new IndicatorThread();
    private BatteryIndicator indiBA = new BatteryIndicator();
    private PipeReader pipeReader = new PipeReader();
    private RConsole rcons = new RConsole();
    private BroadcastThread broadcast = new BroadcastThread();
    private RemoteMenuThread remoteMenuThread = new RemoteMenuThread();
    
    //private GraphicMenu curMenu;
    private int timeout = 0;
    private boolean btVisibility;
    private static String version = "Unknown";
    private static String hostname;
    private static List<String> ips = getIPAddresses();
    private static LocalBTDevice bt;
	private static GraphicStartup menu = new GraphicStartup();
	
	private static TextLCD lcd = LocalEV3.get().getTextLCD();
    
    /**
     * Main method
     */
	public static void main(String[] args) throws Exception {
		System.out.println("Menu started");
		
        if (args.length > 0) {    	
        	hostname = args[0];
        }
        
        if (args.length > 1) {    	
        	version = args[1];
        }
        
        System.out.println("Host name: " + hostname);
        System.out.println("Version: " + version);
        
        // Check for autorun
    	File f = getDefaultProgram();
    	if (f != null)
    	{
        	String auto = Settings.getProperty(defaultProgramAutoRunProperty, "");        	
    		if (auto.equals("ON") && !Button.LEFT.isDown())
            {
            	System.out.println("Executing " + f.getPath());
                exec(JAVA_RUN_JAR + f.getPath());
            }
    	}
        
        TuneThread tuneThread = new TuneThread();
        tuneThread.start();
        
        System.out.println("Getting IP addresses");
        ips = getIPAddresses();
        
        // Start the RMI registry 
        InitThread initThread = new InitThread();
        initThread.start();
   
        System.out.println("Starting background threads");
        menu.start();  
        
        System.out.println("Starting the menu");
        menu.mainMenu();
        
        System.out.println("Menu finished");    
        System.exit(0);
	}
	
	
	/**
	 * Start-up thread
	 */
    static class InitThread extends Thread
    {
        /**
         * Create the Bluetooth local device and connect to DBus
         * 
         * Start the RMI server
         * 
         * Broadcast device availability
         * 
         * Get the time from a name server
         */            
        @Override
        public void run()
        {               
        	// Create the Bluetooth local device and connect to DBus
            System.out.println("Creating bluetooth local device");
            bt = Bluetooth.getLocalDevice();
            
        	// Start the RMI server
            System.out.println("Starting RMI");
            
            // Use last IP address, which will be Wifi, it it exists
            String lastIp = null;
            for (String ip: ips) {
            	lastIp = ip;
            }
            
            System.out.println("Setting java.rmi.server.hostname to " + lastIp);
            System.setProperty("java.rmi.server.hostname", lastIp);
            
            try { //special exception handler for registry creation
                LocateRegistry.createRegistry(1099); 
                System.out.println("java RMI registry created.");
            } catch (RemoteException e) {
                //do nothing, error means registry already exists
                System.out.println("java RMI registry already exists.");
            }
            
            try {
    			RMIRemoteEV3 ev3 = new RMIRemoteEV3();
    			Naming.rebind("//localhost/RemoteEV3", ev3);
    			RMIRemoteMenu remoteMenu = new RMIRemoteMenu(menu);
    			Naming.rebind("//localhost/RemoteMenu", remoteMenu);
    		} catch (Exception e) {
    			System.err.println("RMI failed to start: " + e);
    		}
            
            // Broadcast availability of device
            Broadcast.broadcast(hostname);
            
            // Set the date
            try {
				String dt = SntpClient.getDate(Settings.getProperty(ntpProperty, "1.uk.pool.ntp.org"));
				System.out.println("Date and time is " + dt);
				Runtime.getRuntime().exec("date -s " + dt);
			} catch (IOException e) {
				System.err.println("Failed to get time from ntp: " + e);
			}
            
            System.out.println("Initialisation complete");
        }
    }
    
    /**
     *  Start the background threads
     */
    private void start()
    {       
    	ind.start();
    	rcons.start();
    	pipeReader.start();
    	broadcast.start();
    	remoteMenuThread.start();
    }
	
	/**
     * Display the main system menu.
     * Allow the user to select File, Bluetooth, Sound, System operations.
     */
    private void mainMenu()
    {
        GraphicMenu menu = new GraphicMenu(new String[]
                {
                    "Run Default", "Files", "Samples", "Bluetooth", "Wifi", "Sound", "System", "Version"
                },new String[] {ICDefault,ICFiles,ICFiles,ICBlue,ICBlue,ICSound,ICNXT,ICLeJOS},3);
        int selection = 0;
        do
        {
            newScreen(hostname);
            int row = 1;
            for(String ip: ips) {
            	lcd.drawString(ip,8 - ip.length()/2,row++);
            }
            selection = getSelection(menu, selection);
            switch (selection)
            {
                case 0:
                    mainRunDefault();
                    break;
                case 1:
                    filesMenu();
                    break;
                case 2:
                    samplesMenu();
                    break;
                case 3:
                    bluetoothMenu();
                    break;
                case 4:
                    wifiMenu();
                    break;
                case 5:
                    soundMenu();
                    break;
                case 6:
                    systemMenu();
                    break;
                case 7:
                    displayVersion();
                    break;
            }
            
            if (selection < 0)  {
            	if (getYesNo("  Shut down EV3 ?", false) == 1)
            		break;
            }
        } while (true);
        
        // Shut down the EV3
        shutdown();
    }
    
    public class RemoteMenuThread extends Thread {
        @Override
        public void run() {
        	
    		ServerSocket ss;
    		Socket conn = null;
    		
        	// Create a server socket
        	try {
    			ss = new ServerSocket(REMOTE_MENU_PORT);
    			System.out.println("Remote menu server socket created");
    		} catch (IOException e) {
    			System.err.println("Error creating server socket: " + e);
    			return;
    		}
        	
        	while(true) {   		
                try {
                	System.out.println("Waiting for a remote menu connection");
            		conn = ss.accept();
            		//conn.setSoTimeout(2000);
            		
            		ObjectOutputStream os = new ObjectOutputStream(conn.getOutputStream());
            		ObjectInputStream is = new ObjectInputStream(conn.getInputStream());
            		            		
            		try {
	            		while(true) { 
	                		MenuRequest request = (MenuRequest) is.readObject();
	                		MenuReply reply = new MenuReply();
	                		
		            		switch (request.request) {
		            		case RUN_PROGRAM:
		            			runProgram(request.name);
		            			break;
							case DEBUG_PROGRAM:
								debugProgram(request.name);
								break;
							case DELETE_ALL_PROGRAMS:
								deleteAllPrograms();
								break;
							case DELETE_FILE:
								reply.result = deleteFile(request.name);
								os.writeObject(reply);
								break;
							case FETCH_FILE:
								reply.contents = fetchFile(request.name);
								os.writeObject(reply);
								break;
							case GET_FILE_SIZE:
								reply.reply = (int) getFileSize(request.name);
								os.writeObject(reply);
								break;
							case GET_MENU_VERSION:
								reply.value = getMenuVersion();
								os.writeObject(reply);
								break;
							case GET_NAME:
								reply.value = menu.getName();
								os.writeObject(reply);
								break;
							case GET_PROGRAM_NAMES:
								reply.names = getProgramNames();
								os.writeObject(reply);
								break;
							case GET_SAMPLE_NAMES:
								reply.names = getSampleNames();
								os.writeObject(reply);
								break;
							case GET_SETTING:
								reply.value = getSetting(request.name);
								os.writeObject(reply);
								break;
							case GET_VERSION:
								reply.value = getVersion();
								os.writeObject(reply);
								break;
							case RUN_SAMPLE:
		            			runSample(request.name);
								break;
							case SET_NAME:
								setName(request.name);
								break;
							case SET_SETTING:
								setSetting(request.name, request.value);
								break;
							case UPLOAD_FILE:
								reply.result = uploadFile(request.name, request.contents);
								os.writeObject(reply);
								break;     			
		            		}
	            		}
            		
                    } catch(Exception e) {
                    	System.err.println("Error reading from connection " + e);
						try {
							conn.close();
						} catch (IOException e1) {
							System.err.println("Error closing connection: " + e);
						}
                    }
            		
                } catch(Exception e) {
                	System.err.println("Error accepting connection " + e);
                	break;
                }		
        	}
        	
        	try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    
    /**
     * Present the Bluetooth menu to the user.
     */
    private void bluetoothMenu()
    {
    	// Check if BT initialisation is complete
        if (bt == null) {
        	msg("BT not started");
        	return;
        }
        
        int selection = 0;
        GraphicMenu menu = new GraphicMenu(null,null,4);
        boolean visible = false;
        do
        {
            newScreen("Bluetooth");
            visible = bt.getVisibility();
            System.out.println("Visibility is " + visible);

            lcd.drawString("Visibility", 0, 2);
            lcd.drawString(visible ? "on" : "off", 11, 2);
            menu.setItems(new String[]
                    {
                        "Search/Pair", "Devices", "Visibility", "Change PIN"
                    },
                    new String[]{ICSearch,ICNXT,ICVisibility,ICPIN});
            selection = getSelection(menu,selection);
            switch (selection)
            {
                case 0:
                    bluetoothSearch();
                    break;
                case 1:
                    bluetoothDevices();
                    break;
                case 2:
                	visible = !visible;
                    btVisibility = visible;
                    System.out.println("Setting visibility to " + btVisibility);
					try {
						bt.setVisibility(btVisibility);
					} catch (IOException e) {
						System.err.println("Failed to set visibility: " + e);
					}
                    //updateBTIcon();
                    ind.updateNow();
                    break;
                case 3:
                    bluetoothChangePIN();
                    break;
            }
        } while (selection >= 0);
    }
    
    /**
     * Clears the screen, displays a number and allows user to change
     * the digits of the number individually using the NXT buttons.
     * Note the array of bytes represent ASCII characters, not actual numbers.
     * 
     * @param digits Number of digits in the PIN.
     * @param title The text to display above the numbers.
     * @param number Start with a default PIN. Array of bytes up to 8 length.
     * @return
     */
    private boolean enterNumber(String title, byte[] number, int digits)
    {
        // !! Should probably check to make sure defaultNumber is digits in size
        int curDigit = 0;

        while (true)
        {
            newScreen();
            lcd.drawString(title, 0, 2);
            for (int i = 0; i < digits; i++)
            	lcd.drawChar((char)number[i], i * 2 + 1, 4);
            
            if (curDigit >= digits)
            	return true;
            
            Utils.drawRect(curDigit * 20 + 3, 60, 20, 20);

            int ret = getButtonPress();
            switch (ret)
            {
                case Button.ID_ENTER:
                { // ENTER
                    curDigit++;
                    break;
                }
                case Button.ID_LEFT:
                { // LEFT
                    number[curDigit]--;
                    if (number[curDigit] < '0')
                        number[curDigit] = '9';
                    break;
                }
                case Button.ID_RIGHT:
                { // RIGHT
                    number[curDigit]++;
                    if (number[curDigit] > '9')
                        number[curDigit] = '0';
                    break;
                }
                case Button.ID_ESCAPE:
                { // ESCAPE
                    curDigit--;
                    // Return false if user backs out
                    if (curDigit < 0)
                        return false;
                    break;
                }
            }
        }
    }
 
    /**
     * Allow the user to change the Bluetooth PIN.
     */
    private void bluetoothChangePIN()
    {
        // 1. Retrieve PIN from System properties
        String pinStr = SystemSettings.getStringSetting(pinProperty, "1234");
        int len = pinStr.length();
        byte[] pin = new byte[len];
        for (int i = 0; i < len; i++)
            pin[i] = (byte) pinStr.charAt(i);

        // 2. Call enterNumber() method
        if (enterNumber("Enter NXT PIN", pin, 4))
        {
            // 3. Set PIN in system memory.
        	StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pin.length; i++)
                sb.append((char) pin[i]);
            
            try {
				PrintStream out = new PrintStream(new FileOutputStream("/etc/bluetooth/btpin"));
				out.println(sb.toString());
				out.close();
			} catch (IOException e) {
				System.out.println("Failed to write pin to /etc/bluetooth/btpin: " + e);
			}
            
            // 4. Run startbt to restart the agent with the new pin
        	try {
        		lcd.clear();
        		lcd.drawString("Restarting agent", 0, 1);
				Process p = Runtime.getRuntime().exec("/home/root/lejos/bin/startbt");
				int status = p.waitFor();
				System.out.println("startbt returned " + status);
			} catch (IOException | InterruptedException e) {
				System.err.println("Failed to execute startbt: " + e);
			}
        }
    }
    
    /**
     * Perform the Bluetooth search operation
     * Search for Bluetooth devices
     * Present those that are found
     * Allow pairing
     */
    private void bluetoothSearch()
    {
        newScreen("Searching");
        ArrayList<RemoteBTDevice> devList; 
        //indiBT.incCount();
        devList = null;
        try
        {
        	// 0 means "search for all"
	        try {
				devList = (ArrayList<RemoteBTDevice>) bt.search();
			} catch (IOException e) {
				return;
			}
        }
	    finally
	    {
	    }
        if (devList == null || devList.size() <= 0)
        {
            msg("No devices found");
            return;
        }
        
        String[] names = new String[devList.size()];
        String[] icons = new String[devList.size()];
        for (int i = 0; i < devList.size(); i++)
        {
            RemoteBTDevice btrd = devList.get(i);
            names[i] = btrd.getName();
            //icons[i] = getDeviceIcon(btrd.getDeviceClass());
        }
        GraphicListMenu searchMenu = new GraphicListMenu(names, icons);
        GraphicMenu subMenu = new GraphicMenu(new String[]{"Pair"}, new String[]{ICYes},4);
        int selected = 0;
        do
        {
            newScreen("Found");
            selected = getSelection(searchMenu, selected);
            if (selected >= 0)
            {
                RemoteBTDevice btrd = devList.get(selected);
                newScreen();
                //LCD.bitBlt(
                //	Utils.stringToBytes8(getDeviceIcon(btrd.getDeviceClass()))
                //	, 7, 7, 0, 0, 2, 16, 7, 7, LCD.ROP_COPY);
                lcd.drawString(names[selected], 2, 2);
                lcd.drawString(btrd.getAddress(), 0, 3);
                int subSelection = getSelection(subMenu, 0);
                if (subSelection == 0){
                    newScreen("Pairing");
                    //Bluetooth.addDevice(btrd);
                    // !! Assuming 4 length
                    byte[] pin = { '0', '0', '0', '0' };
                    if (!enterNumber("PIN for " + btrd.getName(), pin, pin.length))
                    	break;
                    lcd.drawString("Please wait...", 0, 6);
                    try {
                    	Bluetooth.getLocalDevice().authenticate(btrd.getAddress(), new String(pin));
                    	// Indicate Success or failure:
                    	lcd.drawString("Paired!      ", 0, 6);
                    } catch (Exception e)
                    {
                    	System.err.println("Failed to pair:" + e);
                    	lcd.drawString("UNSUCCESSFUL  ", 0, 6);
                        //Bluetooth.removeDevice(btrd);
                    }
                    lcd.drawString("Press any key", 0, 7);
                    getButtonPress();
                }
            }
        } while (selected >= 0);
    }

    /**
     * Display all currently known Bluetooth devices.
     */
    private void bluetoothDevices()
    {
        List<RemoteBTDevice> devList;
		try {
			devList = (List<RemoteBTDevice>) Bluetooth.getLocalDevice().search();
		} catch (IOException e) {
			return;
		}
        if (devList.size() <= 0)
        {
            msg("No known devices");
            return;
        }
        
        newScreen("Devices");
        String[] names = new String[devList.size()];
        String[] icons = new String[devList.size()];
        int i=0;
        for (RemoteBTDevice btrd: devList)
        {
            names[i] = btrd.getName();
            i++;
        }

        GraphicListMenu deviceMenu = new GraphicListMenu(names, icons);
        GraphicMenu subMenu = new GraphicMenu(new String[] {"Remove"},new String[]{ICDelete},4);
        int selected = 0;
        do
        {
            newScreen();
            selected = getSelection(deviceMenu, selected);
            if (selected >= 0)
            {
                newScreen();
                RemoteBTDevice btrd = devList.get(selected);
                byte[] devclass = btrd.getDeviceClass();
                lcd.drawString(btrd.getName(), 2, 2);
                lcd.drawString(btrd.getAddress(), 0, 3);
                // TODO device class is overwritten by menu
                // LCD.drawString("0x"+Integer.toHexString(devclass), 0, 4);
                int subSelection = getSelection(subMenu, 0);
                if (subSelection == 0)
                {
                    //Bluetooth.removeDevice(btrd);
                    break;
                }
            }
        } while (selected >= 0);
    }
    
    /**
     * Run the default program (if set).
     */
    private void mainRunDefault()
    {
    	File f = getDefaultProgram();
        if (f == null)
        {
       		msg("No default set");
        }
        else
        {
        	System.out.println("Executing " + f.getPath());
        	ind.suspend();
            exec(JAVA_RUN_JAR + f.getPath());
        	ind.resume();
        }
    }
    
    /**
     * Present the system menu.
     * Allow the user to format the filesystem. Change timeouts and control
     * the default program usage.
     */
    private void systemMenu()
    {
        String[] menuData = {"Delete all", "", "Auto Run", "Change name", "NTP host", "Suspend menu", "Unset default"};
        String[] iconData = {ICFormat,ICSleep,ICAutoRun,ICDefault,ICDefault,ICDefault,ICDefault};
        boolean rechargeable = false;
        GraphicMenu menu = new GraphicMenu(menuData,iconData,4);
        int selection = 0;
        do {
            newScreen("System");
            lcd.drawString("RAM", 0, 1);
            lcd.drawInt((int) (Runtime.getRuntime().freeMemory()), 11, 1);
            lcd.drawString("Battery", 0, 2);
            int millis = LocalEV3.ev3.getPower().getVoltageMilliVolt() + 50;
            lcd.drawInt((millis - millis % 1000) / 1000, 11, 2);
            lcd.drawString(".", 12, 2);
            lcd.drawInt((millis % 1000) / 100, 13, 2);
            if (rechargeable)
            	lcd.drawString("R", 15, 2);
            menuData[1] = "Sleep time: " + (timeout == 0 ? "off" : String.valueOf(timeout));
            File f = getDefaultProgram();
            if (f == null){
            	menuData[6] = null;
            	iconData[6] = null;
            }
            menu.setItems(menuData,iconData);
            selection = getSelection(menu, selection);
            switch (selection)
            {
                case 0:              	
                    if (getYesNo("Delete all files?", false) == 1) {
                    	File dir = new File("/home/lejos/programs");
                        for (String fn : dir.list()) {
                            File aFile = new File(dir,fn);
                            System.out.println("Deleting " + aFile.getPath());
                            aFile.delete();
                        }
                    }
                    break;
                case 1:
                	System.out.println("Timeout = " + timeout);
                	System.out.println("Max sleep time = " + maxSleepTime);
                    //timeout++;
                    if (timeout > maxSleepTime) timeout = 0;
                    Settings.setProperty(sleepTimeProperty, String.valueOf(timeout));
                    break;
                case 2:
                    systemAutoRun();
                    break;
                case 3:
                	String newName = new Keyboard().getString();
                	
                	if (newName != null) {
                		setName(newName);
                	}
                	break;
                case 4:
                	String host = new Keyboard().getString();
                	
                	if(host != null) {
                		Settings.setProperty(ntpProperty, host);
                	}
                	break;
                case 5:
                	ind.suspend();
                	lcd.clear();
                	lcd.refresh();
                	lcd.setAutoRefresh(false);
                	System.out.println("Menu suspended");
                    while(true) {
                        int b = Button.getButtons(); 
                        if (b == 6) break;
                        Delay.msDelay(200);
                    }
                    lcd.setAutoRefresh(true);
                    lcd.clear();
                    lcd.refresh();            	
                	ind.resume();
                	System.out.println("Menu resumed");
                	break;
                case 6:
                    Settings.setProperty(defaultProgramProperty, "");
                    Settings.setProperty(defaultProgramAutoRunProperty, "");
                    selection = 0;
                    break;
            }
        } while (selection >= 0);
    }
    
    /**
     * Present details of the default program
     * Allow the user to specify run on system start etc.
     */
    private void systemAutoRun()
    {
    	File f = getDefaultProgram();
    	if (f == null)
    	{
       		msg("No default set");
       		return;
    	}
    	
    	newScreen("Auto Run");
    	lcd.drawString("Default Program:", 0, 2);
    	lcd.drawString(f.getName(), 1, 3);
    	
    	String current = Settings.getProperty(defaultProgramAutoRunProperty, "");
        int selection = getYesNo("Run at power up?", current.equals("ON"));
        if (selection >= 0)
        	Settings.setProperty(defaultProgramAutoRunProperty, selection == 0 ? "OFF" : "ON");
    }
    
    /**
     * Ask the user for confirmation of an action.
     * @param prompt A description of the action about to be performed
     * @return 1=yes 0=no < 0 escape
     */
    private int getYesNo(String prompt, boolean yes)
    {
//    	lcd.bitBlt(null, 178, 64, 0, 0, 0, 64, 178, 64, CommonLCD.ROP_CLEAR);
        GraphicMenu menu = new GraphicMenu(new String[]{"No", "Yes"},new String[]{ICNo,ICYes},5,prompt,4);
        return getSelection(menu, yes ? 1 : 0);
    }
    
    /**
     * Get the default program as a file
     */
    private static File getDefaultProgram()
    {
    	String file = Settings.getProperty(defaultProgramProperty, "");
    	if (file != null && file.length() > 0)
    	{
    		File f = new File(file);
        	if (f.exists())
        		return f;
        	
           	Settings.setProperty(defaultProgramProperty, "");
           	Settings.setProperty(defaultProgramAutoRunProperty, "OFF");
    	}
    	return null;
    }

    /**
     * Display the sound menu.
     * Allow the user to change volume and key click volume.
     */
    private void soundMenu()
    {
        String[] soundMenuData = new String[]{"Volume:    ", "Key volume: ", "Key freq: ", "Key length: "};
        String[] soundMenuData2 = new String[soundMenuData.length];
        GraphicMenu menu = new GraphicMenu(soundMenuData2, new String[] {ICSound,ICSound, ICSound, ICSound},3);
        int[][] Volumes =
        {
            {
                Sound.getVolume() / 10, 784, 250, 0
            },
            {
                Button.getKeyClickVolume() / 10, Button.getKeyClickTone(1) / 200, Button.getKeyClickLength() / 10, 0
            },
            {
                Button.getKeyClickVolume() / 10, Button.getKeyClickTone(1) / 200, Button.getKeyClickLength() / 10, 0
            },
            {
                Button.getKeyClickVolume() / 10, Button.getKeyClickTone(1) / 200, Button.getKeyClickLength() /10, 0
            }
        };
        int selection = 0;
        // Make a note of starting values so we know if they change
        for (int i = 0; i < Volumes.length; i++)
            Volumes[i][3] = Volumes[i][(i == 0 ? 0 : i-1)];
        // remember and Turn off tone for the enter key
        int tone = Button.getKeyClickTone(Button.ID_ENTER);
        Button.setKeyClickTone(Button.ID_ENTER, 0);
        do {
            newScreen("Sound");
            for (int i = 0; i < Volumes.length; i++)
                soundMenuData2[i] = soundMenuData[i] + formatVol(Volumes[i][(i == 0 ? 0 : i -1)]);
            menu.setItems(soundMenuData2);
            selection = getSelection(menu, selection);
            if (selection >= 0)
            {
                Volumes[selection][(selection == 0 ? 0 : selection -1)]++;
                Volumes[selection][(selection == 0 ? 0 : selection -1)] %= 11;
                if (selection > 1 && Volumes[selection][(selection == 0 ? 0 : selection -1)] == 0)
                	Volumes[selection][(selection == 0 ? 0 : selection -1)] = 1;
                if (selection == 0)
                {
                    Sound.setVolume(Volumes[0][0] * 10);
                    Sound.playTone(1000, Volumes[selection][1], Volumes[selection][2]);
                }
                else
                    Sound.playTone(Volumes[selection][1] * 200, Volumes[selection][2] * 10, Volumes[selection][0] * 10);
            }
        } while (selection >= 0);
        // Make sure key click is back on and has new volume
        Button.setKeyClickTone(Button.ID_ENTER, tone);
        Button.setKeyClickVolume(Volumes[1][0] * 10);
        // Save in settings
        if (Volumes[0][0] != Volumes[0][3])
            Settings.setProperty(Sound.VOL_SETTING, String.valueOf(Volumes[0][0] * 10));
        if (Volumes[1][0] != Volumes[1][3])
            Settings.setProperty(Button.VOL_SETTING, String.valueOf(Volumes[1][0] * 10));
        if (Volumes[2][1] != Volumes[2][3])
            Settings.setProperty(Button.FREQ_SETTING, String.valueOf(Volumes[2][1] * 200));
        if (Volumes[3][2] != Volumes[3][3])
            Settings.setProperty(Button.LEN_SETTING, String.valueOf(Volumes[3][2] * 10));
    }
    
    /**
     * Format a string for use when displaying the volume.
     * @param vol Volume setting 0-10
     * @return String version.
     */
    private static String formatVol(int vol)
    {
        if (vol == 0)
            return "mute";
        if (vol == 10)
            return "10";        
        return new StringBuilder().append(' ').append(vol).toString();
    }
    
    /**
     * Display system version information.
     */
    private void displayVersion()
    {
        newScreen("Version");
        lcd.drawString("leJOS:", 0, 2);
        lcd.drawString(version, 6, 2);
        lcd.drawString("Menu:", 0, 3);
        lcd.drawString(Utils.versionToString(Config.VERSION),6, 3);
        getButtonPress();
    }
    
    /**
     * Read a button press.
     * If the read timesout then exit the system.
     * @return The bitcode of the button.
     */
    private int getButtonPress()
    {
        int value = Button.waitForAnyPress(timeout*60000);
        if (value == 0) shutdown();
        return value;
    }
	
    /**
     * Present the menu for a single file.
     * @param file
     */
    private void fileMenu(File file)
    {
        String fileName = file.getName();
        String ext = Utils.getExtension(fileName);
        int selectionAdd;
        String[] items;
        String[] icons;
        if (ext.equals("jar"))
        {
        	selectionAdd = 0;
            items = new String[]{"Execute program", "LCD Debug", "Debug program", "Set as Default", "Delete file"}; 
            icons = new String[]{ICProgram,ICProgram,ICProgram,ICDefault,ICDelete};
        }
        else if (ext.equals("wav"))
        {
        	selectionAdd = 10;
        	items = new String[]{"Play sample", "Delete file"};
        	icons = new String[]{ICSound,ICDelete};
        }
        else
        {
        	selectionAdd = 20;
        	items = new String[]{"Delete file"};
        	icons = new String[]{ICDelete};
        }
        newScreen();
        lcd.drawString("Size:", 0, 2);
        lcd.drawString(Long.toString(file.length()), 5, 2);
        GraphicMenu menu = new GraphicMenu(items,icons,3,fileName,1);
        int selection = getSelection(menu, 0);
        if (selection >= 0)
        {
	        switch(selection + selectionAdd)
	        {
	            case 0:
	            	System.out.println("Running program: " + file.getPath());
	            	ind.suspend();
	            	exec(JAVA_RUN_JAR + file.getPath());
	            	ind.resume();
	                break;
	            case 1:
	            	System.out.println("Running with System output to LCD: " + file.getPath());
	            	PrintStream origOut = System.out, origErr = System.err;
	            	PrintStream lcdOut = new PrintStream(new LCDOutputStream());
	            	ind.suspend();
	            	System.setOut(lcdOut);
	            	System.setErr(lcdOut);
	            	exec(JAVA_RUN_JAR + file.getPath());
	            	System.setOut(origOut);
	            	System.setOut(origErr);
	            	ind.resume();
	                break;
	            case 2:
	            	System.out.println("Debugging program: " + file.getPath());
	            	ind.suspend();
	            	exec(JAVA_DEBUG_JAR + file.getPath());
	            	ind.resume();
	                break;
	            case 3:
	            	Settings.setProperty(defaultProgramProperty, file.getPath());
	            	break;
	            case 10:
	            	System.out.println("Playing " + file.getPath());
	                Sound.playSample(file);
	                break;
	            case 4:
	            case 11:
	            case 20:
	                file.delete();
	                break;
	        }
        }
    }
    
    /**
     * Execute a program and display its output to System.out and error stream to System.err
     */
    private static void exec(String program) {
        try {
        	lcd.clear();
        	lcd.refresh();
        	lcd.setAutoRefresh(false);
            Process p = Runtime.getRuntime().exec(program);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader err= new BufferedReader(new InputStreamReader(p.getErrorStream()));
            
            EchoThread echoIn = new EchoThread(input, System.out);
            EchoThread echoErr = new EchoThread(err, System.err);
            
            echoIn.start();
            echoErr.start();
            
            while(true) {
              int b = Button.getButtons(); 
              if (b == 6) {
            	  System.out.println("Killing the process");
            	  p.destroy(); 
            	  // reset motors after program is aborted
            	  resetMotors();
                  break;
              }
              if (!echoIn.isAlive() && !echoErr.isAlive()) break;           
              Delay.msDelay(200);
            }
            System.out.println("Waiting for process to die");;
            p.waitFor();
            System.out.println("Program finished");
            lcd.setAutoRefresh(true);
            lcd.clear();
            lcd.refresh();
          }
          catch (Exception e) {
            System.err.println("Failed to execute program: " + e);
          } 	
    }
   
    /**
     * Display the files in the file system.
     * Allow the user to choose a file for further operations.
     */
    private void filesMenu()
    {
    	GraphicListMenu menu = new GraphicListMenu(null,null);
    	System.out.println("Finding files ...");
        int selection = 0;
        do {
            File[] files = (new File("/home/lejos/programs")).listFiles();
            int len = 0;
            for (int i = 0; i < files.length && files[i] != null; i++)
                len++;
            if (len == 0)
            {
                msg("No files found");
                return;
            }
            newScreen("Files");
            String fileNames[] = new String[len];
            String[] icons = new String[len];
            for (int i = 0; i < len; i++){
                fileNames[i] = files[i].getName();
                String ext = Utils.getExtension(files[i].getName());
               if (ext.equals("jar"))
                	icons[i] = ICMProgram;
                else if (ext.equals("wav"))
                	icons[i] = ICMSound;
                else
                	icons[i] = ICMFile;
            }
            menu.setItems(fileNames,icons);
            selection = getSelection(menu, selection);
            if (selection >= 0)
                fileMenu(files[selection]);
        } while (selection >= 0);
    }
    
    /**
     * Display the samples in the file system.
     * Allow the user to choose a file for further operations.
     */
    private void samplesMenu()
    {
    	GraphicListMenu menu = new GraphicListMenu(null,null);
    	System.out.println("Finding files ...");
        int selection = 0;
        do {
            File[] files = (new File("/home/root/lejos/samples")).listFiles();
            int len = 0;
            for (int i = 0; i < files.length && files[i] != null; i++)
                len++;
            if (len == 0)
            {
                msg("No samples found");
                return;
            }
            newScreen("Samples");
            String fileNames[] = new String[len];
            String[] icons = new String[len];
            for (int i = 0; i < len; i++){
                fileNames[i] = files[i].getName();
                String ext = Utils.getExtension(files[i].getName());
               if (ext.equals("jar"))
                	icons[i] = ICMProgram;
                else if (ext.equals("wav"))
                	icons[i] = ICMSound;
                else
                	icons[i] = ICMFile;
            }
            menu.setItems(fileNames,icons);
            selection = getSelection(menu, selection);
            if (selection >= 0)
                fileMenu(files[selection]);
        } while (selection >= 0);
    }
    
    /**
     * Start a new screen display using the current title.
     */
    private void newScreen()
    {
    	lcd.clear();
        ind.updateNow();
    }
    
    /**
     * Start a new screen display.
     * Clear the screen and set the screen title.
     * @param title
     */
    private void newScreen(String title)
    {
        indiBA.setTitle(title);
        newScreen();
    }
    
    /**
     * Display a status message
     * @param msg
     */
    private void msg(String msg)
    {
        newScreen();
        lcd.drawString(msg, 0, 2);
        long start = System.currentTimeMillis();
        int button;
        int buttons = Button.readButtons();
        do
        {
        	Thread.yield();
        	
        	int buttons2 = Button.readButtons();
        	button = buttons2 & ~buttons;
        } while (button != Button.ID_ESCAPE && System.currentTimeMillis() - start < 2000);
    }
    
    /**
     * Obtain a menu item selection
     * Allow the user to make a selection from the specified menu item. If a
     * power off timeout has been specified and no choice is made within this
     * time power off the NXT.
     * @param menu Menu to display.
     * @param cur Initial item to select.
     * @return Selected item or < 0 for escape etc.
     */
    private int getSelection(GraphicMenu menu, int cur)
    { 	
        int selection;
        // If the menu is interrupted by another thread, redisplay
        do {
        	selection = menu.select(cur, timeout*60000);
        } while (selection == -2);
        
        if (selection == -3)
            shutdown();
    	
        return selection;
    }
    
    /**
     * Shut down the EV3
     */
    void shutdown() {
    	System.out.println("Shutting down the EV3");
        ind.suspend();
    	exec("init 0");
    	lcd.drawString("  Shutting down", 0, 6);
        lcd.refresh();
    }
    
    class PipeReader extends Thread {
    	
    	@Override
		public synchronized void run()
    	{
    		try {
				InputStream is = new FileInputStream("/home/root/lejos/bin/utils/menufifo");
				
	    		while(true) {
	    			int c = is.read();
	    			if (c < 0) {
	    				Delay.msDelay(200);
	    			} else {
	    				System.out.println("Read from fifo: " + c + " " + ((char) c));
	    				
	    				if (c == 's') {
	                    	ind.suspend();
	                    	lcd.clear();
	                    	lcd.refresh();
	                    	lcd.setAutoRefresh(false);
	                    	System.out.println("Menu suspended");
	    				} else if (c == 'r') {
	    					lcd.setAutoRefresh(true);
	                    	ind.resume();
	                    	System.out.println("Menu resumed");
	    				}
	    			}
	    		}
	    		
			} catch (IOException e) {
				System.err.println("Failed to read from fifo: " + e);
				return;
			}
    	}	
    }
    
    class BroadcastThread extends Thread {
    	
    	@Override
		public synchronized void run()
    	{
    		while(true) {
    			Broadcast.broadcast(hostname);
    			Delay.msDelay(1000);
    		}
    	}
    }
    
    /**
     * Manage the top line of the display.
     * The top line of the display shows battery state, menu titles, and I/O
     * activity.
     */
	class IndicatorThread extends Thread
    {
		public IndicatorThread()
    	{
    		super();
            setDaemon(true);
    	}
    	
    	@Override
		public synchronized void run()
    	{
    		try
    		{
	    		while (true)
	    		{
	    			long time = System.currentTimeMillis();
	    			
	    			byte[] buf = lcd.getDisplay();
	    			// TODO: Fix this
	    			// clear not necessary, pixels are always overwritten
	    			for (int i=0; i<lcd.getWidth(); i++)
	    				buf[i] = 0;	    			
	    			indiBA.draw(time, buf);
	    			lcd.refresh();
    			
    				// wait until next tick
    				time = System.currentTimeMillis();
    				this.wait(Config.ANIM_DELAY - (time % Config.ANIM_DELAY));
    			}
    		}
    		catch (InterruptedException e)
    		{
    			//just terminate
    		}
    	}
    	
    	/**
    	 * Update the indicators
    	 */
    	public synchronized void updateNow()
    	{
    		this.notifyAll();
    	}
    }
	
	/**
	 * Get all the IP addresses for the device
	 */
    public static List<String> getIPAddresses()
    {
        List<String> result = new ArrayList<String>();
        Enumeration<NetworkInterface> interfaces;
        try
        {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e)
        {
            System.err.println("Failed to get network interfaces: " + e);
            return null;
        }
        while (interfaces.hasMoreElements()){
            NetworkInterface current = interfaces.nextElement();
            try
            {
                if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
            } catch (SocketException e)
            {
            	System.err.println("Failed to get network properties: " + e);
            }
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()){
                InetAddress current_addr = addresses.nextElement();
                if (current_addr.isLoopbackAddress()) continue;
                result.add(current_addr.getHostAddress());
            }
        }
        return result;
    } 
 
	@Override
	public void runProgram(String programName) {
    	ind.suspend();
    	exec(JAVA_RUN_JAR + "/home/lejos/programs/" + programName + ".jar");
    	ind.resume();
	}

	@Override
	public boolean deleteFile(String fileName) {
		File f = new File("/home/lejos/programs/" + fileName);
		return f.delete();
	}

	@Override
	public String[] getProgramNames() {
		File[] files = (new File("/home/lejos/programs")).listFiles();
		String[] fileNames = new String[files.length];
		for(int i=0;i<files.length;i++) {
			fileNames[i] = files[i].getName();
		}
		return fileNames;
	}

	@Override
	public void runSample(String programName) {
    	ind.suspend();
    	exec(JAVA_RUN_JAR + "/home/root/lejos/samples/" + programName + ".jar");
    	ind.resume();
	}

	@Override
	public void debugProgram(String programName) {
    	ind.suspend();
    	exec(JAVA_DEBUG_JAR + "/home/lejos/programs/" + programName + ".jar");
    	ind.resume();
	}

	@Override
	public String[] getSampleNames() {
		File[] files = (new File("/home/root/lejos/samples")).listFiles();
		String[] fileNames = new String[files.length];
		for(int i=0;i<files.length;i++) {
			fileNames[i] = files[i].getName();
		}
		return fileNames;
	}

	@Override
	public long getFileSize(String filename) {
		return new File(filename).length();
	}

	@Override
	public boolean uploadFile(String fileName, byte[] contents) {
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			out.write(contents);
			out.close();
			return true;
		} catch (IOException e) {
			System.out.println("Failed to upload file: " + e);
			return false;
		}
	
	}
	
	private void wifiMenu() {
    	GraphicListMenu menu = new GraphicListMenu(null,null);
    	System.out.println("Finding access points ...");
    	LocalWifiDevice wifi = Wifi.getLocalDevice("wlan0");
    	String[] names;
    	try {
    		names = wifi.getAccessPointNames();
    	} catch (Exception e) {
    		System.err.println("Exception getting access points" +e);
    		names = new String[0];
    	}
        int selection = 0;
        
        do {           
            int len = names.length;
            if (len == 0)
            {
                msg("No Access Points found");
                return;
            }
            newScreen("Access Pts");

            menu.setItems(names,null);
            selection = getSelection(menu, selection);
            if (selection >= 0) {
            	System.out.println("Access point is " + names[selection]);
            	Keyboard k = new Keyboard();
            	String pwd = k.getString();
            	if (pwd != null) {
                   	System.out.println("Password is " + pwd);
                	WPASupplicant.writeConfiguration("wpa_supplicant.txt",  "wpa_supplicant.conf",  names[selection], pwd);
                	startWlan();
            	}
             	selection = -1;
            }
        } while (selection >= 0);		
	}
	
	/**
	 * Reset all motors to zero power and float state
	 * and reset tacho counts
	 */
	public static void resetMotors() {
		for(String portName: new String[]{"A","B","C","D"}) {
			Port p = LocalEV3.get().getPort(portName);
			TachoMotorPort mp = p.open(TachoMotorPort.class);
			mp.controlMotor(0, TachoMotorPort.FLOAT);
			mp.resetTachoCount();
			mp.close();
		}
	}

	@Override
	public byte[] fetchFile(String fileName) {
		File f = new File(fileName);
		FileInputStream in;
		try {
			in = new FileInputStream(f);
			byte[] data = new byte[(int)f.length()];
		    in.read(data);
		    in.close();
		    return data;
		} catch (IOException e) {
			System.err.println("Failed to fetch file: " + e);
			return null;
		}

	}

	@Override
	public String getSetting(String setting) {
		return Settings.getProperty(setting, null);
	}

	@Override
	public void setSetting(String setting, String value) {
		Settings.setProperty(setting, value);
	}

	@Override
	public void deleteAllPrograms() {
    	File dir = new File("/home/lejos/programs");
        for (String fn : dir.list()) {
            File aFile = new File(dir,fn);
            System.out.println("Deleting " + aFile.getPath());
            aFile.delete();
        }
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getMenuVersion() {
		return Utils.versionToString(Config.VERSION);
	}

	@Override
	public String getName() {
		return hostname;
	}

	@Override
	public void setName(String name) {
		hostname = name;
		
		// Write host to /etc/hostname
		try {
			PrintStream out = new PrintStream(new FileOutputStream("/etc/hostname"));
			out.println(name);
			out.close();
		} catch (FileNotFoundException e) {
			System.err.println("Failed to write to /etc/hostname: " + e);
		}
		
    	try {
			Process p = Runtime.getRuntime().exec("hostname " + hostname);
			int status = p.waitFor();
			System.out.println("hostname returned " + status);
		} catch (IOException | InterruptedException e) {
			System.err.println("Failed to execute hostname: " + e);
		}
		
		startWlan();
	}
	
	private void startWlan() {
    	try {
			Process p = Runtime.getRuntime().exec("/home/root/lejos/bin/startwlan");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader err= new BufferedReader(new InputStreamReader(p.getErrorStream()));
            PrintStream lcdStream = new PrintStream(new LCDOutputStream());
            
            EchoThread echoIn = new EchoThread(input, lcdStream);
            EchoThread echoErr = new EchoThread(err, lcdStream);
            
            ind.suspend();
            lcd.clear();
            lcdStream.println("Restarting wlan\n");
            
            echoIn.start();
            echoErr.start();
            
			int status = p.waitFor();
			System.out.println("startwlan returned " + status);
			// Get IP addresses again
			ips = getIPAddresses();
			lcd.clear();
        	ind.resume();
		} catch (IOException | InterruptedException e) {
			System.err.println("Failed to execute startwlan: " + e);
		}
	}
}
