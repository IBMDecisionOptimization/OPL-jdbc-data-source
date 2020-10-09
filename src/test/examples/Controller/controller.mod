
 main
 {	
	function loadAndSolve(model_mod, model_dat) {
		writeln("Solving " + model_mod);	
		var cplex1 = new IloCplex();
		var source1 = new IloOplModelSource(model_mod);
		var def1 = new IloOplModelDefinition(source1);
		var opl1 = new IloOplModel(def1, cplex1);
		var data1 = new IloOplDataSource(model_dat);
		
		opl1.settings.mainEndEnabled=true;
		opl1.addDataSource(data1);
		opl1.generate();
		
		if (cplex1.solve())
		{
			writeln("successfully solved " + model_mod);
			opl1.postProcess();      
		}
		else {writeln("No solution found for " + model_mod);}  
			
		opl1.end();
	}
	loadAndSolve("model1.mod", "model1.dat");
	loadAndSolve("model2.mod", "model2.dat");
 }