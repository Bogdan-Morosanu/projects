#include <cmath>
#include <iostream>
#include <fstream>

#include "kern.hpp"
#include "expr.hpp"

void test_2d_kernel(const Kernel<2>& kern,
		    std::ofstream& dump,
		    int sample_grann, 
		    double sample_width);


void test_2d_expression(const Expression<2>& kern,
			std::ofstream& dump,
			int sample_grann, 
			double sample_width);


int main() {
  // for a simple second test
  // let's just output in a csv our kernels over 2D surfaces
  CovMat<2> eye;
  eye << 1, 0, 
         0, 1;
  
  FeatVec<2> mean;
  mean << 2, 2;

  const std::vector<double> std_params = {1.0, 1.0, 1.0};
  const std::vector<double> lin_params = {-2.0, 1.0, 1.0};
  
  KernExpression<2> se(se_generator<2>(std_params), mean, eye);
  KernExpression<2> per(per_generator<2>(std_params), mean, eye);
  KernExpression<2> lin(lin_generator<2>(lin_params), mean, eye);
  KernExpression<2> rq(rq_generator<2>(std_params), mean, eye);
  
  auto& se_x_per = se * per;
  auto& se_x_lin = se * lin;
  auto& per_x_lin = per * lin;
  auto& per_p_lin = per + lin;
  auto& se_p_per = se + per;

  std::ofstream se_dump("../../data-plots/se.csv");
  std::ofstream per_dump("../../data-plots/per.csv");
  std::ofstream lin_dump("../../data-plots/lin.csv");
  std::ofstream rq_dump("../../data-plots/rq.csv");
  std::ofstream se_x_per_dump("../../data-plots/se_x_per.csv");
  std::ofstream se_x_lin_dump("../../data-plots/se_x_lin.csv");
  std::ofstream per_x_lin_dump("../../data-plots/per_x_lin.csv");
  std::ofstream per_p_lin_dump("../../data-plots/per_p_lin.csv");
  std::ofstream se_p_per_dump("../../data-plots/se_p_per.csv");

    
  test_2d_expression(se, se_dump, 100, 4);
  test_2d_expression(per, per_dump, 100, 4);
  test_2d_expression(lin, lin_dump, 100, 4);
  test_2d_expression(rq, rq_dump, 100, 4);
  test_2d_expression(se_x_per, se_x_per_dump, 100, 4);
  test_2d_expression(se_x_lin, se_x_lin_dump, 100, 4);
  test_2d_expression(se_p_per, se_p_per_dump, 100, 4);
  test_2d_expression(per_x_lin, per_x_lin_dump, 100, 4);
  test_2d_expression(per_p_lin, per_p_lin_dump, 100, 4);

  return 0;
}

void test_2d_expression(const Expression<2>& expr,
			std::ofstream& dump,
			int sample_grann, 
			double sample_width) {
  // \todo it would be a good idea to 
  // encode the sample granularity and width or some generalised
  // octave/matlab header in the .csv, but a simple
  // ascii file will do for now.

  double scaling = sample_width / sample_grann;

  for(int r = 0; r < sample_grann; r++) {
    for(int c = 0; c < sample_grann; c++) {
      FeatVec<2> x;
      x << r * scaling , c * scaling;
      dump << expr(x) << ",";
    }
    dump << std::endl;
  }


}

void test_2d_kernel(const Kernel<2>& kern,
		    std::ofstream& dump,
		    int sample_grann, 
		    double sample_width) {
  
  // \todo it would be a good idea to 
  // encode the sample granularity and width or some generalised
  // octave/matlab header in the .csv, but a simple
  // ascii file will do for now.

  double scaling = sample_width / sample_grann;

  for(int r = 0; r < sample_grann; r++) {
    for(int c = 0; c < sample_grann; c++) {
      FeatVec<2> x;
      x << r * scaling , c * scaling;
      dump << kern(x) << ",";
    }
    dump << std::endl;
  }

}

