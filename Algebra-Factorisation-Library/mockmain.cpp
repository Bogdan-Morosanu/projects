#include <iostream>
#include <iomanip>
#include <cblas.h>
#include <cstring>

#include "include/mat.hpp"
#include "include/tmp/mapped_range.hpp"

constexpr int ident(std::size_t x) { return x; }

int main(int argc, char **argv)
{
//	auto mat = afl::Mat<double, 2, 2>(3);
//	mat(0, 0) = 4;
//	mat(1, 0) = 6;
//
//	auto l = mat, u = mat, p = mat;
//	mat.lup_decomp(l, u, p);
//	std::cout << l << std::endl << u << std::endl
//			  << p << std::endl << mat << std::endl
//			  << l * u << std::endl;

//	auto ones = afl::Mat<double, 3, 3>(1);
//
//	std::cout << ones * ones << std::endl;
//
//	ones *= ones;
//
//	std::cout << ones << std::endl;
//
//	auto l = ones, u = ones, p = ones;
//	bool retval = ones.lup_decomp(l, u, p);
//	std::cout << l << u << p << retval <<  std::endl;
//	std::cout << ones << std::endl << l * u << std::endl;

	auto m = afl::Mat<double, 3, 3>(0);
	auto l = m, u = m, p = m;

	double buf[] = {    0.5797200,   0.8562388,   0.6963766,
						0.7862408,   0.0091269,   0.8417164,
						0.1307007,   0.8219878,   0.5528331, };

	std::memcpy(&m(0,0), buf, sizeof(buf));

	m = m.transp();

	m.lup_decomp(l, u, p);

	std::cout << l << std::endl << u << std::endl
			  << p << std::endl << p * m << std::endl
			  << l * u << std::endl;


	return 0;
}
















