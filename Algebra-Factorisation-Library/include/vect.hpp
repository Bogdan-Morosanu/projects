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
        Vect () {}

        Vect project(const Vect& rhs) const;

        Vect normalized() const;

        /// TODO decide if virtual dtor is needed ?
        virtual ~Vect () {}
    };

} /* namespace afl */

#include "vect-inl.hpp"

#endif /* INCLUDE_VECT_HPP_ */
