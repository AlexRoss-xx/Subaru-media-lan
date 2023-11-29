#include "LanNAVI.h"
#include "Android.h"
#include "Config.h"
#include <MemoryFree.h>

byte sendInit = 0;

int cntset = 0;

int startSATcnt = 0;

void LanNAVI::begin() {
	//lan.deviceAddress = 0x140;
	status = EMPTY; //lan device status
	initStatus = INIT_NONE; // init commands 
	controllerStatus = CONTROLLER_UNREG;
	sendInit = 0;
	//sendGetInit = 15;
}

// Use the last received message to determine the corresponding action ID, store it in avclan object
void LanNAVI::getActionID() {
	lan.event = EV_QUEUE_PUSH;

	if (lan.masterAddress != 0x130) {
		lan.actionID = ACT_EXT;
		if (status != REGISTER)
			lan.event = EV_NOEVENT;
	}
	else
	{
		bool compare = lan.slaveAddress != 0x140 && lan.slaveAddress != 0xFFF && lan.slaveAddress != 0x230;
#if  defined(SAT)
		compare = compare && lan.slaveAddress != 0x260;
#endif

		if (compare){
			lan.actionID = ACT_NONE;
		}
		else {
			lan.actionID = lan.getActionID(mtRegInMaskedMain, mtRegInMaskedMainSize);

			if (lan.actionID == ACT_NONE)
				lan.actionID = lan.getActionID(constInComeMessages2, constInComeMessages2Size);

			if (lan.actionID == ACT_NONE )
				lan.actionID = lan.getActionID(constInComeMessages, constInComeMessagesSize);
			
			if (lan.actionID == ACT_NONE)
				lan.actionID = lan.getActionID(mtMaskedMain, mtMaskedMainSize);

			//if (controllerStatus == CONTROLLER_UNREG)
			if (lan.actionID == ACT_NONE)
				lan.actionID = lan.getActionID(inMesagesIgnorePosINIT, inMesagesIgnorePosINITSize);

			if (controllerStatus == CONTROLLER_UNREG)
				if (lan.actionID == ACT_NONE)
					lan.actionID = lan.getActionID(inMesagesIgnorePos, inMesagesIgnorePosSize);
		}
	}
};

