#include "Lan.h"
#include "Timer.h"
#include "Config.h"
#include "Arduino.h"
#include "Messages.h"
#include <avr/pgmspace.h>

// AVCLan driver & timer2 init, 
void  LanDrv::begin() {

	cbi(DATAIN_PORT, DATAIN);  // in
	sbi(DATAOUT_DDR, DATAOUT); // out
	cbi(BUS_ON_IN_PORT, BUS_ON_IN);

	//timer0_source(CK8);

	// headAddress = 0x130;
	// deviceAddress = 0x150; // 140 HEADUNIT
	event = EV_NONE;
	actionID = ACT_NONE;
	//AVC_OUT_DIS;
	//OUTPUT_SET_0;
}


//  determine whether the bus is free (no tx/rx).
//  return TRUE is bus is free.
bool LanDrv::isAvcBusFree(void) {
	timer0_source(CK8);
	// Reset timer.
	TCNT0 = 0;

	while (INPUT_IS_CLEAR) {
		// We assume the bus is free if anything happens for the length of 1 bit.
		if (TCNT0 > CLEAR_LENGTH) {
			return true;
		}
	}
	return false;
}

// Reads specified number of bits from the AVCLan.
// nbBits (byte) -> Number of bits to read.
// Return (word) -> Data value read.
word LanDrv::readBits(byte nbBits) {
	word data = 0;
	_parityBit = 0;
	uint8_t oldSREG = SREG;
	cli(); 	// disable interrupts
	while (nbBits-- > 0) {		 
			// Insert new bit
		data <<= 1;
		// Wait until rising edge of new bit.
		TCNT0 = 0;
		while (INPUT_IS_CLEAR) {
			if (TCNT0 > 0x60) {
				busError = true;
				SREG = oldSREG;
				return 0;
			}
		}

		// Reset timer to measure bit length.
		TCNT0 = 0;
		// Wait until falling edge.
		while (INPUT_IS_SET);		 

		// Compare half way between a '1' (20 us) and a '0' (32 us ): 32 - (32 - 20) /2 = 26 us
		if (TCNT0 < BIT_0_HOLD_ON_LENGTH) {
			// Set new bit.
			data |= 0x0001;
			// Adjust parity.
			_parityBit = !_parityBit;
		}
	}

	while (INPUT_IS_CLEAR && TCNT0 < NORMAL_BIT_LENGTH);
	SREG = oldSREG;
	return data;
}

// Read incoming messages on the AVCLan.
// Return true if success.
byte LanDrv::_readMessage() {
	busError = false;
	//uint8_t t = 0;
	uint8_t oldSREG = SREG;
	cli(); 	// disable interrupts
	
	broadcast = readBits(1);
	if (busError) {
		SREG = oldSREG;
		return 1;
	}
	masterAddress = readBits(12);
	if (busError) {
		SREG = oldSREG;
		return 1;
	}
	//readBits(1);
	 
	bool p = _parityBit;
	if (p != readBits(1)) {
		SREG = oldSREG;	  
		return 3;
	}

	slaveAddress = readBits(12);

	p = _parityBit;
	if (p != readBits(1)) {
		SREG = oldSREG;
		return 4;
	}

	bool forMe = slaveAddress == deviceAddress && !readonly;
#if  defined(SAT)
	forMe = forMe || slaveAddress == deviceAddressSAT;
#endif
#if  defined(CDC)
	forMe = forMe || slaveAddress == deviceAddressCDC;
#endif
	
	if (forMe) {
		// Send ACK.		 
		send1BitWord(0);
	}
	else {
		readBits(1);
	}
	// Control 
	readBits(4);
	p = _parityBit;

	if (p != readBits(1)) {
		SREG = oldSREG;
		return 5;
	}

	if (forMe) {
		// Send ACK.
		send1BitWord(0);
	}
	else {
		readBits(1);
	}

	dataSize = readBits(8);
	p = _parityBit;
	if (p != readBits(1)) {
		SREG = oldSREG;
		return 6;
	}

	if (forMe) {
		// Send ACK.
		send1BitWord(0);
	}
	else {
		readBits(1);
	}

	if (dataSize > MAXMSGLEN) {
		SREG = oldSREG;
		return 7;
	}

	byte i;
	for (i = 0; i < dataSize; i++) {
		message[i] = readBits(8);
		p = _parityBit;
		if (p != readBits(1)) {
			SREG = oldSREG;
			return 8;
		}

		if (forMe) {
			// Send ACK.			
			send1BitWord(0);
		}
		else {
			readBits(1);
		}
	}

	SREG = oldSREG;
	return 0;
}


