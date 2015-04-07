// Generic includes
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>

// Special Int and Chars
#include <stddef.h>
#include <stdint.h>
#include <ctype.h>


// User libraries
#include "consNil.h"

#define DEBUG_PRINT 0

typedef union __environ {
	void 	*buff;
	double	doubleVal;
	long 	longVal;
	int  	intVal;
	char 	charVal;
	short 	shortVal;
} Env;

struct _void_closure {
	void (*apply)(Env data, const void* args );
	Env data;
};

typedef struct _void_closure* SideEffect;

struct _predicate {
	int (*apply)(Env data, const void* args );
	Env data;
};

typedef struct _predicate* Predicate;

struct _generic_closure {
	void* (*apply)(Env data, const void* args );
	Env data;
};

typedef struct _generic_closure* Closure;

Closure new_Closure(void* (*apply)(Env,const void*), Env data);
void* evalClosure( Closure c, const void* args );

Predicate new_Predicate(int (*apply)(Env,const void*), Env data);
int applyPredicate( Predicate p, const void* args );


SideEffect new_SideEffect(void (*apply)(Env, const void*), Env data);
void callSideEffect( SideEffect proc, const void* args );


static int lessThanInt( Env comp, const void* args );

int main( int argc, char** argv ) {
	Env three;
	three.intVal = 3;
	
	Predicate lt3 = new_Predicate( lessThanInt, three );
	
	for(int i = 0; i < 6; i++) {
		printf("%d : p(%d) = %d\n", i, i, applyPredicate( lt3, &i ) );
	}
	
	
	return 0;
}


Closure new_Closure(void* (*apply)(Env,const void*), Env data) {
	Closure this = malloc( sizeof(struct _generic_closure) );
	assert(this);
	this->apply = apply;
	this->data = data;
	return this;
}

inline void* evalClosure( Closure c, const void* args ) {
	return c->apply(c->data, args);
}

Predicate new_Predicate(int (*apply)(Env,const void*), Env data) {
	Predicate this = malloc( sizeof(struct _predicate) );
	assert(this);
	this->apply = apply;
	this->data = data;
	return this;
}

inline int applyPredicate( Predicate p, const void* args ) {
	return p->apply( p->data, args );
}

SideEffect new_SideEffect(void (*apply)(Env, const void*), Env data) {
	SideEffect this = malloc( sizeof(struct _void_closure) );
	assert(this);
	this->apply = apply;
	this->data = data;
	return this;
}

inline void callSideEffect( SideEffect proc, const void* args ) {
	proc->apply( proc->data, args );
}

static int lessThanInt( Env comp, const void* args ) {
	return *(int*)args < comp.intVal;
}










