tuple t {
   int a;
   int b;
   int c;
}

tuple r {
	int val;
}

{t} table1 = ...;
{t} table2 = ...;

{r} result1 = { <z+y+x> | <x,y,z> in table1 };
{r} result2 = { <z*y*x> | <x,y,z> in table2 };

execute {
  writeln("table1 = ",table1);
  writeln("result1 = ",result1);
  writeln("table2 = ",table2);
  writeln("result2 = ",result2);
}