// Processing actions
// For registration  it is  very important  to  send messages into  the main unit according specific sequences
void LanNAVI::processAction(IncomeActionID ActionID) {
	byte r;
	byte hid; // unique varible that send  headunit for registration
	byte sent = 0;   // some registration messages contains several messages one after onother

#ifdef TRACE
	Serial.print("processAction:");
	Serial.println(ActionID);

	Serial.print("initStatus:");
	Serial.println(initStatus);
	Serial.print("controllerStatus:");
	Serial.println(controllerStatus);
	Serial.print("status:");
	Serial.println(status);
#endif

   switch (ActionID) {
	case ACT_LAN_STATUS:
		lan.event = EV_NOEVENT;
		if (controllerStatus == CONTROLLER_UNREG && initStatus == INIT_FINISH) {
			controllerStatus = CONTROLLER_REGISTERED;
			status = REGISTER;
			lan.event = EV_STATUS;
		}
		break;
		//< b 130 FFF 3 10 F0 01 
	case ACT_REGISTER: // register device
		if (lan.headAddress == 0)
			lan.headAddress = lan.masterAddress;
		hid = lan.message[1];
		lan.loadMessage(&CmdRegisterHU);
		lan.message[1] = hid;
		lan.sendMessage();

#ifdef SAT
		lan.loadMessage(&CmdRegisterSAT, lan.deviceAddressSAT);
		lan.message[1] = hid;
		lan.sendMessage();
#endif 	

#ifdef CDC
		lan.loadMessage(&CmdRegisterCDC, lan.deviceAddressCDC);
		lan.message[1] = hid;
		lan.sendMessage();
#endif
		status = INIT;
		lan.event = EV_NOEVENT;
		break;

	case ACT_INIT_B0:
	case ACT_INIT_B1:
		lan.event = EV_NOEVENT;
		break;

	case ACT_INIT_HU1:
	case ACT_INIT_HU:
		initStatus = INIT_STARTED;
		lan.event = EV_NOEVENT;
		break;

	case ACT_CmdSettingHU_SET_MODE:            // 130 FFF C 60 c0 00 0e ff ....	 
		break;

	case ACT_CmdSettingHU_MODE:                // 130 FFF C 60 c0 00 4f ff  ...
		if (controllerStatus == CONTROLLER_UNREG) {
			r = lan.sendMessage(&CmdGetSettingHUEnd);
			// devStatus = REG;
			lan.event = EV_NOEVENT;
		}

		//else {
			// lan.event = EV_NONE;
		//}
		// Serial.println("HU MODE");
		break;

#ifdef SAT
	case ACT_INIT_SAT0:
		lan.event = EV_NOEVENT;
		lan.loadMessage(&CmdRegisterSAT1, 0x260);
		lan.sendMessage();
		// sent = 1;
		break;

	case ACT_INIT_SAT1:
		lan.event = EV_NOEVENT;
		lan.loadMessage(&CmdRegisterSAT2, 0x260);
		lan.sendMessage();
		// sent = 1;
		break;

	case ACT_INIT_SAT2:
		lan.event = EV_NOEVENT;
		lan.loadMessage(&CmdRegisterSAT3, 0x260);
		lan.sendMessage();
		break;

	case ACT_CmdSettingHU_SET_MODE_SAT:
		if (cntset == 0)
		{
			lan.event = EV_NOEVENT;
			cntset = cntset + 1;
		}
		else if (cntset == 2) {
			startSATcnt = 1;
			android.PutMessage(1, lan.slaveAddress, lan.dataSize, lan.message);
		}
		else {
			cntset = cntset + 1;
		}
		break;

	case ACT_SAT_UP:
		android.PutMessage(1, lan.slaveAddress, lan.dataSize, lan.message);
		lan.event = EV_NOEVENT;
		lan.loadMessage(&CmdSAT_UP, 0x260);
		lan.sendMessage();
		break;
#endif

	case ACT_IGNORE:		 
		lan.event = EV_NOEVENT;		 
		break;

	default:
		break;
	}

	if (controllerStatus == CONTROLLER_UNREG) {
		switch (lan.event) {
		case EV_QUEUE_PUSH:
			if (lan.slaveAddress != 0x130) {
				//android.PutMessage(1, lan.slaveAddress, lan.dataSize, lan.message);  // !!!!
				lan.event = EV_NOEVENT;
			}
			break;
		default:
			break;
		};

		InitDevice();
	}	 

#ifdef SAT
	if(startSATcnt>0)
	startSAT();
#endif // USA	

	if (ActionID == ACT_EXT)
		if (status != REGISTER)
			lan.event = EV_NOEVENT;

	sendReInit();
}



void  LanNAVI::InitDevice() {
#ifdef TRACE
	Serial.print("initDevice:");
	Serial.println(initStatus);
#endif // TRACE

	if (initStatus == INIT_STARTED) {
		if (status == INIT) {
			//sendInit1();

			lan.sendMessage(&CmdGetSettingHU11);
			controllerStatus = CONTROLLER_REGISTERED;
			status = REGISTER;
			initStatus = INIT_FINISH;
		}
	}
	else if (initStatus == INIT_SETMODE) {
		//sendInit3();
		initStatus = INIT_FINISH;
	}
	//else if (initStatus == INIT_FINISH) {
		//	lan.sendMessage(&CmdGetSettingHU11);
		//	controllerStatus = CONTROLLER_REGISTERED;
		//	status = REGISTER;		 
		//	Serial.println("init finished");

	//}
}

