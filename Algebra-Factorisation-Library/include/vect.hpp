/*
 * vect.hpp
 *
 *  Created on: Feb 7, 2016
 *      Author: moro
 */

#ifndef INCLUDE_VECT_HPP_
#define INCLUDE_VECT_HPP_

#include "mat.hpp"

namespace afl {

    /// @brief template for vector (we assume column vectors).
    template <typename ElType, int dim>
    class Vect final : public Mat<ElType, dim, 1> {
    public:
        Vect (ElType val)
            : Mat<ElType, dim, 1>(val) {}

        Vect (const std::initializer_list<ElType>& ls)
            : Mat<ElType, dim,1 >(ls) {}

        /// @brief returns projection of *this on rhs.
        inline Vect
        projected(const Vect& rhs) const;

        /// @brief returns vector of length one,
        /// pointing in same direction as *this
        inline Vect
        normalized() const;

        /// @brief returns component of *this which is
        /// orthogonal to axis given.
        inline Vect
        orthogonalized(const Vect& axis) const;

        /// TODO decide if virtual dtor is needed ?
        virtual ~Vect () {}
    };

} /* namespace afl */

#include "vect-inl.hpp"

#endif /* INCLUDE_VECT_HPP_ */
