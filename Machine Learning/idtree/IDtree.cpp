#include <iostream>
#include <algorithm>
#include <vector>
#include <map>
#include <unordered_map>
#include <string>
#include <cmath>
#include <functional>
#include <memory>

/**
    Identification tree classifier:
        will use either entropy or the gini impurity measure
        for quantifying information

    Test function remarks:
        we are using a map to store children and not a vector
        because we want to be able to index the child nodes using
        the test function values directly, instead of having to
        restring the test function to output only integers in the
        [0,children.size()) range

*/

enum class InfoType { entropy, gini};

template<typename Scalar>
std::ostream& operator<<(std::ostream& os, std::vector<Scalar>& v) {
        for(int i = 0; i < v.size(); i++) {
            os<<v[i]<<" ";
        }
        return os;
    }

template<typename Categ, typename Scalar>
struct Element {
    //one element, for storing information in Row-Major form
    Categ category;
    std::vector<Scalar> features;
    Element& operator=(Element& el) {
        category = el.category;
        features = el.features;
    }
};

template<typename Scalar>
struct Test {
    double info_gain;
    int feature_index;
    std::function<unsigned (const std::vector<Scalar>& )> test_fn;
    //test function that will subset feature vector by one index

    unsigned operator()(const std::vector<Scalar>& f) { return test_fn(f); }
    Test(double info, int feature, std::function<unsigned (const std::vector<Scalar>& )> test)
        : info_gain {info}, feature_index {feature}, test_fn {test} {};
};

template<typename Categ, typename Scalar>
class InputMatrix {
    public :
        Element<Categ,Scalar>& operator[](int i) { return inputs[i]; }
        void push_back(Element<Categ,Scalar>& elem) { inputs.push_back(elem); }
        Element<Categ,Scalar>* begin() { return inputs.begin(); }
        Element<Categ,Scalar>* end() { return inputs.end(); }
        size_t size() { return inputs.size();}
    private :
        std::vector<Element<Categ,Scalar>> inputs;

};



template<typename Categ, typename Scalar>
class Node {
    public:
        Node(Node* parent_node, unsigned num_states,
             //the number of states the test function can divide the data into
             std::function<Scalar (const std::vector<Scalar>& )> test_fn)
             : parent {parent_node}, test {test_fn}, label {},
                children { std::map<Scalar,std::unique_ptr<Node>>(num_states) }
             { };
        Node(Node* parent_node,
             Scalar test_value) //parent test value, used to index children vector when evaluating inputs
            : parent {parent_node}, test {nullptr},
            label {}, val_ptst {test_value},
            children {std::map<Scalar,std::unique_ptr<Node>> {}}
            {};

        //Function calling a node will return nullptr
        //to signify reaching a leaf node.
        Node* operator()(const Element<Categ,Scalar>& elem) {
                return (test) ? &(*children[test(elem.features)]) : nullptr;
        }

        void set_test(std::function<Scalar (const std::vector<Scalar>& )> test_fn) {
                test = test_fn;
            }

        Categ get_label() { return label; }
        void set_label(Categ& lb) { label = lb; }
        void set_label(Categ&& lb) { label = lb; }

        Node* comes_from() {return parent;}
        Node* get_child(Scalar s) { return &(*children[s]); }
        void link_child(Node* child) { children[child->val_ptst] = std::unique_ptr<Node> {child}; }
        int child_num() { return children.size();}


    private:

        std::map<Scalar,std::unique_ptr<Node>> children;
        Node* parent;
        std::function<Scalar (const std::vector<Scalar>& )> test;
        //evaluates the test hidden in this node, and
        //returns an index for 0 to N states that classifies
        //the data into one of the N + 1 states of the property
        //that it runs the test on

        Categ label; //only applicable for leaf nodes
        Scalar val_ptst; //value on parent test

};


template<typename Categ, typename Scalar>
class Tree  {
    public :
        Categ eval(Element<Categ,Scalar>& elem) {
            Node<Categ,Scalar>* cnt_node = &(*root);
            Node<Categ,Scalar>* next_node = nullptr;

            while(next_node = (*cnt_node)(elem)) {
                cnt_node = next_node;
            }

            return cnt_node->get_label();
        }

        using InternalMatrix = std::vector<Element<Categ,Scalar>*>;
        Tree(InputMatrix<Categ,Scalar>& inMx,InfoType inf_measure)
            : root { new Node<Categ,Scalar>(nullptr,{})},
              information_measure {inf_measure}
            {
                std::vector<bool> is_used (inMx[0].features.size(),false);
                //is_used[j] will record if feature j was already tested for

                InternalMatrix internal_mx;
                //vector of pointers to be the internal representation
                //used in elaborating the tree

                for(int i = 0; i < inMx.size(); i++) {
                    internal_mx.push_back(&inMx[i]);
                }

                generate_recursive(internal_mx,&(*root),is_used);
                //&*root is there because we want to pass a Node*,
                //not a std::unique_ptr<Node>

            }