// 
// Commented  lines  just for revese  enginering  
void LanNAVI::sendInit1() {
	int r;

	//< d 140 130 3 40 02 10 
	//< d 140 130 3 40 06 10
	//< d 140 260 3 40 42 10
	//< d 140 130 3 40 C0 10
	switch (sendInit) {
		//case 0:
		//	//lan.sendMessage(&CmdGetSettingHU0);
		//	break;
		//case 1: // register device
		//	//lan.sendMessage(&CmdGetSettingHU1);
		//	break;
		//case 2:
		//	//lan.sendMessage(&CmdGetSettingHU2);
		//	break;
		//case 3:            // 130 FFF C 60 c0 00 0e ff ....
		//	//lan.sendMessage(&CmdGetSettingHU3);		
		//	break;
	case 0:
		//	lan.sendMessage(&CmdGetSettingHU4);
		//break;
	//case 1: // register device
	//	lan.sendMessage(&CmdGetSettingHU5);
	//	break;
	//case 2:
		//lan.sendMessage(&CmdGetSettingHUSound);
	//	lan.sendMessage(&CmdGetSettingHU6);
	//	break;
	//case 3:
		//	lan.sendMessage(&CmdGetSettingHU7);
	//	break;
	//case 4:
		//lan.sendMessage(&CmdGetSettingGetChangerInfo);		 
	//	break;
	//case 5:            // 130 FFF C 60 c0 00 0e ff ....
		//lan.sendMessage(&CmdGetSettingGetChangerTable);		 
	//	break;
	//case 6:
		//	lan.sendMessage(&CmdGetSettingGetStationsTableFM1);			 
	//	break;
	//case 7:
		//	lan.sendMessage(&CmdGetSettingGetStationsTableFM2);		 
	//	break;
	//case 8:

#ifdef EU
		//lan.sendMessage(&CmdGetSettingGetStationsTableFM3);
#endif // EU units  have  3 FM mode
	//	break;
	//case 9: // register device
	//	lan.sendMessage(&CmdGetSettingGetStationsTableAM);
	//	break;
	//case 10:
		//	lan.sendMessage(&CmdGetSettingGetChangerInfo);
	//break;
	// case 11:            // 130 FFF C 60 c0 00 0e ff ....
	//	lan.sendMessage(&CmdGetSettingGetChangerTable);		 
		status = GetSettingHUCompleted;
		initStatus = INIT_SETMODE;
		break;
	default:
		break;
	}

	sendInit = sendInit + 11;
}

void LanNAVI::sendReInit() {
	  
	if (sendGetInit <= 14) {
		//status = REGISTER;
		switch (sendGetInit) {
		case 0:
			lan.sendMessage(&CmdGetSettingHUCurrentSation);
			//	lan.sendMessage(&CmdGetSettingHU6);
			break;
		case 1:
			lan.sendMessage(&CmdGetSettingHUSound);
			//	lan.sendMessage(&CmdGetSettingHU6);
			break;
		case 2:
			lan.sendMessage(&CmdGetSettingGetChangerInfo);
			break;
			//case 6:            // 130 FFF C 60 c0 00 0e ff ....
				//lan.sendMessage(&CmdGetSettingGetChangerTable);
			//	break;
		case 3:
			lan.sendMessage(&CmdGetSettingGetStationsTableFM1);
			break;
		case 4:
			lan.sendMessage(&CmdGetSettingGetStationsTableFM2);
			break;
		case 5:
#ifdef EU
			lan.sendMessage(&CmdGetSettingGetStationsTableFM3);
#endif // EU		
			break;
		case 6:
			lan.sendMessage(&CmdGetSettingHUMode);			
			break;
		case 7:
			lan.sendMessage(&CmdGetSettingHUSound);
			//	lan.sendMessage(&CmdGetSettingHU6);
			break;
		case 8:
			lan.sendMessage(&CmdGetSettingGetStationsTableFM1);
			break;
		case 9:
			lan.sendMessage(&CmdGetSettingGetStationsTableFM2);
			break;
		case 10:
#ifdef EU
			lan.sendMessage(&CmdGetSettingGetStationsTableFM3);
			break;
#endif //EU				
		case 11:
			lan.sendMessage(&CmdGetSettingHUMode);
			break;
		case 12:
			lan.sendMessage(&CmdGetSettingHUCurrentSation);			
			break;
		case 13:
			lan.sendMessage(&CmdGetSettingHUMode);
			break;
		case 14:
			lan.sendMessage(&CmdGetSettingHUMode);
			break;
		default:
			break;
		}

		sendGetInit = sendGetInit + 1;
		 
	}
}


void LanNAVI::startSAT() {
	if (startSATcnt == 1) {
		lan.loadMessage(&CmdRegisterSAT4, 0x260);
		lan.sendMessage();
		startSATcnt = startSATcnt + 1;
	}
	else if (startSATcnt == 2) {
		lan.loadMessage(&CmdRegisterSAT5, 0x260);
		lan.sendMessage();
		lan.loadMessage(&CmdRegisterSAT6, 0x260);
		lan.sendMessage();
		lan.loadMessage(&CmdRegisterSAT7, 0x260);
		lan.sendMessage();
		startSATcnt = startSATcnt + 1;
	}
	else if (startSATcnt == 3)
		startSATcnt = startSATcnt + 1;
	else if (startSATcnt == 4) {
		lan.loadMessage(&CmdRegisterSAT8, 0x260);
		lan.sendMessage();
		startSATcnt = 0;
		cntset = 0;
	}
}


void LanNAVI::processEvent(EventID EventID) {

}

LanNAVI lanDevice;