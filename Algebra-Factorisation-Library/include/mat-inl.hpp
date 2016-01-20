/*
 * mat-inl.hpp
 *
 *  Created on: Jan 16, 2016
 *      Author: moro
 */
#include "mat.hpp"
#include "intern/overloads.hpp"
#include "tmp/mapped_range.hpp"

#include <iostream>
#include <array>
#include <limits>
#include <cmath>


#ifndef MAT_INL_HPP
#define MAT_INL_HPP

namespace afl {

	/// @brief factory function, returns Identity matrix
	template <typename ElType, int dim>
	Mat<ElType, dim, dim>
	eye()
	{	// dim * dim assignments + dim * dim comparisons
		// are probably more expensive than
		// dim * dim + dim assignments, so we don't bother
		// checking. init all to zero, make diag 1.
		Mat<ElType, dim, dim> retval(0);
		for (int d = 0; d < dim; ++d) {
			retval(d, d) = 1;
		}
		return retval;
	}

	/// @brief factory function, returns permutation matrix
	/// that will permute the rows of another matrix when
	/// multiplied.
	/// trick: visualise afl::permutation as the permuted eye()
	/// and generating one will become very easy.
	/// (because permutation * eye == permutation).
	template <typename ElType, int dim>
	Mat<ElType, dim, dim>
	permutation(const std::array<int, dim>& p)
	{	// dim * dim assignments + dim * dim comparisons
		// are probably more expensive than
		// dim * dim + dim assignments, so we don't bother
		// checking. init all to zero, make diag 1.
		Mat<ElType, dim, dim> retval(0);
		for (int d = 0; d < dim; ++d) {
			retval(d, p[d]) = 1;
		}
		return retval;
	}

	template<typename ElType, int rows, int cols>
	inline Mat<ElType, rows, cols>::
	Mat(const ElType& val)
	{
		for (int c = 0; c < cols; ++c) {
			for (int r = 0; r < rows; ++r) {
				this->buf[c][r] = val;
			}
		}
	}

	template<typename ElType, int rows, int cols>
	inline ElType
	Mat<ElType, rows, cols>::
	operator()(int r, int c) const
	{
		return buf[c][r];
	}

	template<typename ElType, int rows, int cols>
	inline ElType&
	Mat<ElType, rows, cols>::
	operator () (int r, int c)
	{
		return buf[c][r];
	}

	template<typename ElType, int rows, int cols>
	inline Mat<ElType, cols, rows>
	Mat<ElType, rows, cols>::
	transp() const
	{
		Mat<ElType, cols, rows> retval;
		for (int c = 0; c < cols; ++c) {
			for (int r = 0; r < rows; ++r) {
				retval(c, r) = (*this)(r, c);
			}
		}
		return retval;
	}

	template<typename ElType, int rows, int cols>
	std::ostream&
	operator << (std::ostream& out, const Mat<ElType, rows, cols>& mat)
	{
		out << "[" << std::endl;
		for (int r = 0; r < rows; ++r) {
			out << " ";
			for (int c = 0; c < cols; ++c) {
				out << mat(r, c) << " ";
			}
			out << std::endl;
		}
		out << "]" << std::endl;
		return out;
	}

	template<typename ElType, int rows, int cols>
	inline std::pair<ElType, int>
	Mat<ElType, rows, cols>::
	max_abs(IterationDir d, int idx) const
	{	// denorm_min() is the min non-zero value, but we want
		// to accomodate 0 as well, so we use lowest as well.
		std::pair<ElType, int> retval =
			{ std::numeric_limits<ElType>::lowest(), -1};

		if(d == IterationDir::ROWS) {
			for (int r = 0; r < rows; ++r) {
				if(retval.first < std::abs((*this)(r, idx))) {
					retval = { (*this)(r, idx), idx };
				}
			}
		} else {
			for (int c = 0; c < cols; ++c) {
				if(retval.first < std::abs((*this)(idx, c))) {
					retval = { (*this)(idx, c), idx };
				}
			}
		}

		return retval;
	}

