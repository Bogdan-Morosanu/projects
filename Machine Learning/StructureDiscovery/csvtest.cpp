#include <iostream>
#include <fstream>
#include <cstdlib>

#include "double.hh"
  
namespace csv {
  using dbl = double_::grammar;
  
  struct integer
    : pegtl::seq< pegtl::opt< pegtl::one<'-'>>,
		  pegtl::plus< pegtl::digit>, 
		  pegtl::opt<double_::exponent, pegtl::plus<pegtl::digit>>> {};
  
  struct grammar
    : pegtl::plus< pegtl::sor<dbl, integer>, pegtl::opt<pegtl::one<','> > >  {};
  
  template<typename Rule>
  struct action 
    : pegtl::nothing< Rule > {};
  
  /// template specialisation for doubles
  template<> struct action<dbl> {
    static void apply(const pegtl::input& in, std::vector<double>& out) {
      out.push_back(std::stod(in.string()));
    }
  };


  /// template specialisation for integers
  template<> struct action<integer> {
    static void apply(const pegtl::input& in, std::vector<double>& out) {
      out.push_back(std::stod(in.string()));
    }
  };
}

template<typename T>
std::ostream& operator << (std::ostream& o, std::vector<T> v) {
  for(auto& x : v) {
    o << x << " ";
  }
  return o;
}

int main(int argc, char **argv) {
  if(argc != 2) {
    std::cout << "usage " << argv[0] << ": <csv-file>" << std::endl;
    std::exit(0);
  }
  
  std::ifstream input(argv[1]);
  while(!input.eof()) {
    std::string buf;
    std::getline(input, buf);
    std::vector<double> parsed;
    char *c = const_cast<char*>(buf.c_str());

    pegtl::parse<csv::grammar, csv::action>(0, &c, parsed);
    
    std::cout << parsed << std::endl;
  }

  return 0;
}