// Send a start bit to the AVCLan
void LanDrv::sendStartBit() {
	// Reset timer to measure bit length.

	timer0_source(CK64);
	uint8_t oldSREG = SREG;
	cli();

	timer0_startFrom(3);
	TRANSMIT_1;

	// Pulse level high duration.
	while (TCNT0 <= SEND_START_BIT_HOLD_ON_LENGTH);

	TRANSMIT_0;
	// Pulse level low duration until ~185 us.
	while (TCNT0 <= SEND_START_BIT_LENGTH);
	timer0_source(CK8);  // prescaler 8
	SREG = oldSREG;
}

// Send a 1 bit word to the AVCLan
void LanDrv::send1BitWord(bool data) {
	// Reset timer to measure bit length.
//	uint8_t oldSREG = SREG;
//	timer0_source(CK8);
	//cli();
	timer0_start();
	TRANSMIT_1;

	if (data) {
		while (TCNT0 < SEND_BIT_1_HOLD_ON_LENGTH);
	}
	else {
		while (TCNT0 < SEND_BIT_0_HOLD_ON_LENGTH);
	}

	TRANSMIT_0;
	while (TCNT0 < SEND_NORMAL_BIT_LENGTH);

	//	SREG = oldSREG;

}

// Send a 4 bit word to the AVCLan
void LanDrv::send4BitWord(byte data) {
	_parityBit = 0;
	uint8_t oldSREG = SREG;
	timer0_source(CK8);
	//cli();
	// Most significant bit out first.   
	for (char nbBits = 0; nbBits < 4; nbBits++) {
		// Reset timer to measure bit length.

		TCNT0 = 0;
		TRANSMIT_1;

		if (data & 0x8) {
			// Adjust parity.
			_parityBit = !_parityBit;
			while (TCNT0 < SEND_BIT_1_HOLD_ON_LENGTH);
		}
		else {
			while (TCNT0 < SEND_BIT_0_HOLD_ON_LENGTH);
		}

		TRANSMIT_0;
		// Hold output low until end of bit.
		while (TCNT0 < SEND_NORMAL_BIT_LENGTH);

		// Fetch next bit.
		data <<= 1;
	}
	SREG = oldSREG;
}

// Send a 8 bit word to the AVCLan
void LanDrv::send8BitWord(byte data) {
	_parityBit = 0;
	uint8_t oldSREG = SREG;
	timer0_source(CK8);
	//cli();
	// Most significant bit out first.   
	for (char nbBits = 0; nbBits < 8; nbBits++) {
		// Reset timer to measure bit length.
		TCNT0 = 0;
		TRANSMIT_1;

		if (data & 0x80) {
			// Adjust parity.
			_parityBit = !_parityBit;
			while (TCNT0 < SEND_BIT_1_HOLD_ON_LENGTH);
		}
		else {
			while (TCNT0 < SEND_BIT_0_HOLD_ON_LENGTH);
		}

		TRANSMIT_0;
		// Hold output low until end of bit.
		while (TCNT0 < SEND_NORMAL_BIT_LENGTH);

		// Fetch next bit.
		data <<= 1;
	}
	SREG = oldSREG;
}

// Send a 12 bit word to the AVCLan
void LanDrv::send12BitWord(word data) {
	_parityBit = 0;
	uint8_t oldSREG = SREG;
	timer0_source(CK8);
	//cli();
	// Most significant bit out first.   
	for (char nbBits = 0; nbBits < 12; nbBits++) {
		// Reset timer to measure bit length.
		TCNT0 = 0;
		TRANSMIT_1;

		if (data & 0x0800) {
			// Adjust parity.
			_parityBit = !_parityBit;
			while (TCNT0 < SEND_BIT_1_HOLD_ON_LENGTH);
		}
		else {
			while (TCNT0 < SEND_BIT_0_HOLD_ON_LENGTH);
		}

		TRANSMIT_0;
		// Hold output low until end of bit.
		while (TCNT0 < SEND_NORMAL_BIT_LENGTH);

		// Fetch next bit.
		data <<= 1;
	}
	SREG = oldSREG;
}