        void generate_recursive(InternalMatrix& inMx,
                                Node<Categ,Scalar>* from,
                                std::vector<bool>& is_used)
            {
                std::vector<Test<Scalar>> tst_vect{};
                double start_info = information(inMx);

                //generate tests
                for(int i = 0; i < is_used.size(); i++) {
                    if(!is_used[i]) {
                        Test<Scalar> cnt_test (0.0, i,
                            [i](const std::vector<Scalar>& f) {
                                            return static_cast<unsigned>(f[i]); } );

                        tst_vect.push_back(cnt_test);
                    }
                }

                if(tst_vect.size() == 0) return; //catch base case

                //calculate efficiencies
                for(auto& cnt_test : tst_vect) {
                    std::unordered_map<unsigned,InternalMatrix> buckets;
                    for(auto input : inMx) {
                        buckets[cnt_test(input->features)].push_back(input);
                    }

                    double total_info = 0;
                    for(auto& matrix : buckets) {
                        total_info += information(matrix.second);
                    }

                    cnt_test.info_gain = start_info - total_info;
                }


                std::sort(tst_vect.begin(),tst_vect.end(),
                          [](Test<Scalar> a, Test<Scalar> b) {
                                return (a.info_gain > b.info_gain); });

                //mark best test
                is_used[tst_vect[0].feature_index] = true;
                from->set_test(tst_vect[0].test_fn);


                //for all buckets produced at this level,
                //generate children recursively
                std::unordered_map<unsigned,InternalMatrix> buckets;

                for(auto input : inMx) {
                        buckets[tst_vect[0](input->features)].push_back(input);
                    }

                for(auto inputMx = buckets.begin(); inputMx != buckets.end(); inputMx++) {
                        Node<Categ,Scalar>* cnt_child = new Node<Categ,Scalar>(from, //parent pointer
                                                        tst_vect[0](inputMx->second[0]->features)) ; //test value
                        from->link_child(cnt_child);
                        if(!information(inputMx->second)) {
                            //notice we have gained all information
                            //that can be gained in this subproblem,
                            //time to set them labels and return
                            cnt_child->set_label( inputMx->second[0]->category );
                            continue;
                        } else {
                            generate_recursive(inputMx->second,cnt_child,is_used);
                        }
                    }

                //we are heading back up the tree, ergo we must
                //mark current test as unused in the higher levels
                is_used[tst_vect[0].feature_index] = false;


            }


        double information(InternalMatrix& inMx)
            {
                switch(information_measure) {
                    case InfoType::gini :
                        return gini(inMx);
                    case InfoType::entropy :
                        return entropy(inMx);
                }
            }

        double entropy(InternalMatrix& inMx)
             {
                double entp = 0;

                std::unordered_map<unsigned,unsigned> out_buckets {};
                //out buckets contains a count
                //for each category filtered out by test function

                for(auto input : inMx)  {
                    out_buckets[static_cast<unsigned>(input->category)]++;
                }

                for(auto freq : out_buckets) {
                    //using entropy to asses information in buckets
                    double p = static_cast<double>(freq.second) / static_cast<double>(inMx.size());

                    entp -= (p * std::log2(p));
                }

                return entp;
            }

        double gini(InternalMatrix& inMx)
            {
                double gn = 0;
                std::unordered_map<unsigned,unsigned> out_buckets {};
                //out buckets contains a count
                //for each category filtered out by test function

                for(auto input : inMx) {
                    out_buckets[static_cast<unsigned>(input->category)]++;
                }

                std::vector<double> frequencies {};
                for(auto freq : out_buckets) {
                    frequencies.push_back(static_cast<double>(freq.second) / static_cast<double>(inMx.size()));
                }

                for(int i = 0; i < frequencies.size(); i++) {
                    for(int j = i + 1; j < frequencies.size(); j++) {
                        gn += frequencies[i] * frequencies[j];
                    }
                }

                return gn;
            }

    private :
        InfoType information_measure;
        std::unique_ptr<Node<Categ,Scalar>> root;
        //function to asses information level of inputMatrix

};



int main()
{

    Element<unsigned,unsigned> elem { 0 , {0,0,0,0,0 ,0,0,0,0,0 ,0,0}};
    InputMatrix<unsigned,unsigned> binary;

    for(int i = 0; i < 4096; i++) {
        elem.category = i;
        for(int j = 0; j < 12; j++) {
            elem.features[j] = (i & (1<<j))/(1<<j);
        }
        binary.push_back(elem);
    }

    Tree<unsigned,unsigned> bin_tree(binary,InfoType::entropy);

    for(int i = 0; i < 4096; i++) {
        elem.category = i;
        for(int j = 0; j < 12; j++) {
            elem.features[j] = (i & (1<<j)) / (1<<j);
        }
        if(511 == (bin_tree.eval(elem) % 512)) {
            std::cout<<bin_tree.eval(elem)<<'\n';
        }
    }

    return 0;
}