	template<typename ElType, int rows, int cols>
	inline std::pair<ElType, int>
	Mat<ElType, rows, cols>::
	max(IterationDir d, int idx) const
	{
		std::pair<ElType, int> retval =
			{ std::numeric_limits<ElType>::lowest(), -1};

		if(d == IterationDir::ROWS) {
			for (int r = 0; r < rows; ++r) {
				if(retval.first < (*this)(r, idx)) {
					retval = { (*this)(r, idx), idx };
				}
			}
		} else {
			for (int c = 0; c < cols; ++c) {
				if(retval.first < (*this)(idx, c)) {
					retval = { (*this)(idx, c), idx };
				}
			}
		}

		return retval;
	}

	template<typename ElType, int rows, int cols>
	inline std::pair<ElType, int>
	Mat<ElType, rows, cols>::
	min_abs(IterationDir d, int idx) const
	{
		std::pair<ElType, int> retval =
			{ std::numeric_limits<ElType>::max(), -1};

		if(d == IterationDir::ROWS) {
			for (int r = 0; r < rows; ++r) {
				if(retval.first > std::abs((*this)(r, idx))) {
					retval = { (*this)(r, idx), idx };
				}
			}
		} else {
			for (int c = 0; c < cols; ++c) {
				if(retval.first > std::abs((*this)(idx, c))) {
					retval = { (*this)(idx, c), idx };
				}
			}
		}

		return retval;
	}

	template<typename ElType, int rows, int cols>
	inline std::pair<ElType, int>
	Mat<ElType, rows, cols>::
	min(IterationDir d, int idx) const
	{
		std::pair<ElType, int> retval =
			{ std::numeric_limits<ElType>::max(), -1};

		if(d == IterationDir::ROWS) {
			for (int r = 0; r < rows; ++r) {
				if(retval.first > (*this)(r, idx)) {
					retval = { (*this)(r, idx), idx };
				}
			}
		} else {
			for (int c = 0; c < cols; ++c) {
				if(retval.first > (*this)(idx, c)) {
					retval = { (*this)(idx, c), idx };
				}
			}
		}

		return retval;
	}

	template<typename ElType, int rows, int cols>
	bool Mat<ElType, rows, cols>::
	lup_decomp(Mat& l, Mat& u, Mat& p) const
	{	// notice templated member functions are only instantiated on calling,
		// and not on class instantiation.
		static_assert(rows == cols, "can't decompose non-square matrix");

		// init permutations with the identical permutation.
		auto perm = tmp::mapped_range<int, tmp::ident, rows>();

		// init outputs
		l = Mat(0); u = *this;

		// now do the substitution itself
		for (int c = 0; c < cols; ++c) {
			// compute min index and min val
			// TODO implement via ROI Iterators
			// std::pair<ElType, int> max_v =
			//		u.max_abs(IterationDir::ROWS, c);
			std::pair<ElType, int> max_v = { std::numeric_limits<ElType>::lowest(), -1 };
			for (int i = c; i < cols; ++i) {
				if (max_v.first < std::abs(u(i, c))) {
					max_v = { u(i, c), i };
				}
			}

			if(std::abs(max_v.first) < std::numeric_limits<ElType>::epsilon()) {
				return false; // matrix is singular !
			} else {
				std::swap(perm[c], perm[max_v.second]); // store computed permutation
			}

			// TODO implement swap ranges member function
			// swap the max val equation up to the current row.
			for (int col = c; col < cols; ++col) {
				std::swap(u(c, col), u(max_v.second, col));
			}

			// the row-reduce equation is
			// (row = row - fact * upper rows)
			// this 1.0 is the weight of the row, and the upper
			// row coefficients are stores to the left. i.e. in
			// l(c', c), c' < c.
			l(c, c) = 1.0;
			for (int r = c+1; r < rows; ++r) {
				double fact = u(r, c) / max_v.first; // reduction factor
// 				DEBUG
//				std::cout << "fact : " << fact << std::endl;

				// reduce equation number col
				for (int col = c; col < cols; ++col) {
					u(r, col) -= fact * u(c, col);
				}

				// populate lower matrix with the factors we have used.
				l(r, c) = fact;
			}
//			DEBUG
//			std::cout << "after " << c << "th iteration :  " << std::endl
//					  << "l :" << std::endl << l << "u : " << std::endl << u
//					  << "max (val, idx) : (" << max_v.first << "," << max_v.second << ")"
//					  << std::endl;
		}

		p = permutation<ElType, perm.size()>(perm);
		return true;
	}