/// @brief computes the mahalanobis distance as a kernel function.
template<int K>
double mh_dist(const FeatVec<K>& a,
	       const FeatVec<K>& b,
	       const CovMat<K>& covar) {
  
  auto dist = (a - b);
  auto dist_tr = dist.transpose();
  return std::sqrt(dist_tr * covar.inverse() * dist);
}

/// @brief factory function for constructing
/// squared exponentinal kernels.
/// @param params contains sqrt(amplitude) in params[0]
/// and sqrt(width) in params[1].
/// @return squared exponential kernel function.
template<int K>
KernFunction<K> se_generator(const std::vector<double>& params) {
  const double amplitude = params[0] * params[0];
  const double width = params[1] * params[1];
  
  return [amplitude, width](const FeatVec<K>& a, 
			    const FeatVec<K>& b,
			    const CovMat<K>& covar) {
    const FeatVec<K> x = a - b;
    const double norm_sq = x.transpose() * covar * x;
    return amplitude * std::exp(-norm_sq / (2 * width));
    
  };
}


/// @brief factory function for constructing
/// generalised periodic kernels.
/// @param params contains sqrt(amplitude) in params[0], 
/// sqrt(width) in params[1] and period in params[2].
/// @return generalised periodic kernel function.
template<int K>
KernFunction<K> per_generator(const std::vector<double>& params) {
  const double amplitude = params[0] * params[0];
  const double width = params[1] * params[1];
  const double period = params[2];
  
  return [amplitude, width, period](const FeatVec<K>& a,
				    const FeatVec<K>& b,
				    const CovMat<K>& covar) {
    const FeatVec<K> x = a - b;
    const double norm = std::sqrt(x.transpose() * covar * x);
    const double sinval = std::sin(M_PI * norm / period);
    return amplitude * std::exp(-2 * sinval * sinval / width);
  };
}

/// @brief factory function for constructing liniar kernels.
/// @param params contains bias in params[0], 
/// sqrt(gain) in params[1] and lag in params[2].
/// @return liniar kernel function.
template<int K>
KernFunction<K> lin_generator(const std::vector<double>& params) {
  // \todo look into why negative bias was not wanted?
  // this might have something to do with the kernel not
  // being positive deffinite any longer.
  const double bias = params[0];
  const double gain = params[1] * params[1];
  FeatVec<K> lag;
  // \todo refactor into initialisation method
  for(int i = 0; i < K; i++) { lag(i) = params[2]; }

  return [bias, gain, lag](const FeatVec<K>& a,
			   const FeatVec<K>& b,
			   const CovMat<K>& covar){
    return bias + gain * (a - lag).transpose() * covar * (b - lag);
  };
}

/// @brief factory function for constructing 
/// rational quadratic kernels.
/// @param params contains sqrt(amplitude) in params[0], 
/// sqrt(width) in params[1] and alpha in params[2], where
/// higher alpha values lead to the rational quadratic kernel to 
/// asymptotically approach the squared exponential.
/// @return rational quadratic kernel function.
template<int K>
KernFunction<K> rq_generator(const std::vector<double>& params) {
  const double amplitude = params[0] * params[0];
  const double width = params[1] * params[1];
  const double alpha = params[2];
  
  return [amplitude, width, alpha](const FeatVec<K>& a,
				   const FeatVec<K>& b,
				   const CovMat<K>& covar) {
    const FeatVec<K> x = a - b;
    const double norm_sq = x.transpose() * covar * x;
    const double base = 1 + norm_sq / (2 * alpha * width);
    return amplitude * std::pow(base, -alpha);
  };
}



void simple_test() {
  CovMat<3> eye;
  eye << 1, 0, 0,
         0, 1, 0,
         0, 0, 1;

  FeatVec<3> zero;
  zero << 0, 0, 0;

  Kernel<3> euclidian(mh_dist<3>, zero, eye);
    
  FeatVec<3> pt;
  pt << 0, 1, 1;
  
  double sq_root_two = euclidian(pt);
  
  std::cout << sq_root_two << " " << std::sqrt(2.0) << std::endl;

}


