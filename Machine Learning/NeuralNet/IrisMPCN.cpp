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

/// @brief template class modeling a neural network.
template<int inputs,int nlayer0, int outputs>
class NeuralNet2Layer {
public :

	/// @brief class modeling a single neuron layer
	/// within the neural network.
	template<int layer_inputs, int neurons>
	class NNLayer {

		/// grant access to internals,
		/// NNLayer can only be part of a larger
		/// neural network, and is not intended to be
		/// instantiated anywhere else.
		template<int in, int temp, int out>
		friend class NeuralNet2Layer;

	public:


		/// output vector type.
		using oVect = Matrix<double,1,neurons>;

		/// input vector type.
		using iVect = Matrix<double,1,layer_inputs>;

		  /// @brief evaluate perceptron layer with input.
		  std::shared_ptr<oVect> eval(const iVect& in) {
			  // add bias term so we can
			  Matrix<double,1,layer_inputs+1> inBias;

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

		  /// @brief perfom single weight update
		  void update_weights(const Matrix<double,layer_inputs+1,neurons>& deltas) {
			  for(int i = 0; i < deltas.size(); i++) {
				  weights(i) += deltas(i);
			  }
		  }

		  /// @brief print weight to ostream o.
		  void show_weights(std::ostream& o) { o<<weights; }

	private:
		  /// make sure only NeuralNets can instantiated this class.
		  NNLayer();

		  /// make sure only NeuralNets can instantiated this class.
		  NNLayer (std::function<double (void)> learn_rate,
		  				std::function<double (double)> activation_fn,
		  				std::function<double (double)> act_fn_deriv) //derivative
		  		: weights {Matrix<double,layer_inputs+1,neurons>::Random()},
		  		  lrate {learn_rate}, actfn {activation_fn},
		  		  actderiv {act_fn_deriv} {};


		  Matrix<double,layer_inputs+1,neurons> weights;
		  std::function<double (void)> lrate; //learning rate
		  std::function<double (double)> actfn; //activation function
		  std::function<double (double)> actderiv; //activation function derivative
	};


	// --- Aliases providing abstraction for passing data ---

	/// the usual input vector type.
	using iVect = Matrix<double,1,inputs>;

	/// hidden layer vector type.
	using tempVect = Matrix<double,1,nlayer0>;

	/// output layer vector type
	using oVect = Matrix<double,1,outputs>;

	// --- Aliases for sets of input examples ---

	/// input set: features.
	using iMatrix = std::vector<std::shared_ptr<iVect>>;

	/// output set: classes
	using oMatrix = std::vector<std::shared_ptr<oVect>>;


	NeuralNet2Layer(std::function<double (void)> learn_rate,
			std::function<double (double)> activation_fn,
			std::function<double (double)> activation_deriv)
	: layer0 {NNLayer<inputs,nlayer0>(learn_rate,activation_fn,activation_deriv)},
	  layer1 {NNLayer<nlayer0,outputs>(learn_rate,activation_fn,activation_deriv)}
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
	NNLayer<inputs,nlayer0> layer0;
	NNLayer<nlayer0,outputs> layer1;

};


double sigmoid(double x);
double sigmoid_deriv(double x);



int main() {
	using namespace std;

	NeuralNet2Layer<4,4,3> irisPcn( []() { return 0.25; }, //learn rate
			sigmoid,sigmoid_deriv); //activation fn and its derivative

	using oVect = NeuralNet2Layer<4,4,3>::oVect; //output vector
	using iVect = NeuralNet2Layer<4,4,3>::iVect; //input vector

	using iMatrix = NeuralNet2Layer<4,4,3>::iMatrix;
	using oMatrix = NeuralNet2Layer<4,4,3>::oMatrix;

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

