/*
 * mat.hpp
 *
 *  Created on: Jan 16, 2016
 *      Author: moro
 */

#include <cblas.h>
#include <utility>
#include <array>

#ifndef MAT_HPP_
#define MAT_HPP_

namespace afl {
	template <typename ElType, int rows, int cols>
	class Mat;

	/// @brief factory function, returns Identity matrix
	template <typename ElType, int dim>
	Mat<ElType, dim, dim>
	eye();

	/// TODO transform into iterator and make min, min_abs
	/// max, and max_abs to work with iterators such that we can
	/// easily index ROIs within matrices.
	///
	/// @brief enum class controlling the behaviour
	/// of different cumulating member functions of afl::Mat
	enum class IterationDir { ROWS, COLS };

	/// @brief core Matrix class for linear algebra operations,
	/// class has non-virtual dtor, so do not inherit from it.
	template <typename ElType, int rows, int cols>
	class Mat {
	public:
		static_assert(rows > 0 && cols > 0, "matrix must have positive dimensions!");

		/// @brief default ctor, does not initialise values.
		Mat() {};

		/// @brief initialises matrix with initial value.
		/// @param val initial value.
		inline Mat(const ElType& val);

		/// @brief non-virtual dtor. class not meant to be inherited from!
		~Mat() {};

		/// access constant marix.
		inline ElType
		operator()(int row, int col) const;

		/// access non-const matrix.
		inline ElType&
		operator()(int row, int col);

		/// @brief friend function to multiply two matrices.
		/// a friend function is needed if we need type-conversions
		/// on both lhs and rhs arguments, and to have access
		/// to the buffer without copying.
		template <typename FnElType, int r, int c, int n>
		inline friend Mat<FnElType, r, c>
		operator * (const Mat<FnElType, r, n>& lhs,
					const Mat<FnElType, n, c>& rhs);

		/// @brief friend function to add two matrices.
		/// a friend function is needed if we need type-conversions
		/// on both lhs and rhs arguments, and to have access
		/// to the buffer without copying.
		template <typename FnElType, int r, int c, int n>
		inline friend Mat<FnElType, r, c>
		operator + (const Mat<FnElType, r, n>& lhs,
					const Mat<FnElType, n, c>& rhs);

		/// @brief friend function to subtract two matrices.
		/// a friend function is needed if we need type-conversions
		/// on both lhs and rhs arguments, and to have access
		/// to the buffer without copying.
		template <typename FnElType, int r, int c, int n>
		inline friend Mat<FnElType, r, c>
		operator - (const Mat<FnElType, r, n>& lhs,
					const Mat<FnElType, n, c>& rhs);

//  TODO find out why this does not link.
//		template <int n>
//		inline friend Mat<ElType, rows, cols>
//		operator * (const Mat<ElType, rows, n>& lhs,
//					const Mat<ElType, n, cols>& rhs);

		/// @brief accumulator type multiplication, modifies this in-place.
		inline Mat&
		operator *= (const Mat<ElType, cols, rows>& rhs);

		/// @brief accumulator type addition, modifies this in-place.
		inline Mat&
		operator += (const Mat<ElType, rows, cols>& rhs);

		/// @brief accumulator type substitution, modifies this in-place.
		inline Mat&
		operator -= (const Mat<ElType, rows, cols>& rhs);

		/// @brief performs L-U-P decomposition of this.
		/// @return success status (fails if matrix is singular).
		bool
		lup_decomp(Mat& l, Mat& u, Mat& p) const;

		/// @brief returns a pair of (element, index) that
		/// are the minimum on selected column or row.
		/// To find out the min pair of the 2nd column
		/// we would call Mat::min(ReduceOn::ROWS, 1).
		/// @param d direction to reduce on.
		/// @param idx index fixing other dimension.
		/// @return min (value, index) pair.
		inline std::pair<ElType, int>
		min(IterationDir d, int idx) const;

		/// @brief returns a pair of (element, index) that
		/// have the min absolute value on selected column or row.
		/// To find out the min abs pair of the 2nd column
		/// we would call Mat::min_abs(ReduceOn::ROWS, 1).
		/// @param d direction to reduce on.
		/// @param idx index fixing other dimension.
		/// @return min abs (value, index) pair.
		inline std::pair<ElType, int>
		min_abs(IterationDir d, int idx) const;

		/// @brief returns a pair of (element, index) that
		/// are the minimum on selected column or row.
		/// To find out the max pair of the 2nd column
		/// we would call Mat::max(ReduceOn::ROWS, 1).
		/// @param d direction to reduce on.
		/// @param idx index fixing other dimension.
		/// @return max (value, index) pair.
		inline std::pair<ElType, int>
		max(IterationDir d, int idx) const;

		/// @brief returns a pair of (element, index) that
		/// have the max absolute value on selected column or row.
		/// To find out the max abs val pair of the 2nd column
		/// we would call Mat::max_abs(ReduceOn::ROWS, 1).
		/// @param d direction to reduce on.
		/// @param idx index fixing other dimension.
		/// @return max absolute (value, index) pair.
		inline std::pair<ElType, int>
		max_abs(IterationDir d, int idx) const;

		/// @brief returns transposed matrix.
		inline Mat<ElType, cols, rows>
		transp() const;

		/// @brief returns matrix inverse.
		Mat inv() const;

	private:
		/// col-major storage
		ElType buf[cols][rows];
	};


} /* namespace afl */

#include "mat-inl.hpp"
#endif /* MAT_HPP_ */