void kern_only_test() {
   // for a simple second test
  // let's just output in a csv our kernels over 2D surfaces
  CovMat<2> eye;
  eye << 1, 0, 
         0, 1;
  
  FeatVec<2> mean;
  mean << 2, 2;

  const std::vector<double> std_params = {1.0, 1.0, 1.0};
  const std::vector<double> lin_params = {-2.0, 1.0, 1.0};

  auto se_kfn = se_generator<2>(std_params);
  auto per_kfn = per_generator<2>(std_params);
  auto lin_kfn = lin_generator<2>(lin_params);
  auto rq_kfn = rq_generator<2>(std_params);

  auto se_x_per = [se_kfn, per_kfn]
    (const FeatVec<2>& a, const FeatVec<2>&b, const CovMat<2>& covar) {
    return se_kfn(a, b, covar) * per_kfn(a, b, covar);
  };
  
  auto se_x_lin = [se_kfn, lin_kfn]
    (const FeatVec<2>& a, const FeatVec<2>&b, const CovMat<2>& covar) {
    return se_kfn(a, b, covar) * lin_kfn(a, b, covar);
  };

  auto per_x_lin = [per_kfn, lin_kfn]
    (const FeatVec<2>& a, const FeatVec<2>&b, const CovMat<2>& covar) {
    return per_kfn(a, b, covar) * lin_kfn(a, b, covar);
  };
  
  auto per_p_lin =  [per_kfn, lin_kfn]
    (const FeatVec<2>& a, const FeatVec<2>&b, const CovMat<2>& covar) {
    return per_kfn(a, b, covar) + lin_kfn(a, b, covar);
  };
  
  auto se_p_per =  [se_kfn, per_kfn]
    (const FeatVec<2>& a, const FeatVec<2>&b, const CovMat<2>& covar) {
    return se_kfn(a, b, covar) + per_kfn(a, b, covar);
  };
  
  
  Kernel<2> squared_exp(se_kfn, mean, eye);
  Kernel<2> periodic(per_kfn, mean, eye);
  Kernel<2> liniar(lin_kfn, mean, eye);
  Kernel<2> rational_quad(rq_kfn, mean, eye);
  Kernel<2> se_times_per(se_x_per, mean, eye);
  Kernel<2> se_times_lin(se_x_lin, mean, eye);
  Kernel<2> per_times_lin(per_x_lin, mean, eye);
  Kernel<2> per_plus_lin(per_p_lin, mean, eye);
  Kernel<2> se_plus_per(se_p_per, mean, eye);

  std::ofstream se_dump("../../data-plots/se.csv");
  std::ofstream per_dump("../../data-plots/per.csv");
  std::ofstream lin_dump("../../data-plots/lin.csv");
  std::ofstream rq_dump("../../data-plots/rq.csv");
  std::ofstream se_x_per_dump("../../data-plots/se_x_per.csv");
  std::ofstream se_x_lin_dump("../../data-plots/se_x_lin.csv");
  std::ofstream per_x_lin_dump("../../data-plots/per_x_lin.csv");
  std::ofstream per_p_lin_dump("../../data-plots/per_p_lin.csv");
  std::ofstream se_p_per_dump("../../data-plots/se_p_per.csv");

    
  test_2d_kernel(squared_exp, se_dump, 100, 4);
  test_2d_kernel(periodic, per_dump, 100, 4);
  test_2d_kernel(liniar, lin_dump, 100, 4);
  test_2d_kernel(rational_quad, rq_dump, 100, 4);
  test_2d_kernel(se_times_per, se_x_per_dump, 100, 4);
  test_2d_kernel(se_times_lin, se_x_lin_dump, 100, 4);
  test_2d_kernel(per_times_lin, per_x_lin_dump, 100, 4);
  test_2d_kernel(per_plus_lin, per_p_lin_dump, 100, 4);
  test_2d_kernel(se_plus_per, se_p_per_dump, 100, 4);

}
