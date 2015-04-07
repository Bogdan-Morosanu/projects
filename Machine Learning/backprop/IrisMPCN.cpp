#include <iostream>
#include <cstdio>
#include <cassert>
#include <cstddef>
#include <cmath>
#include <Eigen/Dense>
#include <vector>
#include <functional>
#include <memory>

using namespace Eigen;

template<int inputs, int neurons>
class PcnLayer {

  using oVect = Matrix<double,1,neurons>; //output vector
  using iVect = Matrix<double,1,inputs>; //input vector


public :
  PcnLayer (std::function<double (void)> learn_rate,
	    std::function<double (double)> activation_fn,
	    std::function<double (double)> act_fn_deriv) //derivative
    : weights {Matrix<double,inputs+1,neurons>::Random()},
    lrate {learn_rate}, actfn {activation_fn},
    actderiv {act_fn_deriv} {};

  std::shared_ptr<oVect> eval(iVect& in) {

    Matrix<double,1,inputs+1> inBias; //inputs + Bias

    for(int i = 0; i < in.size(); i++) {
      inBias(i) = in(i); //copy inputs
    }

    inBias(in.size()) = -1; //bias term


    std::shared_ptr<oVect> res {new oVect()};



    (*res) = inBias * weights; //compute cross products

    for(int i = 0; i < res->size(); i++) {
      (*res)(i) = actfn((*res)(i)); //apply threshold
    }



    return res;
  }

  void update_weights(Matrix<double,inputs+1,neurons>& deltas) {
    for(int i = 0; i < deltas.size(); i++) {
      weights(i) += deltas(i);
    }
  }

  void show_weights(std::ostream& o) { o<<weights; }

  Matrix<double,inputs+1,neurons> weights;
  std::function<double (void)> lrate; //learning rate
  std::function<double (double)> actfn; //activation function
  std::function<double (double)> actderiv; //activation function derivative
};



template<int inputs,int nlayer0, int outputs>
class Pcn2L {
public :
  
  /** Aliases providing abstraction for passing data*/
  using iVect = Matrix<double,1,inputs>;
  using tempVect = Matrix<double,1,nlayer0>;
  //tempVect used to pass data between layer 0 and layer 1
  //should technically be private, but is placed here for better visibility
  using oVect = Matrix<double,1,outputs>;

  using iMatrix = std::vector<std::shared_ptr<iVect>>;
  using oMatrix = std::vector<std::shared_ptr<oVect>>;
  

  Pcn2L(std::function<double (void)> learn_rate,
	std::function<double (double)> activation_fn,
	std::function<double (double)> activation_deriv)
    : layer0 {PcnLayer<inputs,nlayer0>(learn_rate,activation_fn,activation_deriv)},
    layer1 {PcnLayer<nlayer0,outputs>(learn_rate,activation_fn,activation_deriv)}
  {};

  std::shared_ptr<oVect> eval(iVect& in) {
    std::shared_ptr<tempVect> temp = layer0.eval(in);
    //automatically frees when out of scope!

    return layer1.eval(*temp);
  }

  std::shared_ptr<oMatrix> eval(iMatrix& inMx) {
    std::shared_ptr<oMatrix> out {new oMatrix(inMx.size())};
    for(int i = 0; i < inMx.size(); i++) {
      (*out)[i] = this->eval(*(inMx[i]));
    }

    return out;
  }

  void show_weights(std::ostream& os) {
    os<<"Layer 0 :\n";
    layer0.show_weights(os);
    os<<"\nLayer 1 :\n";
    layer1.show_weights(os);
    os<<"\n";
  }


  void train(iVect& in, oVect& target) {

    /** Updating Layer 1 */

    std::vector<double> deriv_l1(target.size());
    //used to hold (t-y) * activation_deriv
    //note :: we use a least squares sum error function

    std::shared_ptr<tempVect> hidden = layer0.eval(in);
    std::shared_ptr<oVect> out = layer1.eval(*hidden);

    for(int i = 0; i < target.size(); i++) {
      deriv_l1[i] = (target(i) - (*out)(i)) //derivative of error fn
	* (*out)(i) * (1-(*out)(i)) ; //derivative of act_fn
    } //initialises derivatives terms
    //assumes logistic sigmoid function for activation
    // notice we can't use the layer1.actderiv since 
    // we do not necessarily know x, but rather f(x).

    using DeltaL1 = Matrix<double,nlayer0 + 1,outputs>;
    std::unique_ptr<DeltaL1> deltas_l1 { new DeltaL1()};

    for(int i = 0; i < target.size(); i++) {
      for(int j = 0; j < hidden->size(); j++) {
	(*deltas_l1)(j,i) = layer1.lrate() * deriv_l1[i] * (*hidden)(j);
      }

      //bias terms
      (*deltas_l1)(hidden->size(),i) = layer1.lrate() * deriv_l1[i] * -1;
    }



    /**Updating Layer 0 */
    std::vector<double> deriv_l0(hidden->size());

    for(int i = 0; i < hidden->size(); i++) {
      double backprop_l1 = 0; //the back-propagation from layer 1
      for(int j = 0; j < target.size(); j++) {
	backprop_l1 += layer1.weights(i,j) * deriv_l1[j];
      }

      deriv_l0[i] = (*hidden)[i] * (1-(*hidden)[i]) //derivative of hidden actfn
	* backprop_l1; //add back propagation
    }

    using DeltaL0 = Matrix<double,inputs + 1,nlayer0>;
    std::unique_ptr<DeltaL0> deltas_l0 { new DeltaL0()};

    for(int i = 0; i < hidden->size(); i++) {
      for(int j = 0; j < in.size(); j++) {
	(*deltas_l0)(j,i) = layer0.lrate() * deriv_l0[i] * in(j);
      }

      //bias terms
      (*deltas_l0)(in.size(),i) = layer0.lrate() * deriv_l0[i] * -1;
    }


    layer1.update_weights(*deltas_l1);
    layer0.update_weights(*deltas_l0);
  }
  
