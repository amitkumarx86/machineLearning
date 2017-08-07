#!/usr/bin/perl

# this script is computing lin similarity
use WordNet::Similarity::lin;

use WordNet::QueryData;


my $values = $ARGV[0];
my @arrValues = split / /, $values;
my $word1 = $arrValues[0];
my $pos1  = $arrValues[1];
my $sense1 = $arrValues[2];

my $word2 = $arrValues[3];
my $pos2  = $arrValues[4];
my $sense2 = $arrValues[5];

$word1 .="#".$pos1."#".$sense1;
$word2 .="#".$pos2."#".$sense2;

# print $word1.$word2;


my $wn = WordNet::QueryData->new();

my $mymeasure = WordNet::Similarity::lin->new($wn);

my $value = $mymeasure->getRelatedness($word1, $word2);

($error, $errorString) = $mymeasure->getError();

die "$errorString\n" if($error);

print "$value\n";
