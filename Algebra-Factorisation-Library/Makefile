sources = ./mockmain.cpp ./src/*.cpp
headers = ./include/*.hpp ./include/intern/*.hpp
libs = -lblas -lcblas

all: afl

clean: 
	rm afl

afl: $(sources) $(headers)
	g++ -std=c++14 -Wall $(headers) $(sources) $(libs) -o "bin/afl"