  void train(iMatrix& inMx, oMatrix& target_Mx) {
    for(int i = 0; i < inMx.size(); i++) {
      this->train(*inMx[i],*target_Mx[i]);
    }
  }



  double error(iMatrix& inMx, oMatrix& target) {
    /** Using a least-squares error function */
    double total_err = 0;

    for(int i = 0; i < inMx.size(); i++) {

      std::shared_ptr<iVect> in = inMx[i];
      std::shared_ptr<oVect> out = this->eval(*in);

      double output_err = 0;

      for(int j = 0; j < out->size(); j++) {
	output_err += (*out)(j) - (*target[i])(j);
      }

      total_err += (output_err * output_err);


    }

    return total_err;
  }

private :
  PcnLayer<inputs,nlayer0> layer0;
  PcnLayer<nlayer0,outputs> layer1;

};


double sigmoid(double x);
double sigmoid_deriv(double x);



int main() {
  using namespace std;

  Pcn2L<4,4,3> irisPcn( []() { return 0.25; }, //learn rate
			sigmoid,sigmoid_deriv); //activation fn and its derivative

  using oVect = Pcn2L<4,4,3>::oVect; //output vector
  using iVect = Pcn2L<4,4,3>::iVect; //input vector

  using iMatrix = Pcn2L<4,4,3>::iMatrix;
  using oMatrix = Pcn2L<4,4,3>::oMatrix;




  iMatrix iris_in;
  oMatrix iris_target;

  FILE* iris = fopen("iris_edited.data","r");
  assert(iris);
  for(int i = 0; i < 150; i++) {

    double swidth, slen, pwidth, plen;
    int setosa, versicolor, virginica;

    fscanf(iris,"%lf,%lf,%lf,%lf,%d,%d,%d\n",
	   &swidth,&slen,&pwidth,&plen,&setosa,&versicolor,&virginica);

    iris_in.push_back( shared_ptr<iVect> {new iVect()} );
    (*iris_in[i]) << swidth, slen, pwidth, plen;

    iris_target.push_back( shared_ptr<oVect> {new oVect()} );
    (*iris_target[i]) << setosa, versicolor, virginica;


  }

  /** show data */
      for(int i = 0; i < 150; i++) {
      cout<<(*iris_in[i])(0)<<','<<(*iris_in[i])(1)<<','
      <<(*iris_in[i])(2)<<','<<(*iris_in[i])(3)<<','
      <<(*iris_target[i])(0)<<','<<(*iris_target[i])(1)<<','
      <<(*iris_target[i])(2)<<'\n';
      }
  


  /**
     we shall pick training data as the first 40 of each 50 occurrences
  */

  cout<<"Error before training : "<<irisPcn.error(iris_in,iris_target)<<'\n';

  /*
  for(int i = 0; i < 150 * 3000; i++) {
    if(i % (150 * 1000) == 0) {
      cout<<"Error after training "<< (i / 150 )
	  <<" times : "<<irisPcn.error(iris_in,iris_target)<<'\n';
      cout<<"press any key to continue\n";
      char c;
      cin>>c;
    }
     
    //skip test data
    if(i > 39 && i < 50) continue;
    if(i > 89 && i < 100) continue;
    if(i > 139 && i < 150) continue;
     
    irisPcn.train(*iris_in[i%150],*iris_target[i%150]);
  }
  */
  
  for(int i =0; i < 10000; i++)  {

    if(i % 500 == 0) cout<<"passed "<<i<<" mark"<<endl;

    irisPcn.train(iris_in,iris_target);
  }
  
  for(int i = 0; i < 30; i++) {
    shared_ptr<oVect> out;
    if(i < 10) out = irisPcn.eval(*iris_in[i+40]);
    else if(i < 20) out = irisPcn.eval(*iris_in[i+80]);
    else if(i < 30) out = irisPcn.eval(*iris_in[i+120]);

    cout<<"eval @ "<<i<<' '<<(*out)(0)<<' '
	<<(*out)(1)<<' '<<(*out)(2)<<"\n";
  }
  

  return 0;
}


double sigmoid(double x) {
  return 1.0 / (1.0 + exp(-x));
}

double sigmoid_deriv(double x) {
  return sigmoid(x) * (1 - sigmoid(x));
}