	/// @brief friend function to multiply two matrices.
	/// a friend function is needed if we need type-conversions
	/// on both lhs and rhs arguments.
	template <typename ElType, int rows, int cols, int n>
	inline Mat<ElType, rows, cols>
	operator * (const Mat<ElType, rows, n>& lhs,
				const Mat<ElType, n, cols>& rhs)
	{
		Mat<ElType, rows, cols> retval;

		// we've overloaded some function calls
		// instead of using the C dgemm, sgemm and so on.
		// this has the benefit that afl::Mat's with ElTypes that
		// do not have gemm ops defined will not compile.
		intern::overload_resolver_gemm(
				CBLAS_LAYOUT::CblasColMajor, CBLAS_TRANSPOSE::CblasNoTrans,
				CBLAS_TRANSPOSE::CblasNoTrans, rows, cols, n, 1.0,
				lhs.buf[0], n, rhs.buf[0], cols, 0.0, retval.buf[0], cols);

		return retval;
	}

	template <typename ElType, int rows, int cols, int n>
	inline Mat<ElType, rows, cols>
	operator + (const Mat<ElType, rows, n>& lhs,
				const Mat<ElType, n, cols>& rhs)
	{
		Mat<ElType, rows, cols> retval(lhs);
		return (retval += rhs);
	}

	template <typename ElType, int rows, int cols, int n>
	inline Mat<ElType, rows, cols>
	operator - (const Mat<ElType, rows, n>& lhs,
				const Mat<ElType, n, cols>& rhs)
	{
		Mat<ElType, rows, cols> retval(lhs);
		return (retval -= rhs);
	}

	template<typename ElType, int rows, int cols>
	inline Mat<ElType, rows, cols>&
	Mat<ElType, rows, cols>::
	operator *= (const Mat<ElType, cols, rows>& rhs)
	{	// TODO implement more efficient version of this
		return (*this) = (*this) * rhs;
	}

	template<typename ElType, int rows, int cols>
	inline Mat<ElType, rows, cols>&
	Mat<ElType, rows, cols>::
	operator += (const Mat& rhs)
	{
		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < cols; ++c) {
				this->buf[c][r] += rhs->buf[c][r];
			}
		}
	}

	template<typename ElType, int rows, int cols>
	inline Mat<ElType, rows, cols>&
	Mat<ElType, rows, cols>::
	operator -= (const Mat& rhs)
	{
		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < cols; ++c) {
				this->buf[c][r] -= rhs->buf[c][r];
			}
		}
	}

	template<typename ElType, int rows, int cols>
	inline Mat<ElType, rows, cols>
	operator * (ElType scalar, const Mat<ElType, rows, cols>& mat)
	{
		Mat<ElType, rows, cols> retval;
		for (int c = 0; c < cols; ++c) {
			for (int r = 0; r < rows; ++r) {
				retval(r, c) = scalar * mat(r, c);
			}
		}
		return retval;
	}

	template<typename ElType, int rows, int cols>
	inline Mat<ElType, rows, cols>
	operator * (const Mat<ElType, rows, cols>& mat, ElType scalar)
	{
		return scalar *  mat;
	}
}


#endif
