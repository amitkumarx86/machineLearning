use WordNet::SenseRelate::AllWords;
use WordNet::QueryData;
use WordNet::Tools;

my $qd = WordNet::QueryData->new;
my $wntools = WordNet::Tools->new($qd);    
my %options = (wordnet => $qd,
               wntools => $wntools,
               measure => 'WordNet::Similarity::lesk'
               );

my $obj = WordNet::SenseRelate::AllWords->new(%options);
my $str = $ARGV[0];
my @context = $str;
my @res = $obj->disambiguate (window => 3,
                              scheme => 'normal',
                              tagged => 0,
                              context => [@context]);                                    
print join (' ', @res), "\n";
