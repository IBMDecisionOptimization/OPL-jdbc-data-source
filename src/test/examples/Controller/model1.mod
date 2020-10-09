tuple result {
  string a;
  float time;
}

float time;

execute {
	time = new Date().getTime();
}

{result} Result1 = 
  { <"result1", time> };
  
execute DISPLAY_RESULT1{
  writeln("Result1 = ",Result1)
  time = new Date().getTime();
}

{result} Result2 = 
  { <"result2", time> };
  
execute DISPLAY_RESULT2{
  writeln("Result2 = ",Result2)
}