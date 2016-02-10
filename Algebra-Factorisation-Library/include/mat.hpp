/*
 * mat.hpp
 *
 *  Created on: Jan 16, 2016
 *      Author: moro
 */

#include <cblas.h>
#include <array>

#include <functional>
#include <utility>

#include <type_traits>

#ifndef MAT_HPP_
#define MAT_HPP_

namespace afl {

	template <typename ElType, int rows, int cols>
	class Mat;

	template <typename ElType, int cols>
	using ColVect = Mat<ElType, 1, cols>;

	/// @brief factory function, returns Identity matrix
	template <typename ElType, int dim>
	Mat<ElType, dim, dim>
	eye();

    /// @brief factory function, returns permutation matrix
    /// that will permute the rows of another matrix when
    /// multiplied.
    /// trick: visualise afl::permutation as the permuted eye()
    /// and generating one will become very easy.
    /// (because permutation * eye == permutation).
    template <typename ElType, int dim>
    Mat<ElType, dim, dim>
    permutation(const std::array<int, dim>& p);

    /// TODO transform into iterator and make min, min_abs
    /// max, and max_abs to work with iterators such that we can
    /// easily index ROIs within matrices.
    ///
    /// @brief enum class controlling the behaviour
    /// of different cumulating member functions of afl::Mat
    enum class IterationDir { ROWS, COLS };

	/// @brief reduces matrix using function object passed in.
	/// iteration starts from (0,0) and progresses first along the
    /// direction passed in via the dir parameter (i.e. if
    /// dir == IterationDir::COLS then the next element after
    /// (0,0) will be (0,1) ).
	template <typename SeedType,
	          typename ElType,
	          int rows, int cols>
	inline SeedType
	reduce_with(std::function<SeedType(SeedType, ElType)> reducer,
	            const Mat<ElType, rows, cols>& mat,
	            SeedType seed = SeedType(0),
	            IterationDir dir = IterationDir::COLS);

	/// @brief this is a compile time version of,
	/// the more general reduce_with, for when we know the
	/// static address of the function being passed to.
	/// TODO test if local lambdas (who do not get instantiated
	/// with this template) are also inlined.
    template <typename SeedType,
              typename ElType,
              int rows, int cols,
              SeedType(*reducer)(SeedType, ElType)>
    inline SeedType
    reduce_with(const Mat<ElType, rows, cols>& mat,
                SeedType seed = SeedType(0));


	/// @brief class determining type of system to solve.
	enum class EqSysType { UPPER_TRIANG, LOWER_TRIANG, GENERAL };

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

		/// TODO template operator() on iteration dir parameter
		/// and specialise, also treat the rows == 1 or cols == 1
		/// case specially when iterating along that direction (ie we
		/// don't need to divide anymore).
		///
		/// @brief access based on single index, const version.
		/// proceeds alog matrix by first iteration along direction
		/// specified in iteration dir enum. (i.e. if dir == IterationDir::COLS
		/// then if index is 1, function will try to index elem (0, 1), otherwise
		/// (1, 0) ).
		inline ElType
		operator() (int idx, IterationDir dir = IterationDir::COLS) const;


        /// @brief access based on single index, non-const version.
        /// proceeds alog matrix by first iteration along direction
        /// specified in iteration dir enum. (i.e. if dir == IterationDir::COLS
        /// then if index is 1, function will try to index elem (0, 1), otherwise
        /// (1, 0) ).
		inline ElType&
		operator() (int idx, IterationDir dir = IterationDir::COLS);

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

		/// @brief accumulator type multiplication, modifies this in-place.
		inline Mat&
		operator *= (const Mat<ElType, cols, rows>& rhs);

		/// @brief accumulator type addition, modifies this in-place.
		inline Mat&
		operator += (const Mat<ElType, rows, cols>& rhs);

		/// @brief accumulator type substitution, modifies this in-place.
		inline Mat&
		operator -= (const Mat<ElType, rows, cols>& rhs);

		/// @brief performs element-wise multiplication of matrices.
		inline Mat
		elem_mul(const Mat& rhs) const;

		/// @brief performs element-wise division of matrices
		/// (elems in this are the numertators).
		inline Mat
		elem_div(const Mat& rhs) const;

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
		bool
		inv(Mat& inverse) const;


        /// @brief performs L-U-P decomposition of this.
        /// @return success status (fails if matrix is singular).
        bool
        lup_decomp(Mat& l, Mat& u, Mat& p) const;

		/// @brief tries to solve for equation
		///  (*this) * sol == rhs.
		/// @param rhs right hand side of equation
		/// @param sol solution, output variable
		/// @param type determine if system is triangular or general
		/// @return success status.
		inline bool
		solve(const Mat<ElType, rows, 1>& rhs,
		      Mat<ElType, rows, 1>& sol,
		      EqSysType type = EqSysType::GENERAL) const;

        /// @brief if lup decomposition is already known, system can
		/// be solved more efficiently. this can be helpful if you
		/// have to solve for many equations of the form
        ///  (*this) * sol == rhs.
		/// as you can precompute lup decomposition and then save time
		/// on subsequent calls.
		///
		/// @invariant p * (*this) == l * u;
        /// @param rhs right hand side of equation
        /// @param l lower triangular matrix
		/// @param u upper traingular matrix
		/// @param p permutation matrix
		/// @param sol solution, output variable
        /// @return success status.
		inline bool
		solve_from_lup(const Mat<ElType, rows, 1>& rhs,
		               const Mat& l, const Mat& u, const Mat& p,
		               Mat<ElType, rows, 1>& sol) const;

		/// @brief returns sum of squares of elements.
		inline ElType
		l2_norm() const;

		/// @brief return sum of elements.
		inline ElType
		l1_norm() const;

		/// @brief returns max element.
		inline ElType
		linf_norm() const;

	private:
		/// solves general systems of equations
		inline bool
		solve_general(const Mat<ElType, rows, 1>& rhs,
		              Mat<ElType, rows, 1>& sol) const;

		/// solves upper triangular systems of equations
		bool back_subst(const Mat<ElType, rows, 1>& rhs,
						Mat<ElType, rows, 1>& sol) const;

		/// solves lower triangular systems of equations
		bool fwd_subst(const Mat<ElType, rows, 1>& rhs,
						Mat<ElType, rows, 1>& sol) const;

		/// col-major storage
		ElType buf[cols][rows];
	};


} /* namespace afl */

#include "mat-inl.hpp"
#include "mat-decomp.hpp"
#include "mat-functions.hpp"

#endif /* MAT_HPP_ */
