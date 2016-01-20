kern: kern.cpp kern.hpp expr.hpp expr.cpp dataset.hpp dataset.cpp
	g++ -I../../include/LibEigen/ -std=c++11  kern.cpp kern.hpp expr.hpp expr.cpp dataset.hpp dataset.cpp -o kern

csvtest: csvtest.cpp
	g++ -std=c++11 -I../../include/PEGTL/ csvtest.cpp -o csvtest

clean:
	rm kern