// Read incoming messages on the AVCLan, log message through serial port
// Return true if success.
byte LanDrv::readMessage() {
	//while (lan.isAvcBusFree());
	timer0_source(CK64);
	timer0_start();

	//_startTime = timer2.get_count();  
	while (INPUT_IS_SET);
	if (TCNT0 >= START_BIT_HOLD_ON_LENGTH ) {		
			timer0_source(CK8);
			byte res = lan._readMessage();
			if (!res) {			 
				if (isPrintMessage)
					lan.printMessage(true);
				return res;
			}
			else {				
				if (isPrintMessage)
				{
					Serial.print("error:");
					Serial.print(res);
					Serial.println();
				}
				return 1;
				//while (!lan.isAvcBusFree());
			}
		
		return 1;
	}
	return 1;
}

void LanDrv::printMessage(bool incoming) {
	if (incoming) {
		Serial.print("< ");
	}
	else {
		Serial.print("> ");
	}
	if (broadcast == MSG_BROADCAST) {
		Serial.print("b ");
	}
	else {
		Serial.print("d ");
	}

	Serial.print(masterAddress, HEX);
	Serial.print(" ");

	Serial.print(slaveAddress, HEX);

	Serial.print(" ");
	Serial.print(dataSize, HEX);
	Serial.print(" ");
	for (byte i = 0; i < dataSize; i++) {
		_printHEX(&message[i], 1);
		Serial.print(" ");
	}
	Serial.println();

}

// sends ack bit if I am broadcasting otherwise wait and return received ack bit.
// return FALSE if ack bit not detected.
bool  LanDrv::handleAcknowledge(void) {
	if (broadcast == MSG_BROADCAST) {
		// Acknowledge.    
		send1BitWord(0);
		return true;
	}

	// Return acknowledge bit.
	return  readAcknowledge();
}

// reads the acknowledge bit the AVCLan
// return TRUE if ack detected else FALSE.
bool LanDrv::readAcknowledge(void) {
	// The acknowledge pattern is very tricky: the sender shall drive the bus for the equivalent
	// of a bit '1' (20 us) then release the bus and listen. At this point the target shall have
	// taken over the bus maintaining the pulse until the equivalent of a bit '0' (32 us) is formed.
//	uint8_t oldSREG = SREG;
	timer0_source(CK8);
	//cli();
	// Reset timer to measure bit length.
	TCNT0 = 0;
	TRANSMIT_1;

	// Generate bit '0'.
	while (TCNT0 < SEND_BIT_1_HOLD_ON_LENGTH);
	TRANSMIT_0;

	while (TCNT0 < SEND_BIT_1_HOLD_ON_LENGTH);


	// Measure final resulting bit.
	while (INPUT_IS_SET)

		// Sample half-way through bit '0' (26 us) to detect whether the target is acknowledging.
		if (TCNT0 > BIT_0_HOLD_ON_MIN_LENGTH) {
			// Slave is acknowledging (ack = 0). Wait until end of ack bit.
			while (INPUT_IS_SET);
			//	SREG = oldSREG;

			return true;
		}
	// No sign of life on the bus.
	//SREG = oldSREG;
	return false;
}


// sends the message in global registers on the AVC LAN bus.
// return 0 if successful else error code
byte LanDrv::_sendMessage(void) {
	uint8_t oldSREG = SREG;
	cli();             // disable interrupts
	while (!isAvcBusFree());
	// Send start bit.
	sendStartBit();

	// Broadcast bit.
	send1BitWord(broadcast);

	// Master address = me.
	send12BitWord(masterAddress);
	send1BitWord(_parityBit);

	// Slave address = head unit (HU).
	send12BitWord(slaveAddress);
	send1BitWord(_parityBit);
	if (!handleAcknowledge()) {
		SREG = oldSREG;
		return 1;
	}

	// Control flag + parity.
	send4BitWord(CONTROL_FLAGS);
	send1BitWord(_parityBit);
	if (!handleAcknowledge()) {
		SREG = oldSREG;
		return 2;
	}

	// Data length + parity.
	send8BitWord(dataSize);
	send1BitWord(_parityBit);
	if (!handleAcknowledge()) {
		SREG = oldSREG;
		return 3;
	}

	for (byte i = 0; i < dataSize; i++) {
		send8BitWord(message[i]);
		send1BitWord(_parityBit);
		if (!handleAcknowledge()) {
			SREG = oldSREG;
			return false;
		}
	}

	SREG = oldSREG;
	return 0;
}


