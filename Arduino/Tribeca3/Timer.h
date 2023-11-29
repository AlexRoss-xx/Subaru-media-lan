#pragma once
#ifndef _AVR_TIMER_H_
#define _AVR_TIMER_H_

#include <avr/io.h>

#if defined(__AVR_ATmega128__) || defined(__AVR_ATmega64__)
# warning "This file is known to be incorrect for your MCU type"
#endif

#ifdef __cplusplus
extern "C" {
#endif
 // TCNT0
	 
	enum {
		STOP = 0,
		CK = 1,
		CK8 = 2,
		CK64 = 3,
		CK256 = 4,
		CK1024 = 5,
		T0_FALLING_EDGE = 6,
		T0_RISING_EDGE = 7
	};
	 
 
	static __inline__ void timer0_source(unsigned int src)
	{
		TCCR0B = src;
	}

 
	static __inline__ void timer0_stop(void)
	{
		TCNT0 = 0;
	}

	static __inline__ void timer0_start(void)
	{
		TCNT0 = 0;
	}

	static __inline__ void timer0_startFrom(unsigned int src)
	{
		TCNT0 = src;
	}
#ifdef __cplusplus
}
#endif

#endif /* _AVR_TIMER_H_ */