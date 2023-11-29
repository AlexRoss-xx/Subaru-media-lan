#pragma once

//#define EMULATE

// USA configuration Or EU Configuration
#define USA


#ifdef USA		
//#define  CDCEXT //CDC EXTERNAL - external emu devices (GROM or other simmilar)
#define  CDC // CDC emulation   ( allow to  use RSA mode as output Audio from Android   (pressing  RSA mode twice allow to switch beetween RSA or CDC))
//#define SAT	// SAT Emulation ( allow to  remove SAT radio unit  and use SAT mode as outpu Audio from Android)
//#define TPMS	// TPMS
#endif

#ifdef EU
#define  CDC //CDC emulation
#endif // EU





#define NANO
#ifdef NANO
// define in pin (pin 9 arduino) 
#define DATAIN_DDR		DDRD
#define DATAIN_PORT		PORTD
#define DATAIN_PIN		PIND
#define DATAIN			7

#define DATAOUT_DDR		DDRD
#define DATAOUT_PORT	PORTD
#define DATAOUT_PIN		PIND
#define DATAOUT			6
#endif

#define BUS_ON_IN_PORT	PORTB
#define BUS_ON_IN_PIN	PINB
#define BUS_ON_IN		0

#define AUDIO_OUT_DDR  DDRD
#define AUDIO_OUT_PORT PORTD
#define AUDIO_OUT_PIN  PIND
#define AUDIO_OUT	   3

//#define TRANSMIT_AUDIO_ON	(cbi(AUDIO_OFF_OUT_PORT, AUDIO_OFF_OUT))
//#define TRANSMIT_AUDIO_OFF	(sbi(AUDIO_OFF_OUT_PORT, AUDIO_OFF_OUT))

#define TRANSMIT_AUDIO_OFF			PORTD|= _BV(AUDIO_OUT); 
#define TRANSMIT_AUDIO_ON			PORTD &= ~_BV(AUDIO_OUT)

// MICRO board under developing. Do not define  
#ifdef MICRO
#define DATAIN_DDR    DDRD
#define DATAIN_PORT   PORTE
#define DATAIN_PIN    PINE
#define DATAIN        6

#define DATAOUT_DDR   DDRD
#define DATAOUT_PORT  PORTD
#define DATAOUT_PIN   PIND
#define DATAOUT       7
#endif

#ifdef NANO
#define LED_DDR			DDRB
#define LED_PORT		PORTB
#define LED_PIN			PINB
#define LED_OUT			5
#endif

#ifdef MICRO
#define LED_DDR     DDRC
#define LED_PORT    PORTC
#define LED_PIN     PINC
#define LED_OUT     7
#endif

#define INPUT_BUS_ON_SET	(bit_is_set(BUS_ON_IN_PIN, BUS_ON_IN))
#define INPUT_BUS_ON_CLEAR	(bit_is_clear(BUS_ON_IN_PIN, BUS_ON_IN))

#define INPUT_IS_SET   ((bit_is_set(DATAIN_PIN, DATAIN))	)
#define INPUT_IS_CLEAR ((bit_is_clear(DATAIN_PIN, DATAIN))	)

#define LED_ON			PORTB|= _BV(LED_OUT); 
#define LED_OFF			PORTB &= ~_BV(LED_OUT)

//#define TRANSMIT_1		PORTD &= ~(_BV(DATAOUT));//PORTD|= _BV(DATAOUT); 
//#define TRANSMIT_0		PORTD|= _BV(DATAOUT);//PORTD &= ~_BV(DATAOUT)

//#define OUTPUT_SET_1  (cbi(DATAOUT_PORT, DATAOUT)); 
//#define OUTPUT_SET_0  (sbi(DATAOUT_PORT, DATAOUT));

#ifndef cbi
#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#endif
#ifndef sbi
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))
#endif

#ifndef EMULATE
// HI-LOW signal   HI - clear,LOW -  input
#define TRANSMIT_1	(cbi(DATAOUT_PORT, DATAOUT));   
#define TRANSMIT_0	(sbi(DATAOUT_PORT, DATAOUT)); 
#endif
#ifdef EMULATE
// REVERSE  HI-LOW
#define TRANSMIT_0	(cbi(DATAOUT_PORT, DATAOUT));   
#define TRANSMIT_1	(sbi(DATAOUT_PORT, DATAOUT)); 
#endif