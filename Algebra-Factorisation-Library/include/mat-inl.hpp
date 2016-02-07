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
#include <cassert>

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

    template <typename SeedType,
              typename ElType,
              int rows, int cols>
    inline SeedType
    reduce_with(std::function<SeedType(SeedType, ElType)> reducer,
                const Mat<ElType, rows, cols>& mat,
                SeedType seed,
                IterationDir dir)
    {
        for (int c = 0; c < cols; ++c) {
            for (int r = 0; r < rows; ++r) {
                seed = reducer(seed, mat(r, c));
            }
        }
        return seed;
    }

    template <typename SeedType,
              typename ElType,
              int rows, int cols,
              SeedType(*reducer)(SeedType, ElType)>
    inline SeedType
    reduce_with(const Mat<ElType, rows, cols>& mat,
                SeedType seed)
    {
        for (int c = 0; c < cols; ++c) {
            for (int r = 0; r < rows; ++r) {
                seed = reducer(seed, mat(r, c));
            }
        }
        return seed;
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

	template <typename ElType>
	constexpr inline ElType
	l2_reducer(ElType seed, ElType cnt)
	{
	    return seed + cnt * cnt;
	}

    /// @brief returns sum of squares of elements.
    template <typename ElType, int rows, int cols>
	inline ElType Mat<ElType, rows, cols>::
    l2_norm() const
    {
        return reduce_with<ElType, ElType, rows, cols, l2_reducer>(*this);
    }

    template <typename ElType>
    constexpr inline ElType
    l1_reducer(ElType seed, ElType cnt)
    {
        return seed + cnt;
    }

    /// @brief return sum of elements.
    template <typename ElType, int rows, int cols>
    inline ElType Mat<ElType, rows, cols>::
    l1_norm() const
    {
        return reduce_with<ElType, ElType, rows, cols, l1_reducer>(*this);
    }


    template <typename ElType>
    constexpr inline ElType
    linf_reducer(ElType seed, ElType cnt)
    {
        return std::max(seed, cnt);
    }

    /// @brief returns max element.
    template <typename ElType, int rows, int cols>
    inline ElType Mat<ElType, rows, cols>::
    linf_norm() const
    {
        return reduce_with<ElType, ElType, rows, cols, linf_reducer>(*this,
                               std::numeric_limits<ElType>::lowest());
    }

	template <typename ElType, int rows, int cols>
	bool Mat<ElType, rows, cols>::
	inv(Mat& inverse) const
	{
		static_assert(rows == cols, "pseudo-inverse not yet implemented!");
		Mat l, u, p;
		bool success = this->lup_decomp(l, u, p);

		if(!success) {
		    return false; // matrix singular
		}

		// we'll be inverting one column at a time
		// by solving system A * X = I for each col of X
		for (int c = 0; c < cols; ++c) {
		    Mat<ElType, rows, 1> i_col(0);
		    i_col(c, 0) = 1;

		    Mat<ElType, rows, 1> x_col;
		    success = this->solve_from_lup(i_col, l, u, p, x_col);
		    if(!success) {
		        return false; // matrix singular
		    }

		    for (int r = 0; r < rows; ++r) {
		        inverse(r, c) = x_col(r, 0);
		    }
		}

		return true;
	}

	template<typename ElType, int rows, int cols>
	bool Mat<ElType, rows, cols>::
	lup_decomp(Mat& l, Mat& u, Mat& p) const
	{   // notice templated member functions are only instantiated on calling,
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
	        //      u.max_abs(IterationDir::ROWS, c);
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
	        if(max_v.second != c) {
	            for (int col = c; col < cols; ++col) {
	                std::swap(u(c, col), u(max_v.second, col));
	            }

	            for (int col = 0; col < c; ++col) {
	                std::swap(l(c, col), l(max_v.second, col));
	            }
	        }

	        // the row-reduce equation is
	        // (row = row - fact * upper rows)
	        // this 1.0 is the weight of the row, and the upper
	        // row coefficients are stores to the left. i.e. in
	        // l(c', c), c' < c.
	        l(c, c) = 1.0;
	        for (int r = c+1; r < rows; ++r) {
	            double fact = u(r, c) / max_v.first; // reduction factor

	            // reduce equation number col
	            for (int col = c; col < cols; ++col) {
	                u(r, col) -= fact * u(c, col);
	            }

	            // populate lower matrix with the factors we have used.
	            l(r, c) = fact;
	        }
	    }

	    p = permutation<ElType, perm.size()>(perm);
	    return true;
	}


	template<typename ElType, int rows, int cols>
	inline bool Mat<ElType, rows, cols>::
	solve(const Mat<ElType, rows, 1>& rhs,
			  Mat<ElType, rows, 1>& sol,
			  EqSysType type) const
	{
		switch(type) {
		case EqSysType::GENERAL:
			return this->solve_general(rhs, sol);

		case EqSysType::LOWER_TRIANG:
			return this->fwd_subst(rhs, sol);

		case EqSysType::UPPER_TRIANG:
			return this->back_subst(rhs, sol);

		default:
			assert(false &&
				   "undefined system of equation type "
				   "passed to Mat::solve_for, please review sources");
		}
	}

    template<typename ElType, int rows, int cols>
    inline bool Mat<ElType, rows, cols>::
    solve_from_lup(const Mat<ElType, rows, 1>& rhs,
                   const Mat& l, const Mat& u, const Mat& p,
                   Mat<ElType, rows, 1>& sol) const
    {
        // (*this) * sol = p.transp() * l * u * sol = rhs;

        // step 1: fwd_subst solve l * y = p * rhs;
        auto p_rhs = p * rhs;
        Mat<ElType, rows, 1> y;
        bool success = l.fwd_subst(p_rhs, y);
        if(!success) {
            return false;
        }

        // step 2: back_subst solve u * sol = y;
        success = u.back_subst(y, sol);
        if(!success) {
            return false;
        }

        return true;
    }

	/// solves general systems of equations
	template<typename ElType, int rows, int cols>
	inline bool Mat<ElType, rows, cols>::
	solve_general(const Mat<ElType, rows, 1>& rhs,
	              Mat<ElType, rows, 1>& sol) const
	{
	    Mat l, u, p;
	    this->lup_decomp(l, u, p);
	    return this->solve_from_lup(rhs, l, u, p, sol);
	}

	/// solves upper triangular systems of equations
	template<typename ElType, int rows, int cols>
	bool Mat<ElType, rows, cols>::
	back_subst(const Mat<ElType, rows, 1>& rhs,
	           Mat<ElType, rows, 1>& sol) const
	{
	    sol = rhs; // sol will also keep our temp, mutable rhs vector.

	    for (int r = rows -1; r > -1; --r) {
	        // step 1: row reduce from lower solutions
	        for (int c = r+1; c < cols; ++c) {
	            sol(r, 0) -= (*this)(r, c) * sol(c, 0);
	        }

	        // step 2: compute next answer
	        double div_with = (*this)(r, r);
	        // TODO maybe guard with a flag for no check? (well, outside for loop obviously)
	        if(std::abs(div_with) < std::numeric_limits<ElType>::epsilon()) {
	            return false; // matrix singular !
	        }
	        sol(r, 0) /= div_with;
	    }

	    return true;
	}

	/// solves lower triangular systems of equations
	template<typename ElType, int rows, int cols>
	bool Mat<ElType, rows, cols>::
	fwd_subst(const Mat<ElType, rows, 1>& rhs,
	          Mat<ElType, rows, 1>& sol) const
	{
	    sol = rhs; // sol will also keep our temp, mutable rhs vector.

	    for (int r = 0; r < rows; ++r) {
	        for (int c = r - 1; c > -1; --c) {
	            // step 1: row reduce from higher solutions
	            sol(r, 0) -= (*this)(r, c) * sol(c, 0);
	        }

            // step 2: compute next answer
            double div_with = (*this)(r, r);
            // TODO maybe guard with a flag for no check? (well, outside for loop obviously)
            if(std::abs(div_with) < std::numeric_limits<ElType>::epsilon()) {
                return false; // matrix singular !
            }
            sol(r, 0) /= div_with;
	    }
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
				lhs.buf[0], rows, rhs.buf[0], n, 0.0, retval.buf[0], rows);

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
