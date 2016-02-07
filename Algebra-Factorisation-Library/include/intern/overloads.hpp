#ifndef OVERLOADS_HPP
#define OVERLOADS_HPP

#include <cblas.h>
#include <iostream>

namespace afl {
	namespace intern {

		/// @brief helper that instantiates operator * for afl::Mat
		/// with the right C interface call.
		/// we've overloaded some function calls
		/// instead of using the C dgemm, sgemm and so on.
		/// this has the benefit that afl::Mat's with ElTypes that
		/// do not have gemm ops defined will not compile.
		inline void
		overload_resolver_gemm(CBLAS_LAYOUT layout, CBLAS_TRANSPOSE TransA,
				CBLAS_TRANSPOSE TransB, const int M, const int N,
				const int K, const double alpha, const double *A,
				const int lda, const double *B, const int ldb,
				const double beta, double *C, const int ldc)
		{
		    cblas_dgemm(layout, TransA, TransB, M, N, K,
					alpha, A, lda, B, ldb, beta, C, ldc);
		}


		/// @brief helper that instantiates operator * for afl::Mat
		/// with the right C interface call.
		/// we've overloaded some function calls
		/// instead of using the C dgemm, sgemm and so on.
		/// this has the benefit that afl::Mat's with ElTypes that
		/// do not have gemm ops defined will not compile.
		inline void
		overload_resolver_gemm(CBLAS_LAYOUT layout, CBLAS_TRANSPOSE TransA,
				CBLAS_TRANSPOSE TransB, const int M, const int N,
				const int K, const float alpha, const float *A,
				const int lda, const float *B, const int ldb,
				const float beta, float *C, const int ldc)
		{
			cblas_sgemm(layout, TransA, TransB, M, N, K,
					alpha, A, lda, B, ldb, beta, C, ldc);
		}
	}
}

#endif