// sends the message in global registers on the AVC LAN bus, log message through serial port
// return 0 if successful else error code
byte  LanDrv::sendMessage(void) {
	byte sc = MAXSENDATTEMP;
	byte res;

	do {
		res = lan._sendMessage();
		if (!res) {
			if (isPrintMessage)
				lan.printMessage(false);
		}
		else {
			if (isPrintMessage) {
				Serial.print("W");
				Serial.print(res, HEX);
				Serial.println();
			}
			while (!lan.isAvcBusFree());
		}
		sc--;
	} while (sc && res);
	return res;
}

// sends the message for given mesage ID on the AVC LAN bus, log message through serial port
// return 0 if successful else error code
byte LanDrv::sendMessage(OutMessage *msg) {
	loadMessage(msg);
	return sendMessage();
}



// Loads message data for given mesage ID.
void LanDrv::loadMessage(OutMessage *msg) {
	broadcast = pgm_read_byte_near(&msg->broadcast);
	masterAddress = deviceAddress;

	if (broadcast == MSG_BROADCAST)
		slaveAddress = 0xFFF;
	else
		slaveAddress = headAddress;

	dataSize = pgm_read_byte_near(&msg->dataSize);

	for (byte i = 0; i < dataSize; i++)
		message[i] = pgm_read_byte_near(&msg->data[i]);

}



// Loads message data for given mesage ID.
void LanDrv::loadMessage(OutMessage *msg, word address) {
	broadcast = pgm_read_byte_near(&msg->broadcast);
	masterAddress = address;

	if (broadcast == MSG_BROADCAST)
		slaveAddress = 0xFFF;
	else
		slaveAddress = headAddress;

	dataSize = pgm_read_byte_near(&msg->dataSize);

	for (byte i = 0; i < dataSize; i++)
		message[i] = pgm_read_byte_near(&msg->data[i]);

}


void	 LanDrv::_printHEX(uint8_t *data, uint8_t len) // prints 8-bit data in hex
{
	char tmp[len * 2 + 1];
	byte first;
	int j = 0;
	for (uint8_t i = 0; i < len; i++)
	{
		first = (data[i] >> 4) | 48;
		if (first > 57) tmp[j] = first + (byte)7;
		else tmp[j] = first;
		j++;

		first = (data[i] & 0x0F) | 48;
		if (first > 57) tmp[j] = first + (byte)7;
		else tmp[j] = first;
		j++;
	}

	tmp[len * 2] = 0;
	Serial.print(tmp);
}


// Use the last received message to determine the corresponding action ID
byte LanDrv::getActionID(InMessageTable messageTable[], byte mtSize) {
	
	for (byte msg = 0; msg < mtSize; msg++) {
		bool found = true;

		if (dataSize != pgm_read_byte_near(&messageTable[msg].dataSize))
			continue;

		for (byte i = 0; i < dataSize; i++)
			if (message[i] != pgm_read_byte_near(&messageTable[msg].data[i])) {
				found = false;
				break;
			}

		if (found)
			return pgm_read_byte_near(&messageTable[msg].actionID);
	}

	if (masterAddress == 0x230)
		return  ACT_EXT;

	return ACT_NONE;
}

// Use the last received message to determine the corresponding action ID, use masked message table
byte LanDrv::getActionID(InMaskedMessageTable messageTable[], byte mtSize) {
	
	for (byte msg = 0; msg < mtSize; msg++) {
		bool found = true;

		if (dataSize != pgm_read_byte_near(&messageTable[msg].dataSize))
			continue;

		word mask = pgm_read_byte_near(&messageTable[msg].mask);
		for (byte i = 0; i < dataSize; i++) {
			if (mask & _BV(i))
				continue;
			if (message[i] != pgm_read_byte_near(&messageTable[msg].data[i])) {
				found = false;
				break;
			}
		}

		if (found)
			return pgm_read_byte_near(&messageTable[msg].actionID);
	}
	return ACT_NONE;
}

// Use the last received message to determine the corresponding action ID
byte LanDrv::getActionID(InMessageIgnoreFromPosition messageTable[], byte mtSize) {
	//uint8_t oldSREG = SREG;
	//cli(); 	// disable interrupts
	for (byte msg = 0; msg < mtSize; msg++) {
		bool found = true;

		if (dataSize < pgm_read_byte_near(&messageTable[msg].startIgnore))
			continue;

		for (byte i = 0; (i < pgm_read_byte_near(&messageTable[msg].startIgnore)); i++)
			if (message[i] != pgm_read_byte_near(&messageTable[msg].data[i])) {
				found = false;
				break;
			}

		if (found)
			return pgm_read_byte_near(&messageTable[msg].actionID);
	}
//	SREG = oldSREG;
	return ACT_NONE;
}




LanDrv lan;