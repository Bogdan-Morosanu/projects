#include <iostream>
#include <iomanip>
#include <cblas.h>
#include <cstring>

#include "include/mat.hpp"
#include "include/tmp/mapped_range.hpp"


int main(int argc, char **argv)
{

	auto m = afl::Mat<double, 3, 3>(0);
	auto l = m, u = m, p = m;

	double buf[] = {    0.5797200,   0.8562388,   0.6963766,
						0.7862408,   0.0091269,   0.8417164,
						0.1307007,   0.8219878,   0.5528331, };

	std::memcpy(&m(0,0), buf, sizeof(buf));

	m.lup_decomp(l, u, p);
	afl::Mat<double, 3, 1> target(1);

	auto sol = target;
	bool success = m.solve_from_lup(target, l, u, p, sol);
    if(success) {
        std::cout << m * sol << " = " << std::endl
                << target << std::endl;

    } else {
        std::cout << "matrix singular" << std::endl;
    }

	auto m_inv = m;
	success = m.inv(m_inv);

	if(success) {
	    std::cout << m * m_inv << std::endl;

	} else {
	    std::cout << "matrix singular" << std::endl;
	}

	std::cout << m.l1_norm() << " "
	        << m.l2_norm() << " "
	        << m.linf_norm() << std::endl;

	return 0;
}
















