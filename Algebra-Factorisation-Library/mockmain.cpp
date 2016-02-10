#include <iostream>
#include <iomanip>
#include <cblas.h>
#include <cstring>

#include "include/mat.hpp"
#include "include/tmp/mapped_range.hpp"
#include "include/memory/blockstore.hpp"

typedef afl::Mat<double, 3, 3> Mat3x3d;
afl::mem::BlockStore<sizeof(Mat3x3d), 30, Mat3x3d> store;

int main(int argc, char **argv)
{
    afl::Mat<double, 3, 3> *ptr = static_cast<Mat3x3d*>(store.malloc());
	auto& m = *new(ptr) Mat3x3d(0);
	//auto m = Mat3x3d(0);

	Mat3x3d *lp, *up, *pp;
	lp = (Mat3x3d*) (store.malloc());
	up = (Mat3x3d*) (store.malloc());
	store.free(lp);
	store.free(up);
	pp = (Mat3x3d*) (store.malloc());
	lp = (Mat3x3d*) (store.malloc());
	up = (Mat3x3d*) (store.malloc());

	auto l = *new(lp)Mat3x3d(m);
	auto u = *new(up)Mat3x3d(m);
	auto p = *new(pp)Mat3x3d(m);

	m = Mat3x3d {   0.5797200,   0.8562388,   0.6963766,
				    0.7862408,   0.0091269,   0.8417164,
					0.1307007,   0.8219878,   0.5528331, };

	///std::memcpy(&m(0,0), buf, sizeof(buf));

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

	store.free(ptr);

	return 0;
}
















